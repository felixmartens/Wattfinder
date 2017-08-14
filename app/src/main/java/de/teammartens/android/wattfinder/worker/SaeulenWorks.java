package de.teammartens.android.wattfinder.worker;

import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.fragments.MiniInfoFragment;
import de.teammartens.android.wattfinder.model.Saeule;

/**
 * Created by felix on 02.11.14.
 */

//TODO ClusterItem Clicked wenn es ein CLuster ist dann zoomen

public class SaeulenWorks {
    private static final String LOG_TAG = "Wattfinder SäulenWorks";
    private static final float[] Marker_Colors = {BitmapDescriptorFactory.HUE_RED,(float) 220,(float) 200, BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_ORANGE};
    private static final Integer[] Markers = {R.drawable.marker_1,R.drawable.marker_1,R.drawable.marker_2,R.drawable.marker_3,R.drawable.marker_4};
    private static final Integer[] Markers_Fault = {R.drawable.marker_1_h,R.drawable.marker_1_h,R.drawable.marker_2_h,R.drawable.marker_3_h,R.drawable.marker_4_h};
    private static final Integer CACHE_OUTDATED_MILLIS =  1800000;//30min
    private static final Integer CACHE_EXPIRED_MILLIS = 86400000;//24Stunden
    private static final String fAPIUrl = "https://api.goingelectric.de/chargepoints/";
    private static final int REFRESH_TIME = 600000;
    private static ArrayList<Marker> Marker_Saeule = new ArrayList<Marker>();
    private static ArrayList<MarkerOptions> Marker_Saeule_Options = new ArrayList<MarkerOptions>();
    private static ArrayList<Integer> Saeulen_ID = new ArrayList<Integer>();
    public static ClusterManager<Saeule> mClusterManager;
    public static HashMap<Integer,Saeule> Saeulen = new HashMap<>();
    public static Long letzterAbrufTime = new Long(0);
    private static LatLngBounds letzterAbrufBeiLLB = new LatLngBounds(new LatLng(0,0),new LatLng(0,0));
    private static int letzterAbrufSaeulen = 0;
    private static HashMap<Long,LatLngBounds> CachedRegion = new HashMap<>();
    private static HashMap<Integer,Saeule> SaeulenCache = new HashMap<>();
    private static int letzterAbrufFilter;
    public static Saeule clickedSaeule;
    public static Marker clickedMarker;
    private static Boolean RQ_PENDING = false;
    private static String RQ_URL = "";
    private static final String RQ_TAG = "RQ_Marker";
    private static Toast T;




    public static void reloadMarker(){
        //nur wenn Map auch sichtbar ist:
        Fragment f =  KartenActivity.fragmentManager.findFragmentById(R.id.map);
        Configuration config = KartenActivity.getInstance().getResources().getConfiguration();
        if (config.orientation != config.ORIENTATION_PORTRAIT||!AnimationWorker.isFilterVisibile()){
                   if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "LadeMarker");
             LatLngBounds llB = KartenActivity.mMap.getProjection().getVisibleRegion().latLngBounds;
             ladeMarker(llB.southwest.latitude,llB.southwest.longitude,llB.northeast.latitude,llB.northeast.longitude, "reloadMarker",0);
        }
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
                (KartenActivity.layoutStyle()!="land" && (nwlat - swlat)>8 || (nwlng - swlng)>4 )
                || (KartenActivity.layoutStyle()=="land" && ((nwlat - swlat)>4 || (nwlng - swlng)>8))
                )
        {

            if (GeoWorks.getMapZoom()>0 && !AnimationWorker.startupScreen)T.makeText(KartenActivity.getInstance(), KartenActivity.getInstance().getString(R.string.mapviewlarge),Toast.LENGTH_LONG).show();
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

            JsonObjectRequest pRequest = new JsonObjectRequest(Request.Method.GET,
                    url, (String) null, new Response.Listener<JSONObject>() {


                @Override
                public void onResponse(JSONObject jResponse) {

                    RQ_PENDING = false;
                    NetWorker.resetRETRY();
                    AnimationWorker.hide_mapLoading();
                    try {
                        if (jResponse.getString("status").contentEquals("ok")) {
                            AnimationWorker.hideStartup();
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
                            //Redundantes Aufrufen wenn startkey gefunden und positiv
                            if (jResponse.optInt("startkey", 0) > 0)
                                ladeMarker(swlat, swlng, nwlat, nwlng, "startkey", jResponse.optInt("startkey", 0), PARAMS);



                            new erzeugeMarkerTask(KartenActivity.getInstance(), KartenActivity.mMap).execute(jResponse); //Ergebnis Async verarbeiten wegen UI Thread blocking
                        } else {

                            Toast.makeText(KartenActivity.getInstance(), "Fehler beim Abrufen der Säulen.", Toast.LENGTH_LONG);
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
                    LogWorker.d(LOG_TAG, error.getMessage());
                    RQ_PENDING = false;
                }
            });



                if (!duplicateRQ(hash)) {
                    if (RQ_PENDING)
                    {// Cancel pending Requests
                        KartenActivity.getInstance().cancelPendingRequests(RQ_TAG);
                        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Pending Requests canceled.");
                    }
                    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Request added.");

                    KartenActivity.incAPI_RQ_Count();
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
                    JSONObject M = new JSONObject();
                    M = jsonArray.getJSONObject(i);


                    int id = M.getInt("ge_id");

                    Saeule S = new Saeule(id,M.getString("name"));
                    //if (LogWorker.isVERBOSE()) LogWorker.d("AsyncMarkerWorks", M.getString("name"));
                    JSONObject O = M.getJSONObject("coordinates");
                    S.setPosition(new LatLng(O.getDouble("lat"), O.getDouble("lng")));

                    O = M.getJSONObject("address");
                    S.setAddress(O.getString("street")+", "+O.getString("postcode")+" "+O.getString("city"));

                    JSONArray A = M.getJSONArray("chargepoints");
                    String[] chargepoints=new String[A.length()];
                    Double pMax = 0.0; //um die Farbe des Icons zu bestimmen

                    for (int n=0;n<A.length();n++){
                        O = A.getJSONObject(n);
                        chargepoints[n]=O.getInt("count")+"x "+O.getString("type")+" "+O.getDouble("power")+"kW";
                        if (O.getDouble("power")>pMax) pMax=O.getDouble("power");
                    }
                    S.setChargepoints(TextUtils.join(","+KartenActivity.lineSeparator,chargepoints));
                    //Bestimme Typ des Markers
                    Integer sTyp = 0;
                    if(pMax>0) sTyp=1;
                    if(pMax>=11) sTyp=2;
                    if(pMax>=20) sTyp=3;
                    if(pMax>=43) sTyp=4;
                    S.setTyp(sTyp);

                    S.setFaultreport(M.optBoolean("fault_report",false));

                    Saeulen.put(S.getID(),S);

                    mClusterManager.addItem(S);
                    SaeulenCache.put(S.getID(),S);


                    response++;


                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

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


    public static void setUpClusterer() {
        // Declare a variable for the cluster manager.

        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"setUpClusterer");
        // Position the map.
//        KartenActivity.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng (KartenActivity.mMap.getMyLocation().getLatitude(),KartenActivity.mMap.getMyLocation().getLongitude()), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<Saeule>(KartenActivity.getInstance(), KartenActivity.mMap);

        mClusterManager.getMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                marker.hideInfoWindow();
                MarkerBounce(marker);
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                return false;
            }
        });
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Saeule>() {
            @Override
            public boolean onClusterItemClick(Saeule item) {
                Marker marker = null;
                resetClickMarker();
                if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"OnCLusterItemCLickListener  "+item.getID());
                clickedSaeule = item;

                MiniInfoFragment.setzeSaeule(item.getID(), item);
                //GeoWorks.animateClick();
                GeoWorks.movemapPosition(item.getPosition(),"MarkerClick");
                if(AnimationWorker.isFilterVisibile()) AnimationWorker.toggleFilter();
                if(!AnimationWorker.isDetailsVisibile())AnimationWorker.show_info();else AnimationWorker.hide_info();



                return true;
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Saeule>() {
            @Override
            public boolean onClusterClick(Cluster<Saeule> cluster) {

                KartenActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(),13), 1000, null);
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
        KartenActivity.mMap.setOnCameraChangeListener(mClusterManager);
        KartenActivity.mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setRenderer(new meinClusterRenderer(KartenActivity.getInstance(),KartenActivity.mMap,mClusterManager));

    }

    private static void MarkerBounce(final Marker marker) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    public static void resetClickMarker(){
        if(clickedMarker !=null && clickedSaeule !=null){
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(Markers[0]);
            if ( clickedSaeule.getTyp() < 5 && clickedSaeule.getTyp() >= 0 )
                if (clickedSaeule.isFaultreport())
                    icon = BitmapDescriptorFactory.fromResource(Markers_Fault[clickedSaeule.getTyp()]);
                else
                    icon = BitmapDescriptorFactory.fromResource(Markers[clickedSaeule.getTyp()]);
            try {
                clickedMarker.setIcon(icon);
            }
            catch(IllegalArgumentException e){
                LogWorker.e(LOG_TAG,"ILLEGAL ARGUMENT on setIcon" + e.toString());

            }
        }
    }






    private static class meinClusterRenderer extends DefaultClusterRenderer<Saeule> implements GoogleMap.OnCameraChangeListener {

        public meinClusterRenderer(Context context, GoogleMap map,
                                   ClusterManager<Saeule> clusterManager) {
            super(context, map, clusterManager);

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
        public void onCameraChange(CameraPosition cameraPosition) {

            LatLng mLatLng = cameraPosition.target;
            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "onCameraChange: Versetzt:" + GeoWorks.isPositionversetzt() + " Detailsvisible:" + AnimationWorker.isDetailsVisibile() + " MapZoom" + GeoWorks.getMapZoom() + " cpZoom" + cameraPosition.zoom);

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

    public void holeInfo(){

    }

    public void holeDetails(Integer id){

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
            if (cp.zoom < 8.5) f = 0.2;
            if (cp.zoom < 8.0) f = 0;
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

    public static void clearCache(){
        // SaeulenCache aufräumen (alte Säulendaten entfernen)
        for (Map.Entry<Integer,Saeule> sP : SaeulenCache.entrySet()){
            Saeule S = sP.getValue();
            if (S.getUpdated()<(System.currentTimeMillis()-CACHE_EXPIRED_MILLIS))
                SaeulenCache.remove(sP.getKey());
        }

    }

    public static void reset(){
        letzterAbrufBeiLLB=new LatLngBounds(new LatLng(0,0),new LatLng(0,0));
        letzterAbrufTime=0l;
        letzterAbrufFilter=0;
        RQ_URL="";
        RQ_PENDING=false;
    }


}
