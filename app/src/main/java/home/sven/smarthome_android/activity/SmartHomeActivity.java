package home.sven.smarthome_android.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import home.sven.smarthome_android.fragment.MainMenuFragment;
import home.sven.smarthome_android.R;
import home.sven.smarthome_android.fragment.RelaisFragment;
import home.sven.smarthome_android.fragment.TemperatureFragment;
import home.sven.smarthome_android.singleton.CommunicationHandler;

public class SmartHomeActivity extends AppCompatActivity implements CommunicationHandler.ICommCallBack,NavigationView.OnNavigationItemSelectedListener {
    private final Context context = this;
    private String serverIP;
    private CommunicationHandler communicationHandler;
    private Fragment activeFragment;
    private Fragment mainMenuFragment;
    private Fragment relaisFragment;
    private Fragment temperatureFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_smarthome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        communicationHandler = CommunicationHandler.getInstance();
        serverIP = getIntent().getExtras().getString("ip");

        mainMenuFragment = new MainMenuFragment();
        relaisFragment = new RelaisFragment();
        temperatureFragment = new TemperatureFragment();

        /***** Set fragment to show *****/
        if(savedInstanceState != null) {
            String savedFragment = savedInstanceState.getString("fragment");
            if(savedFragment != null) {
                switch(savedFragment) {
                    case "main":
                        switchFragment(mainMenuFragment);
                        break;

                    case "relay":
                        switchFragment(relaisFragment);
                        break;

                    case "temp":
                        switchFragment(temperatureFragment);
                        break;
                }
            }
        } else {
            switchFragment(mainMenuFragment);
        }
    }

    public void onResume() {
        super.onResume();
        Log.v("Relay - Menu Activity", "onResume()");

        if(!communicationHandler.connect(serverIP)) {
            /** Connection didn't happen, back to MainActivity **/
            Intent intent = new Intent(context,MainActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putString("server_down_window", "true");
            intent.putExtras(mBundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            communicationHandler.close();
            context.startActivity(intent);
            finish();
        }

        communicationHandler.setCallerActivity(this);
    }

    public void onPause() {
        Log.v("Relay - Menu Activity", "onPause()");
        super.onPause();

        communicationHandler.close();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        if(activeFragment.getClass() == RelaisFragment.class) icicle.putString("fragment", "relay");
        if(activeFragment.getClass() == MainMenuFragment.class) icicle.putString("fragment", "main");
        if(activeFragment.getClass() == TemperatureFragment.class) icicle.putString("fragment", "temp");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_smarthome_actionmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.exit:
                Intent intent = new Intent(context,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                callClose();
                context.startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.main_menu:
                Log.v("MainActivity","onNavigationItemSelected() - Clicked Navitem: Main_menu");
                switchFragment(mainMenuFragment);
                break;

            case R.id.relais:
                Log.v("MainActivity","onNavigationItemSelected() - Clicked Navitem: Relais");
                switchFragment(relaisFragment);
                break;

            case R.id.temp:
                Log.v("MainActivity","onNavigationItemSelected() - Clicked Navitem: Temp");
                switchFragment(temperatureFragment);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void switchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        activeFragment = fragment;
    }

    @Override
    public void callClose() {
        Intent intent = new Intent(context,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        communicationHandler.close();
        context.startActivity(intent);
    }

    @Override
    public void updateActiveUI() {
        if(activeFragment.getClass() == RelaisFragment.class) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((RelaisFragment) activeFragment).updateSwitches(communicationHandler.getSwitchStatus());
                }
            });
        }

        if(activeFragment.getClass() == MainMenuFragment.class) {

        }

        if(activeFragment.getClass() == TemperatureFragment.class) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TemperatureFragment) activeFragment).updateTemperatures(communicationHandler.getCurrentTempStatus());
                }
            });
        }
    }
}
