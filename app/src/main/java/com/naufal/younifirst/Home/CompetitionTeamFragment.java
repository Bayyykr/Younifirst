package com.naufal.younifirst.Home;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.naufal.younifirst.R;
import com.naufal.younifirst.controller.KompetisiController;
import com.naufal.younifirst.controller.TeamController;
import com.naufal.younifirst.model.Kompetisi;
import com.naufal.younifirst.model.Team;

import java.util.List;

public class CompetitionTeamFragment extends Fragment {

    private LinearLayout tabKompetisi, tabTim;
    private TextView textKompetisi, textTim;
    private View underlineKompetisi, underlineTim;
    private LinearLayout containerCompetition;

    private KompetisiController kompetisiController;
    private TeamController teamController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_competition_team, container, false);

        // Initialize controllers
        kompetisiController = new KompetisiController();
        teamController = new TeamController();

        // Initialize tabs
        initializeTabs(view);

        // Set default tab
        setKompetisiActive();
        loadKompetisiData();

        return view;
    }

    private void initializeTabs(View view) {
        tabKompetisi = view.findViewById(R.id.tab_kompetisi);
        tabTim = view.findViewById(R.id.tab_tim);
        textKompetisi = view.findViewById(R.id.text_kompetisi);
        textTim = view.findViewById(R.id.text_tim);
        underlineKompetisi = view.findViewById(R.id.underline_kompetisi);
        underlineTim = view.findViewById(R.id.underline_tim);
        containerCompetition = view.findViewById(R.id.container_competition);

        // Tab listeners
        tabKompetisi.setOnClickListener(v -> {
            setKompetisiActive();
            loadKompetisiData();
        });

        tabTim.setOnClickListener(v -> {
            setTimActive();
            loadTeamsData();
        });
    }

    private void loadKompetisiData() {
        Log.d("LOAD_DATA", "Starting to load kompetisi data...");

        kompetisiController.loadKompetisiData(new KompetisiController.KompetisiCallback() {
            @Override
            public void onSuccess(List<Kompetisi> competitions) {
                Log.d("LOAD_DATA", "onSuccess called! Total confirmed competitions: " + competitions.size());

                // Log semua data yang diterima
                for (int i = 0; i < competitions.size(); i++) {
                    Kompetisi k = competitions.get(i);
                    Log.d("LOAD_DATA", "Competition " + i + ": " + k.getNamaLomba() +
                            " | Status: " + k.getStatus());
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d("LOAD_DATA", "Running on UI thread...");

                        // Cek apakah ada data
                        if (competitions.isEmpty()) {
                            Toast.makeText(getContext(),
                                    "Tidak ada kompetisi yang terkonfirmasi",
                                    Toast.LENGTH_SHORT).show();
                            // Optional: tampilkan empty state
                            showEmptyState();
                        } else {
                            setupCompetitionLayout(competitions);
                        }
                    });
                } else {
                    Log.e("LOAD_DATA", "Activity is NULL!");
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("LOAD_DATA", "onFailure: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    // Method optional untuk tampilkan empty state
    private void showEmptyState() {
        containerCompetition.removeAllViews();

        // Buat simple empty state view
        TextView emptyText = new TextView(getContext());
        emptyText.setText("Belum ada kompetisi yang terkonfirmasi");
        emptyText.setTextColor(Color.WHITE);
        emptyText.setGravity(android.view.Gravity.CENTER);
        emptyText.setPadding(20, 50, 20, 50);

        containerCompetition.addView(emptyText);
    }

    private void showEmptyState(String message) {
        containerCompetition.removeAllViews();

        // Buat simple empty state view dengan pesan custom
        TextView emptyText = new TextView(getContext());
        emptyText.setText(message);
        emptyText.setTextColor(Color.WHITE);
        emptyText.setGravity(android.view.Gravity.CENTER);
        emptyText.setPadding(20, 50, 20, 50);

        containerCompetition.addView(emptyText);
    }

    private void loadTeamsData() {
        Log.d("TEAM_FRAGMENT", "Starting to load teams data...");

        teamController.loadTeamsData(new TeamController.TeamCallback() {
            @Override
            public void onSuccess(List<Team> teams) {
                Log.d("TEAM_FRAGMENT", "Teams data loaded successfully. Total teams: " + teams.size());

                // Update UI di main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (teams.isEmpty()) {
                            showEmptyState("Belum ada tim yang terdaftar");
                        } else {
                            setupTeamLayout(teams);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("TEAM_FRAGMENT", "Failed to load teams: " + error);
                // Update UI di main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showEmptyState("Gagal memuat data tim: " + error);
                    });
                }
            }
        });
    }

    private void setupCompetitionLayout(List<Kompetisi> competitions) {
        // Inflate layout kompetisi utama
        containerCompetition.removeAllViews();
        View kompetisiView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_competition_utama, containerCompetition, false);
        containerCompetition.addView(kompetisiView);

        // Setup horizontal upcoming competitions
        setupUpcomingCompetitions(kompetisiView, competitions);

        // Setup vertical competition list
        setupCompetitionList(kompetisiView, competitions);
    }

    private void setupUpcomingCompetitions(View parentView, List<Kompetisi> competitions) {
        LinearLayout containerUpcoming = parentView.findViewById(R.id.horizontal_container_upcoming);
        if (containerUpcoming != null) {
            containerUpcoming.removeAllViews();

            for (int i = 0; i < Math.min(competitions.size(), 5); i++) {
                Kompetisi competition = competitions.get(i);
                View itemView = LayoutInflater.from(getContext())
                        .inflate(R.layout.fragment_competition_mendatang, containerUpcoming, false);
                setupCompetitionMendatangItem(itemView, competition);
                containerUpcoming.addView(itemView);
            }
        }
    }

    private void setupCompetitionList(View parentView, List<Kompetisi> competitions) {
        Log.d("SETUP_LIST", "=== START SETUP LIST ===");
        Log.d("SETUP_LIST", "Competitions size: " + competitions.size());

        // Cari container
        LinearLayout containerList = parentView.findViewById(R.id.container_competition_list);
        Log.d("SETUP_LIST", "Container found directly: " + (containerList != null));

        if (containerList == null) {
            ScrollView scrollView = parentView.findViewById(R.id.kompetisi_utama);
            Log.d("SETUP_LIST", "ScrollView found: " + (scrollView != null));

            if (scrollView != null && scrollView.getChildCount() > 0) {
                View child = scrollView.getChildAt(0);
                if (child instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) child;
                    containerList = viewGroup.findViewById(R.id.container_competition_list);
                    Log.d("SETUP_LIST", "Container found in child: " + (containerList != null));
                }
            }
        }

        final LinearLayout finalContainerList = containerList;

        if (finalContainerList != null) {
            Log.d("SETUP_LIST", "Container found! Clearing views...");
            finalContainerList.removeAllViews();
            finalContainerList.setVisibility(View.VISIBLE);
            finalContainerList.setOrientation(LinearLayout.VERTICAL);

            Log.d("SETUP_LIST", "Adding " + competitions.size() + " items...");

            for (int i = 0; i < competitions.size(); i++) {
                Kompetisi competition = competitions.get(i);
                Log.d("SETUP_LIST", "Adding item " + i + ": " + competition.getNamaLomba());

                try {
                    View itemView = LayoutInflater.from(getContext())
                            .inflate(R.layout.fragment_competition_list, finalContainerList, false);

                    // Set layout params
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    int margin = (int) (10 * getResources().getDisplayMetrics().density);
                    params.setMargins(0, 0, 0, margin);
                    itemView.setLayoutParams(params);

                    // Setup data
                    setupCompetitionListItem(itemView, competition);

                    // Add to container
                    finalContainerList.addView(itemView);
                    Log.d("SETUP_LIST", "Item added. Current child count: " + finalContainerList.getChildCount());

                } catch (Exception e) {
                    Log.e("SETUP_LIST", "ERROR adding item " + i, e);
                    e.printStackTrace();
                }
            }

            // Force layout update - GUNAKAN finalContainerList
            finalContainerList.post(() -> {
                finalContainerList.requestLayout();
                finalContainerList.invalidate();
                Log.d("SETUP_LIST", "Final child count: " + finalContainerList.getChildCount());
            });

            Log.d("SETUP_LIST", "=== END SETUP LIST ===");

        } else {
            Log.e("SETUP_LIST", "CONTAINER IS NULL! Cannot display items!");
            Toast.makeText(getContext(), "Error: Container not found!", Toast.LENGTH_LONG).show();
        }
    }

    private void setupTeamLayout(List<Team> teams) {
        Log.d("TEAM_FRAGMENT", "Setting up team layout with " + teams.size() + " teams");
        containerCompetition.removeAllViews();

        try {
            View teamView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_team_utama, containerCompetition, false);
            containerCompetition.addView(teamView);
            setupTeamList(teamView, teams);
        } catch (Exception e) {
            Log.e("TEAM_FRAGMENT", "Error inflating team layout", e);
            showEmptyState("Error loading team layout");
        }
    }

    private void setupTeamList(View parentView, List<Team> teams) {
        CardView containerTeamList = parentView.findViewById(R.id.container_team_utama);
        if (containerTeamList != null) {
            containerTeamList.removeAllViews();

            for (Team team : teams) {
                View itemView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_tim_kompetisi, containerTeamList, false); // Pastikan menggunakan layout yang benar
                setupTeamItem(itemView, team);
                containerTeamList.addView(itemView);
            }
        } else {
            Log.e("TEAM_FRAGMENT", "Team list container not found!");
        }
    }

    private void setupCompetitionListItem(View itemView, Kompetisi competition) {
        try {
            // Pastikan root layout visible
            itemView.setVisibility(View.VISIBLE);

            // Judul
            TextView title = itemView.findViewById(R.id.text_judul_list);
            if (title != null) {
                title.setText(competition.getNamaLomba());
                Log.d("COMPETITION_ITEM", "Set title: " + competition.getNamaLomba());
            } else {
                Log.e("COMPETITION_ITEM", "Title view is null!");
            }

            // Tanggal
            TextView tanggal = itemView.findViewById(R.id.text_tanggal_list);
            if (tanggal != null) {
                tanggal.setText(formatDate(competition.getTanggalLomba()));
            }

            // Lokasi
            TextView lokasi = itemView.findViewById(R.id.text_lokasi_list);
            if (lokasi != null) {
                lokasi.setText(competition.getLokasi());
            }

            // Poster
            ImageView poster = itemView.findViewById(R.id.POSTER_KONTOL);
            if (poster != null && getContext() != null) {
                String posterUrl = competition.getPoster();
                if (posterUrl != null && !posterUrl.isEmpty()) {
                    Glide.with(this)
                            .load(posterUrl)
                            .placeholder(R.drawable.tryposter)
                            .error(R.drawable.tryposter)
                            .into(poster);
                } else {
                    poster.setImageResource(R.drawable.tryposter);
                }
            }

            // Status & Scope
            TextView status = itemView.findViewById(R.id.text_status_list);
            if (status != null) {
                status.setText(competition.getStatus());
            }

            TextView scope = itemView.findViewById(R.id.text_scope_list);
            if (scope != null) {
                scope.setText(competition.getScope());
            }

            // Setup badges
            setupBadges(itemView, competition.getKategori(), R.id.badge_container_list);

        } catch (Exception e) {
            Log.e("COMPETITION_ITEM", "Error setting up competition list item: " + competition.getNamaLomba(), e);
            e.printStackTrace();
        }
    }

    private void setupCompetitionMendatangItem(View itemView, Kompetisi competition) {
        try {
            // Judul
            TextView title = itemView.findViewById(R.id.text_judul_mendatang);
            if (title != null) {
                title.setText(competition.getNamaLomba());
            }

            // Tanggal
            TextView tanggal = itemView.findViewById(R.id.text_tanggal_mendatang);
            if (tanggal != null) {
                tanggal.setText(formatDate(competition.getTanggalLomba()));
            }

            // Lokasi
            TextView lokasi = itemView.findViewById(R.id.text_lokasi_mendatang);
            if (lokasi != null) {
                lokasi.setText(competition.getLokasi());
            }

            // Poster
            ImageView poster = itemView.findViewById(R.id.img_poster_mendatang);
            if (poster != null && getContext() != null) {
                String posterUrl = competition.getPoster();
                if (posterUrl != null && !posterUrl.isEmpty()) {
                    Glide.with(this)
                            .load(posterUrl)
                            .placeholder(R.drawable.tryposter)
                            .error(R.drawable.tryposter)
                            .into(poster);
                } else {
                    poster.setImageResource(R.drawable.tryposter);
                }
            }

            // Status & Scope
            TextView status = itemView.findViewById(R.id.text_status_mendatang);
            if (status != null) {
                status.setText(competition.getStatus());
            }

            TextView scope = itemView.findViewById(R.id.text_scope_mendatang);
            if (scope != null) {
                scope.setText(competition.getScope());
            }

            // Setup badges
            setupBadges(itemView, competition.getKategori(), R.id.badge_container_mendatang);

        } catch (Exception e) {
            Log.e("COMPETITION_MENDATANG", "Error setting up competition mendatang item", e);
        }
    }

    private void setupTeamItem(View itemView, Team team) {
        try {
            // Header Section - Avatar dan Username
            ImageView chatBubbleAvatar = itemView.findViewById(R.id.chat_bubble_avatar);
            TextView textUsername = itemView.findViewById(R.id.text_username);
            TextView textTime = itemView.findViewById(R.id.text_time);

            if (textUsername != null) {
                textUsername.setText(team.getNamaTeam() != null ? team.getNamaTeam() : "Nama Team");
            }

            if (textTime != null) {
                // Anda bisa menyesuaikan waktu berdasarkan created_at dari team
                textTime.setText(team.getWaktuPost() != null ? formatTime(team.getWaktuPost()   ) : "Baru saja");
            }

            // Avatar/Logo Team
            if (chatBubbleAvatar != null && getContext() != null) {
                String logoUrl = team.getAvatarPemilik();
                if (logoUrl != null && !logoUrl.isEmpty()) {
                    Glide.with(this)
                            .load(logoUrl)
                            .placeholder(R.drawable.gambar_profil_2)
                            .error(R.drawable.gambar_profil_2)
                            .into(chatBubbleAvatar);
                } else {
                    chatBubbleAvatar.setImageResource(R.drawable.gambar_profil_2);
                }
            }

            // Poster Section
            ImageView imgPoster = itemView.findViewById(R.id.img_poster);
            if (imgPoster != null && getContext() != null) {
                String posterUrl = team.getPoster(); // Asumsikan ada method getPoster() di Team
                if (posterUrl != null && !posterUrl.isEmpty()) {
                    Glide.with(this)
                            .load(posterUrl)
                            .placeholder(R.drawable.tryposter)
                            .error(R.drawable.tryposter)
                            .into(imgPoster);
                } else {
                    imgPoster.setImageResource(R.drawable.tryposter);
                }
            }

            // Role Section
            TextView textRole = itemView.findViewById(R.id.text_role);
            if (textRole != null) {
                // Asumsikan ada method getRole() atau getRequiredRole() di Team
                String role = team.getRoleDibutuhkan() != null ? team.getRoleDibutuhkan() : "UI/UX Designer";
                textRole.setText(role);
            }

            // Member Section
            TextView textMember = itemView.findViewById(R.id.text_member);
            if (textMember != null) {
                // Asumsikan ada method getCurrentMembers() dan getMaxMembers() di Team
                String currentMembers = team.getMemberSaatIni() != null ? team.getMemberSaatIni() : "3";
                String maxMembers = team.getMaxanggota() != null ? team.getMaxanggota() : "4";
                textMember.setText(currentMembers + "/" + maxMembers);
            }

            // Description
            TextView textDescription = itemView.findViewById(R.id.text_description);
            if (textDescription != null) {
                String description = team.getDeskripsi() != null ? team.getDeskripsi() : "Butuh 1 orang lagi yuk yang mau join!…Selengkapnya";
                textDescription.setText(description);
            }

            // Footer Stats - Like, Comment, Share
            TextView textLikeCount = itemView.findViewById(R.id.text_like_count);
            TextView textCommentCount = itemView.findViewById(R.id.text_comment_count);
            TextView textShareCount = itemView.findViewById(R.id.text_share_count);

            if (textLikeCount != null) {
                textLikeCount.setText(team.getJumlahLike() != null ? formatCount(team.getJumlahLike()) : "2,5rb");
            }

            if (textCommentCount != null) {
                textCommentCount.setText(team.getJumlahKomentar() != null ? formatCount(team.getJumlahKomentar()) : "2,5rb");
            }

            if (textShareCount != null) {
                textShareCount.setText(team.getJumlahShare() != null ? formatCount(team.getJumlahShare()) : "2,5rb");
            }

            // Join Button
            Button joinButton = itemView.findViewById(R.id.ikuttim);
            if (joinButton != null) {
                joinButton.setOnClickListener(v -> {
                    // Handle join team action
                    onJoinTeamClicked(team);
                });
            }

            // Lainnya Icon
            ImageView iconLainnya = itemView.findViewById(R.id.icon_lainnya);
            if (iconLainnya != null) {
                iconLainnya.setOnClickListener(v -> {
                    // Handle menu lainnya
                    showTeamOptionsMenu(team);
                });
            }

            Log.d("TEAM_ITEM", "Setup team: " + team.getNamaTeam());

        } catch (Exception e) {
            Log.e("TEAM_ITEM", "Error setting up team item", e);
        }
    }

    // Helper method untuk format waktu
    private String formatTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "Baru saja";
        }

        // Implementasi format waktu sesuai kebutuhan
        // Contoh sederhana:
        try {
            // Jika timestamp dalam format yang bisa di-parse, lakukan parsing di sini
            // Untuk sekarang, return as-is atau format sederhana
            return timestamp;
        } catch (Exception e) {
            return "Baru saja";
        }
    }

    // Helper method untuk format angka (like, comment, share count)
    private String formatCount(String count) {
        if (count == null || count.isEmpty()) {
            return "0";
        }

        try {
            int num = Integer.parseInt(count);
            if (num >= 1000) {
                return String.format("%.1frb", num / 1000.0);
            } else {
                return String.valueOf(num);
            }
        } catch (NumberFormatException e) {
            return count;
        }
    }

    // Method untuk handle join team
    private void onJoinTeamClicked(Team team) {
        Toast.makeText(getContext(), "Mencoba bergabung dengan tim: " + team.getNamaTeam(), Toast.LENGTH_SHORT).show();
        // Implementasi logika join team di sini
    }

    // Method untuk show options menu
    private void showTeamOptionsMenu(Team team) {
        // Implementasi menu options untuk team
        Toast.makeText(getContext(), "Menu options untuk: " + team.getNamaTeam(), Toast.LENGTH_SHORT).show();
    }

    private void setupBadges(View itemView, String kategori, int badgeContainerId) {
        try {
            LinearLayout badgeContainer = itemView.findViewById(badgeContainerId);
            if (badgeContainer != null) {
                badgeContainer.removeAllViews();

                if (kategori != null && !kategori.isEmpty()) {
                    // Split berdasarkan koma
                    String[] kategoriArray = kategori.split(",");

                    for (int j = 0; j < kategoriArray.length; j++) {
                        String kat = kategoriArray[j].trim();

                        if (!kat.isEmpty()) {
                            View badge = LayoutInflater.from(getContext())
                                    .inflate(R.layout.badge_kecil, badgeContainer, false);

                            TextView badgeText = badge.findViewById(R.id.text_badge);

                            if (badgeText != null) {
                                badgeText.setText(kat);
                            }

                            // Set background berdasarkan index atau kata kunci
                            setBadgeBackground(badge, kat, j);

                            badgeContainer.addView(badge);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("BADGES", "Error setting up badges", e);
            e.printStackTrace();
        }
    }

    // Method helper untuk set background badge
    private void setBadgeBackground(View badge, String kategori, int index) {
        int backgroundRes;

        // Atur background berdasarkan kata kunci kategori
        String katLower = kategori.toLowerCase();

        if (katLower.contains("umum") || katLower.contains("general")) {
            backgroundRes = R.drawable.badge_green;
        } else if (katLower.contains("seminar") || katLower.contains("workshop")) {
            backgroundRes = R.drawable.badge_blue;
        } else if (katLower.contains("offline") || katLower.contains("onsite")) {
            backgroundRes = R.drawable.badge_red;
        } else if (katLower.contains("mahasiswa") || katLower.contains("student")) {
            backgroundRes = R.drawable.badge_purple;
        } else {
            // Fallback: rotasi warna berdasarkan index
            int[] colors = {
                    R.drawable.badge_green,
                    R.drawable.badge_blue,
                    R.drawable.badge_red,
                    R.drawable.badge_purple
            };
            backgroundRes = colors[index % colors.length];
        }

        badge.setBackgroundResource(backgroundRes);
    }

    private String formatDate(String dateString) {
        // Your existing date formatting logic
        if (dateString == null || dateString.isEmpty()) {
            return "Date not set";
        }
        return dateString;
    }

    private void setKompetisiActive() {
        if (getContext() != null) {
            textKompetisi.setTextColor(getResources().getColor(R.color.blue_37B));
            textKompetisi.setTypeface(getResources().getFont(R.font.is_m));
            textTim.setTextColor(0x80FFFFFF);
            textTim.setTypeface(getResources().getFont(R.font.is_r));
            underlineKompetisi.setBackgroundColor(getResources().getColor(R.color.blue_37B));
            underlineTim.setBackgroundColor(0x00000000);
        }
    }

    private void setTimActive() {
        if (getContext() != null) {
            textTim.setTextColor(getResources().getColor(R.color.blue_37B));
            textTim.setTypeface(getResources().getFont(R.font.is_m));
            textKompetisi.setTextColor(0x80FFFFFF);
            textKompetisi.setTypeface(getResources().getFont(R.font.is_r));
            underlineTim.setBackgroundColor(getResources().getColor(R.color.blue_37B));
            underlineKompetisi.setBackgroundColor(0x00000000);
        }
    }
}