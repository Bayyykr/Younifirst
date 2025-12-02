package com.naufal.younifirst.controller;

import android.util.Log;

import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.model.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventController {

    private static final String TAG = "EventController";

    public interface EventCallback {
        void onSuccess(List<Event> events);
        void onError(String message);
    }

    public void fetchEvent(EventCallback callback) {
        Log.d(TAG, "🎯 Fetching events from API...");

        ApiHelper.fetchEvent(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    List<Event> events = parseEventData(result);
                    Log.d(TAG, "✅ Successfully parsed " + events.size() + " events");
                    callback.onSuccess(events);
                } catch (JSONException e) {
                    Log.e(TAG, "❌ Failed to parse JSON: " + e.getMessage());
                    callback.onError("Format data tidak valid: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Failed to fetch events: " + error);
                callback.onError(error);
            }
        });
    }

    public void fetchUpcomingEvents(EventCallback callback) {
        Log.d(TAG, "📅 Fetching upcoming events...");

        fetchEvent(new EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                // Filter hanya event mendatang (tanggal > sekarang)
                List<Event> upcomingEvents = filterUpcomingEvents(events);

                // Sort by date ascending (terdekat dulu)
                upcomingEvents.sort(Comparator.comparing(Event::getTanggalMulai));

                // Ambil max 5 event terdekat
                int count = Math.min(5, upcomingEvents.size());
                List<Event> limitedUpcoming = upcomingEvents.subList(0, count);

                Log.d(TAG, "✅ Found " + limitedUpcoming.size() + " upcoming events");
                callback.onSuccess(limitedUpcoming);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void fetchTrendingEvents(EventCallback callback) {
        Log.d(TAG, "🔥 Fetching trending events...");

        fetchEvent(new EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                // Debug: Tampilkan semua events yang diterima
                Log.d(TAG, "📊 Total events dari API: " + events.size());
                if (!events.isEmpty()) {
                    for (int i = 0; i < Math.min(5, events.size()); i++) {
                        Event e = events.get(i);
                        Log.d(TAG, "Event " + i + ": " + e.getNameEvent() +
                                ", Kapasitas: " + e.getKapasitas() +
                                ", Date: " + e.getTanggalMulai());
                    }
                }

                // Filter trending: SEMUA EVENT (tidak filter kapasitas)
                List<Event> trendingEvents = filterTrendingEvents(events);

                Log.d(TAG, "✅ Found " + trendingEvents.size() + " trending events");

                // Debug: Tampilkan trending events
                if (!trendingEvents.isEmpty()) {
                    for (int i = 0; i < Math.min(5, trendingEvents.size()); i++) {
                        Event e = trendingEvents.get(i);
                        Log.d(TAG, "Trending " + i + ": " + e.getNameEvent() +
                                ", Kapasitas: " + e.getKapasitas());
                    }
                }

                callback.onSuccess(trendingEvents);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private List<Event> parseEventData(String jsonString) throws JSONException {
        List<Event> events = new ArrayList<>();

        try {
            JSONObject jsonResponse = new JSONObject(jsonString);
            boolean success = jsonResponse.optBoolean("success", false);

            if (!success) {
                Log.e(TAG, "API returned success: false");
                return events;
            }

            JSONArray jsonArray = jsonResponse.optJSONArray("data");

            if (jsonArray == null) {
                Log.e(TAG, "Data array is null");
                return events;
            }

            Log.d(TAG, "📦 Found " + jsonArray.length() + " events in data array");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                try {
                    Event event = new Event(jsonObject);
                    events.add(event);

                    // Debug gambar
                    Log.d(TAG, "🖼️ Event " + i + ": " + event.getNameEvent() +
                            ", Poster field: " + event.getPosterEvent() +
                            ", Full URL: " + event.getFullPosterUrl());

                } catch (JSONException e) {
                    Log.w(TAG, "⚠️ Failed to parse event #" + i + ": " + e.getMessage());
                }
            }

        } catch (JSONException e) {
            throw e;
        }

        Log.d(TAG, "✅ Total events parsed: " + events.size());
        return events;
    }

    private List<Event> filterUpcomingEvents(List<Event> events) {
        List<Event> upcoming = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date now = new Date();

        for (Event event : events) {
            try {
                Date eventDate = dateFormat.parse(event.getTanggalMulai());
                if (eventDate != null && eventDate.after(now)) {
                    upcoming.add(event);
                }
            } catch (ParseException e) {
                Log.w(TAG, "⚠️ Failed to parse event date: " + event.getTanggalMulai());
            }
        }

        Log.d(TAG, "📅 Upcoming events filtered: " + upcoming.size());
        return upcoming;
    }

    private List<Event> filterTrendingEvents(List<Event> events) {
        List<Event> trending = new ArrayList<>();

        // PERUBAHAN PENTING: Ambil SEMUA event, jangan filter kapasitas!
        // Trending = semua event terbaru

        Log.d(TAG, "🔥 Filtering trending events from " + events.size() + " total events");

        // Sort by date descending (terbaru dulu)
        events.sort((e1, e2) -> {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date1 = dateFormat.parse(e1.getTanggalMulai());
                Date date2 = dateFormat.parse(e2.getTanggalMulai());
                return date2.compareTo(date1); // Descending (terbaru dulu)
            } catch (ParseException e) {
                Log.e(TAG, "❌ Error parsing date for sorting");
                return 0;
            }
        });

        // Debug setelah sorting
        if (!events.isEmpty()) {
            Log.d(TAG, "📊 After sorting, first event: " + events.get(0).getNameEvent() +
                    ", Date: " + events.get(0).getTanggalMulai());
        }

        // Ambil semua event sebagai trending (atau maksimal 20)
        int maxTrending = Math.min(20, events.size());
        trending = new ArrayList<>(events.subList(0, maxTrending));

        Log.d(TAG, "✅ Trending events selected: " + trending.size());
        return trending;
    }

    // Method untuk test API response
    public void debugApiResponse(String result) {
        try {
            List<Event> events = parseEventData(result);
            Log.d(TAG, "🔍 DEBUG: Total events: " + events.size());

            if (events.isEmpty()) {
                Log.e(TAG, "🔍 DEBUG: No events parsed!");
                return;
            }

            // Tampilkan 5 event pertama
            for (int i = 0; i < Math.min(5, events.size()); i++) {
                Event event = events.get(i);
                Log.d(TAG, "🔍 Event " + i + ": " + event.getNameEvent() +
                        ", ID: " + event.getEventId() +
                        ", Date: " + event.getTanggalMulai() +
                        ", Kapasitas: " + event.getKapasitas() +
                        ", Organizer: " + event.getOrganizer());
            }

        } catch (JSONException e) {
            Log.e(TAG, "🔍 DEBUG Parse error: " + e.getMessage());
        }
    }
}