package home.sven.smarthome_android.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import java.io.IOException;
import java.util.ArrayList;

import home.sven.smarthome_android.R;
import home.sven.smarthome_android.activity.MainActivity;
import home.sven.smarthome_android.singleton.CommunicationHandler;

public class RelaisFragment extends Fragment {
    private ArrayList<Switch> switchesArrayList;
    private UpdateSwitchesThread updateSwitchesThread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //FloatingActionButton fab = (FloatingActionButton)((MainActivity)getActivity()).findViewById(R.id.fab);
        //fab.setVisibility(View.GONE);
        // Inflate the layout for this fragment
        switchesArrayList = new ArrayList<>();

        updateSwitchesThread = new UpdateSwitchesThread();
        updateSwitchesThread.start();

        return inflater.inflate(R.layout.fragment_relais, container, false);
    }

    private void updateSwitches(String info[]) {
        Switch tempSwitch;
        String[] tempString;

        for(int i=0 ; i<info.length ; i++) {
            tempString = info[i].split(",");
            tempSwitch = switchesArrayList.get(i);

            tempSwitch.setText(tempString[0]);
            if(Boolean.parseBoolean(tempString[1])) tempSwitch.setChecked(true);
            else tempSwitch.setChecked(false);
        }
    }

    private void initView(String info[]) {
        String[] tempString;
        Switch[] switches = new Switch[4];

        switches[0] = (Switch)getView().findViewById(R.id.switch0);
        switches[1] = (Switch)getView().findViewById(R.id.switch1);
        switches[2] = (Switch)getView().findViewById(R.id.switch2);
        switches[3] = (Switch)getView().findViewById(R.id.switch3);

        for(int i=0 ; i<info.length ; i++) {
            tempString = info[i].split(",");

            switches[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    String switchText = ((Switch)view).getText().toString();
                    Log.v("Relay - Menu Activity", switchText+" switch tapped");

                    try {
                        CommunicationHandler.getInstance().sendCommand(switchText);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            switchesArrayList.add(switches[i]);

            if(Boolean.parseBoolean(tempString[1])) switches[i].setChecked(true);
            else switches[i].setChecked(false);
        }
    }

    private class UpdateSwitchesThread extends Thread {
        boolean exit = false;
        private String currentInfo;
        private String lastInfo;

        public void run() {
            while (CommunicationHandler.getInstance().getCurrentStatus() == null) {
                sleep(50);
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initView(CommunicationHandler.getInstance().getCurrentStatus().split(";"));
                }
            });

            lastInfo = "";

            while (!exit) {
                sleep(500);
                currentInfo = CommunicationHandler.getInstance().getCurrentStatus();

                if(currentInfo == null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getActivity(),MainActivity.class);
                            Bundle mBundle = new Bundle();
                            mBundle.putString("server_down_window", "true");
                            intent.putExtras(mBundle);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            CommunicationHandler.getInstance().close();
                            getActivity().startActivity(intent);
                        }
                    });
                    break;
                }

                if (!lastInfo.equals(currentInfo)) {
                    lastInfo = currentInfo;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateSwitches(currentInfo.split(";"));
                        }
                    });
                }
            }
        }

        public void exit() {
            exit = true;
        }

        private void sleep(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
