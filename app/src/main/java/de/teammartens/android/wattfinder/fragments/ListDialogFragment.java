package de.teammartens.android.wattfinder.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Iterator;

import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.model.FilterEintrag;
import de.teammartens.android.wattfinder.worker.FilterWorks;

/**
 * Created by felix on 31.01.18.
 */

public class ListDialogFragment extends DialogFragment {



    private static final int STECKER = 0;
    private static final int KARTEN = 1;
    private static HashMap<String,Boolean> Stecker, Karten;
    private static String[] SteckerListe, KartenListe;

    public static ListDialogFragment newInstance(int id) {
        ListDialogFragment frag = new ListDialogFragment();
        Bundle args = new Bundle();

            args.putInt("id",id);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int id = getArguments().getInt("id");
        String title = "";
        Iterator i;
        switch (id) {
            case STECKER:
                i = FilterWorks.ListeToArrayList(FilterWorks.F_STECKER).iterator();
                while (i.hasNext()) {
                    FilterEintrag f = (FilterEintrag) i.next();
                    Stecker.put(f.getTitel(), f.isSelected());
                }
                title = getActivity().getResources().getString(R.string.filterSteckerTitel);
                break;
            case KARTEN:
                i = FilterWorks.ListeToArrayList(FilterWorks.F_KARTEN).iterator();
                while (i.hasNext()) {
                    FilterEintrag f = (FilterEintrag) i.next();
                    Karten.put(f.getTitel(), f.isSelected());
                }
                title = getActivity().getResources().getString(R.string.filterLadekartenTitel);

                break;

        }

        return new AlertDialog.Builder(getActivity())
                //.setIcon(R.drawable.alert_dialog_icon)
                .setTitle(title)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }
                )
                .setNegativeButton("Zurücksetzen",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String Liste = "";
                                if (id==STECKER)Liste=FilterWorks.F_STECKER;else Liste=FilterWorks.F_KARTEN;
                                FilterWorks.liste_aendern(Liste,FilterWorks.BELIEBIG);
                            }
                        }
                )
                .create();
    }




    protected void onPrepareDialog(int id, Dialog dialog) {
// TODO Auto-generated method stu
// checkBoxen noch aus den FIltern abrufen

        switch (id) {
            case STECKER:
                //refresh_Adapter(STECKER);
                AlertDialog prepare_checkbox_dialog = (AlertDialog) dialog;
                ListView list_checkbox = prepare_checkbox_dialog.getListView();
                for (int i = 0; i < list_checkbox.getCount(); i++) {

                    list_checkbox.setItemChecked(i, Stecker.get(SteckerListe[i]));
                }
                break;
            case KARTEN:
              //  refresh_Adapter(KARTEN);
                AlertDialog prepare_karten_dialog = (AlertDialog) dialog;
                ListView list_karten = prepare_karten_dialog.getListView();
                for (int i = 0; i < list_karten.getCount(); i++) {
                    // t = (TextView) filterView.findViewById(R.id.card_morecards);
                    list_karten.setItemChecked(i, Karten.get(KartenListe[i]));
                }
                break;

        }
    }
}
