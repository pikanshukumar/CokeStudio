package com.example.pikanshu.cokestudio;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.pikanshu.cokestudio.MainActivity.LOG_TAG;

/**
 * Created by pika on 12/3/17.
 */

public class SongAdapter extends ArrayAdapter<Song> {

    private Activity activity;
    private OnDownloadButtonClickListener callback;

    public SongAdapter(Context context, ArrayList<Song> songs) {
        super(context, 0, songs);
    }

    void setOnDownloadButtonClickListener(OnDownloadButtonClickListener callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // check if current view is being reused, otherwise inflate the view

//        PlayerListView.list_scroll_position = position;
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.song_list_item, parent, false);
        }
        final Song currentSong = getItem(position);

        TextView nameTextView = (TextView) listItemView.findViewById(R.id.song_name);
        nameTextView.setText(currentSong.getSongname());
        TextView countryTextView = (TextView) listItemView.findViewById(R.id.artist_name);
        countryTextView.setText(currentSong.getArtistName());

        final Context context = parent.getContext();

        // Loading image for imageView using picasso
//        Picasso.with(context)
//                .load(currentSong.getCoverImgURL()).error(R.drawable.icon_placeholder).noFade().into(image);

        // fav icon action handling
        final ImageView fav = (ImageView) listItemView.findViewById(R.id.fav);
        int favourite = currentSong.isFavourite();
        if (favourite == 1) {
            fav.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_filled));
        } else {
            fav.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_empty));
        }


        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBManager dbManager = new DBManager(context);
                dbManager.open();
                int favourite = currentSong.isFavourite();
                if (favourite == 1) {
                    fav.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_empty));
                    currentSong.setFavourite(0);
                    dbManager.updateFavData(currentSong.getId(), 0);
                } else {
                    fav.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_filled));
                    currentSong.setFavourite(1);
                    dbManager.updateFavData(currentSong.getId(), 1);
                }
                dbManager.close();
            }
        });


        // download button handling
        ImageView download = (ImageView) listItemView.findViewById(R.id.download);

        final int downloaded = currentSong.isDownloaded();
        if (downloaded == 1) {
            download.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_file_downloaded));
        } else {
            download.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_file_download));
        }


        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted(context)) {
                    if (downloaded == 0) {
                        Toast.makeText(context, "Download started !!", Toast.LENGTH_SHORT).show();
                        callback.onClick(currentSong);
                    } else
                        Toast.makeText(context, "Song already downloaded", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // play button handling
        ImageView play = (ImageView) listItemView.findViewById(R.id.play_pause);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MusicPlayer.class);
                intent.putExtra("song", currentSong);
                context.startActivity(intent);
            }
        });
        return listItemView;
    }

    // helper function to check permission
    public boolean isStoragePermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOG_TAG, "Permission is granted");
                return true;
            } else {

                Log.v(LOG_TAG, "Permission is revoked");
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(LOG_TAG, "Permission is granted");
            return true;
        }
    }

    public interface OnDownloadButtonClickListener {
        void onClick(Song song);
    }
}