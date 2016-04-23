package home.sven.smarthome_android.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
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

public class SmartHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final Context context = this;
    private String serverIP;

    /****************** Threads *********************/
    private CommunicationHandler communicationHandler;
    private RelaisFragment relaisFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_smarthome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        communicationHandler = CommunicationHandler.getInstance();
        serverIP = getIntent().getExtras().getString("ip");

        /***** Set first fragment to show *****/
        switchFragment(R.id.fragment_container, new MainMenuFragment());
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

        if(relaisFragment != null) relaisFragment.startThread();

        communicationHandler.startUpdateStatusThread();
    }

    public void onPause() {
        Log.v("Relay - Menu Activity", "onPause()");
        super.onPause();

        if(relaisFragment != null) relaisFragment.stopThread();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_smarthome_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(relaisFragment != null) relaisFragment.stopThread();
        relaisFragment = null;

        switch(id) {
            case R.id.main_menu:
                Log.v("MainActivity","onNavigationItemSelected() - Clicked Navitem: Main_menu");
                switchFragment(R.id.fragment_container, new MainMenuFragment());
                break;

            case R.id.relais:
                Log.v("MainActivity","onNavigationItemSelected() - Clicked Navitem: Relais");
                relaisFragment = new RelaisFragment();
                switchFragment(R.id.fragment_container, relaisFragment );
                break;

            case R.id.temp:
                Log.v("MainActivity","onNavigationItemSelected() - Clicked Navitem: Temp");
                switchFragment(R.id.fragment_container, new TemperatureFragment());
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void switchFragment(int layout, Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(layout, fragment).commit();
    }
}
