package cl.niclabs.adkintunmobile.views.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.Iterator;

import cl.niclabs.adkintunmobile.R;
import cl.niclabs.adkintunmobile.data.persistent.visualization.ApplicationTraffic;
import cl.niclabs.adkintunmobile.data.persistent.visualization.ConnectionModeSample;
import cl.niclabs.adkintunmobile.data.persistent.visualization.NetworkTypeSample;
import cl.niclabs.adkintunmobile.utils.display.DisplayDateManager;
import cl.niclabs.adkintunmobile.utils.information.Connections.Connections;
import cl.niclabs.adkintunmobile.utils.information.Connections.SystemSocket;
import cl.niclabs.adkintunmobile.utils.information.Network;
import cl.niclabs.adkintunmobile.views.activeconnections.ActiveConnectionListElement;
import cl.niclabs.adkintunmobile.views.applicationstraffic.ApplicationsTrafficListElement;
import cl.niclabs.adkintunmobile.views.connectiontype.connectionmode.DailyConnectionModeInformation;
import cl.niclabs.adkintunmobile.views.connectiontype.networktype.DailyNetworkTypeInformation;

public class DashboardFragment extends Fragment {

    protected String title;
    protected Context context;
    protected Toolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.title = getActivity().getString(R.string.app_name);
        this.context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_collapsable_toolbar, container, false);
        View localFragmentView = view.findViewById(R.id.main_fragment);
        inflater.inflate(R.layout.fragment_dashboard, (ViewGroup) localFragmentView, true);
        setupToolbar(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();

        updateStatusToolbar(view);

        updateMobileConsumption(view.findViewById(R.id.card_mobile_consumption));
        updateConnectionMode(view.findViewById(R.id.card_connection_mode));
        updateNetworkType(view.findViewById(R.id.card_network_type));
        updateTopApps(view.findViewById(R.id.card_top_apps));
        updateActiveConnections(view.findViewById(R.id.card_active_connections));
    }

    public void updateStatusToolbar(View view){

        ShimmerFrameLayout container =
                (ShimmerFrameLayout) view.findViewById(R.id.shimmer_dashboard_logo);
        container.startShimmerAnimation();

        TextView tvSim, tvAntenna, tvSignal, tvInternet;

        tvSim = (TextView) view.findViewById(R.id.tv_sim);
        tvAntenna = (TextView) view.findViewById(R.id.tv_antenna);
        tvSignal = (TextView) view.findViewById(R.id.tv_signal);
        tvInternet = (TextView) view.findViewById(R.id.tv_internet);

        tvSim.setText(Network.getSimCarrier(context));
        tvAntenna.setText(Network.getConnectedCarrrier(context));
        tvSignal.setText(Network.getNetworkType(context));

        switch (Network.getActiveNetwork(context)){
            case ConnectionModeSample.MOBILE:
                tvInternet.setText(getString(R.string.view_dashboard_conn_mode_mobile));
                break;
            case ConnectionModeSample.WIFI:
                tvInternet.setText(getString(R.string.view_dashboard_conn_mode_wifi));
                break;
            case ConnectionModeSample.NONE:
                tvInternet.setText(getString(R.string.view_dashboard_conn_mode_unknown));
                break;
        }

        final ImageView ivBackdrop = (ImageView) view.findViewById(R.id.iv_backdrop_toolbar);
        Animation toolbarAnimation = AnimationUtils.loadAnimation(this.context, R.anim.zoom_toolbar);

        toolbarAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.zoom_toolbar);
                anim.setAnimationListener(this);
                ivBackdrop.setAnimation(anim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        ivBackdrop.setAnimation(toolbarAnimation);

    }

    public void setupToolbar(View view) {
        // Setup toolbar as an actionbar
        this.toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(this.toolbar);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(this.title);
        setHasOptionsMenu(true);

    }

    public void updateTopApps(final View view){
        (new Thread(){
            @Override
            public void run() {
                final ApplicationsTrafficListElement[] topApps = getTop3AppsToday();
                final TableLayout topAppsLayout = (TableLayout) view.findViewById(R.id.top_3_table_layout);
                final TextView[] rankingLabels = {(TextView) view.findViewById(R.id.tv_app1),
                        (TextView) view.findViewById(R.id.tv_app2),
                        (TextView) view.findViewById(R.id.tv_app3)};
                final ImageView[] rankingIcons = {(ImageView) view.findViewById(R.id.iv_app1),
                        (ImageView) view.findViewById(R.id.iv_app2),
                        (ImageView) view.findViewById(R.id.iv_app3)};

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean isEmpty = true;
                            for (int i = 0; i < 3; i++) {
                                if (topApps[i] != null) {
                                    isEmpty = false;
                                    rankingIcons[i].setImageDrawable(topApps[i].getLogo());
                                    rankingLabels[i].setText(topApps[i].getLabel());
                                    rankingIcons[i].setVisibility(View.VISIBLE);
                                    rankingLabels[i].setVisibility(View.VISIBLE);
                                    topAppsLayout.setColumnStretchable(i, true);
                                } else {
                                    rankingIcons[i].setVisibility(View.GONE);
                                    rankingLabels[i].setVisibility(View.GONE);
                                }
                            }
                            if (isEmpty) {
                                (view.findViewById(R.id.empty_dialog)).setVisibility(View.VISIBLE);
                            } else
                                (view.findViewById(R.id.empty_dialog)).setVisibility(View.GONE);
                            view.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();
    }

    public void updateMobileConsumption(final View view){
        (new Thread(){
            @Override
            public void run() {



                final long[] monthlyData = ApplicationTraffic.getMonthlyMobileConsumption(context);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) view.findViewById(R.id.tv_download_data)).setText(Network.formatBytes(monthlyData[0]));
                            ((TextView) view.findViewById(R.id.tv_upload_data)).setText(Network.formatBytes(monthlyData[1]));
                            view.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();
    }

    public void updateConnectionMode(final View view){
        (new Thread(){
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                final DailyConnectionModeInformation information = new DailyConnectionModeInformation(context, currentTime, currentTime);
                information.setStatisticsInformation();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded()) {
                                TextView tvPrimaryConn = (TextView) view.findViewById(R.id.tv_primary_conn);
                                switch (information.getPrimaryType()) {
                                    case ConnectionModeSample.NONE:
                                        tvPrimaryConn.setText(getString(R.string.view_dashboard_conn_mode_unknown));
                                        break;
                                    case ConnectionModeSample.MOBILE:
                                        tvPrimaryConn.setText(getString(R.string.view_dashboard_conn_mode_mobile));
                                        break;
                                    case ConnectionModeSample.WIFI:
                                        tvPrimaryConn.setText(getString(R.string.view_dashboard_conn_mode_wifi));
                                        break;
                                }
                                view.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    public void updateNetworkType(final View view){
        (new Thread(){
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                final DailyNetworkTypeInformation information = new DailyNetworkTypeInformation(context, currentTime, currentTime);
                information.setStatisticsInformation();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageView ivPrimaryNet = (ImageView) view.findViewById(R.id.iv_primary_net);
                            switch (information.getPrimaryType()) {
                                case NetworkTypeSample.UNKNOWN:
                                    ivPrimaryNet.setImageResource(R.drawable.ic_10_nored);
                                    break;
                                case NetworkTypeSample.TYPE_G:
                                    ivPrimaryNet.setImageResource(R.drawable.ic_04_g);
                                    break;
                                case NetworkTypeSample.TYPE_E:
                                    ivPrimaryNet.setImageResource(R.drawable.ic_05_edge);
                                    break;
                                case NetworkTypeSample.TYPE_3G:
                                    ivPrimaryNet.setImageResource(R.drawable.ic_06_3g);
                                    break;
                                case NetworkTypeSample.TYPE_H:
                                    ivPrimaryNet.setImageResource(R.drawable.ic_07_h);
                                    break;
                                case NetworkTypeSample.TYPE_Hp:
                                    ivPrimaryNet.setImageResource(R.drawable.ic_08_hp);
                                    break;
                                case NetworkTypeSample.TYPE_4G:
                                    ivPrimaryNet.setImageResource(R.drawable.ic_09_4g);
                                    break;
                            }
                            view.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();
    }

    private void updateActiveConnections(final View view) {
        (new Thread(){
            @Override
            public void run() {
                ArrayList<ActiveConnectionListElement> activeSockets = new ArrayList<>();

                ArrayList<SystemSocket> totalSockets = new ArrayList<>(Connections.getTCPSockets());
                totalSockets.addAll(Connections.getUDPSockets());

                for (SystemSocket socket : totalSockets){
                    ActiveConnectionListElement element =  new ActiveConnectionListElement(context, socket);
                    if (element.isValidElement()){
                        if (!activeSockets.contains(element)){
                            activeSockets.add(element);
                        }else{
                            activeSockets.get(activeSockets.indexOf(element)).addConnection(socket);
                        }
                    }
                }

                int aux = 0;
                for (ActiveConnectionListElement element : activeSockets){
                    aux += element.getTotalActiveConnections();
                }

                final int numberOfConnections = aux;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (numberOfConnections > 0) {
                                ((TextView) view.findViewById(R.id.tv_active_connections)).setText(Integer.toString(numberOfConnections));
                                view.setVisibility(View.VISIBLE);
                            }
                            else
                                view.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }

    public ApplicationsTrafficListElement[] getTop3AppsToday(){
        ApplicationsTrafficListElement[] ret = new ApplicationsTrafficListElement[3];

        // Obtener aplicaciones con actividad de hoy
        long initTime = DisplayDateManager.timestampAtStartDay(System.currentTimeMillis());

        String sqlStatements[] = new String[2];
        sqlStatements[0] = Long.toString(initTime);
        sqlStatements[1] = Integer.toString(ApplicationTraffic.MOBILE);

        Iterator<ApplicationTraffic> apps = ApplicationTraffic.findAsIterator(
                ApplicationTraffic.class,
                "timestamp >= ? and network_type = ?",
                sqlStatements,
                null,
                "rx_bytes DESC",
                "3");

        int i = 0;
        while (apps.hasNext()){
            ApplicationTraffic current = apps.next();

            ApplicationsTrafficListElement elem = new ApplicationsTrafficListElement(context, current);
            ret[i++] = elem;
        }

        return ret;
    }
}
