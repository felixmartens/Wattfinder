package de.teammartens.android.wattfinder.model;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.worker.AnimationWorker;
import de.teammartens.android.wattfinder.worker.ImageWorker;
import de.teammartens.android.wattfinder.worker.LogWorker;

/**
 * Created by felix on 12.02.17.
 */

public class ImagePagerAdapter extends PagerAdapter implements View.OnClickListener {

    private static final String LOG_TAG="IMAGE_PAGER_ADAPTER";
    private Context mContext;
    private Bitmap[] mBitmaps,mPlaceholders;
    private ImageView[] iV = new ImageView[5];
    private Boolean HD = false;


    public void setHD(Boolean HD) {
        this.HD = HD;
    }

    public ImagePagerAdapter(Context mContext, Bitmap[] drawables) {
        this.mContext = mContext;
        this.mBitmaps = new Bitmap[drawables.length];
        for(int b=0;b<drawables.length;b++) this.mBitmaps[b]=drawables[b];
        //this.mBitmaps = drawables;
        this.HD = false;
    }

    public ImagePagerAdapter(Context mContext, Bitmap[] drawables, boolean HD) {
        this.mContext = mContext;
        this.mBitmaps = new Bitmap[drawables.length];
        for(int b=0;b<drawables.length;b++) this.mBitmaps[b]=drawables[b];
        this.HD = HD;
    }

    public ImagePagerAdapter(Context mContext, Bitmap[] drawables,Bitmap[] placeholders, boolean HD) {
        this.mContext = mContext;
        this.mBitmaps = new Bitmap[drawables.length];
        for(int b=0;b<drawables.length;b++) this.mBitmaps[b]=drawables[b];
        this.mPlaceholders = new Bitmap[placeholders.length];
        for(int b=0;b<placeholders.length;b++) this.mPlaceholders[b]=placeholders[b];
        this.HD = HD;
    }

    public void updateItems(Bitmap[] drawables){
        this.mBitmaps = new Bitmap[drawables.length];
        this.mBitmaps = drawables;
        notifyDataSetChanged();
    }

    public void setPlaceHolders(Bitmap[] drawables){
        this.mPlaceholders = new Bitmap[drawables.length];
        for(int b=0;b<drawables.length;b++) this.mPlaceholders[b]=drawables[b];

    }
    public void updateItem(int position, Bitmap bitmap){
        if(bitmap!=null && !bitmap.equals(mBitmaps[position])) {
            if (this.iV != null && this.iV[position] != null) {
                this.iV[position].setImageBitmap(bitmap);
                this.iV[position].setAlpha(0.0f);
                mBitmaps[position] = bitmap;
                setScaleType(position);
                if (LogWorker.isVERBOSE())
                    LogWorker.d(LOG_TAG, "UpdateItem:" + position + " hd:" + HD);
                if (!HD) {
                    this.iV[position].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (LogWorker.isVERBOSE())
                                LogWorker.d(LOG_TAG, "Pager clicked HD:" + HD + " via updateItem");
                            AnimationWorker.show_imagezoom();
                        }
                    });
                }
                fadeIn(this.iV[position]);
            }


            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mBitmaps.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.imgpagerchild, container, false);

        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Instantiate" + position+" Bitmap:"+(mBitmaps[position]!=null?mBitmaps[position].getByteCount():"null"));
        this.iV[position] = (ImageView) itemView.findViewById(R.id.img_pager_item);


        if (!HD) {
            this.iV[position].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (LogWorker.isVERBOSE())
                        LogWorker.d(LOG_TAG, "Pager clicked HD:" + HD + " via updateItem");
                    AnimationWorker.show_imagezoom();
                }
            });
        }


        if(mBitmaps[position]==null) {
            ImageWorker.ladeBild(position,HD, this);
            this.iV[position].setAlpha(0.0f);
            if(mPlaceholders!=null)if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "Instantiate" + position+" setPlaceholder:"+(mPlaceholders[position]!=null?mPlaceholders[position].getByteCount():"null"));

            if(mPlaceholders!=null&&mPlaceholders[position]!=null) {
                this.iV[position].setImageBitmap(mPlaceholders[position]);
                this.iV[position].setScaleType(ImageView.ScaleType.CENTER);
                this.iV[position].setAlpha(0.6f);
            }

        }else {
            this.iV[position].setImageBitmap(mBitmaps[position]);
            setScaleType(position);
            this.iV[position].setAlpha(1.0f);

        }

        container.addView(itemView);

        return itemView;
    }

private  void setScaleType(int position){

    if(HD){
            this.iV[position].setScaleType(ImageView.ScaleType.FIT_CENTER);
    }else{
        if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG, "setScale" + position+" Bitmap:"+(mBitmaps[position]!=null?mBitmaps[position].getByteCount()+" w:"+mBitmaps[position].getWidth()+" h:"+mBitmaps[position].getHeight():"null"));
        if (mBitmaps[position]!=null&&mBitmaps[position].getWidth() > mBitmaps[position].getHeight())
            this.iV[position].setScaleType(ImageView.ScaleType.CENTER_CROP);
        else
            this.iV[position].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }
}

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    private  void fadeIn (final View V) {

            V.animate()

                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            V.setVisibility(View.VISIBLE);

                        }
                    });
        }

public void onClick(View v){
    if (LogWorker.isVERBOSE()) LogWorker.d(LOG_TAG,"Pager clicked HD:"+HD+"via this");
    if(!HD)AnimationWorker.show_imagezoom();
}

}
