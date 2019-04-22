package com.cj.video.player;

/**
 * Created by cai.jia on 2017/8/23.
 */

public interface PlayerConstants {

    int PLAY_EVENT_PREPARED = 1000;
    int PLAY_EVENT_BEGIN = 1001;
    int PLAY_EVENT_LOADING = 1002;
    int PLAY_EVENT_PROGRESS = 1003;
    int PLAY_EVENT_END = 1004;
    int PLAY_EVENT_ERROR = 1005;
    int PLAY_EVENT_VIDEO_SIZE_CHANGE = 1006;
    int PLAY_EVENT_VIDEO_ROTATION = 1007;
    int PLAY_EVENT_INVOKE_PLAY = 1008;
    int PLAY_EVENT_PAUSE = 1009;
    int PLAY_EVENT_RELOAD_SUCCESS = 1010;
    int PLAY_EVENT_RELOAD_ERROR = 1011;
    int PLAY_EVENT_RELOAD_START = 1012;
    int PLAY_EVENT_DECODE = 1013;
    int PLAY_EVENT_BUFFERING_END = 1014;

    int DISPLAY_WRAP_CONTENT = 4001;
    int DISPLAY_CENTER_CROP = 4002;

    String PARAMS_NET_SPEED = "params:netSpeed";
    String PARAMS_PLAY_DURATION = "params:playDuration";
    String PARAMS_PLAY_PROGRESS = "params:playProgress";
    String PARAMS_PLAY_SECOND_PROGRESS = "params:playSecondProgress";
    String PARAMS_PLAY_ERROR = "params:playError";
    String PARAMS_VIDEO_ROTATION = "params:videoRotation";
    String PARAMS_VIDEO_WIDTH = "params:videoWidth";
    String PARAMS_VIDEO_HEIGHT = "params:videoHeight";
    String PARAMS_MEDIA_DECODE = "params:mediaDecode";
    String PARAMS_CACHE_PERCENT = "params:cachePercent";

}
