package home.sven.smarthome_android.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import home.sven.smarthome_android.R;
import home.sven.smarthome_android.activity.MainActivity;
import home.sven.smarthome_android.singleton.CommunicationHandler;

public class TemperatureFragment extends Fragment {
    private CommunicationHandler communicationHandler;
    private TextView textView0;
    private Button refreshButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //FloatingActionButton fab = (FloatingActionButton)((MainActivity)getActivity()).findViewById(R.id.fab);
        //fab.setVisibility(View.GONE);
        // Inflate the layout for this fragment
        communicationHandler = CommunicationHandler.getInstance();
        return inflater.inflate(R.layout.fragment_temperature, container, false);
    }

    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("SmartHome", "onActivityCreated()");

        while(communicationHandler.getCurrentTempStatus() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        updateTemperatures(communicationHandler.getCurrentTempStatus());
    }

    public void updateTemperatures(String[] info) {
        textView0 = (TextView) getView().findViewById(R.id.tempText0);
        textView0.setText("Temperature: "+getRoundedTemperature(info[0])+"°C");
    }

    private String getRoundedTemperature(String temp) {
        float value = Math.round(Float.valueOf(temp)/1000);
        return String.valueOf(value);
    }
}
