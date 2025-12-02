package com.naufal.younifirst.Home;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.naufal.younifirst.R;
import com.naufal.younifirst.controller.KompetisiController;
import com.naufal.younifirst.controller.TeamController;
import com.naufal.younifirst.model.Kompetisi;
import com.naufal.younifirst.model.Team;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    // Di CompetitionTeamFragment, panggil dengan cara ini:
    private void loadTeamsData() {
        Log.d("TEAM_FRAGMENT", "Starting to load teams data...");

        // Gunakan controller yang sudah diperbaiki
        teamController.loadTeamsData(new TeamController.TeamCallback() {
            @Override
            public void onSuccess(List<Team> teams) {
                Log.d("TEAM_FRAGMENT", "Teams data loaded successfully. Total teams: " + teams.size());

                // Debug: log semua teams
                for (Team team : teams) {
                    Log.d("TEAM_FRAGMENT", "Team: " + team.getNamaTeam() +
                            ", Role: " + team.getRoleRequired() +
                            ", Max: " + team.getMaxAnggota());
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (teams.isEmpty()) {
                            showEmptyState("Belum ada tim yang aktif");
                        } else {
                            setupTeamLayout(teams);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("TEAM_FRAGMENT", "Failed to load teams: " + error);
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
            // Inflate layout utama
            View teamView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_team_utama, containerCompetition, false);
            containerCompetition.addView(teamView);

            // Cari container yang benar
            LinearLayout containerTeamList = teamView.findViewById(R.id.container_team_list);

            if (containerTeamList != null) {
                Log.d("TEAM_FRAGMENT", "Found containerTeamList, adding " + teams.size() + " items");
                setupTeamList(containerTeamList, teams);
            } else {
                Log.e("TEAM_FRAGMENT", "container_team_list not found in layout!");
                showEmptyState("Layout error: container not found");
            }

        } catch (Exception e) {
            Log.e("TEAM_FRAGMENT", "Error inflating team layout", e);
            showEmptyState("Error loading team layout");
        }
    }

    private void setupTeamList(LinearLayout container, List<Team> teams) {
        if (container != null) {
            // Clear existing views
            container.removeAllViews();

            Log.d("TEAM_FRAGMENT", "Setting up " + teams.size() + " teams in container");

            for (int i = 0; i < teams.size(); i++) {
                Team team = teams.get(i);
                Log.d("TEAM_FRAGMENT", "Creating item " + i + ": " + team.getNamaTeam());

                try {
                    // Inflate item layout
                    View itemView = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_tim_kompetisi, container, false);

                    // Set layout params dengan margin
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    int margin = (int) (10 * getResources().getDisplayMetrics().density);
                    params.setMargins(0, 0, 0, margin);
                    itemView.setLayoutParams(params);

                    // Setup data team
                    setupTeamItem(itemView, team);

                    // Add to container
                    container.addView(itemView);
                    Log.d("TEAM_FRAGMENT", "Added item " + i + " to container");

                } catch (Exception e) {
                    Log.e("TEAM_FRAGMENT", "Error creating team item " + i, e);
                    e.printStackTrace();
                }
            }

            // Force layout update
            container.post(() -> {
                container.requestLayout();
                container.invalidate();
                Log.d("TEAM_FRAGMENT", "Final child count in container: " + container.getChildCount());
            });

        } else {
            Log.e("TEAM_FRAGMENT", "Team list container is null!");
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

            // Poster - PERBAIKAN: ID yang benar
            ImageView poster = itemView.findViewById(R.id.poster_lomba);
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
            } else {
                Log.e("COMPETITION_ITEM", "Poster view is null or context is null");
            }

            // Biaya (Berbayar/Gratis) - TAMBAHKAN
            TextView biaya = itemView.findViewById(R.id.text_biaya);
            if (biaya != null) {
                String biayaText = competition.getBiaya();
                if (biayaText != null && !biayaText.isEmpty()) {
                    if (biayaText.equalsIgnoreCase("0") ||
                            biayaText.equalsIgnoreCase("gratis") ||
                            biayaText.equalsIgnoreCase("free")) {
                        biaya.setText("Gratis");
                    } else {
                        biaya.setText("Berbayar");
                    }
                } else {
                    biaya.setText("Berbayar"); // Default
                }
            }

            // Scope - TAMBAHKAN
            TextView scope = itemView.findViewById(R.id.text_scope);
            if (scope != null) {
                scope.setText(competition.getScope());
            }

            // Tipe Lomba (Individu/Kelompok) dan Ikon - TAMBAHKAN
            LinearLayout layoutParticipant = itemView.findViewById(R.id.layout_participant_list);
            ImageView iconKelompok = itemView.findViewById(R.id.icon_lomba_type_kelompok);
            ImageView iconIndividu = itemView.findViewById(R.id.icon_lomba_type_individu);
            TextView lombaType = itemView.findViewById(R.id.text_lomba_type);

            if (layoutParticipant != null && lombaType != null) {
                String type = competition.getLomba_type();

                // Set default visibility
                if (iconKelompok != null) iconKelompok.setVisibility(View.GONE);
                if (iconIndividu != null) iconIndividu.setVisibility(View.GONE);

                if (type != null && !type.isEmpty()) {
                    lombaType.setText(type);

                    // Tampilkan ikon sesuai tipe
                    if (type.equalsIgnoreCase("Kelompok") || type.equalsIgnoreCase("Team")) {
                        if (iconKelompok != null) {
                            iconKelompok.setVisibility(View.VISIBLE);
                            iconKelompok.setImageResource(R.drawable.icon_people);
                        }
                    } else if (type.equalsIgnoreCase("Individu") || type.equalsIgnoreCase("Individual")) {
                        if (iconIndividu != null) {
                            iconIndividu.setVisibility(View.VISIBLE);
                            iconIndividu.setImageResource(R.drawable.icon_orang);
                        }
                    }
                } else {
                    lombaType.setText("Individu"); // Default
                    if (iconIndividu != null) {
                        iconIndividu.setVisibility(View.VISIBLE);
                        iconIndividu.setImageResource(R.drawable.icon_orang);
                    }
                }
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
            } else {
                Log.e("COMPETITION_ITEM", "Poster view is null or context is null");
            }

            // Biaya (Berbayar/Gratis)
            TextView biaya = itemView.findViewById(R.id.text_biaya);
            if (biaya != null) {
                // Ambil nilai biaya dari model Kompetisi
                String biayaText = competition.getBiaya();
                if (biayaText != null && !biayaText.isEmpty()) {
                    // Cek apakah berbayar atau gratis
                    if (biayaText.equalsIgnoreCase("0") ||
                            biayaText.equalsIgnoreCase("gratis") ||
                            biayaText.equalsIgnoreCase("free")) {
                        biaya.setText("Gratis");
                    } else {
                        biaya.setText("Berbayar");
                    }
                } else {
                    biaya.setText("Berbayar"); // Default jika tidak ada data
                }
            }

            // Scope
            TextView scope = itemView.findViewById(R.id.text_scope);
            Log.d("SCOPE_ITEM", "Setup competition: " + competition.getScope());
            if (scope != null) {
                scope.setText(competition.getScope());
            }

            // Tipe Lomba (Individu/Kelompok) dan Ikon
            LinearLayout layoutParticipant = itemView.findViewById(R.id.layout_participant_mendatang);
            ImageView iconKelompok = itemView.findViewById(R.id.icon_lomba_type_kelompok);
            ImageView iconIndividu = itemView.findViewById(R.id.icon_lomba_type_individu);
            TextView lombaType = itemView.findViewById(R.id.text_lomba_type);

            if (layoutParticipant != null && lombaType != null) {
                String type = competition.getLomba_type();

                // Set default visibility
                if (iconKelompok != null) iconKelompok.setVisibility(View.GONE);
                if (iconIndividu != null) iconIndividu.setVisibility(View.GONE);

                if (type != null && !type.isEmpty()) {
                    lombaType.setText(type);

                    // Tampilkan ikon sesuai tipe
                    if (type.equalsIgnoreCase("Kelompok") || type.equalsIgnoreCase("Team")) {
                        if (iconKelompok != null) {
                            iconKelompok.setVisibility(View.VISIBLE);
                            iconKelompok.setImageResource(R.drawable.icon_people);
                        }
                    } else if (type.equalsIgnoreCase("Individu") || type.equalsIgnoreCase("Individual")) {
                        if (iconIndividu != null) {
                            iconIndividu.setVisibility(View.VISIBLE);
                            iconIndividu.setImageResource(R.drawable.icon_orang);
                        }
                    }
                } else {
                    lombaType.setText("Individu"); // Default
                    if (iconIndividu != null) {
                        iconIndividu.setVisibility(View.VISIBLE);
                        iconIndividu.setImageResource(R.drawable.icon_orang);
                    }
                }
            }

            // Setup badges berdasarkan kategori
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
                textTime.setText(team.getWaktuPost() != null ? formatTime(team.getWaktuPost()) : "Baru saja");
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
                String posterUrl = team.getPoster();
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

            // Role Section (menggunakan role_required dari database)
            TextView textRole = itemView.findViewById(R.id.text_role);
            if (textRole != null) {
                String roleRequired = team.getRoleRequired();
                if (roleRequired != null && !roleRequired.isEmpty()) {
                    textRole.setText(roleRequired);
                } else {
                    // Fallback ke role jika role_required kosong
                    textRole.setText(team.getRole() != null ? team.getRole() : "UI/UX Designer");
                }
            }

            // Member Section (menggunakan max_anggota dari database)
            TextView textMember = itemView.findViewById(R.id.text_member);
            if (textMember != null) {
                String maxAnggota = team.getMaxAnggota();
                if (maxAnggota != null && !maxAnggota.isEmpty()) {
                    // Anggap ada 0 member saat ini (bisa diubah sesuai kebutuhan)
                    textMember.setText("0/" + maxAnggota);
                } else {
                    textMember.setText("0/4"); // Default
                }
            }

            // Description (menggunakan deskripsi_anggota dari database)
            TextView textDescription = itemView.findViewById(R.id.text_description);
            if (textDescription != null) {
                String deskripsi = team.getDeskripsiAnggota();
                if (deskripsi != null && !deskripsi.isEmpty()) {
                    textDescription.setText(deskripsi);
                } else {
                    textDescription.setText("Butuh anggota untuk bergabung dengan tim...");
                }
            }

            // Nama Kegiatan (tambahkan jika ada TextView untuk ini)
//            TextView textKegiatan = itemView.findViewById(); /
//            if (textKegiatan != null) {
//                textKegiatan.setText(team.getNamaKegiatan() != null ? team.getNamaKegiatan() : "");
//            }

            // Footer Stats
            TextView textLikeCount = itemView.findViewById(R.id.text_like_count);
            TextView textCommentCount = itemView.findViewById(R.id.text_comment_count);
            TextView textShareCount = itemView.findViewById(R.id.text_share_count);

            if (textLikeCount != null) {
                textLikeCount.setText(formatCount(team.getJumlahLike()));
            }

            if (textCommentCount != null) {
                textCommentCount.setText(formatCount(team.getJumlahKomentar()));
            }

            if (textShareCount != null) {
                textShareCount.setText(formatCount(team.getJumlahShare()));
            }

            // Join Button
            Button joinButton = itemView.findViewById(R.id.ikuttim);
            if (joinButton != null) {
                joinButton.setOnClickListener(v -> {
                    onJoinTeamClicked(team);
                });
            }

            // Status Badge (jika ingin menampilkan status)
            setupStatusBadge(itemView, team.getStatus());

            Log.d("TEAM_ITEM", "Setup team: " + team.getNamaTeam() +
                    " | Role: " + team.getRoleRequired() +
                    " | Max: " + team.getMaxAnggota());

        } catch (Exception e) {
            Log.e("TEAM_ITEM", "Error setting up team item", e);
        }
    }

    // Method untuk menampilkan badge status
    private void setupStatusBadge(View itemView, String status) {
//        TextView statusBadge = itemView.findViewById(R.id.status_badge); // Tambahkan TextView di layout
//        if (statusBadge != null && status != null) {
//            statusBadge.setVisibility(View.VISIBLE);
//            statusBadge.setText(status);
//
//            // Atur warna berdasarkan status
//            switch (status.toLowerCase()) {
//                case "active":
//                    statusBadge.setBackgroundResource(R.drawable.badge_green);
//                    break;
//                case "pending":
//                    statusBadge.setBackgroundResource(R.drawable.badge_purple);
//                    break;
//                case "closed":
//                    statusBadge.setBackgroundResource(R.drawable.badge_red);
//                    break;
//                default:
//                    statusBadge.setVisibility(View.GONE);
//            }
//        }
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

                            TextView badgeText = badge.findViewById(R.id.badge_status);

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

    private void onJoinTeamClicked(Team team) {
        if (getContext() == null) return;

        // Buat BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

        // Inflate layout bottom sheet
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.custom_ikut_tim, null);

        // Isi data dari Team ke dalam layout
        setupBottomSheetData(sheetView, team);

        // Setup button daftar
        Button btnDaftar = sheetView.findViewById(R.id.btnDaftar);
        if (btnDaftar != null) {
            btnDaftar.setOnClickListener(v -> {
                // Handle pendaftaran ke tim
                handleTeamRegistration(team, sheetView);
                bottomSheetDialog.dismiss();
            });
        }

        // Setup reset button jika ada
        TextView btnReset = sheetView.findViewById(R.id.text_reset);
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                resetBottomSheetForm(sheetView);
                Toast.makeText(getContext(), "Form direset", Toast.LENGTH_SHORT).show();
            });
        }

        bottomSheetDialog.setContentView(sheetView);

        // Set background transparan
        View parent = (View) sheetView.getParent();
        if (parent != null) {
            parent.setBackgroundColor(Color.TRANSPARENT);
        }

        bottomSheetDialog.show();
    }

    private void setupBottomSheetData(View sheetView, Team team) {
        try {
            // Set judul dengan nama tim
            TextView textFilter = sheetView.findViewById(R.id.text_filter);
            if (textFilter != null) {
                textFilter.setText("IKUT TIM - " + team.getNamaTeam());
            }

            // Cari GridLayout
            GridLayout gridLayout = null;
            // Cari GridLayout dalam struktur view
            LinearLayout linearLayout = sheetView.findViewById(R.id.container_filter);
            if (linearLayout != null) {
                NestedScrollView scrollView = (NestedScrollView) linearLayout.getChildAt(0);
                if (scrollView != null) {
                    LinearLayout innerLayout = (LinearLayout) scrollView.getChildAt(0);
                    if (innerLayout != null) {
                        for (int i = 0; i < innerLayout.getChildCount(); i++) {
                            View child = innerLayout.getChildAt(i);
                            if (child instanceof GridLayout) {
                                gridLayout = (GridLayout) child;
                                break;
                            }
                        }
                    }
                }
            }

            if (gridLayout != null) {
                // Item 1: Member saat ini (indeks 0)
                View memberLayout = gridLayout.getChildAt(0);
                if (memberLayout != null && memberLayout instanceof LinearLayout) {
                    LinearLayout layout = (LinearLayout) memberLayout;
                    // TextView adalah child kedua (indeks 1)
                    if (layout.getChildCount() > 1) {
                        View textView = layout.getChildAt(1);
                        if (textView instanceof TextView) {
                            String currentMembers = team.getMemberSaatIni();
                            String maxMembers = team.getMaxAnggota();
                            ((TextView) textView).setText(currentMembers + "/" + maxMembers + " member");
                        }
                    }
                }

                // Item 2: Nama tim (indeks 1)
                View teamNameLayout = gridLayout.getChildAt(1);
                if (teamNameLayout != null && teamNameLayout instanceof LinearLayout) {
                    LinearLayout layout = (LinearLayout) teamNameLayout;
                    // TextView adalah child kedua (indeks 1)
                    if (layout.getChildCount() > 1) {
                        View textView = layout.getChildAt(1);
                        if (textView instanceof TextView) {
                            ((TextView) textView).setText(team.getNamaTeam());
                        }
                    }
                }

                // Item 3: Deadline join (indeks 2)
                View deadlineLayout = gridLayout.getChildAt(2);
                if (deadlineLayout != null && deadlineLayout instanceof LinearLayout) {
                    LinearLayout layout = (LinearLayout) deadlineLayout;
                    // TextView adalah child kedua (indeks 1)
                    if (layout.getChildCount() > 1) {
                        View textView = layout.getChildAt(1);
                        if (textView instanceof TextView) {
                            // Gunakan waktu post sebagai deadline jika tidak ada field khusus
                            String deadline = team.getWaktuPost() != null ?
                                    formatDate(team.getWaktuPost()) : "Belum ditentukan";
                            ((TextView) textView).setText(deadline);
                        }
                    }
                }

                // Item 4: Status (indeks 3)
                View statusLayout = gridLayout.getChildAt(3);
                if (statusLayout != null && statusLayout instanceof LinearLayout) {
                    LinearLayout layout = (LinearLayout) statusLayout;
                    // TextView adalah child kedua (indeks 1)
                    if (layout.getChildCount() > 1) {
                        View textView = layout.getChildAt(1);
                        if (textView instanceof TextView) {
                            try {
                                int current = Integer.parseInt(team.getMemberSaatIni());
                                int max = Integer.parseInt(team.getMaxAnggota());
                                int needed = max - current;
                                if (needed > 0) {
                                    ((TextView) textView).setText(needed + " Member dibutuhkan");
                                } else {
                                    ((TextView) textView).setText("Tim sudah penuh");
                                }
                            } catch (NumberFormatException e) {
                                ((TextView) textView).setText("Member dibutuhkan");
                            }
                        }
                    }
                }
            }

            // Setup dropdown tim jika ada
            View dropdownView = sheetView.findViewById(R.id.include_dropdown_tim);
            if (dropdownView != null) {
                setupDropdownData(dropdownView, team);
            }

            Log.d("BOTTOM_SHEET", "Bottom sheet data setup completed for team: " + team.getNamaTeam());

        } catch (Exception e) {
            Log.e("BOTTOM_SHEET", "Error setting up bottom sheet data", e);
            Toast.makeText(getContext(), "Gagal memuat data tim", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDropdownData(View dropdownView, Team team) {
        try {
            // 1. Setup dropdown "Dibutuhkan"
            setupDibutuhkanDropdown(dropdownView, team);

            // 2. Setup dropdown "Informasi Lomba"
            setupInformasiLombaDropdown(dropdownView, team);

            // 3. Setup click listeners untuk dropdown
            setupDropdownListeners(dropdownView);

        } catch (Exception e) {
            Log.e("DROPDOWN_SETUP", "Error setting up dropdown data", e);
        }
    }

    private void setupDibutuhkanDropdown(View dropdownView, Team team) {
        try {
            LinearLayout contentContainer = dropdownView.findViewById(R.id.dropdown_content_dibutuhkan_container);
            LinearLayout detailKetentuan = dropdownView.findViewById(R.id.detail_ketentuan_container);

            if (contentContainer != null) {
                // Kosongkan konten yang ada
                contentContainer.removeAllViews();

                String roleRequired = team.getRoleRequired();
                if (roleRequired != null && !roleRequired.isEmpty()) {
                    // Split multiple roles jika dipisah koma
                    String[] roles = roleRequired.split(",");

                    for (String role : roles) {
                        String trimmedRole = role.trim();
                        if (!trimmedRole.isEmpty()) {
                            // Buat layout untuk setiap role
                            LinearLayout roleLayout = createRoleLayout(trimmedRole);
                            contentContainer.addView(roleLayout);
                        }
                    }

                    // Tambahkan detail ketentuan jika ada
                    if (detailKetentuan != null) {
                        contentContainer.addView(detailKetentuan);

                        // Anda bisa mengisi ketentuan dari data tim jika ada
                        // Contoh: setupKetentuan(detailKetentuan, team);
                    }

                    // Buat bonus section jika ada
                    createBonusSection(contentContainer, team);
                }
            }
        } catch (Exception e) {
            Log.e("DIBUTUHKAN_SETUP", "Error setting up dibutuhkan dropdown", e);
        }
    }

    private LinearLayout createRoleLayout(String role) {
        // Buat layout untuk role
        LinearLayout roleLayout = new LinearLayout(getContext());
        roleLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        roleLayout.setOrientation(LinearLayout.HORIZONTAL);
        roleLayout.setGravity(Gravity.CENTER_VERTICAL);
        roleLayout.setPadding(0, 0, 0, 6);

        // TextView untuk nama role
        TextView roleText = new TextView(getContext());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        roleText.setLayoutParams(textParams);
        roleText.setText(role);
        roleText.setTextColor(0xB0FFFFFF); // Warna dengan alpha
        roleText.setTextSize(14);
        roleText.setTypeface(getResources().getFont(R.font.is_r));

        // TextView untuk jumlah orang
        TextView countText = new TextView(getContext());
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        countParams.setMargins(10, 0, 0, 0);
        countText.setLayoutParams(countParams);
        countText.setText("1 orang"); // Default, bisa disesuaikan dengan data tim
        countText.setTextColor(0xFF5E8BFF); // Warna biru
        countText.setTextSize(13);
        countText.setTypeface(getResources().getFont(R.font.is_sb));

        // Set background
        countText.setBackgroundResource(R.drawable.custom_bg_biru_tipis);
        int padding = (int) (10 * getResources().getDisplayMetrics().density);
        countText.setPadding(padding, 6, padding, 6);

        // Tambahkan views ke layout
        roleLayout.addView(roleText);
        roleLayout.addView(countText);

        return roleLayout;
    }

    private void createBonusSection(LinearLayout container, Team team) {
        // Buat bonus section
        LinearLayout bonusLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 12, 0, 0);
        bonusLayout.setLayoutParams(layoutParams);
        bonusLayout.setOrientation(LinearLayout.HORIZONTAL);
        bonusLayout.setGravity(Gravity.CENTER_VERTICAL);
        bonusLayout.setPadding(10, 10, 10, 10);
        bonusLayout.setBackgroundResource(R.drawable.custom_bg_biru_tipis);

        // Icon dan teks "Bonus"
        TextView bonusLabel = new TextView(getContext());
        bonusLabel.setText("Bonus : ");
        bonusLabel.setTextColor(0xFF5E8BFF);
        bonusLabel.setTextSize(14);
        bonusLabel.setTypeface(getResources().getFont(R.font.is_sb));

        // Set icon bintang menggunakan drawableStart
        bonusLabel.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.icon_bintang, 0, 0, 0
        );
        bonusLabel.setCompoundDrawablePadding(
                (int) (10 * getResources().getDisplayMetrics().density)
        );

        // Teks bonus (gunakan deskripsi tim atau custom)
        TextView bonusText = new TextView(getContext());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        textParams.setMargins(4, 0, 0, 0);
        bonusText.setLayoutParams(textParams);

        // Isi teks bonus dari data tim atau default
        String bonusContent = team.getDeskripsiAnggota();
        if (bonusContent != null && !bonusContent.isEmpty()) {
            bonusText.setText(bonusContent);
        } else {
            bonusText.setText("Bergabung untuk pengalaman berharga dan networking");
        }

        bonusText.setTextColor(Color.WHITE);
        bonusText.setTextSize(14);
        bonusText.setTypeface(getResources().getFont(R.font.is_r));

        bonusLayout.addView(bonusLabel);
        bonusLayout.addView(bonusText);

        container.addView(bonusLayout);
    }

    private void setupInformasiLombaDropdown(View dropdownView, Team team) {
        try {
            LinearLayout contentContainer = dropdownView.findViewById(R.id.dropdown_content_informasi_container);

            if (contentContainer != null) {
                // Setup data lomba dari tim
                setupLombaData(contentContainer, team);
            }
        } catch (Exception e) {
            Log.e("LOMBA_SETUP", "Error setting up informasi lomba dropdown", e);
        }
    }

    private void setupLombaData(LinearLayout container, Team team) {
        // Cari semua TextView di container untuk mengisi data
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);

            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                String currentText = textView.getText().toString();

                // Isi data berdasarkan teks yang ada
                switch (currentText) {
                    case "NIFC 3.0":
                        // Isi nama lomba dari tim
                        textView.setText(team.getNamaKegiatan() != null ?
                                team.getNamaKegiatan() : "Nama Lomba");
                        break;

                    case "Nama Penyelenggara":
                        // Isi penyelenggara jika ada data
                        textView.setText("Penyelenggara Lomba");
                        break;

                    case "Link Lomba":
                        // Setup link lomba jika ada
                        setupLinkLomba(textView, team);
                        break;

                    case "Uang Tunai + Sertifikat + Pengalaman + etc":
                        // Setup hadiah jika ada
                        setupHadiah(textView, team);
                        break;
                }
            } else if (child instanceof LinearLayout) {
                // Cek untuk hadiah layout
                LinearLayout linearLayout = (LinearLayout) child;
                if (linearLayout.getBackground() != null) {
                    // Mungkin ini layout hadiah
                    for (int j = 0; j < linearLayout.getChildCount(); j++) {
                        View innerChild = linearLayout.getChildAt(j);
                        if (innerChild instanceof TextView) {
                            TextView innerText = (TextView) innerChild;
                            if (innerText.getText().toString().contains("Uang Tunai")) {
                                setupHadiah(innerText, team);
                            }
                        }
                    }
                }
            }
        }
    }

    private void setupLinkLomba(TextView linkTextView, Team team) {
        // Jika tim punya link lomba, tambahkan di model Team
        // Untuk sementara, set default
        linkTextView.setText("Link Informasi Lomba");

        // Tambahkan click listener untuk membuka link
        linkTextView.setOnClickListener(v -> {
            // Buka browser atau WebView dengan link
            Toast.makeText(getContext(),
                    "Membuka informasi lomba",
                    Toast.LENGTH_SHORT).show();

            // Contoh: buka URL
        /*
        String url = "https://example.com/lomba"; // Ganti dengan link dari team
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
        */
        });
    }

    private void setupHadiah(TextView hadiahTextView, Team team) {
        // Setup teks hadiah
        // Anda bisa menambahkan field hadiah di model Team
        String hadiah = "Hadiah menarik + Sertifikat + Pengalaman berharga";
        hadiahTextView.setText(hadiah);
    }

    private void setupDropdownListeners(View dropdownView) {
        // Setup click listener untuk dropdown "Dibutuhkan"
        LinearLayout dibutuhkanHeader = dropdownView.findViewById(R.id.dropdown_header_dibutuhkan);
        ImageView dibutuhkanIcon = dropdownView.findViewById(R.id.dropdown_icon_dibutuhkan);
        LinearLayout dibutuhkanContent = dropdownView.findViewById(R.id.dropdown_content_dibutuhkan_container);

        if (dibutuhkanHeader != null && dibutuhkanIcon != null && dibutuhkanContent != null) {
            dibutuhkanHeader.setOnClickListener(v -> {
                boolean isVisible = dibutuhkanContent.getVisibility() == View.VISIBLE;
                dibutuhkanContent.setVisibility(isVisible ? View.GONE : View.VISIBLE);

                // Rotate icon
                animateDropdownIcon(dibutuhkanIcon, !isVisible);
            });
        }

        // Setup click listener untuk dropdown "Informasi Lomba"
        LinearLayout informasiHeader = dropdownView.findViewById(R.id.dropdown_header_informasi);
        ImageView informasiIcon = dropdownView.findViewById(R.id.dropdown_icon_informasi);
        LinearLayout informasiContent = dropdownView.findViewById(R.id.dropdown_content_informasi_container);

        if (informasiHeader != null && informasiIcon != null && informasiContent != null) {
            informasiHeader.setOnClickListener(v -> {
                boolean isVisible = informasiContent.getVisibility() == View.VISIBLE;
                informasiContent.setVisibility(isVisible ? View.GONE : View.VISIBLE);

                // Rotate icon
                animateDropdownIcon(informasiIcon, !isVisible);
            });
        }
    }

    private void animateDropdownIcon(ImageView icon, boolean expand) {
        if (expand) {
            icon.setRotation(180); // Rotate down
            icon.setImageResource(R.drawable.icon_drop_down_on); // Ganti dengan icon yang sesuai
        } else {
            icon.setRotation(0); // Rotate up
            icon.setImageResource(R.drawable.icon_drop_down_off); // Ganti dengan icon yang sesuai
        }
    }

    private void resetBottomSheetForm(View sheetView) {
//        try {
//            // Reset dropdown
//            View dropdownView = sheetView.findViewById(R.id.include_dropdown_tim);
//            if (dropdownView != null) {
//                // Reset spinner
//                Spinner roleSpinner = dropdownView.findViewById(R.id.spinner_role);
//                if (roleSpinner != null) {
//                    roleSpinner.setSelection(0);
//                }
//
//                // Reset edit text
//                EditText motivationInput = dropdownView.findViewById(R.id.edit_motivation);
//                if (motivationInput != null) {
//                    motivationInput.setText("");
//                }
//
//                // Reset input lainnya jika ada
//            }
//
//            Toast.makeText(getContext(), "Form berhasil direset", Toast.LENGTH_SHORT).show();
//
//        } catch (Exception e) {
//            Log.e("RESET_FORM", "Error resetting form", e);
//        }
    }

    private void handleTeamRegistration(Team team, View sheetView) {
//        try {
//            // Ambil data dari form
//            String selectedRole = "";
//            String motivation = "";
//
//            View dropdownView = sheetView.findViewById(R.id.include_dropdown_tim);
//            if (dropdownView != null) {
//                Spinner roleSpinner = dropdownView.findViewById(R.id.spinner_role);
//                if (roleSpinner != null && roleSpinner.getSelectedItemPosition() > 0) {
//                    selectedRole = roleSpinner.getSelectedItem().toString();
//                }
//
//                EditText motivationInput = dropdownView.findViewById(R.id.edit_motivation);
//                if (motivationInput != null) {
//                    motivation = motivationInput.getText().toString().trim();
//                }
//            }
//
//            // Validasi
//            if (selectedRole.isEmpty()) {
//                Toast.makeText(getContext(), "Harap pilih peran yang ingin diambil", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (motivation.isEmpty()) {
//                Toast.makeText(getContext(), "Harap isi alasan bergabung", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Tampilkan konfirmasi
//            new AlertDialog.Builder(getContext())
//                    .setTitle("Konfirmasi Pendaftaran")
//                    .setMessage("Apakah Anda yakin ingin mendaftar ke tim " + team.getNamaTeam() +
//                            " sebagai " + selectedRole + "?")
//                    .setPositiveButton("Ya, Daftar", (dialog, which) -> {
//                        // Kirim data pendaftaran ke server
//                        submitTeamRegistration(team, selectedRole, motivation);
//                    })
//                    .setNegativeButton("Batal", null)
//                    .show();
//
//        } catch (Exception e) {
//            Log.e("REGISTRATION", "Error handling team registration", e);
//            Toast.makeText(getContext(), "Terjadi kesalahan saat mendaftar", Toast.LENGTH_SHORT).show();
//        }
    }

    private void submitTeamRegistration(Team team, String selectedRole, String motivation) {
        // Implementasi pengiriman data ke server
        Log.d("REGISTRATION", "Mendaftar ke tim: " + team.getNamaTeam());
        Log.d("REGISTRATION", "Peran: " + selectedRole);
        Log.d("REGISTRATION", "Motivasi: " + motivation);

        // Contoh: Panggil API service
    /*
    ApiService.registerToTeam(
        team.getTeamId(),
        selectedRole,
        motivation,
        new ApiCallback() {
            @Override
            public void onSuccess(String result) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                        "Pendaftaran berhasil dikirim!",
                        Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                        "Gagal mengirim pendaftaran: " + error,
                        Toast.LENGTH_SHORT).show();
                });
            }
        }
    );
    */

        // Untuk sementara, tampilkan toast
        Toast.makeText(getContext(),
                "Pendaftaran ke tim " + team.getNamaTeam() + " berhasil dikirim!",
                Toast.LENGTH_SHORT).show();
    }
}