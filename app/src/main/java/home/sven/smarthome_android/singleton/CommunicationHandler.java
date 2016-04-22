package home.sven.smarthome_android.singleton;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import home.sven.smarthome_android.socket.SocketComm;

public class CommunicationHandler {
    private static CommunicationHandler communicationHandler;
    private final int TCP_COMMAND_PORT = 18745;
    private String currentStatus;
    private SocketComm commandSocketComm;

    private CommunicationHandler(){}

    public static CommunicationHandler getInstance() {
        if(communicationHandler == null) communicationHandler = new CommunicationHandler();
        return communicationHandler;
    }

    public void sendCommand(String message) throws IOException {
        Log.v("CommunicationHandler", "sendCommand()");
        commandSocketComm.send(message);
    }

    public void updateStatus() throws IOException {
        Log.v("CommunicationHandler", "updateStatus()");
        currentStatus = commandSocketComm.receive();
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
                    if(commandSocketComm != null) updateStatus();
                    if(currentStatus == null) exit = true;
                } catch (IOException e) {
                    exit = true;
                    close();
                    e.printStackTrace();
                }
            }
            Log.v("UpdateStatusThread", "stopping...");
        }
    }

    public String getCurrentStatus() {
        return currentStatus;
    }
}