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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.videolan.libvlc.media.VideoView;

import java.util.ArrayList;
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private VideoView videoView;
    private Button btnPlay, btnTransfer, btnChange;
    private EditText editxtRtsp;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private String mFilePath;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private MqttAndroidClient mqttAndroidClient;
    private NaverMap naverMap;
    private UiSettings uiSettings;
    private String ReaLocation;

    private int i = 0;

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
        btnChange = findViewById(R.id.btn_map_change);
        btnTransfer = findViewById(R.id.btn_transfer);
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

        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(), naverMap.getLocationSource().toString(), Toast.LENGTH_SHORT).show();
                naverMap.addOnLocationChangeListener(location ->
                        //Toast.makeText(v.getContext(), location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show()
                        ReaLocation = location.getLatitude() + ", " + location.getLongitude()
                );
                try{
                    mqttAndroidClient.publish("test",ReaLocation.getBytes(),0, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (i == 0){
                    naverMap.setMapType(NaverMap.MapType.Satellite);
                    i = 1;
                }
                else {
                    naverMap.setMapType(NaverMap.MapType.Basic);
                    i = 0;
                }
            }
        });

        mqttAndroidClient = new MqttAndroidClient(this,  "tcp://" + "192.168.20.184" + ":1883", MqttClient.generateClientId());
        connect();

//        naverMap.addOnLocationChangeListener(location
////                Toast.makeText(this, location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show()
////        );

    }



    private void connect() {
        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());    //mqtttoken 이라는것을 만들어 connect option을 달아줌
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());    //연결에 성공한경우
                    Log.e("Connect_success", "Success");
                    try {
                        mqttAndroidClient.subscribe("test", 0 );
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {   //연결에 실패한경우
                    Log.e("connect_fail", "Failure " + exception.toString());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void callBack() {
        //클라이언트의 콜백을 처리하는부분
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {    //모든 메시지가 올때 Callback method
                if (topic.equals("test")){     //topic 별로 분기처리하여 작업을 수행할수도있음
                    String msg = new String(message.getPayload());
                    Log.e("arrive message : ", msg);
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

    }

    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill("aaa", "I am going offline".getBytes(), 1, true);
        return mqttConnectOptions;
    }


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
