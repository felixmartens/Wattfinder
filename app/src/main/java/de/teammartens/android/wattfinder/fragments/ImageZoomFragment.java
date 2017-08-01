package de.teammartens.android.wattfinder.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.teammartens.android.wattfinder.KartenActivity;
import de.teammartens.android.wattfinder.R;
import de.teammartens.android.wattfinder.model.ImagePagerAdapter;
import de.teammartens.android.wattfinder.worker.ImageWorker;
import de.teammartens.android.wattfinder.worker.LogWorker;

import static android.view.View.GONE;

/**
 * Created by felix on 12.02.17.
 */

public class ImageZoomFragment extends Fragment {

    private static final String LOG_TAG = "Fragment ImageZoom";
    private String Titel = "";
    private Integer ID = 0;
    private static ViewPager ImagePager;
    private static View imageView;
    private static LinearLayout pager_indicator;
    private static ImageView[] dots;
    private static Context mContext;


    public ImageZoomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_imagezoom, container, false);
    }

    public void onStart() {
        super.onStart();


        imageView=this.getView();
        mContext=this.getContext();
    }

    public void onResume() {
        super.onResume();
        Titel=DetailsFragment.getmTitel();
        final TextView tv = (TextView) this.getView().findViewById(R.id.izBezeichnung);
        tv.setText(Titel);
        ID=DetailsFragment.getmID();
        initializeWorker();

    }

    public void onPause (){
        super.onPause();
        KartenActivity.hideImageZoom();
    }



    public static void initializeWorker() {


        ImagePager = (ViewPager) imageView.findViewById(R.id.izImagePager);

        pager_indicator = (LinearLayout) imageView.findViewById(R.id.izImgPagerCountDots);

        ImageWorker.initImages(true);

        ImageWorker.setImgAdapterHD(new ImagePagerAdapter(mContext, ImageWorker.getImgBitmaps(true),ImageWorker.getImgBitmaps(false),true));

        ImagePager.setAdapter(ImageWorker.imgAdapterHD);
        ImagePager.setCurrentItem(ImageWorker.getImgIndex());
        if(LogWorker.isVERBOSE())LogWorker.d(LOG_TAG,"getImgIndex"+ImageWorker.getImgIndex());
        ImagePager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < ImageWorker.getImgCount(); i++) {
                    dots[i].setImageDrawable(mContext.getResources().getDrawable(R.drawable.nonselecteditem_dot));
                }

                dots[position].setImageDrawable(mContext.getResources().getDrawable(R.drawable.selecteditem_dot));
                ImageWorker.setImgIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setUiPageViewController();
    }




    private static void setUiPageViewController() {
        int dotsCount = ImageWorker.getImgCount();

        if(dots==null)dots = new ImageView[5];

        for (int i = 0; i < 5; i++) {
            if(pager_indicator.getChildAt(i)!=null){
                dots[i] = (ImageView) pager_indicator.getChildAt(i);


            }else {
                dots[i] = new ImageView(mContext);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(4, 0, 4, 0);

                pager_indicator.addView(dots[i], params);


            }
            dots[i].setImageDrawable(mContext.getResources().getDrawable(R.drawable.nonselecteditem_dot));
            if (i>=dotsCount) dots[i].setVisibility(GONE);
            else dots[i].setVisibility(View.VISIBLE);
        }

        dots[ImageWorker.getImgIndex()].setImageDrawable(mContext.getResources().getDrawable(R.drawable.selecteditem_dot));
    }



}
