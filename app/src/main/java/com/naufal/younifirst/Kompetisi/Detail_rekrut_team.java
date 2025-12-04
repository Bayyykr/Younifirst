package com.naufal.younifirst.Kompetisi;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;

public class Detail_rekrut_team extends AppCompatActivity {

    private PopupWindow popupWindow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_rekrut_tim);

        ImageView backButton = findViewById(R.id.back_to_mainactivity);
        backButton.setOnClickListener(v -> finish());

        LinearLayout headerDibutuhkan = findViewById(R.id.dropdown_header_dibutuhkan);
        LinearLayout contentDibutuhkan = findViewById(R.id.dropdown_content_dibutuhkan_container);
        ImageView iconDibutuhkan = findViewById(R.id.dropdown_icon_dibutuhkan);

        headerDibutuhkan.setOnClickListener(v -> {
            if (contentDibutuhkan.getVisibility() == View.VISIBLE) {
                contentDibutuhkan.setVisibility(View.GONE);
                iconDibutuhkan.setImageResource(R.drawable.icon_drop_down_off);
            } else {
                contentDibutuhkan.setVisibility(View.VISIBLE);
                iconDibutuhkan.setImageResource(R.drawable.icon_drop_down_on);
            }
        });

        LinearLayout headerInformasi = findViewById(R.id.dropdown_header_informasi);
        LinearLayout contentInformasi = findViewById(R.id.dropdown_content_informasi_container);
        ImageView iconInformasi = findViewById(R.id.dropdown_icon_informasi);

        headerInformasi.setOnClickListener(v -> {
            if (contentInformasi.getVisibility() == View.VISIBLE) {
                contentInformasi.setVisibility(View.GONE);
                iconInformasi.setImageResource(R.drawable.icon_drop_down_off);
            } else {
                contentInformasi.setVisibility(View.VISIBLE);
                iconInformasi.setImageResource(R.drawable.icon_drop_down_on);
            }
        });

        View btnBuatPostingan = findViewById(R.id.btnBuatPostinganRekrutTim);
        btnBuatPostingan.setOnClickListener(v -> {
            Intent intent = new Intent(Detail_rekrut_team.this, Posting_rekrut_team.class);
            startActivity(intent);
        });

        ImageView iconLainnya = findViewById(R.id.iconlainnya);

        iconLainnya.setOnClickListener(v -> showPopupMenu(v));
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

            Intent intent = new Intent(Detail_rekrut_team.this, Edit_Tim.class);
            startActivity(intent);
        });

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);

        int marginRight = (int) (-10 * getResources().getDisplayMetrics().density);

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();

        int posX = location[0] - popupWidth - marginRight;
        int posY = location[1];

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, posX, posY);
    }

}
