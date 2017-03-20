package com.example.pikanshu.cokestudio;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by pika on 18/3/17.
 */

public class Song implements Serializable {

    public Long _id; // for cupboard
    private int id; // personal
    private String songname;
    private String songURL;
    private String artistName;
    private String coverImgURL;
    private int downloaded = 0; // download flag
    private int favourite = 0;  // favourite flag

    public Song() {
    }


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

    public int isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }

    public String getSongname() {
        return songname;
    }

    public void setSongname(String songname) {
        this.songname = songname;
    }

    public String getSongURL() {
        return songURL;
    }

    public void setSongURL(String songURL) {
        this.songURL = songURL;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getCoverImgURL() {
        return coverImgURL;
    }

    public void setCoverImgURL(String coverImgURL) {
        this.coverImgURL = coverImgURL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
