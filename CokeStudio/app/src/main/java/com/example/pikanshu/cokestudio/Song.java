package com.example.pikanshu.cokestudio;

import org.json.JSONObject;

/**
 * Created by pika on 18/3/17.
 */

public class Song {

    public Long _id; // for cupboard
    private int id; // personal
    private String songname;
    private String songURL;
    private String artistName;
    private String coverImgURL;

    public Song() {
    }

    private int favourite = 0;

    public Song(JSONObject obj, int id) {
        this.id = id;
        this.songname = obj.optString("song");
        this.songURL = obj.optString("url");
        this.artistName = obj.optString("artists");
        this.coverImgURL = obj.optString("cover_image");
    }

    public Song(String songname, String songURL, String artistName, String coverImgURL, int id) {
        this.id = id;
        this.songname = songname;
        this.songURL = songURL;
        this.artistName = artistName;
        this.coverImgURL = coverImgURL;
    }


    public void setFavourite(int favourite) {
        this.favourite = favourite;
    }

    public int isFavourite() {

        return this.favourite;
    }

    public String getSongname() {
        return songname;
    }

    public String getSongURL() {
        return songURL;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getCoverImgURL() {
        return coverImgURL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSongname(String songname) {
        this.songname = songname;
    }

    public void setSongURL(String songURL) {
        this.songURL = songURL;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setCoverImgURL(String coverImgURL) {
        this.coverImgURL = coverImgURL;
    }
}
