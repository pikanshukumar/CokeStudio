package com.example.pikanshu.cokestudio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

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
import java.nio.charset.Charset;
import java.util.ArrayList;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String SONGS_REQUEST_URL =
            "http://starlord.hackerearth.com/edfora/cokestudio";

    private Context mContext;



    private ArrayList<Song> mSongsList;
    private SongAdapter mSongAdapter;

    private EditText searchEditText;
    private ListView mSongListView;

    public static boolean MODE_FAVOURITE = false;   // favourite option selection flag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchEditText = (EditText) findViewById(R.id.search);
        mSongListView = (ListView) findViewById(R.id.songlist);

        // settin initial value
        MODE_FAVOURITE = false;

        mContext = this;

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("pika", " String : " + s.toString());
                String searchString = searchEditText.getText().toString();
                int textLength = searchString.length();

                if(!searchString.isEmpty()){
                    ArrayList<Song> searchResult = new ArrayList<>();
                    for(int i = 0; i < mSongsList.size(); i++){
                        String songName = mSongsList.get(i).getSongname();
                        if(textLength <= songName.length()){
                            if(songName.toLowerCase().contains(searchString.toLowerCase())){
                                searchResult.add(mSongsList.get(i));
                            }
//                            if(searchString.equalsIgnoreCase(songName.substring(0,textLength)))

                        }
                    }
                    updateListView(searchResult);
                }
                else{
                    updateListView();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.favourites:
                //Favourites action handling
//                Log.d("pika", "Favourites Clicked");
                if(MODE_FAVOURITE == false) {

//                    Log.d("pika", "Favourites Clicked" + MODE_FAVOURITE);
                    item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_filled));

                    DBManager dbManager = new DBManager(mContext);
                    dbManager.open();
                    mSongsList = dbManager.getFavouritesFromDB(1);
                    dbManager.close();

                    updateListView();

                    MODE_FAVOURITE = true;
                }
                else if(MODE_FAVOURITE == true){
//                    Log.d("pika", "Favourites Clicked" + MODE_FAVOURITE);
                    item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_empty));

                    DBManager dbManager = new DBManager(mContext);
                    dbManager.open();
                    mSongsList = dbManager.getListFromDb();
                    dbManager.close();

                    updateListView();

                    MODE_FAVOURITE = false;
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        mSongsList = new ArrayList<>();
        GetSongsAsyncTask task = new GetSongsAsyncTask();

        // Database variables
        DBManager dbManager = new DBManager(this);

        dbManager.open();

        Song song = dbManager.get(1L);



        if(song == null){
            dbManager.close();
            task.execute();
            Log.d(LOG_TAG, "Async Task Executed");
        }
        else{
            Log.d(LOG_TAG, "List Extracted from DB");

            mSongsList = dbManager.getListFromDb();
            dbManager.close();
            updateListView();
        }

        super.onResume();
    }

    //function to update listview after change in players list
    private void updateListView(){
        //mMainList.invalidate();
        mSongAdapter=new SongAdapter(this,mSongsList);
        mSongListView.setAdapter(mSongAdapter);
        mSongAdapter.notifyDataSetChanged();
//        mSongListView.smoothScrollToPosition(list_scroll_position);
//        playersListView.setSelection(list_scroll_position);
    }

    //arg arraylist will be used for arrayadapter (used for search option)
    private void updateListView(ArrayList<Song> songList){
        //mMainList.invalidate();
        mSongAdapter=new SongAdapter(this,songList);
        mSongListView.setAdapter(mSongAdapter);
        mSongAdapter.notifyDataSetChanged();
    }

    // JSON parsing fuction to create players list fron jsonResponse string
    private void getSongsFormJSON(String jsonResponse) {

        // Database variables
        DBManager dbManager;
        dbManager = new DBManager(mContext);
        dbManager.open();

        try {
            JSONArray songs = new JSONArray(jsonResponse);
            for (int i=0; i<songs.length(); i++) {
                JSONObject song = songs.getJSONObject(i);

                // creating Song object
                Song tempSong = new Song(song,i+1);
                // adding Song object to songslist
                mSongsList.add(tempSong);
                long id = dbManager.write(tempSong);
                Log.d("MainActivity", "Saving Song " + tempSong.getSongname() + " " + id);
/*
                Log.d("MainActivity", "Saving Song " + tempSong.getArtistName() + " " + id);
                Log.d("MainActivity", "Saving Song " + tempSong.getSongURL() + " " + id);
                Log.d("MainActivity", "Saving Song " + tempSong.getCoverImgURL() + " " + id);
                Log.d("MainActivity", "Saving Song " + tempSong.getId() + " " + id);
*/


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        dbManager.close();

        updateListView();
    }
    // Async task used for retrieving json string by api calling
    private class GetSongsAsyncTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(SONGS_REQUEST_URL);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
//            Event earthquake = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
            return jsonResponse;
        }

        /**
         * Update the screen with the given earthquake (which was the result of the
         * {@link MainActivity.GetSongsAsyncTask}).
         */
        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse == null) {
                return;
            }

            getSongsFormJSON(jsonResponse);
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } catch (IOException e) {
                // TODO: Handle the exception
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }
    }

}
