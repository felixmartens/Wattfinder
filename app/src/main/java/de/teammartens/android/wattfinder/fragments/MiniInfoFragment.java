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

/**
 * Created by felix on 10.11.14.
 */
public class MiniInfoFragment extends Fragment {
        private static final String LOG_TAG = "InfoFragment";
        private static View infoView;
        public static LatLng mPos= new LatLng(0,0);
        private static Integer mID = 0;
        public static String mTitel = "";
        private static String mUrl = "";
        private static Saeule mSaeule;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

             infoView = inflater.inflate(R.layout.fragment_infoview, container, false);

            // Initialize the Click Listener for opening Details
            infoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    DetailsFragment.setzeSaeule(mID,mPos,mTitel);
                    KartenActivity.toggleDetails();

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
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + MiniInfoFragment.mPos.latitude + "," + MiniInfoFragment.mPos.longitude + "?q=" + MiniInfoFragment.mPos.latitude + "," + MiniInfoFragment.mPos.longitude + "(" + MiniInfoFragment.mTitel + ")"));
                    if (intent.resolveActivity(KartenActivity.getInstance().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });


        }
    }


    public void onResume(){
        super.onResume();
        KartenActivity.setMapPaddingY(infoView.getHeight());


    }
public void onPause(){
    super.onPause();

}

    private static void holeInfo() {

        if (infoView != null) {
            TextView t2 = (TextView) infoView.findViewById(R.id.iName);
            t2.setText(mSaeule.getName());
            mTitel = "Ladepunkt: " + mSaeule.getName();


            t2 = (TextView) infoView.findViewById(R.id.iAdresse);
            t2.setText(mSaeule.getAddress());
            mTitel = mTitel + ", " + mSaeule.getAddress();
            t2 = (TextView) infoView.findViewById(R.id.iAnschluesse);
            t2.setText(mSaeule.getChargepoints());
            t2 = (TextView) infoView.findViewById(R.id.iUpdated);
            t2.setText(KartenActivity.getInstance().getString(R.string.infoUpdated)+mSaeule.getUpdatedString());
            t2 = (TextView) infoView.findViewById(R.id.ifault_report);
            if(mSaeule.isFaultreport()) t2.setVisibility(View.VISIBLE); else t2.setVisibility(View.GONE);

            View v = infoView.findViewById(R.id.loadingPanel);
            v.setVisibility(View.GONE);

           // float f = (infoView.getHeight()/KartenActivity.getDisplayH())*1.0f;
           // if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"hole Info "+ (mSaeule!=null?mSaeule.getName():"") +" H:"+infoView.getHeight() +" f:"+f);
            KartenActivity.setMapPaddingY(infoView.getHeight());
           if (KartenActivity.isDetailsVisibile())
                DetailsFragment.setzeSaeule(mID, mPos, mTitel);


        }
    }

        public static void resetValues(){
            TextView t = (TextView) infoView.findViewById(R.id.iSaeulenid);
            t.setText("");
            t = (TextView) infoView.findViewById(R.id.iAdresse);t.setText("");
            t = (TextView) infoView.findViewById(R.id.iVerbundPreis);t.setText("");
            t = (TextView) infoView.findViewById(R.id.iAnschluesse);t.setText("");
            t = (TextView) infoView.findViewById(R.id.iUpdated);t.setText("");
        }



    public static void setzeSaeule(Integer id, Saeule S){
        mID = id;
        mPos = S.getPosition();
        mTitel = S.getName();
        mSaeule = S;
        holeInfo();



    }








    }
