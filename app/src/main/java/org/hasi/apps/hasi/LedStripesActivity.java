package org.hasi.apps.hasi;

import android.graphics.Color;
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

public class LedStripesActivity extends AppCompatActivity implements MqttCallback {
    private ColorPicker picker;
    private final static int pickerCoolDownTime = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_stripes);

        this.picker = (ColorPicker) findViewById(R.id.led_stripes_picker);

        SVBar svBar = (SVBar) findViewById(R.id.led_stripes_svbar);
        SaturationBar saturationBar = (SaturationBar) findViewById(R.id.led_stripes_saturationbar);
        ValueBar valueBar = (ValueBar) findViewById(R.id.led_stripes_valuebar);

        this.picker.addSVBar(svBar);
        this.picker.addSaturationBar(saturationBar);
        this.picker.addValueBar(valueBar);

        this.picker.setShowOldCenterColor(false);

        MqttManager.getInstance().addTopic("hasi/lights/stripes/set_rgb");
        MqttManager.getInstance().addCallback(this);

        this.picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            boolean taskScheduled = false;
            ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

            @Override
            public void onColorChanged(int color) {
                if (!taskScheduled) {
                    MqttMessage message = new MqttMessage(String.format("%05X", color & 0xFFFFFF).getBytes());

                    try {
                        MqttManager.getInstance().getClient().publish("hasi/lights/stripes/set_rgb", message);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    worker.schedule(new Runnable() {
                        public void run() {
                            taskScheduled = false;
                        }
                    }, pickerCoolDownTime, TimeUnit.MILLISECONDS);

                    taskScheduled = true;
                }
            }
        });
    }

    @Override
    public void connectionLost(Throwable throwable) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        if (topic.equals("hasi/lights/stripes/set_rgb")) {
            // not working well
            /*try {
                String hexColorString = new String(mqttMessage.getPayload());
                int color = Integer.parseInt(hexColorString, 16);
                this.picker.setColor(Color.argb(0xFF, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF));
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
}
