package com.naufal.younifirst.Home;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.naufal.younifirst.Event.DetailEventActivity;
import com.naufal.younifirst.R;
import com.naufal.younifirst.controller.EventController;
import com.naufal.younifirst.model.Event;

import java.util.List;

public class EventFragment extends Fragment {

    private static final String TAG = "EventFragment";
    private LinearLayout containerEvent;
    private LinearLayout trendingContainer;
    private EventController eventController;

    public EventFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        Toast.makeText(getContext(), "Memuat data event...", Toast.LENGTH_SHORT).show();

        View eventUtamaView = view.findViewById(R.id.event_utama);
        if (eventUtamaView != null) {
            containerEvent = eventUtamaView.findViewById(R.id.container_event);
            trendingContainer = eventUtamaView.findViewById(R.id.trending_container);

            hideStaticData(eventUtamaView);
        }

        if (containerEvent == null) containerEvent = view.findViewById(R.id.container_event);
        if (trendingContainer == null) trendingContainer = view.findViewById(R.id.trending_container);

        if (containerEvent == null) {
            Log.e(TAG, "container_event masih null!");
        } else {
            Log.d(TAG, "container_event ditemukan");
        }

        if (trendingContainer == null) {
            Log.e(TAG, "trending_container masih null!");
        } else {
            Log.d(TAG, "trending_container ditemukan");
        }

        loadEvents();

        return view;
    }

    private void hideStaticData(View eventUtamaView) {
        try {
            Log.d(TAG, "🙈 Menyembunyikan data statis dari XML...");

            LinearLayout item1 = eventUtamaView.findViewById(R.id.item1EventMendatang);
            LinearLayout item2 = eventUtamaView.findViewById(R.id.item2EventMendatang);

            if (item1 != null) item1.setVisibility(View.GONE);
            if (item2 != null) item2.setVisibility(View.GONE);

            LinearLayout trendingItem1 = eventUtamaView.findViewById(R.id.trending_item_1);
            LinearLayout trendingItem2 = eventUtamaView.findViewById(R.id.trending_item_2);

            if (trendingItem1 != null) trendingItem1.setVisibility(View.GONE);
            if (trendingItem2 != null) trendingItem2.setVisibility(View.GONE);

            if (trendingContainer != null) {
                for (int i = 0; i < trendingContainer.getChildCount(); i++) {
                    View child = trendingContainer.getChildAt(i);
                    if (child instanceof LinearLayout) child.setVisibility(View.GONE);
                }
            }

            Log.d(TAG, "✅ Semua data statis berhasil di-hide");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error hiding static data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEvents() {
        eventController = new EventController();
        Log.d(TAG, "Memulai loadEvents()");
        loadTrendingEvents();
        loadUpcomingEvents();
    }

    private void loadTrendingEvents() {
        Log.d(TAG, "🔥 Memuat ALL trending events...");

        eventController.fetchTrendingEvents(new EventController.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d(TAG, "✅ ALL Trending events diterima: " + events.size() + " items");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> displayTrendingEvents(events));
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "❌ Error loading trending events: " + message);
            }
        });
    }

    private void loadUpcomingEvents() {
        Log.d(TAG, "📅 Memuat upcoming events...");

        eventController.fetchUpcomingEvents(new EventController.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d(TAG, "✅ Upcoming events diterima: " + events.size() + " items");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> displayUpcomingEvents(events));
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "❌ Error loading upcoming events: " + message);
            }
        });
    }

    private void displayTrendingEvents(List<Event> events) {
        if (trendingContainer == null) return;
        trendingContainer.removeAllViews();

        if (events.isEmpty()) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("Belum ada event trending");
            emptyView.setTextColor(0x7FFFFFFF);
            emptyView.setTextSize(14);
            emptyView.setPadding(0, 32, 0, 32);
            trendingContainer.addView(emptyView);
            return;
        }

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            View eventView = createTrendingEventView(event);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i < events.size() - 1) params.setMargins(0, 0, 0, 16);

            eventView.setLayoutParams(params);
            trendingContainer.addView(eventView);
        }
    }

    private void displayUpcomingEvents(List<Event> events) {
        if (containerEvent == null) return;
        containerEvent.removeAllViews();

        if (events.isEmpty()) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("Belum ada event mendatang");
            emptyView.setTextColor(0x7FFFFFFF);
            emptyView.setTextSize(14);
            emptyView.setPadding(0, 32, 0, 32);
            containerEvent.addView(emptyView);
            return;
        }

        int count = Math.min(5, events.size());
        for (int i = 0; i < count; i++) {
            Event event = events.get(i);
            View eventView = createUpcomingEventView(event);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i < count - 1) params.setMargins(0, 0, 16, 0);

            eventView.setLayoutParams(params);
            containerEvent.addView(eventView);
        }
    }

    private View createTrendingEventView(Event event) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_event_list, trendingContainer, false);

        bindEventToView(view, event, true);
        view.setOnClickListener(v -> openDetailEvent(event));
        return view;
    }

    private View createUpcomingEventView(Event event) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_event_mendatang, containerEvent, false);

        bindEventToView(view, event, false);
        view.setOnClickListener(v -> openDetailEvent(event));
        return view;
    }

    private void bindEventToView(View view, Event event, boolean isTrending) {
        try {
            TextView textDate = view.findViewById(R.id.text_date);
            TextView textTitle = view.findViewById(R.id.text_title);
            TextView textLocation = view.findViewById(R.id.text_location);
            ImageView imgPoster = view.findViewById(R.id.img_poster);

            // Cari badge yang ada di include
            TextView badgeStatus = view.findViewById(R.id.badge_status);

            if (textDate != null) textDate.setText(event.getFormattedDate());
            if (textTitle != null) textTitle.setText(event.getNameEvent());
            if (textLocation != null) textLocation.setText(event.getLokasi());

            // Set badge konsisten untuk semua Event
            applyBadgeToView(badgeStatus, event, isTrending);

            if (imgPoster != null) {
                if (event.getPosterEvent() != null && !event.getPosterEvent().isEmpty()
                        && !"null".equals(event.getPosterEvent())) {
                    Glide.with(this)
                            .load(event.getPosterEvent())
                            .placeholder(R.drawable.tryposter)
                            .into(imgPoster);
                } else {
                    imgPoster.setImageResource(R.drawable.tryposter);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error binding event: " + e.getMessage());
        }
    }


    private void applyBadgeToView(TextView badgeStatus, Event event, boolean isTrending) {
        if (badgeStatus == null) return;

        String badgeText = event.getBadgeText();
        int badgeColor = event.getBadgeColor();

        if (badgeText == null || badgeText.trim().isEmpty() || "null".equalsIgnoreCase(badgeText)) {
            if (isTrending) badgeText = "TRENDING";
            else badgeText = event.isConfirmed() ? "TERKONFIRMASI" : "PENDING";
        }

        if (badgeColor == 0) badgeColor = Color.parseColor("#2D6A4F");

        badgeStatus.setText(badgeText);

        try {
            badgeStatus.setBackground(null);
        } catch (Exception e) {
            badgeStatus.setBackgroundColor(badgeColor);
        }
    }

    private void openDetailEvent(Event event) {
        Intent intent = new Intent(getActivity(), DetailEventActivity.class);
        intent.putExtra("event_id", event.getEventId());
        intent.putExtra("event_name", event.getNameEvent());
        intent.putExtra("event_date", event.getTanggalMulai());
        intent.putExtra("event_location", event.getLokasi());
        intent.putExtra("event_organizer", event.getOrganizer());
        intent.putExtra("event_poster", event.getPosterEvent());
        intent.putExtra("event_description", event.getDescription());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "EventFragment onResume");
    }
}