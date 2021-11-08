package com.badran.badranaudioplayer;

import static com.badran.badranaudioplayer.MusicAdapter.db;
import static com.badran.badranaudioplayer.MusicAdapter.songsCount;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
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

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.MyHolder> {

    private final Context mContext;
    static ArrayList<MusicFiles> albumFiles;
    View view;
    Bitmap bitmap;

    public AlbumDetailsAdapter(Context mContext, ArrayList<MusicFiles> albumFiles) {
        this.mContext = mContext;
        AlbumDetailsAdapter.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.album_name.setText(albumFiles.get(position).getTitle());
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());
        if (image != null) {
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            holder.album_image.setBackgroundResource(0);
            Glide.with(mContext).asBitmap()
                    .load(ImageHelper.getRoundedCornerBitmap(bitmap, 1000))
                    .into(holder.album_image);
        }
        else {
//        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.radius);
            holder.album_image.setBackgroundResource(R.drawable.radius);
        Glide.with(mContext)
                .load(R.drawable.ic_baseline_play_arrow)
                .into(holder.album_image);
        }
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, PlayerActivity.class);
            intent.putExtra("sender", "albumDetails");
            intent.putExtra("position", position);
            mContext.startActivity(intent);
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
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(albumFiles.get(position).getIds()));
        File file = new File(albumFiles.get(position).getPath());
        boolean deleted = file.delete();
        if (deleted) {
            mContext.getContentResolver().delete(contentUri, null, null);
            albumFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeRemoved(position, albumFiles.size());
            Snackbar.make(view, "File Deleted : ", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(view, "Can't be Deleted : ", Snackbar.LENGTH_LONG).show();
        }
    }

    private void addFavorite(int position) {
        String path = albumFiles.get(position).getPath();
        String title = albumFiles.get(position).getTitle();
        String artist = albumFiles.get(position).getArtist();
        String album = albumFiles.get(position).getAlbum();
        String duration = albumFiles.get(position).getDuration();
        String ids = albumFiles.get(position).getIds();

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
        return albumFiles.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {

        ImageView album_image, menuMore;
        TextView album_name;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.music_img);
            album_name = itemView.findViewById(R.id.music_file_nam);
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
}
