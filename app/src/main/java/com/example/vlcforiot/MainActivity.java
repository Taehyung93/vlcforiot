package com.example.vlcforiot;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.videolan.libvlc.media.VideoView;

import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private Button btnPlay;
    private EditText editxtRtsp;

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



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
