package com.badran.badranaudioplayer;

import static com.badran.badranaudioplayer.ApplicationClass.ACTION_NEXT;
import static com.badran.badranaudioplayer.ApplicationClass.ACTION_PLAY;
import static com.badran.badranaudioplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.badran.badranaudioplayer.ApplicationClass.CHANNEL_ID_2;
import static com.badran.badranaudioplayer.PlayerActivity.listSongs;
import static com.badran.badranaudioplayer.PlayerActivity.mIsPlaying;
import static com.badran.badranaudioplayer.PlayerActivity.playPauseBtnInt;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder mBinder = new MyBinder();
    static MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";
    private final Handler handler = new Handler();

//    public final Runnable delayedStopRunnable = () -> playerActivity.getMediaController().getTransportControls().stop();
    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = focusChange -> {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Permanent loss of audio focus
            // Pause playback immediately
            mIsPlaying = false;
            mediaPlayer.pause();
            showNotification();
            // Wait 30 seconds before stopping playback
//            handler.postDelayed(delayedStopRunnable,
//                    TimeUnit.SECONDS.toSeconds(30));
        }
        else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            mediaPlayer.pause();
            showNotification();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            mediaPlayer.setVolume(0.2f,0.2f);
            showNotification();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            mediaPlayer.setVolume(1, 1);
            if (mIsPlaying){
                mediaPlayer.start();
                showNotification();
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManagerCompat nmc = NotificationManagerCompat.from(getBaseContext());
        nmc.cancel(10);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        if (myPosition != -1) {
            playMedia(myPosition);
        }
        if (actionName != null) {
            switch (actionName) {
                case "playPause":
                    playPauseBtnClicked();
                    break;
                case "next":
                    nextBtnClicked();
                    break;
                case "previous":
                    prevBtnClicked();
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles = listSongs;
        position = startPosition;
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result;
        result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (mIsPlaying) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    if (musicFiles != null) {
                        createMediaPlayer(position);
                        mediaPlayer.start();
                    }
                } else {
                    createMediaPlayer(position);
                    mediaPlayer.start();
                }
            }
        }
    }

    public void start() {
        mIsPlaying = true;
        mediaPlayer.start();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void stop() {
        mIsPlaying = false;
        mediaPlayer.stop();
    }

    public void release() {
        mediaPlayer.release();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void createMediaPlayer(int positionInner) {
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        editor.apply();
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    public void pause() {
        mIsPlaying = false;
        mediaPlayer.pause();
    }

    public void OnCompleted() {
        mediaPlayer.setOnCompletionListener(this);
        mIsPlaying = true;
        showNotification();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mIsPlaying) {
            if (actionPlaying != null) {
                actionPlaying.nextBtnClicked();
            }
            if (mediaPlayer != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
                OnCompleted();
            }
        }
    }

    public void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    public void showNotification() {
        mIsPlaying = mediaPlayer.isPlaying();
        if (mIsPlaying) {
            playPauseBtnInt = R.drawable.ic_baseline_pause;
        } else {
            playPauseBtnInt = R.drawable.ic_baseline_play_arrow;
        }
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent prevPending = PendingIntent
                .getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pausePending = PendingIntent
                .getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent nextPending = PendingIntent
                .getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.b);
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtnInt)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous, "Previous", prevPending)
                .addAction(playPauseBtnInt, "Pause", pausePending)
                .addAction(R.drawable.ic_baseline_skip_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setContentIntent(contentIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(mIsPlaying);

        NotificationManagerCompat nmc = NotificationManagerCompat.from(this);
        nmc.notify(10, notification.build());
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    void nextBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
        }
    }

    void prevBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.prevBtnClicked();
        }
    }

    void playPauseBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.playPauseBtnClicked();
        }
    }

    void setVolume(float left, float right) {
        mediaPlayer.setVolume(left, right);
    }
}
