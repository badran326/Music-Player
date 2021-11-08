package com.badran.badranaudioplayer;


import static com.badran.badranaudioplayer.PlayerActivity.mIsPlaying;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyVieHolder> {


    private final Activity mContext;
    static ArrayList<MusicFiles> mFiles;
    static MyDatabase db;
    static long songsCount;
    Bitmap bitmap;
    static int colorPosition = 0;


    MusicAdapter(Activity mContext, ArrayList<MusicFiles> mFiles) {
        MusicAdapter.mFiles = mFiles;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.file_name.setText(mFiles.get(position).getTitle());
//        byte[] image = getAlbumArt(mFiles.get(position).getPath());
//        if (image != null) {
//            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
//
//            Glide.with(mContext).asBitmap()
//                    .load(ImageHelper.getRoundedCornerBitmap(bitmap, 1000))
//                    .into(holder.album_art);
//        }
//        else {
//            Drawable drawable = AppCompatResources.getDrawable(mContext, R.drawable.ic_baseline_play_arrow);
//        if (drawable != null) {
//            bitmap = ((BitmapDrawable)drawable).getBitmap();
//        }
        Glide.with(mContext)
                    .load(R.drawable.ic_baseline_play_arrow)
                    .into(holder.album_art);
//        }
        holder.itemView.setOnClickListener(view -> {
            colorPosition = position;
            mIsPlaying = true;
            Intent intent = new Intent(mContext, PlayerActivity.class);
            intent.putExtra("position", position);
            mContext.startActivityForResult(intent, 1);
        });
        holder.menuMore.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(mContext, view);
            popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.delete) {
                    deleteFile(position, view);
                } else if (menuItem.getItemId() == R.id.favorite) {
                    addFavorite(position);
                } else if (menuItem.getItemId() == R.id.delete_favorite) {
                    deleteFavorite();
                }
                return false;
            });
        });
    }
    private void deleteFile (int position, View view) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(mFiles.get(position).getIds()));
        File file = new File(mFiles.get(position).getPath());
        boolean deleted = file.delete();
        if (deleted) {
            mContext.getContentResolver().delete(contentUri, null, null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeRemoved(position, mFiles.size());
            Snackbar.make(view, "File Deleted : ", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(view, "Can't be Deleted : ", Snackbar.LENGTH_LONG).show();
        }
    }

    private void addFavorite(int position) {
        String path = mFiles.get(position).getPath();
        String title = mFiles.get(position).getTitle();
        String artist = mFiles.get(position).getArtist();
        String album = mFiles.get(position).getAlbum();
        String duration = mFiles.get(position).getDuration();
        String ids = mFiles.get(position).getIds();

        db = new MyDatabase(mContext);
        songsCount = db.getSongsCount();

        MusicFiles mf = new MusicFiles(path, title, artist, album, duration, ids);
        boolean res = db.insertFavorSong(mf);
        if (res) {
            Toast.makeText(mContext, "Add to Favorite", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Error Add to Favorite", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFavorite() {
            Toast.makeText(mContext, "Error Delete From Favorite", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public static class MyVieHolder extends RecyclerView.ViewHolder {

        TextView file_name;
        ImageView album_art, menuMore;

        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_nam);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);
        }
    }
    private byte[] getAlbumArt (String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
    @SuppressLint("NotifyDataSetChanged")
    void updateList (ArrayList<MusicFiles> musicFilesArrayList) {
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}

