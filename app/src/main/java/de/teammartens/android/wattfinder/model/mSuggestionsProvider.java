package de.teammartens.android.wattfinder.model;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.worker.GeoWorks;
import de.teammartens.android.wattfinder.worker.LogWorker;


public class mSuggestionsProvider extends ContentProvider {
    public final static String AUTHORITY = "de.teammartens.android.wattfinder.model.mSuggestionsProvider";
    //public final static int MODE = DATABASE_MODE_QUERIES;
    private static final String[] COLUMNS = {
            "_id", // must include this column
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            SearchManager.SUGGEST_COLUMN_QUERY,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_ICON_2};
            private static final String LOG_TAG = "Wattfinder Suggestions";



    private static final String API_KEY = "AIzaSyBFa-2nRDi4p618T6OfOBFNXyDIQLCVyDY";
    private static GoogleApiClient googleApiClient =null;
    private PlaceAutocompleteAdapter mAdapter;
    private static final LatLngBounds centralEur = new LatLngBounds(new LatLng(43.021,-1.582), new LatLng(58.654,18.457));
    private static LatLngBounds mBOUNDS = centralEur;

    @Override
    public String getType(Uri uri) {

        return null;
    }
    @Override
    public boolean onCreate() {

        return false;
    }

    public boolean onResume(){
 return registerClient();

    }
        private boolean registerClient(){

            googleApiClient = KartenActivity.getInstance().setupGoogleAPI();

            if (googleApiClient == null)
                    return false;
            else{
                googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    public void onConnected(Bundle connectionHint) {


                    }

                    public void onConnectionSuspended(int cause) {

                    }
                });

                googleApiClient.connect();
                return true;
            }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        //mBounds um aktuellen Standpunkt setzten zum gewichten der Ergebnisse
        LatLng mPos = GeoWorks.getmyPosition();
        mBOUNDS = new LatLngBounds( new LatLng(mPos.latitude-0.1,mPos.longitude-0.15), new LatLng(mPos.latitude+0.1,mPos.longitude+0.15));
        String query = selectionArgs[0];
        if (query == null || query.length() == 0) {

            LogWorker.e(LOG_TAG, "Empty Query");
            return null;
        }

        MatrixCursor cursor = new MatrixCursor(COLUMNS);

        int n = 0;

        ContentResolver contentResolver = KartenActivity.getInstance().getContentResolver();

        String contentUri = "content://" + rSuggestionsProvider.AUTHORITY + '/' + SearchManager.SUGGEST_URI_PATH_QUERY;
        Uri uri2 = Uri.parse(contentUri);

        Cursor c = contentResolver.query(uri2, null, null, new String[] { query }, null);
        if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Recent Suggestions: "+ c.getCount());
        for (String cN : c.getColumnNames() )
            if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Recent Suggestions Columns: "+ cN);
        while (c.moveToNext()){

            Object[] o = new Object[]{new Integer(n), // _id
                    c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                    "",
                    "RecentSuggestion",
                    "android.intent.action.SEARCH",
                    c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY)),
                    R.drawable.ic_recent,
                    null};
            n++;
            cursor.addRow(o);
            if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Recent Suggestions query: "+ c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY)));

        }


        c.close();


        if (query.length()>2)
        try {
            ArrayList<PlaceAutocomplete> list = getPlaceSuggestions(query);
            if(list!=null&&list.size()>0)
                for (PlaceAutocomplete s : list) {
                 if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Prediction:" + s.description);

                 cursor.addRow(createRow(new Integer(n), s));
                  n++;
                }
        } catch (Exception e) {
            LogWorker.e(LOG_TAG, "Failed to lookup " + query, e);
        }


        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    private Object[] createRow(Integer id, PlaceAutocomplete S) {
        Object[] o = new Object[]{};
        if (googleApiClient!=null||registerClient()) {//nur wenn Client verbunden oder registerclient erfolgreich

            // Jetzt ncoh mehr Infos abfragen
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(googleApiClient, S.placeId.toString());

            PlaceBuffer buffer = placeResult.await(60, TimeUnit.SECONDS);

            final Status status = buffer.getStatus();
            if (!status.isSuccess()) {
                Toast.makeText(getContext(), "Error contacting API: " + status.toString(),
                        Toast.LENGTH_SHORT).show();
                LogWorker.e(LOG_TAG, "Error getting autocomplete prediction API call: " + status.toString());
                buffer.release();
                return null;
            }
            final Place place = buffer.get(0);

            //Nur wenn innerhalb der Grenzen (Westfrankreich-Tschechien / Norditalien-Schweden)
            if (centralEur.contains(place.getLatLng()))
                o = new Object[]{id, // _id
                        place.getName(),
                        place.getAddress(),
                        "Suggestion",
                        "android.intent.action.SEARCH",
                        place.getAddress() + "::" + place.getLatLng().latitude + "::" + place.getLatLng().longitude,
                        R.drawable.ic_place,
                        R.drawable.powered_by_google_light};
            else if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "Place verworfen (out of bounds): " + place.getName());

            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "Place details received: " + place.getName());

            buffer.release();

        }

            return o;


    }

    private ArrayList<PlaceAutocomplete> getPlaceSuggestions( String query) {
        ArrayList resultList = new ArrayList<com.google.android.gms.location.places.ui.PlaceAutocomplete>();

        if (googleApiClient != null || registerClient()) {//nur wenn Client verbunden oder registerClient erfolgreich


            PendingResult<AutocompletePredictionBuffer> result =
                    Places.GeoDataApi.getAutocompletePredictions(googleApiClient, query,
                            mBOUNDS, new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE).build());
            AutocompletePredictionBuffer autocompletePredictions = result.await(60, TimeUnit.SECONDS);

            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Toast.makeText(getContext(), "Error contacting API: " + status.toString(),
                        Toast.LENGTH_SHORT).show();
                LogWorker.e(LOG_TAG, "Error getting autocomplete prediction API call: " + status.toString());
                autocompletePredictions.release();
                return null;
            }

            Log.i(LOG_TAG, "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                // Get the details of this prediction and copy it into a new PlaceAutocomplete object.
                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        prediction.getPrimaryText(null)));
            }

            // Release the buffer now that all data has been copied.
            autocompletePredictions.release();


        }

        return resultList;
    }


}
