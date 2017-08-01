package de.teammartens.android.wattfinder;

import android.app.Application;

//import org.acra.ACRA;
//import org.acra.ReportField;
//import org.acra.ReportingInteractionMode;
//import org.acra.annotation.ReportsCrashes;
//import org.acra.sender.HttpSender;

import de.teammartens.android.wattfinder.R;

/**
 * Created by felix on 09.08.15.
 */
/*
@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        customReportContent = { ReportField.REPORT_ID, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT,ReportField.SHARED_PREFERENCES },
        formUri = "http://martens.iriscouch.com/acra-wattfinder/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "wattfinder2",
        formUriBasicAuthPassword = "Hm8%tjd*LUt",
        // Your usual ACRA configuration
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
)*/

public class WattfinderApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
    }
}
