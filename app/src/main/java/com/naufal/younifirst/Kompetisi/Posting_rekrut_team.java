package com.naufal.younifirst.Kompetisi;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;

public class Posting_rekrut_team extends AppCompatActivity {

    private ImageView previewImage;
    private LinearLayout containerClickUpload;
    private ImageView btnHapusFoto;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
                if (result != null) {
                    previewImage.setVisibility(View.VISIBLE);
                    btnHapusFoto.setVisibility(View.VISIBLE);
                    previewImage.setImageURI(result);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_team_posting_rekrut_team_2);

        previewImage = findViewById(R.id.previewImagePostingan);
        containerClickUpload = findViewById(R.id.containerClickUpload);

        containerClickUpload.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        setupPreviewContainer();

        Button btnSimpanDraft = findViewById(R.id.btnSimpanDraft);
        btnSimpanDraft.setOnClickListener(v -> {});

        Button btnBagikan = findViewById(R.id.btnBagikan);
        btnBagikan.setOnClickListener(v -> {});

        ImageView back = findViewById(R.id.back_to_mainactivity);
        back.setOnClickListener(v -> finish());
    }
    private void setupPreviewContainer() {
        LinearLayout parent = findViewById(R.id.containerGambarPostingan);
        Context ctx = this;

        FrameLayout frame = new FrameLayout(ctx);
        FrameLayout.LayoutParams frameParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        parent.removeAllViews();
        parent.addView(frame, frameParams);

        frame.addView(previewImage);

        btnHapusFoto = new ImageView(ctx);
        btnHapusFoto.setImageResource(R.drawable.icon_silang_kecil);
        btnHapusFoto.setVisibility(View.GONE);
        containerClickUpload.setVisibility(View.VISIBLE);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(60, 60);
        params.gravity = android.view.Gravity.END | android.view.Gravity.TOP;
        params.topMargin = 20;
        params.rightMargin = 20;

        frame.addView(btnHapusFoto, params);

        btnHapusFoto.setOnClickListener(v -> {
            previewImage.setImageDrawable(null);
            previewImage.setVisibility(View.GONE);
            btnHapusFoto.setVisibility(View.GONE);
        });
    }
}