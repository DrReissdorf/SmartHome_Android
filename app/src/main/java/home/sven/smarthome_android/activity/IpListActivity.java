package home.sven.smarthome_android.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import home.sven.smarthome_android.R;
import home.sven.smarthome_android.singleton.CommunicationHandler;
import home.sven.smarthome_android.tools.Alerter;
import home.sven.smarthome_android.tools.MyAdapter;

public class IpListActivity extends AppCompatActivity {
    private SharedPreferences settings;
    private final Context context = this;
    private AlertDialog alert;
    private ListView listView;
    private String[][] stringsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_list);

        settings = getSharedPreferences("home.sven.smarthome",MODE_PRIVATE); // data saved in android system , key-value pairs

        listView = (ListView)findViewById(R.id.listView);

        refreshList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final String[] clickedItem = ((String[])(parent.getItemAtPosition(position)));

                Log.v("Relay - IpListActivity","---> Click on "+view.toString()+" Button <--- ");
                String serverIP = clickedItem[1];
                if(CommunicationHandler.getInstance().connect(serverIP)) {
                    Intent intent = new Intent(context,SmartHomeActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putString("ip", serverIP);
                    intent.putExtras(mBundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                    finish();
                } else {
                    Alerter.alertServerNotFound(context);
                }

                Log.v("IpListActivity","clicked: "+clickedItem[0]+" "+settings.getString(clickedItem[0],"not found"));
            }
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.listView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            //menu.setHeaderTitle("hallo");
            String[] menuItems = getResources().getStringArray(R.array.ipList_context_menu_items);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();

       // String[] menuItems = getResources().getStringArray(R.array.ipList_context_menu_items);
        //String menuItemName = menuItems[menuItemIndex];

        String selectedIpName = stringsArray[info.position][0];
        String selectedIp = stringsArray[info.position][1];

        switch(menuItemIndex) {
            case 0:
                editIp(selectedIpName, selectedIp);
                break;

            case 1:
                settings.edit().remove(selectedIpName).commit();
                refreshList();
                break;
        }

        return true;
    }

    private void editIp(final String itemName, String itemIp) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.add_ip, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);

        final EditText nameText = (EditText) promptView.findViewById(R.id.nameTextField);
        final EditText ipText = (EditText) promptView.findViewById(R.id.ipTextField);
        final Button dialogAddButton = (Button) promptView.findViewById(R.id.dialogAddButton);

        nameText.setText(itemName);
        ipText.setText(itemIp);
        dialogAddButton.setText("Speichern");

        dialogAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidInput(nameText.getText().toString(), ipText.getText().toString())) {
                    settings.edit().remove(itemName).putString(nameText.getText().toString(),ipText.getText().toString()).commit();
                    refreshList();
                    alert.dismiss();
                } else {
                    Alerter.alertWithOkButton(context,R.string.ipListActivity_alertEmptyInput_title,R.string.ipListActivity_alertEmptyInput_text);
                }

            }
        });

        alert = alertDialogBuilder.create();
        alert.show();
    }

    public void onFloatingActionButtonClick(View v) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.add_ip, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);

        final EditText nameText = (EditText) promptView.findViewById(R.id.nameTextField);
        final EditText ipText = (EditText) promptView.findViewById(R.id.ipTextField);
        final Button dialogAddButton = (Button) promptView.findViewById(R.id.dialogAddButton);

        dialogAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidInput(nameText.getText().toString(), ipText.getText().toString())) {
                    settings.edit().putString(nameText.getText().toString(),ipText.getText().toString()).commit();
                    refreshList();
                    alert.dismiss();
                } else {
                    Alerter.alertWithOkButton(context,R.string.ipListActivity_alertEmptyInput_title,R.string.ipListActivity_alertEmptyInput_text);
                }
            }
        });

        alert = alertDialogBuilder.create();
        alert.show();

    }

    private void refreshList() {
        final Object[] ips = settings.getAll().keySet().toArray();
        final String[] keyArray = new String[ips.length];
        stringsArray = new String[ips.length][2];

        for(int i=0 ; i<ips.length ; i++) {
            keyArray[i] = (String)ips[i];
        }

        Arrays.sort(keyArray);

        for(int i=0 ; i<ips.length ; i++) {
            stringsArray[i][0] = keyArray[i];
            stringsArray[i][1] = settings.getString(keyArray[i],"not found");
        }

        listView.setAdapter(new MyAdapter(this, stringsArray));
        registerForContextMenu(listView);
    }

    private boolean checkValidInput(String name, String ip) {
        if(name.equals("") || ip.equals("")) {
            return false;
        }

        return true;
    }
}
