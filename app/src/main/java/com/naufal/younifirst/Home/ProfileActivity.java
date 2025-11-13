package com.naufal.younifirst.Home;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.naufal.younifirst.R;
import com.naufal.younifirst.custom.CustomEditText;
import com.naufal.younifirst.custom.YearPickerDialog;

public class ProfileActivity extends AppCompatActivity {
    private CustomEditText[] editTexts;
    private View buttonContainer;
    private Button btnBatal, btnSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profil_akun);

        // Inisialisasi semua CustomEditText
        editTexts = new CustomEditText[]{
                findViewById(R.id.et_username),
                findViewById(R.id.et_NamaLengkap),
                findViewById(R.id.et_Angkatan),
                findViewById(R.id.et_StudentEmail),
                findViewById(R.id.et_NIM),
                findViewById(R.id.et_JenisKelamin),
                findViewById(R.id.et_TanggalLahir),
                findViewById(R.id.et_Alamat)
        };

        buttonContainer = findViewById(R.id.buttonContainer);
        btnBatal = findViewById(R.id.btnBatal);
        btnSimpan = findViewById(R.id.btnSimpan);

        // Set hint untuk tiap EditText
        editTexts[0].setHint("Username");
        editTexts[1].setHint("Nama Lengkap");
        editTexts[2].setHint("Angkatan");
        editTexts[3].setHint("Student Email");
        editTexts[4].setHint("NIM");
        editTexts[5].setHint("Jenis Kelamin");
        editTexts[6].setHint("Tahun-Bulan-Tanggal Lahir");
        editTexts[7].setHint("Alamat");

        // Set input type
        editTexts[0].getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        editTexts[3].getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Setup tahun picker untuk field Angkatan
        setupYearPicker();

        // Tampilkan tombol ketika ada fokus
        for (CustomEditText et : editTexts) {
            et.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    buttonContainer.setVisibility(View.VISIBLE);
                } else {
                    // PERBAIKAN: Paksa turunkan hint jika field kosong dan kehilangan fokus
                    if (et.getText().isEmpty()) {
                        et.forceHintDown();
                    }
                }
            });
        }

        // Tombol Batal: hapus isi dan sembunyikan tombol
        btnBatal.setOnClickListener(v -> {
            buttonContainer.setVisibility(View.GONE);
            for (CustomEditText et : editTexts) {
                et.clearAndReset(); // GUNAKAN METHOD BARU
            }
        });

        // Tombol Simpan: sembunyikan tombol dan hilangkan fokus
        btnSimpan.setOnClickListener(v -> {
            buttonContainer.setVisibility(View.GONE);
            for (CustomEditText et : editTexts) {
                et.getEditText().clearFocus();
                // PERBAIKAN: Paksa turunkan hint untuk field yang kosong
                if (et.getText().isEmpty()) {
                    et.forceHintDown();
                }
            }
            // TODO: Tambahkan logika save data ke database/preferences
        });

        ImageView btnBack = findViewById(R.id.back_to_setting);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupYearPicker() {
        editTexts[2].getEditText().setOnClickListener(v -> {
            buttonContainer.setVisibility(View.VISIBLE);

            YearPickerDialog dialog = new YearPickerDialog(this, editTexts[2], new YearPickerDialog.OnYearSelectedListener() {
                @Override
                public void onYearSelected(int year) {
                    editTexts[2].getEditText().setText(String.valueOf(year));

                    editTexts[0].getEditText().requestFocus();
                }
            });

            dialog.show();
        });

        editTexts[2].getEditText().setInputType(InputType.TYPE_NULL);
        editTexts[2].getEditText().setShowSoftInputOnFocus(false);
        editTexts[2].getEditText().setFocusableInTouchMode(false);
        editTexts[2].getEditText().setClickable(true);
    }

    private void hideKeyboard() {
        try {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocus = getCurrentFocus();
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}