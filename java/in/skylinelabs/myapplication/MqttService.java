package in.skylinelabs.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by Reshul Dani on 06-04-2017.
 */
public class MqttService extends Service implements MqttCallback {

    private final IBinder localBinder = new FollowServiceBinder();
    private final String TAG = "Service";
    private MqttClient mqClient;
    private static final String activityClass = "skylinelabs.in.test.MainActivity";

    public class FollowServiceBinder extends Binder {
        public MqttService getService() {
            return MqttService.this;
        }
    }

    public MqttService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(getResources().getString(R.string.username));
            options.setPassword(getResources().getString(R.string.password).toCharArray());
            MqttClient mqClient = new MqttClient(getResources().getString(R.string.server_uri), getResources().getString(R.string.client_id_service)+System.currentTimeMillis(), new MemoryPersistence());
            mqClient.connect(options);
            mqClient.setCallback(this);
            Log.i(TAG, "Connected to client");
            Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
            String topic = getResources().getString(R.string.service_topic);
            mqClient.subscribe(topic);
            Log.i(TAG, "Subscribed "+topic);

        }
        catch(MqttException me){
            Log.e(TAG, "MqttClient Exception Occured in on create!!!", me);
            Toast.makeText(this, "Error Connecting", Toast.LENGTH_LONG).show();

        }
    }


    @Override
    public void connectionLost(Throwable cause) {
        Log.i(TAG, "ConnectionLost");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "Delivered");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();
        Log.i(TAG, message.toString());
        Intent intent = new Intent();
        intent.setClassName(getApplicationContext(), activityClass);
        notifcation(getApplicationContext(), message.toString(), topic, intent, 1);

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        //Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    static void notifcation(Context context, String messageString, String topic, Intent intent, int notificationTitle) {

        //Get the notification manage which we will use to display the notification
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

        long when = System.currentTimeMillis();



        //the message that will be displayed as the ticker
        String ticker = "Message" + " " + messageString;

        //build the pending intent that will start the appropriate activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, intent, 0);

        //build the notification
        Notification.Builder notificationCompat = new Notification.Builder(context);
        notificationCompat.setAutoCancel(true)
                .setContentTitle(topic)
                .setContentIntent(pendingIntent)
                .setContentText(messageString)
                .setTicker(ticker)
                .setWhen(when)
                .setSmallIcon(R.mipmap.ic_launcher);

        Notification notification = notificationCompat.build();
        //display the notification
        mNotificationManager.notify(1, notification);

    }

}