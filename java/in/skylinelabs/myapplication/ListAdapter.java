package in.skylinelabs.myapplication;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MIHIR on 26-05-2016.
 */
public class ListAdapter extends BaseAdapter {
    private final Activity context;
    private RowListHandler rowListHandler;
    private LayoutInflater inflater;


    public ListAdapter(Activity context, RowListHandler rowListHandler) {
        this.context = context;
        this.rowListHandler = rowListHandler;
        inflater = LayoutInflater.from(context);

    }





    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        //LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row, null);
            holder = new ViewHolder();

            holder.device = (TextView) convertView.findViewById(R.id.device);
            holder.state = (Switch) convertView.findViewById(R.id.switch1);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();

        }

        String name = rowListHandler.getDevice(position);
        List<String> namesplit = Arrays.asList(name.split("/"));
        holder.device.setText(namesplit.get(2));
        holder.state.setChecked(rowListHandler.getState(position) == 1);

        holder.state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    String projectname = "$state/";
                    String topic = projectname + rowListHandler.getDevice(position) + "/update";
                    String st = "off";
                    if(holder.state.isChecked())
                        st = "on";
                    String payload = "{\"state\":\""+st+"\"}";
                    byte[] encodedPayload = new byte[0];
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setUserName("admin/a/a");
                    options.setPassword("pass".toCharArray());
                    MqttClient mqClient = new MqttClient("tcp://35.162.23.96:1883", "HomeAutomation/android/activity", new MemoryPersistence());
                    mqClient.connect(options);
                    mqClient.publish(topic, message);
                    Log.i("publish topic", topic);
                    Log.i("publish payloaad", message.toString());

                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        return convertView;
    }


    @Override
    public int getCount() {
        return rowListHandler.getListsize();
    }

    @Override
    public Object getItem(int position) {
        return rowListHandler.getRowListObject(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public RowListHandler getOrder() {
        return rowListHandler;
    }

    public void notifyDataSetChanged() {

        super.notifyDataSetChanged();

    }


    private class ViewHolder {
        TextView device;
        Switch state;
    }

}
