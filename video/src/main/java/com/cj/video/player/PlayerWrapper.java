package com.cj.video.player;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;

import java.io.FileDescriptor;

/**
 * Created by cai.jia on 2017/8/23.
 */

public interface PlayerWrapper{

    void setSurface(Surface surface);

    void setDataSource(String uri, AssetFileDescriptor afd, FileDescriptor fd);

    void prepare();

    void start();

    void stop();

    boolean canPlay();

    boolean isPlaying();

    boolean isPause();

    boolean isComplete();

    void seekTo(int progress);

    void resume();

    void pause();

    void destroy();

    void startPlay(String uri, AssetFileDescriptor afd, FileDescriptor fd);

    void reload(String url);

    void setPlayerMute(int mute);

    void setVideoDisplayMode(int displayMode);

    void setTextureView(TextureView textureView);

    void addOnPlayEventListener(OnPlayEventListener playEventListener);

    void setLooper(boolean isLooper);

    interface OnPlayEventListener{

        void onPlayEvent(int event, Bundle params);
    }
}
