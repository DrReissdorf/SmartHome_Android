package home.sven.smarthome_android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import java.io.IOException;
import home.sven.smarthome_android.R;
import home.sven.smarthome_android.singleton.CommunicationHandler;

public class RelaisFragment extends Fragment {
    private CommunicationHandler communicationHandler;
    private Switch[] switches = new Switch[8];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v("SmartHome", "OnCreateView()");
        communicationHandler = CommunicationHandler.getInstance();
        return inflater.inflate(R.layout.fragment_relais, container, false);
    }

    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("SmartHome", "onActivityCreated()");

        switches[0] = (Switch)getView().findViewById(R.id.switch0);
        switches[1] = (Switch)getView().findViewById(R.id.switch1);
        switches[2] = (Switch)getView().findViewById(R.id.switch2);
        switches[3] = (Switch)getView().findViewById(R.id.switch3);
        switches[4] = (Switch)getView().findViewById(R.id.switch4);
        switches[5] = (Switch)getView().findViewById(R.id.switch5);
        switches[6] = (Switch)getView().findViewById(R.id.switch6);
        switches[7] = (Switch)getView().findViewById(R.id.switch7);

        while(communicationHandler.getCurrentSwitchStatus() == null) try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        initView(communicationHandler.getSwitchNames(),communicationHandler.getSwitchStatus());
    }

    public void updateSwitches(boolean[] switchStatus) {
        for(int i=0 ; i<switchStatus.length ; i++) {
            switches[i].setChecked(switchStatus[i]);
        }
    }

    private void initView(String[] switchNames, boolean[] switchStatus) {
        for(int i=0 ; i<switches.length ; i++) {
            if(i<switchNames.length) {
                switches[i].setText(switchNames[i]);
                switches[i].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        String switchText = ((Switch)view).getText().toString();

                        try {
                            CommunicationHandler.getInstance().sendCommand("relay%"+switchText);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                switches[i].setChecked(switchStatus[i]);
            } else {
                switches[i].setVisibility(View.INVISIBLE);
            }

        }
    }
}
