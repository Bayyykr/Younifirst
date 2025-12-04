package com.naufal.younifirst.Event;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.naufal.younifirst.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DetailEventActivity extends AppCompatActivity {

    private ImageView imgHeader;
    private ImageButton btnZoom, btnBack, btnFlag;
    private ScrollView scrollView;
    private LinearLayout containerFilter, registerSegment;
    private RelativeLayout headerContainer;
    private boolean isZoomed = false;

    private ViewGroup originalImageParent;
    private int originalImageHeight;
    private int popupOffsetX;

    private static final String TAG = "DetailEventActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_event);

        popupOffsetX = -(int) (-10 * getResources().getDisplayMetrics().density);

        initViews();
        setupBackButton();
        setupZoomButton();
        setupBackPressedHandler();

        btnFlag.setOnClickListener(v -> showFlagPopup());
        loadEventDataFromIntent();
    }

    private void initViews() {
        imgHeader = findViewById(R.id.img_header);
        btnZoom = findViewById(R.id.zoom);
        btnBack = findViewById(R.id.back_to_mainactivity);
        btnFlag = findViewById(R.id.flag);
        scrollView = findViewById(R.id.scrollContent);
        containerFilter = findViewById(R.id.container_filter);
        headerContainer = findViewById(R.id.header_container);
        registerSegment = findViewById(R.id.register_segment);

        if (imgHeader != null && imgHeader.getParent() != null) {
            originalImageParent = (ViewGroup) imgHeader.getParent();
            originalImageHeight = imgHeader.getLayoutParams().height;
        }
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            if (isZoomed) zoomOutImage();
            else finish();
        });
    }

    private void setupZoomButton() {
        btnZoom.setOnClickListener(v -> {
            if (!isZoomed) zoomInImage();
        });
    }

    private void zoomInImage() {
        scrollView.setVisibility(View.GONE);
        containerFilter.setVisibility(View.GONE);
        registerSegment.setVisibility(View.GONE);
        btnZoom.setVisibility(View.GONE);

        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        if (imgHeader.getParent() != null) {
            ((ViewGroup) imgHeader.getParent()).removeView(imgHeader);
        }

        FrameLayout.LayoutParams fullscreenParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        imgHeader.setLayoutParams(fullscreenParams);
        imgHeader.setScaleType(ImageView.ScaleType.FIT_CENTER);

        imgHeader.setScaleX(0.8f);
        imgHeader.setScaleY(0.8f);
        imgHeader.setAlpha(0f);

        rootView.addView(imgHeader);
        btnBack.setVisibility(View.VISIBLE);
        btnFlag.setVisibility(View.VISIBLE);

        imgHeader.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .start();

        isZoomed = true;
    }

    private void zoomOutImage() {
        imgHeader.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    ViewGroup currentParent = (ViewGroup) imgHeader.getParent();
                    if (currentParent != null) {
                        currentParent.removeView(imgHeader);
                    }

                    RelativeLayout.LayoutParams originalParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            originalImageHeight
                    );
                    imgHeader.setLayoutParams(originalParams);
                    imgHeader.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    originalImageParent.addView(imgHeader);

                    imgHeader.setScaleX(1f);
                    imgHeader.setScaleY(1f);
                    imgHeader.setAlpha(1f);

                    scrollView.setVisibility(View.VISIBLE);
                    containerFilter.setVisibility(View.VISIBLE);
                    registerSegment.setVisibility(View.VISIBLE);

                    btnZoom.setVisibility(View.VISIBLE);
                    btnBack.setVisibility(View.VISIBLE);
                    btnFlag.setVisibility(View.VISIBLE);
                    headerContainer.setVisibility(View.VISIBLE);

                    isZoomed = false;
                })
                .start();
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isZoomed) zoomOutImage();
                else finish();
            }
        });
    }

    private void showFlagPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        int popupLayout = isZoomed ?
                R.layout.custom_popup_kompetisi_zoom_in :
                R.layout.custom_popup_kompetisi_zoom_out;

        View popupView = inflater.inflate(popupLayout, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);

        int[] location = new int[2];
        btnFlag.getLocationOnScreen(location);

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();

        popupWindow.showAtLocation(btnFlag, Gravity.NO_GRAVITY,
                location[0] - popupWidth + popupOffsetX,
                location[1]);

        View textEdit = popupView.findViewById(R.id.Edit);
        if (textEdit != null) {
            textEdit.setOnClickListener(v -> {
                popupWindow.dismiss();
                startActivity(new Intent(DetailEventActivity.this, EditEvent.class));
            });
        }

        View textHapus = popupView.findViewById(R.id.Hapus);
        if (textHapus != null) {
            textHapus.setOnClickListener(v -> {
                popupWindow.dismiss();
                showHapusPopup();
            });
        }

        View textBagikan = popupView.findViewById(R.id.Bagikan);
        if (textBagikan != null) {
            textBagikan.setOnClickListener(v -> {
                popupWindow.dismiss();
            });
        }
    }

    private void showHapusPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_hapus_event, null);

        final View overlay = new View(this);
        overlay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlay.setBackgroundColor(0x88000000);

        ViewGroup root = findViewById(android.R.id.content);
        root.addView(overlay);

        int width = (int) (320 * getResources().getDisplayMetrics().density);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                width,
                height,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);

        popupWindow.showAtLocation(root, Gravity.CENTER, 0, 0);

        popupView.setScaleX(0f);
        popupView.setScaleY(0f);
        popupView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();

        View btnHapusConfirm = popupView.findViewById(R.id.BtnHapus);
        btnHapusConfirm.setOnClickListener(v -> closePopupWithScale(popupWindow, popupView, overlay));

        View btnBatal = popupView.findViewById(R.id.BtnBatal);
        btnBatal.setOnClickListener(v -> closePopupWithScale(popupWindow, popupView, overlay));

        popupWindow.setOnDismissListener(() -> root.removeView(overlay));
    }

    private void closePopupWithScale(PopupWindow popupWindow, View popupView, View overlay) {
        popupView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    popupWindow.dismiss();
                    ((ViewGroup) overlay.getParent()).removeView(overlay);
                })
                .start();
    }

    private void loadEventDataFromIntent() {
        // Log semua data yang diterima
        Log.d(TAG, "📥 Received Intent Extras:");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d(TAG, "  - " + key + ": " + value);
            }
        } else {
            Log.e(TAG, "❌ No extras found in intent");
            return;
        }

        String eventId = getSafeStringExtra("event_id", null);
        String name = getSafeStringExtra("event_name", "Nama Event");
        String date = getSafeStringExtra("event_date", null);
        String dateEnd = getSafeStringExtra("event_date_end", null);
        String time = getSafeStringExtra("event_time", null);
        String place = getSafeStringExtra("event_location", "Lokasi belum ditentukan");
        String organizer = getSafeStringExtra("event_organizer", null);
        String poster = getSafeStringExtra("event_poster", null);
        String desc = getSafeStringExtra("event_description", "Deskripsi tidak tersedia");
        String kategori = getSafeStringExtra("event_kategori", null);
        String harga = getSafeStringExtra("event_harga", null);
        String dlPendaftaran = getSafeStringExtra("event_dl_pendaftaran", null);
        String status = getSafeStringExtra("event_status", null);
        int kapasitas = getIntent().getIntExtra("event_kapasitas", 0);

        // Log data yang telah diambil
        Log.d(TAG, "📋 Parsed Data from Intent:");
        Log.d(TAG, "  - ID: " + eventId);
        Log.d(TAG, "  - Name: " + name);
        Log.d(TAG, "  - Date Start: " + date);
        Log.d(TAG, "  - Date End: " + dateEnd);
        Log.d(TAG, "  - Time: " + time);
        Log.d(TAG, "  - Place: " + place);
        Log.d(TAG, "  - Organizer: " + organizer);
        Log.d(TAG, "  - Poster: " + poster);
        Log.d(TAG, "  - Kategori: " + kategori);
        Log.d(TAG, "  - Harga: " + harga);
        Log.d(TAG, "  - Deadline: " + dlPendaftaran);
        Log.d(TAG, "  - Status: " + status);
        Log.d(TAG, "  - Kapasitas: " + kapasitas);

        // Temukan view di layout
        TextView title = findViewById(R.id.title_event);
        TextView dateText = findViewById(R.id.tanggalEvent);
        TextView timeText = findViewById(R.id.jamEvent);
        TextView loc2 = findViewById(R.id.lokasiEvent);
        TextView loc = findViewById(R.id.lokasiDetailEvent);
        TextView priceText = findViewById(R.id.hargaEvent);
        TextView descText = findViewById(R.id.content_event);
        TextView batasWaktuText = findViewById(R.id.BatasWaktuPendaftaran);

        // Set data ke view dengan null safety yang lebih baik
        if (title != null) {
            title.setText(name);
        }

        if (dateText != null) {
            if (date != null && !date.trim().isEmpty()) {
                String formattedDate;

                if (dateEnd != null && !dateEnd.trim().isEmpty() && !date.equals(dateEnd)) {
                    // Jika ada tanggal selesai dan berbeda dengan tanggal mulai
                    String formattedStart = formatDateForDisplay(date, false);
                    String formattedEnd = formatDateForDisplay(dateEnd, false);
                    formattedDate = formattedStart + " - " + formattedEnd;
                } else {
                    // Hanya tanggal mulai
                    formattedDate = formatDateTimeForDisplay(date);
                }

                dateText.setText(formattedDate);
                Log.d(TAG, "📅 Formatted Date: " + formattedDate);
            } else {
                dateText.setText("Tanggal akan diumumkan");
            }
            dateText.setVisibility(View.VISIBLE);
        }

        // PERBAIKAN: Waktu pelaksanaan - Format HH:mm saja
        if (timeText != null) {
            if (time != null && !time.trim().isEmpty() && !"null".equalsIgnoreCase(time.trim())) {
                // Format waktu pelaksanaan ke HH:mm saja
                String formattedTime = formatTimeToHHMM(time);
                // Tambahkan "WIB" dan "- Selesai"
                timeText.setText(formattedTime + " WIB - Selesai");
                timeText.setVisibility(View.VISIBLE);
                Log.d(TAG, "⏰ Waktu pelaksanaan (formatted): " + formattedTime + " (raw: " + time + ")");
            } else if (date != null && !date.trim().isEmpty()) {
                // Coba extract waktu dari tanggal jika ada format waktu
                String extractedTime = extractTimeFromDate(date);
                if (extractedTime != null) {
                    timeText.setText(extractedTime + " WIB - Selesai");
                } else {
                    timeText.setText("Waktu akan diumumkan");
                }
                timeText.setVisibility(View.VISIBLE);
            } else {
                timeText.setText("Waktu akan diumumkan");
                timeText.setVisibility(View.VISIBLE);
            }
        }

        if (loc != null) {
            loc.setText(place);
        }

        // Organizer
        if (loc2 != null) {
            if (organizer != null && !organizer.trim().isEmpty()) {
                loc2.setText(organizer);
                loc2.setVisibility(View.VISIBLE);
            } else {
                loc2.setText("Organizer: Informasi belum tersedia");
                loc2.setVisibility(View.VISIBLE);
            }
        }

        // Harga
        if (priceText != null) {
            if (harga != null && !harga.trim().isEmpty() && !"null".equalsIgnoreCase(harga.trim())) {
                if (harga.equalsIgnoreCase("Gratis") ||
                        harga.equals("0") ||
                        harga.equals("0.0") ||
                        harga.equals("0.00")) {
                    priceText.setText("Gratis");
                } else {
                    try {
                        double nominal = Double.parseDouble(harga);
                        java.text.DecimalFormat formatter = new java.text.DecimalFormat("###,###,###");
                        priceText.setText("Rp. " + formatter.format(nominal));
                    } catch (NumberFormatException e) {
                        priceText.setText(harga);
                    }
                }
            } else {
                priceText.setText("Gratis");
            }
            priceText.setVisibility(View.VISIBLE);
            Log.d(TAG, "💰 Harga: " + priceText.getText());
        }

        if (descText != null) {
            descText.setText(desc);
        }

        // Batas waktu pendaftaran
        if (batasWaktuText != null) {
            if (dlPendaftaran != null && !dlPendaftaran.trim().isEmpty() && !"null".equalsIgnoreCase(dlPendaftaran.trim())) {
                String formattedDeadline = formatDateTimeForDisplay(dlPendaftaran);
                batasWaktuText.setText(formattedDeadline);
                batasWaktuText.setVisibility(View.VISIBLE);
            } else {
                batasWaktuText.setText("Batas Pendaftaran: Tidak ditentukan");
                batasWaktuText.setVisibility(View.VISIBLE);
            }
            Log.d(TAG, "⏳ Batas waktu: " + batasWaktuText.getText());
        }

        // PERBAIKAN: Setup badge-badge (kategori + deadline jika perlu)
        setupBadges(kategori, name, dlPendaftaran);

        // Load gambar poster
        if (poster != null && !poster.trim().isEmpty() && !"null".equalsIgnoreCase(poster.trim())) {
            String fullPosterUrl = ensureFullUrl(poster);
            Log.d(TAG, "🖼 Loading poster from: " + fullPosterUrl);

            Glide.with(this)
                    .load(fullPosterUrl)
                    .placeholder(R.drawable.tryposter)
                    .error(R.drawable.tryposter)
                    .into(imgHeader);
        } else {
            imgHeader.setImageResource(R.drawable.tryposter);
            Log.d(TAG, "🖼 Using placeholder poster");
        }
    }

    // ==================== PERBAIKAN: SETUP MULTIPLE BADGES ====================
    private void setupBadges(String kategori, String eventName, String dlPendaftaran) {
        LinearLayout containerFilter = findViewById(R.id.container_filter);
        if (containerFilter == null) return;

        // Hapus badge yang sudah ada (jika ada)
        View existingBadgeEvent = findViewById(R.id.badge_event);
        if (existingBadgeEvent != null && existingBadgeEvent.getParent() != null) {
            ((ViewGroup) existingBadgeEvent.getParent()).removeView(existingBadgeEvent);
        }

        // Cari posisi untuk memasukkan badges (sebelum title)
        int titleIndex = -1;
        for (int i = 0; i < containerFilter.getChildCount(); i++) {
            if (containerFilter.getChildAt(i).getId() == R.id.title_event) {
                titleIndex = i;
                break;
            }
        }

        if (titleIndex == -1) {
            titleIndex = 0; // Fallback ke index 0
        }

        // Buat container horizontal untuk badges
        LinearLayout badgesContainer = new LinearLayout(this);
        badgesContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 0, 0, 10);
        badgesContainer.setLayoutParams(containerParams);

        // Masukkan container badges sebelum title
        containerFilter.addView(badgesContainer, titleIndex);

        // ===== BADGE KATEGORI =====
        // Split kategori jika ada koma
        List<String> kategoriList = parseKategoriList(kategori);

        // Tampilkan maksimal 3 badge kategori
        int maxKategoriBadges = Math.min(3, kategoriList.size());
        for (int i = 0; i < maxKategoriBadges; i++) {
            String kat = kategoriList.get(i);
            if (kat != null && !kat.trim().isEmpty() && !"null".equalsIgnoreCase(kat.trim())) {
                View kategoriBadge = createKategoriBadge(kat.trim());
                if (kategoriBadge != null) {
                    // Tambahkan margin kanan kecuali untuk badge terakhir
                    LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    if (i < maxKategoriBadges - 1) {
                        badgeParams.setMargins(0, 0, 8, 0);
                    }
                    kategoriBadge.setLayoutParams(badgeParams);

                    badgesContainer.addView(kategoriBadge);
                }
            }
        }

        // ===== BADGE DEADLINE =====
        // Cek apakah perlu badge deadline
        if (dlPendaftaran != null && !dlPendaftaran.trim().isEmpty() && !"null".equalsIgnoreCase(dlPendaftaran.trim())) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date deadline = format.parse(dlPendaftaran);
                Date now = new Date();

                long diffInMillis = deadline.getTime() - now.getTime();
                long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                // Jika H-7 atau kurang, tambahkan badge deadline
                if (diffInDays >= 0 && diffInDays <= 7) {
                    View deadlineBadge = createDeadlineBadge(diffInDays);
                    if (deadlineBadge != null) {
                        // Tambahkan margin kiri untuk memisahkan dari badge kategori
                        LinearLayout.LayoutParams deadlineParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        deadlineParams.setMargins(8, 0, 0, 0);
                        deadlineBadge.setLayoutParams(deadlineParams);

                        badgesContainer.addView(deadlineBadge);

                        Log.d(TAG, "⏳ Badge deadline ditambahkan: H-" + diffInDays);
                    }
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing deadline for badge", e);
            }
        }

        Log.d(TAG, "🏷 Total badges ditambahkan: " + badgesContainer.getChildCount());
    }

    // Method untuk parse kategori dari string dengan koma
    private List<String> parseKategoriList(String kategori) {
        List<String> result = new ArrayList<>();

        if (kategori == null || kategori.trim().isEmpty() || "null".equalsIgnoreCase(kategori.trim())) {
            // Jika kategori kosong, tambahkan badge default
            result.add("EVENT");
            return result;
        }

        // Split berdasarkan koma
        String[] parts = kategori.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty() && !"null".equalsIgnoreCase(trimmed)) {
                result.add(trimmed);
            }
        }

        // Jika setelah split masih kosong, tambahkan badge default
        if (result.isEmpty()) {
            result.add("EVENT");
        }

        return result;
    }

    // Method untuk membuat badge kategori
    private View createKategoriBadge(String kategori) {
        try {
            // Inflate layout badge_kecil
            View badgeView = getLayoutInflater().inflate(R.layout.badge_kecil, null, false);
            TextView badgeText = badgeView.findViewById(R.id.text_badge_kecil);

            if (badgeText == null) {
                return null;
            }

            // Set teks badge (singkatkan jika terlalu panjang)
            String badgeTextStr = kategori;
            if (kategori.length() > 12) {
                badgeTextStr = kategori.substring(0, 12) + "..";
            }
            badgeText.setText(badgeTextStr.toUpperCase());

            // Set warna berdasarkan kategori
            String kategoriLower = kategori.toLowerCase();
            int bgResource = R.drawable.badge_green; // Default

            if (kategoriLower.contains("seminar") || kategoriLower.contains("konser")) {
                bgResource = R.drawable.badge_red;
            } else if (kategoriLower.contains("music") || kategoriLower.contains("workshop") ||
                    kategoriLower.contains("festival")) {
                bgResource = R.drawable.badge_blue;
            } else if (kategoriLower.contains("kompetisi") || kategoriLower.contains("lomba") ||
                    kategoriLower.contains("competition")) {
                bgResource = R.drawable.badge_green;
            } else if (kategoriLower.contains("sport") || kategoriLower.contains("olahraga") ||
                    kategoriLower.contains("olah raga")) {
                bgResource = R.drawable.badge_purple;
            }

            badgeView.setBackgroundResource(bgResource);
            badgeText.setTextColor(Color.WHITE);

            return badgeView;
        } catch (Exception e) {
            Log.e(TAG, "Error creating kategori badge: " + e.getMessage());
            return null;
        }
    }

    // Method untuk membuat badge deadline
    private View createDeadlineBadge(long daysUntilDeadline) {
        try {
            // Inflate layout badge_kecil
            View badgeView = getLayoutInflater().inflate(R.layout.badge_kecil, null, false);
            TextView badgeText = badgeView.findViewById(R.id.text_badge_kecil);

            if (badgeText == null) {
                return null;
            }

            // Set teks berdasarkan hari
            String badgeTextStr;
            if (daysUntilDeadline == 0) {
                badgeTextStr = "HARI INI\nBERAKHIR";
            } else if (daysUntilDeadline == 1) {
                badgeTextStr = "BESOK\nBERAKHIR";
            } else {
                badgeTextStr = "HAMIIR\nBERAKHIR";
            }

            badgeText.setText(badgeTextStr);
            badgeText.setLineSpacing(0, 0.8f);
            badgeText.setGravity(Gravity.CENTER);

            // Gunakan background merah untuk deadline
            badgeView.setBackgroundResource(R.drawable.badge_red);
            badgeText.setTextColor(Color.WHITE);

            return badgeView;
        } catch (Exception e) {
            Log.e(TAG, "Error creating deadline badge: " + e.getMessage());
            return null;
        }
    }

    // ==================== HELPER METHODS ====================
    private String formatTimeToHHMM(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return "Waktu akan diumumkan";
        }

        try {
            timeString = timeString.trim();

            // Jika sudah format HH:mm:ss -> ambil HH:mm
            if (timeString.matches("\\d{2}:\\d{2}:\\d{2}")) {
                return timeString.substring(0, 5);
            }
            // Jika format HH:mm
            else if (timeString.matches("\\d{2}:\\d{2}")) {
                return timeString;
            }
            // Jika format lain, coba parse dengan SimpleDateFormat
            else {
                SimpleDateFormat[] possibleFormats = {
                        new SimpleDateFormat("HH:mm:ss", Locale.getDefault()),
                        new SimpleDateFormat("HH:mm", Locale.getDefault()),
                        new SimpleDateFormat("hh:mm a", Locale.US),
                        new SimpleDateFormat("hh:mm:ss a", Locale.US)
                };

                for (SimpleDateFormat format : possibleFormats) {
                    try {
                        Date timeDate = format.parse(timeString);
                        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        return outputFormat.format(timeDate);
                    } catch (ParseException e) {
                        // Continue to next format
                    }
                }

                // Jika tidak bisa di-parse, return as-is
                return timeString;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting time: " + timeString, e);
            return timeString;
        }
    }

    private String extractTimeFromDate(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }

        try {
            // Coba format ISO dengan timezone
            if (dateTimeString.contains("T")) {
                String[] parts = dateTimeString.split("T");
                if (parts.length > 1) {
                    String timePart = parts[1];
                    timePart = timePart.split("[+-Z]")[0];
                    if (timePart.length() >= 8) {
                        return timePart.substring(0, 5);
                    } else if (timePart.length() >= 5) {
                        return timePart.substring(0, 5);
                    }
                }
            } else if (dateTimeString.contains(" ")) {
                String[] parts = dateTimeString.split(" ");
                if (parts.length > 1) {
                    String timePart = parts[1];
                    if (timePart.length() >= 8) {
                        return timePart.substring(0, 5);
                    } else if (timePart.length() >= 5) {
                        return timePart.substring(0, 5);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting time from date: " + dateTimeString, e);
        }
        return null;
    }

    private String getSafeStringExtra(String key, String defaultValue) {
        if (getIntent().hasExtra(key)) {
            String value = getIntent().getStringExtra(key);
            if (value != null && !value.trim().isEmpty() && !"null".equalsIgnoreCase(value.trim())) {
                return value.trim();
            }
        }
        return defaultValue;
    }

    private String formatDateTimeForDisplay(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty() || "null".equalsIgnoreCase(dateTimeString.trim())) {
            return "Tanggal belum ditentukan";
        }

        try {
            SimpleDateFormat inputFormat;
            SimpleDateFormat outputFormat;

            if (dateTimeString.contains("T")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy | HH:mm", new Locale("id", "ID"));
            } else if (dateTimeString.contains(" ")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy | HH:mm", new Locale("id", "ID"));
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
            }

            Date date = inputFormat.parse(dateTimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dateTimeString, e);
            return dateTimeString;
        }
    }

    private String formatDateForDisplay(String dateTimeString, boolean includeTime) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty() || "null".equalsIgnoreCase(dateTimeString.trim())) {
            return "Tanggal belum ditentukan";
        }

        try {
            SimpleDateFormat inputFormat;
            SimpleDateFormat outputFormat;

            if (includeTime) {
                if (dateTimeString.contains("T")) {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy | HH:mm", new Locale("id", "ID"));
                } else if (dateTimeString.contains(" ")) {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy | HH:mm", new Locale("id", "ID"));
                } else {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
                }
            } else {
                if (dateTimeString.contains("T")) {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                } else if (dateTimeString.contains(" ")) {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                } else {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                }
                outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
            }

            Date date = inputFormat.parse(dateTimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dateTimeString, e);
            return dateTimeString;
        }
    }

    private String ensureFullUrl(String posterPath) {
        if (posterPath == null || posterPath.trim().isEmpty()) {
            return null;
        }

        String path = posterPath.trim();

        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String BASE_URL = "http://192.168.1.21:8000";
        if (BASE_URL.endsWith("/")) {
            BASE_URL = BASE_URL.substring(0, BASE_URL.length() - 1);
        }

        return BASE_URL + "/" + path;
    }
}