package com.jk.cordovamediaplugin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cj.video.playVideo.PlayVideoActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String path = "/storage/emulated/0/Pictures/slope/1803f2e2-2ff9-47c3-adf0-4ecc12090405.mp4";
        Intent i = PlayVideoActivity.getIntent(this, path);
        startActivity(i);
    }
}
