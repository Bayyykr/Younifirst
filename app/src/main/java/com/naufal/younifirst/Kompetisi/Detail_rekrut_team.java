package com.naufal.younifirst.Kompetisi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;
import com.naufal.younifirst.controller.TeamController;
import com.naufal.younifirst.model.Team;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Detail_rekrut_team extends AppCompatActivity {

    private PopupWindow popupWindow;
    private String teamId;
    private TeamController teamController;
    private Team currentTeam;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_rekrut_tim);

        // Initialize controller
        teamController = new TeamController();

        // Get team data from intent
        Intent intent = getIntent();
        if (intent != null) {
            teamId = intent.getStringExtra("team_id");
            String teamName = intent.getStringExtra("team_name");
            String maxAnggota = intent.getStringExtra("max_anggota");
            String currentMembers = intent.getStringExtra("current_members");
            String tenggatJoin = intent.getStringExtra("tenggat_join");
            long daysRemaining = intent.getLongExtra("days_remaining", Long.MAX_VALUE);

            Log.d("DETAIL_REKRUT", "Team ID: " + teamId);
            Log.d("DETAIL_REKRUT", "Team Name: " + teamName);
            Log.d("DETAIL_REKRUT", "Max Anggota: " + maxAnggota);
            Log.d("DETAIL_REKRUT", "Current Members: " + currentMembers);
            Log.d("DETAIL_REKRUT", "Tenggat Join: " + tenggatJoin);
            Log.d("DETAIL_REKRUT", "Days Remaining: " + daysRemaining);

            // Set initial data
            setInitialData(teamName, maxAnggota, currentMembers, tenggatJoin);
        }

        // Setup views
        setupViews();

        // Load detailed team data
        loadTeamDetail();
    }

    private void setInitialData(String teamName, String maxAnggota, String currentMembers, String tenggatJoin) {
        try {
            // Find the include view
            View includeView = findViewById(R.id.includeTimAnda);
            if (includeView != null) {
                // Member saat ini
                TextView memberCount = includeView.findViewById(R.id.member_count_1);
                if (memberCount != null) {
                    String memberText = (currentMembers != null ? currentMembers : "1") +
                            "/" + (maxAnggota != null ? maxAnggota : "4");
                    memberCount.setText(memberText);
                }

                // Nama Team
                TextView teamNameView = includeView.findViewById(R.id.team_name_1);
                if (teamNameView != null && teamName != null) {
                    teamNameView.setText(teamName);
                }

                // Deadline Join
                TextView deadlineView = includeView.findViewById(R.id.member_limit_2);
                if (deadlineView != null) {
                    String formattedDate = formatDate(tenggatJoin);
                    deadlineView.setText(formattedDate);
                }

                // Status (menghitung berapa member dibutuhkan)
                TextView statusView = includeView.findViewById(R.id.team_category);
                if (statusView != null) {
                    try {
                        int current = currentMembers != null ? Integer.parseInt(currentMembers) : 1;
                        int max = maxAnggota != null ? Integer.parseInt(maxAnggota) : 4;
                        int needed = max - current;

                        if (needed > 0) {
                            statusView.setText("Butuh " + needed + " Member");
                            statusView.setTextColor(Color.parseColor("#FF5E8BFF")); // Biru
                        } else {
                            statusView.setText("Tim Penuh");
                            statusView.setTextColor(Color.parseColor("#FFE74C3C")); // Merah
                        }
                    } catch (NumberFormatException e) {
                        statusView.setText("Member dibutuhkan");
                        statusView.setTextColor(Color.parseColor("#FF5E8BFF"));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DETAIL_REKRUT", "Error setting initial data: " + e.getMessage());
        }
    }

    private void setupViews() {
        ImageView backButton = findViewById(R.id.back_to_mainactivity);
        backButton.setOnClickListener(v -> finish());

        // Setup dropdown listeners sekarang, tidak menunggu data
        setupDropdownListeners();

        // Setup icon lainnya untuk popup menu
        ImageView iconLainnya = findViewById(R.id.iconlainnya);
        if (iconLainnya != null) {
            iconLainnya.setOnClickListener(v -> showPopupMenu(v));
        }

        // Setup button "Buat Postingan Rekrut Tim" sekarang
        setupBuatPostinganButton();
    }

    private void setupDropdownListeners() {
        View includeView = findViewById(R.id.includeTimAnda);
        if (includeView == null) return;

        // Setup click listener untuk dropdown "Dibutuhkan"
        LinearLayout dibutuhkanHeader = includeView.findViewById(R.id.dropdown_header_dibutuhkan);
        ImageView dibutuhkanIcon = includeView.findViewById(R.id.dropdown_icon_dibutuhkan);
        LinearLayout dibutuhkanContent = includeView.findViewById(R.id.dropdown_content_dibutuhkan_container);

        if (dibutuhkanHeader != null && dibutuhkanIcon != null && dibutuhkanContent != null) {
            // Pastikan header bisa diklik
            dibutuhkanHeader.setClickable(true);
            dibutuhkanHeader.setFocusable(true);

            dibutuhkanHeader.setOnClickListener(v -> {
                boolean isVisible = dibutuhkanContent.getVisibility() == View.VISIBLE;
                dibutuhkanContent.setVisibility(isVisible ? View.GONE : View.VISIBLE);

                // Rotate icon
                if (isVisible) {
                    dibutuhkanIcon.setImageResource(R.drawable.icon_drop_down_off);
                } else {
                    dibutuhkanIcon.setImageResource(R.drawable.icon_drop_down_on);
                }
            });
        }

        // Setup click listener untuk dropdown "Informasi"
        LinearLayout informasiHeader = includeView.findViewById(R.id.dropdown_header_informasi);
        ImageView informasiIcon = includeView.findViewById(R.id.dropdown_icon_informasi);
        LinearLayout informasiContent = includeView.findViewById(R.id.dropdown_content_informasi_container);

        if (informasiHeader != null && informasiIcon != null && informasiContent != null) {
            // Pastikan header bisa diklik
            informasiHeader.setClickable(true);
            informasiHeader.setFocusable(true);

            informasiHeader.setOnClickListener(v -> {
                boolean isVisible = informasiContent.getVisibility() == View.VISIBLE;
                informasiContent.setVisibility(isVisible ? View.GONE : View.VISIBLE);

                // Rotate icon
                if (isVisible) {
                    informasiIcon.setImageResource(R.drawable.icon_drop_down_off);
                } else {
                    informasiIcon.setImageResource(R.drawable.icon_drop_down_on);
                }
            });
        }
    }

    private void setupBuatPostinganButton() {
        View includeView = findViewById(R.id.includeTimAnda);
        if (includeView == null) return;

        View btnBuatPostingan = includeView.findViewById(R.id.btnBuatPostinganRekrutTim);
        if (btnBuatPostingan != null) {
            btnBuatPostingan.setOnClickListener(v -> {
                if (currentTeam != null) {
                    Intent intent = new Intent(Detail_rekrut_team.this, Posting_rekrut_team.class);
                    intent.putExtra("team_id", teamId);
                    intent.putExtra("team_name", currentTeam.getNamaTeam());
                    intent.putExtra("tenggat_join", currentTeam.getFormattedTenggatJoinWithTime());
                    startActivity(intent);
                } else {
                    Toast.makeText(Detail_rekrut_team.this, "Data tim belum dimuat", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadTeamDetail() {
        if (teamId == null || teamId.isEmpty()) {
            Toast.makeText(this, "ID tim tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DETAIL_REKRUT", "Loading detailed data for team ID: " + teamId);

        teamController.getTeamById(teamId, new TeamController.TeamDetailCallback() {
            @Override
            public void onSuccess(Team team) {
                Log.d("DETAIL_REKRUT", "✅ Team detail loaded successfully: " + team.getNamaTeam());
                currentTeam = team;

                runOnUiThread(() -> {
                    updateTeamDetail(team);
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e("DETAIL_REKRUT", "❌ Failed to load team detail: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(Detail_rekrut_team.this,
                            "Gagal memuat detail tim: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateTeamDetail(Team team) {
        try {
            View includeView = findViewById(R.id.includeTimAnda);
            if (includeView == null) return;

            // Update basic info
            TextView memberCount = includeView.findViewById(R.id.member_count_1);
            TextView teamNameView = includeView.findViewById(R.id.team_name_1);
            TextView deadlineView = includeView.findViewById(R.id.member_limit_2);
            TextView statusView = includeView.findViewById(R.id.team_category);

            if (memberCount != null) {
                String memberText = team.getMemberSaatIni() + "/" + team.getMaxAnggota();
                memberCount.setText(memberText);
            }

            if (teamNameView != null) {
                teamNameView.setText(team.getNamaTeam());
            }

            if (deadlineView != null) {
                String formattedDate = formatDate(team.getFormattedTenggatJoinWithTime());
                deadlineView.setText(formattedDate);
            }

            if (statusView != null) {
                try {
                    int current = Integer.parseInt(team.getMemberSaatIni());
                    int max = Integer.parseInt(team.getMaxAnggota());
                    int needed = max - current;

                    if (needed > 0) {
                        statusView.setText("Butuh " + needed + " Member");
                        statusView.setTextColor(Color.parseColor("#FF5E8BFF"));
                    } else {
                        statusView.setText("Tim Penuh");
                        statusView.setTextColor(Color.parseColor("#FFE74C3C"));
                    }
                } catch (NumberFormatException e) {
                    statusView.setText("Member dibutuhkan");
                    statusView.setTextColor(Color.parseColor("#FF5E8BFF"));
                }
            }

            // Setup dropdown "Dibutuhkan" dengan data dari tim
            setupDropdownDibutuhkan(includeView, team);

            // Setup dropdown "Informasi" dengan data dari tim
            setupDropdownInformasi(includeView, team);

        } catch (Exception e) {
            Log.e("DETAIL_REKRUT", "Error updating team detail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupDropdownDibutuhkan(View includeView, Team team) {
        try {
            LinearLayout contentContainer = includeView.findViewById(R.id.dropdown_content_dibutuhkan_container);
            LinearLayout detailKetentuan = includeView.findViewById(R.id.detail_ketentuan_container);

            if (contentContainer != null) {
                // Clear existing content except detail_ketentuan_container
                int childCount = contentContainer.getChildCount();
                for (int i = childCount - 1; i >= 0; i--) {
                    View child = contentContainer.getChildAt(i);
                    if (child.getId() != R.id.detail_ketentuan_container) {
                        contentContainer.removeViewAt(i);
                    }
                }

                // Add role requirements
                String roleRequired = team.getRoleRequired();
                if (roleRequired != null && !roleRequired.isEmpty()) {
                    String[] roles = roleRequired.split(",");

                    for (String role : roles) {
                        String trimmedRole = role.trim();
                        if (!trimmedRole.isEmpty()) {
                            LinearLayout roleLayout = createRoleLayout(trimmedRole);
                            contentContainer.addView(roleLayout,
                                    Math.max(0, contentContainer.indexOfChild(detailKetentuan)));
                        }
                    }

                    // Update ketentuan
                    updateKetentuanFromDatabase(team, detailKetentuan);

                    // Add bonus section
                    createBonusSectionSimple(contentContainer, team);
                }
            }
        } catch (Exception e) {
            Log.e("DROPDOWN_DIBUTUHKAN", "Error setting up dropdown dibutuhkan", e);
        }
    }

    private LinearLayout createRoleLayout(String role) {
        LinearLayout roleLayout = new LinearLayout(this);
        roleLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        roleLayout.setOrientation(LinearLayout.HORIZONTAL);
        roleLayout.setGravity(Gravity.CENTER_VERTICAL);
        roleLayout.setPadding(0, 0, 0, dp(6));

        // TextView untuk nama role
        TextView roleText = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        roleText.setLayoutParams(textParams);
        roleText.setText(role);
        roleText.setTextColor(0xB0FFFFFF);
        roleText.setTextSize(14);
        roleText.setTypeface(getResources().getFont(R.font.is_r));

        // Nonaktifkan klik pada textview agar tidak mengganggu klik header
        roleText.setClickable(false);
        roleText.setFocusable(false);

        // TextView untuk jumlah orang
        TextView countText = new TextView(this);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        countParams.setMargins(dp(10), 0, 0, 0);
        countText.setLayoutParams(countParams);
        countText.setText("1 orang");
        countText.setTextColor(0xFF5E8BFF);
        countText.setTextSize(13);
        countText.setTypeface(getResources().getFont(R.font.is_sb));

        // Nonaktifkan klik pada textview
        countText.setClickable(false);
        countText.setFocusable(false);

        // Set background
        countText.setBackgroundResource(R.drawable.custom_bg_biru_tipis);
        int padding = dp(10);
        countText.setPadding(padding, dp(6), padding, dp(6));

        roleLayout.addView(roleText);
        roleLayout.addView(countText);

        return roleLayout;
    }

    private void updateKetentuanFromDatabase(Team team, LinearLayout detailKetentuan) {
        if (detailKetentuan == null) {
            Log.e("KETENTUAN", "detailKetentuan is null");
            return;
        }

        try {
            Log.d("KETENTUAN_DEBUG", "=== UPDATE KETENTUAN START ===");
            Log.d("KETENTUAN_DEBUG", "Team: " + team.getNamaTeam());
            Log.d("KETENTUAN_DEBUG", "Raw ketentuan from DB: " + team.getKetentuan());

            List<String> ketentuanList = team.getKetentuanList();
            Log.d("KETENTUAN_DEBUG", "Ketentuan list size: " + ketentuanList.size());

            int[] ketentuanIds = {R.id.Kt1, R.id.Kt2, R.id.Kt3, R.id.Kt4};

            for (int id : ketentuanIds) {
                TextView tv = detailKetentuan.findViewById(id);
                if (tv != null) {
                    tv.setVisibility(View.GONE);
                    // Nonaktifkan klik pada textview ketentuan
                    tv.setClickable(false);
                    tv.setFocusable(false);
                    Log.d("KETENTUAN_DEBUG", "Hiding TextView ID: " + id);
                }
            }

            if (!ketentuanList.isEmpty()) {
                for (int i = 0; i < Math.min(ketentuanList.size(), ketentuanIds.length); i++) {
                    String ketentuan = ketentuanList.get(i);
                    TextView tv = detailKetentuan.findViewById(ketentuanIds[i]);

                    if (tv != null) {
                        tv.setText(ketentuan);
                        tv.setVisibility(View.VISIBLE);
                        Log.d("KETENTUAN_DEBUG", "Updated TextView ID " + ketentuanIds[i] + " with: " + ketentuan);
                    }
                }

                detailKetentuan.setVisibility(View.VISIBLE);
                Log.d("KETENTUAN_DEBUG", "Detail ketentuan container VISIBLE");

            } else {
                detailKetentuan.setVisibility(View.GONE);
                Log.d("KETENTUAN_DEBUG", "Detail ketentuan container GONE (no data)");
            }

            Log.d("KETENTUAN_DEBUG", "=== UPDATE KETENTUAN END ===");

        } catch (Exception e) {
            Log.e("KETENTUAN", "Error updating ketentuan from database", e);
            e.printStackTrace();
            detailKetentuan.setVisibility(View.GONE);
        }
    }

    private void createBonusSectionSimple(LinearLayout container, Team team) {
        try {
            // Layout utama bonus (dengan margin top 10dp)
            LinearLayout bonusLayout = new LinearLayout(this);

            LinearLayout.LayoutParams bonusParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
            bonusParams.setMargins(0, dp(10), 0, 0); // MARGIN TOP 10dp
            bonusLayout.setLayoutParams(bonusParams);

            bonusLayout.setOrientation(LinearLayout.HORIZONTAL);
            bonusLayout.setGravity(Gravity.CENTER_VERTICAL);
            bonusLayout.setPadding(dp(10), dp(10), dp(10), dp(10));
            bonusLayout.setBackgroundResource(R.drawable.custom_bg_biru_tipis);

            // Nonaktifkan klik pada bonus layout
            bonusLayout.setClickable(false);
            bonusLayout.setFocusable(false);

            // ================= LEFT PART =================
            LinearLayout leftPart = new LinearLayout(this);
            leftPart.setOrientation(LinearLayout.HORIZONTAL);
            leftPart.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            leftPart.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            leftPart.setClickable(false);
            leftPart.setFocusable(false);

            ImageView icon = new ImageView(this);
            icon.setImageResource(R.drawable.icon_bintang);
            icon.setPadding(0, 0, dp(6), 0);
            icon.setClickable(false);
            icon.setFocusable(false);
            leftPart.addView(icon);

            TextView bonusLabel = new TextView(this);
            bonusLabel.setText("Bonus : ");
            bonusLabel.setTextColor(0xFF5E8BFF);
            bonusLabel.setTextSize(14);
            bonusLabel.setTypeface(getResources().getFont(R.font.is_sb));
            bonusLabel.setClickable(false);
            bonusLabel.setFocusable(false);
            leftPart.addView(bonusLabel);

            // Bonus text
            TextView bonusText = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            textParams.setMargins(dp(0), 0, 0, 0);
            bonusText.setLayoutParams(textParams);

            String bonusContent = team.getKeteranganTambahan();
            bonusText.setText(
                    (bonusContent != null && !bonusContent.trim().isEmpty())
                            ? bonusContent.trim()
                            : "Bergabung untuk pengalaman berharga dan networking"
            );

            bonusText.setTextColor(Color.WHITE);
            bonusText.setTextSize(14);
            bonusText.setTypeface(getResources().getFont(R.font.is_r));
            bonusText.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            bonusText.setMaxLines(3);
            bonusText.setEllipsize(TextUtils.TruncateAt.END);
            bonusText.setClickable(false);
            bonusText.setFocusable(false);

            bonusLayout.addView(leftPart);
            bonusLayout.addView(bonusText);

            if (container.findViewById(R.id.detail_ketentuan_container) != null) {
                int index = container.indexOfChild(container.findViewById(R.id.detail_ketentuan_container)) + 1;
                container.addView(bonusLayout, index);
            } else {
                container.addView(bonusLayout);
            }

        } catch (Exception e) {
            Log.e("BONUS_SECTION", "Error creating bonus section", e);
        }
    }

    private void setupDropdownInformasi(View includeView, Team team) {
        try {
            LinearLayout contentContainer = includeView.findViewById(R.id.dropdown_content_informasi_container);

            if (contentContainer != null) {
                // Setup data lomba dari tim
                setupLombaData(contentContainer, team);
            }
        } catch (Exception e) {
            Log.e("DROPDOWN_INFORMASI", "Error setting up dropdown informasi", e);
        }
    }

    private void setupLombaData(LinearLayout container, Team team) {
        try {
            Log.d("DETAIL_REKRUT", "📊 Setting up lomba data for team: " + team.getNamaTeam());

            // Cari semua TextView di container untuk mengisi data
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);

                if (child instanceof TextView) {
                    TextView textView = (TextView) child;
                    String currentText = textView.getText().toString();

                    // Debug log
                    Log.d("DETAIL_REKRUT", "📝 Found TextView with text: " + currentText);

                    // Isi data berdasarkan teks yang ada
                    if (currentText.equals("NIFC 3.0") ||
                            (textView.getId() == R.id.et_namaPenyelenggara &&
                                    currentText.equals("Nama Penyelenggara"))) {
                        // Isi nama lomba dari tim
                        String namaKegiatan = team.getNamaKegiatan();
                        Log.d("DETAIL_REKRUT", "📋 Nama kegiatan from team: " + namaKegiatan);

                        if (namaKegiatan != null && !namaKegiatan.isEmpty() &&
                                !namaKegiatan.equals("null") && !namaKegiatan.equals("NULL")) {
                            textView.setText(namaKegiatan);
                            Log.d("DETAIL_REKRUT", "✅ Set nama kegiatan: " + namaKegiatan);
                        } else {
                            textView.setText("Nama Kegiatan");
                            Log.d("DETAIL_REKRUT", "⚠ Nama kegiatan kosong, using default");
                        }
                    }
                    else if (currentText.equals("Nama Penyelenggara") ||
                            currentText.contains("Penyelanggara")) {
                        // Isi penyelenggara jika ada data
                        textView.setText("Penyelenggara Tim");
                    }
                    else if (currentText.equals("Link Lomba")) {
                        // Setup link lomba jika ada
                        setupLinkLomba(textView, team);
                    }
                    else if (currentText.contains("Uang Tunai") ||
                            currentText.contains("Sertifikat") ||
                            currentText.contains("Pengalaman")) {
                        // Setup hadiah dari database
                        setupHadiahFromDatabase(textView, team);
                    }
                }
                else if (child instanceof LinearLayout) {
                    LinearLayout linearLayout = (LinearLayout) child;

                    // Cek apakah ini layout hadiah (biasanya memiliki background custom_bg_biru_tipis)
                    if (linearLayout.getBackground() != null) {
                        // Cari TextView di dalam layout hadiah
                        for (int j = 0; j < linearLayout.getChildCount(); j++) {
                            View innerChild = linearLayout.getChildAt(j);
                            if (innerChild instanceof TextView) {
                                TextView innerText = (TextView) innerChild;
                                String innerTextStr = innerText.getText().toString();

                                if (innerTextStr.contains("Hadiah") ||
                                        innerTextStr.contains("Uang Tunai") ||
                                        innerTextStr.contains("Sertifikat")) {
                                    // Setup hadiah dari database
                                    setupHadiahFromDatabase(innerText, team);
                                }
                            }
                        }
                    }
                }
            }

            // Coba juga mencari TextView dengan ID spesifik
            TextView namaKegiatanText = container.findViewById(R.id.et_namaPenyelenggara);
            if (namaKegiatanText != null) {
                String namaKegiatan = team.getNamaKegiatan();
                if (namaKegiatan != null && !namaKegiatan.isEmpty() &&
                        !namaKegiatan.equals("null") && !namaKegiatan.equals("NULL")) {
                    namaKegiatanText.setText(namaKegiatan);
                }
            }

            Log.d("DETAIL_REKRUT", "✅ Lomba data setup completed");

        } catch (Exception e) {
            Log.e("DETAIL_REKRUT", "❌ Error setting up lomba data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupHadiahFromDatabase(TextView hadiahTextView, Team team) {
        try {
            Log.d("HADIAH_DEBUG", "=== SETUP HADIAH FROM DATABASE ===");
            Log.d("HADIAH_DEBUG", "Team: " + team.getNamaTeam());
            Log.d("HADIAH_DEBUG", "Raw hadiah from DB: " + team.getHadiah());

            // 🔥 CEK JIKA HADIAH NULL/KOSONG
            String rawHadiah = team.getHadiah();
            if (rawHadiah == null || rawHadiah.isEmpty() ||
                    rawHadiah.equalsIgnoreCase("null") || rawHadiah.trim().isEmpty()) {

                Log.d("HADIAH_DEBUG", "Hadiah is empty/null, showing 'Tidak ada hadiah'");
                hadiahTextView.setText("Tidak ada hadiah"); // 🔥 TAMPILKAN INI
                return;
            }

            List<String> hadiahList = team.getHadiahList();
            Log.d("HADIAH_DEBUG", "Hadiah list size: " + hadiahList.size());

            if (!hadiahList.isEmpty()) {
                // Format 1: Satu baris dengan pemisah
                if (hadiahList.size() <= 3) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < hadiahList.size(); i++) {
                        builder.append(hadiahList.get(i));
                        if (i < hadiahList.size() - 1) {
                            builder.append(" + ");
                        }
                    }
                    hadiahTextView.setText(builder.toString());
                }
                // Format 2: Multi-line dengan bullet points
                else {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < Math.min(hadiahList.size(), 4); i++) {
                        builder.append("• ").append(hadiahList.get(i));
                        if (i < Math.min(hadiahList.size(), 4) - 1) {
                            builder.append("\n");
                        }
                    }
                    hadiahTextView.setText(builder.toString());
                    hadiahTextView.setSingleLine(false);
                    hadiahTextView.setMaxLines(4);
                }

                Log.d("HADIAH_DEBUG", "Formatted hadiah: " + hadiahTextView.getText());
            } else {
                // 🔥 JIKA LIST KOSONG SETELAH PARSING
                hadiahTextView.setText("Tidak ada hadiah");
                Log.d("HADIAH_DEBUG", "Hadiah list is empty after parsing, showing 'Tidak ada hadiah'");
            }

            Log.d("HADIAH_DEBUG", "=== END SETUP HADIAH ===");

        } catch (Exception e) {
            Log.e("HADIAH_SETUP", "Error setting up hadiah from database", e);
            hadiahTextView.setText("Tidak ada hadiah"); // 🔥 FALLBACK
        }
    }

    private void setupLinkLomba(TextView linkTextView, Team team) {
        // Set default text
        linkTextView.setText("Link Informasi Lomba");

        // Aktifkan klik untuk link lomba
        linkTextView.setClickable(true);
        linkTextView.setFocusable(true);

        // Tambahkan click listener untuk membuka link
        linkTextView.setOnClickListener(v -> {
            Toast.makeText(this, "Membuka informasi lomba", Toast.LENGTH_SHORT).show();
        });
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty() ||
                dateString.equals("null") || dateString.equals("0000-00-00")) {
            return "Belum ditentukan";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = inputFormat.parse(dateString);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            return outputFormat.format(date);
        } catch (Exception e) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                Date date = inputFormat.parse(dateString);

                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
                return outputFormat.format(date);
            } catch (Exception e2) {
                return dateString;
            }
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void showPopupMenu(View anchor) {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        View popupView = LayoutInflater.from(this)
                .inflate(R.layout.custom_popup_detail_tim, null);

        popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(10f);

        View editForumBtn = popupView.findViewById(R.id.editforum);
        editForumBtn.setOnClickListener(v -> {
            if (popupWindow != null) {
                popupWindow.dismiss();
            }

            if (currentTeam != null) {
                Intent intent = new Intent(Detail_rekrut_team.this, Edit_Tim.class);
                // Pass team data to Edit_Tim
                intent.putExtra("team_id", teamId);
                intent.putExtra("team_name", currentTeam.getNamaTeam());
                intent.putExtra("max_anggota", currentTeam.getMaxAnggota());
                intent.putExtra("tenggat_join", currentTeam.getFormattedTenggatJoinWithTime());
                intent.putExtra("role_required", currentTeam.getRoleRequired());
                intent.putExtra("deskripsi", currentTeam.getDeskripsiAnggota());
                intent.putExtra("keterangan_tambahan", currentTeam.getKeteranganTambahan());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Data tim belum dimuat", Toast.LENGTH_SHORT).show();
            }
        });

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);

        int marginRight = dp(-10);
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();

        int posX = location[0] - popupWidth - marginRight;
        int posY = location[1];

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, posX, posY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from Edit_Tim
        if (teamId != null && !teamId.isEmpty()) {
            loadTeamDetail();
        }
    }
}