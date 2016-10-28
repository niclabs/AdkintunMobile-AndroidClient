package cl.niclabs.adkintunmobile.utils.activemeasurements.speedtest;

import cl.niclabs.adkintunmobile.utils.activemeasurements.ActiveMeasurementsTest;
import cl.niclabs.adkintunmobile.views.activemeasurements.SpeedTestDialog;
import fr.bmartel.speedtest.SpeedTestMode;

public class SpeedTest {
    private SpeedTestDialog mainTest;
    private int fileOctetSize;
    private String host;

    public SpeedTest(SpeedTestDialog mainTest, int fileOctetSize, String currentServer) {
        this.fileOctetSize = fileOctetSize;
        this.mainTest = mainTest;
        host = currentServer;
    }

    public void start(){
        startSpeedTest(SpeedTestMode.DOWNLOAD, host, fileOctetSize);
    }

    private void startSpeedTest(SpeedTestMode mode, String host, int fileOctetSize) {
        new SpeedTestTask(this, host, fileOctetSize).execute(mode);
    }

    protected void onSpeedTestTaskFinish(SpeedTestMode mode) {
        switch (mode){
            case DOWNLOAD:
                startSpeedTest(SpeedTestMode.UPLOAD, host, fileOctetSize);
                break;
            case UPLOAD:
                //mainTest.onSpeedTestFinish();
                break;
        }
    }

    protected void onProgress(SpeedTestMode mode, int progressPercent, float transferRateBit) {
        mainTest.onSpeedTestProgress(mode, progressPercent, transferRateBit);
    }
}