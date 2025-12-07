package com.naufal.younifirst.Home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.naufal.younifirst.R;
import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.model.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    // Views untuk welcome section
    private TextView tvWelcomeMessage;

    // Views untuk postingan Lost & Found
    private ConstraintLayout containerPostingan;
    private ImageView ivUserImage, ivItemImage;
    private TextView tvUserName, tvTimeAgo, tvLocation, tvDescription;
    private Button btnStatus;
    private ImageView btnMore, btnShare;
    private LinearLayout layoutLocation;
    private TextView tvEmptyState;

    // Views untuk Event Mendatang
    private TextView tvEventSectionTitle;
    private LinearLayout containerEvents;
    private TextView tvEventEmptyState;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        initViews(view);

        // Set username from login
        setUserWelcomeMessage();

        // Fetch and display lost found data
        fetchAndDisplayLostFound();

        // Fetch and display upcoming events
        fetchAndDisplayUpcomingEvents();

        return view;
    }

    private void initViews(View view) {
        // Welcome section
        tvWelcomeMessage = view.findViewById(R.id.tvWelcomeMessage);

        // Postingan Lost & Found container
        containerPostingan = view.findViewById(R.id.containerPostingan);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        // User info
        ivUserImage = view.findViewById(R.id.iv_user_image);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvTimeAgo = view.findViewById(R.id.tv_time_ago);
        btnStatus = view.findViewById(R.id.btn_status);
        btnMore = view.findViewById(R.id.btn_more);

        // Item image
        ivItemImage = view.findViewById(R.id.iv_item_image);

        // Location
        layoutLocation = view.findViewById(R.id.layout_location);
        tvLocation = view.findViewById(R.id.tv_location);

        // Description
        tvDescription = view.findViewById(R.id.tv_description);

        // Share button
        btnShare = view.findViewById(R.id.btn_share);

        // Event Mendatang section
        tvEventSectionTitle = view.findViewById(R.id.tvEventSectionTitle);
        containerEvents = view.findViewById(R.id.containerEvents);
        tvEventEmptyState = view.findViewById(R.id.tvEventEmptyState);
    }

    private void setUserWelcomeMessage() {
        // Cek apakah context tersedia
        if (getActivity() == null) return;

        // Debug: Cek semua data di SharedPreferences
        String email = ApiHelper.getSavedEmail();
        String userId = ApiHelper.getSavedUserId();
        String userName = ApiHelper.getSavedUserName();

        Log.d(TAG, "DEBUG - Email dari SharedPrefs: " + email);
        Log.d(TAG, "DEBUG - UserID dari SharedPrefs: " + userId);
        Log.d(TAG, "DEBUG - UserName dari SharedPrefs: " + userName);

        if (tvWelcomeMessage != null) {
            if (userName != null && !userName.isEmpty() && !userName.equals("null")) {
                String welcomeText = "Selamat datang " + userName + "!";
                tvWelcomeMessage.setText(welcomeText);
                Log.d(TAG, "SUCCESS - TextView diupdate: " + welcomeText);
            } else {
                // Coba ambil dari email jika username kosong
                if (email != null && !email.isEmpty() && email.contains("@")) {
                    String nameFromEmail = email.split("@")[0];
                    tvWelcomeMessage.setText("Selamat datang " + nameFromEmail + "!");
                } else {
                    tvWelcomeMessage.setText("Selamat datang!");
                }
            }
        }
    }

    private void fetchAndDisplayLostFound() {
        ApiHelper.fetchLostFound(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        displayLostFoundData(result);
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                // Hide postingan container jika error
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        containerPostingan.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Tidak ada postingan Lost & Found");
                    });
                }
            }
        });
    }

    private void fetchAndDisplayUpcomingEvents() {
        ApiHelper.fetchEvent(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Event data fetched successfully");
                Log.d(TAG, "📄 Response: " + result.substring(0, Math.min(200, result.length())) + "...");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            parseAndDisplayEvents(result);
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parsing events: " + e.getMessage());
                            showEventEmptyState("Gagal memuat event");
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Failed to fetch events: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showEventEmptyState("Tidak ada event mendatang");
                    });
                }
            }
        });
    }

    private void parseAndDisplayEvents(String jsonData) {
        Log.d(TAG, "🔍 Parsing events data...");

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray dataArray = jsonObject.optJSONArray("data");

            Log.d(TAG, "📊 JSON structure: has data array? " + (dataArray != null));
            Log.d(TAG, "📊 JSON keys: " + jsonObject.toString().substring(0, Math.min(200, jsonObject.toString().length())));

            // Coba berbagai struktur JSON yang mungkin
            if (dataArray == null) {
                if (jsonObject.has("events")) {
                    dataArray = jsonObject.getJSONArray("events");
                    Log.d(TAG, "📊 Using 'events' array");
                } else if (jsonObject.has("data")) {
                    // Coba parsing data sebagai string terlebih dahulu
                    String dataString = jsonObject.optString("data", "");
                    if (!dataString.isEmpty()) {
                        try {
                            dataArray = new JSONArray(dataString);
                            Log.d(TAG, "📊 Parsed 'data' string to array");
                        } catch (JSONException e) {
                            Log.e(TAG, "Cannot parse 'data' as array: " + e.getMessage());
                        }
                    }
                }
            }

            containerEvents.removeAllViews(); // Clear previous views

            if (dataArray != null && dataArray.length() > 0) {
                Log.d(TAG, "✅ Found " + dataArray.length() + " events");

                int eventCount = 0;

                for (int i = 0; i < dataArray.length(); i++) {
                    try {
                        JSONObject eventJson = dataArray.getJSONObject(i);
                        Event event = new Event(eventJson);

                        // Debug log event data
                        Log.d(TAG, "🎯 Event " + i + ": " + event.getNameEvent());
                        Log.d(TAG, "   📅 Date: " + event.getTanggalMulai());
                        Log.d(TAG, "   🏢 Location: " + event.getLokasi());
                        Log.d(TAG, "   📸 Poster: " + event.getSafePosterUrl());

                        // Filter hanya event yang belum lewat tanggalnya
                        if (isUpcomingEvent(event)) {
                            // Create and add event card
                            View eventCard = createEventCard(event);
                            containerEvents.addView(eventCard);

                            // Add margin between cards (except for last card)
                            if (eventCount < 4) { // Add margin to all but the last card
                                LinearLayout.LayoutParams params =
                                        (LinearLayout.LayoutParams) eventCard.getLayoutParams();
                                params.setMargins(0, 0, 16, 0); // Right margin 16dp
                                eventCard.setLayoutParams(params);
                            }

                            eventCount++;

                            // Limit to 5 events for home screen
                            if (eventCount >= 5) {
                                break;
                            }
                        } else {
                            Log.d(TAG, "⏰ Event '" + event.getNameEvent() + "' is not upcoming");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "❌ Error parsing event at index " + i + ": " + e.getMessage());
                    }
                }

                if (eventCount > 0) {
                    // Update UI
                    tvEventSectionTitle.setVisibility(View.VISIBLE);
                    containerEvents.setVisibility(View.VISIBLE);
                    tvEventEmptyState.setVisibility(View.GONE);

                    Log.d(TAG, "✅ Loaded " + eventCount + " upcoming events");
                } else {
                    // No upcoming events
                    showEventEmptyState("Tidak ada event mendatang");
                    Log.d(TAG, "ℹ No upcoming events found");
                }
            } else {
                // No events data
                showEventEmptyState("Belum ada event");
                Log.d(TAG, "ℹ No events data found in response");
            }
        } catch (JSONException e) {
            Log.e(TAG, "❌ JSON parsing error: " + e.getMessage());
            showEventEmptyState("Gagal memuat event");
        }
    }

    private View createEventCard(Event event) {
        // Inflate a new event card using the same layout as EventFragment
        View eventCard = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_event_mendatang, null);

        try {
            // Get views from card - SAME as EventFragment
            ImageView imgPoster = eventCard.findViewById(R.id.img_poster);
            TextView badgeStatus = eventCard.findViewById(R.id.badge_status);
            TextView textDate = eventCard.findViewById(R.id.text_date);
            TextView textTitle = eventCard.findViewById(R.id.text_title);
            TextView textLocation = eventCard.findViewById(R.id.text_location);

            // Set event data - SAME as EventFragment
            textTitle.setText(event.getNameEvent());
            textDate.setText(event.getFormattedDateForCard());
            textLocation.setText(event.getLokasi());

            // Set badge status if needed - SAME logic as EventFragment
            if (event.isAlmostEnding()) {
                String badgeText = getBadgeTextForDeadline(event);
                if (badgeText != null) {
                    badgeStatus.setText(badgeText);
                    badgeStatus.setVisibility(View.VISIBLE);
                    eventCard.findViewById(R.id.badge_background).setVisibility(View.VISIBLE);
                    Log.d(TAG, "🏷 Added badge: " + badgeText + " for event: " + event.getNameEvent());
                } else {
                    badgeStatus.setVisibility(View.GONE);
                    eventCard.findViewById(R.id.badge_background).setVisibility(View.GONE);
                }
            } else {
                badgeStatus.setVisibility(View.GONE);
                eventCard.findViewById(R.id.badge_background).setVisibility(View.GONE);
            }

            // Load poster image with Glide - SAME as EventFragment
            String posterUrl = event.getSafePosterUrl();
            if (posterUrl != null && !posterUrl.isEmpty()) {
                Log.d(TAG, "🖼 Loading poster from URL: " + posterUrl);
                Glide.with(this)
                        .load(posterUrl)
                        .placeholder(R.drawable.tryposter)
                        .error(R.drawable.tryposter)
                        .centerCrop()
                        .into(imgPoster);
            } else {
                imgPoster.setImageResource(R.drawable.tryposter);
                Log.d(TAG, "🖼 Using default poster for: " + event.getNameEvent());
            }

            // Set click listener
            eventCard.setOnClickListener(v -> {
                Toast.makeText(getContext(),
                        "Event: " + event.getNameEvent(),
                        Toast.LENGTH_SHORT).show();
                // TODO: Navigate to event detail
                Log.d(TAG, "👆 Clicked event: " + event.getNameEvent());
            });

            return eventCard;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating event card: " + e.getMessage());
            return eventCard;
        }
    }

    // Helper method for badge text - SAME as EventFragment
    private String getBadgeTextForDeadline(Event event) {
        if (!event.isAlmostEnding()) {
            return null;
        }

        int days = event.getDaysUntilDeadline();

        if (days < 0) {
            return null; // Sudah lewat deadline, tidak perlu badge
        } else if (days == 0) {
            return "Hari ini\nBerakhir";
        } else if (days == 1) {
            return "Besok\nBerakhir";
        } else {
            return "Hampir\nBerakhir";
        }
    }

    private void showEventEmptyState(String message) {
        tvEventSectionTitle.setVisibility(View.GONE);
        containerEvents.setVisibility(View.GONE);
        tvEventEmptyState.setVisibility(View.VISIBLE);
        tvEventEmptyState.setText(message);
        Log.d(TAG, "📭 Event empty state: " + message);
    }

    private boolean isUpcomingEvent(Event event) {
        try {
            String dateStr = event.getTanggalMulai();
            if (dateStr == null || dateStr.isEmpty() || "null".equalsIgnoreCase(dateStr)) {
                Log.d(TAG, "⚠ Event '" + event.getNameEvent() + "' has no date");
                return false;
            }

            SimpleDateFormat sdf;

            // Try format with time first
            if (dateStr.contains(" ")) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            } else if (dateStr.contains("T")) {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }

            Date eventDate = sdf.parse(dateStr);
            Date currentDate = new Date();

            boolean isUpcoming = !eventDate.before(currentDate);

            Log.d(TAG, "📅 Checking event date: " + dateStr +
                    " -> " + sdf.format(eventDate) +
                    " (Upcoming: " + isUpcoming + ")");

            return isUpcoming;

        } catch (ParseException e) {
            Log.e(TAG, "❌ Error parsing event date for '" + event.getNameEvent() + "': " + e.getMessage());
            Log.e(TAG, "   Date string: " + event.getTanggalMulai());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "❌ Unexpected error checking event date: " + e.getMessage());
            return false;
        }
    }

    private void displayLostFoundData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray dataArray = jsonObject.optJSONArray("data");

            if (dataArray != null && dataArray.length() > 0) {
                // Ambil postingan terbaru (indeks pertama)
                JSONObject item = dataArray.getJSONObject(0);

                // Set user info
                String userName;
                if (item.has("user") && !item.isNull("user")) {
                    JSONObject user = item.getJSONObject("user");
                    userName = user.optString("name", "User");
                } else {
                    userName = ApiHelper.getSavedUserName();
                    if (userName == null || userName.isEmpty()) {
                        userName = "User";
                    }
                }
                tvUserName.setText(userName);

                // Set time ago
                String createdAt = item.optString("created_at", "");
                if (!createdAt.isEmpty()) {
                    tvTimeAgo.setText(getTimeAgo(createdAt));
                }

                // Set status button
                String status = item.optString("status", "");
                if ("found".equalsIgnoreCase(status)) {
                    btnStatus.setText("Menemukan");
                    btnStatus.setBackgroundResource(R.drawable.custom_button_rounded_corner_putih);
                } else if ("lost".equalsIgnoreCase(status)) {
                    btnStatus.setText("Kehilangan");
                    // Buat background merah untuk lost status
                    btnStatus.setBackgroundResource(R.drawable.custom_button_rounded_corner_putih);
                }

                // Set location
                String location = item.optString("lokasi", "Lokasi tidak diketahui");
                tvLocation.setText(location);

                // Set description
                String description = item.optString("deskripsi", "");
                tvDescription.setText(description);

                // Set item image
                String imageUrl = item.optString("foto_barang", "");
                if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
                    // Load image with Glide
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.gambar_lostandfound)
                            .error(R.drawable.gambar_lostandfound)
                            .centerCrop()
                            .into(ivItemImage);
                } else {
                    ivItemImage.setImageResource(R.drawable.gambar_lostandfound);
                }

                // Set user image (default untuk sekarang)
                ivUserImage.setImageResource(R.drawable.profile_coba);

                // Show the container
                containerPostingan.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);

            } else {
                // Hide container jika tidak ada data
                containerPostingan.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Belum ada postingan Lost & Found");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            containerPostingan.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Gagal memuat postingan");
        }
    }

    private String getTimeAgo(String dateTime) {
        try {
            // Format: "yyyy-MM-dd HH:mm:ss"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date createdDate = sdf.parse(dateTime);
            Date currentDate = new Date();

            long diff = currentDate.getTime() - createdDate.getTime();
            long diffMinutes = diff / (60 * 1000);
            long diffHours = diff / (60 * 60 * 1000);
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffDays > 0) {
                return diffDays + " hari lalu";
            } else if (diffHours > 0) {
                return diffHours + " jam lalu";
            } else if (diffMinutes > 0) {
                return diffMinutes + " menit lalu";
            } else {
                return "Baru saja";
            }
        } catch (Exception e) {
            return "Baru saja";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "HomeFragment onResume - Refreshing data");

        // Refresh data when fragment is resumed
        fetchAndDisplayLostFound();
        fetchAndDisplayUpcomingEvents();
    }
}