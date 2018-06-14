package de.teammartens.android.wattfinder.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.model.ChargeEvent;
import de.teammartens.android.wattfinder.model.FilterEintrag;
import de.teammartens.android.wattfinder.worker.FilterWorks;
import de.teammartens.android.wattfinder.worker.Utils;

import static de.teammartens.android.wattfinder.worker.Utils.generateViewId;
import static de.teammartens.android.wattfinder.worker.Utils.getResId;

/**
 * Created by felix on 31.01.18.
 */

public class ChargeeventDialog extends DialogFragment {

    private final String title = "ChargeEvent/CheckIn";
    private final String sP_nickname = "ChargeEvent_Nickname";
    private final String sP_plug = "ChargeEvent_Plug";
    private View myView;
    private KartenActivity instance;
    private ChargeEvent event = new ChargeEvent();


    public static ChargeeventDialog newInstance(int id) {
        ChargeeventDialog frag = new ChargeeventDialog();
        Bundle args = new Bundle();

            args.putInt("id",id);
        frag.setArguments(args);
        return frag;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.dialog_chargeevent, container, false);
        getDialog().setTitle(title);
        TextView tv = myView.findViewById(R.id.ce_userid);
        String s = KartenActivity.getCEuID().toString();
        tv.setText(s);
        instance=KartenActivity.getInstance();
        populateRadioGroup(R.id.ce_plug,R.array.ce_plugs,R.array.ce_plug_values);

        RadioGroup rg = myView.findViewById(R.id.ce_plug);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                event.setPlug(group.findViewById(checkedId).getTag().toString());
                TextView tv = myView.findViewById(R.id.ce_plug_text);
                tv.setText(instance.getString(R.string.filterSteckerTitel)+event.getPlug());
                group.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
                tv = myView.findViewById(R.id.ce_comment);
                tv.setText(event.getComment());
                tv.setVisibility(View.VISIBLE);
                SharedPreferences sharedPreferences = instance.getPreferences(Context.MODE_PRIVATE);
                sharedPreferences.edit().putString(sP_plug,event.getPlug()).commit();
            }
        });

        tv = myView.findViewById(R.id.ce_plug_text);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View rv = myView.findViewById(R.id.ce_plug);
                v.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                }
        });


        rg = myView.findViewById(R.id.ce_result);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                event.setReason(Integer.parseInt(group.findViewById(checkedId).getTag().toString()));
                TextView tv = myView.findViewById(R.id.ce_result_text);
                tv.setText(instance.getString(R.string.ce_result_title)+event.getReasonString());
                group.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
                if(event.getReason()>10)
                    show_result_option();
                else
                    if(!event.getPlug().trim().isEmpty()) myView.findViewById(R.id.ce_comment).setVisibility(View.VISIBLE);
                    else myView.findViewById(R.id.ce_plug).setVisibility(View.VISIBLE);
            }
        });

        rg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // das sollte nur aufgerufen werden wenn keine Änderung war
                v.setVisibility(View.GONE);
                TextView tv = myView.findViewById(R.id.ce_result_text);
                tv.setVisibility(View.VISIBLE);
            }
        });


        tv = myView.findViewById(R.id.ce_result_text);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                //show_result_option();
                RadioGroup rg = myView.findViewById(R.id.ce_result);
                rg.setVisibility(View.VISIBLE);
            }
        });

        rg = myView.findViewById(R.id.ce_result_option);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                event.setReason(Integer.parseInt(group.findViewById(checkedId).getTag().toString()));
                TextView tv = myView.findViewById(R.id.ce_result_text);
                tv.setText(instance.getString(R.string.ce_result_title)+event.getReasonString());
                group.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
            }
        });


        View.OnClickListener RadioViewListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View V = myView.findViewById(R.id.ce_result_success);
                Integer defC = instance.getResources().getColor(R.color.colorCard);
                V.setBackgroundColor(defC);
                V = myView.findViewById(R.id.ce_result_error);
                V.setBackgroundColor(defC);
                V = myView.findViewById(R.id.ce_result_others);
                V.setBackgroundColor(defC);
                defC = instance.getResources().getColor(R.color.colorAccent);
                v.setBackgroundColor(defC);
                event.setReason(Integer.parseInt(v.getTag().toString()));

                show_result_option();
            }
        };

        tv = myView.findViewById(R.id.ce_result_success);
        tv.setOnClickListener(RadioViewListener);
        tv = myView.findViewById(R.id.ce_result_error);
        tv.setOnClickListener(RadioViewListener);
        tv = myView.findViewById(R.id.ce_result_others);
        tv.setOnClickListener(RadioViewListener);


        tv = myView.findViewById(R.id.ce_nickname);
        tv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    TextView tv = (TextView) v;
                    event.setNickname(tv.getText().toString());
                    SharedPreferences sharedPreferences = instance.getPreferences(Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString(sP_nickname,event.getNickname()).commit();
                }
            }
        });


        event = new ChargeEvent();
        SharedPreferences sharedPreferences = instance.getPreferences(Context.MODE_PRIVATE);
        event.setNickname(sharedPreferences.getString(sP_nickname,""));

        tv = myView.findViewById(R.id.ce_nickname);
        if(event.getNickname().trim().isEmpty()){
            tv.setText(event.getNickname());
        }

        tv = myView.findViewById(R.id.ce_date);
        tv.setText(event.getTimestampString());

        tv = myView.findViewById(R.id.ce_result_success);
        tv.performClick();

        event.setPlug(sharedPreferences.getString(sP_plug,""));

        if(!event.getPlug().trim().isEmpty()) {
            rg = myView.findViewById(R.id.ce_plug);
            if (rg != null) {
                for (int i = 0; i < rg.getChildCount(); i++) {
                    View rb = rg.getChildAt(i);
                    if (rb.getTag().equals(event.getPlug())) {
                        RadioButton radioButton = (RadioButton) rb;
                        radioButton.setChecked(true);
                    }
                }
            }
        }

        return myView;
    }

    private void show_result_option(){
            // wenn nicht success dann zeige Auswahl der Begründung
            RadioGroup rg = myView.findViewById(R.id.ce_result_option);

            if(event.getReason()>99) {
                if (event.getReason() > 199)
                    populateRadioGroup(rg, R.array.ce_result_others,R.array.ce_result_others_values);
                else
                    populateRadioGroup(rg,R.array.ce_result_error,R.array.ce_result_error_values);
                rg.setVisibility(View.VISIBLE);
            }else rg.setVisibility(View.GONE);

    }




    protected void onPrepareDialog(int id, Dialog dialog) {

    }

    private void populateRadioGroup(Integer rgId,Integer textArray){
        populateRadioGroup(rgId,textArray,-1);
    }


    private void populateRadioGroup(Integer rgId,Integer textArray,Integer tagArray){
        RadioGroup rg = (RadioGroup) myView.findViewById(rgId);
        populateRadioGroup(rg,textArray,tagArray);
    }

    private void populateRadioGroup(RadioGroup rg,Integer textArray,Integer tagArray){
        rg.removeAllViews();
        RadioButton rb = new RadioButton(this.getContext());
        String[] PlugList = instance.getResources().getStringArray(textArray);
        String[] PlugValueList = new String[12];
        if(tagArray>0)PlugValueList = instance.getResources().getStringArray(tagArray);

        for (int i = 0; i < PlugList.length; i++) {
            rb = new RadioButton(this.getContext());
            rb.setId(generateViewId());
            rb.setText((String) PlugList[i]);
            if(tagArray>0)rb.setTag((String) PlugValueList[i]);
            rg.addView(rb,i);
        }
    }

}
