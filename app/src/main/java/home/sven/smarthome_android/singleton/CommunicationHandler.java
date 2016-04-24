package home.sven.smarthome_android.singleton;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import home.sven.smarthome_android.activity.MainActivity;
import home.sven.smarthome_android.socket.SocketComm;

public class CommunicationHandler {
    private static CommunicationHandler communicationHandler;
    private final int TCP_COMMAND_PORT = 18745;
    private String currentSwitchStatus;
    private String currentTempStatus;
    private SocketComm commandSocketComm;
    private Context smartHomeContext;

    private CommunicationHandler(){}

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
        public void callClose();
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
                                System.out.println(received[1]);
                                break;

                            case "temp":
                                Log.v("Smart Home", "UpdateStatusThread - temp");
                                currentTempStatus = received[1];
                                break;
                        }

                    }
                } catch (Exception e) {
                    this.exit = true;
                    currentTempStatus = null;
                    currentSwitchStatus = null;
                    close();
                    callerActivity.callClose();
                    e.printStackTrace();
                }
            }
            Log.v("UpdateStatusThread", "stopping...");
        }
    }

    public String getCurrentSwitchStatus() {
        return currentSwitchStatus;
    }

    public String getCurrentTempStatus() {
        return currentTempStatus;
    }
}