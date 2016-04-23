package home.sven.smarthome_android.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import java.io.IOException;

import home.sven.smarthome_android.R;
import home.sven.smarthome_android.activity.MainActivity;
import home.sven.smarthome_android.singleton.CommunicationHandler;

public class RelaisFragment extends Fragment {
    private final int UPDATE_SWITCH_SLEEP = 250; // update switches every xxxx ms
    private CommunicationHandler communicationHandler;
    private Switch[] switches = new Switch[8];
    private String currentInfo;
    private String lastInfo = "";
    private UpdateSwitchesThread updateSwitchesThread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v("SmartHome", "OnCreateView()");
        //FloatingActionButton fab = (FloatingActionButton)((MainActivity)getActivity()).findViewById(R.id.fab);
        //fab.setVisibility(View.GONE);
        // Inflate the layout for this fragment
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

        while(communicationHandler.getCurrentStatus() == null) try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        initView(communicationHandler.getCurrentStatus().split(";"));

        startThread();
    }

    private void updateSwitches(String info[]) {
        Log.v("SmartHome", "updateSwitches()");
        String[] tempString;

        for (int i = 0; i < info.length; i++) {
            tempString = info[i].split(",");
            switches[i].setText(tempString[0]);
            if (Boolean.parseBoolean(tempString[1])) switches[i].setChecked(true);
            else switches[i].setChecked(false);
        }
    }

    public void startThread() {
        Log.v("SmartHome", "startThread()");
        if(updateSwitchesThread != null) return;
        updateSwitchesThread = new UpdateSwitchesThread();
        updateSwitchesThread.start();
    }
    public void stopThread() {
        Log.v("SmartHome", "stopThread()");
        if(updateSwitchesThread == null) return;
        updateSwitchesThread.exit();
        updateSwitchesThread = null;
    }

    private void initView(String info[]) {
        String[] tempString;

        for(int i=0 ; i<switches.length ; i++) {
            if(i<info.length) {
                tempString = info[i].split(",");
                switches[i].setText(tempString[0]);
                switches[i].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        String switchText = ((Switch)view).getText().toString();

                        try {
                            CommunicationHandler.getInstance().sendCommand("relay;"+switchText);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                if(Boolean.parseBoolean(tempString[1])) switches[i].setChecked(true);
                else switches[i].setChecked(false);
            } else {
                switches[i].setVisibility(View.INVISIBLE);
            }

        }
    }

    private class UpdateSwitchesThread extends Thread {
        boolean exit = false;

        public void run() {
            while (communicationHandler.getCurrentStatus() == null) {
                sleep(50);
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initView(communicationHandler.getCurrentStatus().split(";"));
                }
            });

            while (!exit) {
                Log.v("SmartHome", "UpdateSwitchesThread active");
                sleep(UPDATE_SWITCH_SLEEP);
                currentInfo = communicationHandler.getCurrentStatus();

                if(currentInfo == null) {
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
