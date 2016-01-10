package org.hasi.apps.hasi;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MqttManager implements MqttCallback {
    private static MqttManager instance = null;

    private static String broker = "tcp://atlas.hasi:1883";
    private static int qos = 2;

    private MemoryPersistence persistence;
    private MqttClient client;

    private ArrayList<MqttCallback> callbacks;
    private ArrayList<String> topics;

    private MqttManager() throws MqttException {
        this.persistence = new MemoryPersistence();
        this.client = new MqttClient(broker, "testClient", persistence);

        this.callbacks = new ArrayList<MqttCallback>();
        this.topics = new ArrayList<String>();

        this.client.setCallback(this);
    }

    public void connect() throws MqttException {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        this.client.connect(connOpts);

        for (String topic : this.topics) {
            this.client.subscribe(topic);
        }
    }

    public void disconnect() throws MqttException {
        this.client.disconnect();
    }

    public MqttClient getClient() {
        return this.client;
    }

    public static MqttManager getInstance() {
        if (MqttManager.instance == null) {
            try {
                MqttManager.instance = new MqttManager();
            } catch (MqttException e) {
                e.printStackTrace();

                return null;
            }
        }

        return instance;
    }

    public void addCallback(MqttCallback mqttCallback) {
        this.callbacks.add(mqttCallback);
    }

    public void addTopic(String topic) {
        this.topics.add(topic);
    }

    @Override
    public void connectionLost(Throwable throwable) {
        for (MqttCallback callback : this.callbacks) {
            callback.connectionLost(throwable);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        for (MqttCallback callback : this.callbacks) {
            callback.messageArrived(topic, mqttMessage);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        for (MqttCallback callback : this.callbacks) {
            callback.deliveryComplete(iMqttDeliveryToken);
        }
    }
}
