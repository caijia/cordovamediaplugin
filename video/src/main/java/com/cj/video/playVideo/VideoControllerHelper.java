package com.cj.video.playVideo;

/**
 * Created by cai.jia on 2017/7/4 0004
 */

public class VideoControllerHelper implements OnPlayMediaListener {

    private Controller controller;

    public VideoControllerHelper(Controller controller) {
        this.controller = controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void attachVideoView(VideoView videoView) {
        videoView.setOnPlayMediaListener(this);
        if (controller != null) {
            controller.attachVideoView(videoView);
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        if (controller != null) {
            controller.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onCompletion() {
        if (controller != null) {
            controller.onCompletion();
        }
    }

    @Override
    public boolean onError(int what, int extra) {
        if (controller != null) {
            controller.onError();
        }
        return true;
    }

    @Override
    public void onBufferStart(int speed) {
        if (controller != null) {
            controller.onBufferStart(speed);
        }
    }

    @Override
    public void onBufferEnd(int speed) {
        if (controller != null) {
            controller.onBufferEnd(speed);
        }
    }

    @Override
    public void onVideoRotation(int rotation) {
    }

    @Override
    public void onPrepared() {
        if (controller != null) {
            controller.onPrepared();
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
    }

    @Override
    public void onPlayMediaProgress(long duration, long currentPosition) {
        if (controller != null) {
            controller.onPlayProgress(currentPosition, duration);
        }
    }

    @Override
    public void onStart() {
        if (controller != null) {
            controller.onStart();
        }
    }

    @Override
    public void onPause() {
        if (controller != null) {
            controller.onPause();
        }
    }

    @Override
    public void onPreparing() {
        if (controller != null) {
            controller.onPreparing();
        }
    }
}
