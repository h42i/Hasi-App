package org.hasi.apps.hasi;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LedStripesActivity extends AppCompatActivity implements MqttManagerCallback {
    private ColorPicker picker;
    private final static int pickerCoolDownTime = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_stripes);

        this.picker = (ColorPicker) findViewById(R.id.led_stripes_picker);

        SaturationBar saturationBar = (SaturationBar) findViewById(R.id.led_stripes_saturationbar);
        ValueBar valueBar = (ValueBar) findViewById(R.id.led_stripes_valuebar);

        this.picker.addSaturationBar(saturationBar);
        this.picker.addValueBar(valueBar);

        MqttManager.getInstance().addTopic("hasi/lights/stripes/set_rgb");
        MqttManager.getInstance().addCallback(this);

        // whysoever this is a little bit slow
        this.picker.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                new AsyncTask<Integer, Void, Void>() {
                    @Override
                    protected Void doInBackground(Integer... colors)
                    {
                        MqttMessage message = new MqttMessage(String.format("%05X", colors[0] & 0xFFFFFF).getBytes());

                        try {
                            MqttManager.getInstance().getClient().publish("hasi/lights/stripes/set_rgb", message);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                    }
                }.execute(color);
            }
        });
    }

    @Override
    public void connectionEstablished() {

    }

    @Override
    public void connectionLost(Throwable throwable) {
    }

    @Override
    public void messageArrived(String topic, final MqttMessage mqttMessage) throws Exception {
        if (topic.equals("hasi/lights/stripes/set_rgb")) {
            final ColorPicker thisPicker = this.picker;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String hexColorString = new String(mqttMessage.getPayload());
                        int color = Integer.parseInt(hexColorString, 16);
                        thisPicker.setOldCenterColor(Color.argb(0xFF, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
