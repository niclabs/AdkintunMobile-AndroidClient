package cl.niclabs.adkintunmobile.workers;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Map;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import cl.niclabs.adkintunmobile.R;
import cl.niclabs.adkintunmobile.utils.activemeasurements.ping.Ping;
import cl.niclabs.adkintunmobile.utils.activemeasurements.speedtest.ActiveServers;
import cl.niclabs.adkintunmobile.utils.passivemeasurements.LocationManager;
import cl.niclabs.adkintunmobile.utils.passivemeasurements.Telephony;
import cl.niclabs.adkintunmobile.utils.activemeasurements.speedtest.SpeedTest;
import fr.bmartel.speedtest.model.SpeedTestMode;

public class PeriodicMeasurementsWorker extends AdkintunWorker  {

    public static final String SLAVE_WORKER = "SLAVE_WORKER";

    private int fileSize;
    private String serverHost;
    private String serverPort;
    private SpeedTest downloadSpeedTest;
    private SpeedTest uploadSpeedTest;
    private String pingAddress;
    private int pingTTL;
    private int pingTimeOutMillis;
    private int pingCount;
    private Ping ping;

    private LocationManager locationManager;

    private Telephony telephony;

    public PeriodicMeasurementsWorker(final Context context, WorkerParameters params) {
        super(context, params);
        pingAddress = "8.8.8.8";
        pingTTL = 100;
        pingTimeOutMillis = 5000;
        pingCount = 1;
        ping = new Ping(pingTimeOutMillis, pingTTL, pingCount);

        // Set up Telephony and Location Objects to get the required information
        telephony = new Telephony(context);
        locationManager = new LocationManager(getLooper(), context);
        downloadSpeedTest = getSpeedTest(SpeedTestMode.DOWNLOAD);
        uploadSpeedTest = getSpeedTest(SpeedTestMode.UPLOAD);
    }

    @NonNull
    @Override
    public Worker.Result doWork() {
        Map<String, Object> telephonyData;
        Map<String, Object> locationData;
        Map<String, Object> data;

        // Get signal data
        telephonyData = telephony.run();
        locationData = locationManager.run();
        data = telephonyData;
        data.putAll(locationData);
        downloadSpeedTest.run();
        uploadSpeedTest.run();

        try {
            ping.doPing(pingAddress);
        } catch (Exception e) {
            Log.e("Ping Error", e.getMessage());
        }

        if (ping.getResult() != null) {
            data.putAll(ping.getResult());
        }

        data.put("DOWNLOAD", downloadSpeedTest.getTransferRateOctet().toString());
        data.put("UPLOAD", uploadSpeedTest.getTransferRateOctet().toString());

        // Write to file
        Log.i("DATA", data.toString());
        writeToCSV("AdkintunLogging.csv", data);

        for (String key : data.keySet()) {
            updateFieldWithName(key, (String) data.get(key));
        }

        //ActiveServers act = new ActiveServers(getApplicationContext());
        //act.getActiveServers();
        Data output = new Data.Builder().putAll(data).build();
        return Result.success(output);
    }

    private Looper getLooper() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        return Looper.myLooper();
    }

    public SpeedTest getSpeedTest(SpeedTestMode mode) {
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String speedTestFileSizeValue = sharedPreferences.getString(context.getString(R.string.settings_speed_test_file_size_key), "1000000");
        serverHost = context.getString(R.string.speed_test_server_host);
        serverPort = context.getString(R.string.speed_test_server_port);
        fileSize = Integer.parseInt(speedTestFileSizeValue);
        //Log.i("SpeedtestData", serverHost + " " + serverPort + " " + fileSize);
        return new SpeedTest(serverHost, serverPort, fileSize, mode);
    }

    private void updateFieldWithName(String name, String value){
        String packageName = getApplicationContext().getPackageName();
        int resId = getApplicationContext().getResources().getIdentifier(name, "string", packageName);
        if (resId == 0) {
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getApplicationContext().getString(resId), value);
        editor.apply();
    }

    private void writeToCSV(String fileName, Map<String, Object> map) {
        if (map != null) {
            try {
                String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
                String filePath = baseDir + File.separator + fileName;
                File f = new File(filePath);
                CSVWriter writer;

                Object[] mapKeys = map.keySet().toArray();
                Object[] mapValues = map.values().toArray();
                if (f.exists() && !f.isDirectory()) {
                    FileWriter mFileWriter = new FileWriter(filePath, true);
                    writer = new CSVWriter(mFileWriter);
                } else {
                    // Create new file and put headers on it
                    writer = new CSVWriter(new FileWriter(filePath));
                    if (mapKeys != null) {
                        String[] header = Arrays.copyOf(mapKeys, mapKeys.length, String[].class);
                        writer.writeNext(header);
                    }
                }
                String[] data = Arrays.copyOf(mapValues, mapValues.length, String[].class);
                writer.writeNext(data);
                writer.close();
            } catch (Exception e) {
                Log.e("IOException", e.toString());
            }
        }
    }

}
