package de.teammartens.android.wattfinder.worker;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.teammartens.android.wattfinder.KartenActivity;

/**
 * Created by felix on 29.01.17.
 */

public class LogWorker {
    private static boolean VERBOSE = false;

    public static String getlogID() {
        return id;
    }

    private static String id = "abcd1234";
    private static final String LOG_TAG = "LOG_WORKER";
    private static Set<LogEntry> LogCache = new HashSet<LogEntry>();

    public static boolean isVERBOSE() {
        return VERBOSE;
    }

    public static void setVERBOSE(boolean VERBOSE) {
        LogWorker.VERBOSE = VERBOSE;
        d(LOG_TAG,"LogVerbose set to "+VERBOSE);
        AnimationWorker.show_debug();
    }



    public static void e(String TAG, String message){
        cacheLog("E",TAG,message);
        if (message != null && message.length() > 0) Log.e(TAG,message);
    }

    public static void e(String TAG, String message, Exception e){
        cacheLog("E",TAG,message+" Exception:"+e);
        if (message != null && message.length() > 0) Log.e(TAG,message,e);
    }

    public static void d(String TAG, String message){

        cacheLog("D",TAG,message);
        if (message != null && message.length() > 0) Log.d(TAG,message);
    }

    public static void init_logging(){
        SharedPreferences sPref = KartenActivity.sharedPref;

        id = sPref.getString("LoggingID","abcdef");
        if (id.equals("abcdef"))
            id=generate_id();


    }


    private static void cacheLog(final String type, final String TAG, final String message){

        LogCache.add(new LogEntry(type,TAG,message));

        if (LogCache.size()==25) {

            sendLog(LogCache);//LogCache.clear();
             }
    }

    public static void sendLog(){
        sendLog(LogCache);
    }
    public static void sendLog(final Set<LogEntry> logs){
        String uri="http://wattfinder.7martens.de/LogWorkerAPI.php";

        if(logs.size()>0) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, uri,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(LOG_TAG, "Log sent. " + response);

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(LOG_TAG, "Log not sent: " + error.toString());
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("logID", id);
                    params.put("RQCOUNT", String.valueOf(KartenActivity.getAPI_RQ_Count()));
                    Iterator<LogEntry> it = logs.iterator();
                    int i = 0;
                    LogEntry e = new LogEntry();
                    while (it.hasNext()) {
                        e = it.next();

                        params.put("logType[" + i + "]", e.getType());
                        params.put("logTag[" + i + "]", e.getTag());
                        params.put("logMessage[" + i + "]", e.getMessage());

                        i++;

                    }
                    logs.clear();
                    Log.v(LOG_TAG,i+"/"+LogCache.size()+" Logs verschickt");
                    return params;
                }

            };

            KartenActivity.getInstance().addToRequestQueue(stringRequest);
        }
    }

    private static String generate_id(){
        final String alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";
        final int N = alphabet.length();
        String id="";

        Random r = new Random();

        for (int i = 0; i < 6; i++) {
            id+=(alphabet.charAt(r.nextInt(N)));
        }
        d(LOG_TAG,"New id generated: "+id);
        SharedPreferences.Editor editor = KartenActivity.sharedPref.edit();
        editor.putString("LoggingID",id);
        editor.commit();
        return id;
    }

private static class LogEntry {
    private String mType = "";
    private String mMessage = "";
    private String mTag ="";


    LogEntry(String Type, String Tag, String Message){
        mTag=Tag;
        mType=Type;
        mMessage=Message;
    }

    LogEntry(){
        mTag="null";
        mType="null";
        mMessage="null";
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }



}


}
