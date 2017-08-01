package de.teammartens.android.wattfinder;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.SearchRecentSuggestions;
import android.app.DialogFragment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.app.ActionBar;

import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


import de.teammartens.android.wattfinder.fragments.DetailsFragment;
import de.teammartens.android.wattfinder.fragments.FilterFragment;
import de.teammartens.android.wattfinder.fragments.ImageZoomFragment;
import de.teammartens.android.wattfinder.fragments.MiniInfoFragment;
import de.teammartens.android.wattfinder.model.ArrayAdapterSearchView;
import de.teammartens.android.wattfinder.model.rSuggestionsProvider;
import de.teammartens.android.wattfinder.worker.FilterWorks;
import de.teammartens.android.wattfinder.worker.GeoWorks;
import de.teammartens.android.wattfinder.worker.LogWorker;
import de.teammartens.android.wattfinder.worker.NetWorker;
import de.teammartens.android.wattfinder.worker.SaeulenWorks;

import static de.teammartens.android.wattfinder.worker.FilterWorks.filter_initialized;


public class KartenActivity extends FragmentActivity
        implements GoogleApiClient.OnConnectionFailedListener,ActivityCompat.OnRequestPermissionsResultCallback,OnMapReadyCallback {
    private static final String LOG_TAG = "Wattfinder";
    public static GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int MY_PERMISSIONS_REQUEST_LOCATION = 9;
    private final static int MY_PERMISSIONS_REMOVE_LOCATION = 8;
    //private LocationClient mLocationClient;
    public static LocationManager mLocationManager;
    //public static Location mCurrentLocation;
    private int PlayServiceStatus;
    public static GoogleApiClient mGoogleApiClient;
    private String suchtext;

    public static final boolean VERBOSE = true;
    public static String lineSeparator ="";
    public static SupportMapFragment mapFragment;
    public static FragmentManager fragmentManager;
    public static SharedPreferences sharedPref ;
    private static boolean BackstackEXIT = false;
    private static boolean startupScreen = true;
    private static Integer API_RQ_Count = 0;

    private static int MapPaddingY,MapPaddingX = 0;
    private static final String sP_APIRQCount = "APIRQCount";
    private static final String sP_ZoomLevel = "LastZoomLevel";
    private static final String sP_Latitude = "LastLatitude";
    private static final String sP_Longitude = "LastLongitude";
    private static final String sP_Timestamp = "TimeatLastPosition";
    private static final Long TimestampValid = 24*3600*1000l;
    private static final Float defaultLat = 52.5170365f;
    private static final Float defaultLng = 13.3888599f;
    private static final LatLng defaultLatLng = new LatLng(defaultLat,defaultLng);
    private static boolean permissiondenied = false;
    public static boolean mapReady = false;
    private static boolean skipEula = false;

public static ActionBar actionBar;
    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static KartenActivity sInstance;


    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    //Mein eigener Location Listener
    private  class myLocationListener implements LocationListener {

        // Define the callback method that receives location updates
        @Override
        public void onLocationChanged(Location location) {


            //mCurrentLocation = location;
            if(location!=null)GeoWorks.setmyPosition(new LatLng (location.getLatitude(),location.getLongitude()));
            if ( VERBOSE) LogWorker.d(LOG_TAG, "Location erhalten:"+(location==null?"null":""));


            //GeoWorks.meinMarker();



        }




        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }
    public LocationListener  mLocationListener = new myLocationListener();

    /*
 * Handle results returned to the FragmentActivity
 * by Google Play services
 */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */

                        break;
                }

        }
    }
@Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        setMapReady(true);
        Float Lat = sharedPref.getFloat(sP_Latitude,defaultLat);
        if (Lat==0f)Lat=defaultLat;
        Float Lng = sharedPref.getFloat(sP_Longitude,defaultLng);
        if (Lng==0f)Lng=defaultLng;
        Float zoom = sharedPref.getFloat(sP_ZoomLevel,GeoWorks.DEFAULT_ZOOM);
        if (!GeoWorks.validLatLng(new LatLng(Lat,Lng))){
            Lat=defaultLat;
            Lng=defaultLng;
            zoom=GeoWorks.DEFAULT_ZOOM;
        }

        Long TS = sharedPref.getLong(sP_Timestamp,0);
    if(!(Lat.equals(defaultLat)&&Lng.equals(defaultLng))&&zoom>GeoWorks.MAX_ZOOM&&(System.currentTimeMillis()-TS)<TimestampValid)
    {
        GeoWorks.CUSTOM_MAPVIEW=true;
        GeoWorks.movemapPosition(new LatLng (Lat,Lng),zoom,"mapReady");
    }
    else {
        GeoWorks.CUSTOM_MAPVIEW=false;
        GeoWorks.setmyPosition(GeoWorks.getmyPosition());

    }

    LogWorker.d("WattfinderInternal","Lat: "+Lat+"Lng: "+Lng+"Z: "+zoom+" geladen");

    View v = getInstance().findViewById(R.id.MapTouchOverlay);
    v.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            GeoWorks.CUSTOM_MAPVIEW=true;
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG+" the Map", "Custom Map Move detected");
            return false;
        }
    });

    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "MapClick");
            GeoWorks.movemapPosition("showMapClick");
            show_map();
            //und noch den geklickten Marker wieder resetten
            SaeulenWorks.resetClickMarker();
        }


    });



    v = findViewById(R.id.fab_filter);
    v.requestFocus();
    v.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleFilter();
        }
    });
    //slideDown(v, 0);
    slideUp(v, 0);
    v = findViewById(R.id.fab_mylocation);
    v.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            show_map();
            GeoWorks.CUSTOM_MAPVIEW = false;
            GeoWorks.movemapPosition(GeoWorks.getmyPosition(),GeoWorks.DEFAULT_ZOOM,"fab_Mylocation");
        }
    });

    //slideDown(v, 0);
    slideUp(v, 0);


        BackstackEXIT=false;
        FilterWorks.lade_filter_db();
         SaeulenWorks.setUpClusterer();
        SaeulenWorks.reset();
        SaeulenWorks.checkMarkerCache("MapReady");
        show_map();hide_info();
        if (FilterWorks.filter_initialized()) KartenActivity.hideStartup();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        sInstance = this;

        setContentView(R.layout.mainlayout);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        fragmentManager = getSupportFragmentManager();

        mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

            setupGoogleAPI();

            // Get the intent, verify the action and get the query
            Intent intent = getIntent();
            if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String data = intent.getDataString();
                if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Intent Data: -"+data+"-");
                String query = intent.getStringExtra(SearchManager.QUERY);
                if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Intent Query: -"+query+"-");
                if (data == null || !data.equals("Suggestion")){
                //erstmal in Recents speichern
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                        rSuggestionsProvider.AUTHORITY, rSuggestionsProvider.MODE);
                suggestions.saveRecentQuery(query, null);


                GeoWorks.starteSuche(query);}
                else{
                    GeoWorks.starteSucheSuggested(query);
                }
            }else if (!FilterWorks.filter_initialized())showStartup();
            //Preload Saäulen wenn gute Internetverbindung
            //SaeulenWorks.ladeMarker(46.727812939969645,6.26220703125,54.89177403135015,14.65576171875); //Ganz Deutschland


        //}


    }

private void setupGoogleAPI(){
    // Getting Google Play availability status
    PlayServiceStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

    // Showing status
    if(PlayServiceStatus!=ConnectionResult.SUCCESS){ // Google Play Services are not available

        int requestCode = 10;
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(PlayServiceStatus, this, requestCode);
        dialog.show();

    }else { // Google Play Services are available


        if (mapFragment != null) {
            mapFragment.getView().bringToFront();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, 0, this)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }

}

    public static boolean GoogleAPIConnected(){

        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogWorker.init_logging();
        lineSeparator =System.getProperty("line.separator");

    }
    @Override
    protected void onPause() {
        super.onPause();


        FilterWorks.filter_speichern();

        SharedPreferences.Editor e = sharedPref.edit();
        CameraPosition cp = mMap.getCameraPosition();
        if (GeoWorks.validLatLng(cp.target)&&cp.zoom>GeoWorks.MAX_ZOOM){
            e.putFloat(sP_Latitude, new Float(cp.target.latitude));
            e.putFloat(sP_Longitude, new Float(cp.target.longitude));
            e.putFloat(sP_ZoomLevel, cp.zoom);
            e.putLong(sP_Timestamp, System.currentTimeMillis());
            e.putInt(sP_APIRQCount, API_RQ_Count);
            e.commit();
            LogWorker.d("WattfinderInternal", "Pause");
            LogWorker.d("WattfinderInternal", "GeoWorks Lat: " + GeoWorks.getMapPosition().latitude + " Lng: " + GeoWorks.getMapPosition().longitude + " Z: " + GeoWorks.getMapZoom() + "");
            LogWorker.d("WattfinderInternal", "Lat: " + new Float(cp.target.latitude) + " Lng: " + new Float(cp.target.longitude) + " Z: " + cp.zoom + " saved");
        }


        LogWorker.sendLog();

    }
    @Override
    protected void onResume() {
        super.onResume();
        sInstance = this;
        //LogWorker.setVERBOSE(true);


        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.hide();
        }
        prepareSearch();


        if(API_RQ_Count==0) API_RQ_Count = sharedPref.getInt(sP_APIRQCount,0);
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"APIRQCOUNT:"+getAPI_RQ_Count());

        skipEula = sharedPref.getBoolean("skipEula",false);

        NetWorker.resetRETRY();

        fragmentManager = getSupportFragmentManager();
        if (PlayServiceStatus == ConnectionResult.SUCCESS) {

            setupLocationListener();
        }

        //TODO
        if (!NetWorker.networkavailable()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.nonetworkavailable))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        finish();}
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }


    }






        /*
         * Called when the Activity is no longer visible.
         */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
            if (mLocationManager != null)
                removeLocationListener();

        super.onStop();
        setMapReady(false);
    }

    @Override
    public void onBackPressed() {
        if(fragmentManager.getBackStackEntryCount() != 0) {

// TODO: Hier muss unbedingt nachgearbeitet werden. Momentan funktioniert die Erkennung ob Map visible ist nur ab Android15

            fragmentManager.popBackStack();

            SupportMapFragment f =  ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            if((android.os.Build.VERSION.SDK_INT>14 && f.getUserVisibleHint() )||android.os.Build.VERSION.SDK_INT<15){
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "BackStacked to Map");
                SaeulenWorks.reloadMarker();

                GeoWorks.movemapPosition("showMapBackPress");
                show_map();
                //show_info();
            }
        } else {
            // Damit muss Back zweimal gedrückt werden um die App zu verlassen.
            if(BackstackEXIT) {
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "BackStacked to EXIT");
                super.onBackPressed();
            }else{
                Toast.makeText(this,this.getString(R.string.Backstackexit), Toast.LENGTH_LONG).show();
                BackstackEXIT=true;
            }
        }
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        if (requestCode==MY_PERMISSIONS_REQUEST_LOCATION&&grantResults[0]==PackageManager.PERMISSION_GRANTED) setupLocationListener();
            else{
                    permissiondenied=true;
                }
        if (requestCode==MY_PERMISSIONS_REMOVE_LOCATION&&grantResults[0]==PackageManager.PERMISSION_GRANTED) removeLocationListener();
    }

    private void removeLocationListener(){
        if (ContextCompat.checkSelfPermission(KartenActivity.getInstance(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(KartenActivity.getInstance(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REMOVE_LOCATION);
        } else {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
    private void setupLocationListener(){


        if (ContextCompat.checkSelfPermission(KartenActivity.getInstance(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (!startupScreen && !permissiondenied)

            ActivityCompat.requestPermissions(KartenActivity.getInstance(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
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

            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(C,true), 10000, 100, mLocationListener);
        }
    }



    public boolean onCreateOptionsMenu(Menu menu) {

        //getMenuInflater().inflate(R.menu.menu, menu);

        //this.menu = menu;


/*
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        //SearchView search = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        MenuItem searchItem = menu.findItem(R.id.menu_search);

        if (searchItem!= null){
            final ArrayAdapterSearchView searchView = (ArrayAdapterSearchView) MenuItemCompat.getActionView(searchItem);
            if (searchView == null) LogWorker.e("Building SearchView","search was Null");
            searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
            searchView.setQueryRefinementEnabled(true);

        }else
        {
            LogWorker.e("Building SearchView", "searchItem was Null");
        }

*/

        return true;

    }
  private  void prepareSearch(){
    ArrayAdapterSearchView searchView=(ArrayAdapterSearchView) findViewById(R.id.map_search);
    if (searchView == null) LogWorker.e("Building SearchView","search was Null");
    searchView.setQueryHint(getString(R.string.search_hint));

    SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);


        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);

        searchView.setQueryRefinementEnabled(true);
        searchView.clearFocus();


    findViewById(R.id.main_screen).requestFocus();







}


    /**
     * Implementation of Volley
     * adpated: http://arnab.ch/blog/2013/08/asynchronous-http-requests-in-android-using-volley/
     */

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized KartenActivity getInstance() {
        return sInstance;
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? LOG_TAG : tag);

        if(VERBOSE) VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     *
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(LOG_TAG);

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        LogWorker.e(LOG_TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
// TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }







    public static void show_info() {

        FragmentTransaction fT = fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out,
                        R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out);
        Fragment f = fragmentManager.findFragmentByTag("iFragment");
        Fragment df = fragmentManager.findFragmentByTag("dFragment");
        if(df!=null&&df.isVisible())return;else {
            //wenn Details zusehen sind dann nix Info

            if (f == null) {
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "info wird neu gebildet");
                fT.add(R.id.infoFragment, Fragment.instantiate(getInstance(), MiniInfoFragment.class.getName()), "iFragment").addToBackStack(null).commit();
            } else if (f.isHidden()) {
                if (VERBOSE)
                    LogWorker.d(LOG_TAG, "info schon vorhanden" + f.isHidden() + " --" + f.isVisible() + "--" + f.isAdded());
                fT.show(f).addToBackStack(null).commit(); //replace(R.id.infoFragment, Fragment.instantiate(getInstance(), MiniInfoFragment.class.getName()), "iFragment");

            }


            slideDown(getInstance().findViewById(R.id.fab_filter), 0);
            slideDown(getInstance().findViewById(R.id.fab_mylocation), 0);

            BackstackEXIT=false;
        }
    }

    public static void hide_info(){
        Fragment f = fragmentManager.findFragmentByTag("iFragment");
        if (f !=null && f.isVisible()){
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out,
                    R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out).hide(f).commit();
        //slideDown(getInstance().findViewById(R.id.fab_directions), 0);
        //slideDown(getInstance().findViewById(R.id.fab_directions), 500);
        slideUp(getInstance().findViewById(R.id.fab_filter), 200);
        slideUp(getInstance().findViewById(R.id.fab_mylocation), 200);
            setMapPaddingY(0);
        }
        //getInstance().findViewById(R.id.fab_filter).setVisibility(View.VISIBLE);
        //getInstance().findViewById(R.id.fab_mylocation).setVisibility(View.VISIBLE);
        //getInstance().findViewById(R.id.fab_directions).setVisibility(View.GONE);
    }


    public static void show_imagezoom() {

        FragmentTransaction fT = fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out,
                        R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out);
        Fragment f = fragmentManager.findFragmentByTag("izFragment");

        //slideDown(v,0);
        //toggleDetails();
        View v = getInstance().findViewById(R.id.imageZoomFragment);
        fadeIn(v,0,1.0f);
        v.bringToFront();
        Fragment iF = fragmentManager.findFragmentByTag("iFragment");
        if (iF != null) fT.hide(iF);
        Fragment dF = fragmentManager.findFragmentByTag("dFragment");
        if (dF != null) fT.hide(dF);

            if (f == null) {
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "imagezoom wird neu gebildet");
                fT.add(R.id.imageZoomFragment, Fragment.instantiate(getInstance(), ImageZoomFragment.class.getName()), "izFragment").addToBackStack(null).commit();
            } else if (f.isHidden()) {
                if (VERBOSE)
                    LogWorker.d(LOG_TAG, "imageZoom schon vorhanden" + f.isHidden() + " --" + f.isVisible() + "--" + f.isAdded());
                fT.show(f).addToBackStack(null).commit(); //replace(R.id.infoFragment, Fragment.instantiate(getInstance(), MiniInfoFragment.class.getName()), "iFragment");

            }


            BackstackEXIT=false;

    }


    public static void hideImageZoom(){
        FragmentTransaction fT = fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out,
                        R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out);
        View v = getInstance().findViewById(R.id.imageZoomFragment);
        fadeOut(v,0);
        Fragment dF = fragmentManager.findFragmentByTag("dFragment");
        if (dF != null) fT.show(dF);
        dF = fragmentManager.findFragmentByTag("iFragment");
        if (dF != null) fT.hide(dF);
        fT.addToBackStack("details").commit();
    }


    public static void toggleFilter(){

        Fragment f = fragmentManager.findFragmentByTag("fFragment");
        if (f != null) {
            fragmentManager.popBackStack();

            View mapSearch = getInstance().findViewById(R.id.SearchViewContainer);
            slideSearchBarDown(mapSearch, 0);
        } else {
            FragmentTransaction Ft = fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fragment_slide_in,
                            R.anim.fragment_slide_out,
                            R.anim.fragment_slide_in,
                            R.anim.fragment_slide_out);
            Fragment iF = fragmentManager.findFragmentByTag("iFragment");
            if (iF != null) Ft.hide(iF);
            Ft.add(R.id.filterFragment, Fragment
                            .instantiate(getInstance(), FilterFragment.class.getName()),
                    "fFragment"
            ).addToBackStack(null).commit();



            hide_mapSearch();

        }
       /* if(layoutStyle().equals("default"))
        {GeoWorks.movemapPosition("toggleFilter",false);if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"ToggleFilter; VErsetz False");}
        else
        {GeoWorks.movemapPosition("toggleFilter",true);if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"ToggleFilter; VErsetz True");}*/
        BackstackEXIT=false;
    }

    public static void toggleDetails() {

        Fragment f = fragmentManager.findFragmentByTag("dFragment");
        if (f != null) {
            fragmentManager.popBackStack();

            View mapSearch = getInstance().findViewById(R.id.SearchViewContainer);

           GeoWorks.movemapPosition("hideDetails");
            slideSearchBarDown(mapSearch,0);
        } else {
            FragmentTransaction Ft = fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fragment_slide_in,
                            R.anim.fragment_slide_out,
                            R.anim.fragment_slide_in,
                            R.anim.fragment_slide_out);
            Fragment iF = fragmentManager.findFragmentByTag("iFragment");
            if (iF != null) Ft.hide(iF);
            Ft.add(R.id.detailFragment, Fragment
                            .instantiate(getInstance(), DetailsFragment.class.getName()),
                    "dFragment"
            ).addToBackStack(null).commit();


            hide_mapSearch();

            //GeoWorks.animateClick(true);

        }

        BackstackEXIT=false;
    }

    public static void show_map(){
if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Show Map");
        //if(fragmentManager==null)fragmentManager=getFragmentManager();
        if (isDetailsVisibile())
                fragmentManager.popBackStack();


        if (isFilterVisibile())
                fragmentManager.popBackStack();


        hide_info();
        slideUp(getInstance().findViewById(R.id.fab_filter), 200);
        slideUp(getInstance().findViewById(R.id.fab_mylocation), 200);
            View mapSearch = getInstance().findViewById(R.id.SearchViewContainer);

        show_debug();
        slideSearchBarDown(mapSearch,0);
        mapSearch.clearFocus();
        mapFragment.getView().requestFocus();
        //slideDown(getInstance().findViewById(R.id.fab_directions), 500);
        //slideUp(getInstance().findViewById(R.id.fab_filter), 200);
        //slideUp(getInstance().findViewById(R.id.fab_mylocation), 200);
        //findViewById(R.id.fab_filter).requestFocus();
        //GeoWorks.animateClick(false);
        setMapPaddingY(0);
       //GeoWorks.movemapPosition("showMap",false);
        BackstackEXIT=false;

    }


    public static void show_debug(){
        TextView t = (TextView) getInstance().findViewById(R.id.debugHeader);
        t.setText("DEBUG VERSION! LogID:"+LogWorker.getlogID());
        //mapSearch.setVisibility(View.VISIBLE);
        //mapSearch.bringToFront();
        if(LogWorker.isVERBOSE())t.setVisibility(View.VISIBLE);else t.setVisibility(View.GONE);
    }
    public static void hide_mapSearch(){
       // View mapSearch = getInstance().findViewById(R.id.SearchViewContainer);

       // slideSearchBarUp(mapSearch,0);
    }


    public static void hide_mapLoading(){
        View mapLoading = getInstance().findViewById(R.id.mapProgress);
        fadeOut(mapLoading,0);
    }


    public static void show_mapLoading(){
        View mapLoading = getInstance().findViewById(R.id.mapProgress);
       fadeIn(mapLoading,0,0.3f);
    }

    public static void hideStartup(){
        View startup = getInstance().findViewById(R.id.startupScreen);
        if (startupScreen) {
            if(!skipEula && (System.currentTimeMillis() - sharedPref.getLong(sP_Timestamp,0))>3600*1000){//Zeige Eula wenn skipEula nicht aktiviert und letztes Programmende ist mindestens eine Stunde eher
                final View v = getInstance().findViewById(R.id.eulaScreen);
                if (v!=null) {
                    final CheckBox cb = (CheckBox) getInstance().findViewById(R.id.skipEula);

                    hide_mapSearch();
                    fadeIn(v, 0, 1.0f);
                    Button b = (Button) getInstance().findViewById(R.id.eulaButton);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            skipEula=cb.isChecked();
                            sharedPref.edit().putBoolean("skipEula",skipEula).commit();
                            slideDown(v,0);
                            getInstance().setupLocationListener();

                            SaeulenWorks.reloadMarker();

                            View mapSearch = getInstance().findViewById(R.id.SearchViewContainer);
                            show_debug();
                            slideSearchBarDown(mapSearch,0);
                        }
                    });

                }

            }

            slideDown(startup, 500);
            startupScreen=false;
        }
        //slideSearchBarDown(mapSearch,0);
    }



    public static void showStartup(){
        View startup = getInstance().findViewById(R.id.startupScreen);
        View v = getInstance().findViewById(R.id.fab_debug);
        if(v!=null) v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogWorker.setVERBOSE(true);
            }
        });

        startup.setVisibility(View.VISIBLE);
        FilterWorks.lade_filterlisten_API();


        startupScreen=true;
        //hide_mapSearch();

    }

    public static void slideDown (final View V,Integer offset) {


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            V.animate()
                    .setStartDelay(offset)
                    .translationY(V.getHeight())
                    .alpha(0.0f)
                    .setDuration(500)
                    ;
        } else {
            V.animate()
                    .translationY(V.getHeight())
                    .alpha(0.0f)
                    .setDuration(500)
                    ;
        }
    }
    public static void slideUp (final View V,Integer offset) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            V.animate()
                    .setStartDelay(offset)
                    .translationY(0)
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.VISIBLE);
                            V.bringToFront();
                        }
                    });
        } else {
            V.animate()
                    .translationY(0)
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.VISIBLE);
                            V.bringToFront();
                        }
                    });
        }
    }


    public static void setMapPaddingY(Integer h) {
       mMap.setPadding(0,0,0,h);
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"PaddingY:"+h);
       if (h==0){

           //CameraUpdate CU = CameraUpdateFactory.newLatLngZoom(VersatzBerechnen(Geo), zoom);
       }
       MapPaddingY = h;
    }
    public static void setMapPaddingX(Integer w) {
        mMap.setPadding(0,0,w,0);
        MapPaddingX = w;
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"PaddingX:"+w);
    }


    public static void setMapPadding(View v) {
        Configuration config = KartenActivity.getInstance().getResources().getConfiguration();
        if (config.orientation == config.ORIENTATION_PORTRAIT) {
            mMap.setPadding(0, 0, 0, v.getHeight());
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "PaddingY:" + v.getHeight());
        }else
        {
            mMap.setPadding(0, 0, v.getWidth(),0);
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "PaddingX:" + v.getWidth());
        }
    }
   /* private static LatLng VersatzBerechnen(LatLng mLatLng, int d, boolean reverse){
        LatLng vLatLng = mLatLng;
        LatLngBounds VR = mMap.getProjection().getVisibleRegion().latLngBounds;

        Double hP = KartenActivity.getInstance().getResources().getDisplayMetrics().heightPixels*1.0;

        Double dD = 0.5-((hP-d*1.0)/hP)*0.5;


        Configuration config = KartenActivity.getInstance().getResources().getConfiguration();
        if (config.orientation == config.ORIENTATION_PORTRAIT){
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"VersatzMapPadding: "+hP+"-"+v.getHeight()+"--"+String.format("%+10.2f",((hP-d*1.0)/hP)*0.5)+ "---" +String.valueOf(((hP-d*1.0)/hP)*0.5));

            Double deltaY = dD * (VR.northeast.latitude - VR.southwest.latitude);
            vLatLng=new LatLng((reverse?mLatLng.latitude + deltaY:mLatLng.latitude - deltaY), mLatLng.longitude);
        }else
        {    hP = KartenActivity.getInstance().getResources().getDisplayMetrics().widthPixels*1.0;
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"VersatzMapPaddingXX: "+hP+"-"+d+"--"+String.format("%+10.2f",((hP-d*1.0)/hP)*0.5)+ "---" +String.valueOf(((hP-d*1.0)/hP)*0.5));

            dD = 0.5-((hP-d*1.0)/hP)*0.5;

            Double deltaX = dD * (VR.northeast.longitude - VR.southwest.longitude);
            vLatLng=new LatLng(mLatLng.latitude, (reverse?mLatLng.longitude-deltaX:mLatLng.longitude+deltaX));
        }

        return vLatLng;
    }*/

    public static void fadeOut (final View V,Integer offset) {


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            V.animate()
                    .setStartDelay(offset)

                    .alpha(0.0f)
                    .setDuration(500)
            ;
        } else {
            V.animate()

                    .alpha(0.0f)
                    .setDuration(500)
            ;
        }
    }

    public static void fadeIn (final View V,Integer offset, Float Alpha) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            V.animate()
                    .setStartDelay(offset)

                    .alpha(Alpha)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.VISIBLE);
                            V.bringToFront();
                        }
                    });
        } else {
            V.animate()

                    .alpha(Alpha)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.VISIBLE);
                            V.bringToFront();
                        }
                    });
        }
    }




    private static void slideSearchBarDown (final View V,Integer offset) {

/*
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            V.animate()
                    .setStartDelay(offset)
                    .translationY(0)
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.VISIBLE);
                            V.bringToFront();
                            V.clearFocus();
                        }
                    })
            ;
        } else {
            V.animate()
                    .translationY(0)
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.VISIBLE);
                            V.bringToFront();
                            V.clearFocus();
                        }
                    })
                    ;
        }
        */
    }


    public static Integer getAPI_RQ_Count() {
        return API_RQ_Count;
    }

    public static void incAPI_RQ_Count() {
        API_RQ_Count++;

    }


    public static boolean isDetailsVisibile(){

        Fragment dFragment = KartenActivity.fragmentManager.findFragmentByTag("dFragment");

        if (dFragment != null && dFragment.isVisible()){
           // if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Details Fragment visible.");
            return true;
        }

      //  if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Details Fragment NOT visible.");
        return false;
    }

    public static boolean isFilterVisibile(){

        Fragment fFragment = KartenActivity.fragmentManager.findFragmentByTag("fFragment");
        if (fFragment != null && fFragment.isVisible()){
           // if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Filter Fragment visible.");
            return true;
        }

        //if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Filter Fragment NOT visible.");

        return false;
    }

    public static LatLng Loc2LatLng (Location l)
    {
        return new LatLng(l.getLatitude(),l.getLongitude());
    }

    public static boolean isMapReady() {
        return mapReady;
    }

    public static void setMapReady(boolean mapReady) {
        KartenActivity.mapReady = mapReady;
    }

    public static String layoutStyle(){

        View v  = getInstance().findViewById(R.id.mapContainer);
        if (v!=null&&v.getTag()!=null&&!v.getTag().toString().isEmpty()){
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"LayoutStyle:-"+v.getTag().toString()+"-");
            return v.getTag().toString();
        }
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"LayoutStyle:default.");
        return "default";
    }
}



