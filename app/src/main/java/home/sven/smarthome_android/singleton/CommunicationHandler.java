package home.sven.smarthome_android.singleton;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import home.sven.smarthome_android.socket.SocketComm;

public class CommunicationHandler {
    private static CommunicationHandler communicationHandler;
    private final int TCP_COMMAND_PORT = 18745;

    private String[] currentTempStatus;
    private SocketComm commandSocketComm;

    /* SWITCHES */
    private String[] switchNames;
    private boolean[] switchStatus;
    private String currentSwitchStatus;
    private String lastSwitchStatus = "";

    private CommunicationHandler(){
        currentTempStatus = new String[1];
    }

    public static CommunicationHandler getInstance() {
        if(communicationHandler == null) communicationHandler = new CommunicationHandler();
        return communicationHandler;
    }

    public void sendCommand(String message) throws IOException {
        Log.v("CommunicationHandler", "sendCommand()");
        commandSocketComm.send(message);
    }

    public boolean connect(String ip) {
        Log.v("CommunicationHandler", "connect()");
        try {
            boolean ret = new ConnectSocketsTask().execute(ip).get();
            return ret;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    /* CALLBACK FOR ACTIVITY */
    public interface ICommCallBack {
        void callClose();
        void updateActiveUI();
    }

    private ICommCallBack callerActivity;

    public void setCallerActivity(Activity activity) {
        callerActivity = (ICommCallBack)activity;
    }
    /********************************************/

    public void startUpdateStatusThread() {
        new UpdateStatusThread().start();
    }

    public boolean close() {
        Log.v("CommunicationHandler", "close()");
        try {
            return new CloseSocketsTask().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public class ConnectSocketsTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                if(commandSocketComm == null) commandSocketComm = new SocketComm(params[0], TCP_COMMAND_PORT);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }

    public class CloseSocketsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if(commandSocketComm != null) {
                    commandSocketComm.close();
                    commandSocketComm = null;
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private class UpdateStatusThread extends Thread {
        private boolean exit = false;
        public void run() {
            while(!exit) {
                try {
                    if(commandSocketComm != null) {
                        String message = commandSocketComm.receive();
                        String[] received = message.split("%");

                        switch(received[0]) {
                            case "relay":
                                Log.v("Smart Home", "UpdateStatusThread - relay");
                                currentSwitchStatus = received[1];
                                if(!lastSwitchStatus.equals(currentSwitchStatus)) {
                                    lastSwitchStatus = currentSwitchStatus;
                                    updateSwitchStatusMap();
                                }
                                break;

                            case "temp":
                                Log.v("Smart Home", "UpdateStatusThread - temp");
                                currentTempStatus[0] = received[1];
                                break;
                        }
                        callerActivity.updateActiveUI();
                    }
                } catch (Exception e) {
                    this.exit = true;
                    close();
                    //callerActivity.callClose();
                    e.printStackTrace();
                }
            }
            Log.v("UpdateStatusThread", "stopping...");
        }
    }

    private void updateSwitchStatusMap() {
        Log.v("SmartHome", "updateSwitches()");
        String[] status = currentSwitchStatus.split(";");
        String[] tempString;

        switchNames = new String[status.length];
        switchStatus = new boolean[status.length];

        for (int i = 0; i < status.length; i++) {
            tempString = status[i].split(",");
            switchNames[i] = tempString[0];
            switchStatus[i] = Boolean.valueOf(tempString[1]);
        }
    }

    public String[] getSwitchNames() {
        return switchNames;
    }

    public boolean[] getSwitchStatus() {
        return switchStatus;
    }

    public String getCurrentSwitchStatus() {
        return currentSwitchStatus;
    }

    public String[] getCurrentTempStatus() {
        return currentTempStatus;
    }
}