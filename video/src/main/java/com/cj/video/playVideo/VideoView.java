package com.cj.video.playVideo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

/**
 * Created by cai.jia on 2017/6/30 0030
 */

public class VideoView extends TextureView implements TextureView.SurfaceTextureListener,
        OnPlayMediaListener {

    public static final int WRAP_CONTENT = 1;
    public static final int CENTER_CROP = 2;

    private MediaPlayerHelper playerHelper;
    private int videoWidth;
    private int videoHeight;
    private Surface surface;
    private OnPlayMediaListener callback;

    private int scaleType = WRAP_CONTENT;
    private int rotation;
    private SurfaceTexture surfaceTexture;
    private String videoUrl;
    private boolean surfaceAvailable;

    public VideoView(@NonNull Context context) {
        this(context, null);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs,
                     @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs,
                     @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        playerHelper = new MediaPlayerHelper(context);
        playerHelper.setOnPlayMediaListener(this);
        setSurfaceTextureListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        playerHelper.setOnPlayMediaListener(this);
//        setVideoScaleType();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        surfaceAvailable = true;
        if (this.surfaceTexture == null) {
            this.surfaceTexture = surfaceTexture;
            surface = new Surface(this.surfaceTexture);
            if (waitStart && !TextUtils.isEmpty(videoUrl)) {
                start(videoUrl);
            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (playerHelper.isInPlaybackState()) {
                    setSurfaceTexture(this.surfaceTexture);
                }

            } else {
                surface = new Surface(surfaceTexture);
                setSurface(surface);
                start(videoUrl);
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        setVideoScaleType();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        surfaceAvailable = false;
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        setVideoScaleType();
    }

    private boolean waitStart;

    public void start(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        videoUrl = url;
        if (playerHelper != null && surfaceAvailable) {
            waitStart = false;
            playerHelper.start(url, surface);
        }else{
            waitStart = true;
        }
    }

    public void release() {
        if (surfaceTexture != null) {
            surfaceTexture.release();
            this.surfaceTexture = null;
        }

        if (surface != null) {
            surface.release();
            surface = null;
        }

        if (playerHelper != null) {
            playerHelper.stopPlayback();
        }
    }

    public void pause() {
        if (playerHelper != null) {
            playerHelper.pause();
        }
    }

    private void setSurface(Surface surface) {
        if (playerHelper != null) {
            playerHelper.setSurface(surface);
        }
    }

    public void seekTo(int milliSeconds) {
        if (playerHelper != null) {
            playerHelper.seekTo(milliSeconds);
        }
    }

    public void relativeSeekTo(int milliSeconds) {
        if (playerHelper != null) {
            playerHelper.relativeSeekTo(milliSeconds);
        }
    }

    public void setOnPlayMediaListener(OnPlayMediaListener callback) {
        this.callback = callback;
    }

    @Override
    public void onBufferingUpdate(int percent) {
        if (callback != null) {
            callback.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onCompletion() {
        if (callback != null) {
            callback.onCompletion();
        }
    }

    @Override
    public void onStart() {
        if (callback != null) {
            callback.onStart();
        }
    }

    @Override
    public void onPause() {
        if (callback != null) {
            callback.onPause();
        }
    }

    @Override
    public void onPreparing() {
        if (callback != null) {
            callback.onPreparing();
        }
    }

    @Override
    public boolean onError(int what, int extra) {
        if (callback != null) {
            callback.onError(what, extra);
        }
        return true;
    }

    @Override
    public void onBufferStart(int speed) {
        if (callback != null) {
            callback.onBufferStart(speed);
        }
    }

    @Override
    public void onBufferEnd(int speed) {
        if (callback != null) {
            callback.onBufferEnd(speed);
        }
    }

    @Override
    public void onVideoRotation(int rotation) {
        if (callback != null) {
            callback.onVideoRotation(rotation);
        }
        this.rotation = rotation;
    }

    @Override
    public void onPrepared() {
        if (callback != null) {
            callback.onPrepared();
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        setVideoScaleType();
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

    private void setVideoScaleType() {
        switch (scaleType) {
            case CENTER_CROP: {
                TextureTransformHelper.centerCrop(this, videoWidth, videoHeight, rotation);
                break;
            }

            case WRAP_CONTENT: {
                TextureTransformHelper.wrapContent(this, videoWidth, videoHeight, rotation);
                break;
            }
        }
    }

    public void setScaleType(int scaleType) {
        this.scaleType = scaleType;
    }
}
