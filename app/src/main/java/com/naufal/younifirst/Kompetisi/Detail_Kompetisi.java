package com.naufal.younifirst.Kompetisi;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.naufal.younifirst.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Detail_Kompetisi extends AppCompatActivity {

    // Deklarasi view
    private ImageView imgPoster;
    private TextView titleEventCard, biayaText, scopeText, lombaTypeText, titleEvent,
            tanggalText, lokasiText, hargaText, hadiahText, deskripsiText,
            usernameText, createdAtText;
    private ImageButton backButton, flagButton;
    private LinearLayout badgeContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_postingan_lomba);

        // Initialize views
        initializeViews();

        // Handle back button
        backButton.setOnClickListener(v -> finish());

        // Handle flag/options button
        flagButton.setOnClickListener(v -> showOptionsMenu());

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            loadKompetisiData(intent);
        } else {
            Toast.makeText(this, "Data kompetisi tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        // Back and options buttons
        backButton = findViewById(R.id.back_to_mainactivity);
        flagButton = findViewById(R.id.flag);

        // Main content views
        imgPoster = findViewById(R.id.img_poster);
        titleEventCard = findViewById(R.id.title_event_card);

        // Header section
        biayaText = findViewById(R.id.Biaya);
        scopeText = findViewById(R.id.Scope);
        lombaTypeText = findViewById(R.id.lombaType);

        // Title
        titleEvent = findViewById(R.id.title_event);

        // Badge container - PERBAIKAN INI
        try {
            // Cara 1: Cari LinearLayout yang merupakan parent dari iniscope
            View iniscopeView = findViewById(R.id.iniscope);
            if (iniscopeView != null) {
                // Get parent view
                View parent = (View) iniscopeView.getParent();
                if (parent != null && parent instanceof LinearLayout) {
                    badgeContainer = (LinearLayout) parent;
                    Log.d("INIT_VIEWS", "Found badge container as parent of iniscope");
                } else {
                    // Jika parent bukan LinearLayout, cari container badges langsung
                    badgeContainer = findViewById(R.id.iniscope);
                    if (badgeContainer == null) {
                        LinearLayout containerFilter = findViewById(R.id.container_filter);
                        if (containerFilter != null) {
                            for (int i = 0; i < containerFilter.getChildCount(); i++) {
                                View child = containerFilter.getChildAt(i);
                                if (child instanceof LinearLayout) {
                                    LinearLayout linearChild = (LinearLayout) child;
                                    if (linearChild.getChildCount() > 0) {
                                        View firstChild = linearChild.getChildAt(0);
                                        if (firstChild != null && firstChild.getId() == R.id.iniscope) {
                                            badgeContainer = linearChild;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (badgeContainer == null) {
                Log.w("INIT_VIEWS", "Badge container not found, creating new one");
                badgeContainer = new LinearLayout(this);
            }

        } catch (Exception e) {
            Log.e("INIT_VIEWS", "Error finding badge container", e);
            badgeContainer = new LinearLayout(this);
        }

        // Detail sections
        tanggalText = findViewById(R.id.initanggallomba);
        lokasiText = findViewById(R.id.ini);
        hargaText = findViewById(R.id.iniHargaLomba);
        hadiahText = findViewById(R.id.iniHadiah);
        deskripsiText = findViewById(R.id.content_event);
    }

    private void loadKompetisiData(Intent intent) {
        try {
            String namaLomba = intent.getStringExtra("NAMA_LOMBA");
            String tanggalLomba = intent.getStringExtra("TANGGAL_LOMBA");
            String lokasi = intent.getStringExtra("LOKASI");
            String kategori = intent.getStringExtra("KATEGORI");
            String poster = intent.getStringExtra("POSTER");
            String scope = intent.getStringExtra("SCOPE");
            String deskripsi = intent.getStringExtra("DESKRIPSI");
            String hadiah = intent.getStringExtra("HADIAH");
            String lombaType = intent.getStringExtra("LOMBA_TYPE");
            String biaya = intent.getStringExtra("BIAYA");
            String status = intent.getStringExtra("STATUS");
            String createdAt = intent.getStringExtra("CREATED_AT");
            String penyelenggara = intent.getStringExtra("NAMA_PENYELENGGARA");
            String hargaLomba = intent.getStringExtra("HARGA_LOMBA");

            // Nama lomba
            titleEvent.setText(namaLomba != null ? namaLomba : "-");

            // Biaya
            if (biayaText != null) {
                if (biaya != null && (biaya.equalsIgnoreCase("gratis") || biaya.equals("0") || biaya.equalsIgnoreCase("free"))) {
                    biayaText.setText("Gratis");
                } else {
                    biayaText.setText("Berbayar");
                }
            }

            // Scope
            if (scopeText != null) {
                scopeText.setText(scope != null ? scope : "Nasional");
            }

            // Tipe lomba
            if (lombaTypeText != null) {
                if ("individual".equalsIgnoreCase(lombaType)) {
                    lombaTypeText.setText("Individu");
                } else if ("team".equalsIgnoreCase(lombaType) || "kelompok".equalsIgnoreCase(lombaType)) {
                    lombaTypeText.setText("Kelompok");
                } else {
                    lombaTypeText.setText(lombaType != null ? lombaType : "-");
                }
            }

            // Tanggal
            if (tanggalText != null && tanggalLomba != null) {
                tanggalText.setText(formatTanggal(tanggalLomba));
            }

            // Lokasi
            if (lokasiText != null) {
                lokasiText.setText(lokasi != null ? lokasi : "-");
            }

            if (hargaText != null) {
                if (hargaLomba != null && !hargaLomba.trim().isEmpty() && !"null".equalsIgnoreCase(hargaLomba.trim())) {
                    String hargaDisplay = formatHargaFromDatabase(hargaLomba);
                    hargaText.setText(hargaDisplay);
                    Log.d("HARGA_DEBUG", "Harga ditampilkan (dari DB): " + hargaDisplay);
                } else if (biaya != null && !biaya.trim().isEmpty()) {
                    String hargaDisplay = formatHargaFromDatabase(biaya);
                    hargaText.setText(hargaDisplay);
                    Log.d("HARGA_DEBUG", "Harga ditampilkan (dari biaya): " + hargaDisplay);
                } else {
                    hargaText.setText("Gratis");
                    Log.d("HARGA_DEBUG", "Harga ditampilkan (default): Gratis");
                }
                hargaText.setVisibility(View.VISIBLE);
            }

            // Hadiah
            if (hadiahText != null) {
                hadiahText.setText(hadiah != null ? hadiah : "-");
            }

            // Deskripsi
            if (deskripsiText != null) {
                deskripsiText.setText(deskripsi != null ? deskripsi : "-");
            }

            // Nama penyelenggara
            if (titleEventCard != null) {
                titleEventCard.setText(penyelenggara != null ? penyelenggara : "-");
            }

            // Created at
            if (createdAtText != null) {
                createdAtText.setText(createdAt != null ? formatTanggal(createdAt) : "-");
            }

            // Poster
            if (imgPoster != null) {
                if (poster != null && !poster.isEmpty()) {
                    Glide.with(this)
                            .load(getFullPosterUrl(poster))
                            .placeholder(R.drawable.tryposter)
                            .error(R.drawable.tryposter)
                            .into(imgPoster);
                } else {
                    imgPoster.setImageResource(R.drawable.tryposter);
                }
            }

            // Kategori badges
            if (kategori != null && !kategori.isEmpty() && badgeContainer != null) {
                setupBadges(kategori);
            }

        } catch (Exception e) {
            Log.e("DETAIL_KOMPETISI", "Error loading kompetisi data", e);
            Toast.makeText(this, "Gagal memuat data kompetisi", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ TAMBAHKAN METHOD BARU UNTUK FORMAT HARGA DARI DATABASE
    private String formatHargaFromDatabase(String hargaData) {
        if (hargaData == null || hargaData.trim().isEmpty() || "null".equalsIgnoreCase(hargaData.trim())) {
            return "Gratis";
        }

        try {
            // Cek apakah "gratis" (case insensitive)
            if (hargaData.equalsIgnoreCase("gratis") ||
                    hargaData.equalsIgnoreCase("free") ||
                    hargaData.equalsIgnoreCase("0") ||
                    hargaData.equalsIgnoreCase("0.0") ||
                    hargaData.equalsIgnoreCase("0.00")) {
                return "Gratis";
            }

            // Coba parse sebagai angka
            String cleanHarga = hargaData.replace(".", "").replace(",", "").trim();

            try {
                int hargaInt = Integer.parseInt(cleanHarga);
                if (hargaInt == 0) {
                    return "Gratis";
                }
                // Format dengan titik pemisah ribuan
                java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
                return "Rp " + formatter.format(hargaInt);
            } catch (NumberFormatException e1) {
                // Coba parse sebagai double
                try {
                    double hargaDouble = Double.parseDouble(cleanHarga);
                    if (hargaDouble == 0) {
                        return "Gratis";
                    }
                    java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
                    return "Rp " + formatter.format(hargaDouble);
                } catch (NumberFormatException e2) {
                    // Jika bukan angka, tampilkan apa adanya
                    return hargaData;
                }
            }
        } catch (Exception e) {
            Log.e("FORMAT_HARGA", "Error formatting harga: " + hargaData, e);
            return hargaData;
        }
    }

    // ✅ PERBAIKAN: Method formatRupiah yang lebih baik
    private String formatRupiah(int amount) {
        try {
            if (amount == 0) {
                return "Gratis";
            }

            // Format dengan titik pemisah ribuan
            java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
            return "Rp " + formatter.format(amount);
        } catch (Exception e) {
            Log.e("FORMAT_RUPIAH", "Error formatting rupiah: " + amount, e);
            return "Rp " + amount;
        }
    }


    private String formatTanggal(String tanggal) {
        try {
            SimpleDateFormat[] formats = {
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                    new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            };

            Date date = null;
            for (SimpleDateFormat format : formats) {
                try {
                    date = format.parse(tanggal);
                    if (date != null) break;
                } catch (ParseException e) {
                }
            }

            if (date != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            Log.e("FORMAT_TANGGAL", "Error formatting date: " + tanggal, e);
        }

        return tanggal;
    }


    private String getFullPosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }

        if (posterPath.startsWith("http://") || posterPath.startsWith("https://")) {
            return posterPath;
        }

        String baseUrl = "http://10.10.182.83:8000";

        if (posterPath.startsWith("/")) {
            posterPath = posterPath.substring(1);
        }

        String fullUrl = baseUrl + "/" + posterPath;
        Log.d("POSTER_URL", "Full URL: " + fullUrl);

        return fullUrl;
    }

    private void setupBadges(String kategori) {
        try {
            if (kategori == null || kategori.isEmpty() || badgeContainer == null) {
                return;
            }

            badgeContainer.removeAllViews();

            String[] kategoriArray = kategori.split(",");

            for (String kat : kategoriArray) {
                String trimmedKat = kat.trim();
                if (!trimmedKat.isEmpty()) {
                    LinearLayout newBadge = new LinearLayout(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    int margin = dp(5);
                    params.setMargins(0, dp(10), margin, 0);
                    newBadge.setLayoutParams(params);
                    newBadge.setOrientation(LinearLayout.HORIZONTAL);
                    newBadge.setBackgroundResource(R.drawable.badge_blue);
                    newBadge.setPadding(dp(10), dp(5), dp(10), dp(5));

                    TextView badgeText = new TextView(this);
                    badgeText.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    badgeText.setText(trimmedKat);
                    badgeText.setTextColor(getResources().getColor(android.R.color.white));
                    badgeText.setTextSize(8);
                    badgeText.setTypeface(getResources().getFont(R.font.is_r));

                    newBadge.addView(badgeText);
                    badgeContainer.addView(newBadge);
                }
            }

        } catch (Exception e) {
            Log.e("SETUP_BADGES", "Error setting up badges", e);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void showOptionsMenu() {
        Toast.makeText(this, "Menu options", Toast.LENGTH_SHORT).show();
    }
}