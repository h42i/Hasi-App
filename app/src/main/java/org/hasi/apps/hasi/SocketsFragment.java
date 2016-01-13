package org.hasi.apps.hasi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.eclipse.paho.client.mqttv3.*;

public class SocketsFragment extends Fragment implements MqttManagerCallback {
    private Switch switch1;
    private Switch switch2;
    private Switch switch3;
    private Switch switch4;
    private Switch switch5;

    private final static int switch1RealNum = 2;
    private final static int switch2RealNum = 7;
    private final static int switch3RealNum = 4;
    private final static int switch4RealNum = 5;
    private final static int switch5RealNum = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sockets_fragment, container, false);

        this.switch1 = (Switch) view.findViewById(R.id.sockets_switch1);
        this.switch2 = (Switch) view.findViewById(R.id.sockets_switch2);
        this.switch3 = (Switch) view.findViewById(R.id.sockets_switch3);
        this.switch4 = (Switch) view.findViewById(R.id.sockets_switch4);
        this.switch5 = (Switch) view.findViewById(R.id.sockets_switch5);

        // entrance ceiling
        this.switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch1.getTag() == null) {
                    switchSocket(switch1RealNum, isChecked);
                }
            }
        });

        // entrance infoscreen
        this.switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch2.getTag() == null) {
                    switchSocket(switch2RealNum, isChecked);
                }
            }
        });

        // lamp pole
        this.switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch3.getTag() == null) {
                    switchSocket(switch3RealNum, isChecked);
                }
            }
        });

        // wall spotlight
        this.switch4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch4.getTag() == null) {
                    switchSocket(switch4RealNum, isChecked);
                }
            }
        });

        // torch
        this.switch5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switch5.getTag() == null) {
                    switchSocket(switch5RealNum, isChecked);
                }
            }
        });

        MqttManager.getInstance().addTopic("hasi/sockets/" + switch1RealNum + "/get");
        MqttManager.getInstance().addTopic("hasi/sockets/" + switch2RealNum + "/get");
        MqttManager.getInstance().addTopic("hasi/sockets/" + switch3RealNum + "/get");
        MqttManager.getInstance().addTopic("hasi/sockets/" + switch4RealNum + "/get");
        MqttManager.getInstance().addTopic("hasi/sockets/" + switch5RealNum + "/get");

        MqttManager.getInstance().addCallback(this);

        getAllSockets();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getAllSockets();
    }

    private void switchSocket(int socket, boolean on) {
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    String topic = "hasi/sockets/" + params[0].toString() + "/set";
                    MqttMessage message = new MqttMessage((params[1] != 0 ? "on" : "off").getBytes());

                    MqttManager.getInstance().getClient().publish(topic, message);

                    Log.d("Hasi-App", "Turning socket " + params[0].toString() + " " + (params[1] != 0 ? "on" : "off"));

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

    private void getAllSockets() {
        getSocket(switch1RealNum);
        getSocket(switch2RealNum);
        getSocket(switch3RealNum);
        getSocket(switch4RealNum);
        getSocket(switch5RealNum);
    }

    private void getSocket(int socket) {
        if (MqttManager.getInstance().getClient().isConnected()) {
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
    }

    @Override
    public void connectionEstablished() {
        getAllSockets();
    }

    @Override
    public void connectionLost(Throwable throwable) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        final String myTopic = topic;
        final MqttMessage myMQTTMessage = mqttMessage;

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean on = new String(myMQTTMessage.getPayload()).equals("on");

                    switch (myTopic) {
                        case "hasi/sockets/" + switch1RealNum + "/get":
                            switch1.setTag("stateInfo");
                            switch1.setChecked(on);
                            switch1.setTag(null);
                            break;

                        case "hasi/sockets/" + switch2RealNum + "/get":
                            switch2.setTag("stateInfo");
                            switch2.setChecked(on);
                            switch2.setTag(null);
                            break;

                        case "hasi/sockets/" + switch3RealNum + "/get":
                            switch3.setTag("stateInfo");
                            switch3.setChecked(on);
                            switch3.setTag(null);
                            break;

                        case "hasi/sockets/" + switch4RealNum + "/get":
                            switch4.setTag("stateInfo");
                            switch4.setChecked(on);
                            switch4.setTag(null);
                            break;

                        case "hasi/sockets/" + switch5RealNum + "/get":
                            switch5.setTag("stateInfo");
                            switch5.setChecked(on);
                            switch5.setTag(null);
                            break;
                    }
                }
            });
        }
    }
}
