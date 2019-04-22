package com.cj.video.playVideo;

import android.graphics.Matrix;
import android.view.TextureView;

/**
 * Created by cai.jia on 2017/7/2.
 */

public class TextureTransformHelper {

    public static void wrapContent(TextureView textureView, int videoWidth, int videoHeight,int rotation) {
        double aspectRatio;
        if (rotation == 90 || rotation == 270) {
            aspectRatio = (double) videoWidth / videoHeight;

        }else{
            aspectRatio = (double) videoHeight / videoWidth;
        }

        int viewWidth = textureView.getWidth();
        int viewHeight = textureView.getHeight();

        int surfaceWidth, surfaceHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            surfaceWidth = viewWidth;
            surfaceHeight = (int) (viewWidth * aspectRatio);

        } else {
            surfaceWidth = (int) (viewHeight / aspectRatio);
            surfaceHeight = viewHeight;
        }
        int xOffset = (viewWidth - surfaceWidth) / 2;
        int yOffset = (viewHeight - surfaceHeight) / 2;

        Matrix transform = new Matrix();
        transform.postRotate(rotation, viewWidth / 2, viewHeight / 2);
        int oldSurfaceWidth,oldSurfaceHeight;
        if (rotation == 90 || rotation == 270) {
            oldSurfaceWidth = viewHeight;
            oldSurfaceHeight = viewWidth;

        }else{
            oldSurfaceWidth = viewWidth;
            oldSurfaceHeight = viewHeight;
        }
        transform.postTranslate((oldSurfaceWidth - viewWidth) / 2,(oldSurfaceHeight - viewHeight)/2);
        transform.postScale((float) surfaceWidth / oldSurfaceWidth, (float) surfaceHeight / oldSurfaceHeight);
        transform.postTranslate(xOffset, yOffset);
        textureView.setTransform(transform);
    }

    public static void centerCrop(TextureView textureView, int videoWidth, int videoHeight,int rotation) {
        double aspectRatio;
        if (rotation == 90 || rotation == 270) {
            aspectRatio = (double) videoWidth / videoHeight;

        }else{
            aspectRatio = (double) videoHeight / videoWidth;
        }

        int viewWidth = textureView.getWidth();
        int viewHeight = textureView.getHeight();

        int surfaceWidth, surfaceHeight;
        if (viewHeight < (int) (viewWidth * aspectRatio)) {
            surfaceWidth = viewWidth;
            surfaceHeight = (int) (viewWidth * aspectRatio);

        } else {
            surfaceWidth = (int) (viewHeight / aspectRatio);
            surfaceHeight = viewHeight;
        }
        int xOffset = (viewWidth - surfaceWidth) / 2;
        int yOffset = (viewHeight - surfaceHeight) / 2;

        Matrix transform = new Matrix();
        transform.postRotate(rotation, viewWidth / 2, viewHeight / 2);
        int oldSurfaceWidth,oldSurfaceHeight;
        if (rotation == 90 || rotation == 270) {
            oldSurfaceWidth = viewHeight;
            oldSurfaceHeight = viewWidth;

        }else{
            oldSurfaceWidth = viewWidth;
            oldSurfaceHeight = viewHeight;
        }
        transform.postTranslate((oldSurfaceWidth - viewWidth) / 2,(oldSurfaceHeight - viewHeight)/2);
        transform.postScale((float) surfaceWidth / oldSurfaceWidth, (float) surfaceHeight / oldSurfaceHeight);
        transform.postTranslate(xOffset, yOffset);
        textureView.setTransform(transform);
    }
}
