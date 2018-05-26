package de.teammartens.android.wattfinder.worker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
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

import static de.teammartens.android.wattfinder.KartenActivity.fragmentManager;
import static de.teammartens.android.wattfinder.KartenActivity.getInstance;
import static de.teammartens.android.wattfinder.KartenActivity.privacyConsent;

/**
 * Created by felix on 02.08.17.
 */

public class AnimationWorker {

    public static boolean startupScreen = true;
    public static boolean smartFilter = true;

    private final static String LOG_TAG = "AnimationWorker";



    public static void show_info() {

        FragmentTransaction fT = fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out,
                        R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out);
        Fragment f = fragmentManager.findFragmentByTag("iFragment");
        Fragment df = fragmentManager.findFragmentByTag("dFragment");
        if(startupScreen||(df!=null&&df.isVisible()))return;else {
            //wenn Details zusehen sind dann nix Info

            if (f == null) {
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "info wird neu gebildet");
                fT.add(R.id.infoFragment, Fragment.instantiate(getInstance(), MiniInfoFragment.class.getName()), "iFragment").addToBackStack(null).commit();
            } else if (f.isHidden()) {
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "info schon vorhanden" + f.isHidden() + " --" + f.isVisible() + "--" + f.isAdded());
                fT.show(f).addToBackStack(null).commit(); //replace(R.id.infoFragment, Fragment.instantiate(getInstance(), MiniInfoFragment.class.getName()), "iFragment");

            }

            hide_fabs();
            // slideUp(getInstance().findViewById(R.id.fab_filter), 0);
            // slideUp(getInstance().findViewById(R.id.fab_mylocation), 0);

            KartenActivity.BackstackEXIT=false;
        }
    }

    public static void hide_info(){
        Fragment f = fragmentManager.findFragmentByTag("iFragment");
        if (f !=null && f.isVisible()){
            try {
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out,
                        R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out).hide(f).commit();
            }catch(IllegalStateException e){
                LogWorker.e(LOG_TAG,"IllegalSTatException on hideInfo "+e.getCause().getMessage());
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
        Fragment iF = fragmentManager.findFragmentByTag("iFragment");
        if (iF != null) fT.hide(iF);
        Fragment dF = fragmentManager.findFragmentByTag("dFragment");
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
        Fragment dF = fragmentManager.findFragmentByTag("dFragment");
        if (dF != null) fT.show(dF);
        dF = fragmentManager.findFragmentByTag("iFragment");
        if (dF != null) fT.hide(dF);
        fT.addToBackStack("details").commit();
    }


    public static void toggleFilter() {

        Fragment f = fragmentManager.findFragmentByTag("fFragment");
        if (f != null) {
            fragmentManager.popBackStack();

            show_fabs();
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
            Fragment iF = fragmentManager.findFragmentByTag("fFragment");
            if (iF != null) Ft.hide(iF);
            String fragment = (smartFilter?SmartFilterFragment.class.getName():FilterFragment.class.getName());
            Ft.add(R.id.filterFragment, Fragment
                            .instantiate(getInstance(), fragment),
                    "fFragment"
            ).addToBackStack(null).commit();



            hide_fabs();
            hide_mapSearch();
        }

    public static void toggleSmartFilter(){
        FragmentTransaction Ft = fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out,
                        R.anim.fragment_slide_in,
                        R.anim.fragment_slide_out);
        Fragment iF = fragmentManager.findFragmentByTag("fFragment");
        if (iF != null) Ft.hide(iF);
        fragmentManager.popBackStack();
        if (smartFilter){
            Ft.add(R.id.filterFragment,Fragment.instantiate(getInstance(), FilterFragment.class.getName()),"fFragment").addToBackStack(null).commit();
            smartFilter=false;
        }else{
            Ft.add(R.id.filterFragment,Fragment.instantiate(getInstance(), SmartFilterFragment.class.getName()),"fFragment").addToBackStack(null).commit();
            smartFilter=true;

        }


        KartenActivity.sharedPref.edit().putBoolean("smartFilter",smartFilter).commit();
    }

    public static void toggleDetails() {

        Fragment f = fragmentManager.findFragmentByTag("dFragment");
        if (f != null) {
            fragmentManager.popBackStack();

            show_fabs();
            GeoWorks.movemapPosition("hideDetails");

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
            hide_fabs();
            //GeoWorks.animateClick(true);

        }

        KartenActivity.BackstackEXIT=false;
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
            if (isDetailsVisibile())
                fragmentManager.popBackStack();


            if (isFilterVisibile())
                fragmentManager.popBackStack();


            hide_info();
            slideUp(getInstance().findViewById(R.id.fab_filter), 200);
            slideUp(getInstance().findViewById(R.id.fab_mylocation), 200);
            show_debug();
            show_fabs();
            hide_mapSearch();
            if (KartenActivity.mapFragment != null&&KartenActivity.mapFragment.getView()!=null)
                KartenActivity.mapFragment.getView().requestFocus();
            //slideDown(getInstance().findViewById(R.id.fab_directions), 500);
            //slideUp(getInstance().findViewById(R.id.fab_filter), 200);
            //slideUp(getInstance().findViewById(R.id.fab_mylocation), 200);
            //findViewById(R.id.fab_filter).requestFocus();
            //GeoWorks.animateClick(false);
            KartenActivity.setMapPaddingY(0);
            //GeoWorks.movemapPosition("showMap",false);
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
                                    KartenActivity.sharedPref.edit().putBoolean("privacyConsent", KartenActivity.privacyConsent).commit();
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
        FilterWorks.refresh_filterlisten_API();

        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"showStartup");
        startupScreen=true;
        //hide_mapSearch();

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
            ;
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
            ;
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
            ;
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
            ;
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

    public static boolean isDetailsVisibile(){

        Fragment dFragment = fragmentManager.findFragmentByTag("dFragment");

        if (dFragment != null && dFragment.isVisible()){
            // if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Details Fragment visible.");
            return true;
        }

        //  if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Details Fragment NOT visible.");
        return false;
    }

    public static boolean isFilterVisibile(){

        Fragment fFragment = fragmentManager.findFragmentByTag("fFragment");
        if (fFragment != null && fFragment.isVisible()){
            // if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Filter Fragment visible.");
            return true;
        }

        //if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Filter Fragment NOT visible.");

        return false;
    }

}
