package com.jk.audio;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

public class AudioRecordButton extends AppCompatTextView implements
        AudioManager.OnRecordStateChangeListener, View.OnLongClickListener {

    private static final String TEXT_NORMAL = "按住说话";
    private static final String TEXT_RECORDING = "松开结束";
    private static final String TEXT_WANT_CANCEL = "松开手指 取消录制";

    /**
     * 正常状态
     */
    private static final int STATE_NORMAL = 1;

    /**
     * 录制状态
     */
    private static final int STATE_RECORDING = 2;

    /**
     * 松开取消录制状态
     */
    private static final int STATE_WANT_CANCEL = 3;

    /**
     * 录制错误状态
     */
    private static final int STATE_ERROR = 4;

    /**
     * 录制音量变化消息
     */
    private static final int MSG_VOLUME_CHANGED = 11;

    /**
     * 录制结束
     */
    private static final int MSG_RECORD_FINISH = 12;

    /**
     * 手指偏移距离将到松开取消录制状态
     */
    private static final int CANCEL_OFFSET_Y = 20;

    /**
     * 触发取消录制的距离
     */
    private int cancelOffsetY;

    /**
     * 录制按钮的文字
     */
    private String recordNormalText;

    /**
     * 正在录制时的文字
     */
    private String recordingText;

    /**
     * 取消录制时的文字
     */
    private String recordCancelText;

    /**
     * 录制完后保存的文件目录
     */
    private String outputFileDir;

    /**
     * 录制时间太短的临界值
     */
    private float recordShortTime;

    /**
     * 录制时间单位为秒
     */
    private float recordTime;

    private int currentState = STATE_NORMAL;
    private AudioManager audioManager;
    private AudioRecordHandler handler;
    private AudioRecordFinishListener audioRecordFinishListener;

    private Runnable volumeRunnable = new Runnable() {

        @Override
        public void run() {
            if (currentState == STATE_RECORDING) {
                handler.sendEmptyMessage(MSG_VOLUME_CHANGED);
                recordTime += 0.1;
                handler.postDelayed(this, 100);
            }
        }
    };
    private OnRecordStateListener onRecordStateListener;

    public AudioRecordButton(Context context) {
        this(context, null);
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioRecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        cancelOffsetY = dpToPx(context, CANCEL_OFFSET_Y);
        TypedArray a = null;
        try {
            a = context.obtainStyledAttributes(attrs, R.styleable.AudioRecordButton);
            cancelOffsetY = a.getDimensionPixelOffset(
                    R.styleable.AudioRecordButton_arb_cancel_distance, cancelOffsetY);
            recordNormalText = a.getString(R.styleable.AudioRecordButton_arb_normal_text);
            recordingText = a.getString(R.styleable.AudioRecordButton_arb_recording_text);
            recordCancelText = a.getString(R.styleable.AudioRecordButton_arb_cancel_text);
            outputFileDir = a.getString(R.styleable.AudioRecordButton_arb_output_file_dir);
            recordShortTime = a.getFloat(R.styleable.AudioRecordButton_arb_record_short_time,0.5f);

        }finally {
            if (a != null) {
                a.recycle();
            }
        }

        setGravity(Gravity.CENTER);
        setText(getTextOrDefault(recordNormalText, TEXT_NORMAL));
        handler = new AudioRecordHandler(this);
        audioManager = AudioManager.getInstance();
        audioManager.setOnRecordStateChangeListener(this);
        setOnLongClickListener(this);
        setOnRecordStateListener(new AudioRecordDialog(context));
    }

    public void setAudioRecordFinishListener(AudioRecordFinishListener listener) {
        audioRecordFinishListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return super.onTouchEvent(event);
        }

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                //is recording
                if (currentState == STATE_RECORDING || currentState == STATE_WANT_CANCEL) {
                    boolean wantCancel = wantCancel(x, y);
                    changeState(wantCancel ? STATE_WANT_CANCEL : STATE_RECORDING);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                switch (currentState) {
                    case STATE_RECORDING:{
                        boolean isRecordShort = recordTime < recordShortTime;
                        if (isRecordShort) {
                            audioManager.cancel();
                            if (onRecordStateListener != null) {
                                onRecordStateListener.onRecordShort();
                            }
                            handler.sendEmptyMessageDelayed(MSG_RECORD_FINISH, 1300);

                        }else{
                            audioManager.release();
                            if (onRecordStateListener != null) {
                                onRecordStateListener.onRecordComplete();
                            }

                            if (audioRecordFinishListener != null) {
                                audioRecordFinishListener.onFinish(recordTime,
                                        audioManager.getOutputFilePath());
                            }
                        }
                        break;
                    }

                    default:{
                        audioManager.cancel();
                        if (onRecordStateListener != null) {
                            onRecordStateListener.onRecordComplete();
                        }
                        break;
                    }
                }
                resetState();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void resetState() {
        changeState(STATE_NORMAL);
        recordTime = 0;
    }

    private boolean wantCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }
        return y < -cancelOffsetY || y > getHeight() + cancelOffsetY;
    }

    private void changeState(int state) {
        if (currentState != state) {
            currentState = state;
            switch (state) {
                case STATE_NORMAL:
                    setText(getTextOrDefault(recordNormalText, TEXT_NORMAL));
                    break;

                case STATE_RECORDING:
                    setText(getTextOrDefault(recordingText, TEXT_RECORDING));
                    if (onRecordStateListener != null) {
                        onRecordStateListener.onRecording();
                    }
                    break;

                case STATE_WANT_CANCEL:
                    setText(getTextOrDefault(recordCancelText, TEXT_WANT_CANCEL));
                    if (onRecordStateListener != null) {
                        onRecordStateListener.onRecordWantCancel();
                    }
                    break;
            }
        }
    }

    private String getTextOrDefault(String text, String defaultText) {
        return TextUtils.isEmpty(text) ? defaultText : text;
    }

    @Override
    public void onRecordPrepared() {
        currentState = STATE_RECORDING;
        if (onRecordStateListener != null) {
            onRecordStateListener.onRecordPrepared();
        }
        postDelayed(volumeRunnable, 100);
    }

    @Override
    public void onRecordError() {
        currentState = STATE_ERROR;
        if (onRecordStateListener != null) {
            onRecordStateListener.onRecordError();
        }
    }

    public void setOnRecordStateListener(OnRecordStateListener listener) {
        this.onRecordStateListener = listener;
    }

    @Override
    public boolean onLongClick(View v) {
        String dirName = TextUtils.isEmpty(outputFileDir) ? "audioRecord" : outputFileDir;
        audioManager.prepareAudio(v.getContext(), dirName);
        return false;
    }

    /**
     * 录音完成后的回调
     */
    public interface AudioRecordFinishListener {
        void onFinish(float second, String filePath);
    }

    public interface OnRecordStateListener {

        void onRecording();

        void onRecordWantCancel();

        void onRecordPrepared();

        void onRecordShort();

        void onRecordVolumeChange(int volume);

        void onRecordComplete();

        void onRecordError();
    }

    private int dpToPx(Context context,float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()));
    }

    private static class AudioRecordHandler extends Handler {

        private WeakReference<AudioRecordButton> ref;

        AudioRecordHandler(AudioRecordButton button) {
            ref = new WeakReference<>(button);
        }

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_VOLUME_CHANGED: {
                    AudioRecordButton recordButton = ref.get();
                    if (recordButton != null && recordButton.onRecordStateListener != null) {
                        recordButton.onRecordStateListener
                                .onRecordVolumeChange(recordButton.audioManager.getVoiceLevel(7));
                    }
                    break;
                }

                case MSG_RECORD_FINISH: {
                    AudioRecordButton recordButton = ref.get();
                    if (recordButton != null && recordButton.onRecordStateListener != null) {
                        recordButton.onRecordStateListener.onRecordComplete();
                    }
                    break;
                }
            }
        }
    }
}
