package de.teammartens.android.wattfinder.worker;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.fragments.ChargeeventDialog;
import de.teammartens.android.wattfinder.model.Saeule;

/**
 * Created by felix on 02.11.14.
 */

//TODO ClusterItem Clicked wenn es ein CLuster ist dann zoomen

public class SaeulenWorks {
    private static final String LOG_TAG = "Wattfinder SäulenWorks";
    private static final float[] Marker_Colors = {BitmapDescriptorFactory.HUE_RED,(float) 220,(float) 200, BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_ORANGE};
    private static final Integer[] Markers = {R.drawable.marker_1,R.drawable.marker_1,R.drawable.marker_2,R.drawable.marker_3,R.drawable.marker_4,R.drawable.marker_5};
    private static final Integer[] Markers_Fault = {R.drawable.marker_1_h,R.drawable.marker_1_h,R.drawable.marker_2_h,R.drawable.marker_3_h,R.drawable.marker_4_h,R.drawable.marker_5_h};
    private static final Integer[] Markers_Clicked = {R.drawable.marker_1_c,R.drawable.marker_1_c,R.drawable.marker_2_c,R.drawable.marker_3_c,R.drawable.marker_4_c,R.drawable.marker_5_c};
    private static final Integer[] Markers_Fault_Clicked = {R.drawable.marker_1_ch,R.drawable.marker_1_ch, R.drawable.marker_2_ch,R.drawable.marker_3_ch,R.drawable.marker_4_ch,R.drawable.marker_5_ch};

    private static final Integer CACHE_OUTDATED_MILLIS =  1800000;//30min
    private static final Integer CACHE_EXPIRED_MILLIS = 86400000;//24Stunden
    private static final String fAPIUrl = "https://api.goingelectric.de/chargepoints/";
    private static final int REFRESH_TIME = 600000;
    private static ArrayList<Marker> Marker_Saeule = new ArrayList<Marker>();
    private static ArrayList<MarkerOptions> Marker_Saeule_Options = new ArrayList<MarkerOptions>();
    private static ArrayList<Integer> Saeulen_ID = new ArrayList<Integer>();
    public static ClusterManager<Saeule> mClusterManager;
    private static meinClusterRenderer mClusterRenderer;
    public static ConcurrentHashMap<Integer,Saeule> Saeulen = new ConcurrentHashMap<>();
    public static Long letzterAbrufTime = new Long(0);
    private static LatLngBounds letzterAbrufBeiLLB = new LatLngBounds(new LatLng(0,0),new LatLng(0,0));
    private static int letzterAbrufSaeulen = 0;
    private static HashMap<Long,LatLngBounds> CachedRegion = new HashMap<>();
    private static int letzterAbrufFilter;
    public static Saeule currentSaeule;
    public static Marker clickedMarker;
    private static Boolean RQ_PENDING = false;
    private static String RQ_URL = "";
    private static final String RQ_TAG = "RQ_Marker";
    private static Toast T;
    private static final int bounds_x = 16;
    private static final int bounds_y = 8;
    private static Long requestStartTime = 0l;



    public static void reloadMarker(){
        //nur wenn Map auch sichtbar ist:
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "LadeMarker");
             LatLngBounds llB = KartenActivity.mMap.getProjection().getVisibleRegion().latLngBounds;
             ladeMarker(llB.southwest.latitude,llB.southwest.longitude,llB.northeast.latitude,llB.northeast.longitude, "reloadMarker",0);

    }




    public static void ladeMarker(final Double swlat, final Double swlng, final Double nwlat, final Double nwlng, String VERURSACHER, final Integer startkey) {


        ladeMarker(swlat, swlng,nwlat, nwlng, VERURSACHER, startkey, FilterWorks.ParamFilterString());

    }


    public static void ladeMarker(final Double swlat, final Double swlng, final Double nwlat, final Double nwlng, final String VERURSACHER, final Integer startkey, final String PARAMS) {
        //typ=SucheZoom&verbund%5B%5D=alle&stecker%5B%5D=alle&swlat=49.8902426508026&swlng=9.69743094140631&nwlat=50.505597811031045&nwlng=10.50492605859381&kostenlos=false&oeffnungszeiten=false&kostenlosparken=false&hotel=false&restaurant=false&fotos=false&verifiziert=false&notverifiziert=false

        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Suche Säulen durch Verursacher:" + VERURSACHER);



        //DEBUG


        // if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Suche Säulen: URI"+uri);
        if (!FilterWorks.filter_initialized()){
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Suche Säulen abgebrochen. Filter nicht initialisiert");
            return;
        }

        if(GeoWorks.getMapZoom()<GeoWorks.MAX_ZOOM ||
                (KartenActivity.layoutStyle()!="land" && (nwlat - swlat)>bounds_x || (nwlng - swlng)>bounds_y )
                || (KartenActivity.layoutStyle()=="land" && ((nwlat - swlat)>bounds_y || (nwlng - swlng)>bounds_x))
                )
        {

            if (GeoWorks.getMapZoom()>0 && !AnimationWorker.startupScreen)
            {   if(T != null) T.cancel();
                T.makeText(KartenActivity.getInstance(), KartenActivity.getInstance().getString(R.string.mapviewlarge),Toast.LENGTH_SHORT).show();
            }
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Suche Säulen abgebrochen. Kartenausschnitt zu groß. "+GeoWorks.getMapZoom()+"  "+nwlat+"/"+nwlng+"  "+swlat+"/"+swlng);

            return;
        }

        String url=fAPIUrl + "?key=" + KartenActivity.getInstance().getString(R.string.GoingElectric_APIKEY) + "&" + PARAMS +
                "&startkey=" + startkey +
                "&sw_lat="+String.valueOf(swlat)+
                "&sw_lng="+String.valueOf(swlng)+
                "&ne_lat="+String.valueOf(nwlat)+
                "&ne_lng="+String.valueOf(nwlng);
        if (!RQ_PENDING||!RQ_URL.equals(url)) {

            if (T!=null)
                T.cancel();


            final int hash = FilterWorks.paramsHash();


            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "JSONUrl:" + url);
            requestStartTime = System.currentTimeMillis();
            JsonObjectRequest pRequest = new JsonObjectRequest(Request.Method.GET,
                    url, (String) null, new Response.Listener<JSONObject>() {


                @Override
                public void onResponse(JSONObject jResponse) {
                    if ((System.currentTimeMillis() - requestStartTime)>10000){
                        NetWorker.setNetworkQuality(1);
                    }
                    if ((System.currentTimeMillis() - requestStartTime)<2000){
                        NetWorker.rehabilateNetworkQuality();
                    }
                    RQ_PENDING = false;
                    //NetWorker.resetRETRY();
                    AnimationWorker.hide_mapLoading();
                    try {
                        if (jResponse.getString("status").contentEquals("ok")) {
                            AnimationWorker.hideStartup();
                            //Redundantes Aufrufen wenn startkey gefunden und positiv
                            if (jResponse.optInt("startkey", 0) > 0)
                                ladeMarker(swlat, swlng, nwlat, nwlng, "startkey", jResponse.optInt("startkey", 0), PARAMS);

                            if (LogWorker.isVERBOSE())
                                LogWorker.d(LOG_TAG, "Response erhalten, starte AsyncTask");
                            letzterAbrufBeiLLB = new LatLngBounds(new LatLng(swlat, swlng), new LatLng(nwlat, nwlng));
                            letzterAbrufFilter = hash;
                            letzterAbrufTime=System.currentTimeMillis();
                            letzterAbrufSaeulen = jResponse.getJSONArray("chargelocations").length();
                            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,letzterAbrufSaeulen + "Säulen empfangen.");
                            CachedRegion.put(System.currentTimeMillis(), letzterAbrufBeiLLB);

                            if (startkey == 0 ) {//Neuer VErsuch, jedes Mal alle MArker entfernen:
                                Saeulen.clear();
                                mClusterManager.clearItems();
                            }



                            new erzeugeMarkerTask(KartenActivity.getInstance(), KartenActivity.mMap).execute(jResponse); //Ergebnis Async verarbeiten wegen UI Thread blocking
                        } else {

                            Toast.makeText(KartenActivity.getInstance(), "Fehler beim Abrufen der Säulen.", Toast.LENGTH_LONG).show();
                            if (LogWorker.isVERBOSE())
                                LogWorker.d(LOG_TAG, "ERROR:" + jResponse.getString("status"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetWorker.handleError(error,NetWorker.TASK_SAEULEN);
                    RQ_PENDING = false;
                }
            });



                if (startkey>0||!duplicateRQ(hash)) {
                    if (RQ_PENDING)
                    {// Cancel pending Requests
                        KartenActivity.getInstance().cancelPendingRequests(RQ_TAG);
                        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Pending Requests canceled.");
                    }
                    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Request added.");
                    requestStartTime=System.currentTimeMillis();
                    KartenActivity.incAPI_RQ_Count();
                    pRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3,2));
                    KartenActivity.getInstance().addToRequestQueue(pRequest,RQ_TAG);
                    RQ_PENDING = true;RQ_URL=url;

                    AnimationWorker.show_mapLoading();
                } else if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "Request dropped, hashs&LLB matching.");


            }
        else{
        if (LogWorker.isVERBOSE())
            LogWorker.d(LOG_TAG, "Request dropped, last one still pending.");
        }
    }


    private static class erzeugeMarkerTask extends AsyncTask<JSONObject, MarkerOptions, Integer> {
        private final Context mCntxt;
        private final GoogleMap mGMap;

// ---------------------------------------------------------

        public erzeugeMarkerTask(Context cntxt, GoogleMap gmap) {
            this.mCntxt = cntxt;
            this.mGMap = gmap;
        }

        @Override
        protected Integer doInBackground(JSONObject... jResponse) {
            Integer response = 0;

            JSONObject jO = jResponse[0];

            try {

                JSONArray jsonArray = jO.getJSONArray("chargelocations");
                final int length = jsonArray.length();
                if (LogWorker.isVERBOSE()) LogWorker.d("AsyncMarkerWorks", "Marker gefunden: "+length);
                Marker_Saeule_Options.clear();

                for (int i = 0; i < length; i++) {
                    if (Saeulen.get(jsonArray.getJSONObject(i).getInt("ge_id")) == null) {
                        Saeule S = new Saeule(jsonArray.getJSONObject(i));

                        //JSON Decoding moved to class Saeule
                        Saeulen.put(S.getID(), S);
                        mClusterManager.addItem(S);
                        //Why do we need this duplicate?

                        response++;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (jO.optInt("startkey", 0)==0)
                ladeEvents();
            return response;
        }

        @Override
        protected void onPostExecute(Integer result) {

                if (LogWorker.isVERBOSE()) LogWorker.d("AsyncMarkerWorks", "Habe " + result + "/"+Saeulen.size()+" Marker erzeugt");
                //mClusterManager.addItems((Collection) Saeulen);
                //mGMap.animateCamera(CameraUpdateFactory.zoomTo(12), 500, null);
            mClusterManager.cluster();

        }




}



public static void ladeEvents(){
    //Lade ChargeEvents

    // Integer[] i = (Integer[]) Saeulen.keySet().toArray();

    // while (n*10<Saeulen.keySet().size()) {

             /*   for(int ii=n*10;ii<((n*10+10)<Saeulen.keySet().size()?(n*10+10):Saeulen.keySet().size());ii++)
                {

                    saeulenids += "&points[]=" + i[ii];
                }*/


    String evUrl = "https://wattfinder.de/api/get.php";
    if (LogWorker.isVERBOSE()) LogWorker.d("AsyncMarkerWorks", "JSON Request " + evUrl);

     Map<String, String> params = new HashMap<String, String>();
        params.put("key",KartenActivity.getInstance().getString(R.string.Wattfinder_APIKey));
        params.put("p","0");
        params.put("pointsArray",new JSONArray(Saeulen.keySet()).toString());

    JsonObjectRequest pRequest = new JsonObjectRequest(Request.Method.POST,
            evUrl,new JSONObject(params), new Response.Listener<JSONObject>() {


        @Override
        public void onResponse(JSONObject jResponse) {
            if (LogWorker.isVERBOSE())
                LogWorker.d("AsyncMarkerWorks", "JSON Response " + jResponse.toString());
            try{
                JSONArray jA = jResponse.getJSONArray("points");
                if(jResponse.optBoolean("success", false))
                    for (int i = 0; i < jA.length(); i++) {
                        JSONObject jO = jA.getJSONObject(i);
                        if (LogWorker.isVERBOSE())
                            LogWorker.d("AsyncMarkerWorks", "JSON Response ID " + jO.toString());

                        Saeule S=Saeulen.get(jO.getInt("id"));
                        Integer c = jO.getInt("count");
                        if (S != null) {
                            mClusterManager.removeItem(S);
                            S.setEventCount(c);
                            mClusterManager.addItem(S);
                            if(LogWorker.isVERBOSE()&&c>0)LogWorker.d("AsyncEventJSON",S.getID()+": EventCount:"+S.getEventCount());
                            Saeulen.put(S.getID(),S);
                        }
                    }
                mClusterManager.cluster();

            } catch (JSONException jE) {
                LogWorker.e("JSON Event Count", jE.getLocalizedMessage());
            }


        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }){/*@Override
        protected Map<String,String> getParams() throws AuthFailureError{
        Map<String, String> params = new HashMap<String, String>();
        params.put("key",KartenActivity.getInstance().getString(R.string.Wattfinder_APIKey));
        params.put("p","0");
        int i =0;
        for(Integer object: Saeulen.keySet()){
            params.put("points["+(i++)+"]", String.valueOf(object));
        }
        return params;
        }*/




            @Override
            public Map<String, String> getHeaders() {
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json; charset=utf-8");
            headers.put("User-agent", "My useragent");
            return headers;
        }

    };

    KartenActivity.getInstance().addToRequestQueue(pRequest);



}
    public static void setUpClusterer() {
        // Declare a variable for the cluster manager.

        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"setUpClusterer");
        // Position the map.
//        KartenActivity.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng (KartenActivity.mMap.getMyLocation().getLatitude(),KartenActivity.mMap.getMyLocation().getLongitude()), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<Saeule>(KartenActivity.getInstance(), KartenActivity.mMap);


        mClusterManager.setOnClusterInfoWindowClickListener(new ClusterManager.OnClusterInfoWindowClickListener<Saeule>() {
            @Override
            public void onClusterInfoWindowClick(Cluster<Saeule> cluster) {

            }
        });
        /*
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Saeule>() {

        });*/
        mClusterRenderer= new meinClusterRenderer(KartenActivity.getInstance(),KartenActivity.mMap,mClusterManager);
        mClusterManager.setOnClusterItemClickListener(mClusterRenderer);
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Saeule>() {
            @Override
            public boolean onClusterClick(Cluster<Saeule> cluster) {

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                Iterator<Saeule> it = cluster.getItems().iterator();
                while (it.hasNext()) {
                    builder.include(it.next().getPosition());
                }
                KartenActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50), 500, null);
                return true;
            }
        });

        KartenActivity.mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {

                //clickedMarker=arg0;


                MarkerBounce(arg0);
                if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Bounce Marker Bounce  "+arg0.getTitle());
                return null;
            }

            @Override
            public View getInfoContents(Marker arg0) {
                return null;
            }
        });
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        KartenActivity.mMap.setOnCameraIdleListener(mClusterManager);
        KartenActivity.mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setRenderer(mClusterRenderer);

    }

    private static void MarkerBounce(final Marker marker) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1200;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                marker.setAnchor(0.3f, 1.0f + 1.1f * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });






    }

    public static void resetClickMarker(){
        if(clickedMarker !=null && currentSaeule !=null){
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(Markers[0]);
            if ( currentSaeule.getTyp() < (Markers.length+1) && currentSaeule.getTyp() >= 0 )
                if (currentSaeule.isFaultreport())
                    icon = BitmapDescriptorFactory.fromResource(Markers_Fault[currentSaeule.getTyp()]);
                else
                    icon = BitmapDescriptorFactory.fromResource(Markers[currentSaeule.getTyp()]);
            try {
                clickedMarker.setIcon(icon);
            }
            catch(IllegalArgumentException e){
                LogWorker.e(LOG_TAG,"ILLEGAL ARGUMENT on setIcon" + e.toString());

            }
        }
    }






    private static class meinClusterRenderer extends DefaultClusterRenderer<Saeule> implements GoogleMap.OnCameraIdleListener,GoogleMap.OnMarkerClickListener, ClusterManager.OnClusterItemClickListener<Saeule> {

        GoogleMap mMap;
        public meinClusterRenderer(Context context, GoogleMap map,
                                   ClusterManager<Saeule> clusterManager) {

            super(context, map, clusterManager);
            mMap=map;
        }

        @Override
        public boolean onClusterItemClick(Saeule item) {
            Marker marker = getMarker(item);
            resetClickMarker();
            if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"OnCLusterItemCLickListener  "+item.getID());
            currentSaeule = item;
            clickedMarker=marker;
            MarkerBounce(marker);
            //marker.setIcon(BitmapDescriptorFactory.fromResource(Markers_Clicked[currentSaeule.getTyp()]));
            if(clickedMarker !=null && currentSaeule !=null){
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(Markers[0]);

                if ( currentSaeule.getTyp() < Markers.length && currentSaeule.getTyp() >= 0 )
                    if (currentSaeule.isFaultreport())
                        icon = BitmapDescriptorFactory.fromResource(Markers_Fault_Clicked[currentSaeule.getTyp()]);
                    else
                        icon = BitmapDescriptorFactory.fromResource(Markers_Clicked[currentSaeule.getTyp()]);
                try {
                    clickedMarker.setIcon(icon);
                }
                catch(IllegalArgumentException e){
                    LogWorker.e(LOG_TAG,"ILLEGAL ARGUMENT on setIcon" + e.toString());

                }
            }
            //GeoWorks.animateClick();
            GeoWorks.movemapPosition(item.getPosition(),"MarkerClick");
            if(AnimationWorker.isFilterVisibile()) AnimationWorker.toggleFilter();
            SaeulenWorks.populateInfoContainer(item,true);
           // if(!AnimationWorker.isDetailsVisibile()) AnimationWorker.show_details(item);



            return true;
        }

        @Override
        protected void onBeforeClusterItemRendered(Saeule item,
                                                   MarkerOptions markerOptions) {
            super.onBeforeClusterItemRendered(item, markerOptions);
            //markerOptions.title(String.valueOf(item.getID()));
            Integer m = (item.isFaultreport()?Markers_Fault[item.getTyp()]:Markers[item.getTyp()]);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(m));
            //markerOptions.title(String.valueOf(item.getID()));
            //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(Marker_Colors[item.getTyp()]));
        }

        @Override
        protected void onClusterItemRendered(Saeule clusterItem,
                                             Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);

        }

        @Override
        public boolean onMarkerClick(final Marker marker) {
            Saeule S = getClusterItem(marker);
            LogWorker.d(LOG_TAG,"Marker Click:" + S.getName());
            return false;
        }

        @Override
        public void onCameraIdle() {
            CameraPosition cameraPosition = mMap.getCameraPosition();
            LatLng mLatLng = cameraPosition.target;
            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "onCameraChange:"+ " Detailsvisible:" + AnimationWorker.isDetailsVisibile() + " MapZoom" + GeoWorks.getMapZoom() + " cpZoom" + cameraPosition.zoom);

            //Wenn Versatz dann vorher rausrechnen
            if (GeoWorks.validLatLng(mLatLng)) {
                GeoWorks.setMapPosition(
                        //nicht mehr nötig mit MapPadding
                       //(GeoWorks.isPositionversetzt() ? GeoWorks.VersatzBerechnen(mLatLng, true) : mLatLng),
                        mLatLng,
                        //wenn DetailsVisible dann mapZoom nicht ändern
                        AnimationWorker.isDetailsVisibile() ? GeoWorks.getMapZoom() : cameraPosition.zoom);
                Location L = new Location("APP");
                Location D = new Location("APP");
                L.setLatitude(mLatLng.latitude);
                L.setLongitude(mLatLng.longitude);

                SaeulenWorks.checkMarkerCache("SaeulenWorks.onCameraChange");


                if (GeoWorks.MarkerTarget != null) {
                    D.setLatitude(GeoWorks.MarkerTarget.latitude);
                    D.setLongitude(GeoWorks.MarkerTarget.longitude);
                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "Distance " + L.distanceTo(D) + "--" + GeoWorks.CUSTOM_MAPVIEW);
                    if (L.distanceTo(D) > 500)
                        GeoWorks.CUSTOM_MAPVIEW = true;
                }


            }


        }

    }
    public static  void populateInfoContainer(){populateInfoContainer(getCurrentSaeule(),false);}
    private static  void populateInfoContainer(boolean show){populateInfoContainer(getCurrentSaeule(),show);}
    private static  void populateInfoContainer(Saeule mSaeule){populateInfoContainer(mSaeule,false);}

    private static  void populateInfoContainer(Saeule mSaeule,boolean show) {
        View infoView = KartenActivity.getInstance().findViewById(R.id.InfoContainer);

        if (infoView != null && mSaeule != null) {
            setCurrentSaeule(mSaeule);
            View v = infoView.findViewById(R.id.loadingPanel);
            v.setVisibility(View.VISIBLE);
            TextView t2 = (TextView) infoView.findViewById(R.id.iName);
            if(t2!=null){
                t2.setText(mSaeule.getName());
                t2.setVisibility(View.VISIBLE);

            }
            else return;
            String mTitel = "Ladepunkt: " + mSaeule.getName();

            t2 = (TextView) infoView.findViewById(R.id.iEvCount);
            int evc = mSaeule.getEventCount();
            t2.setText((evc>0?evc:"Keine")+ " Bewertung"+(evc>1?"en":""));
            if (evc<0)t2.setVisibility(View.INVISIBLE);

            t2 = (TextView) infoView.findViewById(R.id.iAdresse);
            t2.setText(mSaeule.getAddress());
            mTitel = mTitel + ", " + mSaeule.getAddress();
            t2 = (TextView) infoView.findViewById(R.id.iAnschluesse);
            t2.setText(mSaeule.getChargepoints());
            t2 = (TextView) infoView.findViewById(R.id.iEntfernung);
            t2.setVisibility(View.GONE);
            if (GeoWorks.getSuchPosition()!= null && GeoWorks.isAround(GeoWorks.getSuchPosition()))
            {
                t2.setText(KartenActivity.getInstance().getResources().getString(R.string.infoEntfernungZiel)+" "+GeoWorks.distanceToString(mSaeule.getPosition(),GeoWorks.getSuchPosition()));
                t2.setVisibility(View.VISIBLE);
            }else
            {
                if (GeoWorks.isAround(GeoWorks.getmyPosition())){
                    t2.setText(KartenActivity.getInstance().getResources().getString(R.string.infoEntfernungPos)+" "+GeoWorks.distanceToString(mSaeule.getPosition(),GeoWorks.getmyPosition()));
                    t2.setVisibility(View.VISIBLE);
                }
            }

            t2 = (TextView) infoView.findViewById(R.id.iUpdated);
            t2.setText(KartenActivity.getInstance().getString(R.string.infoUpdated)+mSaeule.getUpdatedString());
            v = (View) infoView.findViewById(R.id.icard_fault);
            if(mSaeule.isFaultreport()) v.setVisibility(View.VISIBLE); else v.setVisibility(View.GONE);

            v = infoView.findViewById(R.id.loadingPanel);
            v.setVisibility(View.GONE);

            // float f = (infoView.getHeight()/KartenActivity.getDisplayH())*1.0f;
            // if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"hole Info "+ (mSaeule!=null?mSaeule.getName():"") +" H:"+infoView.getHeight() +" f:"+f);
            KartenActivity.setMapPaddingY(infoView.getHeight());
            if (AnimationWorker.getSTATE()==AnimationWorker.STATE_DETAIL)
                AnimationWorker.show_details(mSaeule);
            else
                AnimationWorker.show_info();
        }
    }

    public static void resetInfoView(){
        View infoView = KartenActivity.getInstance().findViewById(R.id.InfoContainer);
        if(infoView!=null) {
            TextView t = (TextView) infoView.findViewById(R.id.iSaeulenid);
            t.setText("");
            t = (TextView) infoView.findViewById(R.id.iAdresse);
            t.setText("");
            t = (TextView) infoView.findViewById(R.id.iEntfernung);
            t.setText("");
            t = (TextView) infoView.findViewById(R.id.iAnschluesse);
            t.setText("");
            t = (TextView) infoView.findViewById(R.id.iUpdated);
            t.setText("");
            View v = infoView.findViewById(R.id.icard_fault);
            v.setVisibility(View.GONE);
            v = KartenActivity.getInstance().findViewById(R.id.fab_directions);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + currentSaeule.getPosition().latitude + "," + currentSaeule.getPosition().longitude + "?q=" + currentSaeule.getPosition().latitude + "," + currentSaeule.getPosition().longitude + "(" + currentSaeule.getName() + ")"));
                    if (intent.resolveActivity(KartenActivity.getInstance().getPackageManager()) != null) {
                        KartenActivity.getInstance().startActivity(intent);
                    }
                }
            });
            v = infoView.findViewById(R.id.create_chargeevent);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChargeeventDialog Dialog = new ChargeeventDialog();
                    Dialog.show(KartenActivity.fragmentManager,"ChargeEvent");

                }
            });
            v = infoView.findViewById(R.id.loadingPanel);
            v.setVisibility(View.VISIBLE);

            infoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AnimationWorker.show_details();
                }
            });

        }
    }

    public static void checkMarkerCache(String VERURSACHER){
        CameraPosition cp = KartenActivity.mMap.getCameraPosition();
        LatLngBounds llB = KartenActivity.mMap.getProjection().getVisibleRegion().latLngBounds;
        //Abruf nur starten wenn eine der Ecken des aktuellen Ausshcnitts beim letzten Abruf nicht erfasst wurde
        if ( !duplicateRQ(FilterWorks.paramsHash())) {
        if((!llB.northeast.equals(new LatLng(0,0))&&!llB.southwest.equals(new LatLng(0,0)))) {

            //Factor for overlap depending on zoomlevel
            double f = 1.0;
            if (cp.zoom < 9.5) f = 0.5;
            if (cp.zoom < 8.5 || NetWorker.getNetworkQuality()<3) f = 0.2;
            if (cp.zoom < 8.0 || NetWorker.getNetworkQuality()<2) f = 0;
            double lat_overlap = f * (llB.northeast.latitude - llB.southwest.latitude);
            double lng_overlap = f * (llB.northeast.longitude - llB.southwest.longitude);

            ladeMarker(llB.southwest.latitude - lat_overlap, llB.southwest.longitude - lng_overlap, llB.northeast.latitude + lat_overlap, llB.northeast.longitude + lng_overlap, VERURSACHER, 0);
        }
        }else{
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Marker nicht neu geladen, da Abschnitt bekannt.");
        }


    }

public static boolean duplicateRQ(int hash){
    LatLngBounds llB = KartenActivity.mMap.getProjection().getVisibleRegion().latLngBounds;
    if (    (hash==letzterAbrufFilter) &&
            letzterAbrufBeiLLB.contains(llB.northeast) &&
            letzterAbrufBeiLLB.contains(llB.southwest)&&
            letzterAbrufSaeulen > 0 &&
            (System.currentTimeMillis()-letzterAbrufTime)<REFRESH_TIME)

            return true;
    else {

        if(LogWorker.isVERBOSE()){
            if(hash!=letzterAbrufFilter)LogWorker.d(LOG_TAG,"DUPLICATE CHECK: hash"+hash+"!="+letzterAbrufFilter);
            else if(!letzterAbrufBeiLLB.contains(llB.northeast))LogWorker.d(LOG_TAG,"DUPLICATE CHECK: northeast"+llB.northeast+"("+letzterAbrufBeiLLB+")");
            else if(!letzterAbrufBeiLLB.contains(llB.southwest))LogWorker.d(LOG_TAG,"DUPLICATE CHECK: southwest"+llB.southwest+"("+letzterAbrufBeiLLB+")");
            else if((System.currentTimeMillis()-letzterAbrufTime)>REFRESH_TIME)LogWorker.d(LOG_TAG,"DUPLICATE CHECK: refresh time"+(System.currentTimeMillis()-letzterAbrufTime)+"("+letzterAbrufTime+"/"+REFRESH_TIME+")");
        }
        return false;
    }
}



    public static void reset(){
        letzterAbrufBeiLLB=new LatLngBounds(new LatLng(0,0),new LatLng(0,0));
        letzterAbrufTime=0l;
        letzterAbrufFilter=0;
        RQ_URL="";
        RQ_PENDING=false;
    }

    public static Saeule getCurrentSaeule() {
        return currentSaeule;
    }

    public static void setCurrentSaeule(Saeule currentSaeule) {
        SaeulenWorks.currentSaeule = currentSaeule;
    }
}
