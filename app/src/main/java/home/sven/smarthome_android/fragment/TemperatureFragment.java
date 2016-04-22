package home.sven.smarthome_android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import home.sven.smarthome_android.R;

public class TemperatureFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //FloatingActionButton fab = (FloatingActionButton)((MainActivity)getActivity()).findViewById(R.id.fab);
        //fab.setVisibility(View.GONE);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_temperature, container, false);
    }
}
