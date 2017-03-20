package com.example.pikanshu.cokestudio;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by pika on 12/3/17.
 */

public class DBManager {

    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private Context mContext;

    public DBManager(Context mContext) {
        this.mContext = mContext;
        dbHelper = new DBHelper(mContext);

    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long write(Song song){
        long id = cupboard().withDatabase(db).put(song);
        return id;
    }

    public Song get(long id){
        Song song = cupboard().withDatabase(db).get(Song.class, id);
        return song;
    }

    // helper function to get item list from DB without any ordering or filtering
    public ArrayList<Song> getListFromDb() {

        ArrayList<Song> songsList = new ArrayList<>();

        QueryResultIterable<Song> itr = cupboard().withDatabase(db).query(Song.class).query();

        Log.d("MainActivity", "From db");
        for (Song b : itr)
            songsList.add(b);

        return songsList;
    }


    // helper function to favourites song list from DB
    public ArrayList<Song> getFavouritesFromDB(int value) {
        ArrayList<Song> songList = new ArrayList<>();

        Iterable<Song> itr = cupboard().withDatabase(db)
                .query(Song.class)
                .withSelection("favourite = ?", Integer.toString(value)).query();

        for (Song song : itr)
            songList.add(song);

        return songList;
    }

    // helper fuction to update favourites in DB

    public int updateFavData(int id, int value){
        ContentValues values = new ContentValues(1);
        values.put("favourite", value);
        return cupboard().withDatabase(db).update(Song.class, values, "_id = ?", Integer.toString(id));
    }

    // helper fuction to update downloaded in DB
    public int updateDownloadedData(int id, int value){
        ContentValues values = new ContentValues(1);
        values.put("downloaded", value);
        return cupboard().withDatabase(db).update(Song.class, values, "_id = ?", Integer.toString(id));
    }



}
