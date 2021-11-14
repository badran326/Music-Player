package com.badran.badranaudioplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class FavoriteFragment extends Fragment {

    public static ArrayList<MusicFiles> favorite = new ArrayList<>();
    static TextView countNum;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.favorRecyclerView);
        TextView count = view.findViewById(R.id.count);
        countNum = view.findViewById(R.id.count_num);
        MyDatabase db = new MyDatabase(getActivity());
        favorite = db.getAllFavorSongs();
        count.setText("Favorite songs are");
        countNum.setText(String.valueOf(db.getSongsCount()));
        if (!(favorite.size() < 1)) {
            FavoriteAdapter favoriteAdapter = new FavoriteAdapter(getActivity(), favorite);
            RecyclerView.LayoutManager lm = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(lm);
            recyclerView.setAdapter(favoriteAdapter);
            recyclerView.setSaveEnabled(true);
        }
    }
}