package com.badran.badranaudioplayer;

import static com.badran.badranaudioplayer.PlayerActivity.mIsPlaying;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyHolder> {

    private final Context mContext;
    private final ArrayList<MusicFiles> albumFiles;
    View view;
    Bitmap bitmap;

    public AlbumAdapter(Context mContext, ArrayList<MusicFiles> albumFiles) {
        this.mContext = mContext;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.album_name.setText(albumFiles.get(position).getAlbum());
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());
        if (image != null) {
            holder.album_image.setBackgroundResource(0);
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            Glide.with(mContext).asBitmap()
                    .load(ImageHelper.getRoundedCornerBitmap(bitmap, 1000))
                    .into(holder.album_image);
        }
        else {
//            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.radius);
            holder.album_image.setBackgroundResource(R.drawable.radius);
            Glide.with(mContext)
                    .load(R.drawable.ic_baseline_play_arrow)
                    .into(holder.album_image);
        }
        holder.itemView.setOnClickListener(view -> {
            mIsPlaying = true;
            Intent intent = new Intent(mContext, AlbumDetails.class);
            intent.putExtra("albumName", albumFiles.get(position).getAlbum());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {

        ImageView album_image;
        TextView album_name;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.album_image);
            album_name = itemView.findViewById(R.id.album_name);
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
