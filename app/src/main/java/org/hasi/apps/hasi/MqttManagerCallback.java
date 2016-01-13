package org.hasi.apps.hasi;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MqttManagerCallback {
    public void connectionEstablished();
    public void connectionLost(Throwable throwable);
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception;
}
