package com.siskiapp.downloadmp3music;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.siskiapp.downloadmp3music.SiskiAppMainActivity.DOWNLOAD_DIRECTORY;

public class SiskiAppPlayActivityOffline extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    ProgressDialog mProgressDialog;
//    RelativeLayout banner;
    ImageView songImg, playerstate;
    TextView songTitle, songUsr, songCurrent, songTotal;
    SeekBar seekBar;
    AppCompatImageView rew, ff, repeat, share,  download, favorite, library;
    ProgressBar playerProgress;
    MediaPlayer mediaPlayer;
    Utilities utilities;
    private Handler mHandler = new Handler();
    public AudioManager audioManager;
    boolean repeat_status = false;

    String trackTitle, trackUrl, trackImg, trackArtist, src;

//    private com.facebook.ads.AdView fbView;
//    private com.google.android.gms.ads.AdView adView;
    private com.google.android.gms.ads.InterstitialAd interstitialAd;
    private com.facebook.ads.InterstitialAd interstitialFb;

    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play_offline);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        banner = findViewById(R.id.banner);
        songImg = findViewById(R.id.player_Img);
        playerstate = findViewById(R.id.play_btn);
        songTitle = findViewById(R.id.player_Title);
        songUsr = findViewById(R.id.player_Artist);
        seekBar = findViewById(R.id.seekBar);
        songCurrent = findViewById(R.id.song_current);
        songTotal = findViewById(R.id.song_total);
        rew = findViewById(R.id.rew);
        ff = findViewById(R.id.ff);
        repeat = findViewById(R.id.repeat);
        share = findViewById(R.id.share);
        download = findViewById(R.id.download);
        favorite = findViewById(R.id.favorite);
        library = findViewById(R.id.library);

        playerProgress = findViewById(R.id.player_Progress);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trackTitle = extras.getString("songtitle");
            trackImg = extras.getString("songimg");
            trackArtist = extras.getString("songartist");
            trackUrl = extras.getString("songurl");
            src = extras.getString("source");
        }

        songTitle.setText(trackTitle);
        songTitle.setSelected(true);
        songTitle.setSingleLine(true);
        songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);

        if (trackArtist != null) {
            songUsr.setText(trackArtist);
        } else {
            songUsr.setVisibility(View.GONE);
        }

        Glide.with(this).load(trackImg).error(R.mipmap.ic_launcher).into(songImg);

        mediaPlayer = new MediaPlayer();
        utilities = new Utilities();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // song lifecycle
        if (mediaPlayer.isPlaying() || mediaPlayer.isLooping() || mediaPlayer != null) {

            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        try {
            mediaPlayer.reset();
            if (src.equals("online")) {
//                loadNewInter();
                mediaPlayer.setDataSource(trackUrl + "&key=" + SiskiAppSplashActivity.sc);
                mediaPlayer.prepareAsync();
            } else {
                mediaPlayer.setDataSource(trackUrl);
                mediaPlayer.prepare();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//        if (SiskiAppSplashActivity.ads_sett.equals("fb")) {
//            fbView = new com.facebook.ads.AdView(this, SiskiAppSplashActivity.id_banner, AdSize.BANNER_HEIGHT_50);
//            banner.addView(fbView);
//            fbView.loadAd();
//        } else {
//            adView = new com.google.android.gms.ads.AdView(this);
//            adView.setAdUnitId(SiskiAppSplashActivity.id_banner);
//            adView.setAdSize(com.google.android.gms.ads.AdSize.SMART_BANNER);
//            AdRequest dareq = new AdRequest.Builder().build();
//            banner.addView(adView);
//            adView.loadAd(dareq);
//        }


        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mplayer) {
                playerProgress.setVisibility(View.GONE);
                download.setVisibility(View.VISIBLE);
                repeat.setVisibility(View.VISIBLE);

                updateProgressBar();
                playpausebutton();

            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                songTitle.setText("Sorry. We can't play this song. Please select another song to play.");
                playerstate.setVisibility(View.INVISIBLE);
                return true;
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mplayer) {
                if (repeat_status) {
                    completingplay();
                } else {
                    playerstate.setBackgroundResource(R.drawable.ic_play_besar);
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);

                }
            }
        });

        playerstate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playpausebutton();
            }
        });

        ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int currentPosition = mediaPlayer.getCurrentPosition();
                if (currentPosition + seekForwardTime <= mediaPlayer.getDuration()) {
                    mediaPlayer.seekTo(currentPosition + seekForwardTime);
                } else {
                    mediaPlayer.seekTo(mediaPlayer.getDuration());
                }
            }
        });

        rew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                if (currentPosition - seekBackwardTime >= 0) {
                    mediaPlayer.seekTo(currentPosition - seekBackwardTime);
                } else {
                    mediaPlayer.seekTo(0);
                }
            }
        });

        repeat.setImageResource(R.drawable.ic_repeat);
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatButton();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                final String appName = getPackageName();
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                share.putExtra(Intent.EXTRA_SUBJECT, "Download and enjoy this good application");
                share.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id="
                        + appName);

                startActivity(Intent.createChooser(share, "Share and invite your friends to View this Apps !!"));
            }
        });

        library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SiskiAppPlayActivityOffline.this, SiskiAppOfflineActivity.class);
                startActivity(i);
                finish();
            }
        });


        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id="
                                    + getPackageName())));
                }
            }
        });



        if (src.equals("online")) {
            download.setAlpha(1f);
            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaPlayer.isPlaying() || mediaPlayer.isLooping() || mediaPlayer != null) {
                        mediaPlayer.pause();
                        playerstate.setBackgroundResource(R.drawable.ic_play_besar);
                    }

                    String cutTitle =trackTitle;
                    cutTitle = cutTitle.replace(" ", "-").replace(".", "-") + ".mp3";
                    DownloadManager downloadManager = (DownloadManager) getApplication().getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(trackUrl + "&key=" + SiskiAppSplashActivity.sc));
                    request.setTitle(trackTitle);
                    request.setDescription("Downloading");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(DOWNLOAD_DIRECTORY, cutTitle);
                    request.allowScanningByMediaScanner();
                    long downloadID = downloadManager.enqueue(request);

                    Toast.makeText(getApplicationContext(), "Downloading Start", Toast.LENGTH_SHORT).show();




//                    mProgressDialog = new ProgressDialog(SiskiAppMusicPlayActivity.this);
//                    mProgressDialog.setMessage("Prepare for saving, please wait!");
//                    mProgressDialog.setIndeterminate(true);
//                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//                    mProgressDialog.setCancelable(true);
//
//                    // execute this when the downloader must be fired
//                    final DownloadTask downloadTask = new DownloadTask(SiskiAppMusicPlayActivity.this);
//                    downloadTask.execute(trackUrl + "&key=" + SiskiAppSplashActivity.sc);
//
//                    mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                        @Override
//                        public void onCancel(DialogInterface dialog) {
//                            downloadTask.cancel(true);
//                        }
//                    });
                }
            });
        } else {
            download.setAlpha(0.4f);
            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SiskiAppPlayActivityOffline.this, "This song are downloaded.", Toast.LENGTH_SHORT).show();
                }
            });
        }


        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mediaPlayer.getDuration();
        int currentPosition = utilities.progressToTimer(seekBar.getProgress(), totalDuration);
        mediaPlayer.seekTo(currentPosition);
        updateProgressBar();
    }

    public void repeatButton() {
        if (repeat_status) {
            repeat.setImageResource(R.drawable.ic_repeat);
            repeat_status = false;
        } else {
            repeat.setImageResource(R.drawable.ic_repeat_active);
            repeat_status = true;
        }
    }

    public void completingplay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        } else {
            playpausebutton();
            updateProgressBar();
        }
    }

    public void playpausebutton() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playerstate.setBackgroundResource(R.drawable.ic_play_besar);
        } else {
            mediaPlayer.start();
            playerstate.setBackgroundResource(R.drawable.ic_pause_besar);
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();

            songTotal.setText("" + utilities.milliSecondsToTimer(totalDuration));
            songCurrent.setText("" + utilities.milliSecondsToTimer(currentDuration));

            int progress = (int) (utilities.getProgressPercentage(currentDuration, totalDuration));
            seekBar.setProgress(progress);

            mHandler.postDelayed(this, 100);
        }
    };

    public void loadNewInter() {
        if (SiskiAppSplashActivity.ads_sett.equals("fb")) {
            interstitialFb = new com.facebook.ads.InterstitialAd(this, SiskiAppSplashActivity.id_inter);
            interstitialFb.loadAd();
            interstitialFb.setAdListener(new AbstractAdListener() {
                @Override
                public void onAdLoaded(Ad ad) {
                    super.onAdLoaded(ad);
                    interstitialFb.isAdLoaded();
                }
            });
        } else {
            interstitialAd = new com.google.android.gms.ads.InterstitialAd(this);
            interstitialAd.setAdUnitId(SiskiAppSplashActivity.id_inter);
            AdRequest adreqint = new AdRequest.Builder().build();
            interstitialAd.loadAd(adreqint);
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    interstitialAd.isLoaded();
                }
            });
        }
    }

    public void showInter() {
        if (SiskiAppSplashActivity.ads_sett.equals("fb")) {
            if (interstitialFb.isAdLoaded()) {
                interstitialFb.show();
                loadNewInter();
            } else {
                loadNewInter();
            }
        } else {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
                loadNewInter();
            } else {
                loadNewInter();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String folder;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();

                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                //String filename = Jud +".mp3";
                folder = Environment.getExternalStorageDirectory() + File.separator + "Downloads/";

                File file = new File(folder);
                if (!file.exists()) {
                    file.mkdirs();
                }

                String filesave = trackTitle.replaceAll("\"", "_").replaceAll("\'", "_");
                String filename = filesave + ".mp3";
                File saveFile = new File(file, filename);

                input = connection.getInputStream();
                output = new FileOutputStream(saveFile);

//                byte data[] = new byte[4096];
                byte data[] = new byte[8192];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgressDialog.setMessage("Downloading...");
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.System.canWrite(SiskiAppPlayActivityOffline.this)) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 1405);
                        Toast.makeText(context, "Please allow app to access the storage file", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Sorry. You can't download this file due to Copyright policy.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Sorry. You can't download this file due to Copyright policy.", Toast.LENGTH_SHORT).show();
                }
            } else {
//                Toast.makeText(context, "Download music succsesfully", Toast.LENGTH_SHORT).show();
                // song lifecycle
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (playerProgress != null) {
            playerProgress = null;
        }
        if (mediaPlayer != null) {
            if (mediaPlayer.isLooping() || mediaPlayer.isPlaying()) {

                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }
}
