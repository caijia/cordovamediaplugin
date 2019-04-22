package com.cj.video.player;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;
import android.view.TextureView;

import java.io.FileDescriptor;
import java.util.HashSet;
import java.util.Set;

import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by cai.jia on 2017/8/23.
 */

public class IjkPlayerWrapper implements PlayerWrapper, IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnVideoSizeChangedListener, TextureView.SurfaceTextureListener {

    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_STOPPED = 5;
    private static final int STATE_COMPLETED = 6;
    private static final int STATE_BUFFER_START = 7;
    private static final int RELOAD_BUFFER_TIME = 2000;
    private int state = STATE_IDLE;
    private ProgressHandler playProgressHandler;
    private IMediaPlayer player;
    private Set<OnPlayEventListener> playEventListeners;
    private Bundle params;
    private int cachePercent;
    private TextureView textureView;
    private int displayMode = PlayerConstants.DISPLAY_WRAP_CONTENT;
    private int videoWidth;
    private int videoHeight;
    private SurfaceTexture surfaceTexture;
    private Context context;
    /**
     * 当错误时记录当前播放位置,但在发生错误的时候拿不到当前播放位置,所有在更新播放进度的时候获取播放位置,
     * 位置误差取决于多少时间更新一次播放进度
     */
    private long previousPlayPosition;
    private String uri;
    private AssetFileDescriptor afd;
    private FileDescriptor fd;

    public IjkPlayerWrapper(Context context) {
        this.context = context;
        player = new IjkExoMediaPlayer(context);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnBufferingUpdateListener(this);
        player.setOnInfoListener(this);
        player.setOnVideoSizeChangedListener(this);
        params = new Bundle();
        playProgressHandler = new ProgressHandler(this);
        playEventListeners = new HashSet<>();
    }

    private void setProgressInterval(int interval) {
        if (playProgressHandler != null) {
            playProgressHandler.setInterval(interval);
        }
    }

    private void setMediaPlayerWrapper(IjkPlayerWrapper wrapper) {
        this.player = wrapper.player;
        this.params = wrapper.params;
        this.state = wrapper.state;
        this.previousPlayPosition = wrapper.previousPlayPosition;
        this.playProgressHandler.stop();
        this.playProgressHandler = wrapper.playProgressHandler;
        this.playProgressHandler.setInterval(1000);
        if (playEventListeners != null) {
            for (OnPlayEventListener playEventListener : playEventListeners) {
                wrapper.addOnPlayEventListener(playEventListener);
            }
        }
        this.playEventListeners = wrapper.playEventListeners;
    }

    private void startUpdateProgress() {
        if (playProgressHandler != null) {
            playProgressHandler.start();
        }
    }

    private void stopUpdateProgress() {
        if (playProgressHandler != null) {
            playProgressHandler.stop();
        }
    }

    @Override
    public void setSurface(Surface surface) {
        if (player != null && surface != null) {
            player.setSurface(surface);
        }
    }

    @Override
    public void prepare() {
        state = STATE_PREPARING;
        player.prepareAsync();
        dispatchEvent(PlayerConstants.PLAY_EVENT_INVOKE_PLAY);
    }

    @Override
    public boolean canPlay() {
        return player != null && (state == STATE_PREPARED || state == STATE_PLAYING
                || state == STATE_PAUSED || state == STATE_COMPLETED || state == STATE_BUFFER_START);
    }

    @Override
    public void setDataSource(String uri, AssetFileDescriptor afd, FileDescriptor fd) {
        this.uri = uri;
        this.afd = afd;
        this.fd = fd;
        try {
            if (player != null) {
                if (uri != null) {
                    player.setDataSource(uri);

                } else if (afd != null) {
                    setDataSource(afd);

                } else if (fd != null) {
                    setDataSource(fd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            state = STATE_ERROR;
            dispatchEvent(PlayerConstants.PLAY_EVENT_ERROR);
        }
    }

    @Override
    public void startPlay(String uri, AssetFileDescriptor afd, FileDescriptor fd) {
        if (player != null && (state == STATE_PLAYING || state == STATE_PREPARED ||
                state == STATE_PREPARING)) {
            return;
        }
        stop();
        setDataSource(uri, afd, fd);
        prepare();
        startUpdateProgress();
    }

    private void setDataSource(AssetFileDescriptor afd) {
        try {
            if (player != null) {
                setDataSource(afd.getFileDescriptor());
            }
        } catch (Exception e) {
            e.printStackTrace();
            state = STATE_ERROR;
            dispatchEvent(PlayerConstants.PLAY_EVENT_ERROR);
        }
    }

    private void setDataSource(FileDescriptor fileDescriptor) {
        try {
            if (player != null) {
                player.setDataSource(fileDescriptor);
            }
        } catch (Exception e) {
            e.printStackTrace();
            state = STATE_ERROR;
            dispatchEvent(PlayerConstants.PLAY_EVENT_ERROR);
        }
    }

    /**
     * 有时候可能会一直卡在缓冲状态,这时候我们可以判断一个timeout,将其视为error
     *
     * @return
     */
    private boolean isBufferStart() {
        return state == STATE_BUFFER_START;
    }

    @Override
    public void start() {
        if (canPlay()) {
            player.start();
            state = STATE_PLAYING;
            dispatchEvent(PlayerConstants.PLAY_EVENT_BEGIN);

        } else {
            if (state == STATE_STOPPED) {
                prepare();

            } else if (state == STATE_ERROR) {
                player.reset();
                setDataSource(uri, afd, fd);
                prepare();
                setSurface(createSurface());
            }
        }
    }

    private Surface createSurface() {
        if (textureView == null || this.surfaceTexture == null) {
            return null;
        }
        return new Surface(this.surfaceTexture);
    }

    @Override
    public void stop() {
        if (canPlay()) {
            player.stop();
            state = STATE_STOPPED;
            dispatchEvent(PlayerConstants.PLAY_EVENT_END);
        }
    }

    @Override
    public void resume() {
        start();
    }

    @Override
    public void pause() {
        if (canPlay()) {
            player.pause();
            state = STATE_PAUSED;
            dispatchEvent(PlayerConstants.PLAY_EVENT_PAUSE);
        }
    }

    private void releaseMediaPlayer(boolean clearEvent) {
        stop();
        if (player != null) {
            player.setSurface(null);
            player.setOnPreparedListener(null);
            player.setOnCompletionListener(null);
            player.setOnErrorListener(null);
            player.setOnBufferingUpdateListener(null);
            player.setOnInfoListener(null);
            player.setOnVideoSizeChangedListener(null);
            player.release();
            player = null;
        }

        stopUpdateProgress();

        if (clearEvent) {
            clearPlayEventListeners();
        }
    }

    private void clearPlayEventListeners() {
        if (playEventListeners != null) {
            playEventListeners.clear();
        }
    }

    @Override
    public void destroy() {
        releaseMediaPlayer(true);
        releaseSurface();
    }

    private void releaseSurface() {
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
    }

    @Override
    public boolean isPlaying() {
        return canPlay() && player.isPlaying();
    }

    @Override
    public boolean isPause() {
        return state == STATE_PAUSED;
    }

    @Override
    public boolean isComplete() {
        return state == STATE_COMPLETED;
    }

    private void seekToLong(long progress) {
        if (canPlay()) {
            long duration = player.getDuration();
            player.seekTo(progress < 0 ? 0 : (int) (progress > duration ? duration : progress));
        }
    }

    @Override
    public void seekTo(int progress) {
        seekToLong(progress * 1000);
    }

    @Override
    public void setTextureView(TextureView textureView) {
        this.textureView = textureView;
        textureView.setSurfaceTextureListener(this);
    }

    /**
     * 重新加载新的播放地址(适应于视频清晰度播放等)
     * 重开一个新的播放器,播放新的视频源(禁音),准备完成后,seekTo到当前播放的位置(同步当前播放器的位置),当播放不卡顿时,
     * 设置surfaceView，释放掉前一个播放器(不禁音)。
     * <p>
     * 播放器有时收不到错误消息,设置超时时间(10s),超过时间如果还没播放，视为播放错误
     * <p>
     * 当清晰度切换完成后,收到{@link PlayerConstants#PLAY_EVENT_RELOAD_SUCCESS}
     * 当清晰度切换错误,收到{@link PlayerConstants#PLAY_EVENT_RELOAD_ERROR}
     * 当清晰度切换开始,收到{@link PlayerConstants#PLAY_EVENT_RELOAD_START}
     */
    @Override
    public void reload(String url) {

        if (canPlay() && !TextUtils.isEmpty(url)) {
            dispatchEvent(PlayerConstants.PLAY_EVENT_RELOAD_START);
            //新的播放器
            final IjkPlayerWrapper wrapper = new IjkPlayerWrapper(context);
            wrapper.setProgressInterval(RELOAD_BUFFER_TIME);
            wrapper.setPlayerMute(1); //禁音
            final long startTime = System.currentTimeMillis();
            final int[] count = {0};
            wrapper.addOnPlayEventListener(new OnPlayEventListener() {
                @Override
                public void onPlayEvent(int event, Bundle params) {
                    switch (event) {
                        case PlayerConstants.PLAY_EVENT_BEGIN: {
                            if (!IjkPlayerWrapper.this.canPlay()) {
                                //旧播放器不能播放,视为切换清晰度错误
                                wrapper.releaseMediaPlayer(true);
                                IjkPlayerWrapper.this.dispatchEvent(PlayerConstants.PLAY_EVENT_RELOAD_ERROR);
                            }
                            break;
                        }

                        case PlayerConstants.PLAY_EVENT_PROGRESS: {
                            //判断超时时间,超过10s,新播放器还不能播放,视为切换清晰度错误
                            //有时候播放器可能一直卡在buffingStart状态,这时候超过10s也视为错误
                            //不管什么原因,超过时间直接错误处理
                            if (System.currentTimeMillis() - startTime > 10 * 1000) {
                                wrapper.releaseMediaPlayer(true);
                                IjkPlayerWrapper.this.dispatchEvent(PlayerConstants.PLAY_EVENT_RELOAD_ERROR);
                                return;
                            }

                            long oldProgress = IjkPlayerWrapper.this.getLongProgress();
                            //同步进度
                            wrapper.seekToLong(oldProgress);
                            long newProgress = wrapper.getLongProgress();
                            if (!IjkPlayerWrapper.this.canPlay()) {
                                //旧的播放器不能播了,那么清晰度切换终止
                                wrapper.releaseMediaPlayer(true);
                                IjkPlayerWrapper.this.dispatchEvent(PlayerConstants.PLAY_EVENT_RELOAD_ERROR);
                                return;
                            }

                            if (oldProgress == newProgress && wrapper.isPlaying()) {
                                ++count[0];
                                if (count[0] > 1) {
                                    wrapper.clearPlayEventListeners();
                                    wrapper.setSurface(IjkPlayerWrapper.this.createSurface());

                                    IjkPlayerWrapper.this.releaseMediaPlayer(false);
                                    wrapper.setPlayerMute(0); //不禁音
                                    IjkPlayerWrapper.this.setMediaPlayerWrapper(wrapper);
                                    IjkPlayerWrapper.this.dispatchEvent(PlayerConstants.PLAY_EVENT_RELOAD_SUCCESS);
                                }
                            }
                            break;
                        }

                        case PlayerConstants.PLAY_EVENT_END:
                        case PlayerConstants.PLAY_EVENT_ERROR: {
                            //新播放器不能播了,那么清晰度切换终止
                            wrapper.releaseMediaPlayer(true);
                            IjkPlayerWrapper.this.dispatchEvent(PlayerConstants.PLAY_EVENT_RELOAD_ERROR);
                            break;
                        }
                    }
                }
            });
            wrapper.startPlay(url, null, null);
        }
    }

    @Override
    public void setPlayerMute(int mute) {

    }

    @Override
    public void addOnPlayEventListener(OnPlayEventListener playEventListener) {
        if (playEventListener != null) {
            playEventListeners.add(playEventListener);
        }
    }

    public void removePlayEventListener(OnPlayEventListener playEventListener) {
        if (playEventListener != null) {
            playEventListeners.remove(playEventListener);
        }
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        state = STATE_PREPARED;
        dispatchEvent(PlayerConstants.PLAY_EVENT_PREPARED);
        if (previousPlayPosition > 0) {
            seekToLong(previousPlayPosition);
        }
        start();
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        state = STATE_COMPLETED;
        dispatchEventProgress(getDuration(), getDuration(), getDuration());
        dispatchEvent(PlayerConstants.PLAY_EVENT_END);
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        state = STATE_ERROR;
        dispatchEvent(PlayerConstants.PLAY_EVENT_ERROR);
        return true;
    }

    @Override
    public void setLooper(boolean isLooper) {
        if (player != null) {
            player.setLooping(true);
        }
    }

    @Override
    public void setVideoDisplayMode(int displayMode) {
        this.displayMode = displayMode;
    }

    private int getDuration() {
        if (canPlay()) {
            return (int) player.getDuration() / 1000;
        }
        return 0;
    }

    private int getProgress() {
        if (canPlay()) {
            return (int) player.getCurrentPosition() / 1000;
        }
        return 0;
    }

    private long getLongProgress() {
        if (canPlay()) {
            return (int) player.getCurrentPosition();
        }
        return 0;
    }

    private int getCacheProgress() {
        if (canPlay()) {
            return (int) (getDuration() * (cachePercent / 100f));
        }
        return 0;
    }

    private void setTempPlayPosition(long previousPlayPosition) {
        if (previousPlayPosition <= 0) {
            return;
        }
        this.previousPlayPosition = previousPlayPosition;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
        cachePercent = percent;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START: {
                state = STATE_BUFFER_START;
                params.putInt(PlayerConstants.PARAMS_NET_SPEED, extra);
                dispatchEvent(PlayerConstants.PLAY_EVENT_LOADING);
                break;
            }

            case IMediaPlayer.MEDIA_INFO_BUFFERING_END: {
                state = STATE_PLAYING;
                dispatchEvent(PlayerConstants.PLAY_EVENT_BUFFERING_END);
                break;
            }
        }
        return false;
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int i, int i2) {
        videoWidth = width;
        videoHeight = height;
        params.putInt(PlayerConstants.PARAMS_VIDEO_WIDTH, videoWidth);
        params.putInt(PlayerConstants.PARAMS_VIDEO_HEIGHT, videoHeight);
        dispatchEvent(PlayerConstants.PLAY_EVENT_VIDEO_SIZE_CHANGE);
    }

    private void dispatchEvent(int event) {
        for (OnPlayEventListener playEventListener : playEventListeners) {
            if (playEventListener != null) {
                playEventListener.onPlayEvent(event, params);
            }
        }

        switch (event) {
            case PlayerConstants.PLAY_EVENT_PREPARED:
            case PlayerConstants.PLAY_EVENT_BEGIN:
            case PlayerConstants.PLAY_EVENT_BUFFERING_END:
                startUpdateProgress();
                break;

            case PlayerConstants.PLAY_EVENT_END:
            case PlayerConstants.PLAY_EVENT_ERROR:
                stopUpdateProgress();
                break;
        }
    }

    private void dispatchEventProgress(int duration, int progress, int secondProgress) {
        params.putInt(PlayerConstants.PARAMS_PLAY_DURATION, duration);
        params.putInt(PlayerConstants.PARAMS_PLAY_PROGRESS, progress);
        params.putInt(PlayerConstants.PARAMS_PLAY_SECOND_PROGRESS, secondProgress);
        params.putInt(PlayerConstants.PARAMS_CACHE_PERCENT, cachePercent);

        for (OnPlayEventListener playEventListener : playEventListeners) {
            if (playEventListener != null) {
                playEventListener.onPlayEvent(PlayerConstants.PLAY_EVENT_PROGRESS, params);
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (this.surfaceTexture == null) {
            this.surfaceTexture = surfaceTexture;
            Surface surface = new Surface(this.surfaceTexture);
            setSurface(surface);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                textureView.setSurfaceTexture(this.surfaceTexture);
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private static class ProgressHandler extends Handler {

        private static final int MSG_PROGRESS = 3001;
        IjkPlayerWrapper player;
        boolean isStop;
        private int interval = 1000;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (!isStop) {
                    sendMessage(obtainProgressMessage());
                    postDelayed(this, interval);
                }
            }
        };

        ProgressHandler(IjkPlayerWrapper ijkPlayerWrapper) {
            super(Looper.getMainLooper());
            player = ijkPlayerWrapper;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PROGRESS: {
                    if (player != null && player.canPlay()) {
                        //保存当前的播放进度
                        player.setTempPlayPosition(player.getLongProgress());

                        player.dispatchEventProgress(
                                player.getDuration(),
                                player.getProgress(),
                                player.getCacheProgress());
                    }
                    break;
                }
            }
        }

        void start() {
            isStop = false;
            removeCallbacksAndMessages(null);
            post(task);
        }

        private Message obtainProgressMessage() {
            Message msg = Message.obtain();
            msg.what = MSG_PROGRESS;
            return msg;
        }

        void stop() {
            isStop = true;
            removeCallbacksAndMessages(null);
        }
    }
}
