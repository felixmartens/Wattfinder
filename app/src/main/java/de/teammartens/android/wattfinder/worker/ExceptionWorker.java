package de.teammartens.android.wattfinder.worker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

import de.teammartens.android.wattfinder.KartenActivity;

/**
 * Created by felix on 24.08.17.
 */

public class ExceptionWorker  implements Thread.UncaughtExceptionHandler {

    private static final String[]  mailAddress = {"wattfinder_crash@7martens.de"};
        public static final String EXTRA_MY_EXCEPTION_HANDLER = "EXTRA_MY_EXCEPTION_HANDLER";
        private final Activity context;
        private final Thread.UncaughtExceptionHandler rootHandler;

        public ExceptionWorker(Activity context) {
            this.context = context;
            // we should store the current exception handler -- to invoke it for all not handled exceptions ...
            rootHandler = Thread.getDefaultUncaughtExceptionHandler();
            // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
            Thread.setDefaultUncaughtExceptionHandler(this);
        }


    public void composeCrashMail(String subject, String message)
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, mailAddress);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(KartenActivity.getInstance().getPackageManager()) != null) {
            KartenActivity.getInstance().startActivity(intent);
        }
    }

        @Override
        public void uncaughtException(final Thread thread, final Throwable e) {

            StackTraceElement[] arr = e.getStackTrace();
            GoogleApiClient mGoogleApiClient = KartenActivity.getInstance().setupGoogleAPI();
            final StringBuffer report = new StringBuffer(e.toString());
            final String DOUBLE_LINE_SEP = "\n\n";
            final String SINGLE_LINE_SEP = "\n";
            final String lineSeperator = "-------------------------------\n\n";
            report.append(DOUBLE_LINE_SEP);
            report.append("--------- Stack trace ---------\n\n");
            for (int i = 0; i < arr.length; i++) {
                report.append( "    ");
                report.append(arr[i].toString());
                report.append(SINGLE_LINE_SEP);
            }
            report.append(lineSeperator);
            // If the exception was thrown in a background thread inside
            // AsyncTask, then the actual exception can be found with getCause
            report.append("--------- Cause ---------\n\n");
            Throwable cause = e.getCause();
            if (cause != null) {
                report.append(cause.toString());
                report.append(DOUBLE_LINE_SEP);
                arr = cause.getStackTrace();
                for (int i = 0; i < arr.length; i++) {
                    report.append("    ");
                    report.append(arr[i].toString());
                    report.append(SINGLE_LINE_SEP);
                }
            }
            // Getting the Device brand,model and sdk verion details.
            report.append(lineSeperator);
            report.append("--------- Device ---------\n\n");
            report.append("Brand: ");
            report.append(Build.BRAND);
            report.append(SINGLE_LINE_SEP);
            report.append("Device: ");
            report.append(Build.DEVICE);
            report.append(SINGLE_LINE_SEP);
            report.append("Model: ");
            report.append(Build.MODEL);
            report.append(SINGLE_LINE_SEP);
            report.append("Id: ");
            report.append(Build.ID);
            report.append(SINGLE_LINE_SEP);
            report.append("Product: ");
            report.append(Build.PRODUCT);
            report.append(SINGLE_LINE_SEP);
            report.append(lineSeperator);
            report.append("--------- PlayServices ---------\n\n");
            report.append("Connection: ");
            report.append(GoogleApiAvailability.getInstance().getErrorString(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(KartenActivity.getInstance().getBaseContext())));
            report.append(SINGLE_LINE_SEP);
            report.append("Version: ");
            report.append(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE);
            report.append(SINGLE_LINE_SEP);
            report.append(lineSeperator);
            report.append("--------- Firmware ---------\n\n");
            report.append("SDK: ");
            report.append(Build.VERSION.SDK);
            report.append(SINGLE_LINE_SEP);
            report.append("Release: ");
            report.append(Build.VERSION.RELEASE);
            report.append(SINGLE_LINE_SEP);
            report.append("Incremental: ");
            report.append(Build.VERSION.INCREMENTAL);
            report.append(SINGLE_LINE_SEP);
            report.append("DebugID: ");
            report.append(LogWorker.getlogID());
            report.append(SINGLE_LINE_SEP);
            report.append(lineSeperator);



            Log.e("Report ::", report.toString());


            composeCrashMail("Wattfinder CrashMail", report.toString());
            LogWorker.e("CRASH", report.toString());
            LogWorker.sendLog();



            //ggf noch an den roothAblder weitergeben?
            // rootHandler.uncaughtException(thread, ex);


            // make sure we die, otherwise the app will hang ...
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);



            }
        }


