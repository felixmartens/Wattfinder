package de.teammartens.android.wattfinder.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.Inflater;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.model.PresetAdapter;
import de.teammartens.android.wattfinder.model.PresetEintrag;
import de.teammartens.android.wattfinder.worker.FilterWorks;
import de.teammartens.android.wattfinder.worker.LogWorker;

/**
 * Created by felix on 30.10.16.
 */

public class Filter_Presets_Fragment extends Fragment {
    private final static String LOG_TAG = "Fragment_Presets";
    private static View filterView;
    private static PresetAdapter presetAdapter = null;
    private static Context mContext;

    private static LayoutInflater mInflater;

    public Filter_Presets_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        filterView = inflater.inflate(R.layout.fragment_filter_presets, container, false);
        mContext = getContext();
        mInflater = inflater;



        setupAdapter();
        return filterView;
    }

    public void onResume() {
        super.onResume();
    ladeListe();
        mContext = getContext();
       /* View v = (View) filterView.findViewById(R.id.fab_done);

    if(v!=null)
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               FilterFragment.hide_presets();

            }
        });
*/
       View v = (View) filterView.findViewById(R.id.preset_button_add);

        if(v!=null)
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showEditDialog("",true);

                }
            });

        ;
        setPreset();

    }

    public void onPause(){
        super.onPause();


    }

    private void hide_presets(){
        FragmentManager fM = getChildFragmentManager();
        Fragment f = fM.findFragmentByTag("pFragment");
        if (f !=null && f.isVisible()){
            fM.beginTransaction().setCustomAnimations(R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out,
                    R.anim.fragment_slide_in,
                    R.anim.fragment_slide_out).hide(f).commit();

        }

    }

    public static void setPreset(){
        ((TextView)filterView.findViewById(R.id.fPreset)).setText(KartenActivity.getInstance().getString(R.string.filterPreset) + " " + FilterWorks.PRESET);
    }






    public static void renameProfil(String cTitel, String cnTitel, Boolean reload) {
        if (presetAdapter != null) {
            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "Current EditText " + cnTitel + "-zuvor:-" + cTitel);
            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "FilterEintrag v2 !!Rename! " + cTitel + " : " + cnTitel);
            FilterWorks.renamePreset(cTitel, cnTitel);

            //presetAdapter.notifyDataSetChanged();
            if (reload) ladeListe();

        }
    }


    public static void ladeListe() {
        ArrayList<PresetEintrag> filterListe = new ArrayList<PresetEintrag>();
        filterListe = FilterWorks.PresetArrayList();
        if (presetAdapter != null)presetAdapter.updateListe(filterListe);
    }

    private static void setupAdapter(){

        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "setupPager");
        ArrayList<PresetEintrag> filterListe = new ArrayList<PresetEintrag>();
        final ListView listView = (ListView) filterView.findViewById(R.id.filter_liste_presets);
        filterListe = FilterWorks.PresetArrayList();
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        presetAdapter = new PresetAdapter(mContext,
                R.layout.filterlisteneintrag_preset, filterListe);
        listView.setAdapter(presetAdapter);

    }


    public static void showEditDialog(final String Preset,boolean neu ) {


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        View dialogView  = mInflater.inflate(R.layout.editpresetdialog, null);
        dialogBuilder.setView(dialogView);
        final EditText edt = (EditText) dialogView.findViewById(R.id.editpreset1);


        dialogBuilder.setTitle(mContext.getString(R.string.preset_edit));
        dialogBuilder.setMessage(mContext.getString(R.string.preset_rename_msg));
        if(neu||Preset.trim().isEmpty()){//neues Profil erstellen
            edt.setHint(mContext.getString(R.string.filter_neuesprofil_hint));
        dialogBuilder.setPositiveButton(mContext.getString(R.string.dialog_create), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                FilterWorks.renamePreset("",edt.getText().toString());
                //ladeListe();
                setupAdapter();
            }
        });}else
        {
            //eigentlicher Edit Dialog
            edt.setText(Preset);
            dialogBuilder.setPositiveButton(mContext.getString(R.string.dialog_change), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    renameProfil(Preset,edt.getText().toString(),true);
                }
            });
            dialogBuilder.setNeutralButton(mContext.getString(R.string.dialog_delete), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Delete Preset "+Preset);
                    FilterWorks.clearPreset(Preset);
                    //ladeListe();
                    setupAdapter();
                }
            });
        }

        dialogBuilder.setNegativeButton(mContext.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ladeListe();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


}





