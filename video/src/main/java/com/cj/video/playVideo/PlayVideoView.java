package com.cj.video.playVideo;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by cai.jia on 2017/7/2.
 */

public class PlayVideoView extends FrameLayout {

    private Controller controller;
    private VideoView videoView;
    private FrameLayout videoContainer;
    private VideoControllerHelper helper;
    private String videoUrl;
    public PlayVideoView(@NonNull Context context) {
        this(context, null);
    }
    public PlayVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public PlayVideoView(@NonNull Context context, @Nullable AttributeSet attrs,
                         @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayVideoView(@NonNull Context context, @Nullable AttributeSet attrs,
                         @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutParams p = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        videoContainer = new FrameLayout(context);
        addView(videoContainer, p);

        videoView = new VideoView(context);
        videoContainer.addView(videoView, p);

        setPlayController(new SimpleVideoController(context));
    }

    public void setPlayController(Controller controller) {
        if (this.controller != null) {
            videoContainer.removeView((View) this.controller);
        }
        this.controller = controller;

        LayoutParams p = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        videoContainer.addView((View) controller, p);
        controller.setParentLayout(this, videoContainer);

        if (helper == null) {
            helper = new VideoControllerHelper(controller);

        } else {
            helper.setController(controller);
        }
        helper.attachVideoView(videoView);

        if (!TextUtils.isEmpty(videoUrl)) {
            setVideoUrl(videoUrl);
        }
    }

    public void setScaleType(int scaleType) {
        if (videoView != null) {
            videoView.setScaleType(scaleType);
        }
    }

    public void release() {
        if (videoView != null) {
            videoView.release();
        }
    }

    public boolean onBackPressed() {
        if (controller != null && controller.isFullScreen()) {
            controller.toggleFullScreen();
            return false;
        }
        return true;
    }

    public void setVideoUrl(String videoUrl) {
        setVideoUrl(videoUrl,false);
    }

    public void setVideoUrl(String videoUrl,boolean autoStart) {
        this.videoUrl = videoUrl;
        if (controller == null) {
            return;
        }
        controller.setVideoUrl(videoUrl);
        if (autoStart && videoView != null) {
            videoView.start(videoUrl);
        }
    }
}
