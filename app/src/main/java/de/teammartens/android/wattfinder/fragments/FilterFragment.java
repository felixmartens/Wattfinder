package de.teammartens.android.wattfinder.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;


import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.worker.FilterWorks;
import de.teammartens.android.wattfinder.worker.GeoWorks;
import de.teammartens.android.wattfinder.worker.LogWorker;

import static de.teammartens.android.wattfinder.KartenActivity.slideDown;

/**
 * Created by felix on 10.05.15.
 */
public class FilterFragment extends Fragment {



    private TabLayout tabLayout;
    private static ViewPager viewPager;
    private static final String LOG_TAG = "FilterFragment";
    private static View filterView;
    private static FragmentManager cFm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        filterView = inflater.inflate(R.layout.fragment_filter, container, false);



       // toolbar = (Toolbar) filterView.findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);

       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) filterView.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) filterView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 4)
                {View v = filterView.findViewById(R.id.fab_container);
                    slideDown(v, 100);
                    }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        TextView v = (TextView) filterView.findViewById(R.id.fPreset);
        v.setText(KartenActivity.getInstance().getString(R.string.filterPreset)+" "+FilterWorks.PRESET);
        return filterView;

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new Filter_Basic_Fragment(), getString(R.string.filterBasic));
        adapter.addFragment(new Filter_plugs_Fragment(), getString(R.string.filterSteckerTitel));
        adapter.addFragment(new Filter_cards_Fragment(), getString(R.string.filterLadekartenTitel));
        adapter.addFragment(new Filter_carrier_Fragment(), getString(R.string.filterVerbundTitel));
       // adapter.addFragment(new Filter_Presets_Fragment(), getString(R.string.filterPresets));
        viewPager.setAdapter(adapter);

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }






    public void onStart(){
        super.onStart();
        if (filterView != null) {


            filterView.setAlpha(1.0f);



        }
    }

public void onResume(){
    super.onResume();


    View v = (View) filterView.findViewById(R.id.fPreset);



if(v!=null)
    v.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"FAB Presets");
           show_presets();

        }
    });
    KartenActivity.hide_mapSearch();



    //v = (View) filterView.findViewById(R.id.fab_done);
    //slideDown(v, 0);
    /*if(v!=null)
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideDown(v,200);
                Filter_Presets_Fragment.renameProfil();
               hide_presets();
                Filter_Basic_Fragment.ladeListe();
            }
        });*/

    cFm = getChildFragmentManager();

    if (KartenActivity.layoutStyle()=="land") {
        KartenActivity.setMapPaddingX(filterView.getWidth());
        GeoWorks.movemapPosition("FilterFragment");
    }
}
public void onPause(){
    super.onPause();


}

    public static void setPresetLabel(){
        TextView v = (TextView) filterView.findViewById(R.id.fPreset);
        v.setText(FilterWorks.PRESET);
    }

private void show_presets(){
FragmentManager fM = getChildFragmentManager();
    FragmentTransaction fT = fM.beginTransaction()
            .setCustomAnimations(R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out,
                    R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out);
    Fragment f = fM.findFragmentByTag("pFragment");

        if (f == null) {
            if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Presets wird neu gebildet");
            fT.add(R.id.fragment_filter_presets, Fragment.instantiate(this.getActivity(), Filter_Presets_Fragment.class.getName()), "pFragment").addToBackStack(null).commit();
        } else if (f.isHidden()) {
            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "Presets schon vorhanden" + f.isHidden() + " --" + f.isVisible() + "--" + f.isAdded());
            fT.show(f).addToBackStack(null).commit(); //replace(R.id.infoFragment, Fragment.instantiate(getInstance(), MiniInfoFragment.class.getName()), "iFragment");

        }

    //slideUp(filterView.findViewById(R.id.fab_done), 200);

    //KartenActivity.BackstackEXIT=false;
    }


    public static void hide_presets(){

        Fragment f = cFm.findFragmentByTag("pFragment");
        if (f !=null && f.isVisible()){
            cFm.beginTransaction().setCustomAnimations(R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out,
                    R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out).hide(f).commit();
            setPresetLabel();
            updatePagerChild();
        }

    }

    public static void updatePagerChild(){
        if (LogWorker.isVERBOSE())
            LogWorker.d(LOG_TAG, "updatePagerChild " + viewPager.getCurrentItem() + " Preset:"+FilterWorks.PRESET);
        switch (viewPager.getCurrentItem()){
            case 0:
                Filter_Basic_Fragment.ladeListe();
                break;
            case 1:
                Filter_plugs_Fragment.ladeListe();
                break;
            case 2:
                Filter_cards_Fragment.ladeListe();
                break;
            case 3:
                Filter_carrier_Fragment.ladeListe();
                break;
            default:
                Filter_Basic_Fragment.ladeListe();
                break;

        }

    }



}