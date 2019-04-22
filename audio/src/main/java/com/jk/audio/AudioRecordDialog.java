package com.jk.audio;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AudioRecordDialog implements AudioRecordButton.OnRecordStateListener {

    private ImageView ivRecord;
    private ImageView ivVolume;
    private TextView tvRecordHint;

    private Context context;
    private Dialog dialog;

    public AudioRecordDialog(Context context) {
        this.context = context;
    }

    @Override
    public void onRecording() {
        if (dialog != null && dialog.isShowing()) {
            ivRecord.setVisibility(View.VISIBLE);
            ivVolume.setVisibility(View.VISIBLE);
            tvRecordHint.setVisibility(View.VISIBLE);

            ivRecord.setImageResource(R.drawable.icon_dialog_recording);
            tvRecordHint.setText("手指上滑，取消发送");
        }
    }

    @Override
    public void onRecordWantCancel() {
        if (dialog != null && dialog.isShowing()) {
            ivRecord.setVisibility(View.VISIBLE);
            ivRecord.setImageResource(R.drawable.icon_dialog_cancel);
            ivVolume.setVisibility(View.GONE);
            tvRecordHint.setVisibility(View.VISIBLE);
            tvRecordHint.setText("松开手指，取消发送");
        }
    }

    @Override
    public void onRecordPrepared() {
        if (dialog == null) {
            dialog = new Dialog(context, R.style.Theme_RecorderDialog);
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.dialog_default_audio_record,
                    new LinearLayout(context), false);
            ivRecord = (ImageView) view.findViewById(R.id.iv_record);
            ivVolume = (ImageView) view.findViewById(R.id.iv_volume);
            tvRecordHint = (TextView) view.findViewById(R.id.tv_record_hint);
            dialog.setContentView(view);
        }
        dialog.show();
    }

    @Override
    public void onRecordShort() {
        if (dialog != null && dialog.isShowing()) {
            ivRecord.setVisibility(View.VISIBLE);
            ivRecord.setImageResource(R.drawable.icon_dialog_length_short);
            ivVolume.setVisibility(View.GONE);
            tvRecordHint.setVisibility(View.VISIBLE);
            tvRecordHint.setText("录音时间过短");
        }
    }

    @Override
    public void onRecordVolumeChange(int level) {
        if (dialog != null && dialog.isShowing()) {
            ivRecord.setVisibility(View.VISIBLE);
            ivVolume.setVisibility(View.VISIBLE);
            tvRecordHint.setVisibility(View.VISIBLE);
            ivRecord.setImageLevel(level);
        }
    }

    @Override
    public void onRecordComplete() {
        dismissDialog();
    }

    @Override
    public void onRecordError() {
        Toast.makeText(context, "录制错误", Toast.LENGTH_SHORT).show();
        dismissDialog();
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
