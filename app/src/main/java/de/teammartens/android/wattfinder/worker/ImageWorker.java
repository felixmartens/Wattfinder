package de.teammartens.android.wattfinder.worker;

import android.graphics.Bitmap;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.util.HashSet;
import java.util.Set;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.model.ImagePagerAdapter;

/**
 * Created by felix on 12.02.17.
 */

public class ImageWorker {
    protected View view;
    private static final String LOG_TAG = "ImageWorker";
    public static ImagePagerAdapter imgAdapter,imgAdapterHD;
    private static Bitmap[] ImgBitmaps = new Bitmap[5];
    private static Bitmap[] ImgBitmaps_HD = new Bitmap[5];
    private static Integer[] ImgIDs = new Integer[5];
    private static Integer ImgCount;
    private static Integer ImgIndex;
    private static Set<Integer> loading = new HashSet<Integer>();
    private static final String PhotoUrl = "https://api.goingelectric.de/chargepoints/photo/";
    private static final Integer Size_noHD = 800;
    private static final Integer Size_HD = 1600;


    public static ImagePagerAdapter getImgAdapter() {
        return imgAdapter;
    }

    public static void setImgAdapter(ImagePagerAdapter imgAdapter) {
        ImageWorker.imgAdapter = imgAdapter;
    }

    public static ImagePagerAdapter getImgAdapterHD() {
        return imgAdapterHD;
    }




    public static void setImgAdapterHD(ImagePagerAdapter imgAdapterHD) {
        ImageWorker.imgAdapterHD = imgAdapterHD;


    }

    public static Integer getImgIndex() {
        return ImgIndex;
    }

    public static void setImgIndex(Integer imgIndex) {
        ImgIndex = imgIndex;
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "ImgIndex:" + imgIndex+":"+ImgIndex);
    }

    public static void ladeBild(final int imgID, final boolean HD, final ImagePagerAdapter ipa){
        if(ImgCount>imgID&&ImgIDs[imgID]>0) {
    String url = PhotoUrl + "?key=" + KartenActivity.getInstance().getString(R.string.GoingElectric_APIKEY) + "&id=" + ImgIDs[imgID] + "&size=" + (HD ? Size_HD : Size_noHD);
    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "PhotoUrl:" + url);
    ImageRequest iR = new ImageRequest(url,
            new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    if(loading.contains(imgID))loading.remove(imgID);

                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "PhotoResponse for ID:"+imgID+" " + bitmap.getByteCount() + "Bytes");
                    if(imgID<ImgBitmaps.length)
                        ImgBitmaps[imgID] = bitmap;
                    else
                        LogWorker.e(LOG_TAG,"IMgBItmap Array IndexOutOfBounds: imgID"+imgID+" ImgBitmaps:"+ImgBitmaps.length);


                    if (imgAdapter != null) imgAdapter.updateItem(imgID,bitmap);
                    else
                        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Adapter null:");


                    if (HD) {
                        ImgBitmaps_HD[imgID] = bitmap;
                        ImgBitmaps[imgID] = ImgBitmaps_HD[imgID];
                        if (imgAdapterHD != null) imgAdapterHD.updateItem(imgID,bitmap);
                    }






                }
            }, 0, 0, null,
            new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {

                }
            });
if(!loading.contains(imgID)) {
    KartenActivity.getInstance().addToRequestQueue(iR);
    loading.add(imgID);

    }else
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Already loading:" + imgID);

}else{
    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "not initialized:"+imgID+"/"+ImgCount+" = "+ImgIDs[imgID]);
}
    }


    public static void setImgIDs(int position,Integer id) {
        ImgIDs[position] = id;
        updateImgCount();
    }

    public static void initImages(boolean hd){

        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, (ImgIDs!=null?"ImageIds"+ImgIDs[0]+"-"+ImgIDs[1]+"-"+ImgIDs[2]+"-"+ImgIDs[3]+"-"+ImgIDs[4]:" ImgIDs null"));

        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "ImageCount"+ImgCount);
        //ImgIndex=0;
        ImgBitmaps = new Bitmap[ImgCount];
        ImgBitmaps_HD = ImgBitmaps;
        if(getImgCount()>0){
            ImgBitmaps[0]=null;
            ImgBitmaps_HD[0]= ImgBitmaps[0];
        }

        if (ImgCount>0)ladeBild(0,hd, null);
        if (ImgCount>1)ladeBild(1,hd, null);
    }

    private static void updateImgCount(){
        int b =0;
        for(int a:ImgIDs) if (a>0)b++;
        ImgCount=b;
    }

    public static Integer getImgCount() {

        return ImgCount;
    }


    public static Bitmap[] getImgBitmaps() {
        return getImgBitmaps(false);
    }


    public static Bitmap[] getImgBitmaps(boolean HD) {
        return (HD? ImgBitmaps_HD : ImgBitmaps);
    }

    public static void resetImages(){
        ImgIndex=0;
        ImgCount=0;
        for (int i=0;i<5;i++){
            ImgIDs[i]=0;
        }

    }






}
