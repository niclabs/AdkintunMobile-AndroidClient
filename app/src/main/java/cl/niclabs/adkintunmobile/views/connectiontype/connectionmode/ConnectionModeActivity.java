package cl.niclabs.adkintunmobile.views.connectiontype.connectionmode;

import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cl.niclabs.adkintunmobile.R;
import cl.niclabs.adkintunmobile.data.chart.StatisticInformation;
import cl.niclabs.adkintunmobile.data.persistent.visualization.DailyConnectionModeSummary;
import cl.niclabs.adkintunmobile.utils.display.DoughnutChart;
import cl.niclabs.adkintunmobile.views.connectiontype.ConnectionTypeActivity;

public class ConnectionModeActivity extends ConnectionTypeActivity {

    private final String TAG = "AdkM:ConnectionModeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_mode);
        timeByType = new ArrayList<TextView>();
        timeByType.add( 0, (TextView) findViewById(R.id.tv_disconnect_total_time));
        timeByType.add( 1, (TextView) findViewById(R.id.tv_mobile_total_time));
        timeByType.add( 2, (TextView) findViewById(R.id.tv_wifi_total_time));
        setBaseActivityParams();
        setUpToolbar();
        setUpDoughnutChart();
    }

    public void setBaseActivityParams() {
        this.title = getString(R.string.view_connection_mode);
        this.context = this;
        this.loadingPanel = (RelativeLayout) findViewById(R.id.loading_panel);
    }

    @Override
    public void loadData(long initialTime) {
        long currentTime = System.currentTimeMillis();
        StatisticInformation statistic = new DailyConnectionModeInformation(context, initialTime, currentTime);
        this.chart = (DoughnutChart) this.chartBuilder.createGraphicStatistic(statistic);
    }

    @Override
    public void refreshLegend(long initialTime){
        long [] totalTimeByType = DailyConnectionModeSummary.getTimeByTypeSummary(initialTime);
        for (int i=0; i<timeByType.size(); i++){
            long hours = totalTimeByType[i]/(1000*3600);
            long minutes = (totalTimeByType[i] - hours*1000*3600)/(1000*60);
            timeByType.get(i).setText(hours + " Horas, " + minutes + " Min.");
        }
    }
}
