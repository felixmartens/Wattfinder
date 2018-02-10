package de.teammartens.android.wattfinder.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.model.FilterEintrag;
import de.teammartens.android.wattfinder.worker.AnimationWorker;
import de.teammartens.android.wattfinder.worker.FilterWorks;
import de.teammartens.android.wattfinder.worker.GeoWorks;
import de.teammartens.android.wattfinder.worker.LogWorker;


/**
 * Created by felix on 10.05.15.
 */
public class SmartFilterFragment extends DialogFragment implements CheckBox.OnCheckedChangeListener {


    private static final String LOG_TAG = "SmartFilterFragment";
    private static View filterView;
    private static FragmentManager cFm;
    private static SmartFilterFragment instance;
    private static final int STECKER = 0;
    private static final int KARTEN = 1;
    //private static HashMap<String,Boolean> Stecker, Karten;
    private static List<FilterEintrag> Stecker,Karten;
    private static String[] SteckerListe, KartenListe;
    private static Set<String> L_Stecker,L_Karten = new HashSet<String>();
    private static String presetLabel = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        filterView = inflater.inflate(R.layout.fragment_filter_smart, container, false);


        return filterView;

    }






    public void onStart(){
        super.onStart();
        if (filterView != null) {


            filterView.setAlpha(1.0f);



        }
    }

public void onResume(){
    super.onResume();


   TextView  t= (TextView) filterView.findViewById(R.id.filter_classic_design);
    t.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimationWorker.toggleSmartFilter();
        }
    });


    View v = (View) filterView.findViewById(R.id.fab_preset);

    instance=this;

if(v!=null)
    v.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"FAB Presets");
           show_presets();

        }
    });
    //AnimationWorker.slideUp(v,0);
    v = (View) filterView.findViewById(R.id.fab_done);

    if(v!=null)
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"FAB Done");
                hide_presets();

            }
        });
    AnimationWorker.hide_mapSearch();
    AnimationWorker.hide_fabs();


    cFm = getChildFragmentManager();

    if (KartenActivity.layoutStyle()=="land") {
        KartenActivity.setMapPaddingX(filterView.getWidth());
        GeoWorks.movemapPosition("FilterFragment");
    }
    presetLabel = KartenActivity.getInstance().getResources().getString(R.string.filterPreset);
    setPresetLabel();
    refresh_Adapter(KARTEN);
    refresh_Adapter(STECKER);
    ladeFilter();

    smartview();

}
    public void onPause(){
        super.onPause();


    }



    public static void ladeFilter(){

        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"LadeListe");
        CheckBox c  = (CheckBox) filterView.findViewById(R.id.fKostenlos);
        L_Stecker = new HashSet<String>(5);
        L_Stecker.add("Typ2");L_Stecker.add("Schuko");L_Stecker.add("CHAdeMO");L_Stecker.add("CCS");L_Stecker.add("Tesla Supercharger");
        c  = (CheckBox) filterView.findViewById(R.id.card_plug_typ2);
        c.setChecked(FilterWorks.lese_liste(FilterWorks.F_STECKER,"Typ2"));
        c.setTag(FilterWorks.F_STECKER);
        c.setOnCheckedChangeListener(instance);
        c  = (CheckBox) filterView.findViewById(R.id.card_plug_ccs);
        c.setChecked(FilterWorks.lese_liste(FilterWorks.F_STECKER,"CCS"));
        c.setTag(FilterWorks.F_STECKER);
        c.setOnCheckedChangeListener(instance);
        c  = (CheckBox) filterView.findViewById(R.id.card_plug_chademo);
        c.setChecked(FilterWorks.lese_liste(FilterWorks.F_STECKER,"CHAdeMO"));
        c.setTag(FilterWorks.F_STECKER);
        c.setOnCheckedChangeListener(instance);
        c  = (CheckBox) filterView.findViewById(R.id.card_plug_schuko);
        c.setChecked(FilterWorks.lese_liste(FilterWorks.F_STECKER,"Schuko"));
        c.setTag(FilterWorks.F_STECKER);
        c.setOnCheckedChangeListener(instance);
        c  = (CheckBox) filterView.findViewById(R.id.card_plug_tesla);
        c.setChecked(FilterWorks.lese_liste(FilterWorks.F_STECKER,"Tesla Supercharger"));
        c.setTag(FilterWorks.F_STECKER);
        c.setOnCheckedChangeListener(instance);

        Switch s = (Switch)filterView.findViewById(R.id.filter_card_fastcharge);
        s.setSelected((FilterWorks.lese_minpower()>40));


        s = (Switch) filterView.findViewById(R.id.filter_card_barrierefrei);
        s.setChecked(FilterWorks.lese_filter(FilterWorks.F_BARRIEREFREI));
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FilterWorks.setze_filter(FilterWorks.F_BARRIEREFREI,isChecked);
                smartview();
            }
        });


        TextView t= (TextView) filterView.findViewById(R.id.filter_show_all_plugs);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(STECKER);
            }
        });

        c  = (CheckBox) filterView.findViewById(R.id.fKostenlos);
        c.setChecked(FilterWorks.lese_filter(FilterWorks.F_KOSTENLOS));
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FilterWorks.setze_filter(FilterWorks.F_KOSTENLOS,isChecked);
                smartview();
            }
        });
        c = (CheckBox) filterView.findViewById(R.id.fBestaetigt);
        c.setChecked(FilterWorks.lese_filter(FilterWorks.F_BESTAETIGT));
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FilterWorks.setze_filter(FilterWorks.F_BESTAETIGT,isChecked);
                smartview();
            }
        });
        c = (CheckBox) filterView.findViewById(R.id.fKostenlosparken);
        c.setChecked(FilterWorks.lese_filter(FilterWorks.F_PARKEN));
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FilterWorks.setze_filter(FilterWorks.F_PARKEN,isChecked);
                smartview();
            }
        });



        Spinner sp = (Spinner) filterView.findViewById(R.id.f_opening);
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



        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {FilterWorks.setze_filter(FilterWorks.F_open247, false);FilterWorks.setze_filter(FilterWorks.F_opennow, false);}
                if (position == 1) {FilterWorks.setze_filter(FilterWorks.F_open247, true);FilterWorks.setze_filter(FilterWorks.F_opennow, false);}
                if (position == 2) {FilterWorks.setze_filter(FilterWorks.F_open247, false);FilterWorks.setze_filter(FilterWorks.F_opennow, true);}
                smartview();
                // SaeulenWorks.checkMarkerCache("FILTER_Smart");

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


  /*      SeekBar s = (SeekBar) filterBasicView.findViewById(R.id.fPowerBar);
        t = (TextView) filterBasicView.findViewById(R.id.fPowerDisplay);
        Integer p=FilterWorks.lese_minpower();
        s.setMax(FilterWorks.F_POWER_VALUE.length-1);
        t.setText("\u2265"+FilterWorks.F_POWER_VALUE[0]+"kW");
        for (Integer i=0;i<FilterWorks.F_POWER_VALUE.length;i++) {
            if (p>=FilterWorks.F_POWER_VALUE[i]){
                s.setProgress(i);
                t.setText("\u2265"+FilterWorks.F_POWER_VALUE[i]+"kW");}
        }

*/
        s = (Switch) filterView.findViewById(R.id.fStoerung);
        s.setChecked(FilterWorks.lese_filter(FilterWorks.F_KEINESTOERUNG));
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FilterWorks.setze_filter(FilterWorks.F_KEINESTOERUNG,isChecked);
                smartview();
            }
        });

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         buttonView.setChecked(FilterWorks.liste_aendern(buttonView.getTag().toString(),buttonView.getText().toString()));
         smartview();
    }

    public static void setPresetLabel(){
        TextView v = (TextView) filterView.findViewById(R.id.filter_smart_preset_label);
        v.setText(presetLabel+FilterWorks.PRESET);
    }

private void show_presets(){
FragmentManager fM = getChildFragmentManager();
    FragmentTransaction fT = fM.beginTransaction()
            .setCustomAnimations(R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out,
                    R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out);
    Fragment f = fM.findFragmentByTag("pFragment");

    View v = filterView.findViewById(R.id.card_plug);
    AnimationWorker.fadeOut(v,0);
    v = filterView.findViewById(R.id.card_card);
    AnimationWorker.fadeOut(v,200);
    v= filterView.findViewById(R.id.card_extras);
    AnimationWorker.fadeOut(v,400);

  v = filterView.findViewById(R.id.fab_preset);
  AnimationWorker.slideDown(v,0);
    v = filterView.findViewById(R.id.fab_done);
    AnimationWorker.slideUp(v,500);

        if (f == null) {
            if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Presets wird neu gebildet");
            fT.add(R.id.filter_smart_fragment_preset, Fragment.instantiate(this.getActivity(), Filter_Presets_Fragment.class.getName()), "pFragment").addToBackStack(null).commit();
        } else if (f.isHidden()) {
            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "Presets schon vorhanden" + f.isHidden() + " --" + f.isVisible() + "--" + f.isAdded());
            fT.show(f).addToBackStack(null).commit(); //replace(R.id.infoFragment, Fragment.instantiate(getInstance(), MiniInfoFragment.class.getName()), "iFragment");

        }

    //slideUp(filterView.findViewById(R.id.fab_done), 200);

    KartenActivity.BackstackEXIT=false;
    }




    public static void showDialog(int id){

    //        DialogFragment newFragment = ListDialogFragment.newInstance(id);
    //        newFragment.show(getFragmentManager(), "dialog");



        refresh_Adapter(id);

        switch (id) {
            case STECKER:
                SteckerListe = new String[Stecker.size()-L_Stecker.size()];
                boolean[] Stecker_C = new boolean[Stecker.size()-L_Stecker.size()];
                Iterator i = Stecker.iterator();

                int c =0;
                while (i.hasNext()) {
                    FilterEintrag F = (FilterEintrag) i.next();
                    if (!L_Stecker.contains(F.getTitel())) {
                        Stecker_C[c] = F.isSelected();
                        SteckerListe[c] = F.getTitel();
                        c++;

                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(KartenActivity.getInstance())
                        .setTitle(KartenActivity.getInstance().getResources().getString(R.string.filterSteckerTitel))
                        .setMultiChoiceItems(SteckerListe, Stecker_C, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                //Stecker. setSelected(SteckerListe[which],isChecked);
                                if (! isChecked == FilterWorks.liste_aendern(FilterWorks.F_STECKER,SteckerListe[which]) ){
                                    if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Fehler beim SteckerListe ändern "+isChecked+" nicht erreicht");
                                };
                            }
                        })
                        .setNeutralButton("Alle zurücksetzen", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //alle Stecker abwählen und schliessen
                                        FilterWorks.liste_aendern(FilterWorks.F_STECKER,FilterWorks.BELIEBIG);
                                        smartview();
                                        dialog.dismiss();

                                    }
                                }
                        )
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                smartview();
                                //used to dismiss the dialog upon user selection.
                                dialog.dismiss();
                            }
                        });
                AlertDialog ad = builder.create();

                ad.show();

                break;


            case KARTEN:
                KartenListe = new String[Karten.size()-L_Karten.size()+1];
                boolean[] Karten_C = new boolean[Karten.size()-L_Karten.size()+1];
                if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"KartenSize:"+Karten.size()+" LKarten:"+L_Karten.size());
                Iterator i1 = Karten.iterator(); int C =0;

                while (i1.hasNext()) {
                    FilterEintrag F = (FilterEintrag) i1.next();
                    if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"KartenListeSize:"+KartenListe.length+" C:"+C+" "+F.getTitel()+" "+L_Karten.contains(F.getTitel()));

                    if (!L_Karten.contains(F.getTitel())) {
                        Karten_C[C] = F.isSelected();
                        KartenListe[C] = F.getTitel();
                        C++;
                    }
                }
                AlertDialog.Builder builder1 = new AlertDialog.Builder(KartenActivity.getInstance())
                        .setTitle(KartenActivity.getInstance().getResources().getString(R.string.filterLadekartenTitel))
                        .setMultiChoiceItems(KartenListe, Karten_C, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                //Karten.put(KartenListe[which],isChecked);
                                if (! isChecked == FilterWorks.liste_aendern(FilterWorks.F_KARTEN,KartenListe[which]) ){
                                    LogWorker.d(LOG_TAG,"Fehler beim KartenListe ändern "+isChecked+" nicht erreicht");
                                };
                            }
                        })
                        .setNeutralButton("Alle zurücksetzen", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //alle Karten abwählen und schliessen
                                        FilterWorks.liste_aendern(FilterWorks.F_KARTEN,FilterWorks.BELIEBIG);
                                        smartview();
                                        dialog.dismiss();
                                    }
                                }
                        )
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                //used to dismiss the dialog upon user selection.
                                smartview();
                                dialog.dismiss();
                            }
                        });
                AlertDialog ad1 = builder1.create();

                ad1.show();


        }

    }



    private static void refresh_Adapter(int Liste){
        Iterator i;
        switch (Liste) {
            case STECKER:

                List<FilterEintrag> l = FilterWorks.ListeToArrayList(FilterWorks.F_STECKER);
                /**Stecker = new HashMap<String,Boolean>(l.size());
                Collections.sort(l);

                i = l.iterator();
                while (i.hasNext()){
                    FilterEintrag f = (FilterEintrag) i.next();
                    if (f != null && !f.getTitel().isEmpty())
                            Stecker.put(f.getTitel(),f.isSelected());
                }*/
                Stecker =l;
                break;

            case KARTEN:
                List<FilterEintrag> L = FilterWorks.ListeToArrayList(FilterWorks.F_KARTEN);
                Collections.sort(L);

               /* Karten = new HashMap<String,Boolean>(L.size());
                i = L.iterator();
                while (i.hasNext()){
                    FilterEintrag f = (FilterEintrag) i.next();
                    Karten.put(f.getTitel(),f.isSelected());
                }*/
               Karten = L;
                break;

        }
    }



    public static void hide_presets(){

        Fragment f = cFm.findFragmentByTag("pFragment");
        if (f !=null && f.isVisible()){


            View v = filterView.findViewById(R.id.card_plug);
            AnimationWorker.fadeIn(v,0,1.0f);
            v = filterView.findViewById(R.id.card_card);
            AnimationWorker.fadeIn(v,20,1.0f);
            v= filterView.findViewById(R.id.card_extras);
            AnimationWorker.fadeIn(v,400,1.0f);
             v = filterView.findViewById(R.id.fab_preset);
            AnimationWorker.slideUp(v,500);
            v = filterView.findViewById(R.id.fab_done);
            AnimationWorker.slideDown(v,0);


            cFm.beginTransaction().setCustomAnimations(R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out,
                    R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out).hide(f).commit();
            setPresetLabel();

        }
        ladeFilter();

    }


    private static void smartview(){
        //dynamische ANzeige von relevanten Filtern

        CheckBox c = (CheckBox) filterView.findViewById(R.id.filter_card_1);

        ArrayList<FilterEintrag> aL = FilterWorks.ListeToArrayList(FilterWorks.F_STECKER);
        Iterator i = aL.iterator();
        int l = 0;
        while (i.hasNext()){
            FilterEintrag fe = (FilterEintrag) i.next();
            if (fe.isSelected() && !L_Stecker.contains(fe.getTitel())){
                l++;
            }
        }

        TextView t = (TextView) filterView.findViewById(R.id.card_moreplugs);
        if (l>0){
            t.setText(l+KartenActivity.getInstance().getResources().getString(R.string.filter_smart_moreplugs));
            if(t.getAlpha()<1)AnimationWorker.fadeIn(t,0,1.0f);
        }
        else{
            AnimationWorker.fadeOut(t,0);
        }

        L_Karten = new HashSet<String>(Arrays.asList(KartenActivity.getInstance().getResources().getStringArray(R.array.cards_de_nord)));

        int[] cbid = {R.id.filter_card_1,R.id.filter_card_2,R.id.filter_card_3,R.id.filter_card_4,R.id.filter_card_5,R.id.filter_card_6,R.id.filter_card_7,R.id.filter_card_8};

        t= (TextView) filterView.findViewById(R.id.filter_show_all_cards);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(KARTEN);
            }
        });

        Iterator it = L_Karten.iterator();
        int in=0;
        while (it.hasNext()){
            c= (CheckBox) filterView.findViewById(cbid[in]);
            String K = (String) it.next();
            c.setText(K);
            c.setChecked(FilterWorks.lese_liste(FilterWorks.F_KARTEN,K));
            c.setTag(FilterWorks.F_KARTEN);
            c.setOnCheckedChangeListener(instance);
            in++;
        }


        aL = FilterWorks.ListeToArrayList(FilterWorks.F_KARTEN);
        i = aL.iterator();
        l = 0;
        while (i.hasNext()){
            FilterEintrag fe = (FilterEintrag) i.next();
            if (fe.isSelected() && !Arrays.asList(L_Karten).contains(fe.getTitel())){
                l++;
            }
        }

        t = (TextView) filterView.findViewById(R.id.card_morecards);

        if (l>0){
            t.setText(l+KartenActivity.getInstance().getResources().getString(R.string.filter_smart_morecards));
            if(t.getAlpha()<1)AnimationWorker.fadeIn(t,0,1.0f);
        }
        else{
            AnimationWorker.fadeOut(t,0);

        }


        c = (CheckBox) filterView.findViewById(R.id.fBarrierefrei);

        c.setChecked(FilterWorks.lese_filter(FilterWorks.F_BARRIEREFREI));
        //ergänze die Erläuterung zum Barrierefrei FIlter
        t = (TextView) filterView.findViewById(R.id.fBarrierefrei_hint);
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


        //Wenn nur Tesla SUpercharger, dann keine Ladekarten
        c = (CheckBox) filterView.findViewById(R.id.card_plug_tesla);
        if (c.isChecked()&& FilterWorks.listenlaenge(FilterWorks.F_STECKER)==1){
            AnimationWorker.fadeOut(filterView.findViewById(R.id.card_card),300);

            AnimationWorker.fadeOut(filterView.findViewById(R.id.filter_card_fastcharge),300);
        }else{
            //AnimationWorker.fadeOut(filterView.findViewById(R.id.card_extras),0);
            View v = filterView.findViewById(R.id.card_card);
            if(v.getAlpha()<1) {
                AnimationWorker.fadeIn(v,0,1.0f);
                AnimationWorker.fadeIn(filterView.findViewById(R.id.card_extras),800,1.0f);
            }

            v = filterView.findViewById(R.id.filter_card_fastcharge);
            if(v.getAlpha()<1)  AnimationWorker.fadeIn(v,0,1.0f);
            v = filterView.findViewById(R.id.card_extras);
            if(v.getAlpha()<1)  AnimationWorker.fadeIn(v,800,1.0f);

        }

        Switch sw = (Switch)filterView.findViewById(R.id.filter_card_fastcharge);

        c = (CheckBox) filterView.findViewById(R.id.card_plug_schuko);
        if (c.isChecked()){
            //Blende FastCharge aus bei Schuko (und deaktiviere)
            sw.setChecked(false);
            AnimationWorker.fadeOut(sw,0);
        }else
        {
            if(sw.getAlpha()<1)AnimationWorker.fadeIn(sw,0,1.0f);
        }

        if (sw.isChecked()){
            FilterWorks.setze_power(5);
        }else{

            if (FilterWorks.listenlaenge(FilterWorks.F_STECKER)>0&&!c.isChecked()){
                FilterWorks.setze_power(2);
            }  else{
                FilterWorks.setze_power(0);
            }
        }

        View v =filterView.findViewById(R.id.filter_smart_warning_verbund);

        if(FilterWorks.listenlaenge(FilterWorks.F_VERBUND)>0){
            AnimationWorker.fadeIn(v,0,1.0f);
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Verbund Warnung!!");
        }else{
            AnimationWorker.fadeOut(v,0);
        }


    }



}