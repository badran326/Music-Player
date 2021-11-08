package com.badran.badranaudioplayer;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MyDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "songs_favorite";
    private static final int DB_VERSION = 1;
    public static final String SONG = "song";
    public static final String ID = "id";
    public static final String PATH = "path";
    public static final String TITLE = "title";
    public static final String ARTIST = "artist";
    public static final String ALBUM = "album";
    public static final String DURATION = "duration";
    public static final String IDS = "ids";
    public static final String COLOR = "color";

    public MyDatabase(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL( " CREATE TABLE " +SONG+ " ( " +ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +PATH+ " TEXT UNIQUE, "
                +TITLE+ " TEXT, " +ARTIST+ " TEXT, " +ALBUM+ " TEXT, " +DURATION+ " TEXT, " +IDS+ " TEXT) " );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean insertFavorSong(MusicFiles favoriteSongs) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PATH, favoriteSongs.getPath());
        values.put(TITLE, favoriteSongs.getTitle());
        values.put(ARTIST, favoriteSongs.getArtist());
        values.put(ALBUM, favoriteSongs.getAlbum());
        values.put(DURATION, favoriteSongs.getDuration());
        values.put(IDS, favoriteSongs.getIds());
        long result = db.insert(SONG, null, values);
        return result != -1;
    }

    public boolean upDateFavor(MusicFiles favoriteSongs) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PATH, favoriteSongs.getPath());
        values.put(TITLE, favoriteSongs.getTitle());
        values.put(ARTIST, favoriteSongs.getArtist());
        values.put(ALBUM, favoriteSongs.getAlbum());
        values.put(DURATION, favoriteSongs.getDuration());
        values.put(IDS, favoriteSongs.getIds());

        String[] args = {String.valueOf(favoriteSongs.getId())};
        int result = db.update(SONG, values, "id=?", args);
        return result > 0;
    }

    public boolean deleteFavor(MusicFiles favoriteSongs) {
        SQLiteDatabase db = getWritableDatabase();

        String[] args = {String.valueOf(favoriteSongs.getId())};
        int result = db.delete(SONG, " id=? ", args);
        return result > 0;
    }

    public long getSongsCount() {
        SQLiteDatabase db = getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, SONG);
    }

    public ArrayList<MusicFiles> getAllFavorSongs() {
        ArrayList<MusicFiles> favoriteSongs = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(" SELECT * FROM " +SONG, null);
        if (cursor.moveToFirst()) {
            do {
            int id = cursor.getInt(cursor.getColumnIndex(ID));
            String path = cursor.getString(cursor.getColumnIndex(PATH));
            String title = cursor.getString(cursor.getColumnIndex(TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(ALBUM));
            String duration = cursor.getString(cursor.getColumnIndex(DURATION));
            String ids = cursor.getString(cursor.getColumnIndex(IDS));
            MusicFiles musicFiles = new MusicFiles(id, path, title, artist, album, duration, ids);
                favoriteSongs.add(musicFiles);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return favoriteSongs;
    }
}
