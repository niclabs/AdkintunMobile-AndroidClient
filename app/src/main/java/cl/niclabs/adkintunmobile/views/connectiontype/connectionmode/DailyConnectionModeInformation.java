package cl.niclabs.adkintunmobile.views.connectiontype.connectionmode;

import android.content.Context;
import android.content.res.TypedArray;

import java.util.Iterator;

import cl.niclabs.adkintunmobile.R;
import cl.niclabs.adkintunmobile.data.persistent.visualization.ConnectionModeSample;
import cl.niclabs.adkintunmobile.data.persistent.visualization.DailyConnectionModeSummary;
import cl.niclabs.adkintunmobile.data.persistent.visualization.DailyConnectionTypeSummary;
import cl.niclabs.adkintunmobile.views.connectiontype.DailyConnectionTypeInformation;

/**
 * Information about Connection Type Events of an specific day, according to
 * "initialTime" parameter (UNIX timestamp of that day)
 */
public class DailyConnectionModeInformation extends DailyConnectionTypeInformation {

    public DailyConnectionModeInformation(Context context, long initialTime, long currentTime) {
       super(context, initialTime, currentTime);
    }

    @Override
    public DailyConnectionTypeSummary getSummary(long timestamp) {
        return DailyConnectionModeSummary.getSummary(timestamp);
    }

    @Override
    public TypedArray getConnectionTypeColors() {
        return context.getResources().obtainTypedArray(R.array.connection_mode_legend_colors);
    }

    @Override
    public int getPrimaryType(){
        long[] timeByType = getTimeByType();

        int primaryType = ConnectionModeSample.NONE;

        if (timeByType[ConnectionModeSample.MOBILE] > timeByType[primaryType])
            primaryType = ConnectionModeSample.MOBILE;
        if (timeByType[ConnectionModeSample.WIFI] > timeByType[primaryType])
            primaryType = ConnectionModeSample.WIFI;
        return  primaryType;
    }

    @Override
    protected DailyConnectionTypeSummary getLastDaySummary(long initialTime) {
        String[] whereArgs = new String[1];
        whereArgs[0] = Long.toString(initialTime);
        Iterator<DailyConnectionModeSummary> pastDaysSummaries = DailyConnectionModeSummary.find(DailyConnectionModeSummary.class, "date < ?", whereArgs, "date DESC");
        if (pastDaysSummaries.hasNext()){
            while (pastDaysSummaries.hasNext()){
                DailyConnectionModeSummary pastDaySummary = pastDaysSummaries.next();
                if (pastDaySummary.getSamples().hasNext()){
                    return pastDaySummary;
                }
            }
        }
        return getSummary(initialTime - period);
    }
}
