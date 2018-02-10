package de.teammartens.android.wattfinder.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;

import java.util.ArrayList;

import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.fragments.FilterFragment;
import de.teammartens.android.wattfinder.fragments.Filter_Presets_Fragment;
import de.teammartens.android.wattfinder.fragments.SmartFilterFragment;
import de.teammartens.android.wattfinder.worker.AnimationWorker;
import de.teammartens.android.wattfinder.worker.FilterWorks;
import de.teammartens.android.wattfinder.worker.LogWorker;

import static de.teammartens.android.wattfinder.fragments.Filter_Presets_Fragment.showEditDialog;

/**
 * Created by felix on 14.02.17.
 */

public class PresetAdapter extends ArrayAdapter<PresetEintrag> {
    private static final String LOG_TAG="PRESET_ADAPTER";
    private ArrayList<PresetEintrag> filterListe;
    private Context mContext;


    public PresetAdapter(Context context, int textViewResourceId,
                         ArrayList<PresetEintrag> filterListe) {
        super(context, textViewResourceId, filterListe);
        this.filterListe = new ArrayList<PresetEintrag>();

        this.filterListe.addAll(filterListe);
        mContext=context;
    }

    public void updateListe(ArrayList<PresetEintrag> filterListe){
        this.filterListe = new ArrayList<PresetEintrag>();
        this.filterListe.addAll(filterListe);
        notifyDataSetChanged();
    }

    private class ViewHolder {


        RadioButton presetname;
        ImageView presetedit;

    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        //Log.v("ConvertView", String.valueOf(position));
        if(position<filterListe.size()) {
            final PresetEintrag filterEintrag = filterListe.get(position);

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);

                convertView = vi.inflate(R.layout.filterlisteneintrag_preset, null);

                holder = new ViewHolder();
                holder.presetedit = (ImageView) convertView.findViewById(R.id.preset_action_edit);
                holder.presetname = (RadioButton) convertView.findViewById(R.id.preset_checkBox1);

                holder.presetname.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"ItemClick: "+filterEintrag.getTitel());
                        if(!filterEintrag.isSelected())filterEintrag.setSelected(true);
                        FilterWorks.loadPresets(filterEintrag.getTitel());
                        Filter_Presets_Fragment.ladeListe();
                        //FilterFragment.updatePagerChild();
                        //direkt schliessen statt done-button
                        if(AnimationWorker.smartFilter)SmartFilterFragment.hide_presets();
                        else FilterFragment.hide_presets();
                    }
                });

                holder.presetedit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String P = (String) v.getTag();
                        showEditDialog((String) v.getTag(), false);

                    /*if ( LogWorker.isVERBOSE()) LogWorker.d (LOG_TAG,"Versuche "+P+"zu löschen");
                    FilterWorks.clearPreset(P);*/
                        //ladeListe();
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            holder.presetname.setText(filterEintrag.getTitel());
            holder.presetname.setChecked(filterEintrag.isSelected());

            if (filterEintrag.getTitel().equals(mContext.getString(R.string.filter_standardprofil))) {
                holder.presetedit.setVisibility(View.GONE);
            } else {

                holder.presetedit.setVisibility(View.VISIBLE);
            }
            holder.presetedit.setTag(filterEintrag.getTitel());
            holder.presetname.setTag(filterEintrag.getTitel());


        }
        return convertView;
    }



}

/*
View V = (View) v.getParent();
                    RadioButton T = (RadioButton) v;
                    if (T.isChecked())
                    {if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG, "Button checked ");
                        //erstmal geöffnete EditTexte wieder schliessen
                        renameProfil(true);

                        EditText e = (EditText) V.findViewById(R.id.preset_value);

                        for ( PresetEintrag Fe : presetAdapter.filterListe) {

                            if (!v.getTag().equals(Fe.getTitel())){ Fe.setSelected(false); Fe.setEditable(false);}
                            else {Fe.setSelected(true);Fe.setEditable(true);
                                if(Fe.getTitel().equals(KartenActivity.getInstance().getString(R.string.filter_neuesprofil)))
                                    Fe.setTitel("");}

                            presetAdapter.notifyDataSetChanged();}

                        if (!v.getTag().toString().equals(KartenActivity.getInstance().getString(R.string.filter_neuesprofil)))
                            FilterWorks.loadPresets(v.getTag().toString());


                        e.setTag(v.getTag());
                        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"vTag: "+v.getTag());


                        e.requestFocus();
                        e.requestFocus();
                        current = e;
                           /* e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if (!hasFocus) {

                                        EditText E = (EditText) v;
                                        View V = (View) v.getParent();
                                        if (LogWorker.isVERBOSE())
                                            LogWorker.d(LOG_TAG, "Focus changed " + E.getText());
                                        RadioButton rB = (RadioButton) V.findViewById(R.id.preset_checkBox1);
                                        for (PresetEintrag Fe : presetAdapter.filterListe) {
                                            if (Fe.isEditable() && rB.getTag().equals(Fe.getTitel())) {
                                                if (LogWorker.isVERBOSE())
                                                    LogWorker.d(LOG_TAG, "FilterEintrag Update neuer Titel " + Fe.getTitel() + " : " + E.getText());
                                                Fe.setNeuertitel(E.getText().toString());
                                            }
                                        }
                                    }
                                }
                            });*/


/*}
 */