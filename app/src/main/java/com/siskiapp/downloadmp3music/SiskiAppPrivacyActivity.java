package com.siskiapp.downloadmp3music;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.AdSize;
import com.google.android.gms.ads.AdRequest;

import java.io.IOException;
import java.io.InputStream;

public class SiskiAppPrivacyActivity extends AppCompatActivity {

    RelativeLayout banner;
    TextView textprivacy;

    com.google.android.gms.ads.AdView adView;
    com.facebook.ads.AdView fbView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        banner = findViewById(R.id.banner);
        textprivacy = findViewById(R.id.textpolicy);

        if (SiskiAppSplashActivity.ads_sett.equals("fb")) {
            fbView = new com.facebook.ads.AdView(this, SiskiAppSplashActivity.id_banner, AdSize.BANNER_HEIGHT_50);
            banner.addView(fbView);
            fbView.loadAd();
        } else {
            adView = new com.google.android.gms.ads.AdView(this);
            adView.setAdUnitId(SiskiAppSplashActivity.id_banner);
            adView.setAdSize(com.google.android.gms.ads.AdSize.SMART_BANNER);
            AdRequest dareq = new AdRequest.Builder().build();
            banner.addView(adView);
            adView.loadAd(dareq);
        }

        try {
            InputStream is = getAssets().open("policy.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String txt = new String(buffer);
            textprivacy.setText(txt);
        } catch (IOException ex) {
            return;
        }
    }
}
