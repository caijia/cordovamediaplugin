package com.cj.video.playVideo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by cai.jia on 2017/6/29 0029
 */

public class MediaProgressHelper implements Runnable {

    private static final int MSG_GET_PLAY_DURATION = 1900;
    private MediaPlayerHelper playerHelper;
    private InternalHandler handler;
    private boolean isStop;
    private OnPlayMediaProgressListener playMediaProgressListener;

    public MediaProgressHelper(MediaPlayerHelper playerHelper) {
        this.playerHelper = playerHelper;
        handler = new InternalHandler(this);
    }

    public void start() {
        isStop = false;
        handler.removeMessages(MSG_GET_PLAY_DURATION);
        handler.removeCallbacks(this);
        handler.sendMessage(handler.obtainMessage(MSG_GET_PLAY_DURATION));
        handler.postDelayed(this, 1000);
    }

    public void stop() {
        isStop = true;
        handler.removeMessages(MSG_GET_PLAY_DURATION);
        handler.removeCallbacks(this);
    }

    private void onPlayMediaProgress(long duration, long currentPosition) {
        if (playMediaProgressListener != null) {
            playMediaProgressListener.onPlayMediaProgress(duration, currentPosition);
        }
    }

    @Override
    public void run() {
        if (!isStop) {
            handler.sendMessage(handler.obtainMessage(MSG_GET_PLAY_DURATION));
            handler.postDelayed(this, 1000);
        }
    }

    public void setOnPlayMediaProgressListener(OnPlayMediaProgressListener listener) {
        this.playMediaProgressListener = listener;
    }

    public interface OnPlayMediaProgressListener {

        void onPlayMediaProgress(long duration, long currentPosition);
    }

    private static class InternalHandler extends Handler {

        WeakReference<MediaProgressHelper> ref;

        public InternalHandler(MediaProgressHelper progressHelper) {
            super(Looper.getMainLooper());
            ref = new WeakReference<>(progressHelper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_GET_PLAY_DURATION) {
                MediaProgressHelper progressHelper = ref.get();
                if (progressHelper != null && progressHelper.playerHelper != null) {
                    MediaPlayerHelper playerHelper = progressHelper.playerHelper;
                    long currentPosition = playerHelper.getCurrentPosition();
                    long duration = playerHelper.getDuration();
                    progressHelper.onPlayMediaProgress(duration, currentPosition);
                }
            }
        }
    }

}
