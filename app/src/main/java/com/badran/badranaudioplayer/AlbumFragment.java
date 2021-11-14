package com.badran.badranaudioplayer;

import static com.badran.badranaudioplayer.MainActivity.albums;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumFragment extends Fragment {

    public AlbumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        TextView count = view.findViewById(R.id.count);
        TextView countNum = view.findViewById(R.id.count_num);
        count.setText("albums are");
        countNum.setText("" + albums.size());
        if (!(albums.size() < 1)) {
            AlbumAdapter albumAdapter = new AlbumAdapter(getActivity(), albums);
            RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity(), 2);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(lm);
            recyclerView.setAdapter(albumAdapter);
            recyclerView.setSaveEnabled(true);
        }
        return view;
    }
}