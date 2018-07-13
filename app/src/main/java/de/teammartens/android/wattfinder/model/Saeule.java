package de.teammartens.android.wattfinder.model;

import android.os.Parcel;
import android.os.Parcelable;
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

public class Saeule implements ClusterItem,Parcelable {
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
           mName =  jO.getString("name");

        JSONObject O = jO.getJSONObject("coordinates");
        setPosition(new LatLng(O.getDouble("lat"), O.getDouble("lng")));

        O = jO.getJSONObject("address");
        setAddress(O.getString("street") + ", " + O.getString("postcode") + " " + O.getString("city"));

        JSONArray A = jO.getJSONArray("chargepoints");
        Double pMax = 0.0; //um die Farbe des Icons zu bestimmen
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < A.length(); n++) {
            O = A.getJSONObject(n);
            if(n>0)sb.append("," + KartenActivity.lineSeparator);
            sb.append(O.getInt("count") + "x " + O.getString("type") + " " + O.getDouble("power") + "kW");
            if (O.getDouble("power") > pMax) pMax = O.getDouble("power");
        }
        setChargepoints(sb.toString());
        //Bestimme Typ des Markers
        Integer sTyp = 0;
        if (pMax >= 100) sTyp = 5;
        else  if (pMax >= 43) sTyp = 4;
            else     if (pMax >= 20) sTyp = 3;
                else  if (pMax >= 11) sTyp = 2;
                    else if (pMax > 0) sTyp = 1;
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


    //----------------------------------------------
    // Parcelable Interface

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Saeule createFromParcel(Parcel in) {
            return new Saeule(in);
        }

        public Saeule[] newArray(int size) {
            return new Saeule[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(mID);
        dest.writeInt(mTyp);
        dest.writeString(mName);
        dest.writeString(mAddress);
        dest.writeDouble(mPosition.latitude);
        dest.writeDouble(mPosition.longitude);
        dest.writeString(mChargepoints);
        dest.writeInt((faultreport?1:0));
        dest.writeInt(ev_count);
        dest.writeLong(updated);

    }

    public Saeule(Parcel pIn) {
        this.mID = pIn.readInt();
        this.mTyp = pIn.readInt();
        this.mName = pIn.readString();
        this.mAddress = pIn.readString();
        this.mPosition = new LatLng(pIn.readDouble(),pIn.readDouble());
        this.mChargepoints = pIn.readString();
        this.faultreport = (pIn.readInt()>0?true:false);
        this.ev_count = pIn.readInt();
        this.updated = pIn.readLong();
    }
}
