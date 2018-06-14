package de.teammartens.android.wattfinder.model;

import android.content.res.Resources;

import org.json.JSONObject;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.worker.Utils;


public class ChargeEvent {
    private Integer chargepoint,provider;
    private Integer reason,source;
    private String nickname,comment,userid,entryId = "";
    private String plug;
    private boolean deleted, isfault = false;
    private Long timestamp, updatedAt,upstreamUpdatedAt = 0l;

    public ChargeEvent(Integer chargepoint, Integer provider, Integer reason, Integer source, String plug) {
        this.chargepoint = chargepoint;
        this.provider = provider;
        this.reason = reason;
        this.source = source;
        this.plug = plug;
    }

    public ChargeEvent(Integer chargepoint, Integer provider, Integer reason, Integer source) {
        this.chargepoint = chargepoint;
        this.provider = provider;
        this.reason = reason;
        this.source = source;
    }

    public ChargeEvent() {
        this.chargepoint = 0;
        this.provider = 0;
        this.reason = 100;
        this.source = 0;
        this.userid = "0";
        this.timestamp = System.currentTimeMillis()/1000;
        this.nickname="";
        this.comment="";
        this.deleted=false;
        this.isfault=false;
        this.updatedAt = System.currentTimeMillis()/1000;
        this.upstreamUpdatedAt = System.currentTimeMillis()/1000;


    }

    public boolean extractFromJSON(JSONObject jsonObject){

            this.chargepoint=jsonObject.optInt("chargepoint",-1);
            this.provider = 0;
            this.reason = jsonObject.optInt("reason",-1);
            this.source = jsonObject.optInt("source",-1);
            if(this.source>=0&&this.reason>=0&&this.chargepoint>0){
                this.entryId=jsonObject.optString("entryId","-1");

                this.plug = jsonObject.optString("plug","");
                this.nickname = jsonObject.optString("nickname","");
                this.comment = jsonObject.optString("comment","");
                this.deleted = jsonObject.optBoolean("deleted",false);
                this.isfault = jsonObject.optBoolean("isfault",false);
                this.userid = jsonObject.optString("userid","");
                this.timestamp = jsonObject.optLong("Timestamp",0);
                this.updatedAt = jsonObject.optLong("updatedAt",0);
                this.upstreamUpdatedAt = jsonObject.optLong("upstreamUpdatedAt",0);
                return true;
            }else{
                // not even enough basic infos in JSONObject
                return false;
            }

    }

    public Integer getChargepoint() {
        return chargepoint;
    }

    public void setChargepoint(Integer chargepoint) {
        this.chargepoint = chargepoint;
    }

    public Integer getProvider() {
        return provider;
    }

    public void setProvider(Integer provider) {
        this.provider = provider;
    }

    public Integer getReason() {
        return reason;
    }

    public String getReasonString() {
        Resources Res = KartenActivity.getInstance().getResources();

        String s = " Illeagal reason code.";
        if (reason>9&&reason<12) s=Res.getString(R.string.ce_result_success);
            else if (reason>99&&reason<103){
                String[] a = Res.getStringArray(R.array.ce_result_error);
                s=Res.getString(R.string.ce_result_error)+", "+a[reason-100];
            }
            else if (reason>199&&reason<204){
            String[] a = Res.getStringArray(R.array.ce_result_others);
            s=Res.getString(R.string.ce_result_others)+", "+a[reason-200];
        }


        return s;
    }


    public void setReason(Integer reason) {
        this.reason = reason;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public String getNickname() {
        return (nickname!=null?nickname:"");
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getComment() {
        return (comment!=null?comment:"");
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserid() {
        return (userid!=null?userid:"0");
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPlug() {
        return (plug!=null?plug:"");
    }

    public void setPlug(String plug) {
        this.plug = plug;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isIsfault() {
        return (isfault||(reason>99&&reason<200));
    }

    public void setIsfault(boolean isfault) {
        this.isfault = isfault;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getTimestampString() {
        return Utils.createDate(timestamp);
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUpstreamUpdatedAt() {
        return upstreamUpdatedAt;
    }

    public void setUpstreamUpdatedAt(Long upstreamUpdatedAt) {
        this.upstreamUpdatedAt = upstreamUpdatedAt;
    }

    public String getEntryId() {
        return (entryId!=null?entryId:"-1");
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }


}
