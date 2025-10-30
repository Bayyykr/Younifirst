package com.naufal.younifirst.Home;

import android.os.Bundle;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;
import com.naufal.younifirst.custom.CustomEditText;

public class ProfileActivity extends AppCompatActivity {
    private CustomEditText etUsername, etNamaLengkap, etAngkatan, etStudentEmail, etNIM, etJenisKelamin, etTanggalLahir,etAlamat ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profil_akun);

        // Initialize views
        etUsername = findViewById(R.id.et_username);
        etNamaLengkap = findViewById(R.id.et_NamaLengkap);
        etAngkatan = findViewById(R.id.et_Angkatan);
        etStudentEmail = findViewById(R.id.et_StudentEmail);
        etNIM = findViewById(R.id.et_NIM);
        etJenisKelamin = findViewById(R.id.et_JenisKelamin);
        etTanggalLahir = findViewById(R.id.et_TanggalLahir);
        etAlamat = findViewById(R.id.et_Alamat);

        // Set hint (jika tidak lewat XML)
        etUsername.setHint("Username");
        etNamaLengkap.setHint("Nama Lengkap");
        etAngkatan.setHint("Angkatan");
        etStudentEmail.setHint("Student Email");
        etNIM.setHint(" NIM");
        etJenisKelamin.setHint("Jenis Kelamin");
        etTanggalLahir.setHint("Tahun-Bulan-Tanggal lahir");
        etAlamat.setHint("Alamat");

        // Set input types
        etUsername.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        etNamaLengkap.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        etAngkatan.getEditText().setInputType(InputType.TYPE_CLASS_DATETIME);
        etStudentEmail.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etNIM.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        etJenisKelamin.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        etTanggalLahir.getEditText().setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
        etAlamat.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
    }
}