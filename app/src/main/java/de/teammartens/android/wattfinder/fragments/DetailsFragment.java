package de.teammartens.android.wattfinder.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.model.ChargeEvent;
import de.teammartens.android.wattfinder.model.ImagePagerAdapter;
import de.teammartens.android.wattfinder.model.Saeule;
import de.teammartens.android.wattfinder.worker.AnimationWorker;
import de.teammartens.android.wattfinder.worker.GeoWorks;
import de.teammartens.android.wattfinder.worker.ImageWorker;
import de.teammartens.android.wattfinder.worker.LogWorker;
import de.teammartens.android.wattfinder.worker.NetWorker;
import de.teammartens.android.wattfinder.worker.Utils;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.view.View.GONE;

/**
 * Created by felix on 10.05.15.
 */
public class DetailsFragment extends Fragment {



    private static final String LOG_TAG = "DetailsFragment";
    private static final String fAPIUrl = "https://api.goingelectric.de/chargepoints/";

    private static View detailsView;
    private static LatLng mPos= new LatLng(0,0);
    private static Integer mID = 0;
    public static String mTitel = "";
    private static String mUrl = "";
    protected View view;
    private static ViewPager detailImages;

    private static LinearLayout pager_indicator;
    private static ImageView[] dots;
    private static Context mContext;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        detailsView = inflater.inflate(R.layout.fragment_details_cards, container, false);
mContext =this.getContext();
        View v = detailsView.findViewById(R.id.fab_routing);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + mPos.latitude + "," + mPos.longitude + "?q=" + mPos.latitude + "," + mPos.longitude + "(" + mTitel + ")"));
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

            v = detailsView.findViewById(R.id.fab_browser);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                });


        /*ImageSwitcher iS = (ImageSwitcher) detailsView.findViewById(R.id.dImage);

        iS.setFactory(new ViewSwitcher.ViewFactory() {
                                     public View makeView() {
                                         ImageView myView = new ImageView(mContext.getApplicationContext());
                                         myView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                                         return myView;
                                     }
                                 });
        iS.setImageResource(R.drawable.icon_mono);
        Animation in = AnimationUtils.loadAnimation(mContext,android.R.anim.slide_in_left);
        iS.setInAnimation(in);
        in=AnimationUtils.loadAnimation(mContext,android.R.anim.slide_out_right);
        iS.setOutAnimation(in);


        iS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImgIndex++;
                if (ImgIndex>=ImgCount||ImgIndex<0) ImgIndex=0;

                if(ImgCount>1) ladeBild(ImgIndex);
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "LadeBild "+ImgIndex+" ("+ImgIDs[ImgIndex]+")");
            }
        });

*/
        resetValues();


        return detailsView;
    }




    public void onStart(){
        super.onStart();
        if (detailsView != null) {
            TextView t = (TextView) detailsView.findViewById(R.id.dSaeulenid);
            if (t != null && !t.getText().equals(mID))
                holeDetails();
            detailsView.setVisibility(View.VISIBLE);
            detailsView.setAlpha(1.0f);

            View v = detailsView.findViewById(R.id.dLoadImages);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initializeWorker();
                    v.setVisibility(GONE);
                }
            });

        }
    }

    public void onResume(){
        super.onResume();
        AnimationWorker.hide_mapSearch();
        AnimationWorker.hide_fabs();
        load_events();

    }



    private static void holeDetails() {
        View v =detailsView.findViewById(R.id.loadingPanel);
        v.setVisibility(View.VISIBLE);
        TextView t = (TextView) detailsView.findViewById(R.id.dSaeulenid);
        t.setText("ID"+String.valueOf(mID));
        String url=fAPIUrl + "?key=" + mContext.getString(R.string.GoingElectric_APIKEY) + "&ge_id="+mID;
        GeoWorks.movemapPosition(mPos,GeoWorks.DETAIL_ZOOM,"DetailFragment");
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "JSONUrl:"+url);

        JsonObjectRequest dRequest = new JsonObjectRequest(Request.Method.GET,
                url, (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jResponse) {
                try {
                    if (jResponse.getString("status").contentEquals("ok")) {
                        NetWorker.rehabilateNetworkQuality();


                        //Response verarbeiten
                        JSONObject jO = jResponse.getJSONArray("chargelocations").getJSONObject(0);
                        if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG, "Detail Request erhalten. ID:"+jO.getInt("ge_id"));

                        mUrl = "http:"+jO.getString("url");
                        if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG, "URL:"+mUrl);

                        TextView t2 = (TextView) detailsView.findViewById(R.id.dBezeichnung);
                        t2.setText(jO.getString("name"));
                        mTitel=jO.getString("name");

                        JSONObject O = jO.getJSONObject("address");
                        t2 = (TextView) detailsView.findViewById(R.id.dAdresse);
                        t2.setText(O.getString("street")+KartenActivity.lineSeparator
                                    +O.getString("postcode")+" "+O.getString("city")+KartenActivity.lineSeparator
                                    +O.getString("country"));
                        mTitel+=", "+O.getString("city");

                        t2 = (TextView) detailsView.findViewById(R.id.dBetreiber_c);
                        if(!jO.optBoolean("operator",true))detailsView.findViewById(R.id.dBetreiber).setVisibility(GONE);else{ t2.setText(jO.optString("operator"));detailsView.findViewById(R.id.dBetreiber).setVisibility(View.VISIBLE);}


                        t2 = (TextView) detailsView.findViewById(R.id.dVerbund_c);
                        if(!jO.optBoolean("network",true))detailsView.findViewById(R.id.dVerbund).setVisibility(GONE);else {t2.setText(jO.optString("network"));detailsView.findViewById(R.id.dVerbund).setVisibility(View.VISIBLE);}


                        O = jO.getJSONObject("cost");
                        TextView t1 = (TextView) detailsView.findViewById(R.id.dKosten_c);
                        t2 = (TextView) detailsView.findViewById(R.id.dKosten_b);
                        detailsView.findViewById(R.id.dKosten).setVisibility(View.VISIBLE);
                        t1.setVisibility(View.VISIBLE);t2.setVisibility(View.VISIBLE);
                        if (!O.optBoolean("description_short",true)&&!O.optBoolean("description_long",true)) {

                            detailsView.findViewById(R.id.dKosten).setVisibility(GONE);

                        }else {
                            if (!O.optBoolean("description_short",true)) t1.setVisibility(GONE);
                            else t1.setText(decodeHTML(O.getString("description_short")));
                            if (!O.optBoolean("description_long",true)) t2.setVisibility(GONE);
                            else t2.setText(decodeHTML(O.getString("description_long")));

                        }

                        CheckBox ct = (CheckBox) detailsView.findViewById(R.id.dfreeParking);
                        ct.setChecked(O.optBoolean("freeparking",false));


                        ct = (CheckBox) detailsView.findViewById(R.id.dfreeCharging);
                        ct.setChecked(O.optBoolean("freecharging",false));
                        ct = (CheckBox) detailsView.findViewById(R.id.dVerified);
                        ct.setChecked(jO.optBoolean("verified",false));
                        ct = (CheckBox) detailsView.findViewById(R.id.dBarrierefrei);
                        ct.setChecked(jO.optBoolean("barrierfree",false));


                        JSONArray A = jO.getJSONArray("chargepoints");
                        t2 = (TextView) detailsView.findViewById(R.id.dStecker_b);
                        t2.setText("");
                        if (A.length()>0) {
                            for (int n =0;n<A.length();n++) {
                                O=A.getJSONObject(n);
                                t2.append(O.optString("count")+"x "+O.optString("type")+" "+O.optString("power")+"kW"+KartenActivity.lineSeparator);
                            }
                            detailsView.findViewById(R.id.dStecker).setVisibility(View.VISIBLE);
                        }else {

                            detailsView.findViewById(R.id.dStecker).setVisibility(GONE);

                        }


                        t2 = (TextView) detailsView.findViewById(R.id.dHinweise_b);
                        if(!jO.optBoolean("general_information",true))
                            detailsView.findViewById(R.id.dHinweise).setVisibility(GONE);else{ t2.setText(decodeHTML(jO.optString("general_information")));detailsView.findViewById(R.id.dHinweise).setVisibility(View.VISIBLE);}
                        t2 = (TextView) detailsView.findViewById(R.id.dPosition_b);
                        if(!jO.optBoolean("location_description",true))
                            detailsView.findViewById(R.id.dPosition).setVisibility(GONE);else{ t2.setText(decodeHTML(jO.optString("location_description")));detailsView.findViewById(R.id.dPosition).setVisibility(View.VISIBLE);}
                        t2 = (TextView) detailsView.findViewById(R.id.dLadeweile_b);
                        if(!jO.optBoolean("ladeweile",true))
                            detailsView.findViewById(R.id.dLadeweile).setVisibility(GONE);else{ t2.setText(decodeHTML(jO.optString("ladeweile")));detailsView.findViewById(R.id.dLadeweile).setVisibility(View.VISIBLE);}


                        t2 = (TextView) detailsView.findViewById(R.id.dStoerung_b);

                        if (!jO.optBoolean("fault_report",true)){
                            t2.setVisibility(GONE);
                            detailsView.findViewById(R.id.dStoerung_c).setVisibility(GONE);
                            detailsView.findViewById(R.id.dStoerungTitel).setVisibility(GONE);
                            detailsView.findViewById(R.id.dStoerung).setVisibility(GONE);}
                        else{
                            O = jO.getJSONObject("fault_report");
                            t2.setText(getDate(O.optString("created","0"))+": "+KartenActivity.lineSeparator+ decodeHTML(O.optString("description")));
                            t2.setVisibility(View.VISIBLE);
                            detailsView.findViewById(R.id.dStoerung_c).setVisibility(View.VISIBLE);
                            detailsView.findViewById(R.id.dStoerungTitel).setVisibility(View.VISIBLE);
                            detailsView.findViewById(R.id.dStoerung).setVisibility(View.VISIBLE);
                        }


                        O = jO.getJSONObject("openinghours");
                        ct = (CheckBox) detailsView.findViewById(R.id.d247);
                        ct.setChecked(O.optBoolean("24/7",false));
                        t1 = (TextView) detailsView.findViewById(R.id.dZeiten_z);
                        t2 = (TextView) detailsView.findViewById(R.id.dZeiten_b);

                            if (O.optString("days").isEmpty()) t1.setVisibility(GONE);
                            else {
                                t1.setVisibility(View.VISIBLE);
                                JSONObject j2 = O.getJSONObject("days");

                                t1.setText(mContext.getString(R.string.monday)+formatOpening(j2.getString("monday"))+KartenActivity.lineSeparator+
                                        mContext.getString(R.string.tuesday)+formatOpening(j2.getString("tuesday"))+KartenActivity.lineSeparator+
                                        mContext.getString(R.string.wednesday)+formatOpening(j2.getString("wednesday"))+KartenActivity.lineSeparator+
                                        mContext.getString(R.string.thursday)+formatOpening(j2.getString("thursday"))+KartenActivity.lineSeparator+
                                        mContext.getString(R.string.friday)+formatOpening(j2.getString("friday"))+KartenActivity.lineSeparator+
                                        mContext.getString(R.string.saturday)+formatOpening(j2.getString("saturday"))+KartenActivity.lineSeparator+
                                        mContext.getString(R.string.sunday)+formatOpening(j2.getString("sunday")));
                            }
                            if (!O.optBoolean("description",true)) t2.setVisibility(GONE);
                            else {t2.setVisibility(View.VISIBLE);
                             t2.setText(decodeHTML(O.getString("description")));}



                        //Retrieve Charge Events

                        load_events();





                        A = jO.getJSONArray("photos");
                        ImageWorker.resetImages();
                        RelativeLayout imgPager = (RelativeLayout) detailsView.findViewById(R.id.imgPagerParent);
                        if (A.length()>0){
                            //ImgCount=A.length();

                            for (int n =0;n<A.length();n++) {
                                O=A.getJSONObject(n);
                                ImageWorker.setImgIDs(n,O.getInt("id"));
                                //ImgIDs[n]=O.getInt("id");
                                if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"PhotoId:"+O.getInt("id"));
                            }
                            //Bilder nur über WiFi sofort laden, sonst erst auf Aufforderung
                            View v = detailsView.findViewById(R.id.dLoadImages);
                            if(NetWorker.isWiFi()){
                                initializeWorker();
                                v.setVisibility(View.GONE);
                            }

                            else
                            {
                                v.setVisibility(View.VISIBLE);
                                v = detailsView.findViewById(R.id.d_ImageBack);
                                v.setVisibility(View.VISIBLE);
                                v = detailsView.findViewById(R.id.dImagePager);
                                v.setVisibility(View.GONE);
                            }

                            imgPager.setVisibility(View.VISIBLE);
                        }else{
                            imgPager.setVisibility(GONE);
                        }

                        /*
                        //
                        //Hier müssen noch die Bilder geladen werden
ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>() {
   @Override
   public void onResponse(Bitmap response) {
     iv.setImageBitmap(response);
   }
}, 0, 0, null, null);
http://abhiandroid.com/ui/imageswitcher
http://indragni.com/blog/2013/03/31/android-imageswitcher-example/

                         */

                        View v =detailsView.findViewById(R.id.loadingPanel);
                        v.setVisibility(View.INVISIBLE);

                        KartenActivity.setMapPadding(detailsView);
                    } else {
                        Toast.makeText(KartenActivity.getInstance(), "Fehler beim Abrufen der Detailinformation. Bitte nochmal versuchen.", Toast.LENGTH_LONG);
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, "ERROR:" + jResponse.getString("status"));
                    }
                }catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }


        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                LogWorker.d(LOG_TAG,error.getLocalizedMessage());

            }
        });


        KartenActivity.getInstance().addToRequestQueue(dRequest);

    }

    public static void resetValues(){
        TextView t = (TextView) detailsView.findViewById(R.id.dSaeulenid);
        t.setText("");

    }



    public static void setzeSaeule(Integer id, LatLng pos, String titel){
        mID = id;
        mPos = pos;
        mTitel=titel;

        if (detailsView != null) {
            TextView t = (TextView) detailsView.findViewById(R.id.dSaeulenid);
            if (t != null && !t.getText().equals(mID))
                holeDetails();

        }


    }



    public static void initializeWorker() {

        View v = detailsView.findViewById(R.id.d_ImageBack);
        v.setVisibility(GONE);
        detailImages = (ViewPager) detailsView.findViewById(R.id.dImagePager);
        detailImages.setVisibility(View.VISIBLE);
        pager_indicator = (LinearLayout) detailsView.findViewById(R.id.dImgPagerCountDots);

        ImageWorker.initImages(false);

        ImageWorker.setImgAdapter(new ImagePagerAdapter(mContext, ImageWorker.getImgBitmaps(false),false));
        detailImages.setAdapter(ImageWorker.imgAdapter);
        detailImages.setCurrentItem(0);
        detailImages.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < ImageWorker.getImgCount(); i++) {
                    dots[i].setImageDrawable(mContext.getResources().getDrawable(R.drawable.nonselecteditem_dot));
                }

                dots[position].setImageDrawable(mContext.getResources().getDrawable(R.drawable.selecteditem_dot));
                ImageWorker.setImgIndex(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setUiPageViewController();
    }

    private static void setUiPageViewController() {
        int dotsCount = ImageWorker.getImgCount();

        if(dots==null)dots = new ImageView[5];

        for (int i = 0; i < 5; i++) {
            if(pager_indicator.getChildAt(i)!=null){
                dots[i] = (ImageView) pager_indicator.getChildAt(i);


            }else {
                dots[i] = new ImageView(mContext);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(4, 0, 4, 0);

                pager_indicator.addView(dots[i], params);


            }
            dots[i].setImageDrawable(mContext.getResources().getDrawable(R.drawable.nonselecteditem_dot));
            if (i>=dotsCount) dots[i].setVisibility(GONE);
            else dots[i].setVisibility(View.VISIBLE);
        }

        dots[0].setImageDrawable(mContext.getResources().getDrawable(R.drawable.selecteditem_dot));
    }



public static String formatOpening(String s){

    s=s.replace("from ","");
    s=s.replace(" till ","-");
    s=s.replace("closed",KartenActivity.getInstance().getString(R.string.closed));
    return s;
}
    public static void bestaetigen() {

        //Säuelenfunktion bestätigen
    }

    public static String getmTitel() {
        return mTitel;
    }

    public static Integer getmID() {
        return mID;
    }

    private static String getDate(String time) {
        Long t =0l;
        if (time!=null)
            t = Long.valueOf(time);
        Calendar cal = Calendar.getInstance(Locale.GERMAN);
        cal.setTimeInMillis(t*1000);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    private static String decodeHTML (String text){
        if (text != null)
        return Html.fromHtml(text).toString();

        return "";
    }

    private static void load_events(){
        if(mID>0) {
            final View eventView = detailsView.findViewById(R.id.dEvents);
            AnimationWorker.fadeOut(eventView,0);
            String evUrl = "https://wattfinder.de/api/get.php?key=" + mContext.getString(R.string.Wattfinder_APIKey) + "&p=0&cp=" + mID;

            JsonObjectRequest pRequest = new JsonObjectRequest(Request.Method.GET,
                    evUrl, (String) null, new Response.Listener<JSONObject>() {


                @Override
                public void onResponse(JSONObject jResponse) {

                    if (jResponse.optBoolean("success", false) && jResponse.optInt("count", 0) > 0) {
                        try {
                            JSONArray jA = jResponse.getJSONArray("events");
                            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                            ViewGroup parentView = (ViewGroup) detailsView.findViewById(R.id.dEvents_list);
                            parentView.removeAllViews();
                            //Sort Entries
                            HashMap<Integer, Long> hmap = new HashMap<Integer, Long>();
                            for (int i = 0; i < jA.length(); i++) {
                                hmap.put(i, jA.getJSONObject(i).optLong("Timestamp", 0));
                            }
                            Map<Integer, Long> map = Utils.sortByValues(hmap);

                            Iterator i = map.keySet().iterator();
                            while (i.hasNext()) {
                                Integer i1 = (Integer) i.next();
                                View childLayout = inflater.inflate(R.layout.layout_chargeevent, null);
                                childLayout.setId((Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ? View.generateViewId() : Utils.generateViewId()));
                                ChargeEvent CE = new ChargeEvent();
                                if (CE.extractFromJSON(jA.getJSONObject(i1))) {
                                    TextView tv = (TextView) childLayout.findViewById(R.id.evDate);
                                               /* Date d = new Date();
                                                d.setTime(CE.getTimestamp());
                                                 */
                                    tv.setText(CE.getTimestampString());
                                    tv.setTag(CE.getEntryId());

                                    tv = childLayout.findViewById(R.id.evNick);
                                    tv.setText(CE.getNickname());

                                    tv = childLayout.findViewById(R.id.evPlug);
                                    tv.setText(CE.getPlug()+"("+CE.getReason()+")");

                                    tv = childLayout.findViewById(R.id.evComment);
                                    tv.setText(CE.getComment());

                                    View v = childLayout.findViewById(R.id.evIconFault);
                                    if(CE.isIsfault())v.setVisibility(View.VISIBLE);

                                    parentView.addView(childLayout);
                                } else {
                                    LogWorker.e("JSON Event Extract", jA.getJSONObject(i1).toString());

                                }
                            }
                        } catch (JSONException jE) {
                            LogWorker.e("JSON Event Count", jE.getLocalizedMessage());
                        }

                        AnimationWorker.fadeIn(eventView, 0, 1.0f);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            KartenActivity.getInstance().addToRequestQueue(pRequest);
        }
    }
}

