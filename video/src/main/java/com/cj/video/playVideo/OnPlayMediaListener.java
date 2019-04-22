package com.cj.video.playVideo;

public interface OnPlayMediaListener {

    void onBufferingUpdate(int percent);

    void onCompletion();

    boolean onError(int what, int extra);

    void onBufferStart(int speed);

    void onBufferEnd(int speed);

    void onVideoRotation(int rotation);

    void onPrepared();

    void onVideoSizeChanged(int width, int height);

    void onPlayMediaProgress(long duration, long currentPosition);

    void onStart();

    void onPause();

    void onPreparing();
}