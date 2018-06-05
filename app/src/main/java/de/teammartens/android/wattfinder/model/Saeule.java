package de.teammartens.android.wattfinder.model;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.worker.LogWorker;

/**
 * Created by felix on 29.12.16.
 */

public class Saeule implements ClusterItem {
    private  LatLng mPosition;
    private final Integer mID;
    private  Integer mTyp=0;
    private String mChargepoints="";
    private String mAddress="";
    private String mName="";
    private boolean faultreport=false;
    private Integer ev_count=0;



    private Long updated;

    public Saeule(Integer ID,String name) {
        mID = ID;
        mName = name;
        setUpdated();
    }


    public Saeule(JSONObject jO) {
        mID = jO.optInt("ge_id",0);
        try {
             //if (LogWorker.isVERBOSE()) LogWorker.d("AsyncMarkerWorks", M.getString("name"));
        JSONObject O = jO.getJSONObject("coordinates");
        setPosition(new LatLng(O.getDouble("lat"), O.getDouble("lng")));

        O = jO.getJSONObject("address");
        setAddress(O.getString("street") + ", " + O.getString("postcode") + " " + O.getString("city"));

        JSONArray A = jO.getJSONArray("chargepoints");
        String[] chargepoints = new String[A.length()];
        Double pMax = 0.0; //um die Farbe des Icons zu bestimmen

        for (int n = 0; n < A.length(); n++) {
            O = A.getJSONObject(n);
            chargepoints[n] = O.getInt("count") + "x " + O.getString("type") + " " + O.getDouble("power") + "kW";
            if (O.getDouble("power") > pMax) pMax = O.getDouble("power");
        }
        setChargepoints(TextUtils.join("," + KartenActivity.lineSeparator, chargepoints));
        //Bestimme Typ des Markers
        Integer sTyp = 0;
        if (pMax > 0) sTyp = 1;
        if (pMax >= 11) sTyp = 2;
        if (pMax >= 20) sTyp = 3;
        if (pMax >= 43) sTyp = 4;
        if (pMax >= 100) sTyp = 5;
        setTyp(sTyp);

        setFaultreport(jO.optBoolean("fault_report", false));
        setEventCount(-1);
    }catch(JSONException e){
        if(LogWorker.isVERBOSE()&&e!=null) Log.e("SaeuleConst","JSON ERROR: "+e.getLocalizedMessage());
        Toast.makeText(KartenActivity.getInstance(),"Error decoding JSON repsonse",Toast.LENGTH_SHORT).show();
    }

        setUpdated();
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public Integer getID() {
        return mID;
    }


    public Integer getTyp() {
        return mTyp;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
        setUpdated();
    }

    public void setChargepoints(String mChargepoints) {
        this.mChargepoints = mChargepoints; setUpdated();
    }

    public void setPosition(LatLng mPosition) {
        setUpdated();

        this.mPosition = mPosition;

    }

    public void setTyp(Integer mTyp) {
        this.mTyp = mTyp; setUpdated();
    }

    public String getAddress() {
        return mAddress;
    }

    public String getChargepoints() {
        return mChargepoints;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String getTitle(){
     return mName;
    }
    @Override
    public String getSnippet(){
        return mAddress;
    }

    public Long getUpdated(){
        return updated;
    }
    public String getUpdatedString() {
        String format = "dd.MM HH:mm";
       if ( DateUtils.isToday(updated) ) format = "HH:mm";
        //TODO GESTERN ergänzen
        //if ( DateUtils.isToday (updated-24*3600*1000) ) format = "gestern, HH:mm";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.GERMANY);
        return dateFormat.format(new Date());

    }

    private void setUpdated(){
        this.updated = System.currentTimeMillis();
    }

    public boolean isFaultreport() {
        return faultreport;
    }

    public void setFaultreport(boolean faultreport) {
        this.faultreport = faultreport;
    }

    public Integer getEventCount() {
        return (ev_count==null?0:ev_count);
    }

    public void setEventCount(Integer ev_count) {
        this.ev_count = ev_count;
    }
}
