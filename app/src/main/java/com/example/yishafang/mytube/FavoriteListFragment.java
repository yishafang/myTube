package com.example.yishafang.mytube;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author yishafang on 10/15/15.
 */
public class FavoriteListFragment extends Fragment {
    public static final String ARG_PAGE = "FAVORITE";

    public static FavoriteListFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        FavoriteListFragment favoriteListFragment = new FavoriteListFragment();
        favoriteListFragment.setArguments(args);
        return favoriteListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_list, container, false);

        return view;
    }
}
