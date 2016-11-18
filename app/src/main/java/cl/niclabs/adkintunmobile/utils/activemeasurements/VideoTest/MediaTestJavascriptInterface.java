package cl.niclabs.adkintunmobile.utils.activemeasurements.VideoTest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.util.ArrayList;

import cl.niclabs.adkintunmobile.R;
import cl.niclabs.adkintunmobile.views.activemeasurements.viewfragments.ActiveMeasurementsSettingsActivity;

public class MediaTestJavascriptInterface {
    private MediaTest mediaTest;
    private Context context;

    public MediaTestJavascriptInterface(MediaTest mediaTest, Context context) {
        this.mediaTest = mediaTest;
        this.context = context;
    }

    @JavascriptInterface
    public void doEchoTest(String echo) {
        Log.d("VideoView", echo);
    }

    @JavascriptInterface
    public void playVideo() {
        //emulateClick(webView);
    }

    @JavascriptInterface
    public void onVideoEnded(String quality, int timesBuffering, float loadedFraction) {
        mediaTest.onVideoEnded(getPixelsFromQuality(quality), timesBuffering, loadedFraction);
    }

    @JavascriptInterface
    public void onVideoTestFinish() {
        mediaTest.finish();
    }

    @JavascriptInterface
    public void makeToast(String quality) {
        String text = getPixelsFromQuality(quality);
        Toast.makeText(context, text,
                Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void startCountingBytes() {
        mediaTest.startCountingBytes();
    }

    @JavascriptInterface
    public boolean getQuality(String quality) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        switch (quality){
            case "tiny":
                return sharedPreferences.getBoolean(context.getString(R.string.settings_video_test_quality_tiny_key), false);
            case "small":
                return sharedPreferences.getBoolean(context.getString(R.string.settings_video_test_quality_small_key), false);
            case "medium":
                return sharedPreferences.getBoolean(context.getString(R.string.settings_video_test_quality_medium_key), false);
            case "large":
                return sharedPreferences.getBoolean(context.getString(R.string.settings_video_test_quality_large_key), false);
            case "hd720":
                return sharedPreferences.getBoolean(context.getString(R.string.settings_video_test_quality_hd720_key), false);
            default:
                return false;
        }
    }

    @JavascriptInterface
    public void noneSelectedQuality(){
        mediaTest.noneSelectedQuality();
    }

    @JavascriptInterface
    public void setMaxQuality(String maxQuality){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.settings_video_test_max_quality_key), getPixelsFromQuality(maxQuality));
        editor.apply();
        noneSelectedQuality();
    }

    public String getPixelsFromQuality(String quality){
        String text;
        switch (quality){
            case "tiny":
                text = "144p";
                break;
            case "small":
                text = "240p";
                break;
            case "medium":
                text = "360p";
                break;
            case "large":
                text = "480p";
                break;
            case "hd720":
                text = "720p";
                break;
            default:
                text = "unknown";
                break;
        }
        return text;
    }
}
