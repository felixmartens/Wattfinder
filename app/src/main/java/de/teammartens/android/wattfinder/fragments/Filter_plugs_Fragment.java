package de.teammartens.android.wattfinder.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.ArrayList;

import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.model.FilterEintrag;
import de.teammartens.android.wattfinder.model.mFilterListAdapter;
import de.teammartens.android.wattfinder.worker.FilterWorks;
import de.teammartens.android.wattfinder.worker.LogWorker;

/**
 * Created by felix on 30.10.16.
 */

public class Filter_plugs_Fragment extends Fragment {

    private static mFilterListAdapter dataAdapter = null;
    private static final String LOG_TAG = "FilterFragmentPlugs";
    private static final String LISTE= FilterWorks.F_STECKER;

    public Filter_plugs_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter_plugs, container, false);
    }



public void onStart(){
    super.onStart();
    ListView listView = (ListView) this.getView().findViewById(R.id.filter_liste_plugs);


    ArrayList<FilterEintrag> filterListe = new ArrayList<FilterEintrag>();

    //filterListe.add(new FilterEintrag("beliebige Karten",FilterWorks.BELIEBIG,FilterWorks.Liste_beliebig(LISTE)));
    final CheckBox cB = (CheckBox) this.getView().findViewById(R.id.fPlugs_all);
    cB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG," FilterListe "+LISTE+" BELIEBIG "+b );
            if(b)FilterWorks.setListeBeliebig(LISTE);
            ladeListe();
        }
    });

    cB.setChecked(FilterWorks.Liste_beliebig(LISTE));

    filterListe = new ArrayList<>(FilterWorks.ListeToArrayList(LISTE));
    if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG," FilterListe "+LISTE+" enthält "+filterListe.size()+" Objekte." );
    listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    dataAdapter = new mFilterListAdapter(this.getContext(),
            R.layout.filterlisteneintrag, FilterWorks.ListeToArrayList(LISTE));
    dataAdapter.setListe(LISTE);
    dataAdapter.setTag("Filter_Stecker");
    dataAdapter.setcBeliebig(cB);
    listView.setAdapter(dataAdapter);
    if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG," FilterListeAdapter "+LISTE+" enthält "+dataAdapter.getCount()+" Objekte." );
}


    public void onResume(){

        //dataAdapter.updatefilterListe(FilterWorks.ListeToArrayList(LISTE));
        //dataAdapter.notifyDataSetChanged();
        super.onResume();


    }

    public static void ladeListe(){
        if(dataAdapter!=null) {
            dataAdapter.updatefilterListe(FilterWorks.ListeToArrayList(LISTE));
            dataAdapter.notifyDataSetChanged();
        }
    }
}
