
package com.marakana.android.videocapturedemo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

public class VideoCaptureActivity extends Activity {
    private static final String TAG = "VideoCaptureActivity";

    Camera camera;

    ImageButton recordButton;

    ImageButton stopButton;

    FrameLayout cameraPreviewFrame;

    CameraPreview cameraPreview;

    MediaRecorder mediaRecorder;

    File file;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setContentView(R.layout.video_capture);
        this.cameraPreviewFrame = (FrameLayout)super.findViewById(R.id.camera_preview);
        this.recordButton = (ImageButton)super.findViewById(R.id.recordButton);
        this.stopButton = (ImageButton)super.findViewById(R.id.stopButton);
        this.toggleButtons(false);
        // we'll enable this button once the camera is ready
        this.recordButton.setEnabled(false);
    }

    void toggleButtons(boolean recording) {
        this.recordButton.setEnabled(!recording);
        this.recordButton.setVisibility(recording ? View.GONE : View.VISIBLE);
        this.stopButton.setEnabled(recording);
        this.stopButton.setVisibility(recording ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // initialize the camera in background, as this may take a while
        new AsyncTask<Void, Void, Camera>() {

            @Override
            protected Camera doInBackground(Void... params) {
                try {
                    Camera camera = Camera.open();
                    return camera == null ? Camera.open(0) : camera;
                } catch (RuntimeException e) {
                    Log.wtf(TAG, "Failed to get camera", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Camera camera) {
                if (camera == null) {
                    Toast.makeText(VideoCaptureActivity.this, R.string.cannot_record,
                            Toast.LENGTH_SHORT);
                } else {
                    VideoCaptureActivity.this.initCamera(camera);
                }
            }
        }.execute();
    }

    void initCamera(Camera camera) {
        // we now have the camera
        this.camera = camera;
        // create a preview for our camera
        this.cameraPreview = new CameraPreview(VideoCaptureActivity.this, this.camera);
        // add the preview to our preview frame
        this.cameraPreviewFrame.addView(this.cameraPreview, 0);
        // enable just the record button
        this.recordButton.setEnabled(true);
    }

    void releaseCamera() {
        if (this.camera != null) {
            this.camera.lock(); // unnecessary in API >= 14
            this.camera.stopPreview();
            this.camera.release();
            this.camera = null;
            this.cameraPreviewFrame.removeView(this.cameraPreview);
        }
    }

    void releaseMediaRecorder() {
        if (this.mediaRecorder != null) {
            this.mediaRecorder.reset(); // clear configuration (optional here)
            this.mediaRecorder.release();
            this.mediaRecorder = null;
        }
    }

    void releaseResources() {
        this.releaseMediaRecorder();
        this.releaseCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.releaseResources();
    }

    // gets called by the button press
    public void startRecording(View v) {
        Log.d(TAG, "startRecording()");
        // we need to unlock the camera so that mediaRecorder can use it
        this.camera.unlock(); // unnecessary in API >= 14
        // now we can initialize the media recorder and set it up with our
        // camera
        this.mediaRecorder = new MediaRecorder();
        this.mediaRecorder.setCamera(this.camera);
        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        this.mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        this.mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        this.mediaRecorder.setOutputFile(this.initFile().getAbsolutePath());
        this.mediaRecorder.setPreviewDisplay(this.cameraPreview.getHolder().getSurface());
        try {
            this.mediaRecorder.prepare();
            // start the actual recording
            // throws IllegalStateException if not prepared
            this.mediaRecorder.start();
            Toast.makeText(this, R.string.recording, Toast.LENGTH_SHORT).show();
            // enable the stop button by indicating that we are recording
            this.toggleButtons(true);
        } catch (Exception e) {
            Log.wtf(TAG, "Failed to prepare MediaRecorder", e);
            Toast.makeText(this, R.string.cannot_record, Toast.LENGTH_SHORT).show();
            this.releaseMediaRecorder();
        }
    }

    // gets called by the button press
    public void stopRecording(View v) {
        Log.d(TAG, "stopRecording()");
        assert this.mediaRecorder != null;
        try {
            this.mediaRecorder.stop();
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
            // we are no longer recording
            this.toggleButtons(false);
        } catch (RuntimeException e) {
            // the recording did not succeed
            Log.w(TAG, "Failed to record", e);
            if (this.file != null && this.file.exists() && this.file.delete()) {
                Log.d(TAG, "Deleted " + this.file.getAbsolutePath());
            }
            return;
        } finally {
            this.releaseMediaRecorder();
        }
        if (this.file == null || !this.file.exists()) {
            Log.w(TAG, "File does not exist after stop: " + this.file.getAbsolutePath());
        } else {
            Log.d(TAG, "Going to display the video: " + this.file.getAbsolutePath());
            Intent intent = new Intent(this, VideoPlaybackActivity.class);
            intent.setData(Uri.fromFile(file));
            super.startActivity(intent);
        }
    }

    private File initFile() {
        File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), this
                        .getClass().getPackage().getName());
        if (!dir.exists() && !dir.mkdirs()) {
            Log.wtf(TAG, "Failed to create storage directory: " + dir.getAbsolutePath());
            Toast.makeText(VideoCaptureActivity.this, R.string.cannot_record, Toast.LENGTH_SHORT);
            this.file = null;
        } else {
            this.file = new File(dir.getAbsolutePath(), new SimpleDateFormat(
                    "'IMG_'yyyyMMddHHmmss'.m4v'").format(new Date()));
        }
        return this.file;
    }
}
