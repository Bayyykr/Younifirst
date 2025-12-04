package com.naufal.younifirst.Kompetisi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;

public class TimAnda extends AppCompatActivity {

    private ImageView backButton, iconSearch, searchClose, Bbuttonpostingrekrut;
    private TextView titlePengaturan, emptyText;
    private LinearLayout searchBar, containerPostinganLomba, containerInfoTim;
    private EditText searchInput;
    private Button BtnPosting;
    private ImageButton btnPostinglomba;

    private ActivityResultLauncher<Intent> tambahPostingLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_rekrut_tim);

        backButton = findViewById(R.id.back_to_mainactivity);
        iconSearch = findViewById(R.id.iconsearch);
        titlePengaturan = findViewById(R.id.title_pengaturan);
        searchBar = findViewById(R.id.search_bar1);
        searchInput = findViewById(R.id.search_input1);
        searchClose = findViewById(R.id.search_close);
        btnPostinglomba = findViewById(R.id.btnBuatTim);
        Bbuttonpostingrekrut = findViewById(R.id.back_to_mainactivity);

        containerPostinganLomba = findViewById(R.id.containerPostinganLomba);
        emptyText = findViewById(R.id.emptyPostinganText);

        containerInfoTim = findViewById(R.id.containerInfoTim);

        BtnPosting = findViewById(R.id.btnSimpanPostingLomba);

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
        });

        tambahPostingLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        boolean posted = result.getData().getBooleanExtra("posted", false);

                        if (posted) {
                            containerPostinganLomba.setVisibility(View.VISIBLE);
                            emptyText.setVisibility(View.GONE);
                        }
                    }
                }
        );

        btnPostinglomba.setOnClickListener(v -> {
            Intent intent = new Intent(TimAnda.this, BuatTim.class);
            tambahPostingLauncher.launch(intent);
        });

        containerInfoTim.setOnClickListener(v -> {
            Intent intent = new Intent(TimAnda.this, Detail_rekrut_team.class);
            startActivity(intent);
        });
        BtnPosting.setOnClickListener(v -> {
            Intent intent = new Intent(TimAnda.this, Posting_rekrut_team.class);
            startActivity(intent);
        });
    }
}
