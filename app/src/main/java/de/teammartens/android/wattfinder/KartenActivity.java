package de.teammartens.android.wattfinder;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

import de.teammartens.android.wattfinder.model.ArrayAdapterSearchView;
import de.teammartens.android.wattfinder.model.Saeule;
import de.teammartens.android.wattfinder.model.rSuggestionsProvider;
import de.teammartens.android.wattfinder.worker.AnimationWorker;
import de.teammartens.android.wattfinder.worker.ExceptionWorker;
import de.teammartens.android.wattfinder.worker.FilterWorks;
import de.teammartens.android.wattfinder.worker.GeoWorks;
import de.teammartens.android.wattfinder.worker.LogWorker;
import de.teammartens.android.wattfinder.worker.NetWorker;
import de.teammartens.android.wattfinder.worker.SaeulenWorks;



public class KartenActivity extends FragmentActivity
        implements GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback,OnMapReadyCallback {

    private static final String LOG_TAG = "Wattfinder";
    public static GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private final static Integer
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public final static Integer MY_PERMISSIONS_REQUEST_LOCATION = 17;
    public final static Integer MY_PERMISSIONS_REMOVE_LOCATION = 18;
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
    public static boolean BackstackEXIT = false;

    private static Integer API_RQ_Count = 0;

    private static int MapPaddingY,MapPaddingX = 0;
    private static final String sP_APIRQCount = "APIRQCount";
    private static final String sP_ZoomLevel = "LastZoomLevel";
    private static final String sP_Latitude = "LastLatitude";
    private static final String sP_Longitude = "LastLongitude";
    public static final String sP_Timestamp = "TimeatLastPosition";
    private static final String zP_Latitude = "TargetLatitude";
    private static final String zP_Longitude = "TargetLongitude";
    private static final String zP_String = "TargetString";
    private static final String sP_Saeule = "currentSaeule";
    private static final String sP_CEuID = "ceuserid";
    private static Integer CEuID=0;
    private static final Long TimestampValid = 12*3600*1000l;
    private static final Float defaultLat = 52.5170365f;
    private static final Float defaultLng = 13.3888599f;
    private static final LatLng defaultLatLng = new LatLng(defaultLat,defaultLng);
    private static boolean permissiondenied = false;
    public static boolean mapReady = false;
    public static boolean privacyConsent = false;

public static ActionBar actionBar;
    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static KartenActivity sInstance;






    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout3);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        if(savedInstanceState == null){
            //really make new Instance
            sInstance = this;
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
           if(!FilterWorks.filter_initialized()) AnimationWorker.showStartup();

            lineSeparator =System.getProperty("line.separator");


            API_RQ_Count = sharedPref.getInt(sP_APIRQCount,0);
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"APIRQCOUNT:"+getAPI_RQ_Count());
            if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"privacyConsent "+privacyConsent);

            privacyConsent = sharedPref.getBoolean("privacyConsent",false);
            if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"privacyConsent "+privacyConsent);
        }
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);


        fragmentManager = getSupportFragmentManager();

        //Aktiviere Handling für UncaughtException
        if (LogWorker.DEFAULT_DEBUGGING) new ExceptionWorker(KartenActivity.this);




        //Preload Saäulen wenn gute Internetverbindung
        //SaeulenWorks.ladeMarker(46.727812939969645,6.26220703125,54.89177403135015,14.65576171875); //Ganz Deutschland


        //}

        GeoWorks.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    protected void onStart() {
        super.onStart();


    }
    @Override
    protected void onPause() {
        super.onPause();


        FilterWorks.filter_speichern();

        if (mMap!=null) {
            if (sharedPref == null)
                sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor e = sharedPref.edit();
            CameraPosition cp = mMap.getCameraPosition();
            if (GeoWorks.validLatLng(cp.target) && cp.zoom > GeoWorks.MAX_ZOOM) {
                e.putFloat(sP_Latitude, new Float(cp.target.latitude));
                e.putFloat(sP_Longitude, new Float(cp.target.longitude));
                e.putFloat(sP_ZoomLevel, cp.zoom);
                e.putLong(sP_Timestamp, System.currentTimeMillis());
                LatLng c = GeoWorks.getSuchPosition();
                if (c!= null){
                    e.putFloat(zP_Latitude, new Float(c.latitude));
                    e.putFloat(zP_Longitude, new Float(c.longitude));
                    e.putString(zP_String,GeoWorks.getSuchString());
                }else
                {
                    e.putFloat(zP_Latitude, 0f);
                    e.putFloat(zP_Longitude, 0f);
                }

                e.putInt(sP_APIRQCount, API_RQ_Count);
                e.apply();
                /*LogWorker.d("WattfinderInternal", "Pause");
                LogWorker.d("WattfinderInternal", "GeoWorks Lat: " + GeoWorks.getMapPosition().latitude + " Lng: " + GeoWorks.getMapPosition().longitude + " Z: " + GeoWorks.getMapZoom() + "");
                LogWorker.d("WattfinderInternal", "Lat: " + new Float(cp.target.latitude) + " Lng: " + new Float(cp.target.longitude) + " Z: " + cp.zoom + " saved");

*/            }

        }
        LogWorker.sendLog();

    }
    @Override
    protected void onResume() {

        if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"onResume");
        if(FilterWorks.filter_initialized())AnimationWorker.hideStartup();
        super.onResume();
        sInstance = this;
        mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        LogWorker.init_logging();
        load_CEuID();

        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.hide();
        }
//        prepareSearch();




        //NetWorker.resetRETRY();

        fragmentManager = getSupportFragmentManager();
        if (PlayServiceStatus == ConnectionResult.SUCCESS) {

            GeoWorks.setupLocationListener();
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

    GeoWorks.init_searchfragment();
    SaeulenWorks.resetInfoView();
    AnimationWorker.hide_info();
   AnimationWorker.show_fabs();
   AnimationWorker.startupScreen=true;
   mapReady=false;
   FilterWorks.filter_initialized();
    }






    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
            GeoWorks.removeLocationListener();

        super.onStop();
        //setMapReady(false);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG, "onSaveInstanceState");


       // saved that text in bundle object i.e. outState

        outState.putDouble(sP_Latitude,GeoWorks.getMapPosition().latitude);
        outState.putDouble(sP_Latitude,GeoWorks.getMapPosition().longitude);
        outState.putFloat(sP_ZoomLevel,GeoWorks.getMapZoom());
        outState.putParcelable(sP_Saeule,SaeulenWorks.getCurrentSaeule());
        SaeulenWorks.populateInfoContainer();
        outState.putInt("STATE",AnimationWorker.getSTATE());
        outState.putBoolean("DEBUG",LogWorker.isVERBOSE());


    }

//Restoring the State

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG, "onRestoreInstanceState");

        GeoWorks.setMapPosition(new LatLng(savedInstanceState.getDouble(sP_Latitude),savedInstanceState.getDouble(sP_Longitude)),
                    savedInstanceState.getFloat(sP_ZoomLevel));
        SaeulenWorks.setCurrentSaeule((Saeule) savedInstanceState.getParcelable(sP_Saeule));
        AnimationWorker.restoreState(savedInstanceState.getInt("STATE"));
        LogWorker.setVERBOSE(savedInstanceState.getBoolean("DEBUG"));
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
                AnimationWorker.show_map();
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




    public static boolean checkPermissionLocation(int id){

        if (ContextCompat.checkSelfPermission(KartenActivity.getInstance(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (!AnimationWorker.startupScreen /*&& !permissiondenied*/) {
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "requestLocation Permission");
                ActivityCompat.requestPermissions(KartenActivity.getInstance(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        id);
            }
        } else {
            GeoWorks.setLocation_permission(true);
            return true;
        }
        return false;
    }
    /*
 * Handle results returned to the FragmentActivity
 * by Google Play services
 */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Decide what to do based on the original request code
        if (requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST
                && resultCode == Activity.RESULT_OK){
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                setupGoogleAPI();
                }

        }

@Override
    public void onMapReady(GoogleMap googleMap) {
    if (!mapReady) {
        mMap = googleMap;
        setMapReady(true);

        setMapCenter();

        View v = getInstance().findViewById(R.id.MapTouchOverlay);
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                GeoWorks.CUSTOM_MAPVIEW = true;
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG + " the Map", "Custom Map Move detected");
                return false;//Das ist wichtig damit sich die Map trotzdem bewegt
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "MapClick");
                //GeoWorks.movemapPosition("showMapClick");
                //fragmentManager.popBackStack();
                AnimationWorker.show_map();
                //und noch den geklickten Marker wieder resetten
                SaeulenWorks.resetClickMarker();
            }


        });


        v = findViewById(R.id.fab_filter);
        v.requestFocus();
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterWorks.refresh_filterlisten_API();
                AnimationWorker.show_filter();
            }
        });
        //slideDown(v, 0);
        AnimationWorker.slideUp(v, 0);
        v = findViewById(R.id.fab_mylocation);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkPermissionLocation(MY_PERMISSIONS_REQUEST_LOCATION)) {

                    AnimationWorker.show_map();

                    if (GeoWorks.validLatLng(GeoWorks.getmyPosition())) {
                        GeoWorks.CUSTOM_MAPVIEW = false;
                        //GeoWorks.movemapPosition(GeoWorks.getmyPosition(),GeoWorks.MY_LOCATION_ZOOM,"fab_Mylocation");
                        GeoWorks.setmyPosition();
                    } else {
                        Toast.makeText(getInstance(), getString(R.string.novalidlocation), Toast.LENGTH_SHORT).show();
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, "StandortButton: Kein gültiger Standort:"+GeoWorks.getmyPosition());
                    }

                }
            }
        });

        v = findViewById(R.id.fab_search);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimationWorker.showSearchBar();
            }
        });

        //slideDown(v, 0);
        AnimationWorker.slideUp(v, 0);


        v = findViewById(R.id.buttonMapStyle);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = KartenActivity.mMap.getMapType();
                if (i < 4) KartenActivity.mMap.setMapType(i + 1);
                else KartenActivity.mMap.setMapType(1);

            }
        });

        KartenActivity.mMap.setMapType(1);
        BackstackEXIT = false;
        FilterWorks.lade_filter_db();
        SaeulenWorks.setUpClusterer();
        SaeulenWorks.reset();
        SaeulenWorks.checkMarkerCache("MapReady");
        AnimationWorker.show_map();
        AnimationWorker.hide_info();
        if (FilterWorks.filter_initialized()) AnimationWorker.hideStartup();
    }
}




public GoogleApiClient setupGoogleAPI(){

        if ((mGoogleApiClient != null && mGoogleApiClient.isConnected()))
            return mGoogleApiClient;

        // Getting Google Play availability status
        PlayServiceStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getBaseContext());

        // Showing status

        if (PlayServiceStatus != ConnectionResult.SUCCESS) { // Google Play Services are not available



            final String appPackageName = "com.google.android.gms"; // getPackageName() from Context or Activity object
           /* try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }*/

            LogWorker.e(LOG_TAG, "PlayServices not connected:"+GoogleApiAvailability.getInstance().getErrorString(PlayServiceStatus));

            Dialog dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, PlayServiceStatus, CONNECTION_FAILURE_RESOLUTION_REQUEST, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    LogWorker.e(LOG_TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                            + PlayServiceStatus);
                    CharSequence s = "Could not connect to Google API Client: Error " + String.valueOf(PlayServiceStatus);
                   Toast.makeText(KartenActivity.getInstance(), s ,Toast.LENGTH_SHORT).show();

                }
            });
            dialog.show();


        } else { // Google Play Services are available


                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this, 0, this)
                        .addApi(Places.GEO_DATA_API)
                        .build();
                return mGoogleApiClient;

        }
return null;
}

    public static boolean GoogleAPIConnected(){

        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }





    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        if (requestCode==MY_PERMISSIONS_REQUEST_LOCATION&&
                grantResults!=null&&grantResults.length>0&&
                grantResults[0]==PackageManager.PERMISSION_GRANTED) {
            GeoWorks.setupLocationListener();
            GeoWorks.setLocation_permission(true);
        }
            else{
            if (LogWorker.isVERBOSE())LogWorker.e(LOG_TAG,"requestLocation Permission DENIED");
                    permissiondenied=true;
                    GeoWorks.setLocation_permission(false);
                }
        if (requestCode==MY_PERMISSIONS_REMOVE_LOCATION&&
                grantResults!=null&&grantResults.length>0&&
                grantResults[0]==PackageManager.PERMISSION_GRANTED) GeoWorks.removeLocationListener();
    }




  private  void prepareSearch(){
    ArrayAdapterSearchView searchView=(ArrayAdapterSearchView) findViewById(R.id.map_search);
    if (searchView == null && LogWorker.isVERBOSE()) LogWorker.e("Building SearchView","search was Null");
    searchView.setQueryHint(getString(R.string.search_hint));

    SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

    searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        searchView.setIconified(false);

        searchView.setQueryRefinementEnabled(true);
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"OnQuerySubmit: "+query);
                GeoWorks.starteSuche(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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




    public static Integer getAPI_RQ_Count() {
        return API_RQ_Count;
    }

    public static void incAPI_RQ_Count() {
        API_RQ_Count++;

    }
    public static void setMapPaddingY(Integer h) {
       /* if(mMap!=null) {
            mMap.setPadding(0, 0, 0, h);
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "PaddingY:" + h);
            if (h == 0) {

                //CameraUpdate CU = CameraUpdateFactory.newLatLngZoom(VersatzBerechnen(Geo), zoom);
            }
            MapPaddingY = h;
        }*/
    }
    public static void setMapPaddingX(Integer w) {
       /* if(mMap!=null) {
            mMap.setPadding(0, 0, w, 0);
            MapPaddingX = w;
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "PaddingX:" + w);
        }*/
    }


    public static void setMapPadding(View v) {
        if(mMap!=null) {
            Configuration config = KartenActivity.getInstance().getResources().getConfiguration();
            if (config.orientation == config.ORIENTATION_PORTRAIT) {
                mMap.setPadding(0, 0, 0, v.getHeight());
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "PaddingY:" + v.getHeight());
            } else {
                mMap.setPadding(0, 0, v.getWidth(), 0);
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "PaddingX:" + v.getWidth());
            }
        }
    }

    public static void setMapCenter(){


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
        //erstmal setzen um zumindest eine POsition zu haben
        //GeoWorks.setmyPosition(new LatLng(Lat,Lng));

        Long TS = sharedPref.getLong(sP_Timestamp,0);


            if ( ((System.currentTimeMillis()-TS)<TimestampValid) &&
                    !(Lat.equals(defaultLat) && Lng.equals(defaultLng)) && zoom > GeoWorks.MAX_ZOOM) {
                GeoWorks.CUSTOM_MAPVIEW = true;

                LogWorker.d("SetMapCenter", "Lat: " + Lat + "Lng: " + Lng + "Z: " + zoom + " geladen");
                GeoWorks.movemapPosition(new LatLng(Lat, Lng), zoom, "mapReady");
            } else {
                GeoWorks.CUSTOM_MAPVIEW = false;
                if (GeoWorks.validLatLng(GeoWorks.getmyPosition())) {

                    LogWorker.d("SetMapCenter", " zum Standort bewegen");
                    GeoWorks.setmyPosition(GeoWorks.getmyPosition());
                } else {

                    LogWorker.d("SetMapCenter", "zum Standard Standort bewegen");
                    GeoWorks.movemapPosition(new LatLng(defaultLat, defaultLng), GeoWorks.DEFAULT_ZOOM, "mapReadyDefault");
                }
            }
            //Lade Suchmarker
            Lat = sharedPref.getFloat(zP_Latitude,0);
            Lng = sharedPref.getFloat(zP_Longitude,0);
            if(Lat>0&&Lng>0){
                GeoWorks.Suchmarker(new LatLng(Lat,Lng),sharedPref.getString(zP_String,""));
            }



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

    public static void load_CEuID(){
        CEuID=sharedPref.getInt(sP_CEuID,0);
        if (CEuID==0)CEuID=generate_CEuId();

    }
    private static Integer generate_CEuId(){
        final String alphabet = "0123456789";
        final int N = alphabet.length();
        StringBuilder id= new StringBuilder();

        Random r = new Random();
        id.append(787);
        for (int i = 0; i < 6; i++) {
            id.append(alphabet.charAt(r.nextInt(N)));
        }
        if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"New ChargeEvent-ID generated: "+id);
        sharedPref.edit().putInt(sP_CEuID, Integer.parseInt(id.toString())).apply();
        return Integer.parseInt(id.toString());
    }

    public static Integer getCEuID() {
        return CEuID;
    }
}



