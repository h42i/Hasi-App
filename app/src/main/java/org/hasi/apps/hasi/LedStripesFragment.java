package org.hasi.apps.hasi;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class LedStripesFragment extends Fragment implements MqttManagerCallback {
    private ColorPicker picker;
    private final static int pickerCoolDownTime = 20;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.led_stripes_fragment, container, false);

        this.picker = (ColorPicker) view.findViewById(R.id.led_stripes_picker);

        SaturationBar saturationBar = (SaturationBar) view.findViewById(R.id.led_stripes_saturationbar);
        ValueBar valueBar = (ValueBar) view.findViewById(R.id.led_stripes_valuebar);

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

        return view;
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

            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
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
}
