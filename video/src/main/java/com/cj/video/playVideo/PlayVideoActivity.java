package com.cj.video.playVideo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.cj.video.R;

public class PlayVideoActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "extra:url";
    PlayVideoView videoView;
    Toolbar toolbar;
    private String url;

    public static Intent getIntent(Context context, String url) {
        Intent i = new Intent(context, PlayVideoActivity.class);
        i.putExtra(EXTRA_URL, url);
        return i;
    }

    private void handIntent(Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            return;
        }

        Bundle args = intent.getExtras();
        url = args.getString(EXTRA_URL);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        videoView = findViewById(R.id.video_view);
        toolbar = findViewById(R.id.toolbar);
        setTranslucentStatus(this);
        handIntent(getIntent());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener((v) -> onBackPressed());

        init();
    }

    /**
     * 设置状态栏透明
     */
    public static void setTranslucentStatus(Activity activity) {
        // 5.0以上系统状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void init() {
        videoView.setVideoUrl(url, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (videoView.onBackPressed()) {
            videoView.release();
            super.onBackPressed();
        }
    }
}
