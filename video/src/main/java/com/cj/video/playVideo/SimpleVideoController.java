package com.cj.video.playVideo;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cj.video.R;

/**
 * Created by cai.jia on 2017/7/2.
 */
public class SimpleVideoController extends GestureVideoController implements
        Controller, ControllerSwitcher.OnPlayStateListener,
        ControllerBottomBar.OnPlayProgressChangeListener, View.OnClickListener {

    private ControllerLoading controllerLoading;
    private ControllerSwitcher controllerSwitcher;
    private ControllerBottomBar controllerBottomBar;
    private VideoView videoView;
    private String videoUrl;
    private boolean isShowController;
    private Handler handler;

    public SimpleVideoController(@NonNull Context context) {
        this(context, null);
    }

    public SimpleVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleVideoController(@NonNull Context context, @Nullable AttributeSet attrs,
                                 @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SimpleVideoController(@NonNull Context context, @Nullable AttributeSet attrs,
                                 @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.video_controller, this, true);
        controllerLoading = (ControllerLoading) findViewById(R.id.controller_loading);
        controllerSwitcher = (ControllerSwitcher) findViewById(R.id.view_switcher);
        controllerBottomBar = (ControllerBottomBar) findViewById(R.id.controller_bottom_bar);
        controllerSwitcher.setOnPlayStateListener(this);
        controllerBottomBar.setOnPlayProgressChangeListener(this);
        this.setOnClickListener(this);
        handler = new Handler();
    }

    public void onLeftVerticalMove(@MoveState int state, float distance,float deltaY) {
        if (!isPrepared) {
            return;
        }
        int dp = ControllerUtil.spToDp(getContext(), distance);
        int brightness = dp * 2;
        switch (state) {
            case GestureVideoController.START:
                controllerSwitcher.startSetBrightness();
                break;

            case GestureVideoController.MOVE:
                controllerSwitcher.incrementBrightness(brightness);
                break;

            case GestureVideoController.END:
                controllerSwitcher.hide();
                break;
        }
    }

    public void onRightVerticalMove(@MoveState int state, float distance,float deltaY) {
        if (!isPrepared) {
            return;
        }
        int dp = ControllerUtil.spToDp(getContext(), distance);
        int volume = (int) (dp * 0.1f);
        switch (state) {
            case GestureVideoController.START:
                controllerSwitcher.startSetVolume();
                break;

            case GestureVideoController.MOVE:
                controllerSwitcher.incrementVolume(volume);
                break;

            case GestureVideoController.END:
                controllerSwitcher.hide();
                break;
        }
    }

    public void onHorizontalMove(@MoveState int state, float distance,float deltaX) {
        if (!isPrepared) {
            return;
        }
        int dp = ControllerUtil.spToDp(getContext(), distance);
        int time = Math.round(dp * 0.5f) * 1000;
        switch (state) {
            case GestureVideoController.START:
                int currentProgress = controllerBottomBar.getProgress();
                int max = controllerBottomBar.getMax();
                controllerSwitcher.startTimeProgress(currentProgress, max);
                break;

            case GestureVideoController.MOVE:
                controllerSwitcher.incrementTimeProgress(time);
                long currentTime = controllerSwitcher.getCurrentProgress();
                controllerBottomBar.setProgress((int) (currentTime + time));
                break;

            case GestureVideoController.END:
                controllerSwitcher.stopTimeProgress();
                videoView.seekTo(controllerBottomBar.getProgress());
                break;
        }
    }

    @Override
    public void onPreparing() {
        controllerSwitcher.hide();
        controllerLoading.show();
    }

    private boolean isPrepared;

    @Override
    public void onPrepared() {
        isPrepared = true;
        controllerLoading.hide();
        videoView.start(videoUrl);
    }

    @Override
    public void onStart() {
        showController();
        controllerSwitcher.setPlayingState(true);
    }

    @Override
    public void onPause() {
        controllerSwitcher.setPlayingState(false);
    }

    @Override
    public void onCompletion() {
        controllerSwitcher.setPlayingState(false);
    }

    @Override
    public void onError() {
        controllerSwitcher.setPlayingState(false);
    }

    @Override
    public void onBufferStart(int speed) {
        controllerSwitcher.setPlayingState(false);
        controllerSwitcher.hide();
        controllerLoading.setNetSpeed(speed);
    }

    @Override
    public void onBufferEnd(int speed) {
        controllerLoading.hide();
        boolean isPlaying = controllerSwitcher.isPlaying();
        controllerSwitcher.setPlayingState(isPlaying);
        setPlaying(isPlaying);
    }

    @Override
    public void onPlayProgress(long progress, long total) {
        controllerBottomBar.setMax((int) total);
        boolean gestureTimeProgress = controllerSwitcher.isGestureTimeProgress();
        if (!gestureTimeProgress) {
            controllerBottomBar.setProgress((int) progress);
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        int secondaryProgress = Math.round(controllerBottomBar.getMax() * percent * 0.01f);
        controllerBottomBar.setSecondaryProgress(secondaryProgress);
    }

    @Override
    public void attachVideoView(VideoView view) {
        videoView = view;
    }

    @Override
    public void showController() {
        controllerBottomBar.show();
        controllerSwitcher.show();
        isShowController = true;
        handler.removeCallbacks(hideControllerTask);
        handler.postDelayed(hideControllerTask, 4000);
    }

    private Runnable hideControllerTask = new Runnable() {
        @Override
        public void run() {
            hideController();
        }
    };

    @Override
    public void hideController() {
        controllerBottomBar.hide();
        controllerSwitcher.hide();
        isShowController = false;
    }

    @Override
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @Override
    public void setParentLayout(ViewGroup parent, ViewGroup container) {
        controllerBottomBar.setFullScreenLayout(parent, container);
    }

    @Override
    public boolean isFullScreen() {
        return controllerBottomBar.isFullScreen();
    }

    @Override
    public void toggleFullScreen() {
        controllerBottomBar.toggleFullScreen();
    }

    @Override
    public void release() {
        handler.removeCallbacks(hideControllerTask);
        controllerBottomBar.hide();
//        controllerBottomBar.reset();
        controllerLoading.hide();
        controllerSwitcher.show();
    }

    private void setPlaying(boolean isPlaying) {
        if (isPlaying) {
            videoView.pause();

        } else {
            videoView.start(videoUrl);
        }
    }

    @Override
    public void onPlayState(boolean isPlaying) {
        setPlaying(isPlaying);
    }

    @Override
    public void onPlayProgressChange(int progress) {
        videoView.seekTo(progress);
    }

    @Override
    public void onClick(View v) {
        if (v == this) {
            if (!isPrepared) {
                return;
            }
            if (isShowController) {
                hideController();
            } else {
                showController();
            }
        }
    }
}
