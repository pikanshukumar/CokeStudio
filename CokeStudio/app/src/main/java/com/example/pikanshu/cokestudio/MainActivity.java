package com.example.pikanshu.cokestudio;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String SONGS_REQUEST_URL =
            "http://starlord.hackerearth.com/edfora/cokestudio";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL = 1;

    private Context mContext;


    private ArrayList<Song> mSongsList;
    private SongAdapter mSongAdapter;

    private EditText searchEditText;
    private ListView mSongListView;

    // Download complete callback
    SongAdapter.OnDownloadButtonClickListener callback;
//    DownloadFile downloadFileTask;

    public static boolean MODE_FAVOURITE = false;   // favourite option selection flag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchEditText = (EditText) findViewById(R.id.search);
        mSongListView = (ListView) findViewById(R.id.songlist);

        // checking permission
        isStoragePermissionGranted();

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
        if(callback == null) {
            callback = new SongAdapter.OnDownloadButtonClickListener() {
                @Override
                public void onClick(Song song) {
                    Log.d(LOG_TAG, "DownloadButton Clicked");
                    ArrayList<String> param = new ArrayList<>();
                    param.add(song.getSongname());
                    param.add(song.getSongURL());
                    new DownloadFile(mContext).execute(param);

                }
            };
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this,"Can not download file",Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    //function to update listview after change in players list
    private void updateListView(){
        //mMainList.invalidate();
        mSongAdapter=new SongAdapter(this,mSongsList);
        mSongAdapter.setOnDownloadButtonClickListener(callback);
        mSongListView.setAdapter(mSongAdapter);
        mSongAdapter.notifyDataSetChanged();
//        mSongListView.smoothScrollToPosition(list_scroll_position);
//        playersListView.setSelection(list_scroll_position);
    }

    //arg arraylist will be used for arrayadapter (used for search option)
    private void updateListView(ArrayList<Song> songList){
        //mMainList.invalidate();
        mSongAdapter=new SongAdapter(this,songList);
        mSongAdapter.setOnDownloadButtonClickListener(callback);
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

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOG_TAG,"Permission is granted");
                return true;
            } else {

                Log.v(LOG_TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(LOG_TAG,"Permission is granted");
            return true;
        }
    }


    private void OnDownloadFinish(String songName){

        Toast.makeText(this,"Download Finished !!",Toast.LENGTH_SHORT).show();
        DBManager dbManager = new DBManager(this);
        dbManager.open();

        for(int i = 0; i < mSongsList.size(); i++){
            Song song = mSongsList.get(i);
            if(song.getSongname().equalsIgnoreCase(songName.toLowerCase())){
                song.setDownloaded(1);
                dbManager.updateDownloadedData(song.getId(),1);
                break;
            }
        }
        dbManager.close();
        updateListView();
    }

    public class DownloadFile extends AsyncTask<ArrayList<String>, Integer, String> {

        NotificationManager mNotifyManager;
        Notification.Builder mBuilder;
        int ID = 1; // notification ID
        private Context mContext;
        private File storageDir = null;

        private String appName;

//        public DownloadFinished callback;

        public DownloadFile(Context context) {
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder       = new Notification.Builder(mContext);

            mBuilder.setContentTitle("Song Download")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.ic_file_download);

            mBuilder.setProgress(100, 0, false);
            // Displays the progress bar for the first time.
            mNotifyManager.notify(ID, mBuilder.build());

            appName = mContext.getApplicationInfo().loadLabel(mContext.getPackageManager()).toString();
            storageDir = new File(Environment.getExternalStorageDirectory(),appName);
            Log.d("download", storageDir.getAbsolutePath().toString());

            if(!storageDir.exists()){
                storageDir.mkdirs();
            }
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(ArrayList<String>... urlParams) {
            int count;
            int interval = 10;
            HttpURLConnection conexion = null;
            HttpURLConnection conn = null;
            ArrayList<String> passed = null;
            try {
                passed = urlParams[0]; // get passed arraylist

                mBuilder.setContentTitle(appName + " : " + passed.get(0));

                URL url = new URL(passed.get(1));

                conexion = (HttpURLConnection) url.openConnection();
                conexion.setInstanceFollowRedirects(false);

                URL secondURL = new URL(conexion.getHeaderField("Location"));

                conn = (HttpURLConnection) secondURL.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);

                conn.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conn.getContentLength();

                Log.d("download", "1st URL :" + url.toString() );
                Log.d("download", "2nd URL :" +  secondURL.toString());
                Log.d("download", " Data Length:" + lenghtOfFile );

                // downlod the file
                InputStream input = new BufferedInputStream(secondURL.openStream());
                Log.d("download",storageDir.toString()+File.separator+passed.get(0)+".mp3");
                OutputStream output = new FileOutputStream(storageDir.toString()+File.separator+passed.get(0)+".mp3");

                byte data[] = new byte[1024];

                long total = 0;
//            Log.d("download", " input.read(data)" + input.read(data));
                while ((count = input.read(data)) != -1) {
//                Log.d("download", " Writing Data");
                    total += count;
                    // publishing the progress....
                    int progressValue = (int) (total * 100 / lenghtOfFile);
                    int temp = progressValue/interval;

                    if( temp > 0 ){
                        publishProgress(progressValue);
                        interval += 10;
                    }
                    output.write(data, 0, count);
                }
                Log.d("download", " Writing Data Complete");
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
            }
            if(passed != null) return passed.get(0);
            return  null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            if(mBuilder != null){
                Log.d("download", "Progress : " + values[0]);
                mBuilder.setProgress(100,values[0],false);
                // Displays the progress bar for the first time.
                mNotifyManager.notify(ID, mBuilder.build());
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            // When the loop is finished, updates the notification

            if(mBuilder != null) {
                mBuilder.setContentText("Download complete")
                        // Removes the progress bar
                        .setProgress(0, 0, false);

                mNotifyManager.notify(ID, mBuilder.build());
            }
            OnDownloadFinish(s);
            super.onPostExecute(s);
        }

    }


}
