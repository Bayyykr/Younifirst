package com.naufal.younifirst.Kompetisi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.naufal.younifirst.R;
import com.naufal.younifirst.api.ApiHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostingLomba extends AppCompatActivity {

    private ImageView backButton, iconSearch, searchClose;
    private TextView titlePengaturan, emptyText;
    private LinearLayout searchBar, containerPostinganLomba;
    private EditText searchInput;
    private ImageButton btnPostinglomba;

    private ActivityResultLauncher<Intent> tambahPostingLauncher;
    private List<JSONObject> postinganList = new ArrayList<>();
    private boolean isLoading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_kompetisi_postingan_lomba_anda);

        // Initialize ApiHelper
        ApiHelper.initialize(getApplicationContext());

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Setup tambah posting launcher
        setupTambahPostingLauncher();

        // Load data saat pertama kali dibuka
        loadPostinganLomba();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data setiap kali kembali ke halaman ini
        if (!isLoading) {
            loadPostinganLomba();
        }
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_to_mainactivity);
        iconSearch = findViewById(R.id.iconsearch);
        titlePengaturan = findViewById(R.id.title_pengaturan);
        searchBar = findViewById(R.id.search_bar1);
        searchInput = findViewById(R.id.search_input1);
        searchClose = findViewById(R.id.search_close);
        btnPostinglomba = findViewById(R.id.btnPostingLombaAnda);
        containerPostinganLomba = findViewById(R.id.containerPostinganLomba);
        emptyText = findViewById(R.id.emptyPostinganText);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        iconSearch.setOnClickListener(v -> {
            titlePengaturan.setVisibility(View.GONE);
            iconSearch.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            searchInput.requestFocus();
        });

        searchClose.setOnClickListener(v -> {
            searchBar.setVisibility(View.GONE);
            titlePengaturan.setVisibility(View.VISIBLE);
            iconSearch.setVisibility(View.VISIBLE);
            searchInput.setText("");
            // Clear search results and show all
            loadPostinganLomba();
        });

        btnPostinglomba.setOnClickListener(v -> {
            Intent intent = new Intent(PostingLomba.this, TambahPostingLomba.class);
            tambahPostingLauncher.launch(intent);
        });
    }

    private void setupTambahPostingLauncher() {
        tambahPostingLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        boolean posted = result.getData().getBooleanExtra("posted", false);
                        if (posted) {
                            // Tampilkan toast success
                            Toast.makeText(this, "✅ Lomba berhasil dibuat!", Toast.LENGTH_SHORT).show();

                            // Refresh daftar postingan
                            loadPostinganLomba();
                        }
                    }
                }
        );
    }

    private void loadPostinganLomba() {
        String userId = ApiHelper.getSavedUserId();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            showEmptyState(true);
            return;
        }

        if (isLoading) return;

        isLoading = true;
        showLoadingState(true);

        Log.d("POSTING_LOMBA", "🔍 Loading postingan for user: " + userId);

        // Gunakan FormBody untuk mengirim user_id
        FormBody formBody = new FormBody.Builder()
                .add("user_id", userId)
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.0.104:8000/api/kompetisi/getpostingan_lomba")
                .post(formBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("POSTING_LOMBA", "❌ Network error: " + e.getMessage());

                runOnUiThread(() -> {
                    isLoading = false;
                    showLoadingState(false);
                    Toast.makeText(PostingLomba.this, "Network error", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int responseCode = response.code();
                String result = response.body() != null ? response.body().string() : "";

                Log.d("POSTING_LOMBA", "📡 Response Code: " + responseCode);
                Log.d("POSTING_LOMBA", "📡 Response: " + result.substring(0, Math.min(200, result.length())));

                runOnUiThread(() -> {
                    isLoading = false;
                    showLoadingState(false);

                    if (responseCode == 200) {
                        try {
                            JSONObject jsonResponse = new JSONObject(result);
                            boolean success = jsonResponse.optBoolean("success", false);

                            if (success) {
                                JSONArray competitions = jsonResponse.optJSONArray("competitions");

                                if (competitions != null && competitions.length() > 0) {
                                    Log.d("POSTING_LOMBA", "✅ Found " + competitions.length() + " competitions");
                                    processAndDisplayKompetisiData(competitions);
                                    showEmptyState(false);
                                } else {
                                    Log.d("POSTING_LOMBA", "📭 No competitions found");
                                    showEmptyState(true);
                                    setupEmptyStateWithAction();
                                }
                            } else {
                                String message = jsonResponse.optString("message", "Failed to load data");
                                Toast.makeText(PostingLomba.this, message, Toast.LENGTH_SHORT).show();
                                showEmptyState(true);
                            }
                        } catch (JSONException e) {
                            Log.e("POSTING_LOMBA", "❌ JSON Parse Error: " + e.getMessage());
                            Toast.makeText(PostingLomba.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            showEmptyState(true);
                        }
                    } else {
                        Log.e("POSTING_LOMBA", "❌ Server Error: " + responseCode);
                        Toast.makeText(PostingLomba.this, "Server error: " + responseCode, Toast.LENGTH_SHORT).show();
                        showEmptyState(true);
                    }
                });
            }
        });
    }

    private void loadPostinganLombaFallback(String userId) {
        Log.d("POSTING_LOMBA", "🔄 Fallback: menggunakan fetchKompetisi biasa");

        ApiHelper.fetchKompetisi(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d("POSTING_LOMBA", "✅ Fallback API Success");

                runOnUiThread(() -> {
                    isLoading = false;
                    showLoadingState(false);

                    try {
                        JSONObject response = new JSONObject(result);
                        JSONArray allCompetitions = null;

                        // Cek struktur response
                        if (response.has("data")) {
                            allCompetitions = response.getJSONArray("data");
                        } else if (response.has("competitions")) {
                            allCompetitions = response.getJSONArray("competitions");
                        } else {
                            try {
                                allCompetitions = new JSONArray(result);
                            } catch (JSONException e) {
                                Log.e("POSTING_LOMBA", "Invalid JSON format");
                            }
                        }

                        if (allCompetitions != null) {
                            // Filter berdasarkan user_id
                            JSONArray userCompetitions = new JSONArray();
                            for (int i = 0; i < allCompetitions.length(); i++) {
                                JSONObject kompetisi = allCompetitions.getJSONObject(i);

                                // Cari user_id di berbagai field
                                String kompetisiUserId = "";
                                if (kompetisi.has("user_id")) {
                                    kompetisiUserId = kompetisi.optString("user_id", "");
                                } else if (kompetisi.has("userId")) {
                                    kompetisiUserId = kompetisi.optString("userId", "");
                                } else if (kompetisi.has("created_by")) {
                                    kompetisiUserId = kompetisi.optString("created_by", "");
                                }

                                if (kompetisiUserId.equals(userId)) {
                                    userCompetitions.put(kompetisi);
                                }
                            }

                            if (userCompetitions.length() > 0) {
                                processAndDisplayKompetisiData(userCompetitions);
                                showEmptyState(false);
                            } else {
                                Log.d("POSTING_LOMBA", "📭 Tidak ada postingan kompetisi (fallback)");
                                showEmptyState(true);
                                setupEmptyStateWithAction();
                            }
                        } else {
                            Log.d("POSTING_LOMBA", "📭 Tidak ada data kompetisi");
                            showEmptyState(true);
                        }

                    } catch (JSONException e) {
                        Log.e("POSTING_LOMBA", "❌ Error parsing JSON (fallback): " + e.getMessage());
                        Toast.makeText(PostingLomba.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        showEmptyState(true);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e("POSTING_LOMBA", "❌ Fallback API Failure: " + error);

                runOnUiThread(() -> {
                    isLoading = false;
                    showLoadingState(false);

                    Toast.makeText(PostingLomba.this, "Gagal memuat data: " + error, Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                    setupEmptyStateWithAction();
                });
            }
        });
    }

    private void processAndDisplayKompetisiData(JSONArray data) throws JSONException {
        // Kosongkan list dan container
        postinganList.clear();
        containerPostinganLomba.removeAllViews();

        Log.d("POSTING_LOMBA", "🔄 Processing " + data.length() + " items");

        // Sort data by created_at (newest first)
        List<JSONObject> sortedList = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject kompetisi = data.getJSONObject(i);
            sortedList.add(kompetisi);
            Log.d("POSTING_LOMBA", "📝 Item " + i + ": " + kompetisi.optString("nama_lomba", "Unnamed"));
        }

        // Sort berdasarkan created_at (descending)
        sortedList.sort((o1, o2) -> {
            try {
                String date1 = o1.optString("created_at", o1.optString("tanggal_lomba", ""));
                String date2 = o2.optString("created_at", o2.optString("tanggal_lomba", ""));
                return date2.compareTo(date1); // Newest first
            } catch (Exception e) {
                return 0;
            }
        });

        // Add sorted items
        int addedCount = 0;
        for (JSONObject kompetisi : sortedList) {
            postinganList.add(kompetisi);
            boolean added = addPostinganItem(kompetisi);
            if (added) addedCount++;
        }

        Log.d("POSTING_LOMBA", "✅ Successfully added " + addedCount + " items to UI");
    }

    private boolean addPostinganItem(JSONObject kompetisi) {
        try {
            String namaLomba = kompetisi.optString("nama_lomba", "Nama Lomba");
            Log.d("POSTING_LOMBA", "➕ Adding item: " + namaLomba);

            // Inflate layout item
            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.fragment_item_postinganlomba, containerPostinganLomba, false);

            // Extract data from JSON
            String lokasi = kompetisi.optString("lokasi", "Online");
            String biaya = kompetisi.optString("biaya", "gratis");
            String scope = kompetisi.optString("scope", "nasional");
            String lombaType = kompetisi.optString("lomba_type", "individual");
            String tanggalLomba = kompetisi.optString("tanggal_lomba", "");
            String status = kompetisi.optString("status", "pending");
            String posterUrl = kompetisi.optString("poster_lomba", kompetisi.optString("poster", ""));
            String penyelenggara = kompetisi.optString("penyelenggara", "");
            String kategori = kompetisi.optString("kategori", "");
            String hadiah = kompetisi.optString("hadiah", "");

            Log.d("POSTING_LOMBA", "📋 Item data - " +
                    "Nama: " + namaLomba +
                    ", Status: " + status +
                    ", Tgl: " + tanggalLomba +
                    ", Lokasi: " + lokasi);

            // Find views dengan null check
            TextView tvJenis = itemView.findViewById(R.id.tvJenis);
            TextView tvNamaLomba = itemView.findViewById(R.id.tvNamaLomba);
            TextView tvTanggal = itemView.findViewById(R.id.tvTanggal);
            TextView tvLokasi = itemView.findViewById(R.id.tvLokasi);
            TextView badgeStatus = itemView.findViewById(R.id.badge_status);
            TextView tvStatusApproval = itemView.findViewById(R.id.tvStatusApproval);
            ImageView ivPoster = itemView.findViewById(R.id.ivPoster);

            if (tvJenis == null || tvNamaLomba == null || tvTanggal == null || tvLokasi == null) {
                Log.e("POSTING_LOMBA", "❌ CRITICAL: Some views are null! Check XML IDs");
                return false;
            }

            // Set data to views
            String jenisText = formatBiaya(biaya) + " - " + capitalizeFirstLetter(scope);
            tvJenis.setText(jenisText);
            tvNamaLomba.setText(namaLomba);
            tvLokasi.setText(lokasi);

            String formattedDate = formatDateRange(tanggalLomba);
            tvTanggal.setText(formattedDate);

            if (badgeStatus != null) {
                badgeStatus.setText(formatLombaType(lombaType));
            }

            // Set status approval
            if (tvStatusApproval != null) {
                String statusText = getStatusText(status);
                tvStatusApproval.setText(statusText);
                tvStatusApproval.setTextColor(getStatusColor(status));

                switch (status.toLowerCase()) {
                    case "approved":
                    case "confirm":
                        tvStatusApproval.setBackgroundResource(R.drawable.badge_green);
                        break;
                    case "rejected":
                        tvStatusApproval.setBackgroundResource(R.drawable.badge_red);
                        break;
                    default:
                        tvStatusApproval.setBackgroundResource(R.drawable.badge_purple);
                        break;
                }
            }

            // Load poster image if available
            if (ivPoster != null) {
                if (posterUrl != null && !posterUrl.isEmpty() && !posterUrl.equals("null")) {
                    String fullPosterUrl = getFullPosterUrl(posterUrl);
                    if (fullPosterUrl != null) {
                        Log.d("POSTING_LOMBA", "🖼 Loading poster: " + fullPosterUrl);
                        Glide.with(this)
                                .load(fullPosterUrl)
                                .placeholder(R.drawable.tryposter)
                                .error(R.drawable.tryposter)
                                .into(ivPoster);
                    } else {
                        ivPoster.setImageResource(R.drawable.tryposter);
                    }
                } else {
                    ivPoster.setImageResource(R.drawable.tryposter);
                }
            }

            // Set onClick listener untuk detail
            itemView.setOnClickListener(v -> {
                openDetailKompetisi(kompetisi);
            });

            // Tambahkan ke container
            containerPostinganLomba.addView(itemView);
            Log.d("POSTING_LOMBA", "✅ Item '" + namaLomba + "' added successfully");
            return true;

        } catch (Exception e) {
            Log.e("POSTING_LOMBA", "❌ Error adding item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void openDetailKompetisi(JSONObject kompetisi) {
        try {
            Intent detailIntent = new Intent(PostingLomba.this, Detail_Kompetisi.class);

            // Pass all data to Detail_Kompetisi
            detailIntent.putExtra("NAMA_LOMBA", kompetisi.optString("nama_lomba", ""));
            detailIntent.putExtra("TANGGAL_LOMBA", kompetisi.optString("tanggal_lomba", ""));
            detailIntent.putExtra("LOKASI", kompetisi.optString("lokasi", ""));
            detailIntent.putExtra("KATEGORI", kompetisi.optString("kategori", ""));
            detailIntent.putExtra("POSTER", kompetisi.optString("poster_lomba", kompetisi.optString("poster", "")));
            detailIntent.putExtra("SCOPE", kompetisi.optString("scope", ""));
            detailIntent.putExtra("DESKRIPSI", kompetisi.optString("deskripsi", ""));
            detailIntent.putExtra("HADIAH", kompetisi.optString("hadiah", ""));
            detailIntent.putExtra("LOMBA_TYPE", kompetisi.optString("lomba_type", ""));
            detailIntent.putExtra("BIAYA", kompetisi.optString("biaya", ""));
            detailIntent.putExtra("STATUS", kompetisi.optString("status", ""));
            detailIntent.putExtra("CREATED_AT", kompetisi.optString("created_at", ""));
            detailIntent.putExtra("NAMA_PENYELENGGARA", kompetisi.optString("penyelenggara", ""));
            detailIntent.putExtra("HARGA_LOMBA", kompetisi.optString("harga", kompetisi.optString("harga_lomba", "0")));
            detailIntent.putExtra("KOMPETISI_ID", kompetisi.optString("id", kompetisi.optString("lomba_id", "")));
            detailIntent.putExtra("USER_ID", kompetisi.optString("user_id", ""));

            startActivity(detailIntent);

        } catch (Exception e) {
            Log.e("POSTING_LOMBA", "❌ Error opening detail: " + e.getMessage());
            Toast.makeText(this, "Gagal membuka detail", Toast.LENGTH_SHORT).show();
        }
    }

        private String getFullPosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isEmpty() || posterPath.equals("null")) {
            Log.d("POSTER_URL", "📭 Poster path is null or empty");
            return null;
        }

        if (posterPath.startsWith("http://") || posterPath.startsWith("https://")) {
            Log.d("POSTER_URL", "🌐 Already full URL: " + posterPath);
            return posterPath;
        }

        String baseUrl = "http://192.168.0.104:8000";

        if (posterPath.startsWith("/")) {
            posterPath = posterPath.substring(1);
        }

        String fullUrl = baseUrl + "/" + posterPath;
        Log.d("POSTER_URL", "🔗 Constructed URL: " + fullUrl);

        return fullUrl;
    }

    private String formatBiaya(String biaya) {
        if (biaya == null) return "Gratis";

        switch (biaya.toLowerCase()) {
            case "berbayar":
            case "paid":
                return "Berbayar";
            default:
                return "Gratis";
        }
    }

    private String formatLombaType(String type) {
        if (type == null) return "Individu";

        switch (type.toLowerCase()) {
            case "team":
            case "tim":
                return "Tim";
            case "individual":
            case "individu":
                return "Individu";
            default:
                return type;
        }
    }

    private String formatDateRange(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Tanggal belum ditentukan";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));

            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Coba format lain
            try {
                SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));

                Date date = inputFormat2.parse(dateString);
                return outputFormat.format(date);
            } catch (ParseException e2) {
                return dateString;
            }
        }
    }

    private String getStatusText(String status) {
        if (status == null) return "Menunggu";

        switch (status.toLowerCase()) {
            case "pending":
            case "waiting":
                return "Menunggu";
            case "approved":
            case "confirm":
                return "Disetujui";
            case "rejected":
                return "Ditolak";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return getResources().getColor(R.color.orange);

        switch (status.toLowerCase()) {
            case "pending":
            case "waiting":
                return getResources().getColor(R.color.orange);
            case "approved":
            case "confirm":
                return getResources().getColor(R.color.green);
            case "rejected":
                return getResources().getColor(R.color.red);
            default:
                return getResources().getColor(R.color.white);
        }
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            containerPostinganLomba.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            Log.d("POSTING_LOMBA", "📭 Showing empty state");
        } else {
            containerPostinganLomba.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
            Log.d("POSTING_LOMBA", "📊 Showing data state");
        }
    }

    private void showLoadingState(boolean isLoading) {
        if (isLoading) {
            // Tampilkan loading indicator
            emptyText.setText("Memuat data...");
            emptyText.setVisibility(View.VISIBLE);
            containerPostinganLomba.setVisibility(View.GONE);
            Log.d("POSTING_LOMBA", "⏳ Showing loading state");
        } else {
            // Hilangkan loading
            Log.d("POSTING_LOMBA", "✅ Hiding loading state");
        }
    }

    // Method untuk tombol aksi di empty state
    private void setupEmptyStateWithAction() {
        emptyText.setText("Anda belum membagikan lomba apapun. Unggah lomba pertama Anda dan lihat aktivitasnya disini!");

        // Tambahkan button untuk membuat postingan
        Button btnCreateFirst = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.CENTER;
        btnCreateFirst.setLayoutParams(params);
        btnCreateFirst.setText("Buat Postingan Pertama");
        btnCreateFirst.setBackgroundResource(R.drawable.custom_button_rounded);
        btnCreateFirst.setTextColor(getResources().getColor(android.R.color.white));
        btnCreateFirst.setPadding(40, 20, 40, 20);
        btnCreateFirst.setOnClickListener(v -> {
            Intent intent = new Intent(PostingLomba.this, TambahPostingLomba.class);
            tambahPostingLauncher.launch(intent);
        });

        // Tambahkan ke container
        LinearLayout container = findViewById(R.id.containerPostinganLomba);
        if (container != null) {
            container.addView(btnCreateFirst);
        }
    }
}