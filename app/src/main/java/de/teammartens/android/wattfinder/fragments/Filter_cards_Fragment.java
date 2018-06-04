package de.teammartens.android.wattfinder.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
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

public class Filter_cards_Fragment extends Fragment {
    private static mFilterListAdapter CardAdapter = null;
    private static final String LOG_TAG = "FilterFragmentCards";
    private static final String LISTE= FilterWorks.F_KARTEN;

    public Filter_cards_Fragment() {
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
        return inflater.inflate(R.layout.fragment_filter_cards, container, false);
    }




public void onStart(){
    super.onStart();
    ArrayList<FilterEintrag> filterListe = new ArrayList<FilterEintrag>();
    ListView listView = (ListView) this.getView().findViewById(R.id.filter_liste_cards);

    final CheckBox cB = (CheckBox) this.getView().findViewById(R.id.fCards_all);
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
    //if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG," FilterListe "+LISTE+" enthält "+filterListe.size()+" Objekte." );
    listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    CardAdapter = new mFilterListAdapter(this.getContext(),
            R.layout.filterlisteneintrag, filterListe);
    CardAdapter.setListe(LISTE);
    CardAdapter.setTag("Filter_Karten");
    CardAdapter.setcBeliebig(cB);
    listView.setAdapter(CardAdapter);

    //if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG," FilterListeAdapter "+LISTE+" enthält "+CardAdapter.getCount()+" Objekte." );
}


    public void onResume() {

        //CardAdapter.updatefilterListe(FilterWorks.ListeToArrayList(LISTE));
        //CardAdapter.notifyDataSetChanged();

        super.onResume();
    }

    public static void ladeListe(){
        if(CardAdapter!=null) {
            CardAdapter.updatefilterListe(FilterWorks.ListeToArrayList(LISTE));
            CardAdapter.notifyDataSetChanged();
        }
    }

}
