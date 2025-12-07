package com.naufal.younifirst.Kompetisi;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;

public class DaftarLombaSekarang extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_daftar_sekarang_lomba);

        ImageButton backBtn = findViewById(R.id.back_to_mainactivity);

        backBtn.setOnClickListener(v -> {
            finish();
        });
    }
}
