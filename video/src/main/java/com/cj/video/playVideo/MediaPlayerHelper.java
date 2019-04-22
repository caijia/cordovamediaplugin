package com.cj.video.playVideo;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static tv.danmaku.ijk.media.player.IMediaPlayer.MEDIA_ERROR_UNKNOWN;

/**
 * Created by cai.jia on 2017/6/2 0002
 */

public class MediaPlayerHelper implements IjkMediaPlayer.OnPreparedListener,
        IjkMediaPlayer.OnCompletionListener, IjkMediaPlayer.OnErrorListener,
        IjkMediaPlayer.OnBufferingUpdateListener, IjkMediaPlayer.OnInfoListener,
        MediaProgressHelper.OnPlayMediaProgressListener, IjkMediaPlayer.OnVideoSizeChangedListener {

    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private IMediaPlayer mediaPlayer;
    private OnPlayMediaListener callback;
    private MediaProgressHelper progressHelper;

    private int currentState = STATE_IDLE;

    private Context context;
    public MediaPlayerHelper(Context context) {
        this.context = context;
    }

    public void setOnPlayMediaListener(OnPlayMediaListener callback) {
        this.callback = callback;
    }

    public void setDataSource(@NonNull String url) {
        setDataSource(url, null);
    }

    public void setDataSource(@NonNull String url, @Nullable Surface surface) {
        try {
            release();
            if (mediaPlayer == null) {
                mediaPlayer = new IjkMediaPlayer();
            }

            if (progressHelper == null) {
                progressHelper = new MediaProgressHelper(this);
            }
            progressHelper.setOnPlayMediaProgressListener(this);
            mediaPlayer.setDataSource(context, Uri.parse(url));
            if (surface != null) {
                mediaPlayer.setSurface(surface);
            }

            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);

        } catch (IOException e) {
            e.printStackTrace();
            currentState = STATE_ERROR;
            if (callback != null) {
                callback.onError(MEDIA_ERROR_UNKNOWN, -1);
            }
        }
    }

    public void prepareAsync() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.prepareAsync();
                currentState = STATE_PREPARING;
                if (callback != null) {
                    callback.onPreparing();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            currentState = STATE_ERROR;
            if (callback != null) {
                callback.onError(MEDIA_ERROR_UNKNOWN, -1);
            }
        }
    }

    public void stopPlayback() {
        callback = null;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            currentState = STATE_IDLE;
        }

        if (progressHelper != null) {
            progressHelper.stop();
        }
    }

    public void start(String url,Surface surface) {
        if (isInPlaybackState()) {
            start();
            if (callback != null) {
                callback.onStart();
            }

        }else if (currentState == STATE_ERROR || currentState == STATE_IDLE){
            setDataSource(url,surface);
            prepareAsync();
        }
    }

    public void start() {
        if (isInPlaybackState()) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                currentState = STATE_PLAYING;

            }else if(currentState == STATE_PLAYBACK_COMPLETED){
                seekTo(0);
            }
        }

        if (progressHelper != null) {
            progressHelper.start();
        }
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                currentState = STATE_PAUSED;
                if (callback != null) {
                    callback.onPause();
                }
            }
        }

        if (progressHelper != null) {
            progressHelper.stop();
        }
    }

    public void seekTo(long currentPosition) {
        if (isInPlaybackState()) {
            mediaPlayer.seekTo(currentPosition);
        }
    }

    public void relativeSeekTo(int milliSeconds) {
        if (isInPlaybackState()) {
            seekTo(mediaPlayer.getCurrentPosition() + milliSeconds);
        }
    }

    public boolean isInPlaybackState() {
        return (mediaPlayer != null &&
                currentState != STATE_ERROR &&
                currentState != STATE_IDLE &&
                currentState != STATE_PREPARING);
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            currentState = STATE_IDLE;
        }
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        if (callback != null) {
            callback.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        currentState = STATE_PLAYBACK_COMPLETED;
        if (callback != null) {
            callback.onCompletion();
            long duration = mediaPlayer.getDuration();
            callback.onPlayMediaProgress(duration, duration);
        }

        if (progressHelper != null) {
            progressHelper.stop();
        }
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        release();
        currentState = STATE_ERROR;

        if (progressHelper != null) {
            progressHelper.stop();
        }

        if (callback != null) {
            callback.onError(what, extra);
            return true;
        }
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        if (callback == null) {
            return false;
        }

        switch (what) {
            case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED: {
                callback.onVideoRotation(extra);
                break;
            }

            case IMediaPlayer.MEDIA_INFO_BUFFERING_START: {
                callback.onBufferStart(extra);
                break;
            }

            case IMediaPlayer.MEDIA_INFO_BUFFERING_END: {
                callback.onBufferEnd(extra);
                break;
            }
        }

        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        currentState = STATE_PREPARED;
        if (callback != null) {
            callback.onPrepared();
        }
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int i2, int i3) {
        if (callback != null) {
            callback.onVideoSizeChanged(width, height);
        }
    }

    @Override
    public void onPlayMediaProgress(long duration, long currentPosition) {
        if (callback != null) {
            callback.onPlayMediaProgress(duration, currentPosition);
        }
    }

    public long getCurrentPosition() {
        if (isInPlaybackState()) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void setSurface(Surface surface) {
        if (mediaPlayer != null) {
            mediaPlayer.setSurface(surface);
        }
    }

    public long getDuration() {
        if (isInPlaybackState()) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }
}
