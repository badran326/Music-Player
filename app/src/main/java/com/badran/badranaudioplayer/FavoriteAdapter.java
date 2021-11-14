package com.badran.badranaudioplayer;

import static com.badran.badranaudioplayer.FavoriteFragment.countNum;
import static com.badran.badranaudioplayer.MainActivity.close;
import static com.badran.badranaudioplayer.MusicAdapter.songsCount;
import static com.badran.badranaudioplayer.PlayerActivity.mIsPlaying;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import java.util.ArrayList;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.MyHolder> {

    private final Context mContext;
    private final ArrayList<MusicFiles> favoriteFiles;
    MyDatabase db;

    public FavoriteAdapter(Context mContext, ArrayList<MusicFiles> favoriteFiles) {
        this.mContext = mContext;
        this.favoriteFiles = favoriteFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.favorite_name.setText(favoriteFiles.get(position).getTitle());
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] image = getAlbumArt(favoriteFiles.get(position).getPath());
                if (image != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    Bitmap resized = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
                    holder.favorite_image.post(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(mContext)
                                    .load(ImageHelper.getRoundedCornerBitmap(resized, 300))
                                    .into(holder.favorite_image);
                        }
                    });
                } else {
                    holder.favorite_image.post(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(mContext)
                                    .load(R.drawable.ic_baseline_play_arrow)
                                    .into(holder.favorite_image);
                        }
                    });
                }
            }
        });
        t1.start();
        holder.itemView.setOnClickListener(view -> {
            mIsPlaying = true;
            close = false;
            Intent intent = new Intent(mContext, PlayerActivity.class);
            intent.putExtra("sender", "favoriteName");
            intent.putExtra("position", position);
            mContext.startActivity(intent);
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.delete) {
                            deleteFile(view);
                        } else if (menuItem.getItemId() == R.id.favorite) {
                            addFavorite(position);
                        } else if (menuItem.getItemId() == R.id.delete_favorite) {
                            deleteFavorite(position);
                        }
                        return false;
                    }
                });
            }
        });
    }

    private void deleteFile(View view) {
        Snackbar.make(view, "Can't be Deleted : ", Snackbar.LENGTH_LONG).show();
    }

    private void addFavorite(int position) {
        String path = favoriteFiles.get(position).getPath();
        String title = favoriteFiles.get(position).getTitle();
        String artist = favoriteFiles.get(position).getArtist();
        String album = favoriteFiles.get(position).getAlbum();
        String duration = favoriteFiles.get(position).getDuration();
        String ids = favoriteFiles.get(position).getIds();

        db = new MyDatabase(mContext);
        songsCount = db.getSongsCount();

        MusicFiles mf = new MusicFiles(path, title, artist, album, duration, ids);
        boolean res = db.insertFavorSong(mf);
        if (res) {
            Toast.makeText(mContext, "Add to Favorite", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Can't be add to Favorite", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFavorite(int position) {

        db = new MyDatabase(mContext);

        boolean res = db.deleteFavor(favoriteFiles.get(position));
        favoriteFiles.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeRemoved(position, favoriteFiles.size());
        songsCount = db.getSongsCount();
        countNum.setText(String.valueOf(songsCount));
        if (res) {
            Toast.makeText(mContext, "Delete From Favorite", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Error Delete From Favorite", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return favoriteFiles.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {

        ImageView favorite_image, menuMore;
        TextView favorite_name;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            favorite_image = itemView.findViewById(R.id.music_img);
            favorite_name = itemView.findViewById(R.id.music_file_nam);
            menuMore = itemView.findViewById(R.id.menuMore);
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }


}
