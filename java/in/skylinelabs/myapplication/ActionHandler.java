package in.skylinelabs.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;


public class ActionHandler {
    AsyncTaskComplete callback;
    ProgressDialog progressDialog;
    private Context context;
    private String ROOMS_URL = "http://54.69.11.206/test1.php";
    private String THINGS_URL = "http://54.69.11.206/get_things.php";
    private String TIME_RULES_URL = "http://54.69.11.206/set_time_rule.php";


    public ActionHandler(Context context, AsyncTaskComplete callback) {
        this.callback = callback;
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    public void send_time_rule(String collection, String republish_topic, String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("collection", collection);
        jsonObject.addProperty("topic", republish_topic);
        jsonObject.addProperty("message", message);
        postJsonObject(jsonObject, TIME_RULES_URL, "TimeRule", "");

    }
        public void get_rooms(String projectname) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("projectname", projectname);
        postJsonObject(jsonObject, ROOMS_URL, "Rooms", "");
    }

    public void get_things(String projectname, String roomname) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("projectname", projectname);
        jsonObject.addProperty("type", roomname);
        postJsonObject(jsonObject, THINGS_URL, "Things", "");
    }


    private void postJsonObject(final JsonObject jsonObject, String url, final String action, String progress_status) {
        HttpJsonPost httpJsonPost = new HttpJsonPost(url, action, progress_status, context, callback);
        httpJsonPost.execute(jsonObject);

    }

}
