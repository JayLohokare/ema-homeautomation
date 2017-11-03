package in.skylinelabs.myapplication;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;


public class RoomWise extends ActionBarActivity implements AsyncTaskComplete{

    private ListAdapter adapter;
    private ListView listView;
    public RowListHandler rowListHandler;
    private ActionHandler actionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_wise);

        String room = getIntent().getStringExtra("Room");
        Toast.makeText(getApplicationContext(), room, Toast.LENGTH_LONG);
        Log.i("Room", room);
        actionHandler = new ActionHandler(RoomWise.this, this);
        rowListHandler = new RowListHandler();
        rowListHandler.clearAll();
        adapter = new ListAdapter(this, rowListHandler);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        actionHandler.get_things(getResources().getString(R.string.projectname), room);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room_wise, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(JsonObject result, String action) throws JSONException {

        JsonArray arr = result.get("items").getAsJsonArray();
        for(int i = 0; i < arr.size(); i++) {
            String id = arr.get(i).getAsJsonObject().get("id").getAsString();
            String state = arr.get(i).getAsJsonObject().get("state").getAsString();
            int st = 0;
            if(state.equals("on"))
                st = 1;
            rowListHandler.addRow(new RowObject(st, id));
            adapter.notifyDataSetChanged();
        }

    }
}
