package com.imr3.trebouet.tp_android_interface_riches;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main_VideoView extends AppCompatActivity {
    private final String MAPVIEW_BUNDLE_KEY = "map";
    private MapView mMapView;
    private JSONArray mWaypoints;
    private VideoView mvideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main__video_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(mapViewBundle);

        try {

            // Chargement Fichier
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.chapters)));
            StringBuilder strb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                strb.append(line + "\n");
            }

            JSONObject y = new JSONObject(strb.toString());


            //Player vidéo
            final VideoView videoView = findViewById(R.id.videoView);
            mvideoView = videoView;

            final MediaController controller = new MediaController(this);
            videoView.setMediaController(controller);

            JSONObject film = y.getJSONObject("Film");

            Uri uri = Uri.parse(film.getString("file_url"));
            videoView.setVideoURI(uri);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    videoView.start();
                }
            });


            //Chapitrage

            LinearLayout chapLayout = findViewById(R.id.chapter_buttons);


            JSONArray chapters = y.getJSONArray("Chapters");
            for (int i = 0; i < chapters.length(); i++) {
                final JSONObject chapter = (JSONObject) chapters.get(i);
                Button chapt = new Button(this);
                chapt.setText(chapter.getString("title"));
                chapt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            videoView.seekTo(chapter.getInt("pos") * 1000);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                chapLayout.addView(chapt);

            }
            //WebWiew

            WebView webView = findViewById(R.id.webView);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }
            });
            webView.loadUrl(film.getString("synopsis_url"));


            //MapView
            mWaypoints = y.getJSONArray("Waypoints");
            initMap();
        }catch(Exception e){
            e.printStackTrace();
        }



    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle != null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void initMap(){
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                try {
                    long lat;
                    long lng;
                    String label;
                    int timestamp;
                    for (int i = 0; i < mWaypoints.length(); i++) {
                        lat = mWaypoints.getJSONObject(i).getLong("lat");
                        lng = mWaypoints.getJSONObject(i).getLong("lng");
                        label = mWaypoints.getJSONObject(i).getString("label");
                        timestamp = mWaypoints.getJSONObject(i).getInt("timestamp");
                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat,lng))
                                .title(label));
                        marker.setTag(timestamp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        int timestamp = (int)marker.getTag();
                        mvideoView.seekTo(timestamp * 1000);
                        return false;
                    }
                });
            }
        });
    }
    /*private int setProgress() {
        int position = mvideoView.getCurrentPosition();
        int duration = mvideoView.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = mvideoView.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }
        return position;
    }

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && mvideoView.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };*/
}
