package in.skylinelabs.myapplication;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by Reshul Dani on 06-04-2017.
 */
public interface IMessageCallback {

    /**
     * This method is called when the connection to the server is lost.
     *
     * @param cause the reason behind the loss of connection.
     */
    void connectionLost(Throwable cause);

    /**
     * This method is called when a message arrives from the server.
     *
     * @param topic       name of the topic on the message was published to
     * @param mqttMessage the actual message
     * @throws Exception
     */
    void messageArrived(String topic, MqttMessage mqttMessage) throws Exception;

    /**
     * Called when delivery for a message has been completed, and all acknowledgments have been received.
     *
     * @param messageToken he delivery token associated with the message.
     */
    void deliveryComplete(IMqttDeliveryToken messageToken);

    /**
     * Called when connection is established
     *
     * @param iMqttToken token for this connection
     */
    void onConnectionSuccess(IMqttToken iMqttToken);

    /**
     * Called when connection has failed
     *
     * @param iMqttToken token when failure occured
     * @param throwable  exception
     */
    void onConnectionFailure(IMqttToken iMqttToken, Throwable throwable);

    /**
     * Called when disconnection is successfull
     *
     * @param iMqttToken token for this connection
     */
    void onDisconnectionSuccess(IMqttToken iMqttToken);

    /**
     * Called when disconnection failed
     *
     * @param iMqttToken token when failure occured
     * @param throwable  exception
     */
    void onDisconnectionFailure(IMqttToken iMqttToken, Throwable throwable);
}