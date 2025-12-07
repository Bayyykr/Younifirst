package com.naufal.younifirst.Home;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naufal.younifirst.Kompetisi.BuatTim;
import com.naufal.younifirst.R;
import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.custom.CustomEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PengaturanActivity extends AppCompatActivity {
    private static final String TAG = "PengaturanActivity";

    // UI untuk pengaturan
    private SwitchCompat switchtema, switchnotifikasi;
    private ImageButton btnSettingProfile;
    private CustomEditText etDeskripsiMasukan;
    private LinearLayout layoutUploadedImages;
    private ImageButton btnGalery;
    private static final int REQUEST_CODE_GALLERY = 101;
    private ArrayList<Uri> imageUris = new ArrayList<>();

    // UI untuk postingan
    private RecyclerView recyclerViewPostingan;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private TextView tabLostFound, tabTim;
    private String currentKategori = "Lost&Found";

    // Data
    private List<PostinganItem> postinganList = new ArrayList<>();
    private PostinganAdapter adapter;

    // User info
    private String currentUserId = "";
    private String currentUserName = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ApiHelper
        ApiHelper.initialize(getApplicationContext());

        // Get current user info
        currentUserId = ApiHelper.getSavedUserId();
        currentUserName = ApiHelper.getSavedUserName();

        Log.d(TAG, "👤 Current User - ID: " + currentUserId + ", Name: " + currentUserName);

        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showPengaturanLayout();
    }

    // ==================== PENGATURAN LAYOUT ====================
    private void showPengaturanLayout() {
        setContentView(R.layout.layout_pengaturan);

        // Inisialisasi komponen pengaturan
        switchtema = findViewById(R.id.switch_tema);
        switchnotifikasi = findViewById(R.id.switch_notifikasi);
        btnSettingProfile = findViewById(R.id.btn_setting_profile);
        ImageView backButtonMain = findViewById(R.id.back_to_mainactivity);
        LinearLayout bantuanDanMasukkan = findViewById(R.id.bantuandanmasukkan);
        LinearLayout layoutPostingan = findViewById(R.id.layout_postingan);

        // Warna switch
        int[][] states = new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}};
        int[] trackColors = new int[]{Color.WHITE, Color.WHITE};
        switchtema.setTrackTintList(new ColorStateList(states, trackColors));
        switchnotifikasi.setTrackTintList(new ColorStateList(states, trackColors));

        // Click listeners
        btnSettingProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        backButtonMain.setOnClickListener(v -> finish());
        bantuanDanMasukkan.setOnClickListener(v -> showBantuanLayout());

        // Navigasi ke postingan Anda
        if (layoutPostingan != null) {
            layoutPostingan.setOnClickListener(v -> showPostinganLayout());
        }
    }

    // ==================== POSTINGAN LAYOUT ====================
    private void showPostinganLayout() {
        setContentView(R.layout.layout_postingan_anda);

        Log.d(TAG, "🎯 Show Postingan Layout for user: " + currentUserName);

        // Inisialisasi view
        ImageView backButton = findViewById(R.id.back_to_mainactivity);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);
        recyclerViewPostingan = findViewById(R.id.recyclerView_postingan);
        tabLostFound = findViewById(R.id.tab_lostfound);
        tabTim = findViewById(R.id.tab_tim);
        Button btnBuatPostingan = findViewById(R.id.btn_buat_postingan);

        // Setup recyclerview
        recyclerViewPostingan.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostinganAdapter(this, postinganList);
        recyclerViewPostingan.setAdapter(adapter);

        // Setup tabs
        setupTabNavigation();
        setActiveTab(tabLostFound);

        // Click listeners
        backButton.setOnClickListener(v -> showPengaturanLayout());

        if (btnBuatPostingan != null) {
            btnBuatPostingan.setOnClickListener(v -> {
                Intent intent = new Intent(PengaturanActivity.this, BuatTim.class);
                startActivity(intent);
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        // Load data
        loadPostinganData();
    }

    private void setupTabNavigation() {
        tabLostFound.setOnClickListener(v -> {
            if (!"Lost&Found".equals(currentKategori)) {
                currentKategori = "Lost&Found";
                setActiveTab(tabLostFound);
                loadPostinganData();
            }
        });

        tabTim.setOnClickListener(v -> {
            if (!"Tim".equals(currentKategori)) {
                currentKategori = "Tim";
                setActiveTab(tabTim);
                loadPostinganData();
            }
        });
    }

    private void setActiveTab(TextView activeTab) {
        // Reset semua tab
        tabLostFound.setTextColor(Color.parseColor("#858891"));
        tabTim.setTextColor(Color.parseColor("#858891"));
        tabLostFound.setBackgroundColor(Color.parseColor("#1A2340"));
        tabTim.setBackgroundColor(Color.parseColor("#1A2340"));

        // Set tab aktif
        if (activeTab.getId() == R.id.tab_lostfound) {
            tabLostFound.setTextColor(Color.WHITE);
            tabLostFound.setBackgroundColor(Color.parseColor("#2D3748"));
        } else {
            tabTim.setTextColor(Color.WHITE);
            tabTim.setBackgroundColor(Color.parseColor("#2D3748"));
        }
    }

    // ==================== LOAD DATA - SIMPLE & WORKING ====================
    private void loadPostinganData() {
        Log.d(TAG, "🔄 Loading data for kategori: " + currentKategori);

        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            showEmptyState("Silakan login untuk melihat postingan Anda");
            return;
        }

        showLoading(true);

        // Gunakan method fetchPostinganSimple dari ApiHelper
        ApiHelper.fetchPostinganSimple(currentKategori, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ API Success, processing data...");
                processData(result);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ API Error: " + error);
                runOnUiThread(() -> {
                    showLoading(false);
                    loadDummyData();
                    Toast.makeText(PengaturanActivity.this,
                            "Menggunakan data contoh", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void processData(String result) {
        runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(result);
                boolean success = json.optBoolean("success", false);

                if (success) {
                    JSONArray dataArray = json.optJSONArray("data");

                    if (dataArray != null && dataArray.length() > 0) {
                        List<PostinganItem> userPosts = new ArrayList<>();
                        int totalPosts = dataArray.length();
                        int userPostCount = 0;
                        int kategoriMatchCount = 0;

                        Log.d(TAG, "📊 Found " + totalPosts + " total posts");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject item = dataArray.getJSONObject(i);

                            // Cari user_id dengan berbagai kemungkinan key
                            String itemUserId = "";
                            if (item.has("user_id")) {
                                itemUserId = item.optString("user_id", "");
                            } else if (item.has("userId")) {
                                itemUserId = item.optString("userId", "");
                            } else if (item.has("userid")) {
                                itemUserId = item.optString("userid", "");
                            } else if (item.has("user")) {
                                itemUserId = item.optString("user", "");
                            }

                            String kategori = item.optString("kategori", "");
                            String judul = item.optString("nama_barang", "No Title");

                            Log.d(TAG, "\n🔍 Post #" + (i+1) + " - Judul: " + judul);
                            Log.d(TAG, "   👤 User ID: '" + itemUserId + "' vs Current: '" + currentUserId + "'");
                            Log.d(TAG, "   🏷 Kategori: '" + kategori + "'");

                            // Hanya ambil jika milik user yang login
                            if (currentUserId.equals(itemUserId)) {
                                userPostCount++;
                                Log.d(TAG, "   ✅ IS USER'S POST!");

                                // Filter berdasarkan tab
                                if (shouldShowInTab(kategori, currentKategori)) {
                                    kategoriMatchCount++;

                                    PostinganItem post = new PostinganItem();
                                    post.id = item.optString("id", "");
                                    post.userId = itemUserId;
                                    post.nama = item.optString("nama", currentUserName);
                                    post.kategori = kategori;
                                    post.judul = judul;
                                    post.deskripsi = item.optString("deskripsi", "");
                                    post.tanggal = formatDate(item.optString("created_at", ""));
                                    post.likes = item.optInt("likes", 0);
                                    post.comments = item.optInt("comments", 0);
                                    post.shares = item.optInt("shares", 0);

                                    userPosts.add(post);
                                    Log.d(TAG, "   ✅ ADDED to list");
                                } else {
                                    Log.d(TAG, "   ❌ Kategori mismatch for current tab");
                                }
                            } else {
                                Log.d(TAG, "   ❌ NOT user's post");
                            }
                        }

                        Log.d(TAG, "\n📊 SUMMARY:");
                        Log.d(TAG, "   Total posts: " + totalPosts);
                        Log.d(TAG, "   User's posts: " + userPostCount);
                        Log.d(TAG, "   Matching kategori for '" + currentKategori + "': " + kategoriMatchCount);

                        if (userPosts.isEmpty()) {
                            showEmptyState("Anda belum memiliki postingan " + currentKategori);
                        } else {
                            updatePostinganList(userPosts);
                            Toast.makeText(this,
                                    "Menampilkan " + userPosts.size() + " postingan Anda",
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.d(TAG, "📭 No data found in API response");
                        showEmptyState("Tidak ada data ditemukan");
                    }
                } else {
                    String message = json.optString("message", "No message");
                    Log.e(TAG, "❌ API success = false: " + message);
                    showEmptyState("Gagal memuat data: " + message);
                }

                showLoading(false);

            } catch (Exception e) {
                Log.e(TAG, "❌ Error processing data: " + e.getMessage());
                showLoading(false);
                loadDummyData();
            }
        });
    }

    private boolean shouldShowInTab(String itemKategori, String selectedTab) {
        if (itemKategori == null || selectedTab == null) return false;

        // Normalize kategori
        itemKategori = itemKategori.toLowerCase().trim();

        Log.d(TAG, "🔍 Checking kategori - Item: '" + itemKategori + "', Tab: '" + selectedTab + "'");

        if ("Lost&Found".equals(selectedTab)) {
            // Untuk Lost&Found, terima berbagai variasi
            boolean shouldShow = itemKategori.contains("lost") ||
                    itemKategori.contains("found") ||
                    itemKategori.contains("hilang") ||
                    itemKategori.contains("temu") ||
                    itemKategori.equals("lost&found") ||
                    itemKategori.equals("lostfound") ||
                    itemKategori.equals("lost found");

            Log.d(TAG, "   ✅ Should show in Lost&Found: " + shouldShow);
            return shouldShow;

        } else if ("Tim".equals(selectedTab)) {
            // Untuk Tim, terima berbagai variasi
            boolean shouldShow = itemKategori.contains("tim") ||
                    itemKategori.contains("team") ||
                    itemKategori.contains("kelompok") ||
                    itemKategori.contains("proyek") ||
                    itemKategori.contains("project") ||
                    itemKategori.equals("tim") ||
                    itemKategori.equals("team");

            Log.d(TAG, "   ✅ Should show in Tim: " + shouldShow);
            return shouldShow;
        }

        return false;
    }


    private void loadDummyData() {
        List<PostinganItem> dummyList = new ArrayList<>();

        if ("Lost&Found".equals(currentKategori)) {
            dummyList.add(new PostinganItem(
                    "1", currentUserId, currentUserName, "Lost&Found",
                    "Kunci Motor Hilang",
                    "Hilang kunci motor di area kampus",
                    "2 jam yang lalu", 15, 5, 2
            ));
            dummyList.add(new PostinganItem(
                    "2", currentUserId, currentUserName, "Lost&Found",
                    "Dompet Ditemukan",
                    "Menemukan dompet di kantin pusat",
                    "1 hari yang lalu", 24, 8, 3
            ));
        } else {
            dummyList.add(new PostinganItem(
                    "3", currentUserId, currentUserName, "Tim",
                    "Mencari Frontend Developer",
                    "Untuk project mobile app development",
                    "2 hari yang lalu", 32, 12, 5
            ));
            dummyList.add(new PostinganItem(
                    "4", currentUserId, currentUserName, "Tim",
                    "Butuh UI/UX Designer",
                    "Startup edutech mencari designer",
                    "3 hari yang lalu", 18, 6, 1
            ));
        }

        updatePostinganList(dummyList);
    }

    private void updatePostinganList(List<PostinganItem> newList) {
        postinganList.clear();
        postinganList.addAll(newList);
        adapter.notifyDataSetChanged();

        if (postinganList.isEmpty()) {
            showEmptyState("Anda belum memiliki postingan " + currentKategori);
        } else {
            recyclerViewPostingan.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewPostingan.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerViewPostingan.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);

        TextView tvMessage = layoutEmpty.findViewById(R.id.tv_empty_message);
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "Baru saja";
        }

        try {
            // Format sederhana: "2024-01-15" -> "15 Jan"
            String[] parts = dateStr.split(" ")[0].split("-");
            if (parts.length >= 3) {
                String[] months = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
                int monthIdx = Integer.parseInt(parts[1]) - 1;
                if (monthIdx >= 0 && monthIdx < 12) {
                    return parts[2] + " " + months[monthIdx];
                }
            }
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }

    // ==================== DATA CLASS ====================
    class PostinganItem {
        String id;
        String userId;
        String nama;
        String kategori;
        String judul;
        String deskripsi;
        String tanggal;
        int likes;
        int comments;
        int shares;

        PostinganItem() {}

        PostinganItem(String id, String userId, String nama, String kategori,
                      String nama_barang, String deskripsi, String tanggal,
                      int likes, int comments, int shares) {
            this.id = id;
            this.userId = userId;
            this.nama = nama;
            this.kategori = kategori;
            this.judul = judul;
            this.deskripsi = deskripsi;
            this.tanggal = tanggal;
            this.likes = likes;
            this.comments = comments;
            this.shares = shares;
        }
    }

    // ==================== ADAPTER ====================
    class PostinganAdapter extends RecyclerView.Adapter<PostinganAdapter.ViewHolder> {
        private Context context;
        private List<PostinganItem> itemList;

        public PostinganAdapter(Context context, List<PostinganItem> itemList) {
            this.context = context;
            this.itemList = itemList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_postingan, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PostinganItem item = itemList.get(position);

            // Set data
            holder.tvNamaPoster.setText(item.nama);
            holder.tvTanggal.setText(item.tanggal);
            holder.tvJudul.setText(item.judul);
            holder.tvDeskripsi.setText(item.deskripsi);
            holder.tvLikes.setText(String.valueOf(item.likes));
            holder.tvComments.setText(String.valueOf(item.comments));
            holder.tvShares.setText(String.valueOf(item.shares));

            // Badge kategori
            if (item.kategori != null && !item.kategori.isEmpty()) {
                holder.badgeKategori.setText(item.kategori);
                holder.badgeKategori.setVisibility(View.VISIBLE);

                if (item.kategori.toLowerCase().contains("lost") ||
                        item.kategori.toLowerCase().contains("found")) {
                    holder.badgeKategori.setBackgroundResource(R.drawable.badge_green);
                    holder.badgeKategori.setTextColor(Color.parseColor("#4CAF50"));
                } else if (item.kategori.toLowerCase().contains("tim")) {
                    holder.badgeKategori.setBackgroundResource(R.drawable.badge_purple);
                    holder.badgeKategori.setTextColor(Color.parseColor("#2196F3"));
                } else {
                    // Gunakan background solid untuk kategori lainnya
                    holder.badgeKategori.setBackgroundColor(Color.parseColor("#2D3748"));
                    holder.badgeKategori.setTextColor(Color.parseColor("#858891"));
                }
            } else {
                holder.badgeKategori.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNamaPoster, tvTanggal, badgeKategori, tvJudul, tvDeskripsi;
            TextView tvLikes, tvComments, tvShares;
            ImageView imagePost, iconMore;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNamaPoster = itemView.findViewById(R.id.tv_nama_poster);
                tvTanggal = itemView.findViewById(R.id.tv_tanggal);
                badgeKategori = itemView.findViewById(R.id.badge_kategori);
                tvJudul = itemView.findViewById(R.id.tv_judul);
                tvDeskripsi = itemView.findViewById(R.id.tv_deskripsi);
                tvLikes = itemView.findViewById(R.id.tv_likes);
                tvComments = itemView.findViewById(R.id.tv_comments);
                tvShares = itemView.findViewById(R.id.tv_shares);
                imagePost = itemView.findViewById(R.id.image_post);
                iconMore = itemView.findViewById(R.id.icon_more);
            }
        }
    }

    // ================ METODE-METODE LAIN YANG SUDAH ADA ================
    private void showBantuanLayout() {
        setContentView(R.layout.layout_bantuan);

        ImageView backFromBantuan = findViewById(R.id.back_to_mainactivity);
        LinearLayout pusatBantuan = findViewById(R.id.pusatbantuan);
        LinearLayout kirimMasukan = findViewById(R.id.kirimmasukan);
        LinearLayout navigasiFaq = findViewById(R.id.bantuanfaq);
        LinearLayout infoAplikasi = findViewById(R.id.infoaplikasi);

        backFromBantuan.setOnClickListener(v -> showPengaturanLayout());
        if (pusatBantuan != null) pusatBantuan.setOnClickListener(v -> showPusatBantuanLayout());
        if (kirimMasukan != null) kirimMasukan.setOnClickListener(v -> showKirimMasukanLayout());
        if (navigasiFaq != null) navigasiFaq.setOnClickListener(v -> showFAQLayout());
        if (infoAplikasi != null) infoAplikasi.setOnClickListener(v -> showInfoAplikasi());
    }

    private void showPusatBantuanLayout() {
        setContentView(R.layout.layout_pusat_bantuan);

        ImageView backFromPusat = findViewById(R.id.back_to_mainactivity);
        TextView akundanlogin = findViewById(R.id.akundanlogin);
        TextView panduanUmum = findViewById(R.id.panduanumum);
        TextView fiturAplikasi = findViewById(R.id.fituraplikasi);
        TextView hubungiKami = findViewById(R.id.hubungikami);

        backFromPusat.setOnClickListener(v -> showBantuanLayout());

        if (akundanlogin != null) akundanlogin.setOnClickListener(v -> showFragmentAkunLogin());
        if (panduanUmum != null) panduanUmum.setOnClickListener(v -> showFragmentPanduanUmum());
        if (fiturAplikasi != null) fiturAplikasi.setOnClickListener(v -> showFragmentFiturAplikasi());
        if (hubungiKami != null) hubungiKami.setOnClickListener(v -> showFragmentHubungiKami());
    }

    private void showpengaturan() {
        setContentView(R.layout.layout_bantuan);

        ImageView backFromPusat = findViewById(R.id.back_to_mainactivity);
        backFromPusat.setOnClickListener(v -> showBantuanLayout());
    }

    private void showInfoAplikasilayout() {
        setContentView(R.layout.layout_bantuan);

        ImageView backFromPusat = findViewById(R.id.back_to_mainactivity);
        LinearLayout infoApk = findViewById(R.id.infoaplikasi);

        backFromPusat.setOnClickListener(v -> showBantuanLayout());

        if (infoApk != null) infoApk.setOnClickListener(v -> showInfoAplikasi());
    }

    private void showKirimMasukanLayout() {
        setContentView(R.layout.layout_kirim_masukan);

        ImageView backFromMasukan = findViewById(R.id.back_to_mainactivity);
        if (backFromMasukan != null) backFromMasukan.setOnClickListener(v -> showBantuanLayout());

        etDeskripsiMasukan = findViewById(R.id.etdeskripsimasalahteknis);
        if (etDeskripsiMasukan != null) {
            etDeskripsiMasukan.getEditText().setSingleLine(false);
            etDeskripsiMasukan.getEditText().setLines(5);
            etDeskripsiMasukan.getEditText().setGravity(Gravity.LEFT | Gravity.START);
            etDeskripsiMasukan.setHint("Deskripsikan masalah teknis");

            int paddingDp = 20;
            float density = getResources().getDisplayMetrics().density;
            int paddingPx = (int) (paddingDp * density + 0.5f);
            etDeskripsiMasukan.getEditText().setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        }

        layoutUploadedImages = findViewById(R.id.layout_uploaded_images);
        btnGalery = findViewById(R.id.btn_galery);
        if (btnGalery != null) {
            btnGalery.setOnClickListener(v -> openGallery());
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                addImageToLayout(selectedImage);
            }
        }
    }

    private void addImageToLayout(Uri imageUri) {
        imageUris.add(imageUri);

        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(200, 200);
        frameParams.setMargins(10, 0, 10, 0);
        frame.setLayoutParams(frameParams);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(imageUri);

        ImageButton btnRemove = new ImageButton(this);
        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(50, 50);
        btnParams.gravity = Gravity.END | Gravity.TOP;
        btnRemove.setLayoutParams(btnParams);
        btnRemove.setBackgroundColor(Color.TRANSPARENT);
        btnRemove.setImageResource(R.drawable.icon_silang_kecil);
        btnRemove.setOnClickListener(v -> {
            layoutUploadedImages.removeView(frame);
            imageUris.remove(imageUri);
        });

        frame.addView(imageView);
        frame.addView(btnRemove);
        layoutUploadedImages.addView(frame);
    }

    private void showFragmentNavigasiAplikasi() {
        setContentView(R.layout.fragment_navigasi_aplikasi_pusat_bantuan);

        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) {
            back.setOnClickListener(v -> showFragmentPanduanUmum());
        }

        LinearLayout navigasiBawah = findViewById(R.id.navigasibawah);
        if (navigasiBawah != null) {
            navigasiBawah.setOnClickListener(v -> showFragmentNavigasiAplikasiBawah());
        }
    }

    private void showFragmentNavigasiAplikasiBawah() {
        setContentView(R.layout.fragment_navigasi_aplikasi_bawah_pusat_bantuan);

        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) {
            back.setOnClickListener(v -> showFragmentNavigasiAplikasi());
        }
    }

    private void showFragmentAkunLogin() {
        setContentView(R.layout.fragment_akun_login_bantuan);
        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showPusatBantuanLayout());
    }

    private void showInfoAplikasi() {
        setContentView(R.layout.layout_bantuan_info_aplikasi);
        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showpengaturan());
    }

    private void showFragmentPanduanUmum() {
        setContentView(R.layout.fragment_panduan_umum_pusat_bantuan);

        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showPusatBantuanLayout());

        LinearLayout navigasiAplikasi = findViewById(R.id.navigasiaplikasi);
        if (navigasiAplikasi != null) {
            navigasiAplikasi.setOnClickListener(v -> showFragmentNavigasiAplikasi());
        }
    }

    private void showFragmentFiturAplikasi() {
        setContentView(R.layout.fragment_fitur_aplikasi_pusat_bantuan);
        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showPusatBantuanLayout());
    }

    private void showFragmentHubungiKami() {
        setContentView(R.layout.fragment_hubungi_kami_pusat_bantuan);
        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showPusatBantuanLayout());
    }

    //INI DROPDOWN FAQ
    private void showFAQLayout() {
        setContentView(R.layout.layout_bantuan_faq);

        ImageView back = findViewById(R.id.back_to_mainactivity);
        if (back != null) back.setOnClickListener(v -> showBantuanLayout());

        LinearLayout faqItem1 = findViewById(R.id.dropdwon_faq_item_1);
        LinearLayout faqHeader1 = findViewById(R.id.faq_header_1);
        TextView faqAnswer1 = findViewById(R.id.faq_answer_1);
        ImageView faqIcon1 = findViewById(R.id.faq_icon_1);

        final boolean[] isExpanded1 = {false};

        faqHeader1.setOnClickListener(v -> {
            if (!isExpanded1[0]) {
                faqAnswer1.setVisibility(View.VISIBLE);
                faqIcon1.setImageResource(R.drawable.icon_drop_down_on);
                faqItem1.setBackgroundResource(R.drawable.bg_faq_active);
                isExpanded1[0] = true;
            } else {
                faqAnswer1.setVisibility(View.GONE);
                faqIcon1.setImageResource(R.drawable.icon_drop_down_off);
                faqItem1.setBackgroundResource(R.drawable.bg_faq_default);
                isExpanded1[0] = false;
            }
        });

        LinearLayout faqItem2 = findViewById(R.id.dropdwon_faq_item_2);
        LinearLayout faqHeader2 = findViewById(R.id.faq_header_2);
        TextView faqAnswer2 = findViewById(R.id.faq_answer_2);
        ImageView faqIcon2 = findViewById(R.id.faq_icon_2);

        final boolean[] isExpanded2 = {false};

        faqHeader2.setOnClickListener(v -> {
            if (!isExpanded2[0]) {
                faqAnswer2.setVisibility(View.VISIBLE);
                faqIcon2.setImageResource(R.drawable.icon_drop_down_on);
                faqItem2.setBackgroundResource(R.drawable.bg_faq_active);
                isExpanded2[0] = true;
            } else {
                faqAnswer2.setVisibility(View.GONE);
                faqIcon2.setImageResource(R.drawable.icon_drop_down_off);
                faqItem2.setBackgroundResource(R.drawable.bg_faq_default);
                isExpanded2[0] = false;
            }
        });

        LinearLayout faqItem3 = findViewById(R.id.dropdwon_faq_item_3);
        LinearLayout faqHeader3 = findViewById(R.id.faq_header_3);
        TextView faqAnswer3 = findViewById(R.id.faq_answer_3);
        ImageView faqIcon3 = findViewById(R.id.faq_icon_3);

        final boolean[] isExpanded3 = {false};

        faqHeader3.setOnClickListener(v -> {
            if (!isExpanded3[0]) {
                faqAnswer3.setVisibility(View.VISIBLE);
                faqIcon3.setImageResource(R.drawable.icon_drop_down_on);
                faqItem3.setBackgroundResource(R.drawable.bg_faq_active);
                isExpanded3[0] = true;
            } else {
                faqAnswer3.setVisibility(View.GONE);
                faqIcon3.setImageResource(R.drawable.icon_drop_down_off);
                faqItem3.setBackgroundResource(R.drawable.bg_faq_default);
                isExpanded3[0] = false;
            }
        });

        LinearLayout faqItem4 = findViewById(R.id.dropdwon_faq_item_4);
        LinearLayout faqHeader4 = findViewById(R.id.faq_header_4);
        TextView faqAnswer4 = findViewById(R.id.faq_answer_4);
        ImageView faqIcon4 = findViewById(R.id.faq_icon_4);

        final boolean[] isExpanded4 = {false};

        faqHeader4.setOnClickListener(v -> {
            if (!isExpanded4[0]) {
                faqAnswer4.setVisibility(View.VISIBLE);
                faqIcon4.setImageResource(R.drawable.icon_drop_down_on);
                faqItem4.setBackgroundResource(R.drawable.bg_faq_active);
                isExpanded4[0] = true;
            } else {
                faqAnswer4.setVisibility(View.GONE);
                faqIcon4.setImageResource(R.drawable.icon_drop_down_off);
                faqItem4.setBackgroundResource(R.drawable.bg_faq_default);
                isExpanded4[0] = false;
            }
        });

        LinearLayout faqItem5 = findViewById(R.id.dropdwon_faq_item_5);
        LinearLayout faqHeader5 = findViewById(R.id.faq_header_5);
        TextView faqAnswer5 = findViewById(R.id.faq_answer_5);
        ImageView faqIcon5 = findViewById(R.id.faq_icon_5);

        final boolean[] isExpanded5 = {false};

        faqHeader5.setOnClickListener(v -> {
            if (!isExpanded5[0]) {
                faqAnswer5.setVisibility(View.VISIBLE);
                faqIcon5.setImageResource(R.drawable.icon_drop_down_on);
                faqItem5.setBackgroundResource(R.drawable.bg_faq_active);
                isExpanded5[0] = true;
            } else {
                faqAnswer5.setVisibility(View.GONE);
                faqIcon5.setImageResource(R.drawable.icon_drop_down_off);
                faqItem5.setBackgroundResource(R.drawable.bg_faq_default);
                isExpanded5[0] = false;
            }
        });
    }
}