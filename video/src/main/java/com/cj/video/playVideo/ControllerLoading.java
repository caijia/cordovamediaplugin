package com.cj.video.playVideo;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cj.video.R;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Created by cai.jia on 2017/7/7 0007
 */

public class ControllerLoading extends LinearLayout {

    private TextView netSpeedTv;

    public ControllerLoading(@NonNull Context context) {
        this(context,null);
    }

    public ControllerLoading(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ControllerLoading(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ControllerLoading(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.controller_progress, this, true);
        netSpeedTv = (TextView) findViewById(R.id.net_speed_tv);
        setVisibility(GONE);
    }

    public void show() {
        setVisibility(VISIBLE);
        netSpeedTv.setVisibility(GONE);
    }

    public void setNetSpeed(int speed) {
        setVisibility(VISIBLE);
        netSpeedTv.setVisibility(VISIBLE);
        double speedF;
        if (speed > 1024) {
            speedF = (float)speed / 1024;
        }else{
            speedF = speed;
        }
        netSpeedTv.setText(String.format(Locale.CHINESE, "%sK/s", format(speedF)));
    }

    private String format(double value) {
        String s = String.format(Locale.CHINESE, "%.2f", value);
        DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
        return decimalFormat.format(Double.parseDouble(s));
    }

    public void hide() {
        setVisibility(GONE);
    }
}
