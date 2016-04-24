package home.sven.smarthome_android.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import home.sven.smarthome_android.tools.Alerter;
import home.sven.smarthome_android.R;
import home.sven.smarthome_android.singleton.CommunicationHandler;
import home.sven.smarthome_android.socket.UdpDiscover;

public class MainActivity extends AppCompatActivity {
    private final Context context = this;
    private final int UDP_DISCOVER_PORT = 18744;

    private ProgressDialog dialog;
    private CommunicationHandler communicationHandler;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("Relay - Main Activity", "oncreate()");

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        /************* if app crashed and alertwindows need to be displayed ***********/
        Bundle windowBundle = getIntent().getExtras();
        if(windowBundle != null) {
            String temp = windowBundle.getString("server_down_window");
            if(temp != null) {
                if(windowBundle.getString("server_down_window").equals("true")) alertServerDown();
            }
        }
        /*******************************************************************************/

        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage(context.getString(R.string.wait_find_server_dialog_text));
        // Set progress style to spinner
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        communicationHandler = CommunicationHandler.getInstance();
    }

    public void onStart() {
        super.onStart();
    }


    private void insertIp() {
        EditText ipText = (EditText)findViewById(R.id.ipText);
        if(ip == null) {
            Alerter.alertWithOkButton(context,R.string.alertNoServerOnLocalNetwork_title,R.string.alertNoServerOnLocalNetwork_text);
        }
        else ipText.setText(ip);
    }

    public void connectButtonListener(View v) {
        Log.v("Relay - Main Activity","---> Click on "+v.toString()+" Button <--- ");

        EditText ipText = (EditText)findViewById(R.id.ipText);
        if(ipText.getText().toString().equals("")) Alerter.alertWithOkButton(context, R.string.alertEmptyInput_title,R.string.alertEmptyInput_text);
        else {
            String serverIP = ipText.getText().toString();

            if(communicationHandler.connect(serverIP)) {
                Intent intent = new Intent(context,SmartHomeActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putString("ip", serverIP);
                intent.putExtras(mBundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                context.startActivity(intent);
            } else {
                Alerter.alertServerNotFound(context);
            }
        }
    }

    public void findServerButtonListener(View v) {
        Log.v("Relay - Main Activity", "---> Click on " + v.toString() + " Button <--- ");

        try {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getActiveNetworkInfo();

            if(mWifi.getType() == ConnectivityManager.TYPE_WIFI) {
                dialog.show();
                new FindServerTask().execute("relay_control_reissdorf");
            } else {
                Alerter.alertWithOkButton(context,R.string.notConnectedToWifi_title,R.string.notConnectedToWifi_text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToIpListActivityButtonListener(View v) {
        Log.v("Relay - Main Activity", "---> Click on " + v.toString() + " Button <--- ");
        Intent intent = new Intent(context,IpListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }

    public class FindServerTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            ip = UdpDiscover.findIP(params[0], UDP_DISCOVER_PORT);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    insertIp();
                }
            });
            Log.v("Relay - Main Activity", "FindServerTask(): Found IP: "+result);
        }
    }

    public void alertServerDown() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.menu_act_connection_lost_title)
                .setMessage(R.string.menu_act_connection_lost_text)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
