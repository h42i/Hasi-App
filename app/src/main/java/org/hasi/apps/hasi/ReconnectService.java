package org.hasi.apps.hasi;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ReconnectService extends Service {
    public static final long NOTIFY_INTERVAL = 5 * 1000;

    private Handler handler = new Handler();
    private Timer timer = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (this.timer != null) {
            this.timer.cancel();
        } else {
            this.timer = new Timer();
        }

        this.timer.scheduleAtFixedRate(new ReconnectTask(), 0, NOTIFY_INTERVAL);
    }

    class ReconnectTask extends TimerTask {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MqttClient client = MqttManager.getInstance().getClient();

                    if (!client.isConnected()) {
                        try {
                            MqttManager.getInstance().connect();
                        } catch (MqttException e) {
                            System.err.println("Error: Can't connect to " + MqttManager.getInstance().getBroker());
                        }
                    }
                }

            });
        }
    }
}
