package com.naufal.younifirst.Event;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
import java.util.Date;
import java.util.Locale;

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

        originalImageParent = (ViewGroup) imgHeader.getParent();
        originalImageHeight = imgHeader.getLayoutParams().height;
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
        String name = getIntent().getStringExtra("event_name");
        String date = getIntent().getStringExtra("event_date");
        String time = getIntent().getStringExtra("event_time");
        String place = getIntent().getStringExtra("event_location"); // organizer
        String place2 = getIntent().getStringExtra("event_detail_place"); // lokasi
        String price = getIntent().getStringExtra("event_price");
        String desc = getIntent().getStringExtra("event_description");
        String poster = getIntent().getStringExtra("event_poster");
        String dlPendaftaran = getIntent().getStringExtra("event_dl_pendaftaran");

        String kategori = getIntent().getStringExtra("event_kategori");

        ImageView img = findViewById(R.id.img_header);
        TextView title = findViewById(R.id.title_event);
        TextView dateText = findViewById(R.id.tanggalEvent);
        TextView timeText = findViewById(R.id.jamEvent);
        TextView loc = findViewById(R.id.lokasiEvent);
        TextView loc2 = findViewById(R.id.lokasiDetailEvent);
        TextView priceText = findViewById(R.id.hargaEvent);
        TextView descText = findViewById(R.id.content_event);
        TextView batasWaktuText = findViewById(R.id.BatasWaktuPendaftaran);

        View badgeView = findViewById(R.id.badge_event);

        title.setText(name != null ? name : "Nama Event");

        if (date != null && !date.isEmpty()) {
            dateText.setText(formatDate(date));
        } else {
            dateText.setText("Tanggal belum ditentukan");
        }

        if (time != null && !time.isEmpty()) {
            timeText.setText(time);
        } else {
            timeText.setText("Waktu akan diumumkan");
        }

        loc.setText(place != null ? place : "Organizer tidak tersedia");
        loc2.setText(place2 != null ? place2 : "Lokasi tidak tersedia");

        priceText.setText(price != null ? price : "Gratis");

        descText.setText(desc != null ? desc : "Deskripsi tidak tersedia");

        if (batasWaktuText != null) {
            if (dlPendaftaran != null && !dlPendaftaran.isEmpty() && !"null".equals(dlPendaftaran)) {
                batasWaktuText.setText(dlPendaftaran);
            } else {
            }
        }

        setBadgeWithCategory(badgeView, kategori);

        Glide.with(this)
                .load(poster)
                .placeholder(R.drawable.tryposter)
                .into(imgHeader);
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    private String formatDateTimeForDisplay(String dateTimeString) {
        try {
            SimpleDateFormat inputFormat;

            // Cek format input
            if (dateTimeString.contains(" ")) {
                // Format dengan waktu: "2025-12-13 12:00:00"
                inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            } else {
                // Format tanpa waktu: "2025-12-13"
                inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }

            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy | HH:mm", new Locale("id", "ID"));
            Date date = inputFormat.parse(dateTimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Jika parsing gagal, return original string
            return dateTimeString;
        }
    }

    // PERBAIKAN: Method baru untuk set badge berdasarkan kategori
    private void setBadgeWithCategory(View badgeView, String kategori) {
        if (badgeView == null) return;

        TextView badgeTextView = badgeView.findViewById(R.id.badge_status);
        if (badgeTextView != null) {
            // PERBAIKAN: Generate warna berdasarkan kategori (jika ada) atau nama event
            String baseString = (kategori != null && !kategori.isEmpty()) ? kategori : "DefaultEvent";
            int color = generateColorFromString(baseString);

            // Set warna background badge
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(20); // radius sesuai dengan XML
            badgeTextView.setBackground(null);

            // Set teks badge
            if (kategori != null && !kategori.isEmpty()) {
                // Ambil kata pertama jika kategori berupa multiple (contoh: "Webinar, Seminar")
                String[] kategoriParts = kategori.split(",");
                String firstKategori = kategoriParts[0].trim().toUpperCase();
                badgeTextView.setText(firstKategori);
            } else {
                badgeTextView.setText("EVENT");
            }
        }
    }

    private int generateColorFromString(String input) {
        int[] badgeColors = {
                Color.parseColor("#FF6B6B"),  // Merah muda
                Color.parseColor("#4ECDC4"),  // Turquoise
                Color.parseColor("#FFD166"),  // Kuning
                Color.parseColor("#06D6A0"),  // Hijau mint
                Color.parseColor("#118AB2"),  // Biru
                Color.parseColor("#EF476F"),  // Pink
                Color.parseColor("#7209B7"),  // Ungu
                Color.parseColor("#3A86FF"),  // Biru cerah
                Color.parseColor("#FB5607"),  // Orange
                Color.parseColor("#8338EC")   // Ungu tua
        };

        // Hash string untuk mendapatkan index yang konsisten
        int hash = Math.abs(input.hashCode());
        int index = hash % badgeColors.length;

        return badgeColors[index];
    }
}