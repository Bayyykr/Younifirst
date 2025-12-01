package com.naufal.younifirst.Home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.naufal.younifirst.R;

public class PostingLostActivity extends AppCompatActivity {

    private TextView backButtonMain, tambahLokasi, tambahTelepon, tambahEmail;
    private LinearLayout itemLt3, itemLt4, itemParkiran;
    private ImageView iconLt3, iconLt4, iconParkiran;
    private ImageView checkLt3, checkLt4, checkParkiran;
    private TextView textLt3, textLt4, textParkiran;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_lostnfound_kehilangan);

        backButtonMain = findViewById(R.id.back_to_mainactivity);
        backButtonMain.setOnClickListener(v -> finish());

        tambahLokasi = findViewById(R.id.tambah_lokasi_textview);
        tambahTelepon = findViewById(R.id.tambah_telepon_textview);
        tambahEmail = findViewById(R.id.tambah_email_textview);

        tambahLokasi.setOnClickListener(v -> showTambahLokasiPopup());
        tambahTelepon.setOnClickListener(v -> showTambahTeleponPopup());
        tambahEmail.setOnClickListener(v -> showTambahEmailPopup());
    }

    private void showTambahLokasiPopup() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(this)
                .inflate(R.layout.fragment_lostnfound_tambahkan_lokasi, null);

        bottomSheetDialog.setContentView(bottomSheetView);

        makeBottomSheetTransparent(bottomSheetDialog);

        itemLt3 = bottomSheetView.findViewById(R.id.item_lokasi_lt3);
        itemLt4 = bottomSheetView.findViewById(R.id.item_lokasi_lt4);
        itemParkiran = bottomSheetView.findViewById(R.id.item_lokasi_parkiran);

        iconLt3 = bottomSheetView.findViewById(R.id.icon_lokasi_lt3);
        iconLt4 = bottomSheetView.findViewById(R.id.icon_lokasi_lt4);
        iconParkiran = bottomSheetView.findViewById(R.id.icon_lokasi_parkiran);

        checkLt3 = bottomSheetView.findViewById(R.id.check_lt3);
        checkLt4 = bottomSheetView.findViewById(R.id.check_lt4);
        checkParkiran = bottomSheetView.findViewById(R.id.check_parkiran);

        textLt3 = bottomSheetView.findViewById(R.id.text_lokasi_lt3);
        textLt4 = bottomSheetView.findViewById(R.id.text_lokasi_lt4);
        textParkiran = bottomSheetView.findViewById(R.id.text_lokasi_parkiran);

        itemLt3.setOnClickListener(v -> selectLocation("lt3"));
        itemLt4.setOnClickListener(v -> selectLocation("lt4"));
        itemParkiran.setOnClickListener(v -> selectLocation("parkiran"));

        bottomSheetDialog.show();
    }

    private void showTambahTeleponPopup() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(this)
                .inflate(R.layout.fragment_lostnfound_tambahkan_nomor_telepon, null);

        bottomSheetDialog.setContentView(bottomSheetView);
        makeBottomSheetTransparent(bottomSheetDialog);

        bottomSheetDialog.show();
    }

    private void showTambahEmailPopup() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(this)
                .inflate(R.layout.fragment_lostnfound_tambahkan_alamat_email, null);

        bottomSheetDialog.setContentView(bottomSheetView);
        makeBottomSheetTransparent(bottomSheetDialog);

        bottomSheetDialog.show();
    }

    private void makeBottomSheetTransparent(BottomSheetDialog dialog) {
        View parent = (View) ((View) dialog.getWindow().getDecorView().findViewById(com.google.android.material.R.id.design_bottom_sheet));
        if (parent != null) {
            parent.setBackgroundColor(Color.TRANSPARENT);

            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(parent);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetBehavior.setSkipCollapsed(true);
        }
    }

    private void selectLocation(String selected) {
        resetState(iconLt3, textLt3, checkLt3);
        resetState(iconLt4, textLt4, checkLt4);
        resetState(iconParkiran, textParkiran, checkParkiran);

        switch (selected) {
            case "lt3": setSelected(iconLt3, textLt3, checkLt3); break;
            case "lt4": setSelected(iconLt4, textLt4, checkLt4); break;
            case "parkiran": setSelected(iconParkiran, textParkiran, checkParkiran); break;
        }
    }

    private void resetState(ImageView icon, TextView text, ImageView check) {
        icon.setImageResource(R.drawable.icon_lokasi);
        text.setTextColor(getResources().getColor(R.color.white));
        check.setVisibility(View.GONE);
    }

    private void setSelected(ImageView icon, TextView text, ImageView check) {
        icon.setImageResource(R.drawable.icon_lokasi_selected);
        text.setTextColor(getResources().getColor(R.color.btn_fabmenu));
        check.setVisibility(View.VISIBLE);
    }
}
