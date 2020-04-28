package com.powervoice.vlcforiot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.videolan.libvlc.media.VideoView;

import java.util.ArrayList;
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private VideoView videoView;
    private Button btnPlay;
    private EditText editxtRtsp;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private String mFilePath;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private UiSettings uiSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //checkPermission();

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        //naverMap.setLocationSource(locationSource);
        mapFragment.getMapAsync(this);


        videoView = findViewById(R.id.playerView);
        btnPlay = findViewById(R.id.btnPlay);
        editxtRtsp = findViewById(R.id.editxtRtsp);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnPlay.getText().toString().equals("PLAY")){
                    mFilePath = editxtRtsp.getText().toString();
                    createPlayer(mFilePath);
                    btnPlay.setText("STOP");
                }
                else {
                    btnPlay.setText("PLAY");
                    releasePlayer();
                }
            }
        });


    }

//    public void checkPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//        {
//            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
//
//            }
//        }
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
//            if (!locationSource.isActivated()) { // 권한 거부됨
//                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
//            }
            Log.d("sex","kill -9");
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);
        //uiSettings.setLocationButtonEnabled(true);
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        //Marker marker = new Marker();
        //marker.setPosition(new LatLng(36.763695, 127.281796));
        //marker.setMap(naverMap);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        releasePlayer();
    }



    public void createPlayer(String mediaPath){
        releasePlayer();
        try {
            if (mediaPath.length() > 0) {
                Toast toast = Toast.makeText(this, mediaPath, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }

            final ArrayList<String> args = new ArrayList<>();
            args.add("-vvv");
            args.add("--aout=opensles");
            args.add("--audio-time-stretch"); // time stretching
            //args.add("--network-caching=0");
            //args.add("--live-caching=0");
            //args.add("--sout-mux-caching=0");
            mLibVLC = new LibVLC(this, args);
            mMediaPlayer = new MediaPlayer(mLibVLC);

            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(videoView);
            vout.attachViews();
            try {
                final Media media = new Media(mLibVLC, Uri.parse(mediaPath));
                mMediaPlayer.setMedia(media);
                media.release();
            } catch (Exception e) {
                throw new RuntimeException("Invalid URL");
            }
            mMediaPlayer.play();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    public void releasePlayer(){
        //라이브러리가 없다면
        //바로 종료
        if (mLibVLC == null)
            return;
        if(mMediaPlayer != null) {
            //플레이 중지

            mMediaPlayer.stop();

            final IVLCVout vout = mMediaPlayer.getVLCVout();

            //연결된 뷰 분리
            vout.detachViews();
        }

        mLibVLC.release();
        mLibVLC = null;
    }

}
