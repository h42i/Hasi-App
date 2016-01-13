package org.hasi.apps.hasi;

import android.os.AsyncTask;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.*;

import java.util.ArrayList;
import java.util.Random;

public class MqttManager implements MqttCallback {
    private static MqttManager instance = null;

    private static String broker = "tcp://atlas.hasi:1883";
    private static int qos = 2;

    private MemoryPersistence persistence;
    private MqttClient client;

    private ArrayList<MqttManagerCallback> callbacks;
    private ArrayList<String> topics;

    private MqttManager() throws MqttException {
        this.persistence = new MemoryPersistence();
        this.client = new MqttClient(broker, "Hasi-App-MQTT-" + new Random().nextInt(), persistence);

        this.callbacks = new ArrayList<MqttManagerCallback>();
        this.topics = new ArrayList<String>();

        this.client.setCallback(this);
    }

    public void connect() throws MqttException {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        try {
            this.client.connect(connOpts);
        } catch (MqttException e) {
            System.err.println("Error: Can't connect to " + broker);
        }

        for (String topic : this.topics) {
            this.client.subscribe(topic);
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                while (!client.isConnected()) {
                    try {
                        Thread.sleep(500, 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (MqttManagerCallback callback : callbacks) {
                    callback.connectionEstablished();
                }

                return null;
            }
        }.execute();
    }

    public void disconnect() throws MqttException {
        this.client.disconnect();
    }

    public MqttClient getClient() {
        return this.client;
    }

    public static String getBroker() {
        return broker;
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

    public void addCallback(MqttManagerCallback mqttManagerCallback) {
        this.callbacks.add(mqttManagerCallback);
    }

    public void addTopic(String topic) {
        if (this.client.isConnected()) {
            try {
                this.client.subscribe(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        this.topics.add(topic);
    }

    @Override
    public void connectionLost(Throwable throwable) {
        for (MqttManagerCallback callback : this.callbacks) {
            callback.connectionLost(throwable);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        for (MqttManagerCallback callback : this.callbacks) {
            callback.messageArrived(topic, mqttMessage);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
}
