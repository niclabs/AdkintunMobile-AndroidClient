package cl.niclabs.adkintunmobile.data;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.util.Iterator;
import java.util.List;

import cl.niclabs.adkintunmobile.data.persistent.CdmaObservationWrapper;
import cl.niclabs.adkintunmobile.data.persistent.ConnectivityObservationWrapper;
import cl.niclabs.adkintunmobile.data.persistent.GsmObservationWrapper;
import cl.niclabs.adkintunmobile.data.persistent.SampleWrapper;
import cl.niclabs.adkintunmobile.data.persistent.StateChangeWrapper;
import cl.niclabs.adkintunmobile.data.persistent.TelephonyObservationWrapper;
import cl.niclabs.adkintunmobile.data.persistent.TrafficObservationWrapper;
import cl.niclabs.adkintunmobile.data.persistent.visualization.ApplicationTraffic;
import cl.niclabs.adkintunmobile.data.persistent.visualization.ConnectionModeSample;
import cl.niclabs.adkintunmobile.data.persistent.visualization.NetworkTypeSample;

/**
 * Clase para manipular los datos de eventos persistidos en el telefono.
 *
 * La creación de un objeto Report rescata todos los valores persistidos en Sugar en el dispositivo
 * de manera que al serializarlo, se genera un JSON con el reporte listo para el servidor.
 * 
 */
public class Report {

    @SerializedName("sim_records")
    SimSingleton simRecord;
    @SerializedName("device_records")
    DeviceSingleton deviceRecord;

    @SerializedName("cdma_records")
    public List <CdmaObservationWrapper> cdmaRecords;
    @SerializedName("connectivity_records")
    public List <ConnectivityObservationWrapper> connectivityRecords;
    @SerializedName("gsm_records")
    public List <GsmObservationWrapper> gsmRecords;
    @SerializedName("state_records")
    public List <StateChangeWrapper> stateRecords;
    @SerializedName("telephony_records")
    public List <TelephonyObservationWrapper> telephonyRecords;
    @SerializedName("traffic_records")
    public List <TrafficObservationWrapper> trafficRecords;

    public Report(Context context) {
        this.simRecord = SimSingleton.getInstance(context);
        this.deviceRecord = DeviceSingleton.getInstance(context);
        this.cdmaRecords = CdmaObservationWrapper.listAll(CdmaObservationWrapper.class);
        this.connectivityRecords = ConnectivityObservationWrapper.listAll(ConnectivityObservationWrapper.class);
        this.gsmRecords = GsmObservationWrapper.listAll(GsmObservationWrapper.class);
        this.stateRecords = StateChangeWrapper.listAll(StateChangeWrapper.class);
        this.telephonyRecords = TelephonyObservationWrapper.listAll(TelephonyObservationWrapper.class);
        this.trafficRecords = TrafficObservationWrapper.listAll(TrafficObservationWrapper.class);
    }

    public boolean recordsToSend(){
        boolean cdma = this.cdmaRecords.isEmpty();
        boolean connectivity = this.connectivityRecords.isEmpty();
        boolean gsm = this.gsmRecords.isEmpty();
        boolean state = this.stateRecords.isEmpty();
        boolean telephony = this.telephonyRecords.isEmpty();
        boolean traffic = this.trafficRecords.isEmpty();
        return !(cdma && connectivity && gsm && state && telephony && traffic);
    }

    public void cleanDBRecords(){
        CdmaObservationWrapper.deleteAll(CdmaObservationWrapper.class);
        GsmObservationWrapper.deleteAll(GsmObservationWrapper.class);
        SampleWrapper.deleteAll(SampleWrapper.class);
        ConnectivityObservationWrapper.deleteAll(ConnectivityObservationWrapper.class);
        StateChangeWrapper.deleteAll(StateChangeWrapper.class);
        TelephonyObservationWrapper.deleteAll(TelephonyObservationWrapper.class);
        TrafficObservationWrapper.deleteAll(TrafficObservationWrapper.class);
    }

    public void saveVisualSamples(){
        int lastType = -1;
        for (ConnectivityObservationWrapper observation : connectivityRecords){
            ConnectionModeSample sample = new ConnectionModeSample(observation);
            if (sample.getType() == lastType)
                continue;
            else {
                sample.save();
                lastType = sample.getType();
            }
        }

        lastType = -1;
        for (GsmObservationWrapper observation : gsmRecords){
            NetworkTypeSample sample = new NetworkTypeSample(observation);
            if (sample.getType() == lastType)
                continue;
            else {
                sample.save();
                lastType = sample.getType();
            }
        }

        // Backup ApplicationTraffic
        Iterator<TrafficObservationWrapper> iterator = TrafficObservationWrapper.findAsIterator(TrafficObservationWrapper.class, "uid > 0");
        while(iterator.hasNext()){
            ApplicationTraffic value = new ApplicationTraffic(iterator.next());
            ApplicationTraffic refValue = ApplicationTraffic.findFirst(ApplicationTraffic.class,
                    "uid = ? and timestamp = ? and network_type = ?",
                    Integer.toString(value.uid),
                    Long.toString(value.timestamp),
                    Integer.toString(value.networkType));
            if(refValue == null)
                value.save();
            else{
                refValue.txBytes += value.txBytes;
                refValue.rxBytes += value.rxBytes;
                //refValue.tcpRxBytes += value.tcpRxBytes;
                //refValue.tcpTxBytes += value.tcpTxBytes;
                refValue.save();
            }

        }
    }
}
