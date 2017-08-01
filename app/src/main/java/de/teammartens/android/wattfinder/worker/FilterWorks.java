package de.teammartens.android.wattfinder.worker;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

//import org.acra.ACRA;
//import org.acra.ErrorReporter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.model.FilterEintrag;
import de.teammartens.android.wattfinder.model.PresetEintrag;

import static de.teammartens.android.wattfinder.KartenActivity.showStartup;


/**
 * Created by felix on 16.06.15.
 */
public class FilterWorks {
    private static final String LOG_TAG = "Wattfinder FilterWorks";
    public static final int F_KOSTENLOS = 0;
    public static final int F_BARRIEREFREI = 1;
    public static final int F_open247 = 2;
    public static final int F_opennow = 8;
    public static final int F_PARKEN = 3;
    public static final int F_BESTAETIGT = 4;
    public static final int F_KEINESTOERUNG = 5;
    public static final int F_HOTELS = 6;
    public static final int F_RESTAURANT = 7;
    public static String PRESET = null;
    public static String BELIEBIG = "BELIEBIG99";
    public static final String F_STECKER = "STECKER";
    public static final String F_VERBUND = "VERBUND";
    public static final String F_KARTEN = "KARTEN";
    public static final String F_PRESET = "PRESETS";
    public static final Integer[]  F_POWER_VALUE = {2,6,11,20,22,43,50,100};
    private static ArrayList<Boolean> filter = new ArrayList<Boolean>();
    private static Integer filter_minpower = 0;
    private static Set<String> presets = new HashSet<String>();
    private static Set<String> stecker = new HashSet<String>();
    private static Set<String> verbund = new HashSet<String>();
    private static Set<String> karten = new HashSet<String>();




    public static Set<String> stecker_verfuegbar_API = new HashSet<String>();
    public static Set<String>  verbund_verfuegbar_API = new HashSet<String>();
    public static HashMap<Integer,String>  karten_verfuegbar_API = new HashMap<Integer,String>();

    private static String fUrl = "http://www.goingelectric.de/stromtankstellen/";
    private static String fAPIUrl_plugs = "https://api.goingelectric.de/chargepoints/pluglist/";
    private static String fAPIUrl_networks = "https://api.goingelectric.de/chargepoints/networklist/";
    private static String fAPIUrl_cards = "https://api.goingelectric.de/chargepoints/chargecardlist/";
    private static Long f_TIMESTAMP = 0l;
    private final static Long f_OUTDATED = 3600*12l;
    /*
    Gespeicherte Filtereinstellungen laden
     */



    public static void lade_filter_db() {
        SharedPreferences sPref = KartenActivity.sharedPref;
        if (PRESET==null) PRESET = sPref.getString("currentPreset", KartenActivity.getInstance().getString(R.string.filter_standardprofil));
        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "PRESET:" + PRESET);
        if (PRESET.isEmpty()) PRESET=KartenActivity.getInstance().getString(R.string.filter_standardprofil);

        presets=sPref.getStringSet("PRESETS", new HashSet<String>());

            if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "presets:" + presets.toString());
        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Aktuelles Profil:" + PRESET);
        presets.add(PRESET);
        filter.clear();
        filter.add(sPref.getBoolean(PRESET + "KOSTENLOS", false));
        filter.add(sPref.getBoolean(PRESET + "BARRIEREFREI", false));
        filter.add(sPref.getBoolean(PRESET + "open247", false));
        filter.add(sPref.getBoolean(PRESET + "PARKEN", false));
        filter.add(sPref.getBoolean(PRESET + "BESTAETIGT", false));
        filter.add(sPref.getBoolean(PRESET + "KEINESTOERUNG", false));
        filter.add(sPref.getBoolean(PRESET + "HOTELS", false));
        filter.add(sPref.getBoolean(PRESET + "RESTAURANT", false));
        filter.add(sPref.getBoolean(PRESET + "opennow", false));

        filter_minpower= sPref.getInt(PRESET+"MINPOWER", 0);

        stecker = sPref.getStringSet(PRESET + "STECKER", new HashSet<String>());
        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Stecker aus DB geladen:" + stecker.toString());

        verbund = sPref.getStringSet(PRESET + "VERBUND", new HashSet<String>());



        karten = sPref.getStringSet (PRESET + "KARTEN", new HashSet<String>());
        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Karten aus DB geladen:" + karten.toString());
        convert2API();

    }



    /*
   Listen für Stecker,Verbund, und Karten von Webseite via JSON API laden
   12/2016
    */
    public static void lade_filterlisten_API() {
        if ((System.currentTimeMillis() / 1000 - f_TIMESTAMP) > f_OUTDATED ||
                !filter_initialized()) {

            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Lade Filterlisten API");
            String params = "key=" + KartenActivity.getInstance().getString(R.string.GoingElectric_APIKEY);
            karten_verfuegbar_API.clear();
            stecker_verfuegbar_API.clear();
            verbund_verfuegbar_API.clear();
            JsonObjectRequest filterReqPlugs = new JsonObjectRequest(Request.Method.GET, fAPIUrl_plugs + "?" + params, (String) null, new Response.Listener<JSONObject>() {
                @Override

                public void onResponse(JSONObject response) {
                    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "FilterPlugResponse");
                    //string=fixEncoding(string);
                    try {
                        NetWorker.resetRETRY();
                        String status = response.getString("status");
                        Set<String> steckerT = new HashSet<String>();
                        JSONArray Stecker = response.getJSONArray("result");
                        final int Steckerzahl = Stecker.length();
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, Steckerzahl + " Stecker gefunden");
                        if (Steckerzahl > 0) {
                            f_TIMESTAMP = System.currentTimeMillis() / 1000;
                            stecker_verfuegbar_API.clear();

                            for (int i = 0; i < Steckerzahl; i++) {
                                stecker_verfuegbar_API.add(Stecker.getString(i));
                                // if (LogWorker.isVERBOSE())  LogWorker.d(LOG_TAG, "Stecker:" + Stecker.getString(i));
                            }

                            if (!stecker_verfuegbar_API.isEmpty())
                                steckerT.addAll(stecker);
                                for (String s : steckerT) {
                                    if (!stecker_verfuegbar_API.contains(s)) {
                                        APIconverter(F_STECKER, s);
                                    }
                                }

                            if (filter_initialized())
                                KartenActivity.hideStartup();
                        } else {
                            if (LogWorker.isVERBOSE())
                                LogWorker.e(LOG_TAG, "KEINE Stecker gefunden!!!!!");
                        }

                    } catch (JSONException e) {
                        LogWorker.e(LOG_TAG, "JSONERROR:" + e.getMessage());
                        e.printStackTrace();
                    }

                }
            }
                    , new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    NetWorker.handleError(error,NetWorker.TASK_FILTER);

                }

            });

            JsonObjectRequest filterReqNetworks = new JsonObjectRequest(Request.Method.GET, fAPIUrl_networks + "?" + params, (String) null, new Response.Listener<JSONObject>() {
                @Override

                public void onResponse(JSONObject response) {
                    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "FilterNetworkResponse");
                    //string=fixEncoding(string);
                    try {
                        NetWorker.resetRETRY();
                        String status = response.getString("status");
                        Set<String> verbundT = new HashSet<String>();
                        JSONArray Verbund = response.getJSONArray("result");
                        final int Verbundzahl = Verbund.length();
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, Verbundzahl + " Verbünde gefunden");
                        if (Verbundzahl > 0) {
                            f_TIMESTAMP = System.currentTimeMillis() / 1000;
                            verbund_verfuegbar_API.clear();

                            for (int i = 0; i < Verbundzahl; i++) {
                                verbund_verfuegbar_API.add(Verbund.getString(i));
                                // if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Verbund:" + Verbund.getString(i));
                            }


                            if (!verbund_verfuegbar_API.isEmpty())
                                verbundT.addAll(verbund);
                                for (String v : verbundT) {
                                    if (!verbund_verfuegbar_API.contains(v)) {
                                        APIconverter(F_VERBUND, v);
                                    }
                                }
                            if (filter_initialized())
                                KartenActivity.hideStartup();
                        } else {
                            if (LogWorker.isVERBOSE())
                                LogWorker.e(LOG_TAG, "KEINE Verbund gefunden!!!!!");
                        }

                    } catch (JSONException e) {
                        LogWorker.e(LOG_TAG, "JSONERROR:" + e.getMessage());
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetWorker.handleError(error,NetWorker.TASK_FILTER);
                }

            });

            JsonObjectRequest filterReqCards = new JsonObjectRequest(Request.Method.GET, fAPIUrl_cards + "?" + params, (String) null, new Response.Listener<JSONObject>() {
                @Override

                public void onResponse(JSONObject response) {
                    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "FilterCardsResponse");
                    //string=fixEncoding(string);
                    try {
                        NetWorker.resetRETRY();
                        String status = response.getString("status");
                        JSONArray Karten = response.getJSONArray("result");
                        Set<String> kartenT = new HashSet<String>();
                        final int Kartenzahl = Karten.length();
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, Kartenzahl + " Karten gefunden");
                        if (Kartenzahl > 0) {
                            f_TIMESTAMP = System.currentTimeMillis() / 1000;
                            karten_verfuegbar_API.clear();
                            JSONObject jO = new JSONObject();
                            for (int i = 0; i < Kartenzahl; i++) {
                                jO = Karten.getJSONObject(i);
                                karten_verfuegbar_API.put(jO.getInt("card_id"), jO.getString("name"));

                                //if (LogWorker.isVERBOSE())  LogWorker.d(LOG_TAG, "Karte:" + jO.getString("name"));
                            }

                            if (!karten_verfuegbar_API.isEmpty())

                            kartenT.addAll(karten);
                                for (String s : kartenT) {
                                    if (!s.matches("[0-9]+") || !karten_verfuegbar_API.containsKey(Integer.decode(s))) {
                                        APIconverter(F_KARTEN, s);
                                    }
                                }
                            if (filter_initialized())
                                KartenActivity.hideStartup();

                        } else {
                            if (LogWorker.isVERBOSE())
                                LogWorker.e(LOG_TAG, "KEINE Karten gefunden!!!!!");
                        }

                    } catch (JSONException e) {
                        LogWorker.e(LOG_TAG, "JSONERROR:" + e.getMessage());
                        e.printStackTrace();
                    }

                }
            }
                    , new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetWorker.handleError(error,NetWorker.TASK_FILTER);

                }

            });

            KartenActivity.getInstance().addToRequestQueue(filterReqPlugs);
            KartenActivity.incAPI_RQ_Count();
            KartenActivity.getInstance().addToRequestQueue(filterReqNetworks);
            KartenActivity.incAPI_RQ_Count();
            KartenActivity.getInstance().addToRequestQueue(filterReqCards);
            KartenActivity.incAPI_RQ_Count();

        }else
            SaeulenWorks.checkMarkerCache("ladefilterAPI-abgebrochen");
    }

    /*
    Filtereinstellungen speichern
     */
    public static void filter_speichern(){

        //nicht specihern wenn neues Profil drin steht
        if (PRESET.equals(KartenActivity.getInstance().getString(R.string.filter_neuesprofil))||PRESET.isEmpty())
            return;


        SharedPreferences.Editor editor = KartenActivity.sharedPref.edit();
        editor.putBoolean(PRESET+"KOSTENLOS", filter.get(F_KOSTENLOS));
        editor.putBoolean(PRESET+"BARRIEREFREI", filter.get(F_BARRIEREFREI));
        editor.putBoolean(PRESET+"open247", filter.get(F_open247));
        editor.putBoolean(PRESET+"opennow", filter.get(F_opennow));
        editor.putBoolean(PRESET+"PARKEN", filter.get(F_PARKEN));
        editor.putBoolean(PRESET+"BESTAETIGT", filter.get(F_BESTAETIGT));
        editor.putBoolean(PRESET+"KEINESTOERUNG", filter.get(F_KEINESTOERUNG));
        editor.putBoolean(PRESET+"HOTELS", filter.get(F_HOTELS));
        editor.putBoolean(PRESET+"RESTAURANT", filter.get(F_RESTAURANT));
        editor.putInt(PRESET+"MINPOWER", filter_minpower);
        editor.putStringSet(PRESET+"STECKER", stecker);
        editor.putStringSet(PRESET+"VERBUND",verbund);
        editor.putStringSet(PRESET+"KARTEN", karten);
        editor.putString("currentPreset", PRESET);
        editor.putStringSet("PRESETS", presets);
        if (! editor.commit()){
           /* if (KartenActivity.SEND_REPORTS){
                ErrorReporter ER = ACRA.getErrorReporter();
                ER.putCustomData("error","commit fehlgeschlagen");
                ER.putCustomData("PRESET",PRESET);
                ER.putCustomData("presets",presets.toString());
                ER.handleSilentException(null);
            }*/
            Toast.makeText(KartenActivity.getInstance(),R.string.SaveFilterError,Toast.LENGTH_LONG);
            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"FEHLER: Filter für Profil "+PRESET+" nicht gespeichert.");

        }
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Filter für Profil "+PRESET+" gespeichert.");
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"FilterStecker:"+stecker.toString());
        if(LogWorker.isVERBOSE()){
            Set<String> steckTEST = KartenActivity.sharedPref.getStringSet(PRESET + "STECKER", new HashSet<String>());
            if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Stecker in DB gespeichert:" + steckTEST.toString());
            Set<String> KartenTEST = KartenActivity.sharedPref.getStringSet(PRESET + "KARTEN", new HashSet<String>());
            if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "KARTEN in DB gespeichert:" + KartenTEST.toString());
        }


    }

    /*
    Einen der Boolean-Filter Typen setzen und Ergebnis bekommen (zur Kontrolle)
     */
    public static boolean setze_filter(Integer F,Boolean Value){

        filter.set(F,Value);

        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Filter"+F+"("+FilterInt2StrHelper(F)+") gesetzt: "+filter.get(F) );
        filter_speichern();
        return filter.get(F);


    }

    private static String FilterInt2StrHelper(int F){
        switch (F){
            case F_KOSTENLOS: return "F_KOSTENLOS";

            case F_BARRIEREFREI: return "F_BARRIEREFREI";

            case F_open247: return "F_open247";

            case F_opennow: return "F_opennow";

            case F_PARKEN: return "F_PARKEN";

            case F_BESTAETIGT: return "F_BESTAETIGT";

            case F_KEINESTOERUNG: return "F_KEINESTOERUNG";

            case F_HOTELS: return "F_HOTELS";

            case F_RESTAURANT: return "F_RESTAURANT";

        }



        return "";
    }

    public static int setze_power(Integer progress){

        filter_minpower=F_POWER_VALUE[progress];
        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Filter POWER gesetzt: "+filter_minpower );
        filter_speichern();
        return filter_minpower;


    }


    public static boolean lese_filter(Integer F){
        if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG," Lese Filter: "+F+"("+FilterInt2StrHelper(F)+"):"+filter.get(F));
        return filter.get(F);


    }

    public static boolean lese_liste(String Liste,String Value){

        switch (Liste){
            case F_STECKER:
                if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"LeseFilterListe: "+Liste+":"+Value+" "+stecker.contains(Value));
                return (stecker.contains(Value));

            case F_VERBUND:
                if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"LeseFilterListe: "+Liste+":"+Value+" "+verbund.contains(Value));
               return (verbund.contains(Value));

            case F_KARTEN:
                //Erstmal Titel zu Id umsetzen
                String cardid = "0";
                if(karten_verfuegbar_API.containsValue(Value))
                    for (Map.Entry<Integer,String> e : karten_verfuegbar_API.entrySet())
                    {
                        if (e.getValue().equals(Value)){
                            cardid=String.valueOf(e.getKey());
                            break;
                        }
                    }

                if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"LeseFilterListe: "+Liste+":"+Value+"("+cardid+")"+karten.contains(cardid));
                return karten.contains(cardid);

        }
        return false;
    }

    public static int lese_minpower(){
        if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Filter: POWER "+filter_minpower);
        return filter_minpower;


    }
    /*
    Einen Wert der Liste hinzufügen/löschen und Ergebnis bekommen (zur Kontrolle)
    */
    public static boolean liste_aendern(String Liste,String Value){
        switch (Liste){
            case F_STECKER:
                if (Value.equals(BELIEBIG)){
                    if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "ListeAendern: SteckerFilter: BELIEBIG");
                    stecker.clear();return true;
                }
                if(stecker.contains(Value)) stecker.remove(Value); else stecker.add(Value);
                if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "ListeAendern: SteckerFilter:"+Value+" = "+stecker.contains(Value));
                filter_speichern();
                if (stecker.contains(Value))return true; else return false;

            case F_VERBUND:
                if (verbund.contains(Value)) verbund.remove(Value); else verbund.add(Value);
                if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "ListeAendern: VerbundFilter:" + Value + " = " + verbund.contains(Value));
                filter_speichern();
                if (verbund.contains(Value))return true; else return false;

            case F_KARTEN:
                //Erstmal Titel zu Id umsetzen
                String cardid = "0";
                if(karten_verfuegbar_API.containsValue(Value))
                    for (Map.Entry<Integer,String> e : karten_verfuegbar_API.entrySet())
                    {
                        if (e.getValue().equals(Value)){
                            cardid=String.valueOf(e.getKey());
                            break;
                        }
                    }
                if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "ListeAendern: KartenFilter ändern:" + cardid + " = bisher " + karten.contains(cardid));
                if (karten.contains(cardid)) karten.remove(cardid); else karten.add(cardid);
                if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "ListeAendern: KartenFilter geändert:" + cardid + " = " + karten.contains(cardid));
                filter_speichern();
                return karten.contains(cardid);

        }
        return false;
    }
/*
    public static String lese_liste(String Liste){
        String L = "";
        switch (Liste){
            case F_STECKER:
                for(String s : stecker){
                    if (!L.isEmpty()) L=L+", ";
                    L=L+stecker_verfuegbar.get(s);
                }
                break;

            case F_VERBUND:
                for(String s : verbund){
                    if (!L.isEmpty()) L=L+", ";
                    L=L+verbund_verfuegbar.get(s);
                }
                break;

            case F_KARTEN:
                for(String s : karten){
                    if (!L.isEmpty()) L=L+", ";
                    L=L+karten_verfuegbar.get(s);
                }
                break;

        }
        return L;
    }
*/
    public static Boolean Liste_beliebig(String Liste){

        switch (Liste){
            case F_STECKER:
                if (stecker.size()>0) return false; else return true;


            case F_VERBUND:
                if (verbund.size()>0) return false; else return true;


            case F_KARTEN:
                if (karten.size()>0) return false; else return true;


        }
        return false;

    }


    public static Boolean setListeBeliebig(String Liste){

        switch (Liste){
            case F_STECKER:
                stecker.clear();
                if (stecker.size()>0) return false; else return true;


            case F_VERBUND:
                verbund.clear();
                if (verbund.size()>0) return false; else return true;


            case F_KARTEN:
                karten.clear();
                if (karten.size()>0) return false; else return true;


        }
        return false;

    }


    public static String ParamListe() {

        String param = "";


        if (verbund.isEmpty()) param+="verbund[]=alle";
        else {
            String vb = "";
            for (String v : verbund) {
                if (!vb.isEmpty()) vb += "&";
                vb +=  "verbund[]="+v ;
            }
            param+=vb;
        }
param+="&";
        if (stecker.isEmpty()) param="stecker[]=alle";
        else {
            String st = "";
            for (String s : stecker) {
                if (!st.isEmpty()) st += "&";
                st += "stecker[]=" + s;
            }
            param+= st ;
        }
        param+="&";
        if (karten.isEmpty()) param+="ladekarte[]=alle";
        else {
            String kt = "";
            for (String k : karten) {
                if (kt.isEmpty()) kt += "&";
                kt +=  "ladekarte[]="+ k ;
            }
            param+=kt ;
        }
        param= URLEncoder.encode(param);
        param=param.replaceAll("%3D","=").replaceAll("%26","&");
        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"ParamList:"+param);
return param;
    }
    public static Map<String, String> ParamFilter(){

        Map<String, String> params = new HashMap<String, String>();
        params.put("typ", "SucheZoom");
        if (verbund.isEmpty()) params.put("verbund[]","alle");
        else {
           int vi =0;
            for (String v : verbund)
               {

                params.put("verbund["+vi+"]",v) ;
                   vi++;
            }
        }
        if (stecker.isEmpty()) params.put("stecker[]","alle");
        else {
            int vi =0;
            for (String v : stecker)
            {

                params.put("stecker["+vi+"]",v) ;
                vi++;
            }
        }
        if (karten.isEmpty()) params.put("ladekarte[]","alle");
        else {
            int vi =0;
            for (String v : karten)
            {

                params.put("ladekarte["+vi+"]",v) ;
                vi++;
            }
        }

        if (filter!=null && filter.size()>0) {
            params.put("kostenlos", filter.get(F_KOSTENLOS).toString());
            params.put("oeffnungszeiten", filter.get(F_open247).toString());

            params.put("kostenlosparken", filter.get(F_PARKEN).toString());
            params.put("hotel", filter.get(F_HOTELS).toString());
            params.put("restaurant", filter.get(F_RESTAURANT).toString());
            params.put("fotos", "false");
            params.put("verifiziert", filter.get(F_BESTAETIGT).toString());
            params.put("barrierefrei", filter.get(F_BARRIEREFREI).toString());
            params.put("notverifiziert", "false");
            params.put("keinestoerung", filter.get(F_KEINESTOERUNG).toString());
            params.put("stoerung", "false");
            //if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "PARAMS:" + params.keySet().toString());
            //if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "PARAMS:" + params.values().toString());
        }
        return params;
    }




 public static String ParamFilterString(){
     String params="";

     try {
         if (stecker != null && stecker.size() > 0)
             params += "plugs=" + URLEncoder.encode(TextUtils.join(",", stecker), "UTF-8") + "&";

         if ( verbund != null && verbund.size() > 0)
             params += "networks=" + URLEncoder.encode(TextUtils.join(",", verbund), "UTF-8") + "&";

         if ( karten != null && karten.size() > 0)
             params += "chargecards=" + URLEncoder.encode(TextUtils.join(",", karten), "UTF-8") + "&";

     }catch(UnsupportedEncodingException e){
         e.printStackTrace();
     }

     if (filter!=null&&filter.size()>0) {
         params += "min_power=" + lese_minpower() + "&";
         params += "freecharging=" + filter.get(F_KOSTENLOS).toString() + "&";
         params += "freeparking=" + filter.get(F_PARKEN).toString() + "&";

         if (filter.get(F_opennow)) params += "open_now=" + filter.get(F_opennow).toString() + "&";
         else if (filter.get(F_open247)) params += "open_twentyfourseven=" + filter.get(F_open247).toString() + "&";
                else params += "open_twentyfourseven=" + filter.get(F_open247).toString() + "&" + "open_now=" + filter.get(F_opennow).toString() + "&";


         params += "verified=" + filter.get(F_BESTAETIGT).toString() + "&";
         params += "barrierfree=" + filter.get(F_BARRIEREFREI).toString() + "&" ;


         params += "exclude_faults=" + filter.get(F_KEINESTOERUNG).toString();

     }


    return params;


 }

 public static int paramsHash(){
     String params="";

     try {
         if (stecker != null && stecker.size() > 0)
             params += "plugs=" + URLEncoder.encode(TextUtils.join(",", stecker), "UTF-8") + "&";

         if ( verbund != null && verbund.size() > 0)
             params += "networks=" + URLEncoder.encode(TextUtils.join(",", verbund), "UTF-8") + "&";

         if (karten != null && karten.size() > 0)
             params += "cards=" + URLEncoder.encode(TextUtils.join(",", karten), "UTF-8") + "&";

     }catch(UnsupportedEncodingException e){
         e.printStackTrace();
     }

     if (filter!=null&&filter.size()>0) {
         params += "min_power=" + lese_minpower() + "&";
         params += "freecharging=" + filter.get(F_KOSTENLOS).toString() + "&";
         params += "freeparking=" + filter.get(F_PARKEN).toString() + "&";

         if (filter.get(F_opennow)) params += "open_now=" + filter.get(F_opennow).toString() + "&";
         else if (filter.get(F_open247))
             params += "open_twentyfourseven=" + filter.get(F_open247).toString() + "&";
         else
             params += "open_twentyfourseven=" + filter.get(F_open247).toString() + "&" + "open_now=" + filter.get(F_opennow).toString() + "&";


         params += "verified=" + filter.get(F_BESTAETIGT).toString() + "&";
         params += "barrierfree=";
         params += filter.get(F_BARRIEREFREI).toString();
         params += "&";
         params += "exclude_faults=" + filter.get(F_KEINESTOERUNG).toString();
     }
         return params.hashCode();

 }





    public static ArrayList<FilterEintrag> ListeToArrayList(String Liste){

            ArrayList<FilterEintrag> filterListe = new ArrayList<FilterEintrag>();

        switch (Liste){
            case FilterWorks.F_STECKER:

                for ( String v : stecker_verfuegbar_API){
                    filterListe.add(new FilterEintrag(v, stecker.contains(v)));
                    if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Stecker:" + v + ";" + stecker.contains(v));
                }
                if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,stecker_verfuegbar_API.size()+"/"+filterListe.size()+" Stecker verfügbar");
                break;

            case FilterWorks.F_VERBUND:
                for ( String v : verbund_verfuegbar_API){
                    filterListe.add(new FilterEintrag(v,verbund.contains(v)));

                }
                if(LogWorker.isVERBOSE())  LogWorker.d(LOG_TAG,verbund_verfuegbar_API.size()+" Verbünde verfügbar");
                break;

            case FilterWorks.F_KARTEN:
                for ( Map.Entry<Integer,String> e : karten_verfuegbar_API.entrySet()){
                    filterListe.add(new FilterEintrag(e.getValue(),karten.contains(String.valueOf(e.getKey()))));

                }
                if(LogWorker.isVERBOSE())  LogWorker.d(LOG_TAG,karten_verfuegbar_API.size()+" Karten verfügbar");
                break;



        }
       Collections.sort(filterListe, new Comparator<FilterEintrag>() {
            @Override
            public int compare(FilterEintrag fE1, FilterEintrag fE2) {

                return fE1.getTitel().compareTo(fE2.getTitel());
            }
        });
        if(LogWorker.isVERBOSE())  LogWorker.d(LOG_TAG,"FilterListeToArray "+Liste+" mit "+filterListe.size()+" Einträge verfügbar");
        return filterListe;
    }

    public static ArrayList<PresetEintrag> PresetArrayList(){

        ArrayList<PresetEintrag> filterListe = new ArrayList<PresetEintrag>();


                for ( String v : presets){
                    filterListe.add(new PresetEintrag(v,v.equals(PRESET)));
                }
                if(LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,presets.size()+" Presets verfügbar");




        Collections.sort(filterListe, new Comparator<PresetEintrag>() {
            @Override
            public int compare(PresetEintrag fE1, PresetEintrag fE2) {

                return fE1.getTitel().compareTo(fE2.getTitel());
            }
        });

        return filterListe;
    }


    public static void loadPresets(String preset){
        if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Lade Preset "+preset);
        if (preset.equals(PRESET))
            return;
        filter_speichern();
        PRESET=preset;
        lade_filter_db();
        //FilterFragment.setPreset();

    }


    public static void renamePreset(String old, String neu){
        if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Rename Preset "+old +" zu "+neu);
        if (neu.isEmpty()|| presets.contains(neu)||neu.equals(KartenActivity.getInstance().getString(R.string.filter_neuesprofil))) {


            return;
        }
        old=old.trim();
        neu=neu.trim();
        if(!old.isEmpty()&&presets.contains(old)){
            //loadPresets(old);
            presets.add(neu);
            PRESET = neu;
            filter_speichern();
            clearPreset(old);

        }else{
            //Dann sollte es neu sein
            presets.add(neu);
            PRESET = neu;
            filter_speichern();
            lade_filter_db();
        }

    }
    public static void clearPreset(){
        clearPreset(PRESET);
    }
    public static void clearPreset(String preset) {
        if (! preset.equals(KartenActivity.getInstance().getString(R.string.filter_standardprofil))&&presets.contains(preset))presets.remove(preset);
        SharedPreferences sharedPref = KartenActivity.getInstance().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor r = sharedPref.edit();

        r.remove(preset+"KOSTENLOS");
        r.remove(preset + "BARRIEREFREI");
        r.remove(preset + "open247");
        r.remove(preset + "PARKEN");
        r.remove(preset + "BESTAETIGT");
        r.remove(preset + "KEINESTOERUNG");
        r.remove(preset + "HOTELS");
        r.remove(preset + "RESTAURANT");
        r.remove(preset + "RESTAURANT");
        r.remove(preset + "STECKER");
        r.remove(preset + "VERBUND");
        r.remove(preset + "KARTEN");
        r.putString("currentPreset", "");

        r.putStringSet("PRESETS",presets);
        r.commit();
        if (PRESET.equals(preset))PRESET=KartenActivity.getInstance().getString(R.string.filter_standardprofil);

        lade_filter_db();


    }

    public static boolean filter_initialized(){
        if(LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG,"FilterInitialized: Stecker:"+(stecker_verfuegbar_API!=null?stecker_verfuegbar_API.size():"0")+
                                                        " Verbund:"+(verbund_verfuegbar_API!=null?verbund_verfuegbar_API.size():"0")+
                                                        " Karten:"+(karten_verfuegbar_API!=null?karten_verfuegbar_API.size():"0")+
                                                        " Filter:"+filter.toString()+
                                                        "MapReady: "+KartenActivity.isMapReady());

        boolean ret = stecker_verfuegbar_API!=null &&stecker_verfuegbar_API.size()>0
                &&verbund_verfuegbar_API!=null&&verbund_verfuegbar_API.size()>0
                &&karten_verfuegbar_API!=null&&karten_verfuegbar_API.size()>0
                &&filter!=null&&filter.size()>5
                &&KartenActivity.isMapReady();
        if(LogWorker.isVERBOSE())
            LogWorker.d(LOG_TAG,"filter initialized" + ret);
        return (ret);
    }



    private static void convert2API(){

        if (!stecker_verfuegbar_API.isEmpty())
            for (String s : stecker){
                if(!stecker_verfuegbar_API.contains(s)){
                    APIconverter(F_STECKER,s);
                }
            }

        if (!verbund_verfuegbar_API.isEmpty())
            for (String v : verbund){
                if(!verbund_verfuegbar_API.contains(v)){
                    APIconverter(F_VERBUND,v);
                }
            }

    }

    public static int listenlaenge (String Liste){

        if(Liste==F_KARTEN) return karten.size();
        if(Liste==F_STECKER) return stecker.size();
        if(Liste==F_VERBUND) return verbund.size();

        return 0;
    }

    private static void APIconverter(String Liste, String Value){
        Boolean CONVERTED = false;

       if(Liste==F_STECKER){
                            HashMap<String,String> converter = new HashMap<>();
                            converter.put("typ_2_","Typ2");
                            converter.put("typ_3_","Typ3");
                            converter.put("typ_1_","Typ1");
                            converter.put("typ_13","Typ13");
                            converter.put("typ_15","Typ15");
                            converter.put("typ_25","Typ25");
                          converter.put("schuko","Schuko");
                          converter.put("cee_rot","CEE Rot");
                          converter.put("cee_blau","CEE Blau");
                          converter.put("ceeplus","CEE+");
                          converter.put("hpc","Tesla HPC");
                          converter.put("supercharger","Tesla Supercharger");
                          converter.put("chademo","CHAdeMO");
                          converter.put("ccs","CCS");

           for (Map.Entry<String,String> e : converter.entrySet()){
               if (Value.contains(e.getKey())){
                   stecker.remove(Value);
                   stecker.add(e.getValue());
                   if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Convert "+Value +" zu "+e.getValue());
                   CONVERTED=true;
               }
           }

           if (!CONVERTED){
               stecker.remove(Value);
               if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Convert von"+Value +" für "+Liste + "fehlgeschlagen. Gelöscht.");
           }
        }


        if (Liste==F_VERBUND){
            CONVERTED=false;
            HashMap<String,String> converter = new HashMap<>();
            converter.put("A1-Telekom","A1 Telekom");converter.put("AAE-Naturstrom","AAE Naturstrom");converter.put("AESE","AESE");converter.put("aew-e-mobility","aew e-mobility");converter.put("Aggerenergie","Aggerenergie");converter.put("Agger-Energie","Agger Energie");converter.put("AkkuTour","Akku.Tour");converter.put("ALDI-Sued","ALDI Süd");converter.put("allego","allego");converter.put("Amersam","Amersam");converter.put("ARBOe","ARBÖ");converter.put("Arctic-Roads","Arctic Roads");converter.put("Auchan","Auchan");converter.put("Auto-Bleue","Auto Bleue");converter.put("autoPILDYK","autoPILDYK");converter.put("Be-Charged","Be Charged");converter.put("be-emobil","be emobil");converter.put("Bilkraft","Bilkraft");converter.put("Blue-Corner","Blue Corner");converter.put("Bosch","Bosch");converter.put("Bosch-Get-eReady","Bosch Get eReady");converter.put("BS-Energy","BS Energy");converter.put("CEZ","CEZ");converter.put("Charge-Fuel","Charge&amp;Fuel");converter.put("chargeIT-mobility","chargeIT mobility");converter.put("ChargeMaster","ChargeMaster");converter.put("ChargeNow","ChargeNow");converter.put("Chargepoint","Chargepoint");converter.put("charge-your-car","charge your car");converter.put("CleanCharge","CleanCharge");converter.put("Clever","Clever");converter.put("CNR","CNR");converter.put("Crome","Crome");converter.put("Crowdfunding","Crowdfunding");converter.put("CSDD","CSDD");converter.put("DEM","DEM");converter.put("Drehstromnetz","Drehstromnetz");converter.put("Due-Energie","Due Energie");converter.put("e-charge","e-charge");converter.put("e-GAP","e-GAP");converter.put("e-mobil-saar","e-mobil saar");converter.put("e-motion","e-motion");converter.put("e-moving","e-moving");converter.put("e-regio","e-regio");converter.put("e-SolCar","e-SolCar");converter.put("E-Wald","E-Wald");converter.put("E-WALD","E-WALD");converter.put("EON","E.ON");converter.put("EON-Ceska-Tschechien","E.ON Ceska (Tschechien)");converter.put("EON-DK","E.ON DK");converter.put("EAM","EAM");converter.put("easy4you","easy4you");converter.put("ebee","ebee");converter.put("ecar18","ecar18");converter.put("ECO-Fuel","ECO Fuel");converter.put("Ecotap","Ecotap");converter.put("ecotricity","ecotricity");converter.put("EE-Mobil","EE-Mobil");converter.put("eins","eins");converter.put("Electrodrive","Electrodrive");converter.put("ElectroDrive-Salzburg","ElectroDrive Salzburg");converter.put("ElectroDrive-Tirol","ElectroDrive Tirol");converter.put("Electromotive","Electromotive");converter.put("elektro-crpalka","elektro-crpalka");converter.put("Elektro-Celje","Elektro Celje");converter.put("ELLA","ELLA");converter.put("ELMO","ELMO");converter.put("EMiS","EMiS");converter.put("emma","emma");converter.put("eMORAIL","eMORAIL");converter.put("EnBW","EnBW");converter.put("endesa","endesa");converter.put("Enel-Drive","Enel Drive");converter.put("Energie-AG","Energie AG");converter.put("Energie-Burgenland","Energie Burgenland");converter.put("Energiedienst","Energiedienst");converter.put("Energie-Graz","Energie Graz");converter.put("Energieregion-Pyhrn-Priel","Energieregion Pyhrn-Priel");converter.put("Energie-Steiermark","Energie Steiermark");converter.put("ENI","ENI");converter.put("ENIO","ENIO");converter.put("enovos","enovos");converter.put("Enspirion","Enspirion");converter.put("ePoint","ePoint");converter.put("ESB-ecars","ESB ecars");converter.put("essent","essent");converter.put("Estonteco","Estonteco");converter.put("EV-Point","EV-Point");converter.put("EV-Box","EV Box");converter.put("Evite","Evite");converter.put("EVMapacz","EVMapa.cz");converter.put("EVN","EVN");converter.put("EVnetNL","EVnetNL");converter.put("evpass","evpass");converter.put("EV-Power","EV Power");converter.put("EWB","EWB");converter.put("EWE-SWB","EWE / SWB");converter.put("FairEnergie","FairEnergie");converter.put("Fastned","Fastned");converter.put("Feistritzwerke","Feistritzwerke");converter.put("Fenie-Energia","Fenie Energía");converter.put("Fortum","Fortum");converter.put("Freshmile","Freshmile");converter.put("Galactico","Galactico");converter.put("Garda-uno","Garda uno");converter.put("GGEW","GGEW");converter.put("Greenflux","Greenflux");converter.put("greenmotion","greenmotion");converter.put("Greenway","Greenway");converter.put("Greenway-Polska","Greenway Polska");converter.put("Gronn-Kontakt","Grønn Kontakt");converter.put("hastobe","has.to.be");converter.put("IAM","IAM");converter.put("IBIL","IBIL");converter.put("Ich-tanke-Strom","Ich tanke Strom");converter.put("inCharge","inCharge");converter.put("innogy-eRoaming","innogy eRoaming");converter.put("Inselwerke","Inselwerke");converter.put("Interparking","Interparking");converter.put("Kaufland","Kaufland");converter.put("Kelag","Kelag");converter.put("KiWhi","KiWhi");converter.put("Ladefoxx","Ladefoxx");converter.put("LADE-I-NORGE","LADE I NORGE");converter.put("Ladenetz","Ladenetz");converter.put("Ladenokkel","Ladenøkkel");converter.put("Ladeverbund-Franken","Ladeverbund Franken+");converter.put("landmobile","landmobile");converter.put("Latvenergo","Latvenergo");converter.put("Leclerc","Leclerc");converter.put("LGV-Network","LGV Network");converter.put("Lidl","Lidl");converter.put("Linz-AG","Linz AG");converter.put("LIVE-Barcelona","LIVE Barcelona");converter.put("LSW","LSW");converter.put("Lyse-Energi","Lyse Energi");converter.put("m-way","m-way");converter.put("Mainova","Mainova");converter.put("MaxBet","MaxBet");converter.put("MisterGreen","MisterGreen");converter.put("MOBIE","MOBI.E");converter.put("MobiSDEC","MobiSDEC");converter.put("Move","Move");converter.put("Muenchen-Umland","München Umland");converter.put("NewMotion","NewMotion");converter.put("Nomad-Power","Nomad Power");converter.put("Nuon","Nuon");converter.put("OPG-Center-Parking","OPG Center-Parking");converter.put("ORES","ORES");converter.put("ovag-Energie","ovag-Energie");converter.put("Parador","Parador");converter.put("Park-Charge","Park&amp;Charge");converter.put("PBW","PBW");converter.put("Petrol","Petrol");converter.put("PlugSurfing","PlugSurfing");converter.put("Plusdebornes","Plusdebornes");converter.put("Polyfazer","Polyfazer");converter.put("punihr","puni.hr");converter.put("REWAG","REWAG");converter.put("REWE","REWE");converter.put("RhoenEnergie","RhönEnergie");converter.put("Ricarica","Ricarica");converter.put("RiParTi","RiParTi");converter.put("Rotkaeppchenland","Rotkäppchenland");converter.put("Route220","Route220");converter.put("Schneller-Strom-tanken","Schneller Strom tanken");converter.put("Schnell-Laden-Berlin","Schnell Laden Berlin");converter.put("SDE28","SDE28");converter.put("SDEM","SDEM");converter.put("SIEIL","SIEIL");converter.put("Smatrics","Smatrics");converter.put("Sodetrel","Sodetrel");converter.put("Source-London","Source London");converter.put("Stadtwerke-Halle","Stadtwerke Halle");converter.put("Stadtwerke-Landshut","Stadtwerke Landshut");converter.put("Stadtwerke-Marburg","Stadtwerke Marburg");converter.put("Stromnetz-Hamburg","Stromnetz Hamburg");converter.put("StromTicket","StromTicket");converter.put("Sudstroum","Sudstroum");converter.put("SWD","SWD");converter.put("SW-Giessen","SW Gießen");converter.put("swisscharge","swisscharge");converter.put("SWL","SWL");converter.put("SW-Lauterbach","SW Lauterbach");converter.put("SW-Muenster","SW Münster");converter.put("SyDEV","SyDEV");converter.put("Tank-Rast","Tank&amp;Rast");converter.put("TANKE-EVN","TANKE EVN");converter.put("TankE-RheinEnergie","TankE RheinEnergie");converter.put("TANKE-WienEnergie","TANKE WienEnergie");converter.put("Tesla-Destination-Charging","Tesla Destination Charging");converter.put("Tesla-Supercharger","Tesla Supercharger");converter.put("texx-energy","texx energy");converter.put("ThePluginCompany","ThePluginCompany");converter.put("Thuega","Thüga");converter.put("TIWAG","TIWAG");converter.put("Tulln-Energie","Tulln Energie");converter.put("ubitricity","ubitricity");converter.put("Vattenfall","Vattenfall");converter.put("VelSar","VelSar");converter.put("Virta","Virta");converter.put("VLOTTE","VLOTTE");converter.put("Vmotion","Vmotion");converter.put("Voss-Energi","Voss Energi");converter.put("Wels-Strom","Wels Strom");converter.put("OeAMTC","ÖAMTC");
            for (Map.Entry<String,String> e : converter.entrySet()){
                if (Value.contains(e.getKey())){
                    verbund.remove(Value);
                    verbund.add(e.getValue());
                    if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Convert "+Value +" zu "+e.getValue());
                    CONVERTED=true;
                }
            }

            if (!CONVERTED){
                verbund.remove(Value);
                if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Convert von"+Value +" für "+Liste + "fehlgeschlagen. Gelöscht.");
            }
        }


        //Karten wurden bisher und in Zukunft an den ids identfiziert und sollten daher nicht konvertiert werden müssen
        //Trotzdem Konversion von Name zu ID versuchen

        if (Liste==F_KARTEN){
            CONVERTED=false;

             for (Map.Entry<Integer,String> e : karten_verfuegbar_API.entrySet()){
                if (Value.equals(e.getValue())){
                    karten.remove(Value);
                    karten.add(String.valueOf(e.getKey()));
                    if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Convert "+Value +" zu "+e.getKey());
                    CONVERTED=true;
                }
            }

            if (!CONVERTED){
                karten.remove(Value);
                if (LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"Convert von"+Value +" für "+Liste + "fehlgeschlagen. Gelöscht.");
            }
        }


    }

}
