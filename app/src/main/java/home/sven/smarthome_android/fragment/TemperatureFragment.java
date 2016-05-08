package home.sven.smarthome_android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import home.sven.smarthome_android.R;
import home.sven.smarthome_android.singleton.CommunicationHandler;

public class TemperatureFragment extends Fragment {
    private CommunicationHandler communicationHandler;
    private ArrayList<TextView> textViews;
    private LinearLayout linearLayout;

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

        textViews = new ArrayList<>();
        linearLayout = (LinearLayout)getView().findViewById(R.id.temperatureLinearLayout);

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
        if(textViews != null) {
            TextView textView;

            while(info.length > textViews.size()) {
                textView = new TextView(getActivity());
                textViews.add(textView);
                linearLayout.addView(textView);
            }

            while(info.length < textViews.size()) {
                textView = textViews.get(textViews.size()-1);
                textViews.remove(textView);
                linearLayout.removeView(textView);
            }
        }

        for(int i=0 ; i<textViews.size() ; i++) {
            textViews.get(i).setText("  "+getRoundedTemperature(info[i]) + "°C");
        }
    }

    private String getRoundedTemperature(String temp) {
        float value = Math.round(Float.valueOf(temp)/1000);
        return String.valueOf(value);
    }
}
