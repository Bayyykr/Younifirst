package com.naufal.younifirst.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.naufal.younifirst.Event.DetailEventActivity;
import com.naufal.younifirst.R;

public class EventFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public EventFragment() {
    }

    public static EventFragment newInstance(String param1, String param2) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        setupClickListeners(view);

        return view;
    }

    private void setupClickListeners(View view) {
        View item1EventMendatang = view.findViewById(R.id.item1EventMendatang);
        if (item1EventMendatang != null) {
            item1EventMendatang.setOnClickListener(v -> {
                openDetailEvent();
            });
        }

        View sedangTrending1 = view.findViewById(R.id.trending_item_1);
        if (sedangTrending1 != null) {
            sedangTrending1.setOnClickListener(v -> {
                openDetailEvent();
            });
        }

        setupAllTrendingItemsClick(view);
    }

    private void setupAllTrendingItemsClick(View view) {
        // Cari parent container dari item trending
        ViewGroup trendingContainer = (ViewGroup) view.findViewById(R.id.trending_container);
        if (trendingContainer == null) {
            int[] trendingIds = {
                    R.id.trending_container,
            };

            for (int id : trendingIds) {
                View trendingItem = view.findViewById(id);
                if (trendingItem != null) {
                    trendingItem.setOnClickListener(v -> openDetailEvent());
                }
            }
        } else {
            for (int i = 0; i < trendingContainer.getChildCount(); i++) {
                View child = trendingContainer.getChildAt(i);
                child.setOnClickListener(v -> openDetailEvent());
            }
        }
    }

    private void openDetailEvent() {
        Intent intent = new Intent(getActivity(), DetailEventActivity.class);
        startActivity(intent);
    }
}