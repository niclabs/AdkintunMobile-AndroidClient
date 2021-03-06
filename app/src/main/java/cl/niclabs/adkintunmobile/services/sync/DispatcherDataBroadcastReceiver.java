package cl.niclabs.adkintunmobile.services.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cl.niclabs.adkintunmobile.R;
import cl.niclabs.adkintunmobile.data.persistent.visualization.ConnectionModeSample;
import cl.niclabs.adkintunmobile.data.persistent.visualization.NewsNotification;
import cl.niclabs.adkintunmobile.utils.display.DisplayDateManager;
import cl.niclabs.adkintunmobile.utils.information.Network;
import cl.niclabs.adkintunmobile.utils.volley.HttpMultipartRequest;
import cl.niclabs.adkintunmobile.utils.volley.VolleySingleton;
import cl.niclabs.adkintunmobile.views.activemeasurements.ActiveMeasurementsActivity;

public class DispatcherDataBroadcastReceiver extends BroadcastReceiver{

    private final String TAG = "AdkM:DispatcherDataBR";

    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "Iniciado intento de despacho de datos");
        if (Network.getActiveNetwork(context) == ConnectionModeSample.WIFI && !ActiveMeasurementsActivity.isRunning()) {

            Log.d(TAG, "Wifi disponible");
            // 1.- List report files in data directory
            File outputDir = context.getFilesDir();
            File[] reportFiles = outputDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.startsWith(context.getString(R.string.synchronization_report_filename)) &&
                            filename.endsWith(context.getString(R.string.synchronization_report_file_extension));
                }
            });
            Log.d(TAG, "Requests pendientes: " + reportFiles.length);

            for (int i=0; i<reportFiles.length; i++){
                File reportFile = reportFiles[i];
                sendData(reportFile, context);
            }
            VolleySingleton.getInstance(context).getRequestQueue().start();
        }
        else{
            Log.d(TAG, "Wifi no disponible");
            VolleySingleton.getInstance(context).getRequestQueue().stop();
            VolleySingleton.getInstance(context).getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
    }

    /**
     * Convert File into array of bytes
     * @param file
     * @return
     */
    private static byte[] readContentIntoByteArray(File file) {
        FileInputStream fileInputStream;
        byte[] bFile = new byte[(int) file.length()];
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bFile;
    }

    private void sendData(final File reportFile, final Context context) {
        byte[] data = readContentIntoByteArray(reportFile);
        byte[] multipartBody = null;

        // Preparar Archivo a adjuntar
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            // Primer archivo adjuntado al DataOutputStream
            String fileMultiPartFormData = context.getString(R.string.synchronization_report_file_multipartdata);
            String filePostNameParam = context.getString(R.string.synchronization_report_file_post_name_parameter);
            HttpMultipartRequest.buildPart(dos, filePostNameParam, data, fileMultiPartFormData);
            // Crear multipart body
            multipartBody = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Preparar headers del request
        Map<String,String> headers = new HashMap<String, String>();
        String authKey = context.getString(R.string.settings_sampling_hostname_token_key);
        String authValue = context.getString(R.string.token_post_adkserver);
        headers.put(authKey, authValue);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String requestURL =
                sharedPreferences.getString(
                        context.getString(R.string.settings_sampling_hostname_key),
                        context.getString(R.string.adkintun_hostname_default));

        // Creación multipart request
        HttpMultipartRequest multipartRequest =
                new HttpMultipartRequest(
                        requestURL,
                        headers,
                        multipartBody,
                        new Response.Listener<NetworkResponse>() {
                            @Override
                            public void onResponse(NetworkResponse response) {
                                Log.d(TAG, "Upload " + reportFile.getName() + "(" + reportFile.length() + "b)" + " successfully to " + requestURL);

                                // Actualizar registro de la última sincronización exitosa
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(context.getString(R.string.settings_sampling_lastsync_key), DisplayDateManager.getDateString(System.currentTimeMillis()));
                                editor.apply();

                                NewsNotification syncLog = new NewsNotification(
                                        NewsNotification.SYNC_LOG,
                                        DisplayDateManager.getDateString(System.currentTimeMillis()),
                                        "OK\n" + reportFile.getName());
                                syncLog.save();

                                // Borrar archivo
                                reportFile.delete();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Upload failed to " + requestURL + ": " + error.getMessage() + " : " + error.toString());
                                VolleySingleton.getInstance(context).getRequestQueue().stop();
                                VolleySingleton.getInstance(context).getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
                                    @Override
                                    public boolean apply(Request<?> request) {
                                        return true;
                                    }
                                });
                                NewsNotification syncLog = new NewsNotification(
                                        NewsNotification.SYNC_LOG,
                                        DisplayDateManager.getDateString(System.currentTimeMillis()),
                                        "onErrorResponse - " + error.getMessage() + " : " + error.toString() +"\n" + reportFile.getName());
                                syncLog.save();
                            }
                        });

        // Agregar multipartrequest a la cola de peticiones
        VolleySingleton.getInstance(context).addToRequestQueue(multipartRequest);
    }
}
