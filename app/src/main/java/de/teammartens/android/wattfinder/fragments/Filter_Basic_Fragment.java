package de.teammartens.android.wattfinder.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.worker.FilterWorks;
import de.teammartens.android.wattfinder.worker.LogWorker;
import de.teammartens.android.wattfinder.worker.SaeulenWorks;

/**
 * Created by felix on 30.10.16.
 */

public class Filter_Basic_Fragment extends Fragment {
    private static View filterBasicView;
    private static final String LOG_TAG = "Fragment Basic Filter";

    public Filter_Basic_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        filterBasicView = inflater.inflate(R.layout.fragment_filter_basic, container, false);
        return filterBasicView;
    }


    public void onResume(){
        super.onResume();

        CheckBox v = (CheckBox) this.getView().findViewById(R.id.fKostenlos);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                cb.setSelected(FilterWorks.setze_filter(FilterWorks.F_KOSTENLOS, cb.isChecked()));
                SaeulenWorks.checkMarkerCache("FILTER_Basic");
            }
        });



        v = (CheckBox) filterBasicView.findViewById(R.id.fKostenlosparken);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                cb.setChecked(FilterWorks.setze_filter(FilterWorks.F_PARKEN, cb.isChecked()));
                SaeulenWorks.checkMarkerCache("FILTER_Basic");
            }
        });

        v = (CheckBox) filterBasicView.findViewById(R.id.fHotels);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                cb.setChecked(FilterWorks.setze_filter(FilterWorks.F_HOTELS, cb.isChecked()));
                SaeulenWorks.checkMarkerCache("FILTER_Basic");
            }
        });

        v = (CheckBox) filterBasicView.findViewById(R.id.fRestaurants);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                cb.setChecked(FilterWorks.setze_filter(FilterWorks.F_RESTAURANT, cb.isChecked()));
                SaeulenWorks.checkMarkerCache("FILTER_Basic");
            }
        });

        v = (CheckBox) filterBasicView.findViewById(R.id.fBestaetigt);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                cb.setChecked(FilterWorks.setze_filter(FilterWorks.F_BESTAETIGT, cb.isChecked()));
                SaeulenWorks.checkMarkerCache("FILTER_Basic");
            }
        });

        v = (CheckBox) filterBasicView.findViewById(R.id.fBarrierefrei);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                cb.setChecked(FilterWorks.setze_filter(FilterWorks.F_BARRIEREFREI, cb.isChecked()));
                SaeulenWorks.checkMarkerCache("FILTER_Basic");
            }
        });

        v = (CheckBox) filterBasicView.findViewById(R.id.fStoerung);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                cb.setChecked(FilterWorks.setze_filter(FilterWorks.F_KEINESTOERUNG, cb.isChecked()));
                SaeulenWorks.checkMarkerCache("FILTER_Basic");
            }
        });

        Spinner v2 = (Spinner) filterBasicView.findViewById(R.id.f_opening);

        v2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {FilterWorks.setze_filter(FilterWorks.F_open247, false);FilterWorks.setze_filter(FilterWorks.F_opennow, false);}
                if (position == 1) {FilterWorks.setze_filter(FilterWorks.F_open247, true);FilterWorks.setze_filter(FilterWorks.F_opennow, false);}
                if (position == 2) {FilterWorks.setze_filter(FilterWorks.F_open247, false);FilterWorks.setze_filter(FilterWorks.F_opennow, true);}

                SaeulenWorks.checkMarkerCache("FILTER_Basic");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SeekBar s = (SeekBar) filterBasicView.findViewById(R.id.fPowerBar);
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    TextView t = (TextView) filterBasicView.findViewById(R.id.fPowerDisplay);
                    t.setText("\u2265" + FilterWorks.setze_power(progress) + "kW");
                    SaeulenWorks.checkMarkerCache("FILTER_Basic");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"FilterBasic onResume");
        ladeListe();
    }


    public static void ladeListe(){
        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"LadeListe");
        CheckBox c  = (CheckBox) filterBasicView.findViewById(R.id.fKostenlos);

        c.setChecked(FilterWorks.lese_filter(FilterWorks.F_KOSTENLOS));

        c = (CheckBox) filterBasicView.findViewById(R.id.fKostenlosparken);

        c.setChecked(FilterWorks.lese_filter(FilterWorks.F_PARKEN));

        c = (CheckBox) filterBasicView.findViewById(R.id.fBestaetigt);

        c.setChecked(FilterWorks.lese_filter(FilterWorks.F_BESTAETIGT));
        c = (CheckBox) filterBasicView.findViewById(R.id.fBarrierefrei);

        c.setChecked(FilterWorks.lese_filter(FilterWorks.F_BARRIEREFREI));
        //ergänze die Erläuterung zum Barrierefrei FIlter
        TextView t = (TextView) filterBasicView.findViewById(R.id.fBarrierefrei_hint);
        String bfText ="";
        final int listenlang = (FilterWorks.listenlaenge(FilterWorks.F_VERBUND)+FilterWorks.listenlaenge(FilterWorks.F_KARTEN));
        if (listenlang>0) {
            bfText = KartenActivity.getInstance().getString(R.string.also);
            t.setText(listenlang+" "+KartenActivity.getInstance().getString(R.string.filterKartenaktiv));
        }
        else{
            bfText=KartenActivity.getInstance().getString(R.string.only);
            t.setText(KartenActivity.getInstance().getString(R.string.filterkeineKarten));
        }
        c.setText(bfText+ " " +KartenActivity.getInstance().getString(R.string.filterBarrierefrei));




        c = (CheckBox) filterBasicView.findViewById(R.id.fStoerung);

        c.setChecked(FilterWorks.lese_filter(FilterWorks.F_KEINESTOERUNG));


        Spinner sp = (Spinner) filterBasicView.findViewById(R.id.f_opening);
        List<String> list = new ArrayList<String>();
        list.add(KartenActivity.getInstance().getString(R.string.filter_opananytime));
        list.add(KartenActivity.getInstance().getString(R.string.filter247));
        list.add(KartenActivity.getInstance().getString(R.string.filter_openednow));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(KartenActivity.getInstance(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(dataAdapter);

        if(FilterWorks.lese_filter(FilterWorks.F_open247)) sp.setSelection(1);
            else if (FilterWorks.lese_filter(FilterWorks.F_opennow)) sp.setSelection(2);
            else sp.setSelection(0);

        SeekBar s = (SeekBar) filterBasicView.findViewById(R.id.fPowerBar);
        t = (TextView) filterBasicView.findViewById(R.id.fPowerDisplay);
        Integer p=FilterWorks.lese_minpower();
        s.setMax(FilterWorks.F_POWER_VALUE.length-1);
        t.setText("\u2265"+FilterWorks.F_POWER_VALUE[0]+"kW");
        for (Integer i=0;i<FilterWorks.F_POWER_VALUE.length;i++) {
            if (p>=FilterWorks.F_POWER_VALUE[i]){
                s.setProgress(i);
                t.setText("\u2265"+FilterWorks.F_POWER_VALUE[i]+"kW");}
        }




    }

    public void onPause(){
        super.onPause();

    }

    public void onStop(){
        super.onStop();

    }

    public void onStart(){
        super.onStart();

    }

    public void onDestroy(){
        super.onDestroy();

    }


}
