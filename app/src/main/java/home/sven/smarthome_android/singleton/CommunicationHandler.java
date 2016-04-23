package home.sven.smarthome_android.singleton;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import home.sven.smarthome_android.socket.SocketComm;

public class CommunicationHandler {
    private static CommunicationHandler communicationHandler;
    private final int TCP_COMMAND_PORT = 18745;
    private String currentSwitchStatus;
    private String currentTempStatus;
    private SocketComm commandSocketComm;
    private final int UPDATE_TEMPERATURE_SLEEP = 2500;
    private UpdateTemperatureThread updateTemperatureThread;

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

    public void startUpdateStatusThread() {
        new UpdateStatusThread().start();
    }
    public void startUpdateTemperatureThread() {
        updateTemperatureThread = new UpdateTemperatureThread();
        updateTemperatureThread.start();
    }

    public boolean close() {
        Log.v("CommunicationHandler", "close()");
        updateTemperatureThread.exit();
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

                        System.out.println("message: "+message);

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
                    if(currentSwitchStatus == null) exit = true;
                } catch (IOException e) {
                    this.exit = true;
                    close();
                    e.printStackTrace();
                }
            }
            Log.v("UpdateStatusThread", "stopping...");
        }
    }

    private class UpdateTemperatureThread extends Thread {
        private boolean exit = false;
        public void run() {
            while(!exit) {
                try {
                    sendCommand("temp%");
                    sleep(UPDATE_TEMPERATURE_SLEEP);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.v("UpdateStatusThread", "stopping...");
        }

        public void exit() {
           this.exit = true;
        }
    }

    public String getCurrentSwitchStatus() {
        return currentSwitchStatus;
    }

    public String getCurrentTempStatus() {
        return currentTempStatus;
    }
}