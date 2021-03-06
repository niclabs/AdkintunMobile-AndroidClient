package cl.niclabs.adkintunmobile.services.sync;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import cl.niclabs.adkintunmobile.R;
import cl.niclabs.adkintunmobile.data.Report;
import cl.niclabs.adkintunmobile.services.SetupSystem;
import cl.niclabs.adkintunmobile.utils.compression.CompressionUtils;

public class Synchronization extends Service {

    private final String TAG = "AdkM:Synchronization";
    private Context context;

    private final String SYNC_INTENT = "cl.niclabs.adkintunmobile.intent.DISPATCHER_DATA";

    public Synchronization() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
        Log.d(this.TAG, "Creado El servicio de sincronización");

        // 1.- Build a report and get CompressionType
        Report report = new Report(getApplicationContext());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int selectedCompressionType = Integer.parseInt(sharedPreferences.getString(getString(R.string.settings_sampling_compression_type_key), "1"));
        CompressionUtils.CompressionType compressionType = CompressionUtils.CompressionType.getCompressionType(selectedCompressionType);
        Log.d(TAG, "Usando compresión: " + compressionType);

        if (report.recordsToSend()){
            // 2.- Save report
            report.saveFile(context, compressionType);
            // 3.- Backup visualization data
            report.saveVisualSamples();
            // 4.- Clean DB
            report.cleanDBRecords();
        }
        // 5.- Intent send data
        runIntentSendData();
        // 6.- StopService
        stopSelf();
    }

    private void runIntentSendData() {
        Intent intent = new Intent();
        intent.setAction(SYNC_INTENT);
        context.sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(this.TAG, "Detenido El servicio de sincronización");
        //SetupSystem.startUpSystem(context);
        SetupSystem.schedulleBroadcastReceivers(context);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
