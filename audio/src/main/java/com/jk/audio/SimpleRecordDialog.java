package com.jk.audio;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class SimpleRecordDialog extends DialogFragment {

    private static final int MSG_RECORD_VOICE = 200;
    private AudioManager audioManager;
    private RecordTimeHandler recordTimeHandler;

    private boolean stopRecordTime;
    private long recordTime;
    private Runnable recordTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (!stopRecordTime) {
                recordTime += 1;
                recordTimeHandler.sendEmptyMessage(MSG_RECORD_VOICE);
                recordTimeHandler.postDelayed(this, 1000);
            }
        }
    };
    private boolean isClickOk;
    private OnRecordVoiceCompleteListener onRecordVoiceCompleteListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioManager = AudioManager.getInstance();
        recordTimeHandler = new RecordTimeHandler(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("正在录音")
                .setMessage("00:00")
                .setPositiveButton("完成", (d, v) -> {
                    isClickOk = true;
                    stopRecordTime();
                    audioManager.release();
                    if (onRecordVoiceCompleteListener != null) {
                        onRecordVoiceCompleteListener.onRecordVoiceComplete(
                                audioManager.getOutputFilePath(), recordTime);
                    }
                    d.dismiss();
                })
                .setNegativeButton("取消", (d, v) -> {
                    d.dismiss();
                })
                .create();
        alertDialog.setOnShowListener(dialog -> {
            audioManager.prepareAudio(getActivity(), "bridge");
            startRecordTime();
        });
        return alertDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        stopRecordTime();
        audioManager.release();
        if (!isClickOk) {
            audioManager.cancel();
        }
    }

    private void startRecordTime() {
        recordTime = 0;
        recordTimeHandler.sendEmptyMessage(MSG_RECORD_VOICE);
        recordTimeHandler.postDelayed(recordTimeRunnable, 1000);
    }

    private void stopRecordTime() {
        stopRecordTime = true;
        recordTimeHandler.removeMessages(MSG_RECORD_VOICE);
        recordTimeHandler.removeCallbacks(recordTimeRunnable);
    }

    private void updateUi() {
        if (getDialog() != null) {
            AlertDialog dialog = (AlertDialog) getDialog();
            dialog.setMessage(formatTime(recordTime));
        }
    }

    private String formatTime(long second) {
        long h = second / 3600;
        long m = (second % 3600) / 60;
        long s = second % 60;
        if (h <= 0) {
            return String.format(Locale.CHINESE, "%02d:%02d", m, s);
        } else {
            return String.format(Locale.CHINESE, "%02d:%02d:%02d", h, m, s);
        }
    }

    public void setOnRecordVoiceCompleteListener(OnRecordVoiceCompleteListener listener) {
        this.onRecordVoiceCompleteListener = listener;
    }

    public interface OnRecordVoiceCompleteListener {
        void onRecordVoiceComplete(String filePath, long second);
    }

    private static class RecordTimeHandler extends Handler {

        WeakReference<SimpleRecordDialog> ref;

        RecordTimeHandler(SimpleRecordDialog dialog) {
            ref = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECORD_VOICE:
                    if (ref != null && ref.get() != null) {
                        ref.get().updateUi();
                    }
                    break;
            }
        }
    }
}
