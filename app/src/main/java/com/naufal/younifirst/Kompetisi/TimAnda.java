package com.naufal.younifirst.Kompetisi;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.naufal.younifirst.R;
import com.naufal.younifirst.api.ApiHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class TimAnda extends AppCompatActivity {

    private ImageView backButton, iconSearch, searchClose;
    private TextView titlePengaturan, emptyPostinganText;
    private LinearLayout searchBar, containerPostinganLomba;
    private EditText searchInput;
    private ImageButton btnBuatTim;

    private ActivityResultLauncher<Intent> tambahTimLauncher;

    private boolean isSearchMode = false;
    private List<JSONObject> allTimList = new ArrayList<>(); // Semua data tim
    private List<JSONObject> filteredTimList = new ArrayList<>(); // Data setelah filter
    private boolean isLoading = false;
    private String currentUserId = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_rekrut_tim);

        // Initialize ApiHelper
        ApiHelper.initialize(getApplicationContext());

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Setup tambah tim launcher
        setupTambahTimLauncher();

        // Setup search functionality
        setupSearch();

        // Load data saat pertama kali dibuka
        loadTimData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data setiap kali kembali ke halaman ini
        if (!isLoading) {
            loadTimData();
        }
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_to_mainactivity);
        iconSearch = findViewById(R.id.iconsearch);
        titlePengaturan = findViewById(R.id.title_pengaturan);
        searchBar = findViewById(R.id.search_bar1);
        searchInput = findViewById(R.id.search_input1);
        searchClose = findViewById(R.id.search_close);
        btnBuatTim = findViewById(R.id.btnBuatTim);
        containerPostinganLomba = findViewById(R.id.containerPostinganLomba);
        emptyPostinganText = findViewById(R.id.emptyPostinganText);

        // Hapus include default karena kita akan membuat item dinamis
        containerPostinganLomba.removeAllViews();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        iconSearch.setOnClickListener(v -> {
            titlePengaturan.setVisibility(View.GONE);
            iconSearch.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            searchInput.requestFocus();

            // Tambahkan ini: Auto refresh saat masuk mode search
            isSearchMode = true;
            if (!isLoading) {
                loadTimData(); // Refresh data saat masuk mode search
            }
        });

        searchClose.setOnClickListener(v -> {
            searchBar.setVisibility(View.GONE);
            titlePengaturan.setVisibility(View.VISIBLE);
            iconSearch.setVisibility(View.VISIBLE);
            searchInput.setText("");

            // Reset search mode
            isSearchMode = false;

            // Reset ke semua data tanpa filter
            filteredTimList.clear();
            filteredTimList.addAll(allTimList);
            displayTeams(filteredTimList);
        });

        btnBuatTim.setOnClickListener(v -> {
            Intent intent = new Intent(TimAnda.this, BuatTim.class);
            tambahTimLauncher.launch(intent);
        });
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Real-time search saat user mengetik
                filterTeams(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Tambahkan action search di keyboard
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                filterTeams(searchInput.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void filterTeams(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // Jika search kosong, tampilkan semua tim user
            filteredTimList.clear();
            filteredTimList.addAll(allTimList);
        } else {
            // Filter berdasarkan nama tim
            filteredTimList.clear();
            String query = searchQuery.toLowerCase().trim();

            for (JSONObject team : allTimList) {
                try {
                    String namaTeam = team.optString("nama_team", "").toLowerCase();
                    if (namaTeam.contains(query)) {
                        filteredTimList.add(team);
                    }
                } catch (Exception e) {
                    Log.e("TIM_ANDA", "Error filtering team: " + e.getMessage());
                }
            }
        }

        // Tampilkan hasil filter (sudah terurut)
        displayTeams(filteredTimList);
    }

    private void setupTambahTimLauncher() {
        tambahTimLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        boolean posted = result.getData().getBooleanExtra("posted", false);
                        if (posted) {
                            // Tampilkan toast success
                            Toast.makeText(this, "✅ Tim berhasil dibuat!", Toast.LENGTH_SHORT).show();

                            // Refresh daftar tim
                            loadTimData();
                        }
                    }
                }
        );
    }

    private void loadTimData() {
        currentUserId = ApiHelper.getSavedUserId();

        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            showEmptyState(true);
            return;
        }

        if (isLoading) return;

        isLoading = true;

        // Tampilkan loading state yang berbeda berdasarkan mode
        if (isSearchMode) {
            showSearchLoadingState(true);
        } else {
            showLoadingState(true);
        }

        Log.d("TIM_ANDA", "🔍 Loading tim for user: " + currentUserId +
                ", Search Mode: " + isSearchMode);

        ApiHelper.fetchTeams(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d("TIM_ANDA", "✅ API Success - Result length: " + result.length());

                runOnUiThread(() -> {
                    isLoading = false;

                    // Sembunyikan loading state berdasarkan mode
                    if (isSearchMode) {
                        showSearchLoadingState(false);
                    } else {
                        showLoadingState(false);
                    }

                    try {
                        processTeamData(result);
                    } catch (Exception e) {
                        Log.e("TIM_ANDA", "❌ Error processing team data: " + e.getMessage());
                        Toast.makeText(TimAnda.this, "Error processing data", Toast.LENGTH_SHORT).show();
                        showEmptyState(true);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e("TIM_ANDA", "❌ API Failure: " + error);

                runOnUiThread(() -> {
                    isLoading = false;

                    // Sembunyikan loading state berdasarkan mode
                    if (isSearchMode) {
                        showSearchLoadingState(false);
                    } else {
                        showLoadingState(false);
                    }

                    Toast.makeText(TimAnda.this, "Gagal memuat data: " + error, Toast.LENGTH_SHORT).show();
                    showEmptyState(true);

                    if (!isSearchMode) {
                        setupEmptyStateWithAction();
                    }
                });
            }
        });
    }

    private void showSearchLoadingState(boolean isLoading) {
        if (isLoading) {
            // Tampilkan loading indicator khusus untuk search
            emptyPostinganText.setText("Mencari tim...");
            emptyPostinganText.setVisibility(View.VISIBLE);
            containerPostinganLomba.setVisibility(View.GONE);
            Log.d("TIM_ANDA", "🔍 Showing search loading state");
        } else {
            // Hilangkan loading
            Log.d("TIM_ANDA", "✅ Hiding search loading state");
        }
    }

    private void processTeamData(String result) {
        try {
            Log.d("TIM_ANDA", "🔄 Processing team data...");

            // Kosongkan list
            allTimList.clear();
            filteredTimList.clear();

            JSONObject response = new JSONObject(result);
            JSONArray allTeams = null;

            // Cek struktur response berdasarkan apa yang ada di ApiHelper
            if (response.has("data")) {
                allTeams = response.getJSONArray("data");
                Log.d("TIM_ANDA", "📊 Found 'data' array with " + allTeams.length() + " items");
            } else if (response.has("teams")) {
                allTeams = response.getJSONArray("teams");
                Log.d("TIM_ANDA", "📊 Found 'teams' array with " + allTeams.length() + " items");
            } else if (response.has("items")) {
                allTeams = response.getJSONArray("items");
                Log.d("TIM_ANDA", "📊 Found 'items' array with " + allTeams.length() + " items");
            } else {
                // Coba langsung sebagai array
                try {
                    allTeams = new JSONArray(result);
                    Log.d("TIM_ANDA", "📊 Response is direct array with " + allTeams.length() + " items");
                } catch (JSONException e) {
                    Log.e("TIM_ANDA", "❌ Invalid JSON format");
                    showEmptyState(true);
                    return;
                }
            }

            if (allTeams == null || allTeams.length() == 0) {
                Log.d("TIM_ANDA", "📭 No teams found in response");
                showEmptyState(true);
                setupEmptyStateWithAction();
                return;
            }

            // Filter tim berdasarkan user_id
            int userTeamCount = 0;

            for (int i = 0; i < allTeams.length(); i++) {
                try {
                    JSONObject team = allTeams.getJSONObject(i);

                    // Cari user_id di berbagai kemungkinan field
                    String teamUserId = "";
                    if (team.has("user_id")) {
                        teamUserId = team.optString("user_id", "");
                    } else if (team.has("userId")) {
                        teamUserId = team.optString("userId", "");
                    } else if (team.has("created_by")) {
                        teamUserId = team.optString("created_by", "");
                    } else if (team.has("ketua_id")) {
                        teamUserId = team.optString("ketua_id", "");
                    }

                    // Debug log untuk tim milik user
                    String namaTeam = team.optString("nama_team", "No Name");
                    Log.d("TIM_ANDA_FILTER", "Team: " + namaTeam +
                            ", Team User ID: " + teamUserId +
                            ", Current User ID: " + currentUserId);

                    if (teamUserId.equals(currentUserId)) {
                        // Ambil data yang dibutuhkan
                        JSONObject simpleTeam = new JSONObject();
                        simpleTeam.put("nama_team", team.optString("nama_team", "Nama Tim"));
                        simpleTeam.put("max_anggota", team.optString("max_anggota", "4"));
                        simpleTeam.put("tenggat_join", team.optString("tenggat_join", ""));
                        simpleTeam.put("current_members", team.optString("current_members", "1"));
                        simpleTeam.put("id", team.optString("id", team.optString("team_id", "")));

                        // Hitung hari tersisa untuk sorting
                        long daysRemaining = calculateDaysRemaining(simpleTeam.optString("tenggat_join", ""));
                        simpleTeam.put("days_remaining", daysRemaining);

                        allTimList.add(simpleTeam);
                        userTeamCount++;

                        Log.d("TIM_ANDA", "✅ Added user's team: " + namaTeam +
                                ", Tenggat: " + simpleTeam.optString("tenggat_join", "") +
                                ", Hari tersisa: " + daysRemaining);
                    }

                } catch (Exception e) {
                    Log.e("TIM_ANDA", "❌ Error processing team item " + i + ": " + e.getMessage());
                }
            }

            Log.d("TIM_ANDA", "✅ Total user teams found: " + userTeamCount + " out of " + allTeams.length() + " total teams");

            if (userTeamCount == 0) {
                Log.d("TIM_ANDA", "📭 User has no teams");
                showEmptyState(true);
                setupEmptyStateWithAction();
            } else {
                // Urutkan tim berdasarkan tenggat terdekat
                sortTeamsByDeadline();

                // Salin ke filtered list untuk ditampilkan
                filteredTimList.clear();
                filteredTimList.addAll(allTimList);
                displayTeams(filteredTimList);
                showEmptyState(false);
            }

        } catch (Exception e) {
            Log.e("TIM_ANDA", "❌ Error in processTeamData: " + e.getMessage());
            e.printStackTrace();
            showEmptyState(true);
        }
    }

    private long calculateDaysRemaining(String tenggatJoin) {
        if (tenggatJoin == null || tenggatJoin.isEmpty() || tenggatJoin.equals("null") || tenggatJoin.equals("0000-00-00")) {
            return Long.MAX_VALUE; // Tim tanpa tenggat dianggap paling akhir
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date deadline = sdf.parse(tenggatJoin);
            Date today = new Date();

            // Hitung selisih dalam milidetik
            long diff = deadline.getTime() - today.getTime();

            // Konversi ke hari
            long daysRemaining = diff / (1000 * 60 * 60 * 24);

            return daysRemaining;

        } catch (ParseException e) {
            Log.e("TIM_ANDA", "❌ Error parsing tenggat_join: " + tenggatJoin);
            return Long.MAX_VALUE;
        }
    }

    private void sortTeamsByDeadline() {
        // Urutkan tim berdasarkan hari tersisa (ascending)
        // 1. Tim dengan tenggat terdekat (hari tersisa paling sedikit)
        // 2. Tim tanpa tenggat di paling bawah
        // 3. Jika sama, urutkan berdasarkan nama

        allTimList.sort((team1, team2) -> {
            try {
                long daysRemaining1 = team1.optLong("days_remaining", Long.MAX_VALUE);
                long daysRemaining2 = team2.optLong("days_remaining", Long.MAX_VALUE);

                // Urutkan berdasarkan hari tersisa
                if (daysRemaining1 != daysRemaining2) {
                    return Long.compare(daysRemaining1, daysRemaining2); // Ascending
                }

                // Jika hari tersisa sama, urutkan berdasarkan nama
                String name1 = team1.optString("nama_team", "");
                String name2 = team2.optString("nama_team", "");
                return name1.compareToIgnoreCase(name2);

            } catch (Exception e) {
                return 0;
            }
        });

        // Log untuk debugging
        Log.d("TIM_ANDA_SORT", "📊 Teams sorted by deadline:");
        for (int i = 0; i < allTimList.size(); i++) {
            JSONObject team = allTimList.get(i);
            String nama = team.optString("nama_team", "No Name");
            String tenggat = team.optString("tenggat_join", "No Deadline");
            long daysRemaining = team.optLong("days_remaining", Long.MAX_VALUE);

            String daysText = (daysRemaining == Long.MAX_VALUE) ? "Tanpa tenggat" : daysRemaining + " hari";
            Log.d("TIM_ANDA_SORT", "  " + (i + 1) + ". " + nama +
                    " | Tenggat: " + tenggat +
                    " | Tersisa: " + daysText);
        }
    }

    private void displayTeams(List<JSONObject> teams) {
        // Kosongkan container
        containerPostinganLomba.removeAllViews();

        if (teams == null || teams.isEmpty()) {
            Log.d("TIM_ANDA", "📭 No teams to display");
            showEmptyState(true);
            return;
        }

        Log.d("TIM_ANDA", "🔄 Displaying " + teams.size() + " teams (sorted by deadline)");

        int addedCount = 0;
        for (int i = 0; i < teams.size(); i++) {
            JSONObject team = teams.get(i);

            // Tambahkan indicator untuk tim dengan tenggat terdekat
            long daysRemaining = team.optLong("days_remaining", Long.MAX_VALUE);
            if (i == 0 && daysRemaining != Long.MAX_VALUE) {
                Log.d("TIM_ANDA", "⏰ Top team (nearest deadline): " +
                        team.optString("nama_team", "") +
                        " - " + daysRemaining + " hari lagi");
            }

            boolean added = addTimItem(team);
            if (added) addedCount++;
        }

        Log.d("TIM_ANDA", "✅ Successfully displayed " + addedCount + " items");

        if (addedCount == 0) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
    }

    private boolean addTimItem(JSONObject team) {
        try {
            String namaTeam = team.optString("nama_team", "Nama Tim");
            String tenggatJoin = team.optString("tenggat_join", "");
            long daysRemaining = team.optLong("days_remaining", Long.MAX_VALUE);

            Log.d("TIM_ANDA_ITEM", "➕ Adding team item: " + namaTeam +
                    " | Tenggat: " + tenggatJoin +
                    " | Hari tersisa: " + (daysRemaining == Long.MAX_VALUE ? "Tanpa tenggat" : daysRemaining));

            // Inflate layout item
            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.fragment_item_buat_tim, containerPostinganLomba, false);

            // Extract data dari JSON
            String maxAnggota = team.optString("max_anggota", "4");
            String currentMembers = team.optString("current_members", "1");
            String teamId = team.optString("id", "");

            // Find views dengan null check
            TextView tvNamaTim = itemView.findViewById(R.id.tvnamatim);
            TextView tvJumlahOrang = itemView.findViewById(R.id.tjumlahorang);
            TextView tvTanggal = itemView.findViewById(R.id.tvtanggal);
            Button btnBuatPostingan = itemView.findViewById(R.id.btnSimpanPostingLomba);
            LinearLayout containerInfoTim = itemView.findViewById(R.id.containerInfoTim);

            if (tvNamaTim == null || tvJumlahOrang == null || tvTanggal == null ||
                    btnBuatPostingan == null || containerInfoTim == null) {
                Log.e("TIM_ANDA_ITEM", "❌ CRITICAL: Some views are null!");
                return false;
            }

            // Set data to views
            tvNamaTim.setText(namaTeam);

            // Format jumlah anggota
            String jumlahAnggotaText = currentMembers + "/" + maxAnggota;
            tvJumlahOrang.setText(jumlahAnggotaText);

            // Format tanggal dengan indicator hari tersisa
            String formattedDate = formatDateWithDeadlineIndicator(tenggatJoin, daysRemaining);
            tvTanggal.setText(formattedDate);

            // Optional: Highlight tim dengan tenggat terdekat
            if (daysRemaining >= 0 && daysRemaining <= 3) {
                // Tim dengan tenggat 3 hari ke bawah
                tvTanggal.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (daysRemaining >= 4 && daysRemaining <= 7) {
                tvTanggal.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            } else if (daysRemaining >= 11 && daysRemaining <= 1000) {
                tvTanggal.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                // Tim dengan tenggat normal
                tvTanggal.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            }

            // Set click listener untuk detail tim
            containerInfoTim.setOnClickListener(v -> {
                openDetailTim(team);
            });

            // Set click listener untuk buat postingan rekrut tim
            btnBuatPostingan.setOnClickListener(v -> {
                openBuatPostinganRekrut(team);
            });

            // Tambahkan ke container
            containerPostinganLomba.addView(itemView);
            Log.d("TIM_ANDA_ITEM", "✅ Team '" + namaTeam + "' added successfully");
            return true;

        } catch (Exception e) {
            Log.e("TIM_ANDA_ITEM", "❌ Error adding team item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String formatDateWithDeadlineIndicator(String dateString, long daysRemaining) {
        String formattedDate = formatDate(dateString);

        if (daysRemaining == Long.MAX_VALUE) {
            return formattedDate; // Tanpa tenggat
        }

        if (daysRemaining < 0) {
            // Tenggat sudah lewat
            return formattedDate + " (Telah lewat)";
        } else if (daysRemaining == 0) {
            // Tenggat hari ini
            return formattedDate + " (Hari ini)";
        } else if (daysRemaining == 1) {
            // Tenggat besok
            return formattedDate + " (Besok)";
        } else if (daysRemaining <= 7) {
            // Tenggat dalam 7 hari
            return formattedDate + " (" + daysRemaining + " hari lagi)";
        }

        return formattedDate;
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty() || dateString.equals("null") || dateString.equals("0000-00-00")) {
            return "Belum ditentukan";
        }

        try {
            // Coba format yyyy-MM-dd
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = inputFormat.parse(dateString);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            return outputFormat.format(date);
        } catch (ParseException e1) {
            try {
                // Coba format lain jika gagal
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                Date date = inputFormat.parse(dateString);

                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
                return outputFormat.format(date);
            } catch (ParseException e2) {
                // Jika semua format gagal, kembalikan string asli
                return dateString;
            }
        }
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            containerPostinganLomba.setVisibility(View.GONE);
            emptyPostinganText.setVisibility(View.VISIBLE);

            // Update text berdasarkan apakah sedang search atau tidak
            String searchText = searchInput.getText().toString().trim();

            if (isSearchMode && !searchText.isEmpty()) {
                // Mode search dengan query
                emptyPostinganText.setText("Mencari tim: \"" + searchText + "\"");
            } else if (isSearchMode) {
                // Mode search tanpa query
                emptyPostinganText.setText("Ketik nama tim untuk mencari...");
            } else {
                // Normal mode
                emptyPostinganText.setText("Anda belum memiliki tim. Buat tim pertama Anda dan mulai rekrut tim Anda di sini!!");
            }

            Log.d("TIM_ANDA", "📭 Showing empty state, Search Mode: " + isSearchMode);
        } else {
            containerPostinganLomba.setVisibility(View.VISIBLE);
            emptyPostinganText.setVisibility(View.GONE);
            Log.d("TIM_ANDA", "📊 Showing data state");
        }
    }

    private void showLoadingState(boolean isLoading) {
        if (isLoading) {
            // Tampilkan loading indicator
            emptyPostinganText.setText("Memuat data tim...");
            emptyPostinganText.setVisibility(View.VISIBLE);
            containerPostinganLomba.setVisibility(View.GONE);
            Log.d("TIM_ANDA", "⏳ Showing loading state");
        } else {
            // Hilangkan loading
            Log.d("TIM_ANDA", "✅ Hiding loading state");
        }
    }

    // Method untuk tombol aksi di empty state
    private void setupEmptyStateWithAction() {
        emptyPostinganText.setText("Anda belum memiliki tim. Buat tim pertama Anda dan mulai rekrut tim Anda di sini!!");

        // Tambahkan button untuk membuat tim
        Button btnCreateFirst = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.CENTER;
        btnCreateFirst.setLayoutParams(params);
        btnCreateFirst.setText("Buat Tim Pertama");
        btnCreateFirst.setBackgroundResource(R.drawable.custom_button_rounded);
        btnCreateFirst.setTextColor(getResources().getColor(android.R.color.white));
        btnCreateFirst.setPadding(40, 20, 40, 20);
        btnCreateFirst.setOnClickListener(v -> {
            Intent intent = new Intent(TimAnda.this, BuatTim.class);
            tambahTimLauncher.launch(intent);
        });

        // Tambahkan ke container
        LinearLayout container = findViewById(R.id.containerPostinganLomba);
        if (container != null) {
            container.addView(btnCreateFirst);
        }
    }

    private void openDetailTim(JSONObject team) {
        try {
            Intent detailIntent = new Intent(TimAnda.this, Detail_rekrut_team.class);

            // Pass only essential data
            detailIntent.putExtra("team_id", team.optString("id", ""));
            detailIntent.putExtra("team_name", team.optString("nama_team", ""));
            detailIntent.putExtra("max_anggota", team.optString("max_anggota", ""));
            detailIntent.putExtra("current_members", team.optString("current_members", ""));
            detailIntent.putExtra("tenggat_join", team.optString("tenggat_join", ""));
            detailIntent.putExtra("days_remaining", team.optLong("days_remaining", Long.MAX_VALUE));

            startActivity(detailIntent);

        } catch (Exception e) {
            Log.e("TIM_ANDA", "❌ Error opening team detail: " + e.getMessage());
            Toast.makeText(this, "Gagal membuka detail tim", Toast.LENGTH_SHORT).show();
        }
    }

    private void openBuatPostinganRekrut(JSONObject team) {
        try {
            Intent intent = new Intent(TimAnda.this, Posting_rekrut_team.class);
            intent.putExtra("team_id", team.optString("id", ""));
            intent.putExtra("team_name", team.optString("nama_team", ""));
            intent.putExtra("tenggat_join", team.optString("tenggat_join", ""));
            intent.putExtra("days_remaining", team.optLong("days_remaining", Long.MAX_VALUE));
            startActivity(intent);
        } catch (Exception e) {
            Log.e("TIM_ANDA", "❌ Error opening posting rekrut: " + e.getMessage());
            Toast.makeText(this, "Gagal membuka halaman posting rekrut", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method untuk debug
    private String getKeys(JSONObject jsonObject) {
        try {
            StringBuilder keys = new StringBuilder();
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                keys.append(iterator.next()).append(", ");
            }
            return keys.toString();
        } catch (Exception e) {
            return "Error getting keys";
        }
    }
}