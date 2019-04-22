package com.cj.video.playVideo;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cj.video.R;

/**
 * Created by cai.jia on 2017/7/6 0006
 */

public class ControllerSwitcher extends FrameLayout implements View.OnClickListener {

    public static final int STATE_START_PAUSE = 0;
    public static final int STATE_BRIGHTNESS = 1;
    public static final int STATE_VOLUME = 2;
    public static final int STATE_PROGRESS = 3;
    private TextView startPauseTv;
    private ProgressBar brightnessProgressBar;
    private ProgressBar volumeProgressBar;
    private TextView incrementTimeTv;
    private TextView currentTimeTv;
    private TextView totalTimeTv;
    private ProgressBar timeProgressBar;
    private View[] viewArray;
    private OnPlayStateListener onPlayStateListener;
    private int volume;
    private int brightness;
    private long currentProgress;
    private boolean isGestureTimeProgress;

    public ControllerSwitcher(Context context) {
        this(context, null);
    }

    public ControllerSwitcher(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControllerSwitcher(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ControllerSwitcher(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.controller_view_switcher, this, true);
        startPauseTv = (TextView) findViewById(R.id.video_start_pause_tv);
        brightnessProgressBar = (ProgressBar) findViewById(R.id.brightness_progress_bar);
        volumeProgressBar = (ProgressBar) findViewById(R.id.volume_progress_bar);
        incrementTimeTv = (TextView) findViewById(R.id.increment_time_tv);
        currentTimeTv = (TextView) findViewById(R.id.current_time_tv);
        totalTimeTv = (TextView) findViewById(R.id.total_time_tv);
        timeProgressBar = (ProgressBar) findViewById(R.id.time_progress_bar);
        FrameLayout brightnessFl = (FrameLayout) findViewById(R.id.brightness_fl);
        FrameLayout volumeFl = (FrameLayout) findViewById(R.id.volume_fl);
        RelativeLayout timeFl = (RelativeLayout) findViewById(R.id.time_fl);
        viewArray = new View[]{startPauseTv, brightnessFl, volumeFl, timeFl};

        startPauseTv.setOnClickListener(this);

        int maxVolume = ControllerUtil.getMaxVolume(context);
        int currentVolume = ControllerUtil.getVolume(context);
        volumeProgressBar.setMax(maxVolume);
        volumeProgressBar.setProgress(currentVolume);

        int currentBrightness = ControllerUtil.getActivityBrightness(context);
        brightnessProgressBar.setMax(255);
        brightnessProgressBar.setProgress(currentBrightness);
    }

    @Override
    public void onClick(View v) {
        if (v == startPauseTv) {
            boolean isPlaying = v.isSelected();
            if (onPlayStateListener != null) {
                onPlayStateListener.onPlayState(isPlaying);
            }
        }
    }

    public void show() {
        setVisibility(VISIBLE);
        setVisibleState(STATE_START_PAUSE);
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void setOnPlayStateListener(OnPlayStateListener playStateListener) {
        this.onPlayStateListener = playStateListener;
    }

    private void setVisibleState(int state) {
        if (state < 0 || state > 3) {
            return;
        }
        setVisibility(VISIBLE);
        int length = viewArray.length;
        for (int i = 0; i < length; i++) {
            viewArray[i].setVisibility(i == state ? VISIBLE : GONE);
        }
    }

    public void setPlayingState(boolean isPlaying) {
        if (isPlaying) {
            setPlayStartState();
        } else {
            setPlayStopState();
        }
    }

    private void setPlayStartState() {
        setVisibleState(STATE_START_PAUSE);
        startPauseTv.setSelected(true);
    }

    private void setPlayStopState() {
        setVisibleState(STATE_START_PAUSE);
        startPauseTv.setSelected(false);
    }

    public boolean isPlaying() {
        return startPauseTv.isSelected();
    }

    public void startSetVolume() {
        setVisibleState(STATE_VOLUME);
        volume = ControllerUtil.getVolume(getContext());
    }

    public void incrementVolume(int incrementVolume) {
        setVisibleState(STATE_VOLUME);
        int currentVolume = volume + incrementVolume;
        volumeProgressBar.setProgress(currentVolume + incrementVolume);
        ControllerUtil.setVolume(getContext(), currentVolume);
    }

    public void startSetBrightness() {
        setVisibleState(STATE_BRIGHTNESS);
        brightness = ControllerUtil.getActivityBrightness(getContext());
    }

    public void incrementBrightness(int incrementBrightness) {
        setVisibleState(STATE_BRIGHTNESS);
        int currentBrightness = brightness + incrementBrightness;
        brightnessProgressBar.setProgress(currentBrightness);
        ControllerUtil.setActivityBrightness(getContext(), currentBrightness);
    }

    public boolean isGestureTimeProgress() {
        return isGestureTimeProgress;
    }

    public long getCurrentProgress() {
        return currentProgress;
    }

    public void startTimeProgress(long currentProgress, long total) {
        this.currentProgress = currentProgress;
        isGestureTimeProgress = true;
        setVisibleState(STATE_PROGRESS);
        String currentTime = ControllerUtil.formatTime(currentProgress);
        String totalTime = ControllerUtil.formatTime(total);
        currentTimeTv.setText(currentTime);
        totalTimeTv.setText(totalTime);
        timeProgressBar.setMax((int) total);
        timeProgressBar.setProgress((int) currentProgress);
    }

    public void incrementTimeProgress(long progress) {
        setVisibleState(STATE_PROGRESS);
        String currentTime = ControllerUtil.formatTime(currentProgress + progress);
        String incrementTime = ControllerUtil.formatTime(true, progress);
        incrementTimeTv.setText(incrementTime);
        currentTimeTv.setText(currentTime);
        timeProgressBar.setProgress((int) (currentProgress + progress));
    }

    public void stopTimeProgress() {
        setVisibility(GONE);
        isGestureTimeProgress = false;
    }

    public interface OnPlayStateListener {

        void onPlayState(boolean isPlaying);
    }
}
