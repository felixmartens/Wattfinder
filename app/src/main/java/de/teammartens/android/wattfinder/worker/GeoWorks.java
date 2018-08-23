package de.teammartens.android.wattfinder.worker;

import android.animation.Animator;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;

import static de.teammartens.android.wattfinder.KartenActivity.Loc2LatLng;
import static de.teammartens.android.wattfinder.KartenActivity.fragmentManager;
import static de.teammartens.android.wattfinder.KartenActivity.getInstance;
import static de.teammartens.android.wattfinder.KartenActivity.layoutStyle;
import static de.teammartens.android.wattfinder.KartenActivity.mMap;

/**
 * Created by felix on 02.11.14.
 */
public class GeoWorks {
    private static final String LOG_TAG = "WattfinderGeoWorks";



    public static final Float DEFAULT_ZOOM = 10.5f;
    public static final Float DETAIL_ZOOM = 17f;
    public static final Float MY_LOCATION_ZOOM = 13.5f;
    public static final Float MAX_ZOOM = 6.0f; // wenn Zoom kleiner werde keine Säulen mehr geladen wiel kartenausschnitt zu groß
    private static String mQuery="";
    public static boolean CUSTOM_MAPVIEW=false;
    public static LatLng MarkerTarget;
    public static LatLng myPosition,mapPosition, suchPosition,lastCountryPosition;
    public static String suchString,countryCode;
    private static final String[] countryCodes = {"de","nl","be","no","fr","ch","at","dk"};
    private static final double NordSuedLat = 50.20;
    private static final double WestOstLng = 10.085;
    private static final int LOCATION_INTERVAL = 10000;
    private static final int LOCATION_MIN_INTERVAL = 5000;
    private static final int aroundDistance = 3000;
    public static final Float defaultLat = 52.5170365f;
    public static final Float defaultLng = 13.3888599f;
    public static final LatLng defaultLatLng = new LatLng(defaultLat,defaultLng);


    private static boolean location_permission = false;
    private static LocationCallback mLocationCallback;
    public static FusedLocationProviderClient mFusedLocationClient;
    private static LocationRequest mLocationRequest;

    public static Float mapZoom;
    public static Marker Marker_Ich, Marker_Suche = null;

    public static LatLng getmyPosition() {
        if(myPosition==null||!location_permission||!validLatLng(myPosition))return null;
        return myPosition;
    }

    public static void setmyPosition(boolean moveMap) {
        setmyPosition(getmyPosition(),getMapZoom(),moveMap);
    }

    public static void setmyPosition() {
        setmyPosition(getmyPosition());
    }

    public static void setmyPosition(LatLng mapPosition) {
        setmyPosition(mapPosition,MY_LOCATION_ZOOM,!CUSTOM_MAPVIEW);
    }

    public static void setmyPosition(LatLng mPosition,Float zoom, boolean moveMap) {

        if (mPosition != null &&validLatLng(mPosition) ){
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"setMyPosition mPosition="+mPosition);
            if (KartenActivity.mMap != null) {
             if (Marker_Ich != null) Marker_Ich.remove();
             Marker_Ich = KartenActivity.mMap.addMarker(new MarkerOptions().position(mPosition)
                    .title("Meine Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_standort)));
                if (moveMap||!CUSTOM_MAPVIEW)
                    movemapPosition(mPosition, zoom, "setmyPositionZoom");
            }
            myPosition = mPosition;

            findmyCountry();

        }else
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"setMyPosition mPosition= null!");

    }

    public static void setMyPosition(Location myPosition) {
        setMyPosition(myPosition,!CUSTOM_MAPVIEW);
    }

    public static void setMyPosition(Location myPosition, boolean moveMap) {
        if (myPosition!=null) {
            LatLng myloc = Loc2LatLng(myPosition);
            setmyPosition(myloc,MY_LOCATION_ZOOM,moveMap);
            //if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Location erhalten:" + (myPosition == null ? "null" : myPosition.toString()));

        }
    }

    public static void movemapPosition(final String VERURSACHER){
        movemapPosition(getMapPosition(),getMapZoom(),VERURSACHER);
    }



    public static void movemapPosition(final LatLng mapPosition, final String VERURSACHER){
        if(mapPosition!=null){
        movemapPosition(mapPosition,getMapZoom(),VERURSACHER);
        }
    }




    public static void movemapPosition(final LatLng position, final float zoom, final String VERURSACHER){
       if(position!=null&& mMap != null&&zoom>2.0f) {

            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap to "+position.toString() + "/"+zoom+" wegen "+VERURSACHER);
            LatLng nPosition = new LatLng(position.latitude,position.longitude);

            Float currentZoom = mMap.getCameraPosition().zoom;



            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap: MapPosition "+(getMapPosition()!=null?getMapPosition().toString():"null") + " :: mMap"+(mMap!=null?mMap.getCameraPosition().target.toString():"null")+" :: nPostion "+nPosition.toString());
            if (!mMap.getCameraPosition().target.equals(nPosition)||zoom!=currentZoom) {
                //Hier wird jetzt unterschieden welche Animation gewählt wird

                CameraUpdate CU = CameraUpdateFactory.zoomTo(zoom);

                if(GeoWorks.mapPosition==null)mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,zoom));
                else {
                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "moveMap to " + nPosition.toString());
                    //zuerst prüfen wie groß ist der Abstand zwischen altem und neuen Mittelpunkt
                    Location alt = new Location("MapLocation");
                    Location neu = new Location("NewLocation");
                    Float D = 0f;

                    alt.setLatitude(GeoWorks.mapPosition.latitude);
                    alt.setLongitude(GeoWorks.mapPosition.longitude);
                    neu.setLatitude(nPosition.latitude);
                    neu.setLongitude(nPosition.longitude);
                    D = alt.distanceTo(neu);
                    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap D=" + D);
                       /* if(D>100000f){
                            //wenn Abstand größer als 100km dann move und zoom in
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nPosition, 5));
                            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap move&zoom "+nPosition.toString());
                            CU = CameraUpdateFactory.zoomTo(zoom);
                        }else{
                            // kleinerer Abstand dann abhängig, wenn zoom identsich nur bewegen, sonst noch zoomen

                            */
                    if (zoom == currentZoom)
                        CU = CameraUpdateFactory.newLatLng(nPosition);
                    else {
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(nPosition));
                              /*  if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap to "+nPosition.toString()+" zoomBy "+(zoom-currentZoom)+"("+currentZoom+"-"+zoom+")");
                                //CU = CameraUpdateFactory.zoomBy(zoom-currentZoom);

                                CU = CameraUpdateFactory.newLatLngZoom(nPosition, zoom);*/
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(nPosition));
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, "moveMap move&zoom " + nPosition.toString());
                        CU = CameraUpdateFactory.zoomTo(zoom);
                    }

                    // }


                    mMap.animateCamera(CU, 1000, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            if (LogWorker.isVERBOSE())
                                LogWorker.d(LOG_TAG, "moveMap animateFinish -" + mMap.getCameraPosition().target.toString() + " -Zoom:" + zoom + " Verursacher:" + VERURSACHER);


                            SaeulenWorks.checkMarkerCache(VERURSACHER);
                        }

                        @Override
                        public void onCancel() {
                            if (LogWorker.isVERBOSE())
                                LogWorker.d(LOG_TAG, "moveMap animateCancel -Zoom:" + zoom + " Verursacher:" + VERURSACHER);

                        }
                    });
                }
            }else if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap Position unverändert");

            //GeoWorks.mapPosition = position;
            //GeoWorks.mapZoom = zoom;
        }
    }


    public static Float getMapZoom() {
        return (mapZoom!=null?mapZoom:DEFAULT_ZOOM);
    }

    public static LatLng getMapPosition() {
        return (mapPosition==null?defaultLatLng:mapPosition);
    }

    public static void setMapPosition(LatLng mapPosition) {
        GeoWorks.mapPosition = mapPosition;
    }

    public static void setMapPosition(LatLng mapPosition,Float zoom) {
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"setMapPosition:"+mapPosition.toString()+" z:"+zoom);
        GeoWorks.mapPosition = mapPosition;
        GeoWorks.mapZoom = zoom;
    }


    public static String distanceToString(LatLng Coord1,LatLng Coord2){
        Float distance = 0f;

        distance=distanceToFloat(Coord1,Coord2);

        String Distance = "";

        if (distance>1000){
            Distance = String.format("%.1f",distance/1000)+" km";
        }else{
            Distance = String.format("%.0f",distance)+" m";
        }

        return Distance;
    }


    public static Float distanceToFloat(LatLng Coord1,LatLng Coord2){
        Float distance = 0f;
        Location alt = new Location("MapLocation");
        Location neu = new Location("MapLocation");

        if (Coord1== null || Coord2 == null) {if (LogWorker.isVERBOSE()) Log.e(LOG_TAG,"Distance2Float NPE: Coord1"+Coord1+" - Coord2:"+Coord2);return 0f;}
        alt.setLatitude(Coord1.latitude);
        alt.setLongitude(Coord1.longitude);
        neu.setLatitude(Coord2.latitude);
        neu.setLongitude(Coord2.longitude);
        distance=alt.distanceTo(neu);
        return distance;

    }


    public static boolean isAround (LatLng Coord){

        return (distanceToFloat(Coord,getMapPosition())<aroundDistance);
    }



    public static void starteSuche(String query){
        Address A = new Address(Locale.GERMANY);
        if(query==null){LogWorker.e(LOG_TAG, "EMPTY Search Triggered" + query);}else{
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Search Triggered"+query);
            Geocoder GC = new Geocoder(KartenActivity.getInstance());
            if (GC.isPresent()){
                try {
                    List<Address> list = GC.getFromLocationName(query, 1);
                    if(list != null&&list.size()>0){
                         A = list.get(0);
                        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Gecode Result latitude "+ A.getLatitude() + "longitude "+ A.getLongitude()+"at "+ A.getAddressLine(0));
                        Suchmarker(new LatLng(A.getLatitude(),A.getLongitude()), A.getAddressLine(0) );
                        //movemapPosition(new LatLng(A.getLatitude(),A.getLongitude()),MY_LOCATION_ZOOM,"starteSuche");

                    }
                }catch(IOException e){
                    if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"starteSuche IOError "+e.getLocalizedMessage());
                }

            }else{
                Toast.makeText(KartenActivity.getInstance(),"Error!! Geocoding not available",Toast.LENGTH_LONG).show();
            }


/*
        //LatLng result = getLatLongFromAddress(query);
        String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                URLEncoder.encode(query) + "&sensor=true&language=de&region=eu";

        JsonObjectRequest req = new JsonObjectRequest(uri, null,
                new Response.Listener<JSONObject>() {


                    @Override
                    public void onResponse(JSONObject response) {


                        try {


                            double lng = ((JSONArray)response.get("results")).getJSONObject(0)
                                    .getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lng");

                            double lat = ((JSONArray)response.get("results")).getJSONObject(0)
                                    .getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lat");
                            String fAddress = ((JSONArray)response.get("results")).getJSONObject(0)
                                    .getString("formatted_address");



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });

// Access the RequestQueue through your singleton class.
        KartenActivity.getInstance().addToRequestQueue(req);
*/
        }


    }

    /*

    gleiche Funktion wie oben aber diesmal nicht suchen nur anzeigen
     */
    public static void starteSucheSuggested(String query){
        if(query==null){LogWorker.e(LOG_TAG, "EMPTY Suggested Search Triggered" + query);}else{


            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Suggested Search Triggered"+query);



            String[] M1 = query.split("::");

            String fAddress = M1[0];
            Double mLat = Double.parseDouble(M1[1]);
            Double mLng = Double.parseDouble(M1[2]);

            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"latitude "+ mLat);
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"longitude "+ mLng);
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"at "+ fAddress);
            LatLng l = new LatLng(mLat,mLng);

            Suchmarker(l, fAddress);




                    }
            }

    public static LatLng getSuchPosition() {
        return suchPosition;
    }

    public static String getSuchString() {
        return suchString;
    }

    public static void Suchmarker(){
        Suchmarker(getSuchPosition(),getSuchString(),false, true);
    }

    public static void Suchmarker(boolean move){
        Suchmarker(getSuchPosition(),getSuchString(),false, move);
    }


    public static void Suchmarker(LatLng Coord, String Desc){
        Suchmarker(Coord,Desc,false, true);
    }

    public static void Suchmarker(LatLng Coord, String Desc, boolean detailzoom){
        Suchmarker(Coord,Desc,detailzoom, true);
    }

    public static void Suchmarker(LatLng Coord, String Desc, boolean detail_zoom, boolean move){

        if (mMap != null) {



            if (validLatLng(Coord) && !Desc.isEmpty()){
                suchPosition = Coord;
                suchString = Desc;
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Create Marker Suche " + Desc);
                if (Marker_Suche !=null) Marker_Suche.remove();
                Marker_Suche = mMap.addMarker(new MarkerOptions().position(Coord).title(Desc).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_suche)));

                CUSTOM_MAPVIEW=true;
                //AnimationWorker.show_map();

                
                if(move)movemapPosition(Coord,(detail_zoom?DETAIL_ZOOM:MY_LOCATION_ZOOM),"GeoWorks.Suchmarker");

            }

            //SaeulenWorks.ladeMarker(Coord.latitude, Coord.longitude, "GeoWorks.Suchmarker");

        }
    }






    public static boolean isPositionversetzt(){
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"isPositionVersetzt: Details:"+AnimationWorker.isDetailsVisibile()+" Filter:"+AnimationWorker.isFilterVisibile()+" layout:"+layoutStyle());

        if(AnimationWorker.isDetailsVisibile()||
                (AnimationWorker.isFilterVisibile()&&!KartenActivity.layoutStyle().equals("default")))
        {
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"isPositionVersetzt:true");


            return true;
        }

        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"isPositionVersetzt:false");
        return false;
    }


    public static boolean validLatLng (LatLng mLatLng){

        LatLngBounds llb = new LatLngBounds(new LatLng(33.724339, -22.93945), new LatLng(66.478208, 34.628906));

        if(mLatLng== null ) return false;
        //if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"mLatLng: "+mLatLng+" "+llb.contains(mLatLng));
        return (llb != null
                && llb.contains(mLatLng));
    }


    /* get Counrry Code for choosing list of cards*/

    public static void findmyCountry(){
        final LatLng P = getMapPosition();

        if (P!=null&&(lastCountryPosition==null||distanceToFloat(P,lastCountryPosition)>100000)) {
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"findmyCountry Geocoder gestartet weil: lastCountry:"+(lastCountryPosition==null?"null":"notnull")+" - Distanz: "+distanceToFloat(P,lastCountryPosition));

            Geocoder GC = new Geocoder(KartenActivity.getInstance(),Locale.getDefault());
            if (GC.isPresent()) {
                try {
                    List<Address> list = GC.getFromLocation(P.latitude, P.longitude, 1);
                    if (list.size()>0) {
                        String s = list.get(0).getCountryCode().toLowerCase();

                        if (Arrays.asList(countryCodes).contains(s)) {
                            lastCountryPosition=P;
                            if (s.equalsIgnoreCase("de")) {
                                if (P.latitude < NordSuedLat) {
                                    GeoWorks.countryCode = "de_sued";
                                } else {
                                    if (P.longitude > WestOstLng) {
                                        GeoWorks.countryCode = "de_ne";
                                    } else {
                                        GeoWorks.countryCode = "de_nw";
                                    }

                                }
                            } else {
                                GeoWorks.countryCode = s;
                            }
                        } else {
                            GeoWorks.countryCode = "de_nw";
                        }
                        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"findmyCountry Geocoder Result: -"+s+"- -"+getCountryCode());

                    }


                } catch (IOException e) {
                    if(countryCode==null) GeoWorks.countryCode = "de_nw"; // alles so lassen außer es ist null
                    if(LogWorker.isVERBOSE())LogWorker.e(LOG_TAG,"GEoCoding Error: "+e.getLocalizedMessage()+e.getCause());
                }
            }
        }
    }

    public static String getCountryCode() {
        return countryCode;
    }




    /*
    Handle Locations
     */



    public static void removeLocationListener(){
        if(location_permission&&KartenActivity.checkPermissionLocation(KartenActivity.MY_PERMISSIONS_REMOVE_LOCATION)
                &&mFusedLocationClient!= null&&mLocationCallback!=null) {

            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }
    public static void setupLocationListener(){


        if(KartenActivity.checkPermissionLocation(KartenActivity.MY_PERMISSIONS_REQUEST_LOCATION)) {
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        GeoWorks.setMyPosition(location);
                    }
                }


            };

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(LOCATION_INTERVAL);
            mLocationRequest.setFastestInterval(LOCATION_MIN_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            try{
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);


                 mFusedLocationClient.getLastLocation()
                     .addOnSuccessListener(KartenActivity.getInstance(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                       // Got last known location. In some rare situations this can be null.
                       if (location != null) {
                           setMyPosition(location);
                           if(LogWorker.isVERBOSE())Log.d(LOG_TAG,"got LastLocation"+location.toString()+"/"+location.getProvider());
                       }
                          else  if(LogWorker.isVERBOSE())Log.d(LOG_TAG,"got LastLocation null");
                      }
                    });

            }catch(SecurityException e) {
                Log.e(LOG_TAG, e.getStackTrace().toString());
            }

        /*
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


            Criteria C = new Criteria();
            C.setAccuracy(Criteria.ACCURACY_FINE);
            C.setPowerRequirement(Criteria.POWER_MEDIUM);
            C.setSpeedRequired(false);
            C.setBearingRequired(false);
            Location mLocation = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(C,false));


            // Getting Current Location as of GPS


            if(mLocation!=null){
                GeoWorks.setmyPosition(Loc2LatLng(mLocation));
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "getMyPosition "+(GeoWorks.getmyPosition() == null?"null":"notnull")+"CUSTOMMapView:"+GeoWorks.CUSTOM_MAPVIEW );
            }

            if(mLocation!=null && GeoWorks.validLatLng(Loc2LatLng(mLocation)))
                AnimationWorker.show_myloc();
            else
                AnimationWorker.hide_myloc();

            mLocationManager.removeUpdates(mLocationListener);

            mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(C,true), 10000, 100, mLocationListener);
        */
        }else{
            //if Permission is denied und Map ist not correctly iniatilized
            if(KartenActivity.mMap!=null) {
                CameraPosition cp = KartenActivity.mMap.getCameraPosition();
                if (cp.zoom < 5 || !validLatLng(cp.target)) {
                    //Karten nicht zentriert
                    if (getMapPosition() == null) KartenActivity.setMapCenter();
                    else setMapPosition(getMapPosition());
                }
            }
        }

    }


    public static boolean isLocation_permission() {
        return location_permission;
    }

    public static void setLocation_permission(boolean location_permission) {
        GeoWorks.location_permission = location_permission;
    }

    public static void init_searchfragment(){
        Fragment f = KartenActivity.getInstance().getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment) f;

       if(autocompleteFragment==null){
           FragmentTransaction fT = fragmentManager.beginTransaction();
           fT.add(R.id.place_autocomplete_fragment, Fragment.instantiate(getInstance(), SupportPlaceAutocompleteFragment.class.getName()), AnimationWorker.FLAG_SEARCH).commit();
           autocompleteFragment = (SupportPlaceAutocompleteFragment)fragmentManager.findFragmentById(R.id.place_autocomplete_fragment);

       }

       autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "Place: " + place.getName() + "  TYPE:" + place.getPlaceTypes().contains(Place.TYPE_STREET_ADDRESS));//get place details here
                Suchmarker(place.getLatLng(), place.getName().toString(), place.getPlaceTypes().contains(Place.TYPE_STREET_ADDRESS));
                //AnimationWorker.hide_mapSearch();
                AnimationWorker.setSTATE(AnimationWorker.STATE_SEARCH);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                LogWorker.e(LOG_TAG, "An error occurred: " + status);
            }
        });
        //autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input).requestFocus();
        autocompleteFragment.getView().setVisibility(View.GONE);
    }

}
