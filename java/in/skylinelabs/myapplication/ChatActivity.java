package in.skylinelabs.myapplication;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class ChatActivity extends ActionBarActivity  implements AsyncTaskComplete {

    private EditText messageET, edt;
    private ListView messagesContainer;
    private ImageView sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    private DatabaseHandler db;
    protected static final int RESULT_SPEECH = 1;
    int hot = 0;

    ArrayList<String> Rooms;
    ArrayList<String> Devices;
    private ActionHandler actionHandler;

    private static final String [] DANGEROUS_PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /*private void initPermissions() {
        List<String> missingPermissions = new ArrayList<String>();
        for(String permission : DANGEROUS_PERMISSIONS) {
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (missingPermissions.size() > 0) {
            String [] permissions = new String[missingPermissions.size()];
            ActivityCompat.requestPermissions(
                    this,
                    missingPermissions.toArray(permissions),
                    1);
        } else {
            // we have all permissions, move on
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_chat_main);
        actionHandler = new ActionHandler(this, ChatActivity.this);
        //initPermissions();
        initControls();

        ImageView mic = (ImageView) findViewById(R.id.micButton);
        mic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    edt = (EditText) findViewById(R.id.messageEdit);
                    edt.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Ops! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
        actionHandler.get_rooms(getResources().getString(R.string.projectname));
        Rooms = new ArrayList<String>();
        Devices = new ArrayList<String>();
        loadHistory();
        DisplayContent("Hey! Kym here.");
        //actionHandler.getUserSummary(username);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initControls() {
        db = new DatabaseHandler(this);
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (ImageView) findViewById(R.id.sendButton);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                chatMessage.setMessage(messageText);
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setTag(1);

                messageET.setText("");
                Log.d("Insert: ", "Inserting ..");
                db.addtodatabase(chatMessage);
                displayMessage(chatMessage);
                String[] tokens = messageText.split(" ");

                if(hot == 1){
                    String room = "";
                    for (String token : tokens) {
                        for (String sroom : Rooms) {
                            if (token.matches(sroom)) {
                                room = sroom;
                                break;
                            }
                        }
                    }
                    //get devices in room
                    actionHandler.get_things(getResources().getString(R.string.projectname), room);

                    hot = 0;
                }
                else {
                    String keys[] = {"dark", "turn"};
                    String main_key = "";
                    for (String token : tokens) {
                        for (String key : keys) {
                            if (token.matches(key)) {
                                main_key = key;
                                break;
                            }
                        }
                    }

                    if (main_key.matches("turn")) {
                        String on_off = "";
                        String room = "";
                        String device = "";
                        int at_flag = 0;
                        String at = "";
                        int i = 0;
                        for (String token : tokens) {
                            if (token.matches("turn")) {
                                on_off = tokens[i + 1];
                            }
                            if (token.matches("at")) {
                                at = tokens[i + 1];
                                at_flag = 1;
                            }
                            for (String sroom : Rooms) {
                                if (token.matches(sroom)) {
                                    room = sroom;
                                    device = tokens[i + 1];
                                }
                            }
                            i++;
                        }
                        if(at_flag == 1) {
                            String topic = getResources().getString(R.string.projectname) + "/" + room + "/" + device;
                            String prefix = "$state/";
                            String republish_topic = prefix + topic + "/update";
                            String payload = "{`state`:`"+on_off+"`}";
                            String payload_rep = payload.replace('`', '"');
                            actionHandler.send_time_rule(get_time(at), republish_topic,on_off);
                            DisplayContent("Time Rule set!");
                            Log.i("Time rule", get_time(at)+ " " + republish_topic + " " + payload_rep);
                        }
                        else {
                            String topic = getResources().getString(R.string.projectname) + "/" + room + "/" + device;
                            publish(topic, on_off);
                            DisplayContent("Ok! Turned it "+on_off);
                        }
                        at_flag = 0;
                    } else if (main_key.matches("dark")) {
                        DisplayContent("Which Room?");
                        DisplayContent(Rooms.toString());
                        hot = 1;
                    }
                }
            }
        });
        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();

    }
    public void DisplayContent(String message1)
    {
        ChatMessage m = new ChatMessage();
        m.setMessage(message1);
        m.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        m.setTag(0);
        db.addtodatabase(m);
        displayMessage(m);
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    edt.setText(text.get(0));
                }
                break;
            }

        }
    }


    private void loadHistory(){

        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);
        Log.d("Reading: ", "Reading all contacts..");
        List<ChatMessage> cm = db.getAllMessages();

        for (ChatMessage cn : cm) {
            String log = "Id: " + cn.getId() + " ,Date: " + cn.getDate() + " ,Message: " + cn.getMessage() + " ,Tag: " + cn.getTag();
            Log.d("Row: ", log);
        }
        for(int i=0; i<cm.size(); i++) {
            ChatMessage message = cm.get(i);
            displayMessage(message);
        }

    }
    public String get_time(String at) {
        String time = "";
        String[] stimes = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven",
        "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty", "twentyone", "twentytwo", "twentythree", "twentyfour"};
        time = stimes[Integer.valueOf(at) - 1];
        return time;
    }
    public void publish(String topic, String state){
        try {
            String prefix = "$state/";
            String publish_topic = prefix + topic + "/update";
            String payload = "{\"state\":\""+state+"\"}";
            byte[] encodedPayload = new byte[0];
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("admin/a/a");
            options.setPassword("pass".toCharArray());
            MqttClient mqClient = new MqttClient("tcp://35.162.23.96:1883", "HomeAutomation/android/activity", new MemoryPersistence());
            mqClient.connect(options);
            mqClient.publish(publish_topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void handleResult(JsonObject result, String action) throws JSONException {

        switch (action) {
            case "Rooms":
                String rooms = result.get("items").getAsString();
                Log.i("Chat Jsonresponse", rooms);
                List<String> roomlist = Arrays.asList(rooms.split(","));
                boolean b = roomlist.get(0).equals("");
                Log.i("first", Boolean.toString(b));
                //DisplayContent(result.get("msg").getAsString());
                for(String rm :roomlist){
                    if(!rm.equals("") && !rm.equals("android")){
                        Log.i("object", rm);
                        Rooms.add(rm);
                    }
                }
                break;
            case "Things" :
                 JsonArray Things_arr = result.get("items").getAsJsonArray();
                for(int i = 0; i < Things_arr.size(); i++) {
                    String id = Things_arr.get(i).getAsJsonObject().get("id").getAsString();
                    Devices.add(id);
                    Log.i("Devices", Devices.get(i));
                }
                Log.i("size", String.valueOf(Devices.size()));
                for (int i = 0; i < Devices.size(); i++) {
                    List<String> namesplit = Arrays.asList(Devices.get(i).split("/"));
                    Log.i("Split id", namesplit.toString());
                    if(namesplit.get(2).equals("bulb")){
                        Log.i("Publish id", namesplit.get(2));
                        publish(Devices.get(i), "on");
                        DisplayContent("Turned on the " + namesplit.get(1) + " " + namesplit.get(2));
                    }
                }

                Log.i("Json response", Things_arr.toString());
                break;

        }

    }
}
