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

        refreshButton = (Button) getView().findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTemperatures();
            }
        });

        updateTemperatures();
    }

    private void updateTemperatures() {
        String received = communicationHandler.getCurrentTempStatus();


        if(received == null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivity(),MainActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putString("server_down_window", "true");
                    intent.putExtras(mBundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    communicationHandler.close();
                    getActivity().startActivity(intent);
                }
            });
        } else {
            String[] tempInfo = received.split(";");
            textView0 = (TextView) getView().findViewById(R.id.tempText0);
            textView0.setText("Temperatur: "+getRoundedTemperature(tempInfo[0])+"°C");
        }
    }

    private String getRoundedTemperature(String temp) {
        float value = Math.round(Float.valueOf(temp)/100);
        return String.valueOf(value/10);
    }
}
