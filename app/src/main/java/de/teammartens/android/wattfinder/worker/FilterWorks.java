package de.teammartens.android.wattfinder.worker;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.model.FilterEintrag;
import de.teammartens.android.wattfinder.model.PresetEintrag;

//import org.acra.ACRA;
//import org.acra.ErrorReporter;



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
    public static final String PRESET_ALL = "ALLE";
    public static final Integer[] F_POWER_VALUE = {2, 6, 11, 20, 22, 43, 50, 100};
    private static ArrayList<Boolean> filter = new ArrayList<Boolean>();
    private static Integer filter_minpower = 0;
    private static Set<String> presets = new HashSet<String>();
    private static Set<String> stecker = new HashSet<String>();
    private static Set<String> verbund = new HashSet<String>();
    private static Set<String> karten = new HashSet<String>();
    private static Set<String> stecker_convertTemp = new HashSet<String>();//wird benötigt um der Concurrentmdificationexecption aus dem Weg zu gehen
    private static Set<String> verbund_convertTemp = new HashSet<String>();
    private static Set<String> karten_convertTemp = new HashSet<String>();


    public static Set<String> stecker_verfuegbar_API = new HashSet<String>();
    public static Set<String> verbund_verfuegbar_API = new HashSet<String>();
    public static HashMap<Integer, String> karten_verfuegbar_API = new HashMap<Integer, String>();

    private static String fUrl = "http://www.goingelectric.de/stromtankstellen/";
    private static String fAPIUrl_plugs = "https://api.goingelectric.de/chargepoints/pluglist/";
    private static String fAPIUrl_networks = "https://api.goingelectric.de/chargepoints/networklist/";
    private static String fAPIUrl_cards = "https://api.goingelectric.de/chargepoints/chargecardlist/";
    private static Long f_TIMESTAMP = 0l;
    private final static Long f_OUTDATED = 3600 * 12l;
    /*
    Gespeicherte Filtereinstellungen laden
     */


    public static void lade_filter_db() {


        SharedPreferences sPref = KartenActivity.sharedPref;
        if (PRESET == null)
            PRESET = sPref.getString("currentPreset", KartenActivity.getInstance().getString(R.string.filter_standardprofil));
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "PRESET:" + PRESET);
        if (PRESET.isEmpty())
            PRESET = KartenActivity.getInstance().getString(R.string.filter_standardprofil);

        presets = sPref.getStringSet("PRESETS", new HashSet<String>());

        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "presets:" + presets.toString());
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Aktuelles Profil:" + PRESET);
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

        filter_minpower = sPref.getInt(PRESET + "MINPOWER", 0);
        stecker.clear();
        stecker.addAll(sPref.getStringSet(PRESET + "STECKER", new HashSet<String>()));
        if (LogWorker.isVERBOSE())
            LogWorker.d(LOG_TAG, "Stecker aus DB geladen:" + stecker.toString());

        verbund = sPref.getStringSet(PRESET + "VERBUND", new HashSet<String>());



        karten = sPref.getStringSet(PRESET + "KARTEN", new HashSet<String>());
        if (LogWorker.isVERBOSE())
            LogWorker.d(LOG_TAG, "Karten aus DB geladen:" + karten.toString());
        //convert2API();


        f_TIMESTAMP = sPref.getLong("fTimestamp",0);
        if (!filter_initialized()) {
            stecker_verfuegbar_API = sPref.getStringSet("STECKER_API", new HashSet<String>());

            karten_verfuegbar_API = deserialize_karten(sPref.getStringSet("KARTEN_API", new HashSet<String>()));

            verbund_verfuegbar_API = sPref.getStringSet("VERBUND_API", new HashSet<String>());
        }
    }


    /*
   Listen für Stecker,Verbund, und Karten von goingelectric via JSON API laden
   12/2016
    */
    public static void refresh_filterlisten_API() {
        if ((System.currentTimeMillis() / 1000 - f_TIMESTAMP) > f_OUTDATED ||
                !filter_initialized() || NetWorker.getNetworkQuality()>1) {

            if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Refresh Filterlisten API");
            filter_API_request(F_STECKER);
        } else
            SaeulenWorks.checkMarkerCache("ladefilterAPI-nicht nötig");
    }

    public static void filter_API_request(final String Liste){
        SharedPreferences sPref = KartenActivity.sharedPref;
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Lade Filterlisten API - "+Liste);
        String params = "key=" + KartenActivity.getInstance().getString(R.string.GoingElectric_APIKEY);
        String fAPIUrl="";
        Set<String> sp_Cache = new HashSet<String>();
        switch (Liste) {
            case F_STECKER:
                fAPIUrl = fAPIUrl_plugs;
              //  sp_Cache = sPref.getStringSet("STECKER_API",new HashSet<String>());

                break;
            case F_VERBUND:
                fAPIUrl = fAPIUrl_networks;
               // sp_Cache = sPref.getStringSet("VERBUND_API",new HashSet<String>());

                break;
            case F_KARTEN:
                fAPIUrl = fAPIUrl_cards;
               // sp_Cache = sPref.getStringSet("KARTEN_API",new HashSet<String>());

                break;
        }
    //    final Set<String> sp_CacheF = sp_Cache;
        final Long requestTimeStart = System.currentTimeMillis();
        JsonObjectRequest filterReq = new JsonObjectRequest(Request.Method.GET, fAPIUrl + "?" + params, (String) null, new Response.Listener<JSONObject>() {
            @Override

            public void onResponse(JSONObject response) {
                if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "FilterAPIResponse " +Liste);
                //string=fixEncoding(string);
               if ((System.currentTimeMillis() - requestTimeStart)>5000){
                   NetWorker.setNetworkQuality(1);
               }
                if ((System.currentTimeMillis() - requestTimeStart)<2000){
                    NetWorker.rehabilateNetworkQuality();
                }
                try {

                    String status = response.getString("status");
                    Set<String> listeT = new HashSet<String>();
                    Set<String> listeAPI_hs = new HashSet<String>();
                    HashMap<Integer,String> listeAPI_hm = new HashMap<Integer,String>();
                    JSONArray JSON_LISTE = response.getJSONArray("result");
                    final int Listenlaenge = JSON_LISTE.length();
                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG,"FilterAPIRq "+Liste + ": "+ Listenlaenge + " Einträge gefunden");
                    if (Listenlaenge > 0) {
                        f_TIMESTAMP = System.currentTimeMillis() / 1000;


                        if(Liste==F_KARTEN){

                            JSONObject jO = new JSONObject();
                            for (int i = 0; i < Listenlaenge; i++) {
                                jO = JSON_LISTE.getJSONObject(i);
                                listeAPI_hm.put(jO.getInt("card_id"), jO.getString("name"));

                              //  if (LogWorker.isVERBOSE())  LogWorker.d(LOG_TAG, "Karte "+(i+1)+"/"+Listenlaenge+":" + jO.getString("name"));
                            }
                            if (listeAPI_hm.size()<Listenlaenge){
                                if (LogWorker.isVERBOSE())  LogWorker.d(LOG_TAG, "Liste Karten zu kurz!!!");
                                filter_API_request(F_KARTEN);
                            }
                            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,listeAPI_hm.size() + " gespeichert");

                        }
                        else{

                            for (int i = 0; i < Listenlaenge; i++) {
                                listeAPI_hs.add(JSON_LISTE.getString(i));
                                // if ( LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Verbund:" + Verbund.getString(i));
                            }
                            if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,listeAPI_hs.size() + " gespeichert");


                        }





                        /*if (!listeAPI.isEmpty())
                            listeT.addAll(verbund);
                        for (String v : listeT) {
                            if (!listeAPI.contains(v)) {
                                APIconverter(Liste/, v);
                            }
                        }*/

                        switch (Liste) {
                            case F_STECKER:

                                stecker_verfuegbar_API = listeAPI_hs;
                                filter_API_request(F_VERBUND);
                                break;
                            case F_VERBUND:
                                verbund_verfuegbar_API = listeAPI_hs;
                                filter_API_request(F_KARTEN);
                                break;
                            case F_KARTEN:
                                karten_verfuegbar_API = listeAPI_hm;
                                filter_liste_speichern();
                                break;
                        }
                        if (filter_initialized())
                            AnimationWorker.hideStartup();


                    } else {
                        if (LogWorker.isVERBOSE())
                            LogWorker.e(LOG_TAG, "Leere Antwort !!! API ReQUest "+ Liste);
                    }

                } catch (JSONException e) {
                    LogWorker.e(LOG_TAG, "JSONERROR:" + e.getMessage());
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                    NetWorker.setNetworkQuality(0);
                    switch (Liste) {
                        case F_STECKER:
                            filter_API_request(F_VERBUND);
                            break;
                        case F_VERBUND:
                            filter_API_request(F_KARTEN);
                            break;
                        case F_KARTEN:
                            break;
                    }

                NetWorker.handleError(error, NetWorker.TASK_FILTER,Liste);
            }

        });

        /*Request nur starten wenn Netzwerkverbindung einigermaßen gut ist, sonst nehemen wir die gespeicherten*/

        if(!filter_initialized()||NetWorker.getNetworkQuality()>1||(NetWorker.getNetworkQuality()==1&&(System.currentTimeMillis()/1000 - f_TIMESTAMP)>f_OUTDATED) ){

            //Wenn shcon irgendwelche Listen da sind dann begrenze de Versuche um nicht zuviel Netzwerkkapazität zu nehmen
            if (filter_initialized()&&NetWorker.getNetworkQuality()<3)
                filterReq.setRetryPolicy(new DefaultRetryPolicy(5000, 1,1));
         KartenActivity.getInstance().addToRequestQueue(filterReq);
         KartenActivity.incAPI_RQ_Count();}
        else {
            switch (Liste) {
                case F_STECKER:

                    filter_API_request(F_VERBUND);
                    break;
                case F_VERBUND:

                    filter_API_request(F_KARTEN);
                    break;
                case F_KARTEN:
                    
                    break;
            }
        }


    }
    /*
    Filtereinstellungen speichern
     */
    public static void filter_speichern() {

        //nicht specihern wenn neues Profil drin steht
        if (PRESET == null || PRESET.equals(KartenActivity.getInstance().getString(R.string.filter_neuesprofil)) || PRESET.isEmpty())
            return;


        SharedPreferences.Editor editor = KartenActivity.sharedPref.edit();
        editor.putBoolean(PRESET + "KOSTENLOS", filter.get(F_KOSTENLOS));
        editor.putBoolean(PRESET + "BARRIEREFREI", filter.get(F_BARRIEREFREI));
        editor.putBoolean(PRESET + "open247", filter.get(F_open247));
        editor.putBoolean(PRESET + "opennow", filter.get(F_opennow));
        editor.putBoolean(PRESET + "PARKEN", filter.get(F_PARKEN));
        editor.putBoolean(PRESET + "BESTAETIGT", filter.get(F_BESTAETIGT));
        editor.putBoolean(PRESET + "KEINESTOERUNG", filter.get(F_KEINESTOERUNG));
        editor.putBoolean(PRESET + "HOTELS", filter.get(F_HOTELS));
        editor.putBoolean(PRESET + "RESTAURANT", filter.get(F_RESTAURANT));
        editor.putInt(PRESET + "MINPOWER", filter_minpower);
        editor.putStringSet(PRESET + "STECKER", stecker);
        editor.putStringSet(PRESET + "VERBUND", verbund);
        editor.putStringSet(PRESET + "KARTEN", karten);
        filter_liste_speichern();
        editor.putString("currentPreset", PRESET);
        editor.putStringSet("PRESETS", presets);
        if (!editor.commit()) {

            Toast.makeText(KartenActivity.getInstance(), R.string.SaveFilterError, Toast.LENGTH_LONG).show();
            if (LogWorker.isVERBOSE())
                LogWorker.e(LOG_TAG, "FEHLER: Filter für Profil " + PRESET + " nicht gespeichert.");

        }
        if (LogWorker.isVERBOSE())
            LogWorker.d(LOG_TAG, "Filter für Profil " + PRESET + " gespeichert.");
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "FilterStecker:" + stecker.toString());
        if (LogWorker.isVERBOSE()) {
            Set<String> steckTEST = KartenActivity.sharedPref.getStringSet(PRESET + "STECKER", new HashSet<String>());
            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "Stecker in DB gespeichert:" + steckTEST.toString());
            Set<String> KartenTEST = KartenActivity.sharedPref.getStringSet(PRESET + "KARTEN", new HashSet<String>());
            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "KARTEN in DB gespeichert:" + KartenTEST.toString());
        }


    }


    public static void filter_liste_speichern(){
        SharedPreferences.Editor editor = KartenActivity.sharedPref.edit();

        editor.putStringSet("STECKER_API", stecker_verfuegbar_API);
        editor.putStringSet("VERBUND_API", verbund_verfuegbar_API);
        editor.putStringSet("KARTEN_API", serialize_karten());
        if (!editor.commit()) {

            Toast.makeText(KartenActivity.getInstance(), R.string.SaveFilterError, Toast.LENGTH_LONG).show();
            if (LogWorker.isVERBOSE())
                LogWorker.e(LOG_TAG, "FEHLER: FilterLISTEN nicht gespeichert.");

        }    }

    /*
    Helper um karten_API-available in den sharedPref speichern zu können
    */

    private static HashSet<String> serialize_karten(){
        HashSet<String> hs = new HashSet<String>();
        if(karten_verfuegbar_API!=null && karten_verfuegbar_API.size()>0)
        for (Map.Entry<Integer, String> e : karten_verfuegbar_API.entrySet()) {
            hs.add(e.getKey()+"||"+e.getValue());
        }

        return hs;
    }

        /*
    Helper um karten_API-available in den sharedPref speichern zu können
    */

    private static HashMap<Integer,String> deserialize_karten(Set<String> liste_karten){
        HashMap<Integer,String> hm= new HashMap<Integer,String>();
        if (liste_karten!=null && liste_karten.size()>0)
        for (String l:liste_karten
             ) {
            String[] split = l.split("||");
            hm.put(Integer.getInteger(split[0]),split[1]);
        }

        return hm;
    }

    /*
    Einen der Boolean-Filter Typen setzen und Ergebnis bekommen (zur Kontrolle)
     */
    public static boolean setze_filter(Integer F, Boolean Value) {

        if(filter.get(F)!=Value) {
            filter.set(F, Value);

            if (LogWorker.isVERBOSE())
                LogWorker.d(LOG_TAG, "Filter" + F + "(" + FilterInt2StrHelper(F) + ") gesetzt: " + filter.get(F));
            filter_speichern();
            SaeulenWorks.checkMarkerCache("setze Filter " + FilterInt2StrHelper(F));
        }
        return filter.get(F);


    }

    private static String FilterInt2StrHelper(int F) {
        switch (F) {
            case F_KOSTENLOS:
                return "F_KOSTENLOS";

            case F_BARRIEREFREI:
                return "F_BARRIEREFREI";

            case F_open247:
                return "F_open247";

            case F_opennow:
                return "F_opennow";

            case F_PARKEN:
                return "F_PARKEN";

            case F_BESTAETIGT:
                return "F_BESTAETIGT";

            case F_KEINESTOERUNG:
                return "F_KEINESTOERUNG";

            case F_HOTELS:
                return "F_HOTELS";

            case F_RESTAURANT:
                return "F_RESTAURANT";

        }


        return "";
    }

    public static int setze_power(Integer progress) {

        if(filter_minpower != F_POWER_VALUE[progress]){
            filter_minpower = F_POWER_VALUE[progress];
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Filter POWER gesetzt: " + filter_minpower);
        filter_speichern();}
        return filter_minpower;


    }

    public static void setze_power_fastcharge(boolean fast){
        if (fast){
            setze_power(5);
        }else{

            if (listenlaenge(F_STECKER)>0&&!lese_liste(F_STECKER,"Schuko")){
                setze_power(2);
            }  else{
                FilterWorks.setze_power(0);
            }
        }
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"setzeFastCharge: "+lese_minpower());
    }


    public static boolean lese_filter(Integer F) {
        if (LogWorker.isVERBOSE())
            LogWorker.d(LOG_TAG, " Lese Filter: " + F + "(" + FilterInt2StrHelper(F) + "):" + (filter!=null&&filter.size()>0&&F<filter.size()?filter.get(F):false));
        return (filter!=null&&filter.size()>0&&F<filter.size()?filter.get(F):false);


    }

    public static boolean lese_liste(String Liste, String Value) {

        switch (Liste) {
            case F_STECKER:
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "LeseFilterListe: " + Liste + ":" + Value + " " + stecker.contains(Value));
                return (stecker.contains(Value));

            case F_VERBUND:
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "LeseFilterListe: " + Liste + ":" + Value + " " + verbund.contains(Value));
                return (verbund.contains(Value));

            case F_KARTEN:
                //Erstmal Titel zu Id umsetzen
                String cardid = "0";
                if (karten_verfuegbar_API.containsValue(Value))
                    for (Map.Entry<Integer, String> e : karten_verfuegbar_API.entrySet()) {
                        if (e.getValue().equals(Value)) {
                            cardid = String.valueOf(e.getKey());
                            break;
                        }
                    }

                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "LeseFilterListe: " + Liste + ":" + Value + "(" + cardid + ")" + karten.contains(cardid));
                return karten.contains(cardid);

        }
        return false;
    }

    public static int lese_minpower() {
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Filter: POWER " + filter_minpower);
        return filter_minpower;


    }


    // Liste gleich komplett ändern
    public static boolean liste_aendern(String Liste, HashMap<String,Boolean> Value) {



        return false;
    }


    /*
    Einen Wert der Liste hinzufügen/löschen und Ergebnis bekommen (zur Kontrolle)
    */
    public static boolean liste_aendern(String Liste, String Value) {
        switch (Liste) {
            case F_STECKER:
                if (Value.equals(BELIEBIG)) {
                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "ListeAendern: SteckerFilter: BELIEBIG");
                    stecker.clear();
                    SaeulenWorks.checkMarkerCache("setze Liste "+Liste+" "+Value);
                    return true;
                }
                if (stecker.contains(Value)) stecker.remove(Value);
                else stecker.add(Value);
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "ListeAendern: SteckerFilter:" + Value + " = " + stecker.contains(Value));
                filter_speichern();
                if (stecker.contains(Value)){ SaeulenWorks.checkMarkerCache("setze Liste "+Liste+" "+Value);return true;}
                else return false;

            case F_VERBUND:
                if (Value.equals(BELIEBIG)) {
                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "ListeAendern: VerbundFilter: BELIEBIG");
                    verbund.clear();
                    SaeulenWorks.checkMarkerCache("setze Liste "+Liste+" "+Value);
                    return true;
                }

                if (verbund.contains(Value)) verbund.remove(Value);
                else verbund.add(Value);
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "ListeAendern: VerbundFilter:" + Value + " = " + verbund.contains(Value));
                filter_speichern();
                if (verbund.contains(Value)) {        SaeulenWorks.checkMarkerCache("setze Liste "+Liste+" "+Value);
                    return true;}
                else return false;

            case F_KARTEN:
                //Erstmal Titel zu Id umsetzen
                String cardid = "0";
                if (Value.equals(BELIEBIG)) {
                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "ListeAendern: KartenFilter: BELIEBIG");
                    karten.clear();
                    SaeulenWorks.checkMarkerCache("setze Liste "+Liste+" "+Value);
                    return true;
                }


                if (karten_verfuegbar_API.containsValue(Value))
                    for (Map.Entry<Integer, String> e : karten_verfuegbar_API.entrySet()) {
                        if (e.getValue().equals(Value)) {
                            cardid = String.valueOf(e.getKey());
                            break;
                        }
                    }
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "ListeAendern: KartenFilter ändern:" + cardid + " = bisher " + karten.contains(cardid));
                if (karten.contains(cardid)) karten.remove(cardid);
                else karten.add(cardid);
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "ListeAendern: KartenFilter geändert:" + cardid + " = " + karten.contains(cardid));
                filter_speichern();
                SaeulenWorks.checkMarkerCache("setze Liste "+Liste+" "+Value);

                return karten.contains(cardid);

        }
        return false;
    }

    public static boolean liste_aendern(String Liste, String Value, Boolean add) {
        switch (Liste) {
            case F_STECKER:
                if (Value.equals(BELIEBIG)) {
                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "ListeAendern: SteckerFilter: BELIEBIG");
                    stecker.clear();
                    SaeulenWorks.checkMarkerCache("setze Liste "+Liste+" "+Value);
                    return true;
                }

                if((!add&&stecker.contains(Value))||(add&&!stecker.contains(Value))) {
                    if (!add && stecker.contains(Value)) stecker.remove(Value);
                    else {
                        if (add && !stecker.contains(Value)) stecker.add(Value);
                    }

                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "ListeAendern: SteckerFilter:" + Value + " = " + stecker.contains(Value));
                    filter_speichern();
                    SaeulenWorks.checkMarkerCache("setze Liste " + Liste + " " + Value);

                        return stecker.contains(Value);
                }

            case F_VERBUND:
                if (Value.equals(BELIEBIG)) {
                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "ListeAendern: VerbundFilter: BELIEBIG");
                    verbund.clear();
                    SaeulenWorks.checkMarkerCache("setze Liste "+Liste+" "+Value);
                    return true;
                }

                if (verbund.contains(Value)) verbund.remove(Value);
                else verbund.add(Value);
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "ListeAendern: VerbundFilter:" + Value + " = " + verbund.contains(Value));
                filter_speichern();
                if (verbund.contains(Value)) {        SaeulenWorks.checkMarkerCache("setze Liste "+Liste+" "+Value);
                    return true;}
                else return false;

            case F_KARTEN:
                //Erstmal Titel zu Id umsetzen
                String cardid = "0";
                if (Value.equals(BELIEBIG)) {
                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "ListeAendern: KartenFilter: BELIEBIG");
                    karten.clear();
                    SaeulenWorks.checkMarkerCache("setze Liste "+Liste+" "+Value);
                    return true;
                }


                if (karten_verfuegbar_API.containsValue(Value))
                    for (Map.Entry<Integer, String> e : karten_verfuegbar_API.entrySet()) {
                        if (e.getValue().equals(Value)) {
                            cardid = String.valueOf(e.getKey());
                            break;
                        }
                    }
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "ListeAendern: KartenFilter ändern:" + cardid + " = bisher " + karten.contains(cardid));
                if (karten.contains(cardid)) karten.remove(cardid);
                else karten.add(cardid);
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "ListeAendern: KartenFilter geändert:" + cardid + " = " + karten.contains(cardid));
                filter_speichern();
                SaeulenWorks.checkMarkerCache("setze Liste "+Liste+" "+Value);

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
    public static Boolean Liste_beliebig(String Liste) {

        switch (Liste) {
            case F_STECKER:
                return (stecker.size() > 0);


            case F_VERBUND:
                return (verbund.size() > 0);



            case F_KARTEN:
                return (karten.size() > 0);



        }
        return false;

    }


    public static Boolean setListeBeliebig(String Liste) {

        switch (Liste) {
            case F_STECKER:
                stecker.clear();
                return (stecker.size() > 0);


            case F_VERBUND:
                verbund.clear();
                return (verbund.size() > 0);


            case F_KARTEN:
                karten.clear();
                return (karten.size() > 0);


        }
        return false;

    }


    public static String ParamListe() {

        String param = "";


        if (verbund.isEmpty()) param += "verbund[]=alle";
        else {
            StringBuilder vb = new StringBuilder();
            for (String v : verbund) {
                if (vb.length() > 0) vb.append("&");
                vb.append("verbund[]=").append(v);
            }

            param += vb;
        }
        param += "&";
        if (stecker.isEmpty()) param = "stecker[]=alle";
        else {
            StringBuilder st = new StringBuilder();
            for (String s : stecker) {
                if (st.length() > 0) st.append("&");
                st.append("stecker[]=").append(s);
            }
            param += st;
        }
        param += "&";
        if (karten.isEmpty()) param += "ladekarte[]=alle";
        else {
            StringBuilder kt = new StringBuilder();
            for (String k : karten) {
                if (kt.length() == 0) kt.append("&");
                kt.append("ladekarte[]=").append(k);
            }
            param += kt;
        }
        param = URLEncoder.encode(param);
        param = param.replaceAll("%3D", "=").replaceAll("%26", "&");
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "ParamList:" + param);
        return param;
    }

    public static Map<String, String> ParamFilter() {

        Map<String, String> params = new HashMap<String, String>();
        params.put("typ", "SucheZoom");
        if (verbund.isEmpty()) params.put("verbund[]", "alle");
        else {
            int vi = 0;
            for (String v : verbund) {

                params.put("verbund[" + vi + "]", v);
                vi++;
            }
        }
        if (stecker.isEmpty()) params.put("stecker[]", "alle");
        else {
            int vi = 0;
            for (String v : stecker) {

                params.put("stecker[" + vi + "]", v);
                vi++;
            }
        }
        if (karten.isEmpty()) params.put("ladekarte[]", "alle");
        else {
            int vi = 0;
            for (String v : karten) {

                params.put("ladekarte[" + vi + "]", v);
                vi++;
            }
        }

        if (filter != null && filter.size() > 0) {
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


    public static String ParamFilterString() {
        String params = "";

        try {
            if (stecker != null && stecker.size() > 0)
                params += "plugs=" + URLEncoder.encode(TextUtils.join(",", stecker), "UTF-8") + "&";

            if (verbund != null && verbund.size() > 0)
                params += "networks=" + URLEncoder.encode(TextUtils.join(",", verbund), "UTF-8") + "&";

            if (karten != null && karten.size() > 0)
                params += "chargecards=" + URLEncoder.encode(TextUtils.join(",", karten), "UTF-8") + "&";

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (filter != null && filter.size() > 0) {
            params += "min_power=" + lese_minpower() + "&";
            params += "freecharging=" + filter.get(F_KOSTENLOS).toString() + "&";
            params += "freeparking=" + filter.get(F_PARKEN).toString() + "&";

            if (filter.get(F_opennow))
                params += "open_now=" + filter.get(F_opennow).toString() + "&";
            else if (filter.get(F_open247))
                params += "open_twentyfourseven=" + filter.get(F_open247).toString() + "&";
            else
                params += "open_twentyfourseven=" + filter.get(F_open247).toString() + "&" + "open_now=" + filter.get(F_opennow).toString() + "&";


            params += "verified=" + filter.get(F_BESTAETIGT).toString() + "&";
            params += "barrierfree=" + filter.get(F_BARRIEREFREI).toString() + "&";


            params += "exclude_faults=" + filter.get(F_KEINESTOERUNG).toString();

        }


        return params;


    }

    public static int paramsHash() {
        String params = "";

        try {
            if (stecker != null && stecker.size() > 0)
                params += "plugs=" + URLEncoder.encode(TextUtils.join(",", stecker), "UTF-8") + "&";

            if (verbund != null && verbund.size() > 0)
                params += "networks=" + URLEncoder.encode(TextUtils.join(",", verbund), "UTF-8") + "&";

            if (karten != null && karten.size() > 0)
                params += "cards=" + URLEncoder.encode(TextUtils.join(",", karten), "UTF-8") + "&";

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (filter != null && filter.size() > 0) {
            params += "min_power=" + lese_minpower() + "&";
            params += "freecharging=" + filter.get(F_KOSTENLOS).toString() + "&";
            params += "freeparking=" + filter.get(F_PARKEN).toString() + "&";

            if (filter.get(F_opennow))
                params += "open_now=" + filter.get(F_opennow).toString() + "&";
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



    public static String[] ListetoArray(String Liste){



        switch (Liste) {
            case FilterWorks.F_STECKER:
                        return (String[]) stecker_verfuegbar_API.toArray();


            case FilterWorks.F_KARTEN:
                       return (String[]) karten_verfuegbar_API.values().toArray();

            default:
                return  (String[]) stecker_verfuegbar_API.toArray();

        }


        }

    public static ArrayList<FilterEintrag> ListeToArrayList(String Liste) {

        ArrayList<FilterEintrag> filterListe = new ArrayList<FilterEintrag>();

        switch (Liste) {
            case FilterWorks.F_STECKER:

                for (String v : stecker_verfuegbar_API) {
                    if (v != null && !v.isEmpty()) {
                        filterListe.add(new FilterEintrag(v, stecker.contains(v)));
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, "Stecker:" + v + ";" + stecker.contains(v));
                    }
                }
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, stecker_verfuegbar_API.size() + "/" + filterListe.size() + " Stecker verfügbar");
                break;

            case FilterWorks.F_VERBUND:
                for (String v : verbund_verfuegbar_API) {
                    filterListe.add(new FilterEintrag(v, verbund.contains(v)));

                }
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, verbund_verfuegbar_API.size() + " Verbünde verfügbar");
                break;

            case FilterWorks.F_KARTEN:
                for (Map.Entry<Integer, String> e : karten_verfuegbar_API.entrySet()) {
                    filterListe.add(new FilterEintrag(e.getValue(), karten.contains(String.valueOf(e.getKey()))));

                }
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, karten_verfuegbar_API.size() + " Karten verfügbar");
                break;


        }
        Collections.sort(filterListe);

        if (LogWorker.isVERBOSE())
            LogWorker.d(LOG_TAG, "FilterListeToArray " + Liste + " mit " + filterListe.size() + " Einträge verfügbar");
        return filterListe;
    }

    public static ArrayList<PresetEintrag> PresetArrayList() {

        ArrayList<PresetEintrag> filterListe = new ArrayList<PresetEintrag>();


        for (String v : presets) {
            filterListe.add(new PresetEintrag(v, v.equals(PRESET)));
        }
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, presets.size() + " Presets verfügbar");


        Collections.sort(filterListe, new Comparator<PresetEintrag>() {
            @Override
            public int compare(PresetEintrag fE1, PresetEintrag fE2) {

                return fE1.getTitel().toLowerCase().compareTo(fE2.getTitel().toLowerCase());
            }
        });

        return filterListe;
    }


    public static void loadPresets(String preset) {
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Lade Preset " + preset);
        if (preset.equals(PRESET))
            return;
        filter_speichern();
        PRESET = preset;
        lade_filter_db();
        //FilterFragment.setPreset();

    }

    private static void reset_alle() {
        filter.clear();
        filter.add(false);
        filter.add(false);
        filter.add(false);
        filter.add(false);
        filter.add(false);
        filter.add(false);
        filter.add(false);
        filter.add(false);
        filter.add(false);

        filter_minpower = 0;
        stecker = new HashSet<String>();
        verbund = new HashSet<String>();
        karten = new HashSet<String>();
    }

    public static void renamePreset(String old, String neu) {
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Rename Preset " + old + " zu " + neu);
        if (neu.isEmpty() || presets.contains(neu) || neu.equals(KartenActivity.getInstance().getString(R.string.filter_neuesprofil))) {


            return;
        }
        old = old.trim();
        neu = neu.trim();
        if (!old.isEmpty() && presets.contains(old)) {
            //loadPresets(old);
            presets.add(neu);
            PRESET = neu;
            filter_speichern();
            clearPreset(old);

        } else {
            //Dann sollte es neu sein
            presets.add(neu);
            PRESET = neu;
            filter_speichern();
            lade_filter_db();
        }

    }

    public static void clearPreset() {
        clearPreset(PRESET);
    }

    public static void clearPreset(String preset) {
        if (!preset.equals(KartenActivity.getInstance().getString(R.string.filter_standardprofil)) && presets.contains(preset))
            presets.remove(preset);
        SharedPreferences sharedPref = KartenActivity.getInstance().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor r = sharedPref.edit();

        r.remove(preset + "KOSTENLOS");
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

        r.putStringSet("PRESETS", presets);
        r.commit();
        if (PRESET.equals(preset))
            PRESET = KartenActivity.getInstance().getString(R.string.filter_standardprofil);

        lade_filter_db();


    }

    public static boolean filter_initialized() {
        if (LogWorker.isVERBOSE())
            LogWorker.d(LOG_TAG, "FilterInitialized: Stecker:" + (stecker_verfuegbar_API != null ? stecker_verfuegbar_API.size() : "0") +
                    " Verbund:" + (verbund_verfuegbar_API != null ? verbund_verfuegbar_API.size() : "0") +
                    " Karten:" + (karten_verfuegbar_API != null ? karten_verfuegbar_API.size() : "0") +
                    " Filter:" + filter.toString() +
                    "MapReady: " + KartenActivity.isMapReady());


        if (filter!=null && filter.size()<5) lade_filter_db();

        boolean ret = stecker_verfuegbar_API != null && stecker_verfuegbar_API.size() > 0
                && verbund_verfuegbar_API != null && verbund_verfuegbar_API.size() > 0
                && karten_verfuegbar_API != null && karten_verfuegbar_API.size() > 0
                && filter != null && filter.size() > 5
                && KartenActivity.isMapReady();
        if (LogWorker.isVERBOSE())
            LogWorker.d(LOG_TAG, "filter initialized" + ret);

        if (ret) AnimationWorker.hideStartup();
        return (ret);
    }


    private static void convert2API() {
        try {
            if (!stecker_verfuegbar_API.isEmpty()) {
                stecker_convertTemp.clear();
                stecker_convertTemp.addAll(stecker);
                for (String s : stecker) {
                    if (!stecker_verfuegbar_API.contains(s)) {
                        APIconverter(F_STECKER, s);
                    }
                }
                stecker.clear();
                stecker.addAll(stecker_convertTemp);
            }


            if (!verbund_verfuegbar_API.isEmpty()) {
                verbund_convertTemp.clear();
                verbund_convertTemp.addAll(verbund);
                for (String v : verbund) {
                    if (!verbund_verfuegbar_API.contains(v)) {
                        APIconverter(F_VERBUND, v);
                    }
                }
                verbund.clear();
                verbund.addAll(verbund_convertTemp);
            }

        } catch (ConcurrentModificationException e) {
            LogWorker.e(LOG_TAG, e.getLocalizedMessage());
        }

    }

    public static int listenlaenge(String Liste) {

        if (Liste == F_KARTEN) return karten.size();
        if (Liste == F_STECKER) return stecker.size();
        if (Liste == F_VERBUND) return verbund.size();

        return 0;
    }

    private static void APIconverter(String Liste, String Value) {
        Boolean CONVERTED = false;
        try {
            if (Liste == F_STECKER) {
                HashMap<String, String> converter = new HashMap<>();
                converter.put("typ_2_", "Typ2");
                converter.put("typ_3_", "Typ3");
                converter.put("typ_1_", "Typ1");
                converter.put("typ_13", "Typ13");
                converter.put("typ_15", "Typ15");
                converter.put("typ_25", "Typ25");
                converter.put("schuko", "Schuko");
                converter.put("cee_rot", "CEE Rot");
                converter.put("cee_blau", "CEE Blau");
                converter.put("ceeplus", "CEE+");
                converter.put("hpc", "Tesla HPC");
                converter.put("supercharger", "Tesla Supercharger");
                converter.put("chademo", "CHAdeMO");
                converter.put("ccs", "CCS");

                for (Map.Entry<String, String> e : converter.entrySet()) {
                    if (Value.contains(e.getKey())) {
                        stecker_convertTemp.remove(Value);
                        stecker_convertTemp.add(e.getValue());
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, "Convert " + Value + " zu " + e.getValue());
                        CONVERTED = true;
                    }
                }

                if (!CONVERTED) {
                    stecker_convertTemp.remove(Value);
                    if (LogWorker.isVERBOSE())
                        LogWorker.e(LOG_TAG, "Convert von" + Value + " für " + Liste + "fehlgeschlagen. Gelöscht.");
                }
            }


            if (Liste == F_VERBUND) {
                CONVERTED = false;
                HashMap<String, String> converter = new HashMap<>();
                converter.put("A1-Telekom", "A1 Telekom");
                converter.put("AAE-Naturstrom", "AAE Naturstrom");
                converter.put("AESE", "AESE");
                converter.put("aew-e-mobility", "aew e-mobility");
                converter.put("Aggerenergie", "Aggerenergie");
                converter.put("Agger-Energie", "Agger Energie");
                converter.put("AkkuTour", "Akku.Tour");
                converter.put("ALDI-Sued", "ALDI Süd");
                converter.put("allego", "allego");
                converter.put("Amersam", "Amersam");
                converter.put("ARBOe", "ARBÖ");
                converter.put("Arctic-Roads", "Arctic Roads");
                converter.put("Auchan", "Auchan");
                converter.put("Auto-Bleue", "Auto Bleue");
                converter.put("autoPILDYK", "autoPILDYK");
                converter.put("Be-Charged", "Be Charged");
                converter.put("be-emobil", "be emobil");
                converter.put("Bilkraft", "Bilkraft");
                converter.put("Blue-Corner", "Blue Corner");
                converter.put("Bosch", "Bosch");
                converter.put("Bosch-Get-eReady", "Bosch Get eReady");
                converter.put("BS-Energy", "BS Energy");
                converter.put("CEZ", "CEZ");
                converter.put("Charge-Fuel", "Charge&amp;Fuel");
                converter.put("chargeIT-mobility", "chargeIT mobility");
                converter.put("ChargeMaster", "ChargeMaster");
                converter.put("ChargeNow", "ChargeNow");
                converter.put("Chargepoint", "Chargepoint");
                converter.put("charge-your-car", "charge your car");
                converter.put("CleanCharge", "CleanCharge");
                converter.put("Clever", "Clever");
                converter.put("CNR", "CNR");
                converter.put("Crome", "Crome");
                converter.put("Crowdfunding", "Crowdfunding");
                converter.put("CSDD", "CSDD");
                converter.put("DEM", "DEM");
                converter.put("Drehstromnetz", "Drehstromnetz");
                converter.put("Due-Energie", "Due Energie");
                converter.put("e-charge", "e-charge");
                converter.put("e-GAP", "e-GAP");
                converter.put("e-mobil-saar", "e-mobil saar");
                converter.put("e-motion", "e-motion");
                converter.put("e-moving", "e-moving");
                converter.put("e-regio", "e-regio");
                converter.put("e-SolCar", "e-SolCar");
                converter.put("E-Wald", "E-Wald");
                converter.put("E-WALD", "E-WALD");
                converter.put("EON", "E.ON");
                converter.put("EON-Ceska-Tschechien", "E.ON Ceska (Tschechien)");
                converter.put("EON-DK", "E.ON DK");
                converter.put("EAM", "EAM");
                converter.put("easy4you", "easy4you");
                converter.put("ebee", "ebee");
                converter.put("ecar18", "ecar18");
                converter.put("ECO-Fuel", "ECO Fuel");
                converter.put("Ecotap", "Ecotap");
                converter.put("ecotricity", "ecotricity");
                converter.put("EE-Mobil", "EE-Mobil");
                converter.put("eins", "eins");
                converter.put("Electrodrive", "Electrodrive");
                converter.put("ElectroDrive-Salzburg", "ElectroDrive Salzburg");
                converter.put("ElectroDrive-Tirol", "ElectroDrive Tirol");
                converter.put("Electromotive", "Electromotive");
                converter.put("elektro-crpalka", "elektro-crpalka");
                converter.put("Elektro-Celje", "Elektro Celje");
                converter.put("ELLA", "ELLA");
                converter.put("ELMO", "ELMO");
                converter.put("EMiS", "EMiS");
                converter.put("emma", "emma");
                converter.put("eMORAIL", "eMORAIL");
                converter.put("EnBW", "EnBW");
                converter.put("endesa", "endesa");
                converter.put("Enel-Drive", "Enel Drive");
                converter.put("Energie-AG", "Energie AG");
                converter.put("Energie-Burgenland", "Energie Burgenland");
                converter.put("Energiedienst", "Energiedienst");
                converter.put("Energie-Graz", "Energie Graz");
                converter.put("Energieregion-Pyhrn-Priel", "Energieregion Pyhrn-Priel");
                converter.put("Energie-Steiermark", "Energie Steiermark");
                converter.put("ENI", "ENI");
                converter.put("ENIO", "ENIO");
                converter.put("enovos", "enovos");
                converter.put("Enspirion", "Enspirion");
                converter.put("ePoint", "ePoint");
                converter.put("ESB-ecars", "ESB ecars");
                converter.put("essent", "essent");
                converter.put("Estonteco", "Estonteco");
                converter.put("EV-Point", "EV-Point");
                converter.put("EV-Box", "EV Box");
                converter.put("Evite", "Evite");
                converter.put("EVMapacz", "EVMapa.cz");
                converter.put("EVN", "EVN");
                converter.put("EVnetNL", "EVnetNL");
                converter.put("evpass", "evpass");
                converter.put("EV-Power", "EV Power");
                converter.put("EWB", "EWB");
                converter.put("EWE-SWB", "EWE / SWB");
                converter.put("FairEnergie", "FairEnergie");
                converter.put("Fastned", "Fastned");
                converter.put("Feistritzwerke", "Feistritzwerke");
                converter.put("Fenie-Energia", "Fenie Energía");
                converter.put("Fortum", "Fortum");
                converter.put("Freshmile", "Freshmile");
                converter.put("Galactico", "Galactico");
                converter.put("Garda-uno", "Garda uno");
                converter.put("GGEW", "GGEW");
                converter.put("Greenflux", "Greenflux");
                converter.put("greenmotion", "greenmotion");
                converter.put("Greenway", "Greenway");
                converter.put("Greenway-Polska", "Greenway Polska");
                converter.put("Gronn-Kontakt", "Grønn Kontakt");
                converter.put("hastobe", "has.to.be");
                converter.put("IAM", "IAM");
                converter.put("IBIL", "IBIL");
                converter.put("Ich-tanke-Strom", "Ich tanke Strom");
                converter.put("inCharge", "inCharge");
                converter.put("innogy-eRoaming", "innogy eRoaming");
                converter.put("Inselwerke", "Inselwerke");
                converter.put("Interparking", "Interparking");
                converter.put("Kaufland", "Kaufland");
                converter.put("Kelag", "Kelag");
                converter.put("KiWhi", "KiWhi");
                converter.put("Ladefoxx", "Ladefoxx");
                converter.put("LADE-I-NORGE", "LADE I NORGE");
                converter.put("Ladenetz", "Ladenetz");
                converter.put("Ladenokkel", "Ladenøkkel");
                converter.put("Ladeverbund-Franken", "Ladeverbund Franken+");
                converter.put("landmobile", "landmobile");
                converter.put("Latvenergo", "Latvenergo");
                converter.put("Leclerc", "Leclerc");
                converter.put("LGV-Network", "LGV Network");
                converter.put("Lidl", "Lidl");
                converter.put("Linz-AG", "Linz AG");
                converter.put("LIVE-Barcelona", "LIVE Barcelona");
                converter.put("LSW", "LSW");
                converter.put("Lyse-Energi", "Lyse Energi");
                converter.put("m-way", "m-way");
                converter.put("Mainova", "Mainova");
                converter.put("MaxBet", "MaxBet");
                converter.put("MisterGreen", "MisterGreen");
                converter.put("MOBIE", "MOBI.E");
                converter.put("MobiSDEC", "MobiSDEC");
                converter.put("Move", "Move");
                converter.put("Muenchen-Umland", "München Umland");
                converter.put("NewMotion", "NewMotion");
                converter.put("Nomad-Power", "Nomad Power");
                converter.put("Nuon", "Nuon");
                converter.put("OPG-Center-Parking", "OPG Center-Parking");
                converter.put("ORES", "ORES");
                converter.put("ovag-Energie", "ovag-Energie");
                converter.put("Parador", "Parador");
                converter.put("Park-Charge", "Park&amp;Charge");
                converter.put("PBW", "PBW");
                converter.put("Petrol", "Petrol");
                converter.put("PlugSurfing", "PlugSurfing");
                converter.put("Plusdebornes", "Plusdebornes");
                converter.put("Polyfazer", "Polyfazer");
                converter.put("punihr", "puni.hr");
                converter.put("REWAG", "REWAG");
                converter.put("REWE", "REWE");
                converter.put("RhoenEnergie", "RhönEnergie");
                converter.put("Ricarica", "Ricarica");
                converter.put("RiParTi", "RiParTi");
                converter.put("Rotkaeppchenland", "Rotkäppchenland");
                converter.put("Route220", "Route220");
                converter.put("Schneller-Strom-tanken", "Schneller Strom tanken");
                converter.put("Schnell-Laden-Berlin", "Schnell Laden Berlin");
                converter.put("SDE28", "SDE28");
                converter.put("SDEM", "SDEM");
                converter.put("SIEIL", "SIEIL");
                converter.put("Smatrics", "Smatrics");
                converter.put("Sodetrel", "Sodetrel");
                converter.put("Source-London", "Source London");
                converter.put("Stadtwerke-Halle", "Stadtwerke Halle");
                converter.put("Stadtwerke-Landshut", "Stadtwerke Landshut");
                converter.put("Stadtwerke-Marburg", "Stadtwerke Marburg");
                converter.put("Stromnetz-Hamburg", "Stromnetz Hamburg");
                converter.put("StromTicket", "StromTicket");
                converter.put("Sudstroum", "Sudstroum");
                converter.put("SWD", "SWD");
                converter.put("SW-Giessen", "SW Gießen");
                converter.put("swisscharge", "swisscharge");
                converter.put("SWL", "SWL");
                converter.put("SW-Lauterbach", "SW Lauterbach");
                converter.put("SW-Muenster", "SW Münster");
                converter.put("SyDEV", "SyDEV");
                converter.put("Tank-Rast", "Tank&amp;Rast");
                converter.put("TANKE-EVN", "TANKE EVN");
                converter.put("TankE-RheinEnergie", "TankE RheinEnergie");
                converter.put("TANKE-WienEnergie", "TANKE WienEnergie");
                converter.put("Tesla-Destination-Charging", "Tesla Destination Charging");
                converter.put("Tesla-Supercharger", "Tesla Supercharger");
                converter.put("texx-energy", "texx energy");
                converter.put("ThePluginCompany", "ThePluginCompany");
                converter.put("Thuega", "Thüga");
                converter.put("TIWAG", "TIWAG");
                converter.put("Tulln-Energie", "Tulln Energie");
                converter.put("ubitricity", "ubitricity");
                converter.put("Vattenfall", "Vattenfall");
                converter.put("VelSar", "VelSar");
                converter.put("Virta", "Virta");
                converter.put("VLOTTE", "VLOTTE");
                converter.put("Vmotion", "Vmotion");
                converter.put("Voss-Energi", "Voss Energi");
                converter.put("Wels-Strom", "Wels Strom");
                converter.put("OeAMTC", "ÖAMTC");
                for (Map.Entry<String, String> e : converter.entrySet()) {
                    if (Value.contains(e.getKey())) {
                        verbund_convertTemp.remove(Value);
                        verbund_convertTemp.add(e.getValue());
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, "Convert " + Value + " zu " + e.getValue());
                        CONVERTED = true;
                    }
                }

                if (!CONVERTED) {
                    verbund_convertTemp.remove(Value);
                    if (LogWorker.isVERBOSE())
                        LogWorker.e(LOG_TAG, "Convert von" + Value + " für " + Liste + "fehlgeschlagen. Gelöscht.");
                }
            }


            //Karten wurden bisher und in Zukunft an den ids identfiziert und sollten daher nicht konvertiert werden müssen
            //Trotzdem Konversion von Name zu ID versuchen

            if (Liste == F_KARTEN) {
                CONVERTED = false;

                for (Map.Entry<Integer, String> e : karten_verfuegbar_API.entrySet()) {
                    if (Value.equals(e.getValue())) {
                        karten_convertTemp.remove(Value);
                        karten_convertTemp.add(String.valueOf(e.getKey()));
                        if (LogWorker.isVERBOSE())
                            LogWorker.d(LOG_TAG, "Convert " + Value + " zu " + e.getKey());
                        CONVERTED = true;
                    }
                }

                if (!CONVERTED) {
                    karten_convertTemp.remove(Value);
                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "Convert von" + Value + " für " + Liste + "fehlgeschlagen. Gelöscht.");
                }
            }


        } catch (ConcurrentModificationException e) {
            LogWorker.e(LOG_TAG, e.getLocalizedMessage());
        }

    }
}
