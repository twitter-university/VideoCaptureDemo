
package com.marakana.android.videocapturedemo;

import java.io.File;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoPlaybackActivity extends Activity implements OnPreparedListener,
        OnCompletionListener {
    private static final String TAG = "VideoPlaybackActivity";

    private VideoView videoView;

    private ImageButton backButton;

    private ImageButton playButton;

    private ImageButton stopButton;

    private ImageButton deleteButton;

    private Uri uri;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setContentView(R.layout.video_playback);
        this.videoView = (VideoView)super.findViewById(R.id.video);
        this.uri = super.getIntent().getData();
        this.backButton = (ImageButton)super.findViewById(R.id.backButton);
        this.playButton = (ImageButton)super.findViewById(R.id.playButton);
        this.stopButton = (ImageButton)super.findViewById(R.id.stopButton);
        this.deleteButton = (ImageButton)super.findViewById(R.id.deleteButton);
    }

    private void toggleButtons(boolean playing) {
        this.backButton.setEnabled(!playing);
        this.playButton.setVisibility(playing ? View.GONE : View.VISIBLE);
        this.stopButton.setVisibility(playing ? View.VISIBLE : View.GONE);
        this.deleteButton.setEnabled(!playing);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.videoView.setVideoURI(this.uri);
        this.videoView.setOnPreparedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.videoView.stopPlayback();
    }

    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "Prepared. Subscribing for completion callback.");
        this.videoView.setOnCompletionListener(this);
        Log.d(TAG, "Starting plackback");
        this.videoView.start();
        Toast.makeText(this, R.string.playing, Toast.LENGTH_SHORT).show();
        this.toggleButtons(true);
    }

    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "Completed playback. Go to beginning.");
        this.videoView.seekTo(0);
        this.notifyUser(R.string.completed_playback);
        this.toggleButtons(false);
    }

    // gets called by the button press
    public void back(View v) {
        Log.d(TAG, "Going back");
        super.finish();
    }

    // gets called by the button press
    public void play(View v) {
        Log.d(TAG, "Playing");
        this.videoView.start();
        this.toggleButtons(true);
    }

    public void stop(View v) {
        Log.d(TAG, "Stopping");
        this.videoView.pause();
        this.videoView.seekTo(0);
        this.toggleButtons(false);
    }

    // gets called by the button press
    public void delete(View v) {
        if (new File(this.uri.getPath()).delete()) {
            Log.d(TAG, "Deleted: " + this.uri);
            this.notifyUser(R.string.deleted);
        } else {
            Log.d(TAG, "Failed to delete: " + this.uri);
            this.notifyUser(R.string.cannot_delete);
        }
        Log.d(TAG, "Going back");
        super.finish();
    }

    private void notifyUser(int messageResource) {
        Toast.makeText(this, messageResource, Toast.LENGTH_SHORT).show();
    }
}
