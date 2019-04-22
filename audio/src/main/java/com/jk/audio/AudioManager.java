package com.jk.audio;

import android.content.Context;
import android.media.MediaRecorder;

import java.io.File;
import java.util.UUID;

public class AudioManager {

    private static volatile AudioManager instance;
    private boolean isPrepared;
    private OnRecordStateChangeListener stateChangeListener;
    private MediaRecorder mediaRecorder;
    private String outputFilePath;

    private AudioManager() {
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (instance == null) {
                    instance = new AudioManager();
                }
            }
        }
        return instance;
    }

    public void setOnRecordStateChangeListener(OnRecordStateChangeListener l) {
        stateChangeListener = l;
    }

    public void prepareAudio(Context context,String dirName) {
        try {
            isPrepared = false;
            File outputDir = FileUtil.getDiskCacheDir(context,dirName);
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs();
            }
            String fileName = generateFileName();
            File file = new File(outputDir, fileName);
            outputFilePath = file.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            // 设置输出文件
            mediaRecorder.setOutputFile(file.getAbsolutePath());
            // 设置音频源
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置音频格式
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            // 设置音频编码
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.prepare();
            mediaRecorder.start();
            // 准备结束
            isPrepared = true;
            if (stateChangeListener != null) {
                stateChangeListener.onRecordPrepared();
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (stateChangeListener != null) {
                stateChangeListener.onRecordError();
            }
        }
    }

    /**
     * 随机生成文件名称
     *
     * @return
     */
    private String generateFileName() {
        return UUID.randomUUID().toString() + ".amr";
    }

    public int getVoiceLevel(int maxLevel) {
        if (isPrepared) {
            try {
                // 振幅范围mediaRecorder.getMaxAmplitude():1-32767
                return maxLevel * mediaRecorder.getMaxAmplitude() / 32768 + 1;
            } catch (Exception e) {
            }
        }
        return 1;
    }

    public void release() {
        if (mediaRecorder == null) {
            return;
        }
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

        } catch (Exception e) {
            if (stateChangeListener != null) {
                stateChangeListener.onRecordError();
            }
        }
    }

    public void cancel() {
        release();
        if (outputFilePath != null) {
            File file = new File(outputFilePath);
            file.delete();
            outputFilePath = null;
        }
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public interface OnRecordStateChangeListener {

        void onRecordPrepared();

        void onRecordError();
    }
}