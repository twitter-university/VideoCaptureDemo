
package com.marakana.android.videocapturedemo;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private final Camera camera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        super.getHolder().addCallback(this);
        // required for API <= 11
        super.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated()");
        // now that we have the surface, we can start the preview
        try {
            this.camera.setPreviewDisplay(holder);
            this.camera.startPreview();
        } catch (IOException e) {
            Log.wtf(TAG, "Failed to start camera preview", e);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // we will release the camera preview in our activity before this
        // happens
        Log.d(TAG, "surfaceDestroyed()");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // our activity runs with screenOrientation="landscape" so we don't
        // care about surface changes
        Log.d(TAG, "surfaceChanged()");
    }
}
