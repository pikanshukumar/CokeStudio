package com.example.pikanshu.cokestudio;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by pika on 12/3/17.
 */

public class SongAdapter extends ArrayAdapter<Song> {


    public SongAdapter(Context context, ArrayList<Song> songs) {
        super(context, 0, songs);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // check if current view is being reused, otherwise inflate the view

//        PlayerListView.list_scroll_position = position;
        View listItemView = convertView;

        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.song_list_item,parent,false);
        }
        final Song currentSong = getItem(position);

        TextView nameTextView = (TextView) listItemView.findViewById(R.id.song_name);
        nameTextView.setText(currentSong.getSongname());
        TextView countryTextView = (TextView) listItemView.findViewById(R.id.artist_name);
        countryTextView.setText(currentSong.getArtistName());

        ImageView image = (ImageView) listItemView.findViewById(R.id.song_cover_pic);
        final Context context = parent.getContext();

        // Loading image for imageView using picasso
        Picasso.with(context)
                .load(currentSong.getCoverImgURL()).error(R.drawable.icon_placeholder).noFade().into(image);

        // fav icon action handling
        final ImageView fav = (ImageView) listItemView.findViewById(R.id.fav);
        int favourite = currentSong.isFavourite();
        if(favourite == 1){
            fav.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_filled));
        }
        else{
            fav.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_empty));
        }


        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBManager dbManager = new DBManager(context);
                dbManager.open();
                int favourite = currentSong.isFavourite();
                if(favourite == 1){
                    fav.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_empty));
                    currentSong.setFavourite(0);
                    dbManager.updateStarData(currentSong.getId(),0);
                }
                else{
                    fav.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_filled));
                    currentSong.setFavourite(1);
                    dbManager.updateStarData(currentSong.getId(),1);
                }
                dbManager.close();
            }
        });

        // arrow icon action handling
        /*ImageView description = (ImageView) listItemView.findViewById(R.id.arrow);
        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // starting decription activity
                Intent intent = new Intent(context,DescriptionActivity.class);
                intent.putExtra("player",currentPlayer);
                context.startActivity(intent);
            }
        });*/
        return listItemView;
    }
}