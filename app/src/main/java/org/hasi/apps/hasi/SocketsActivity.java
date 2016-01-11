package org.hasi.apps.hasi;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SocketsActivity extends AppCompatActivity implements MqttCallback {
    private Switch switch1;
    private Switch switch2;
    private Switch switch3;
    private Switch switch4;
    private Switch switch5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sockets);

        this.switch1 = (Switch) findViewById(R.id.sockets_switch1);
        this.switch2 = (Switch) findViewById(R.id.sockets_switch2);
        this.switch3 = (Switch) findViewById(R.id.sockets_switch3);
        this.switch4 = (Switch) findViewById(R.id.sockets_switch4);
        this.switch5 = (Switch) findViewById(R.id.sockets_switch5);

        // entrance ceiling
        this.switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch1.getTag() == null) {
                    switchSocket(2, isChecked);
                }
            }
        });

        // entrance infoscreen
        this.switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch2.getTag() == null) {
                    switchSocket(7, isChecked);
                }
            }
        });

        // lamp pole
        this.switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch3.getTag() == null) {
                    switchSocket(5, isChecked);
                }
            }
        });

        // wall spotlight
        this.switch4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch4.getTag() == null) {
                    switchSocket(4, isChecked);
                }
            }
        });

        // torch
        this.switch5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch5.getTag() == null) {
                    switchSocket(5, isChecked);
                }
            }
        });

        MqttManager.getInstance().addTopic("hasi/sockets/1/get");
        MqttManager.getInstance().addTopic("hasi/sockets/2/get");
        MqttManager.getInstance().addTopic("hasi/sockets/3/get");
        MqttManager.getInstance().addTopic("hasi/sockets/4/get");
        MqttManager.getInstance().addTopic("hasi/sockets/5/get");

        MqttManager.getInstance().addCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        getSocket(1);
        getSocket(2);
        getSocket(3);
        getSocket(4);
        getSocket(5);
    }

    private void switchSocket(int socket, boolean on) {
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... params) {
                MqttMessage message = new MqttMessage((params[1] != 0 ? "on" : "off").getBytes());

                try {
                    MqttManager.getInstance().getClient().publish("hasi/sockets/" + params[0].toString() + "/set", message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute(socket, on ? 1 : 0);
    }

    private void getSocket(int socket) {
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... params) {
                MqttMessage message = new MqttMessage("tellmeyourstate".getBytes());

                try {
                    MqttManager.getInstance().getClient().publish("hasi/sockets/" + params[0].toString() + "/set", message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute(socket);
    }

    @Override
    public void connectionLost(Throwable throwable) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        final String myTopic = topic;
        final MqttMessage myMQTTMessage = mqttMessage;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean on = new String(myMQTTMessage.getPayload()).equals("on");
                    System.out.println(on);

                    switch (myTopic) {
                        case "hasi/sockets/1/get":
                            switch1.setTag("stateInfo");
                            switch1.setChecked(on);
                            switch1.setTag(null);
                            break;

                        case "hasi/sockets/2/get":
                            switch2.setTag("stateInfo");
                            switch2.setChecked(on);
                            switch2.setTag(null);
                            break;

                        case "hasi/sockets/3/get":
                            switch3.setTag("stateInfo");
                            switch3.setChecked(on);
                            switch3.setTag(null);
                            break;

                        case "hasi/sockets/4/get":
                            switch4.setTag("stateInfo");
                            switch4.setChecked(on);
                            switch4.setTag(null);
                            break;

                        case "hasi/sockets/5/get":
                            switch5.setTag("stateInfo");
                            switch5.setChecked(on);
                            switch5.setTag(null);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
}
