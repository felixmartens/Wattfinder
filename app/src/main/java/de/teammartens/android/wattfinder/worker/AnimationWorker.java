package de.teammartens.android.wattfinder.worker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.fragments.DetailsFragment;
import de.teammartens.android.wattfinder.fragments.FilterFragment;
import de.teammartens.android.wattfinder.fragments.ImageZoomFragment;
import de.teammartens.android.wattfinder.fragments.MiniInfoFragment;
import de.teammartens.android.wattfinder.fragments.SmartFilterFragment;
import de.teammartens.android.wattfinder.model.Saeule;

import static de.teammartens.android.wattfinder.KartenActivity.BackstackEXIT;
import static de.teammartens.android.wattfinder.KartenActivity.fragmentManager;
import static de.teammartens.android.wattfinder.KartenActivity.getInstance;
import static de.teammartens.android.wattfinder.KartenActivity.privacyConsent;

/**
 * Created by felix on 02.08.17.
 */

public class AnimationWorker {

    public static boolean startupScreen = true;
    public static boolean smartFilter = true;
    public static final String FLAG_INFO = "infoFragment";
    public static final String FLAG_DETAILS = "detailFragment";
    public static final String FLAG_FILTER = "filterFragment";
    private final static String LOG_TAG = "AnimationWorker";
    public final static Integer MAP = 0;
    public final static Integer INFO = 1;
    public final static Integer DETAIL = 2;
    public final static Integer FILTER = 3;
    private static Integer STATE = MAP;


    public static void show_info() {
        if(!startupScreen) {
            FragmentTransaction fT = fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fragment_slide_in,
                            R.anim.fragment_slide_out,
                            R.anim.fragment_slide_in,
                            R.anim.fragment_slide_out);
            MiniInfoFragment f = (MiniInfoFragment) fragmentManager.findFragmentByTag(FLAG_INFO);
            Fragment df = fragmentManager.findFragmentByTag(FLAG_DETAILS);
            if (!startupScreen && !(df != null && df.isVisible())) {
                //wenn Details zusehen sind dann nix Info

                if (f == null) {
                    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "info wird neu gebildet");
                    fT.add(R.id.infoFragment, Fragment.instantiate(getInstance(), MiniInfoFragment.class.getName()), FLAG_INFO).addToBackStack(FLAG_INFO).commit();
                    f = (MiniInfoFragment) fragmentManager.findFragmentByTag(FLAG_INFO);
                } else if (f.isHidden()) {
                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "info schon vorhanden" + f.isHidden() + " --" + f.isVisible() + "--" + f.isAdded());
                    fT.show(f).addToBackStack(FLAG_INFO).commit(); //replace(R.id.infoFragment, Fragment.instantiate(getInstance(), MiniInfoFragment.class.getName()), "iFragment");

                }
                if (f != null) f.setzeSaeule(SaeulenWorks.getCurrentSaeule());
                setSTATE(INFO);

                hide_fabs();
                // slideUp(getInstance().findViewById(R.id.fab_filter), 0);
                // slideUp(getInstance().findViewById(R.id.fab_mylocation), 0);

                KartenActivity.BackstackEXIT = false;
            }
        }
    }

    public static void hide_info(){hide_fragment(FLAG_INFO);}
    public static void hide_fragment(String FLAG){
        fragmentManager.popBackStack(FLAG,0);
        Fragment f = fragmentManager.findFragmentByTag(FLAG);
        if (f !=null && f.isVisible()){
            try {
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out,
                        R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out).hide(f).commit();
            }catch(IllegalStateException e){
                if(LogWorker.isVERBOSE())LogWorker.e(LOG_TAG,"IllegalSTatException on hide "+FLAG+" "+e.getCause().getMessage());
            }
            //slideDown(getInstance().findViewById(R.id.fab_directions), 0);
            //slideDown(getInstance().findViewById(R.id.fab_directions), 500);
            //slideUp(getInstance().findViewById(R.id.fab_filter), 200);
            //slideUp(getInstance().findViewById(R.id.fab_mylocation), 200);
            KartenActivity.setMapPaddingY(0);
            show_fabs();
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
        Fragment iF = fragmentManager.findFragmentByTag(FLAG_INFO);
        if (iF != null) fT.hide(iF);
        Fragment dF = fragmentManager.findFragmentByTag(FLAG_DETAILS);
        if (dF != null) fT.hide(dF);

        if (f == null) {
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "imagezoom wird neu gebildet");
            fT.add(R.id.imageZoomFragment, Fragment.instantiate(getInstance(), ImageZoomFragment.class.getName()), "izFragment").addToBackStack(null).commit();
        } else if (f.isHidden()) {
            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "imageZoom schon vorhanden" + f.isHidden() + " --" + f.isVisible() + "--" + f.isAdded());
            fT.show(f).addToBackStack(null).commit(); //replace(R.id.infoFragment, Fragment.instantiate(getInstance(), MiniInfoFragment.class.getName()), "iFragment");

        }


        KartenActivity.BackstackEXIT=false;

    }


    public static void hideImageZoom(){
        FragmentTransaction fT = fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out,
                        R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out);
        View v = getInstance().findViewById(R.id.imageZoomFragment);
        fadeOut(v,0);
        Fragment dF = fragmentManager.findFragmentByTag(FLAG_DETAILS);
        if (dF != null) fT.show(dF);
        dF = fragmentManager.findFragmentByTag(FLAG_INFO);
        if (dF != null) fT.hide(dF);
        fT.addToBackStack(FLAG_DETAILS).commit();
    }


    public static void toggleFilter() {

        Fragment f = fragmentManager.findFragmentByTag(FLAG_FILTER);
        if (f != null && f.isVisible()) {
            show_map();
        } else {
            show_filter();

        }
          /* if(layoutStyle().equals("default"))
        {GeoWorks.movemapPosition("toggleFilter",false);if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"ToggleFilter; VErsetz False");}
        else
        {GeoWorks.movemapPosition("toggleFilter",true);if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"ToggleFilter; VErsetz True");}*/
        KartenActivity.BackstackEXIT=false;
    }


    public static void show_filter(){
            FragmentTransaction Ft = fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fragment_slide_in,
                            R.anim.fragment_slide_out,
                            R.anim.fragment_slide_in,
                            R.anim.fragment_slide_out);
            Fragment iF = fragmentManager.findFragmentByTag(FLAG_INFO);
            if (iF != null) Ft.hide(iF);
            iF = fragmentManager.findFragmentByTag(FLAG_FILTER);
            if (iF != null) Ft.hide(iF);
            String fragment = (smartFilter?SmartFilterFragment.class.getName():FilterFragment.class.getName());
            Ft.add(R.id.filterFragment, Fragment
                            .instantiate(getInstance(), fragment),
                    FLAG_FILTER
            ).addToBackStack(FLAG_FILTER).commit();

/*
        View v = getInstance().findViewById(R.id.infoFragment);
        v.setVisibility(GONE);

        v = getInstance().findViewById(R.id.detailFragment);
        v.setVisibility(GONE);
        v = getInstance().findViewById(R.id.filterFragment);
        v.setVisibility(View.VISIBLE);*/
        setSTATE(FILTER);

            hide_fabs();
            hide_mapSearch();
        }

    public static void toggleSmartFilter(){
        FragmentTransaction Ft = fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out,
                        R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out);
        Fragment iF = fragmentManager.findFragmentByTag(FLAG_FILTER);
        if (iF != null) Ft.hide(iF);
        fragmentManager.popBackStack(FLAG_FILTER,0);

        if (smartFilter){
            Ft.add(R.id.filterFragment,Fragment.instantiate(getInstance(), FilterFragment.class.getName()),FLAG_FILTER).addToBackStack(FLAG_FILTER).commit();
            smartFilter=false;
        }else{
            Ft.add(R.id.filterFragment,Fragment.instantiate(getInstance(), SmartFilterFragment.class.getName()),FLAG_FILTER).addToBackStack(FLAG_FILTER).commit();
            smartFilter=true;

        }


        KartenActivity.sharedPref.edit().putBoolean("smartFilter",smartFilter).apply();
    }

    /*
    public static void showDetails(){
        DetailsFragment df = (DetailsFragment)getFragment(FLAG_DETAILS);
        df.setzeSaeule(SaeulenWorks.currentSaeule);
        View v = getInstance().findViewById(R.id.infoFragment);
        v.setVisibility(GONE);
        v = getInstance().findViewById(R.id.filterFragment);
        v.setVisibility(GONE);
        v = getInstance().findViewById(R.id.detailFragment);
        v.setVisibility(View.VISIBLE);
          setSTATE(DETAIL);

    }*/
    public static void toggleDetails() {

        Fragment f = fragmentManager.findFragmentByTag("dFragment");

        if (f != null) {
            fragmentManager.popBackStack(FLAG_DETAILS,0);

            show_fabs();
            GeoWorks.movemapPosition("hideDetails");

        } else {
          show_details();
        }

        KartenActivity.BackstackEXIT=false;
    }

    public static void show_details(){show_details(SaeulenWorks.getCurrentSaeule());}
    public static void show_details(Saeule S){
        FragmentTransaction Ft = fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out,
                        R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out);
        Fragment iF = fragmentManager.findFragmentByTag(FLAG_INFO);
        if (iF != null){
            //fragmentManager.popBackStack(FLAG_INFO,0);
            Ft.hide(iF);}
        DetailsFragment dF = getDetailsFragment();

        if(dF==null) {
            Ft.add(R.id.detailFragment, Fragment
                            .instantiate(getInstance(), DetailsFragment.class.getName()),
                    FLAG_DETAILS
            ).addToBackStack(FLAG_DETAILS).commit();
            dF = getDetailsFragment();
        }else{
            if(!isDetailsVisibile())Ft.show(dF).addToBackStack(FLAG_DETAILS).commit();
        }

       if(dF!=null)dF.setzeSaeule(S);

        hide_mapSearch();
        hide_fabs();
        //GeoWorks.animateClick(true);

    }

    public static DetailsFragment getDetailsFragment(){
        return (DetailsFragment) fragmentManager.findFragmentByTag(FLAG_DETAILS);
    }

    public static Fragment getFragment(String TAG){
        return fragmentManager.findFragmentByTag(TAG);
    }


    public static void show_fabs(){
        hide_mapSearch();
        View fabs = getInstance().findViewById(R.id.fabContainer);
        slideTopDown(fabs,0);

    }

    public static void hide_fabs(){

        View fabs = getInstance().findViewById(R.id.fabContainer);
        slideTopUp(fabs,0);

    }



    public static void show_map(){
        if (KartenActivity.isMapReady()) {
            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Show Map");
            if(fragmentManager==null)fragmentManager=KartenActivity.getInstance().getSupportFragmentManager();

        if(!fragmentManager.isStateSaved()&&fragmentManager.getBackStackEntryCount()>0) {

        if (isVisible(FLAG_DETAILS))
            fragmentManager.popBackStack(FLAG_DETAILS, 0);
        fragmentManager.popBackStack(FLAG_INFO, 0);


        if (isVisible(FLAG_FILTER))
            fragmentManager.popBackStack(FLAG_FILTER, 0);
        }
        if(!fragmentManager.isStateSaved()&&fragmentManager.getBackStackEntryCount()>0&&!BackstackEXIT)
            fragmentManager.popBackStack();
            slideUp(getInstance().findViewById(R.id.fab_filter), 200);
            slideUp(getInstance().findViewById(R.id.fab_mylocation), 200);


            setSTATE(MAP);

            show_debug();
            show_fabs();
            hide_mapSearch();
            //hide_info();
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"showMap trying to hide info: "+(getFragment(FLAG_INFO)==null?"null":"notnull "+getFragment(FLAG_INFO).isVisible()));
            if (KartenActivity.mapFragment != null&&KartenActivity.mapFragment.getView()!=null)
                KartenActivity.mapFragment.getView().requestFocus();
            //slideDown(getInstance().findViewById(R.id.fab_directions), 500);
            //slideUp(getInstance().findViewById(R.id.fab_filter), 200);
            //slideUp(getInstance().findViewById(R.id.fab_mylocation), 200);
            //findViewById(R.id.fab_filter).requestFocus();
            //GeoWorks.animateClick(false);
            KartenActivity.setMapPaddingY(0);
            GeoWorks.movemapPosition("showMap");
            KartenActivity.BackstackEXIT = false;
        }
    }


    public static void show_debug(){
        TextView t = (TextView) getInstance().findViewById(R.id.debugHeader);
        t.setText("DEBUG VERSION! LogID:"+LogWorker.getlogID());
        //mapSearch.setVisibility(View.VISIBLE);
        //mapSearch.bringToFront();
        if(LogWorker.isVERBOSE())t.setVisibility(View.VISIBLE);else t.setVisibility(View.GONE);
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
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"hideStartup "+startupScreen);

        View startup = getInstance().findViewById(R.id.startupScreen);
        if (startupScreen) {
            if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"privacyConsent "+privacyConsent);
            //if(!KartenActivity.privacyConsent && (System.currentTimeMillis() - KartenActivity.sharedPref.getLong(KartenActivity.sP_Timestamp,0))>3600*1000) {//Zeige Eula wenn privacyConsent nicht aktiviert und letztes Programmende ist mindestens eine Stunde eher
              if(!KartenActivity.privacyConsent){
                  if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"privacyConsent treffer ");

                  final View v = getInstance().findViewById(R.id.eulaScreen);
                if (v != null) {
                    fadeIn(v, 0, 1.0f);
                    hide_mapSearch();

                    TextView tv = getInstance().findViewById(R.id.welcomeText);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        tv.setText(Html.fromHtml(getInstance().getString(R.string.privacy_text_short), Html.FROM_HTML_MODE_COMPACT));
                    else
                        tv.setText(Html.fromHtml(getInstance().getString(R.string.privacy_text_short)));


                    Button b = (Button) getInstance().findViewById(R.id.eulaButton);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final CheckBox cb = (CheckBox) getInstance().findViewById(R.id.privacy_consent);
                            TextView tv = getInstance().findViewById(R.id.privacyText);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                tv.setText(Html.fromHtml(getInstance().getString(R.string.privacy_desc_long), Html.FROM_HTML_MODE_COMPACT));
                            else
                                tv.setText(Html.fromHtml(getInstance().getString(R.string.privacy_desc_long)));
                             tv = getInstance().findViewById(R.id.privacyLicense);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                tv.setText(Html.fromHtml(getInstance().getString(R.string.privacy_license), Html.FROM_HTML_MODE_COMPACT));
                            else
                                tv.setText(Html.fromHtml(getInstance().getString(R.string.privacy_license)));

                            show_fabs();
                            View vv = getInstance().findViewById(R.id.welcome_scroll);
                            fadeOut(vv,0);
                            vv = getInstance().findViewById(R.id.privacy_long);
                            fadeIn(vv,0,1.0f);
                            vv = getInstance().findViewById(R.id.eulaSubTitle);
                            fadeIn(vv,0,1.0f);

                            Button b = (Button) getInstance().findViewById(R.id.eulaButton);
                            b.setText(R.string.close);
                            b.setEnabled(false);
                            b.setAlpha(0.3f);
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    KartenActivity.privacyConsent = cb.isChecked();
                                    KartenActivity.sharedPref.edit().putBoolean("privacyConsent", KartenActivity.privacyConsent).apply();
                                    slideDown(v, 0);
                                    GeoWorks.setupLocationListener();

                                    SaeulenWorks.reloadMarker();

                                    show_fabs();
                                    show_debug();

                                }
                            });
                            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    Button b = (Button) getInstance().findViewById(R.id.eulaButton);
                                    if(isChecked)b.setAlpha(1.0f);else b.setAlpha(0.3f);
                                    b.setEnabled(isChecked);
                                }
                            });

                        }
                    });

                }
            }
            KartenActivity.setMapCenter();

            slideDown(startup, 500);
            startup.setVisibility(View.GONE);
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
    if(startup!=null) {
    startup.setVisibility(View.VISIBLE);}
    FilterWorks.refresh_filterlisten_API();

    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "showStartup");
    //hide_mapSearch();

        startupScreen = true;

    }

    public static void slideDown (final View V,Integer offset) {
        if (V== null) return;

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
    public static void rollDown (final View V,Integer offset) {
        if (V== null) return;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            V.animate()
                    .setStartDelay(offset)
                    .y(V.getHeight())
                    .alpha(0.0f)
                    .setDuration(500)
            ;
        } else {

            V.animate()
                    .y(V.getHeight())
                    .alpha(0.0f)
                    .setDuration(500)
            ;
        }
    }
    public static void slideUp (final View V,Integer offset) {
        if (V== null) return;
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

    public static void rollUp (final View V,Integer offset) {
        if (V== null) return;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            V.animate()
                    .setStartDelay(offset)
                    .y(0)
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
                    .y(0)
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

        if (V== null) return;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            V.animate()
                    .setStartDelay(offset)

                    .alpha(0.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.GONE);
                        }
                    });

        } else {
            V.animate()

                    .alpha(0.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.GONE);

                        }
                    });

        }
    }

    public static void fade2Invisible (final View V,Integer offset) {

        if (V== null) return;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            V.animate()
                    .setStartDelay(offset)

                    .alpha(0.001f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.INVISIBLE);
                        }
                    });

        } else {
            V.animate()

                    .alpha(0.001f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.INVISIBLE);

                        }
                    });

        }
    }

    public static void fadeIn (final View V,Integer offset, Float Alpha) {
        if (V== null) return;
        if(V.getVisibility()==View.GONE){
            V.setAlpha(0f);
            V.setVisibility(View.VISIBLE);
        }
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




    public static void slideTopDown (final View V,Integer offset) {

        if (V== null) return;
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

    }
    public static void slideTopUp (final View V,Integer offset) {

        if (V== null) return;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            V.animate()
                    .setStartDelay(offset)
                    .translationY(-V.getHeight())
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.VISIBLE);

                            V.clearFocus();
                        }
                    })
            ;
        } else {
            V.animate()
                    .translationY(-V.getHeight())
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.VISIBLE);

                            V.clearFocus();
                        }
                    })
            ;
        }

    }


    public static void showSearchBar(){

        View v = getInstance().findViewById(R.id.fab_search);
        fade2Invisible(v,200);
        final View V = getInstance().findViewById(R.id.searchContainer);

        V.animate()
                .translationX(0)
                .alpha(1.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        V.setVisibility(View.VISIBLE);
                        V.bringToFront();
                        V.clearFocus();
                        V.findViewById(R.id.map_search).requestFocusFromTouch();
                        //v.setVisibility(View.INVISIBLE);
                    }
                })
        ;

    }


    public static void hide_mapSearch(){
        View v = getInstance().findViewById(R.id.fab_search);
        fadeIn(v,0,1.0f);
        final View V = getInstance().findViewById(R.id.searchContainer);

        V.animate()
                .translationX(V.getWidth())
                .alpha(0.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        V.setVisibility(View.VISIBLE);

                        V.clearFocus();
                    }
                })
        ;
    }



    public static boolean isVisible(String FLAG){

        Fragment fragment = fragmentManager.findFragmentByTag(FLAG);
        return (fragment != null && fragment.isVisible());
    }

    public static boolean isFilterVisibile(){
       return isVisible(FLAG_FILTER);
    }

    public static boolean isDetailsVisibile(){
        return isVisible(FLAG_DETAILS);
    }

    public static Integer getSTATE() {
        return STATE;
    }

    public static void setSTATE(Integer STATE) {
        AnimationWorker.STATE = STATE;
    }
}
