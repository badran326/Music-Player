package com.badran.badranaudioplayer;

import static com.badran.badranaudioplayer.MainActivity.musicFiles;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class SongsFragment extends Fragment {

    public static MusicAdapter musicAdapter;

    public SongsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        TextView count = view.findViewById(R.id.count);
        TextView countNum = view.findViewById(R.id.count_num);
        count.setText("Songs are");
        countNum.setText(String.valueOf(musicFiles.size()));
        if (!(musicFiles.size() < 1)) {
            musicAdapter = new MusicAdapter(getActivity(), musicFiles);
            RecyclerView.LayoutManager lm = new LinearLayoutManager(getActivity());
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(lm);
            recyclerView.setAdapter(musicAdapter);
            recyclerView.setSaveEnabled(true);
        }
    }
}