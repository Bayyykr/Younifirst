package com.naufal.younifirst.Kompetisi;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.naufal.younifirst.R;
import com.naufal.younifirst.custom.CustomEditText;

import java.util.Calendar;

public class Edit_Tim extends AppCompatActivity {

    private ImageView backButton;
    private CustomEditText etNamaTim, etMaksimalMember, tgltutuptim;
    private LinearLayout containerPosisiTim;
    private Button btnTambahPosisi, btnBuatTim;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_tim_edit_tim);

        backButton = findViewById(R.id.back_to_mainactivity);
        etNamaTim = findViewById(R.id.et_namatim);
        etMaksimalMember = findViewById(R.id.Btn_maksimalMember);
        tgltutuptim = findViewById(R.id.et_bataspendaftaran);
        containerPosisiTim = findViewById(R.id.containerPosisiTim);
        btnTambahPosisi = findViewById(R.id.btnTambahPosisi);
        btnBuatTim = findViewById(R.id.btnEditTim);

        etNamaTim.setHint("Nama Tim");
        etMaksimalMember.setHint("Masukkan jumlah member maksimal");
        tgltutuptim.setHint("Masukkan deadline tanggal tutup");

        setupDatePicker(tgltutuptim);

        backButton.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra("posted", false);
            setResult(RESULT_OK, data);
            finish();
        });

        // Tambah posisi baru
        btnTambahPosisi.setOnClickListener(v -> tambahPosisiDinamically());

        // Klik Buat Tim
        btnBuatTim.setOnClickListener(v -> {
            int count = containerPosisiTim.getChildCount();
            for (int i = 0; i < count; i++) {
                View item = containerPosisiTim.getChildAt(i);
                CustomEditText etNamaPosisi = item.findViewById(R.id.et_nama_posisi);
                CustomEditText etJumlahOrang = item.findViewById(R.id.et_jumlah_orang);
                LinearLayout containerKetentuan = item.findViewById(R.id.container_ketentuan);

                String namaPosisi = etNamaPosisi.getText().toString();
                String jumlahOrang = etJumlahOrang.getText().toString();

                int ketCount = containerKetentuan.getChildCount();
                for (int k = 0; k < ketCount; k++) {
                    CustomEditText etKet = (CustomEditText) containerKetentuan.getChildAt(k);
                    String ket = etKet.getText().toString();
                }

            }
        });
    }

    private void setupDatePicker(CustomEditText customEditText) {
        customEditText.setFocusable(false);
        customEditText.setOnCustomClickListener(view -> {

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (dateView, selectedYear, selectedMonth, selectedDay) -> {

                        String formattedDate = String.format(
                                "%02d-%02d-%04d",
                                selectedDay, selectedMonth + 1, selectedYear
                        );

                        customEditText.setText(formattedDate);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });
    }

    private void tambahPosisiDinamically() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View posisiItem = inflater.inflate(R.layout.layout_item_posisi_tim, containerPosisiTim, false);

        ImageButton btnDelete = posisiItem.findViewById(R.id.btn_delete_posisi);
        LinearLayout containerKetentuan = posisiItem.findViewById(R.id.container_ketentuan);
        Button btnTambahKetentuan = posisiItem.findViewById(R.id.btn_tambah_ketentuan);
        // TextView Posisi
        TextView tvPosisi = posisiItem.findViewById(R.id.tv_posisi);

        // Set keterangan posisi sesuai urutan
        int posisiNumber = containerPosisiTim.getChildCount() + 1;
        tvPosisi.setText("Posisi " + posisiNumber);

        btnDelete.setOnClickListener(v -> {
            containerPosisiTim.removeView(posisiItem);
            updateKeteranganPosisi();
        });

        btnTambahKetentuan.setOnClickListener(v -> {
            CustomEditText etKet = new CustomEditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(45)
            );
            params.setMargins(0, dpToPx(10), 0, dpToPx(0));
            etKet.setLayoutParams(params);
            etKet.setHint("Ketentuan tambahan");
            containerKetentuan.addView(etKet);
        });


        containerPosisiTim.addView(posisiItem);
    }

    private void updateKeteranganPosisi() {
        int count = containerPosisiTim.getChildCount();
        for (int i = 0; i < count; i++) {
            View item = containerPosisiTim.getChildAt(i);
            TextView tvPosisi = item.findViewById(R.id.tv_posisi);
            tvPosisi.setText("Posisi " + (i + 1));
        }
    }
    private int dpToPx(int dp){
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
