package cl.niclabs.adkintunmobile.utils.activemeasurements.mediatest;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.Process;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cl.niclabs.adkintunmobile.R;
import cl.niclabs.adkintunmobile.data.persistent.activemeasurement.MediaTestReport;
import cl.niclabs.adkintunmobile.data.persistent.activemeasurement.VideoResult;
import cl.niclabs.adkintunmobile.views.activemeasurements.MediaTestDialog;
import cl.niclabs.adkintunmobile.views.activemeasurements.viewfragments.MediaTestPreferenceFragment;

public class MediaTest {
    private MediaTestDialog dialog;
    private WebView webView;
    private long previousRxBytes = -1;
    private long previousTxBytes = -1;
    private Context context;

    private MediaTestReport report;

    public MediaTest(MediaTestDialog dialog, WebView webView) {
        this.dialog = dialog;
        this.webView = webView;
        this.context = dialog.getContext();
    }

    public void start() {
        this.report = new MediaTestReport();
        this.report.videoId = getContext().getString(R.string.media_test_video_id);
        this.report.setUpReport(getContext());
        this.report.save();

        webView.setWebViewClient(new WebViewClient());
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webView.addJavascriptInterface(new MediaTestJavascriptInterface(this, getContext()), "JSInterface");
        webView.loadUrl("file:///android_asset/video_test.js");
    }

    public void onVideoEnded(String quality, int timesBuffering, float loadedFraction) {

        long currentRxBytes = TrafficStats.getUidRxBytes(Process.myUid());
        long currentTxBytes = TrafficStats.getUidTxBytes(Process.myUid());
        final long totalBytes = (currentRxBytes - previousRxBytes) + (currentTxBytes - previousTxBytes);
        previousRxBytes = -1;

        if (dialog != null) {
            dialog.onVideoEnded(quality, timesBuffering, loadedFraction, totalBytes);
            VideoResult r = new VideoResult();
            r.setUpVideoResult(quality,timesBuffering, loadedFraction, totalBytes);
            r.parentReport = this.report;
            r.save();
        }
    }


    public Context getContext() {
        return webView.getContext();
    }

    public void finish() {
        if (dialog != null)
            dialog.onMediaTestFinish();
    }

    public void startCountingBytes() {
        dialog.onQualityTestStarted();
        if (previousRxBytes == -1) {
            previousRxBytes = TrafficStats.getUidRxBytes(Process.myUid());
            previousTxBytes = TrafficStats.getUidTxBytes(Process.myUid());
        }
    }

    public void noneSelectedQuality() {
        if (dialog != null)
            dialog.dismiss();
    }

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

    public void cancelTask() {
        webView.loadUrl("about:blank");
        finish();
    }

    public void setMaxQuality() {
        if (dialog != null && dialog.getTargetFragment() != null) {
            ((MediaTestPreferenceFragment) dialog.getTargetFragment()).setPreferences();
            dialog.dismiss();
        }
    }
}
