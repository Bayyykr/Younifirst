package com.naufal.younifirst.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

    public EventFragment() {}

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

        loadEvents();
        return view;
    }

    private void hideStaticData(View eventUtamaView) {
        try {
            LinearLayout item1 = eventUtamaView.findViewById(R.id.item1EventMendatang);
            LinearLayout item2 = eventUtamaView.findViewById(R.id.item2EventMendatang);

            if (item1 != null) item1.setVisibility(View.GONE);
            if (item2 != null) item2.setVisibility(View.GONE);

            LinearLayout trendingItem1 = eventUtamaView.findViewById(R.id.trending_item_1);
            LinearLayout trendingItem2 = eventUtamaView.findViewById(R.id.trending_item_2);

            if (trendingItem1 != null) trendingItem1.setVisibility(View.GONE);
            if (trendingItem2 != null) trendingItem2.setVisibility(View.GONE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadEvents() {
        eventController = new EventController();
        loadTrendingEvents();
        loadUpcomingEvents();
    }

    private void loadTrendingEvents() {

        eventController.fetchTrendingEvents(new EventController.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> displayTrendingEvents(events));
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error trending: " + message);
            }
        });
    }

    private void loadUpcomingEvents() {

        eventController.fetchUpcomingEvents(new EventController.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> displayUpcomingEvents(events));
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error upcoming: " + message);
            }
        });
    }

    private void displayTrendingEvents(List<Event> events) {
        if (trendingContainer == null) return;
        trendingContainer.removeAllViews();

        if (events.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("Belum ada event trending");
            empty.setTextColor(0x7FFFFFFF);
            trendingContainer.addView(empty);
            return;
        }

        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            View item = createTrendingEventView(e);

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i < events.size() - 1) p.setMargins(0, 0, 0, 16);
            item.setLayoutParams(p);

            trendingContainer.addView(item);
        }
    }

    private void displayUpcomingEvents(List<Event> events) {
        if (containerEvent == null) return;
        containerEvent.removeAllViews();

        if (events.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("Belum ada event mendatang");
            empty.setTextColor(0x7FFFFFFF);
            containerEvent.addView(empty);
            return;
        }

        int count = Math.min(5, events.size());
        for (int i = 0; i < count; i++) {
            Event e = events.get(i);
            View item = createUpcomingEventView(e);

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i < count - 1) p.setMargins(0, 0, 16, 0);
            item.setLayoutParams(p);

            containerEvent.addView(item);
        }
    }

    private View createTrendingEventView(Event event) {
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_event_list, trendingContainer, false);

        // PERBAIKAN: Gunakan setup yang sama dengan upcoming events
        setupEventView(v, event, true); // true untuk trending layout
        return v;
    }

    private View createUpcomingEventView(Event event) {
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_event_mendatang, containerEvent, false);

        // PERBAIKAN: Gunakan setup yang sama dengan trending events
        setupEventView(v, event, false); // false untuk upcoming layout
        return v;
    }

    private void setupEventView(View view, Event event, boolean isTrendingLayout) {
        // Setup data dasar - SAMA untuk kedua layout
        TextView textTitle = view.findViewById(R.id.text_title);
        TextView textDate = view.findViewById(R.id.text_date);
        TextView textLocation = view.findViewById(R.id.text_location);

        textTitle.setText(event.getNameEvent());

        String formattedDate = event.getFormattedDateForCard();
        textDate.setText(formattedDate);
        textLocation.setText(event.getLokasi());

        // PERBAIKAN: Setup gambar poster jika ada ImageView di layout
        setupEventPoster(view, event);

        // Setup badge kecil (kategori) - SAMA untuk kedua layout
        setupBadgeKecil(view, event);

        // Setup badge besar (status) - SAMA untuk kedua layout
        // Hanya tampil jika event hampir berakhir (H-7)
        setupBadgeBesar(view, event);

        // Setup klik listener - SAMA untuk kedua layout
        view.setOnClickListener(x -> openDetailEvent(event));
    }


    private void setupBadgeKecil(View view, Event event) {
        LinearLayout textContainer = view.findViewById(R.id.text_container);
        if (textContainer == null) return;

        removeExistingBadgeContainer(view);

        String kategori = event.getKategori();

        if (kategori == null || kategori.trim().isEmpty() ||
                "null".equalsIgnoreCase(kategori.trim())) {
            return;
        }

        String[] kategoriList = kategori.split("\\s*,\\s*");

        int validKategoriCount = 0;
        for (String kat : kategoriList) {
            if (kat != null && !kat.trim().isEmpty() && !"null".equalsIgnoreCase(kat.trim())) {
                validKategoriCount++;
            }
        }

        if (validKategoriCount == 0) {
            return;
        }

        LinearLayout badgeContainer = new LinearLayout(getContext());
        badgeContainer.setId(R.id.badge_kecil_container);
        badgeContainer.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 0, 0, 8);
        badgeContainer.setLayoutParams(containerParams);

        int insertPosition = 0;
        for (int i = 0; i < textContainer.getChildCount(); i++) {
            View child = textContainer.getChildAt(i);
            if (child.getId() == R.id.text_date) {
                insertPosition = i;
                break;
            }
        }

        textContainer.addView(badgeContainer, insertPosition);

        int addedBadges = 0;
        for (String kat : kategoriList) {
            if (addedBadges >= 3) break;

            kat = kat != null ? kat.trim() : "";
            if (!kat.isEmpty() && !"null".equalsIgnoreCase(kat)) {
                View badgeView = createBadgeKecilView(kat);
                if (badgeView != null) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    if (addedBadges > 0) {
                        params.setMargins(8, 0, 0, 0);
                    }
                    badgeView.setLayoutParams(params);

                    badgeContainer.addView(badgeView);
                    addedBadges++;
                }
            }
        }
    }

    private void removeExistingBadgeContainer(View view) {
        LinearLayout existingContainer = view.findViewById(R.id.badge_kecil_container);
        if (existingContainer != null && existingContainer.getParent() != null) {
            ((ViewGroup) existingContainer.getParent()).removeView(existingContainer);
        }

        View includeBadge = view.findViewById(R.id.include_badge_kecil);
        if (includeBadge != null && includeBadge.getParent() != null) {
            ((ViewGroup) includeBadge.getParent()).removeView(includeBadge);
        }
    }

    private View createBadgeKecilView(String kategori) {
        try {
            View badgeView = LayoutInflater.from(getContext())
                    .inflate(R.layout.badge_kecil, null, false);

            TextView badgeText = badgeView.findViewById(R.id.text_badge_kecil);
            if (badgeText == null) {
                return null;
            }

            if (kategori == null || kategori.isEmpty() || "null".equalsIgnoreCase(kategori)) {
                return null;
            }

            badgeText.setText(kategori);

            String kategoriLower = kategori.toLowerCase();
            int bgResource = R.drawable.badge_green;

            if (kategoriLower.contains("seminar")) {
                bgResource = R.drawable.badge_red;
            } else if (kategoriLower.contains("music") || kategoriLower.contains("konser")) {
                bgResource = R.drawable.badge_blue;
            } else if (kategoriLower.contains("kompetisi")) {
                bgResource = R.drawable.badge_green;
            } else if (kategoriLower.contains("sport") || kategoriLower.contains("olahraga")) {
                bgResource = R.drawable.badge_purple;
            }

            badgeView.setBackgroundResource(bgResource);
            badgeText.setTextColor(getResources().getColor(android.R.color.white));

            return badgeView;
        } catch (Exception e) {
            return null;
        }
    }
    private void setupEventPoster(View view, Event event) {
        ImageView imgPoster = view.findViewById(R.id.img_poster);
        if (imgPoster != null) {
            // Anda bisa menggunakan Glide/Picasso di sini
            // Contoh dengan Glide:
            String imageUrl = event.getSafePosterUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.tryposter)
                        .into(imgPoster);
            } else {
                imgPoster.setImageResource(R.drawable.tryposter);
            }

            // Untuk sekarang, log saja
            Log.d(TAG, "🖼 Event poster: " + event.getNameEvent() +
                    " - URL: " + event.getSafePosterUrl());
        }
    }

    private void setupBadgeBesar(View rootView, Event event) {
        FrameLayout badgeContainer = rootView.findViewById(R.id.badge_container);
        if (badgeContainer == null) return;

        // Cari ImageView untuk background
        ImageView badgeBackground = null;
        for (int i = 0; i < badgeContainer.getChildCount(); i++) {
            View child = badgeContainer.getChildAt(i);
            if (child instanceof ImageView) {
                badgeBackground = (ImageView) child;
                break;
            }
        }

        TextView badgeStatus = badgeContainer.findViewById(R.id.badge_status);

        if (badgeBackground == null || badgeStatus == null) {
            badgeContainer.setVisibility(View.GONE);
            return;
        }

        // LOGIKA YANG SAMA untuk trending dan upcoming:
        // Hanya tampilkan badge jika event hampir berakhir (H-7 atau kurang)
        if (event.isAlmostEnding()) {
            String badgeText = getBadgeTextForDeadline(event);
            if (badgeText != null && !badgeText.isEmpty()) {
                badgeStatus.setText(badgeText);
                badgeBackground.setImageResource(R.drawable.badge_end);
                badgeContainer.setVisibility(View.VISIBLE);

                Log.d(TAG, "✅ Menampilkan badge 'Hampir Berakhir' untuk: " +
                        event.getNameEvent() + " (H-" + event.getDaysUntilDeadline() + ")");
            } else {
                badgeContainer.setVisibility(View.GONE);
            }
        } else {
            // Jika tidak hampir berakhir, SEMBUNYIKAN badge
            badgeContainer.setVisibility(View.GONE);
        }
    }


    // Helper method untuk mendapatkan teks badge berdasarkan deadline
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

    private void openDetailEvent(Event event) {
        try {
            Intent i = new Intent(getActivity(), DetailEventActivity.class);

            // Debug: Log semua data dari event
            Log.d(TAG, "📤 Sending event data to DetailEventActivity:");
            Log.d(TAG, "  - ID: " + event.getEventId());
            Log.d(TAG, "  - Name: " + event.getNameEvent());
            Log.d(TAG, "  - Tanggal Mulai: " + event.getTanggalMulai());
            Log.d(TAG, "  - Tanggal Selesai: " + event.getTanggalSelesai());
            Log.d(TAG, "  - Waktu Pelaksanaan (RAW): " + event.getWaktu_pelaksanaan());
            Log.d(TAG, "  - Harga: " + event.getHarga());
            Log.d(TAG, "  - Deadline Pendaftaran (RAW): " + event.getDlPendaftaran());
            Log.d(TAG, "  - Kategori: " + event.getKategori());
            Log.d(TAG, "  - Deskripsi: " + event.getDescription());
            Log.d(TAG, "  - Lokasi: " + event.getLokasi());
            Log.d(TAG, "  - Poster: " + event.getPosterEvent());
            Log.d(TAG, "  - Organizer: " + event.getOrganizer());
            Log.d(TAG, "  - Kapasitas: " + event.getKapasitas());
            Log.d(TAG, "  - Status: " + event.getStatus());

            // Data yang dikirim - SEMUA FIELD
            i.putExtra("event_id", event.getEventId() != null ? event.getEventId() : "");
            i.putExtra("event_name", event.getNameEvent() != null ? event.getNameEvent() : "Event");
            i.putExtra("event_date", event.getTanggalMulai() != null ? event.getTanggalMulai() : "");
            i.putExtra("event_date_end", event.getTanggalSelesai() != null ? event.getTanggalSelesai() : ""); // Tambahkan tanggal selesai

            // PERBAIKAN: Kirim waktu pelaksanaan dengan benar
            String waktuPelaksanaan = event.getWaktu_pelaksanaan();
            if (waktuPelaksanaan != null && !waktuPelaksanaan.isEmpty() && !"null".equalsIgnoreCase(waktuPelaksanaan)) {
                i.putExtra("event_time", waktuPelaksanaan);
                Log.d(TAG, "  ✅ Waktu Pelaksanaan dikirim: " + waktuPelaksanaan);
            } else {
                // Jika waktu pelaksanaan kosong, coba extract dari tanggalMulai
                String tanggalMulai = event.getTanggalMulai();
                if (tanggalMulai != null && !tanggalMulai.isEmpty() && !"null".equalsIgnoreCase(tanggalMulai)) {
                    // Extract waktu dari format "yyyy-MM-dd HH:mm:ss"
                    String extractedTime = extractTimeFromDateTime(tanggalMulai);
                    i.putExtra("event_time", extractedTime != null ? extractedTime : "");
                    Log.d(TAG, "  ⚡ Waktu diekstrak dari tanggal: " + extractedTime);
                } else {
                    i.putExtra("event_time", "");
                }
            }

            i.putExtra("event_location", event.getLokasi() != null ? event.getLokasi() : "");
            i.putExtra("event_organizer", event.getOrganizer() != null ? event.getOrganizer() : "");
            i.putExtra("event_poster", event.getPosterEvent() != null ? event.getPosterEvent() : "");
            i.putExtra("event_description", event.getDescription() != null ? event.getDescription() : "");
            i.putExtra("event_kategori", event.getKategori() != null ? event.getKategori() : "");
            i.putExtra("event_kapasitas", event.getKapasitas()); // Tambahkan kapasitas

            // Handle harga - format yang benar
            String harga = event.getHarga();
            if (harga != null && !harga.isEmpty() && !"null".equalsIgnoreCase(harga)) {
                i.putExtra("event_harga", harga);
            } else {
                i.putExtra("event_harga", "0"); // Default gratis
            }

            // PERBAIKAN: Kirim deadline dengan benar
            String deadline = event.getDlPendaftaran();
            if (deadline != null && !deadline.isEmpty() && !"null".equalsIgnoreCase(deadline)) {
                i.putExtra("event_dl_pendaftaran", deadline);
                Log.d(TAG, "  ✅ Deadline dikirim: " + deadline);
            } else {
                i.putExtra("event_dl_pendaftaran", "");
                Log.d(TAG, "  ⚠ Deadline kosong atau null");
            }

            // Tambahkan status
            i.putExtra("event_status", event.getStatus() != null ? event.getStatus() : "");

            startActivity(i);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error opening detail event", e);
            Toast.makeText(getContext(), "Gagal membuka detail event", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractTimeFromDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }

        try {
            // Format dengan waktu: "2025-12-13 12:00:00"
            if (dateTimeString.contains(" ")) {
                String[] parts = dateTimeString.split(" ");
                if (parts.length > 1) {
                    String timePart = parts[1];
                    // Ambil HH:mm
                    if (timePart.length() >= 5) {
                        return timePart.substring(0, 5);
                    }
                }
            }
            // Format ISO: "2025-12-13T12:00:00"
            else if (dateTimeString.contains("T")) {
                String[] parts = dateTimeString.split("T");
                if (parts.length > 1) {
                    String timePart = parts[1];
                    // Hapus timezone jika ada
                    timePart = timePart.split("[+-Z]")[0];
                    // Ambil HH:mm
                    if (timePart.length() >= 5) {
                        return timePart.substring(0, 5);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting time from datetime", e);
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "EventFragment onResume");
    }
}