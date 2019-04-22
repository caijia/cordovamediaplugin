package com.jk.audio;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.cj.video.player.IjkPlayerWrapper;
import com.cj.video.player.PlayerConstants;
import com.cj.video.player.PlayerWrapper;

import java.util.Locale;

import static com.cj.video.player.PlayerConstants.PLAY_EVENT_END;
import static com.cj.video.player.PlayerConstants.PLAY_EVENT_ERROR;
import static com.cj.video.player.PlayerConstants.PLAY_EVENT_PROGRESS;

public class SimplePlayAudioDialog extends DialogFragment {

    private static final String EXTRA_FILE_PATH = "extra:filePath";
    PlayerWrapper playerWrapper;
    private String audioPath;
    private AlertDialog dialog;

    public static SimplePlayAudioDialog getInstance(String filePath) {
        SimplePlayAudioDialog dialog = new SimplePlayAudioDialog();
        Bundle args = new Bundle();
        args.putString(EXTRA_FILE_PATH, filePath);
        dialog.setArguments(args);
        return dialog;
    }

    private void handleArgs() {
        Bundle args = getArguments();
        if (args != null) {
            audioPath = args.getString(EXTRA_FILE_PATH);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playerWrapper = new IjkPlayerWrapper(getActivity());
        handleArgs();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new AlertDialog.Builder(getActivity())
                .setTitle("正在播放音频")
                .setMessage("")
                .setNegativeButton("取消", (d, v) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> playAudio());
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        releasePlayer();
    }

    private void releasePlayer() {
        if (playerWrapper != null) {
            playerWrapper.destroy();
        }
    }

    private void playAudio() {
        if (playerWrapper == null) {
            return;
        }

        playerWrapper.startPlay(audioPath, null, null);
        playerWrapper.addOnPlayEventListener((event, params) -> {
            switch (event) {
                case PLAY_EVENT_PROGRESS: {
                    int duration = params.getInt(PlayerConstants.PARAMS_PLAY_DURATION);
                    int progress = params.getInt(PlayerConstants.PARAMS_PLAY_PROGRESS);
                    String curTime = formatTime(progress);
                    String totalTime = formatTime(duration);
                    dialog.setMessage(curTime + "/" + totalTime);
                    break;
                }

                case PLAY_EVENT_ERROR:
                case PLAY_EVENT_END: {
                    dialog.dismiss();
                    break;
                }
            }
        });
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
}
