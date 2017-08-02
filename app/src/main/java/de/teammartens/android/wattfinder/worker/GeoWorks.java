package de.teammartens.android.wattfinder.worker;

import android.content.res.Configuration;
import android.location.Location;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;

import static de.teammartens.android.wattfinder.KartenActivity.layoutStyle;
import static de.teammartens.android.wattfinder.KartenActivity.mMap;

/**
 * Created by felix on 02.11.14.
 */
public class GeoWorks {
    private static final String LOG_TAG = "WattfinderGeoWorks";


    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final Integer MeinMarkerIcon = R.drawable.marker_standort;
    private static final String API_KEY = "@string/google_maps_key";

    public static final int GEOCODE = 1;
    public static final int REVERSE_GEOCODE = 2;
    public static final int LADE_SAEULEN = 3;
    public static final int LADE_MINIINFO = 4;
    public static final int HOLE_DETAILS = 5;
    public static final int AUTOCOMPLETE = 9;
    public static final Float DEFAULT_ZOOM = 10.5f;
    public static final Float DETAIL_ZOOM = 17f;
    public static final Float MAX_ZOOM = 6.0f; // wenn Zoom kleiner werde keine Säulen mehr geladen wiel kartenausschnitt zu groß
    private static int ACTION = 0;
    private static String mQuery="";
    public static boolean CUSTOM_MAPVIEW=false;
    public static LatLng MarkerTarget;
    public static LatLng myPosition,mapPosition,animTarget;


    public static Float mapZoom;
    public static Marker Marker_Ich, Marker_Suche = null;

    public static LatLng getmyPosition() {
        return myPosition;
    }

    public static void setmyPosition(LatLng mapPosition) {
        setmyPosition(mapPosition,DEFAULT_ZOOM);
    }
    public static void setmyPosition(LatLng mPosition,Float zoom) {

        if (mPosition != null ){
            if (KartenActivity.mMap != null) {
            Location alt = new Location("LastLocation");
            Location neu = new Location("NewLocation");
            Float D =  1000f;
            if (myPosition!=null){
                alt.setLatitude(myPosition.latitude);
                alt.setLongitude(myPosition.longitude);
                neu.setLatitude(mPosition.latitude);
                neu.setLongitude(mPosition.longitude);
                D=alt.distanceTo(neu);
            }



            if (Marker_Ich != null) Marker_Ich.remove();
            Marker_Ich = KartenActivity.mMap.addMarker(new MarkerOptions().position(mPosition)
                .title("Meine Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_standort)));
            if (!CUSTOM_MAPVIEW && D > 100)
                movemapPosition(mPosition, zoom, "setmyPositionZoom");
        }
            myPosition = mPosition;
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

        if(position!=null) {
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap to "+position.toString() + "/"+zoom+" wegen "+VERURSACHER);
            LatLng nPosition = new LatLng(position.latitude,position.longitude);

            Float currentZoom = mMap.getCameraPosition().zoom;



            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap: MapPosition "+(getMapPosition()!=null?getMapPosition().toString():"null") + " :: mMap"+(mMap!=null?mMap.getCameraPosition().target.toString():"null")+" :: nPostion "+nPosition.toString());
            if (!mMap.getCameraPosition().target.equals(nPosition)||zoom!=currentZoom) {
                //Hier wird jetzt unterschieden welche Animation gewählt wird
                if(getMapPosition()==null)setMapPosition(new LatLng(0,0),0f);
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap to "+nPosition.toString());
                //zuerst prüfen wie groß ist der Abstand zwischen altem und neuen Mittelpunkt
                    Location alt = new Location("MapLocation");
                    Location neu = new Location("NewLocation");
                    Float D =  0f;

                        alt.setLatitude(GeoWorks.mapPosition.latitude);
                        alt.setLongitude(GeoWorks.mapPosition.longitude);
                        neu.setLatitude(nPosition.latitude);
                        neu.setLongitude(nPosition.longitude);
                        D=alt.distanceTo(neu);
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap D="+D);
                        CameraUpdate CU = CameraUpdateFactory.zoomTo(zoom);
                       /* if(D>100000f){
                            //wenn Abstand größer als 100km dann move und zoom in
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nPosition, 5));
                            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap move&zoom "+nPosition.toString());
                            CU = CameraUpdateFactory.zoomTo(zoom);
                        }else{
                            // kleinerer Abstand dann abhängig, wenn zoom identsich nur bewegen, sonst noch zoomen

                            */
                            if(zoom==currentZoom)
                                    CU = CameraUpdateFactory.newLatLng(nPosition);
                            else{
                                //mMap.moveCamera(CameraUpdateFactory.newLatLng(nPosition));
                              /*  if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap to "+nPosition.toString()+" zoomBy "+(zoom-currentZoom)+"("+currentZoom+"-"+zoom+")");
                                //CU = CameraUpdateFactory.zoomBy(zoom-currentZoom);

                                CU = CameraUpdateFactory.newLatLngZoom(nPosition, zoom);*/
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(nPosition));
                                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap move&zoom "+nPosition.toString());
                                CU = CameraUpdateFactory.zoomTo(zoom);
                            }

                       // }


                mMap.animateCamera(CU, 1000, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, "moveMap animateFinish -"+ mMap.getCameraPosition().target.toString()+" -Zoom:" + zoom +" Verursacher:" + VERURSACHER);

                        // nach dem Zoomen in den Details den Versatz nochmal korrigieren, deshalb zweite Animation starten
                        if ((AnimationWorker.isDetailsVisibile()||AnimationWorker.isFilterVisibile()||VERURSACHER=="showMapBackPress")&&VERURSACHER!="moveMap")
                                movemapPosition(position, zoom, "moveMap");

                        SaeulenWorks.checkMarkerCache(VERURSACHER);
                    }

                    @Override
                    public void onCancel() {
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, "moveMap animateCancel -Zoom:" + zoom + " Verursacher:" + VERURSACHER);

                    }
                });
            }else if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "moveMap Position unverändert");

            //GeoWorks.mapPosition = position;
            //GeoWorks.mapZoom = zoom;
        }
    }


    public static Float getMapZoom() {
        return (mapZoom!=null?mapZoom:DEFAULT_ZOOM);
    }

    public static LatLng getMapPosition() {
        return mapPosition;
    }

    public static void setMapPosition(LatLng mapPosition) {
        GeoWorks.mapPosition = mapPosition;
    }

    public static void setMapPosition(LatLng mapPosition,Float zoom) {
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"setMapPosition:"+mapPosition.toString()+" z:"+zoom);
        GeoWorks.mapPosition = mapPosition;
        GeoWorks.mapZoom = zoom;
    }




    public static void starteSuche(String query){
        if(query==null){LogWorker.e(LOG_TAG, "EMPTY Search Triggered" + query);}else{


        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Search Triggered"+query);
        mQuery=query;
        //LatLng result = getLatLongFromAddress(query);
        String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                URLEncoder.encode(query) + "&sensor=true&language=de&region=eu";

        CUSTOM_MAPVIEW=true;
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

                            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"latitude "+ lat);
                            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"longitude "+ lng);
                            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"at "+ fAddress);
                            LatLng l = new LatLng(lat,lng);

                            Suchmarker(l, fAddress);

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

        }


    }

    /*

    gleiche Funktion wie oben aber diesmal nicht suchen nur anzeigen
     */
    public static void starteSucheSuggested(String query){
        if(query==null){LogWorker.e(LOG_TAG, "EMPTY Suggested Search Triggered" + query);}else{


            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Suggested Search Triggered"+query);
            mQuery=query;



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

/*
public static void animateClick(){
    //Diese wird nur per MarkerClick ausgelöst, deshalb immer move true

    if(KartenActivity.isDetailsVisibile())
        animateClick(true,true);
    else
        animateClick(false,true);

}

    public static void animateClick(boolean nachObenversetzt){
        //Das sind die automatischen dahe rimmer move false
        animateClick(nachObenversetzt,false);
    }
    public static void animateClick(boolean nachObenversetzt, boolean move){
        //wenn move dann bewege die karte zum geklickten marker
        //wenn nicht dann bewege einfach den mittelpunkt der karte
        View v = KartenActivity.getInstance().findViewById(R.id.detailFragment);
        Double hP = KartenActivity.getInstance().getResources().getDisplayMetrics().heightPixels*1.0;
        //if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Display ratio Calc: "+hP+"-"+v.getHeight()+"--"+String.format("%+10.2f",((hP-v.getHeight()*1.0)/hP)*0.5)+ "---" +String.valueOf(((hP-v.getHeight()*1.0)/hP)*0.5));

        Double d = 0.5-((hP-v.getHeight()*1.0)/hP)*0.5;
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Display ratio Animate Click: "+d);
        LatLng cLatLng = mMap.getCameraPosition().target;
        if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Animate Click MOVE"+move+" CUSTOM"+CUSTOM_MAPVIEW);
        if (move||!CUSTOM_MAPVIEW)  {
        if (SaeulenWorks.clickedSaeule!= null)
            cLatLng =  SaeulenWorks.clickedSaeule.getPosition();
        if (SaeulenWorks.clickedMarker!= null)
            cLatLng =  SaeulenWorks.clickedMarker.getPosition();

            CUSTOM_MAPVIEW=false;
        }
        LatLng nLatLng = cLatLng;
        if (nachObenversetzt) {
            LatLngBounds VR = mMap.getProjection().getVisibleRegion().latLngBounds;
            Configuration config = KartenActivity.getInstance().getResources().getConfiguration();
            if (config.orientation == config.ORIENTATION_PORTRAIT){
                Double deltaY = d * (VR.northeast.latitude - VR.southwest.latitude);
                nLatLng=new LatLng(cLatLng.latitude - deltaY, cLatLng.longitude);
            }else
            {
                Double deltaX = 0.25 * (VR.northeast.longitude - VR.southwest.longitude);
                nLatLng=new LatLng(cLatLng.latitude, cLatLng.longitude+deltaX);
            }

        }else{
            //KartenActivity.show_info();

            //HI könnte theoretisch noch rein dass auch bei CUTOM MAPVIEW die karte wieder korrigiert wird beim shcliessen von details.
        }
        MarkerTarget=nLatLng;

        mMap.animateCamera(CameraUpdateFactory.newLatLng(nLatLng), 500,null);


    }

*/


    public static void Suchmarker(LatLng Coord,String Desc){

        if (mMap != null) {

            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Create Marker Suche " + Desc);

            if (Coord != null && !Desc.isEmpty()){
                if (Marker_Suche !=null) Marker_Suche.remove();
                Marker_Suche = mMap.addMarker(new MarkerOptions().position(Coord).title(Desc).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_suche)));

                CUSTOM_MAPVIEW=true;
                AnimationWorker.hide_info();
                if(AnimationWorker.isDetailsVisibile())AnimationWorker.toggleDetails();
                if(AnimationWorker.isFilterVisibile())AnimationWorker.toggleFilter();
                movemapPosition(Coord,12f,"GeoWorks.Suchmarker");

            }

            //SaeulenWorks.ladeMarker(Coord.latitude, Coord.longitude, "GeoWorks.Suchmarker");

        }
    }

    public void Lademarker(){


    }






    private static ArrayList<String> resultList;

    private ArrayList<String> autocomplete(String input) {

        StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
        sb.append("?key=" + API_KEY);
        sb.append("&components=country:de");
        try {
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = sb.toString();
        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {


                    @Override
                    public void onResponse(JSONObject response) {

                        JSONArray predsJsonArray = null;
                        try {

                            predsJsonArray = response.getJSONArray("predictions");


                        // Extract the Place descriptions from the results
                            resultList = new ArrayList<String>(predsJsonArray.length());
                            for (int i = 0; i < predsJsonArray.length(); i++) {
                                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
                            }

                            //Jetzt noch in Autocomplete einfügen

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



        return resultList;
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

/*
    private static LatLng VersatzBerechnen(LatLng mLatLng) {
    return VersatzBerechnen(mLatLng,false);
    }

    public static LatLng VersatzBerechnen(LatLng mLatLng, boolean reverse){
        LatLng vLatLng = mLatLng;
        /*LatLngBounds VR = mMap.getProjection().getVisibleRegion().latLngBounds;
        View v = KartenActivity.getInstance().findViewById(R.id.detailFragment);
        if (isFilterVisibile()&&layoutStyle()!="default") v = KartenActivity.getInstance().findViewById(R.id.filterFragment);
        Double hP = KartenActivity.getInstance().getResources().getDisplayMetrics().heightPixels*1.0;

        Double d = ((hP-v.getHeight()*1.0)/hP)*0;


        Configuration config = KartenActivity.getInstance().getResources().getConfiguration();
        if (config.orientation == config.ORIENTATION_PORTRAIT){
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Display ratio Calc: "+hP+"-"+v.getHeight()+"--"+String.format("%+10.2f",d)+ "---" +String.valueOf(((hP-v.getHeight()*1.0)/hP)*0.5));

            Double deltaY = d * (VR.northeast.latitude - VR.southwest.latitude);
            vLatLng=new LatLng((reverse?mLatLng.latitude + deltaY:mLatLng.latitude - deltaY), mLatLng.longitude);
        }else
        {    hP = KartenActivity.getInstance().getResources().getDisplayMetrics().widthPixels*1.0;
            d = ((hP-v.getWidth()*1.0)/hP)*0.4;
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Display ratio Calc: "+hP+"-"+v.getWidth()+"--"+String.format("%+10.2f",d)+ "---" +String.valueOf(((hP-v.getWidth()*1.0)/hP)*0.5));



            Double deltaX = d * (VR.northeast.longitude - VR.southwest.longitude);
            vLatLng=new LatLng(mLatLng.latitude, (reverse?mLatLng.longitude-deltaX:mLatLng.longitude+deltaX));
        }

        return vLatLng;
    }
*/

    public static boolean validLatLng (LatLng mLatLng){

        LatLngBounds llb = new LatLngBounds(new LatLng(33.724339, -22.93945), new LatLng(66.478208, 34.628906));
        if (llb.contains(mLatLng)) return true;

        return false;
    }
}
