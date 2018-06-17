package de.teammartens.android.wattfinder.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.model.Saeule;
import de.teammartens.android.wattfinder.worker.AnimationWorker;
import de.teammartens.android.wattfinder.worker.GeoWorks;
import de.teammartens.android.wattfinder.worker.SaeulenWorks;

/**
 * Created by felix on 10.11.14.
 */
public class MiniInfoFragment extends Fragment {
        private  final String LOG_TAG = "InfoFragment";
        private  View infoView;
        public  LatLng mPos= new LatLng(0,0);
        private  Integer mID = 0;
        public  String mTitel = "";
        private  String mUrl = "";
        private  Saeule mSaeule;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

             infoView = inflater.inflate(R.layout.fragment_infoview, container, false);

            // Initialize the Click Listener for opening Details
            infoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnimationWorker.show_details(mSaeule);
                }
            });


            resetValues();
            return infoView;
        }

    public void onStart(){
        super.onStart();
        if (infoView != null) {
            TextView t = (TextView) infoView.findViewById(R.id.iSaeulenid);
            if (t != null && !t.getText().equals(mID))
                holeInfo();
            View v = infoView.findViewById(R.id.fab_directions);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + mPos.latitude + "," + mPos.longitude + "?q=" + mPos.latitude + "," + mPos.longitude + "(" + mTitel + ")"));
                    if (intent.resolveActivity(KartenActivity.getInstance().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
            v = infoView.findViewById(R.id.create_chargeevent);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChargeeventDialog Dialog = new ChargeeventDialog();
                    Dialog.show(KartenActivity.fragmentManager,"ChargeEvent");

                }
            });


        }
    }


    public void onResume(){
        super.onResume();
        KartenActivity.setMapPaddingY(infoView.getHeight());
        if(SaeulenWorks.getCurrentSaeule()!=null) {
            setzeSaeule(SaeulenWorks.getCurrentSaeule());
        }else{
            AnimationWorker.hide_info();
        }

    }
public void onPause(){
    super.onPause();

}

    private  void holeInfo() {

        if (infoView != null && mSaeule != null) {
            TextView t2 = (TextView) infoView.findViewById(R.id.iName);
           if(t2!=null){
               t2.setText(mSaeule.getName());
               t2.setVisibility(View.VISIBLE);

           }
            else return;
            mTitel = "Ladepunkt: " + mSaeule.getName();

            t2 = (TextView) infoView.findViewById(R.id.iEvCount);
            int evc = mSaeule.getEventCount();
            t2.setText((evc>0?evc:"Keine")+ " Bewertung"+(evc>1?"en":""));
            if (evc<0)t2.setVisibility(View.INVISIBLE);

            t2 = (TextView) infoView.findViewById(R.id.iAdresse);
            t2.setText(mSaeule.getAddress());
            mTitel = mTitel + ", " + mSaeule.getAddress();
            t2 = (TextView) infoView.findViewById(R.id.iAnschluesse);
            t2.setText(mSaeule.getChargepoints());
            t2 = (TextView) infoView.findViewById(R.id.iEntfernung);
            t2.setVisibility(View.GONE);
            if (GeoWorks.getSuchPosition()!= null && GeoWorks.isAround(GeoWorks.getSuchPosition()))
            {
                t2.setText(KartenActivity.getInstance().getResources().getString(R.string.infoEntfernungZiel)+" "+GeoWorks.distanceToString(mPos,GeoWorks.getSuchPosition()));
                t2.setVisibility(View.VISIBLE);
            }else
            {
                if (GeoWorks.isAround(GeoWorks.getmyPosition())){
                    t2.setText(KartenActivity.getInstance().getResources().getString(R.string.infoEntfernungPos)+" "+GeoWorks.distanceToString(mPos,GeoWorks.getmyPosition()));
                    t2.setVisibility(View.VISIBLE);
                }
            }

            t2 = (TextView) infoView.findViewById(R.id.iUpdated);
            t2.setText(KartenActivity.getInstance().getString(R.string.infoUpdated)+mSaeule.getUpdatedString());
            View v = (View) infoView.findViewById(R.id.icard_fault);
            if(mSaeule.isFaultreport()) v.setVisibility(View.VISIBLE); else v.setVisibility(View.GONE);

            v = infoView.findViewById(R.id.loadingPanel);
            v.setVisibility(View.GONE);

           // float f = (infoView.getHeight()/KartenActivity.getDisplayH())*1.0f;
           // if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"hole Info "+ (mSaeule!=null?mSaeule.getName():"") +" H:"+infoView.getHeight() +" f:"+f);
            KartenActivity.setMapPaddingY(infoView.getHeight());
           if (AnimationWorker.isDetailsVisibile())
                AnimationWorker.show_details(mSaeule);

        }
    }

        public  void resetValues(){
            TextView t = (TextView) infoView.findViewById(R.id.iSaeulenid);
            t.setText("");
            t = (TextView) infoView.findViewById(R.id.iAdresse);t.setText("");
            t = (TextView) infoView.findViewById(R.id.iEntfernung);t.setText("");
            t = (TextView) infoView.findViewById(R.id.iAnschluesse);t.setText("");
            t = (TextView) infoView.findViewById(R.id.iUpdated);t.setText("");
        }



    public  void setzeSaeule(Saeule S){
            if(S!=null) {
                mID = S.getID();
                mPos = S.getPosition();
                mTitel = S.getName();

                mSaeule = S;
                holeInfo();
            }



    }









    }

