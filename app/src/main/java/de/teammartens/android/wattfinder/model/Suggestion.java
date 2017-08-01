package de.teammartens.android.wattfinder.model;

import com.google.android.gms.maps.model.LatLng;

import de.teammartens.android.wattfinder.R;

/**
 * Created by felix on 02.11.14.
 */
public class Suggestion {
    private String description;
    private String formatted_address;
    private LatLng coordinates;
    private int icon = R.drawable.ic_place;
    private int id;

    public Suggestion(){

    }

    public Suggestion (int ID,String faddress, String desc, Double Lat, Double Lng){
        this.description=desc;
        this.formatted_address=faddress;
        this.id=ID;
        this.coordinates = new LatLng(Lat,Lng);

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }
    public String getCoordString() {
        return String.valueOf(coordinates.latitude)+":"+String.valueOf(coordinates.longitude);
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }
}
