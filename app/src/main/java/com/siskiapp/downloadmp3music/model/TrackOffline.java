package com.siskiapp.downloadmp3music.model;


public class TrackOffline {
    private String songTitle, songUrl;

    public TrackOffline(String songTitle, String songUrl) {
        this.songTitle = songTitle;
        this.songUrl = songUrl;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }
}
