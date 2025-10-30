package com.naufal.younifirst.Home;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.naufal.younifirst.R;

public class PengaturanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pengaturan);

        SwitchCompat switchtema = findViewById(R.id.switch_tema);
        SwitchCompat switchnotifikasi = findViewById(R.id.switch_notifikasi);

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked},
                new int[] {}
        };

        int[] trackColors = new int[] {
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#FFFFFF")
        };

        ColorStateList customTrackTint = new ColorStateList(states, trackColors);

        switchnotifikasi.setTrackTintList(customTrackTint);
        switchtema.setTrackTintList(customTrackTint);
    }
}
