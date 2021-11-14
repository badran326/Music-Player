package com.badran.badranaudioplayer;

import static com.badran.badranaudioplayer.AlbumDetailsAdapter.albumFiles;
import static com.badran.badranaudioplayer.FavoriteFragment.favorite;
import static com.badran.badranaudioplayer.MainActivity.close;
import static com.badran.badranaudioplayer.MainActivity.repeatBoolean;
import static com.badran.badranaudioplayer.MainActivity.shuffleBoolean;
import static com.badran.badranaudioplayer.MusicAdapter.mFiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, prevBtn, shuffleBtn, repeatBtn, backBtn;
    static FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    static int position;
    static int playPauseBtnInt;
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    private final Handler handler = new Handler();
    static MusicService musicService;
    static boolean mIsPlaying;
    static int testPosition = -1;
    static String testSender = null;
    static int listSize;
    String sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFulScreen();
        setContentView(R.layout.activity_player);
        initViews();
        if (shuffleBoolean) {
            shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_on);
        } else {
            shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_of);
        }
        if (repeatBoolean) {
            repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_on);
        } else {
            repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_of);
        }
        getIntentMethod();
        if (mIsPlaying) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (musicService != null && fromUser) {
                        musicService.seekTo(progress * 1000);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        duration_played.setText(formattedTime(mCurrentPosition));
                    }
                    handler.post(this);
                }
            });
            shuffleBtn.setOnClickListener(view -> {
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_of);
                } else {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_on);
                }
            });
            repeatBtn.setOnClickListener(view -> {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_of);
                } else {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_on);
                }
            });
        }
    }

    private void setFulScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void prevThreadBtn() {
        Thread prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(view -> prevBtnClicked());
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {
        if (shuffleBoolean && !repeatBoolean) {
            position = getRandom(listSongs.size() - 1);
        } else if (!shuffleBoolean && !repeatBoolean) {
            position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
        }
        uri = Uri.parse((listSongs.get(position).getPath()));
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            musicService.createMediaPlayer(position);
            playPauseBtnInt = R.drawable.ic_baseline_pause;
            musicService.start();
        } else {
            musicService.stop();
            musicService.release();
            musicService.createMediaPlayer(position);
            playPauseBtnInt = R.drawable.ic_baseline_play_arrow;
        }
        setVariables(position, uri, playPauseBtnInt);
        testPosition = position;
    }

    private void nextThreadBtn() {
        Thread nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(view -> nextBtnClicked());
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        if (shuffleBoolean && !repeatBoolean) {
            position = getRandom(listSongs.size() - 1);
        } else if (!shuffleBoolean && !repeatBoolean) {
            position = ((position + 1) % listSongs.size());
        }
        uri = Uri.parse((listSongs.get(position).getPath()));
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            musicService.createMediaPlayer(position);
            playPauseBtnInt = R.drawable.ic_baseline_pause;
            musicService.start();
        } else {
            musicService.stop();
            musicService.release();
            musicService.createMediaPlayer(position);
            playPauseBtnInt = R.drawable.ic_baseline_play_arrow;
        }
        setVariables(position, uri, playPauseBtnInt);
        testPosition = position;
    }

    void setVariables (int position, Uri uri, int playPauseBtnInt) {
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        seekBar.setMax(musicService.getDuration() / 1000);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.post(this);
            }
        });
        musicService.OnCompleted();
        musicService.showNotification();
        playPauseBtn.setBackgroundResource(playPauseBtnInt);
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    private void playThreadBtn() {
        Thread playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(view -> playPauseBtnClicked());
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.pause();
        } else {
            musicService.start();
        }
        seekBar.setMax(musicService.getDuration() / 1000);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.post(this);
            }
        });
        if (mIsPlaying) {
            playPauseBtnInt = R.drawable.ic_baseline_pause;
        } else {
            playPauseBtnInt = R.drawable.ic_baseline_play_arrow;
        }
        musicService.showNotification();
        playPauseBtn.setImageResource(playPauseBtnInt);
    }

    private String formattedTime(int mCurrentPosition) {
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        String totalOut = minutes + ":" + seconds;
        String totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        sender = getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("albumDetails") && !close) {
            listSongs = albumFiles;
        } else if (sender != null && sender.equals("favoriteName") && !close) {
            listSongs = favorite;
        } else if (sender == null && !close){
            listSongs = mFiles;
        }
        testSender = sender;
        if (listSongs != null) {
            if (position != testPosition || listSize != listSongs.size() && !close) {
                listSize = listSongs.size();
                playPauseBtnInt = R.drawable.ic_baseline_pause;
                playPauseBtn.setImageResource(playPauseBtnInt);
                uri = Uri.parse(listSongs.get(position).getPath());

                Intent intent = new Intent(this, MusicService.class);
                intent.putExtra("servicePosition", position);
                startService(intent);
            }
        }
    }

    private void initViews() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlay);
        duration_total = findViewById(R.id.durationTotal);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(view -> {
            finish();
        });
    }

    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int duration = Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        duration_total.setText(formattedTime(duration));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            cover_art.setBackgroundResource(0);
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(palette -> {
                assert palette != null;
                Palette.Swatch swatch = palette.getDominantSwatch();
                ImageView gradient = findViewById(R.id.imageViewGredient);
                LinearLayout mContainer = findViewById(R.id.mContainer);
                gradient.setBackgroundResource(R.drawable.gredient_bg);
                mContainer.setBackgroundResource(R.drawable.main_bg);
                GradientDrawable gradientDrawable;
                GradientDrawable gradientDrawableBg;
                if (swatch != null) {
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{swatch.getRgb(), 0x00000000});
                    gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{swatch.getRgb(), swatch.getRgb()});
                    song_name.setTextColor(swatch.getTitleTextColor());
                    artist_name.setTextColor(swatch.getBodyTextColor());
                } else {
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff00000, 0x00000000});
                    gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff00000, 0xff00000});
                    song_name.setTextColor(Color.WHITE);
                    artist_name.setTextColor(Color.DKGRAY);
                }
                gradient.setBackground(gradientDrawable);
                mContainer.setBackground(gradientDrawableBg);
            });
        } else {
//            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.radius);
//                ImageAnimation(this, cover_art, bitmap);
            cover_art.setBackgroundResource(R.drawable.bachgound);
            Glide.with(this.getBaseContext())
                    .load(R.drawable.ic_baseline_play_arrow)
                    .into(cover_art);
            ImageView gredient = findViewById(R.id.imageViewGredient);
            LinearLayout mContainer = findViewById(R.id.mContainer);
            gredient.setBackgroundResource(R.drawable.gredient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);
            song_name.setTextColor(Color.WHITE);
            artist_name.setTextColor(Color.DKGRAY);
        }
    }

    public void ImageAnimation(final Context context, ImageView imageView, Bitmap bitmap) {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (position != testPosition && position != -1) {
            MusicService.MyBinder myBinder = (MusicService.MyBinder) iBinder;
            musicService = myBinder.getService();
        }
        testPosition = position;
        musicService.setCallBack(this);
        if (mIsPlaying) {
            playPauseBtnInt = R.drawable.ic_baseline_pause;
            playPauseBtn.setImageResource(playPauseBtnInt);
            musicService.start();
        }
        setVariables(position, uri, playPauseBtnInt);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;

    }
}