package in.skylinelabs.myapplication;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Reshul Dani on 06-04-2017.
 */
public class MqttHandler {

    private static final String TAG = "TAg";
    /**
     * MQTT client
     */
    private MqttAndroidClient mClient = null;

    /**
     * client ID used to authenticate
     */
    protected String mClientId = "";

    /**
     * Android context
     */
    private Context mContext = null;

    /**
     * callback for MQTT events
     */
    private MqttCallback mClientCb = null;

    /**
     * callback for MQTT connection
     */
    private IMqttActionListener mConnectionCb = null;

    /**
     * Sets whether the client and server should remember state across restarts and reconnects
     */
    protected boolean mCleanSessionDefault = false;

    /**
     * Sets the connection timeout value (in seconds)
     */
    protected int mTimeoutDefault = 30;

    /**
     * Sets the "keep alive" interval (in seconds)
     */
    protected int mKeepAliveDefault = 60;

    /**
     * connection state
     */
    private boolean connected = false;

    /**
     * list of message callbacks
     */
    private List<IMessageCallback> mMessageCallbacksList = new ArrayList<>();

    private final static String SERVER_URI = "broker.hivemq.com";

    private final static int SERVER_PORT = 1883;

    public MqttHandler(Context context) {

        this.mContext = context;

        this.mClientCb = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                connected = false;
                for (int i = 0; i < mMessageCallbacksList.size(); i++) {
                    mMessageCallbacksList.get(i).connectionLost(cause);
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                for (int i = 0; i < mMessageCallbacksList.size(); i++) {
                    mMessageCallbacksList.get(i).messageArrived(topic, mqttMessage);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                for (int i = 0; i < mMessageCallbacksList.size(); i++) {
                    mMessageCallbacksList.get(i).deliveryComplete(token);
                }
            }
        };
    }

    public boolean isConnected() {
        if (mClient == null)
            return false;
        else
            return connected;
    }

    public void connect() {

        try {
            if (!isConnected()) {

                MqttConnectOptions options = new MqttConnectOptions();

                options.setCleanSession(mCleanSessionDefault);
                options.setConnectionTimeout(mTimeoutDefault);
                options.setKeepAliveInterval(mKeepAliveDefault);

                mClient = new MqttAndroidClient(mContext, "tcp://" + SERVER_URI + ":" + SERVER_PORT, mClientId);
                mClient.setCallback(mClientCb);

                mConnectionCb = new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        connected = true;
                        for (int i = 0; i < mMessageCallbacksList.size(); i++) {
                            mMessageCallbacksList.get(i).onConnectionSuccess(iMqttToken);
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        connected = false;
                        for (int i = 0; i < mMessageCallbacksList.size(); i++) {
                            mMessageCallbacksList.get(i).onConnectionFailure(iMqttToken, throwable);
                        }
                    }
                };

                try {
                    mClient.connect(options, mContext, mConnectionCb);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            } else {
                Log.v(TAG, "cant connect - already connected");
            }
        } catch (IllegalArgumentException e) {
            Log.v(TAG, "parameters error. cant connect");
        }
    }

    public void disconnect() {

        if (isConnected()) {
            try {
                mClient.disconnect(mContext, mConnectionCb);

            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            Log.v(TAG, "cant disconnect - already disconnected");
        }
    }

    /**
     * Publish a message to MQTT server
     *
     * @param topic      message topic
     * @param message    message body
     * @param isRetained define if message should be retained on MQTT server
     * @param listener   completion listener (null allowed)
     * @return
     */
    public IMqttDeliveryToken publishMessage(String topic, String message, boolean isRetained, IMqttActionListener listener) {

        if (isConnected()) {

            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setRetained(isRetained);
            mqttMessage.setQos(0);

            try {
                return mClient.publish(topic, mqttMessage, mContext, listener);
            } catch (MqttPersistenceException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "cant publish message. Not connected");
        }
        return null;
    }

    /**
     * Subscribe to topic
     *
     * @param topic    topic to subscribe
     * @param listener completion listener (null allowed)
     * @return
     */
    public void subscribe(String topic, IMqttActionListener listener) {

        if (isConnected()) {
            try {
                mClient.subscribe(topic, 0, mContext, listener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "cant publish message. Not connected");
        }
    }

    /**
     * Unsubscribe a topic
     *
     * @param topic    topic to unsubscribe
     * @param listener completion listener (null allowed)
     */
    public void unsubscribe(String topic, IMqttActionListener listener) {

        if (isConnected()) {
            try {
                mClient.unsubscribe(topic, mContext, listener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "cant publish message. Not connected");
        }
    }

    public void addCallback(IMessageCallback callback) {
        mMessageCallbacksList.add(callback);
    }
}

