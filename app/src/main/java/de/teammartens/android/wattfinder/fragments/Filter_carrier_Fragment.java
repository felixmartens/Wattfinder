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

public class Filter_carrier_Fragment extends Fragment {
    private static mFilterListAdapter dataAdapter = null;
    private static final String LOG_TAG = "FilterFragmentCarrier";
    private static final String LISTE= FilterWorks.F_VERBUND;

    public Filter_carrier_Fragment() {
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
        return inflater.inflate(R.layout.fragment_filter_carrier, container, false);
    }

    public void onStart(){
        super.onStart();
        final ListView listView = (ListView) this.getView().findViewById(R.id.filter_liste_carrier);

        final CheckBox cB = (CheckBox) this.getView().findViewById(R.id.fCarrier_all);
        cB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG," FilterListe "+LISTE+" BELIEBIG "+b );
                if(b)FilterWorks.setListeBeliebig(LISTE);
                if(dataAdapter!=null) {
                    dataAdapter.updatefilterListe(FilterWorks.ListeToArrayList(LISTE));
                    dataAdapter.notifyDataSetChanged();
                }
            }
        });

        cB.setChecked(FilterWorks.Liste_beliebig(LISTE));
        ArrayList<FilterEintrag> filterListe = new ArrayList<FilterEintrag>();
        filterListe = FilterWorks.ListeToArrayList(LISTE);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        dataAdapter = new mFilterListAdapter(this.getActivity(),
                R.layout.filterlisteneintrag, filterListe);
        dataAdapter.setListe(LISTE);
        dataAdapter.setTag("Filter_Verbund");
        dataAdapter.setcBeliebig(cB);
        listView.setAdapter(dataAdapter);
    }
    public void onResume(){
        super.onResume();


    }

    public static void ladeListe(){
        if(dataAdapter!=null) {
            dataAdapter.updatefilterListe(FilterWorks.ListeToArrayList(LISTE));
            dataAdapter.notifyDataSetChanged();
        }
    }
}
