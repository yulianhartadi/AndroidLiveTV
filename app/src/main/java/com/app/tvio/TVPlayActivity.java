package com.app.tvio;


import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.util.Util;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;


public class TVPlayActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    private VideoView mVideoView;
    private String url;
    private ProgressBar load;
    private TextView empty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tv_play);
        url = getIntent().getStringExtra("videoUrl");
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
    }

    public void init() {
        load = findViewById(R.id.load);
        empty = findViewById(R.id.empty);
        mVideoView = findViewById(R.id.surface_view);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnPreparedListener(this);

        //Custom Warning Message
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                customDialog();
                return true;
            }
        });

        Uri videoUri = Uri.parse(url);
        mVideoView.setVideoURI(videoUri);
        mVideoView.requestFocus();
        loading();
    }

    private void loading() {
        load.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
    }

    private void loadComplete(MediaPlayer arg0) {
        load.setVisibility(View.GONE);
        // vv.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
        mVideoView.start();
        mVideoView.resume();
    }

    private void error(String msg) {
        load.setVisibility(View.GONE);
        mVideoView.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);
        if (msg != null)
            empty.setText(msg);
        //Toast.makeText(getApplicationContext(), "TV Offline", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // TODO Auto-generated method stub
        Log.d("ONLINE TV", "Prepared");
        loadComplete(mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        //Log.d("ONLINE TV", "Error");
        //error("Unable to play this channel.");
        //Toast.makeText(getApplicationContext(), "Unable to play this channel", Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO Auto-generated method stub
        Log.d("ONLINE TV", "Complete");
    }

    private void customDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_warning);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        (dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), ((AppCompatButton) v).getText().toString() + " Clicked", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                Intent backIntent = new Intent(TVPlayActivity.this, MainActivity.class);
                startActivity(backIntent);
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }




}
