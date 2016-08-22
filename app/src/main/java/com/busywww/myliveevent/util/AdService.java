package com.busywww.myliveevent.util;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.busywww.myliveevent.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by BusyWeb on 10/3/2014.
 */
public class AdService {

    @SuppressLint("NewApi")
    public static class BannerFragment extends Fragment {

        //private static BannerFragment _bannerFragment = null;
        private AdView mAdView;
        private boolean isOnline;
        private TextView mTextView;
        private ImageButton mImageButton;

        public static BannerFragment newInstance() {
//            if (_bannerFragment == null) {
//                _bannerFragment = new BannerFragment();
//            }
//            return _bannerFragment;
            return new BannerFragment();
        }

        public BannerFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (container == null) {
                return null;
            }
            isOnline = UtilNetwork.IsOnline(AppShared.gContext);
            if (isOnline) {
                return inflater.inflate(R.layout.fragment_ad_banner, container, false);
            } else {
                return inflater.inflate(R.layout.fragment_ad_banner_myad, container, false);
            }
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            if (isOnline) {
                mAdView = (AdView) getView().findViewById(R.id.adView);

                // Create an ad request. Check logcat output for the hashed device ID to
                // get test ads on a physical device. e.g.
                // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
                AdRequest adRequest = new AdRequest.Builder()
//                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                        .addTestDevice("E8C1A939A52F387168C48CC03EAB8BAC")	// nexus 7
//                        .addTestDevice("1EC8F452AD1838A8F2DD2DF92A40C20B")	// nexus s
//                        .addTestDevice("9986B85AAD5AE0FA3DE84B361C5810EA")	// nexus 4
                        .build();

                // Start loading the ad in the background.
                mAdView.loadAd(adRequest);
            } else {
                mTextView = (TextView)getView().findViewById(R.id.textViewLinkToApps);
                mTextView.setOnClickListener(appLinkListener);
                mImageButton = (ImageButton)getView().findViewById(R.id.imgbtnLinkToApps);
                mImageButton.setOnClickListener(appLinkListener);
            }

        }

        private View.OnClickListener appLinkListener = new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse("market://search?q=pub:\"Busy WWW\"")));
            }

        };

        /** Called when leaving the activity */
        @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        /** Called when returning to the activity */
        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }
        }

        /** Called before the activity is destroyed */
        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }
    }





}
