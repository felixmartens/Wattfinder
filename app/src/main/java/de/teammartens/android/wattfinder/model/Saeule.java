package de.teammartens.android.wattfinder.model;

import android.text.format.DateUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.text.SimpleDateFormat;
import java.util.Date;

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
        mID = new Integer(ID);
        mName = name;
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
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
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
