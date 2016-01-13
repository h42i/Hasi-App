package org.hasi.apps.hasi;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.larswerkman.holocolorpicker.ColorPicker;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class DashboardFragment extends Fragment implements MqttManagerCallback {
    private ColorPicker picker;
    private final static int pickerCoolDownTime = 20;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dashboard_fragment, container, false);

        return view;
    }

    @Override
    public void connectionEstablished() {

    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

    }
}
