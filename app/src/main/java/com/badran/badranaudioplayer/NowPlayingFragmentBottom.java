package com.badran.badranaudioplayer;

import static android.content.Context.MODE_PRIVATE;
import static com.badran.badranaudioplayer.MainActivity.ARTIST_NAME_TO_FRAG;
import static com.badran.badranaudioplayer.MainActivity.PATH_TO_FRAG;
import static com.badran.badranaudioplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.badran.badranaudioplayer.MainActivity.SONG_NAME_TO_FRAG;
import static com.badran.badranaudioplayer.PlayerActivity.listSize;
import static com.badran.badranaudioplayer.PlayerActivity.mIsPlaying;
import static com.badran.badranaudioplayer.PlayerActivity.playPauseBtnInt;
import static com.badran.badranaudioplayer.PlayerActivity.testPosition;
import static com.badran.badranaudioplayer.PlayerActivity.testSender;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    ImageView nextBtn, albumArt;
    TextView artist, songName;
    FloatingActionButton playPauseBtn;
    RelativeLayout card_bottom;
    View view;
    MusicService musicService;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";

    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);
        card_bottom = view.findViewById(R.id.card_bottom);
        card_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listSize != 0) {
                    Intent intent = new Intent(getContext(), PlayerActivity.class);
                    intent.putExtra("sender", testSender);
                    intent.putExtra("position", testPosition);
                    startActivityForResult(intent, 1);
                }
            }
        });
        artist = view.findViewById(R.id.song_artist_miniPlayer);
        songName = view.findViewById(R.id.song_name_miniPlayer);
        albumArt = view.findViewById(R.id.bottom_album_art);
        nextBtn = view.findViewById(R.id.skip_next_bottom);
        playPauseBtn = view.findViewById(R.id.play_pause_miniPlayer);
        nextBtn.setOnClickListener(view -> {
            if (musicService != null) {
                musicService.nextBtnClicked();
                if (getActivity() != null) {
                    SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
                    String path = preferences.getString(MUSIC_FILE, null);
                    String artistName = preferences.getString(ARTIST_NAME, null);
                    String song = preferences.getString(SONG_NAME, null);
                    if (path != null) {
                        SHOW_MINI_PLAYER = true;
                        PATH_TO_FRAG = path;
                        ARTIST_NAME_TO_FRAG = artistName;
                        SONG_NAME_TO_FRAG = song;
                    } else {
                        SHOW_MINI_PLAYER = false;
                        PATH_TO_FRAG = null;
                        ARTIST_NAME_TO_FRAG = null;
                        SONG_NAME_TO_FRAG = null;
                    }
                    if (SHOW_MINI_PLAYER) {
                        byte[] image = getAlbumArt(PATH_TO_FRAG);
                        if (image != null) {
                            albumArt.setBackgroundResource(0);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 500, 500, true);
                            Glide.with(requireContext()).asBitmap()
                                    .load(ImageHelper.getRoundedCornerBitmap(resized, 500))
                                    .into(albumArt);
                        }
                        else {
                            albumArt.setBackgroundResource(R.drawable.radius);
                            Glide.with(requireContext())
                                    .load(R.drawable.ic_baseline_play_arrow)
                                    .into(albumArt);
                        }
                        songName.setText(SONG_NAME_TO_FRAG);
                        artist.setText(ARTIST_NAME_TO_FRAG);
                    }
                }
            }
        });
        playPauseBtn.setOnClickListener(view -> {

            if (musicService != null) {
                musicService.playPauseBtnClicked();
                if (mIsPlaying) {
                    playPauseBtnInt = R.drawable.ic_baseline_pause;
                } else {
                    playPauseBtnInt = R.drawable.ic_baseline_play_arrow;
                }
                playPauseBtn.setImageResource(playPauseBtnInt);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SHOW_MINI_PLAYER) {
            if (PATH_TO_FRAG != null) {
                byte[] image = getAlbumArt(PATH_TO_FRAG);
                if (image != null) {
                    albumArt.setBackgroundResource(0);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    Bitmap resized = Bitmap.createScaledBitmap(bitmap, 500, 500, true);
                    Glide.with(requireContext()).asBitmap()
                            .load(ImageHelper.getRoundedCornerBitmap(resized, 500))
                            .into(albumArt);
                }
                else {
                    albumArt.setBackgroundResource(R.drawable.radius);
                    Glide.with(requireContext())
                            .load(R.drawable.ic_baseline_play_arrow)
                            .into(albumArt);
                }
                songName.setText(SONG_NAME_TO_FRAG);
                artist.setText(ARTIST_NAME_TO_FRAG);
                Intent intent = new Intent(getContext(), MusicService.class);
                if (getContext() != null) {
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
            }
        }
        if (mIsPlaying) {
            playPauseBtnInt = R.drawable.ic_baseline_pause;
        } else {
            playPauseBtnInt = R.drawable.ic_baseline_play_arrow;
        }
        playPauseBtn.setImageResource(playPauseBtnInt);
    }

    @Override
    public void onPause() {
        super.onPause();
//            requireContext().unbindService(this);
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }
}