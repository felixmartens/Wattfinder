package de.teammartens.android.wattfinder.worker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;

import static de.teammartens.android.wattfinder.KartenActivity.getInstance;


/**
 * Created by felix on 22.07.17.
 */

public class NetWorker {

    public static final int TASK_FILTER = 0;
    public static final int TASK_SAEULEN = 1;
    private static final int DELAY = 5000;
    private static int RETRY = 0;
    private static final int RETRY_MAX = 3;
    private static int NETWORK_QUALITY = 1;

    public static int getNetworkQuality() {
        return NETWORK_QUALITY;
    }

    public static void setNetworkQuality(int networkQuality) {
        NETWORK_QUALITY = networkQuality;
    }

    public static void rehabilateNetworkQuality() {
        if(NETWORK_QUALITY<3)NETWORK_QUALITY++;
        View v = KartenActivity.getInstance().findViewById(R.id.errorMessage);
        if (v != null) AnimationWorker.slideDown(v, 0);
    }

    public static void resetNetworkQuality(){


        if(isWiFi())
            setNetworkQuality(3);
        else {
            ConnectivityManager cm =
                    (ConnectivityManager) KartenActivity.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {


                TelephonyManager tm = (TelephonyManager) KartenActivity.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
                if (tm.getNetworkType() > 2)
                    setNetworkQuality(2);
                else
                    setNetworkQuality(1);
            }
        }
    }

    public static boolean networkavailable() {


        ConnectivityManager cm =
                (ConnectivityManager) KartenActivity.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();


          resetNetworkQuality();

        if (!isConnected) {

            TextView tv = (TextView) KartenActivity.getInstance().findViewById(R.id.errorTitle);
            if (tv != null) tv.setText(getInstance().getString(R.string.error_network_title));

            tv = (TextView) KartenActivity.getInstance().findViewById(R.id.errorText);
            if (tv != null) tv.setText(getInstance().getString(R.string.error_network_title));

            View v = KartenActivity.getInstance().findViewById(R.id.errorMessage);
            if (v != null) AnimationWorker.slideUp(v, 0);
        }


        return isConnected;
    }


    public static void handleError(VolleyError error, final int Task) {
        handleError(error,Task,"");
    }

    public static void handleError(VolleyError error, final int Task, final String Liste) {


/*******************
 *Keine manuellen Retries da VOlley bereits welche implementiert. Lieber passen wir das Verhalten von Volley an
 *

        if (RETRY < RETRY_MAX){
            RETRY++;
            TimerTask T;

            if (Task==TASK_FILTER)
                /*T= new TimerTask() {
                    @Override
                    public void run() {
                        FilterWorks.filter_API_request(Liste);
                    }
                };
                FilterWorks.filter_API_request(Liste);
            else
               /* T = new TimerTask() {
                    @Override
                    public void run() {
                        SaeulenWorks.reloadMarker();
                    }
                };
                SaeulenWorks.reloadMarker();

            //new Timer().schedule(T ,RETRY*DELAY);
        }else
 */
       // {
        if (error != null) {
            Toast.makeText(KartenActivity.getInstance(), "NetzwerkFehler:" + error.getMessage(), Toast.LENGTH_LONG).show();
            if (LogWorker.isVERBOSE()) LogWorker.e("NETWORKER", "Netzwerkfehler: " + Liste + "\n"
                    + error.getMessage() + " \n " + (error.getCause()!=null?error.getCause().getMessage()+ " \n "
                    + error.getCause().toString():"")
            );
            TextView tv = (TextView) KartenActivity.getInstance().findViewById(R.id.errorTitle);
            if (tv != null) tv.setText(getInstance().getString(R.string.error_network_title));

            tv = (TextView) KartenActivity.getInstance().findViewById(R.id.errorText);
            if (tv != null) tv.setText(error.getMessage());
            View v = KartenActivity.getInstance().findViewById(R.id.errorMessage);
            v.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         if (Task == TASK_FILTER) FilterWorks.refresh_filterlisten_API();
                                         else SaeulenWorks.reloadMarker();
                                     }
                                 }

            );

            v = KartenActivity.getInstance().findViewById(R.id.errorMessage);
            if (v != null) AnimationWorker.slideUp(v, 0);
        }


    }


    public static boolean isWiFi(){



        ConnectivityManager cm =
                (ConnectivityManager) KartenActivity.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork == null) return false;

        return (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);


    }

}
