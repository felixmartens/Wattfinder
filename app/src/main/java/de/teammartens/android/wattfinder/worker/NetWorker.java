package de.teammartens.android.wattfinder.worker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;

import java.util.Timer;
import java.util.TimerTask;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;

import static de.teammartens.android.wattfinder.KartenActivity.getInstance;
import static de.teammartens.android.wattfinder.KartenActivity.slideDown;
import static de.teammartens.android.wattfinder.KartenActivity.slideUp;

/**
 * Created by felix on 22.07.17.
 */

public class NetWorker {

    public static final int TASK_FILTER = 0;
    public static final int TASK_SAEULEN = 1;
    private static final int DELAY = 5000;
    private static int RETRY = 0;
    private static final int RETRY_MAX = 3;

    public static boolean networkavailable() {

        ConnectivityManager cm =
                (ConnectivityManager) KartenActivity.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();


        if (!isConnected) {

            TextView tv = (TextView) KartenActivity.getInstance().findViewById(R.id.errorTitle);
            if (tv != null) tv.setText(getInstance().getString(R.string.error_network_title));

            tv = (TextView) KartenActivity.getInstance().findViewById(R.id.errorText);
            if (tv != null) tv.setText(getInstance().getString(R.string.error_network_title));

            View v = KartenActivity.getInstance().findViewById(R.id.errorMessage);
            if (v != null) slideUp(v, 0);
       }



        return isConnected;
    }


    public static void handleError(VolleyError error, final int Task ){


        //TODO
        // 1. Error Message anzeigen
        // 2. Farbe anpassen
        TextView tv = (TextView) KartenActivity.getInstance().findViewById(R.id.errorTitle);
        if(tv!=null)tv.setText(getInstance().getString(R.string.error_network_title));

        tv = (TextView) KartenActivity.getInstance().findViewById(R.id.errorText);
        if(tv!=null)tv.setText(error.getMessage());
        View v = KartenActivity.getInstance().findViewById(R.id.errorMessage);
        v.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     if (Task==TASK_FILTER)FilterWorks.lade_filterlisten_API();
                                     else SaeulenWorks.reloadMarker();
                                 }
                             }

        );

        v = KartenActivity.getInstance().findViewById(R.id.errorMessage);
        if(v!=null) slideUp(v,0);

       /* if (RETRY < RETRY_MAX){
            RETRY++;
            TimerTask T;

            if (Task==TASK_FILTER)
                T= new TimerTask() {
                    @Override
                    public void run() {
                        FilterWorks.lade_filterlisten_API();
                    }
                };
            else
                T = new TimerTask() {
                    @Override
                    public void run() {
                        SaeulenWorks.reloadMarker();
                    }
                };

            new Timer().schedule(T ,RETRY*DELAY);
        }*/




    }

    public static int getRETRY() {
        return RETRY;
    }

    public static void resetRETRY() {
        NetWorker.RETRY = 0;
        View v = KartenActivity.getInstance().findViewById(R.id.errorMessage);
        if(v!=null) slideDown(v,0);
    }
}
