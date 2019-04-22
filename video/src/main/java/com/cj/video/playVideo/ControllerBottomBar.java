package com.cj.video.playVideo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cj.video.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by cai.jia on 2017/7/6 0006
 */

public class ControllerBottomBar extends LinearLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private TextView voiceTv;
    private TextView currentTimeTv;
    private SeekBar progressSeekBar;
    private TextView totalTimeTv;
    private TextView fullScreenTv;
    private ViewGroup videoContainerParent;
    private ViewGroup videoContainer;
    private int maxVolume;

    public ControllerBottomBar(Context context) {
        this(context, null);
    }

    public ControllerBottomBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControllerBottomBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ControllerBottomBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.controller_bottom_bar, this, true);
        setOrientation(VERTICAL);
        voiceTv = (TextView) findViewById(R.id.video_voice_tv);
        currentTimeTv = (TextView) findViewById(R.id.video_current_time_tv);
        progressSeekBar = (SeekBar) findViewById(R.id.video_play_progress_seek_bar);
        totalTimeTv = (TextView) findViewById(R.id.video_total_time_tv);
        fullScreenTv = (TextView) findViewById(R.id.video_full_screen_tv);

        voiceTv.setOnClickListener(this);
        fullScreenTv.setOnClickListener(this);
        progressSeekBar.setOnSeekBarChangeListener(this);
        maxVolume = ControllerUtil.getMaxVolume(context);
        setCurrentVolume(ControllerUtil.getVolume(context));
        hide();
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void reset() {
        currentTimeTv.setText("00:00");
        totalTimeTv.setText("00:00");
        progressSeekBar.setProgress(0);
        progressSeekBar.setSecondaryProgress(0);
    }

    @Override
    public void onClick(View v) {
        if (v == voiceTv) {
            v.setSelected(!v.isSelected());
            boolean hasVolume = v.isSelected();
            int currentVolume = ControllerUtil.getVolume(getContext());
            currentVolume = currentVolume == 0 ? maxVolume / 3 : currentVolume;
            ControllerUtil.setVolume(getContext(), hasVolume ? currentVolume : 0);

        } else if (v == fullScreenTv) {
            toggleFullScreen();
        }
    }

    public void setFullScreenLayout(ViewGroup videoContainerParent, ViewGroup videoContainer) {
        this.videoContainer = videoContainer;
        this.videoContainerParent = videoContainerParent;
    }

    public boolean isFullScreen() {
        if (videoContainerParent == null || videoContainer == null) {
            return false;
        }

        int index = videoContainerParent.indexOfChild(videoContainer);
        return index == -1;
    }

    public void toggleFullScreen() {
        if (videoContainerParent == null || videoContainer == null) {
            return;
        }

        int index = videoContainerParent.indexOfChild(videoContainer);
        boolean notFullScreen = index != -1;
        Activity activity = ControllerUtil.getActivity(getContext());
        if (activity == null) {
            return;
        }

        ViewGroup content = (ViewGroup) activity.findViewById(android.R.id.content);
        if (notFullScreen) {
            videoContainer.setBackgroundColor(Color.BLACK);
            videoContainerParent.removeView(videoContainer);
            content.addView(videoContainer, MATCH_PARENT, MATCH_PARENT);
            ControllerUtil.toggleActionBarAndStatusBar(getContext(), true);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else {
            videoContainer.setBackgroundColor(Color.TRANSPARENT);
            content.removeView(videoContainer);
            videoContainerParent.addView(videoContainer, MATCH_PARENT, MATCH_PARENT);
            ControllerUtil.toggleActionBarAndStatusBar(getContext(), false);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void setCurrentVolume(int currentVolume) {
        boolean hasVolume = currentVolume > 0;
        voiceTv.setSelected(hasVolume);
    }

    public void setProgress(long progress) {
        String currentTime = ControllerUtil.formatTime(progress);
        currentTimeTv.setText(currentTime);
        progressSeekBar.setProgress((int) progress);
    }

    public void setMax(long max) {
        String totalTime = ControllerUtil.formatTime(max);
        totalTimeTv.setText(totalTime);
        progressSeekBar.setMax((int) max);
    }

    public void setSecondaryProgress(int secondaryProgress) {
        progressSeekBar.setSecondaryProgress(secondaryProgress);
    }

    public int getMax() {
        return progressSeekBar.getMax();
    }

    public int getProgress() {
        return progressSeekBar.getProgress();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currentTimeTv.setText(ControllerUtil.formatTime(progress));
        totalTimeTv.setText(ControllerUtil.formatTime(seekBar.getMax()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (onPlayProgressChangeListener != null) {
            onPlayProgressChangeListener.onPlayProgressChange(seekBar.getProgress());
        }
    }

    public interface OnPlayProgressChangeListener{

        void onPlayProgressChange(int progress);
    }

    private OnPlayProgressChangeListener onPlayProgressChangeListener;

    public void setOnPlayProgressChangeListener(OnPlayProgressChangeListener l) {
        this.onPlayProgressChangeListener = l;
    }
}
