package cl.niclabs.adkintunmobile.views.activemeasurements.viewfragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cl.niclabs.adkintunmobile.R;

public class ConnectivitytestFragment extends ActiveMeasurementViewFragment {


    public ConnectivitytestFragment() {
        this.title = "Conectividad";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_active_measurements_connectivitytest, container, false);
    }

}
