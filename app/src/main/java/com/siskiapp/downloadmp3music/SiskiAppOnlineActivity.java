package com.siskiapp.downloadmp3music;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import com.siskiapp.downloadmp3music.adapter.TrackAdapter;
import com.siskiapp.downloadmp3music.model.TrackOnline;
import com.facebook.ads.AdSize;
import com.google.android.gms.ads.AdRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class SiskiAppOnlineActivity extends AppCompatActivity {

    SearchView searchView;
    RecyclerView recView;
    RelativeLayout banner;
    TrackAdapter mAdapter;
    List<Object> listOnline;
    ProgressDialog mProgressDialog;

    com.google.android.gms.ads.AdView adView;
    com.facebook.ads.AdView fbView;
    com.google.android.gms.ads.InterstitialAd interstitialAd;
    com.facebook.ads.InterstitialAd interstitialFb;

    String search_query, api;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        searchView = findViewById(R.id.searchViewOnline);
        recView = findViewById(R.id.recView);
        banner = findViewById(R.id.banner);
        api = getResources().getString(R.string.api);


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search_query = query;
                setTitle("Search Music : "+search_query);
                new LoadSearchData().execute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });

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

        LinearLayoutManager linlayout = new LinearLayoutManager(this);
        linlayout.setOrientation(LinearLayout.VERTICAL);
        recView.setLayoutManager(linlayout);
        recView.setHasFixedSize(true);
        recView.setItemAnimator(new DefaultItemAnimator());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            search_query = extras.getString("query");
            setTitle("Search Music : "+search_query);
        }

        listOnline = new ArrayList<>();
        mAdapter = new TrackAdapter(this,listOnline);

        if (search_query != null) {
            new LoadSearchData().execute();
        }

        recView.setAdapter(mAdapter);

    }

    @SuppressLint("StaticFieldLeak")
    private class LoadSearchData extends AsyncTask<Void, Void, Void> {

        TrackOnline track;
        String title, artwork_url, song_url, username;
        int dur, like;
        URL url = null;
        String data = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listOnline.clear();
            mProgressDialog = new ProgressDialog(SiskiAppOnlineActivity.this);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMessage("Loading music list\nPlease wait...");
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

//                url = new URL(api + "/tracks.json?client_id=" + SiskiAppSplashActivity.sc + "&q=" + search_query.replaceAll(" ","+") + "&limit=100");
                url = new URL(api + "/search/tracks?q="+search_query+"&client_id="+SiskiAppSplashActivity.sc+"&limit=100");

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    data = data + line;
                }
                try {

                    JSONObject jsonnew = new JSONObject(data);
                    JSONArray json = jsonnew.getJSONArray("collection");


                    for (int i = 0; i < json.length(); i++) {

                        title = json.getJSONObject(i).getString("title");

                        artwork_url = json.getJSONObject(i).getString("artwork_url");

                        song_url = "http://fando.id/soundcloud.php?id="+json.getJSONObject(i).getString("id");

//                        Toast.makeText(getApplicationContext(),song_url,Toast.LENGTH_LONG).show();
                        like = json.getJSONObject(i).getInt("likes_count");
                        JSONObject user = json.getJSONObject(i).getJSONObject("user");
                        username = user.getString("username");
                        dur = json.getJSONObject(i).getInt("duration");

                        track = new TrackOnline(title, artwork_url, song_url, username, like, dur);
                        listOnline.add(track);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
            mProgressDialog.dismiss();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_offline) {
            Intent i = new Intent(SiskiAppOnlineActivity.this, SiskiAppOfflineActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
