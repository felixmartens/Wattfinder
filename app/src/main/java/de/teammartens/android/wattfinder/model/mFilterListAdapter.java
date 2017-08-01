package de.teammartens.android.wattfinder.model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import de.teammartens.android.wattfinder.worker.FilterWorks;
import de.teammartens.android.wattfinder.worker.LogWorker;
import de.teammartens.android.wattfinder.worker.SaeulenWorks;
import de.teammartens.android.wattfinder.R;

/**
 * Created by felix on 30.10.16.
 */

public class mFilterListAdapter extends ArrayAdapter<FilterEintrag> {

    private ArrayList<FilterEintrag> filterListe =new ArrayList<FilterEintrag>();
    private  String LISTE = new String();
    private String TAG = new String ();

    public void setcBeliebig(CheckBox cBeliebig) {
        this.cBeliebig = cBeliebig;
    }

    private CheckBox cBeliebig;


    public mFilterListAdapter(Context context, int textViewResourceId,
                              ArrayList<FilterEintrag> filterListe) {
        super(context, textViewResourceId, filterListe);
        this.filterListe = new ArrayList<FilterEintrag>();
        this.filterListe.addAll(filterListe);
        Log.v(TAG+"(Inctance)","neue Filterliste für"+LISTE+" mit "+this.filterListe.size()+" Einträgen");
    }

    private class ViewHolder {
        TextView code;
        CheckBox name;

    }

    public  void updatefilterListe(ArrayList<FilterEintrag> mfilterListe){
       if(filterListe==null){
           filterListe = new ArrayList<FilterEintrag>();
       }
        filterListe.clear();
        filterListe.addAll(mfilterListe);
        notifyDataSetChanged();
        Log.v(TAG+"update","neue Filterliste für"+LISTE+" mit "+this.filterListe.size()+" Einträgen");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        mFilterListAdapter.ViewHolder holder = null;

        FilterEintrag filterEintrag = this.filterListe.get(position);
        //Log.v(TAG+"ConvertView", String.valueOf(position)+"/"+this.filterListe.size()+"-"+filterEintrag.getTitel());

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            convertView = vi.inflate(R.layout.filterlisteneintrag, null);




            holder = new mFilterListAdapter.ViewHolder();

            holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
            holder.name.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;

                    if(LogWorker.isVERBOSE())LogWorker.d("FilterAdapter"+TAG,"onClick:"+cb.getText()+ " ändern zu"+cb.isChecked());
                    // Return wird dann gleich in Anzeige gesetzt - > direkte Rückmeldung obs geklappt hat
                    cb.setChecked(FilterWorks.liste_aendern(LISTE, (String) cb.getText()));
            if (cBeliebig!=null) cBeliebig.setChecked(FilterWorks.Liste_beliebig(LISTE));
                    SaeulenWorks.checkMarkerCache(TAG);
                }
            });
            holder.name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                    if(LogWorker.isVERBOSE())LogWorker.d("FilterAdapter"+TAG,"onCheckedChange:"+buttonView.getText()+ " ändern zu"+isChecked);
                    // Return wird dann gleich in Anzeige gesetzt - > direkte Rückmeldung obs geklappt hat
                   /* buttonView.setChecked(FilterWorks.liste_aendern(LISTE, (String) buttonView.getText()));
                    if (cBeliebig!=null) cBeliebig.setChecked(FilterWorks.Liste_beliebig(LISTE));
                    SaeulenWorks.checkMarkerCache(TAG);*/
                }
            });


            convertView.setTag(holder);
        }
        else {
            holder = (mFilterListAdapter.ViewHolder) convertView.getTag();
        }



        holder.name.setText(filterEintrag.getTitel());
        holder.name.setChecked(filterEintrag.isSelected());



        return convertView;

    }

    public void setTag(String tag){
        TAG = tag;
    }

    public void setListe(String liste){
        LISTE = liste;
    }

}
