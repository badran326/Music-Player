package com.badran.badranaudioplayer;

import static com.badran.badranaudioplayer.MainActivity.musicFiles;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumPhoto, backBtn;
    String albumName;
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    AlbumDetailsAdapter albumDetailsAdapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(view -> {
            finish();
        });
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("albumName"));
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        albumPhoto = findViewById(R.id.albumPhoto);
        albumName = getIntent().getStringExtra("albumName");
        int j = 0;
        for (int i = 0; i < musicFiles.size(); i ++) {
            if (albumName.equals(musicFiles.get(i).getAlbum())) {
                albumSongs.add(j, musicFiles.get(i));
                j ++;
            }
        }
        byte[] image = getAlbumArt(albumSongs.get(0).getPath());
        if (image != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            albumPhoto.setBackgroundResource(0);
            Glide.with(this)
                    .load(bitmap)
                    .into(albumPhoto);
        }
        else {
            albumPhoto.setBackgroundResource(R.drawable.bachgound);
            Glide.with(this)
                    .load(R.drawable.ic_baseline_play_arrow)
                    .into(albumPhoto);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!(albumSongs.size() < 1)) {
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            recyclerView.setSaveEnabled(true);
        }
    }

    private byte[] getAlbumArt (String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}