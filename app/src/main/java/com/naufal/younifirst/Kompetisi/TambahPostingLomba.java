package com.naufal.younifirst.Kompetisi;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.naufal.younifirst.R;
import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.custom.CustomEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TambahPostingLomba extends AppCompatActivity {

    private ImageView BackButton, iconCamera, iconPoster;
    private ShapeableImageView imgProfile;
    private CustomEditText etNamaLomba, etLokasiLomba,
            tglbataspendaftaran, etBiayaPendaftaran, etLinkPendaftaran, etLinkPanduanatauGuidebookLomba,
            etMasukkanHadiahPerlombaan, etNamaPenyelenggara, etLinkProfilePenyelenggara,
            etMasukkanNoWhatsApp, etMasukkanUsernameIG, etKeterangan;
    private Button bIndividu, bTim;
    private Button btnSertifikat, btnSertifikatDanTunai, btnSertifikatDanHadiahMenarik,
            btnSertifikatTunaiDanHadiahMenarik;
    private RadioButton radioGratis, radioBerbayar, radioNasional, radioInternasional;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> pickProfileImageLauncher;
    private Uri selectedImageUri;
    private Uri selectedProfileUri;
    private String selectedLombaType = "individual";
    private String selectedScope = "nasional";
    private String selectedBiayaType = "gratis";
    private String selectedHadiah = "";
    private List<String> selectedTags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_kompetisi_buat_postingan_lomba);

        // Initialize ApiHelper
        ApiHelper.initialize(getApplicationContext());

        String userId = ApiHelper.getSavedUserId();
        Log.d("USER_INFO", "User ID: " + userId);
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_LONG).show();
            finish();
        }

        initializeViews();
        setupHintTexts();
        setupListeners();
        setupImagePicker();
        setupProfileImagePicker();
        setupDatePicker();
        setupCategorySelection();
        setupTagSelection();
    }

    private void initializeViews() {
        etNamaLomba = findViewById(R.id.et_namalomba);
        etLokasiLomba = findViewById(R.id.et_lokasiLomba);
        tglbataspendaftaran = findViewById(R.id.et_bataspendaftaran);
        etBiayaPendaftaran = findViewById(R.id.et_BiayaPendaftaran);
        etLinkPendaftaran = findViewById(R.id.et_LinkPendaftaran);
        etLinkPanduanatauGuidebookLomba = findViewById(R.id.et_linkPanduanatauGuidebookLomba);
        etMasukkanHadiahPerlombaan = findViewById(R.id.et_masukkanhadiahperlombaan);
        etNamaPenyelenggara = findViewById(R.id.et_namaPenyelenggara);
        etLinkProfilePenyelenggara = findViewById(R.id.et_linkProfilePenyelenggara);
        etMasukkanNoWhatsApp = findViewById(R.id.et_masukkanNoWhatsApp);
        etMasukkanUsernameIG = findViewById(R.id.et_masukkanusernameig);
        etKeterangan = findViewById(R.id.et_keterangan);

        bIndividu = findViewById(R.id.bIndividu);
        bTim = findViewById(R.id.bTim);

        btnSertifikat = findViewById(R.id.btn_sertifikat);
        btnSertifikatDanTunai = findViewById(R.id.btn_sertifikatdantunai);
        btnSertifikatDanHadiahMenarik = findViewById(R.id.btn_sertifikatdanhadiahmenariklainnya);
        btnSertifikatTunaiDanHadiahMenarik = findViewById(R.id.btn_sertifikattunaidanhadiahmenariklainnnya);

        radioGratis = findViewById(R.id.Gratis);
        radioBerbayar = findViewById(R.id.Berbayar);
        radioNasional = findViewById(R.id.Nasional);
        radioInternasional = findViewById(R.id.Internasional);

        imgProfile = findViewById(R.id.img_profile);
        iconCamera = findViewById(R.id.icon_camera);
        iconPoster = findViewById(R.id.icon_poster);
        BackButton = findViewById(R.id.back_to_mainactivity);

        // Set default values
        radioNasional.setChecked(true);
        radioGratis.setChecked(true);
        etBiayaPendaftaran.setVisibility(View.GONE);
    }

    private void setupHintTexts() {
        // Set hint untuk semua CustomEditText
        if (etNamaLomba != null) etNamaLomba.setHint("Nama lomba");
        if (etLokasiLomba != null) etLokasiLomba.setHint("Lokasi lomba");
        if (tglbataspendaftaran != null) tglbataspendaftaran.setHint("Tanggal Batas Pendaftaran");
        if (etBiayaPendaftaran != null) etBiayaPendaftaran.setHint("Biaya pendaftaran (Angka)");
        if (etLinkPendaftaran != null) etLinkPendaftaran.setHint("Link pendaftaran");
        if (etLinkPanduanatauGuidebookLomba != null) etLinkPanduanatauGuidebookLomba.setHint("Link Panduan/Guidebook Lomba");
        if (etMasukkanHadiahPerlombaan != null) etMasukkanHadiahPerlombaan.setHint("Masukkan Hadiah Perlombaan");
        if (etNamaPenyelenggara != null) etNamaPenyelenggara.setHint("Nama Penyelenggara");
        if (etLinkProfilePenyelenggara != null) etLinkProfilePenyelenggara.setHint("Link Profil Penyelenggara");
        if (etMasukkanNoWhatsApp != null) etMasukkanNoWhatsApp.setHint("Masukkan nomor WhatsApp");
        if (etMasukkanUsernameIG != null) etMasukkanUsernameIG.setHint("Username Instagram (Opsional)");
        if (etKeterangan != null) etKeterangan.setHint("Keterangan tambahan");

        // Set hint untuk button kategori peserta
        if (bIndividu != null) bIndividu.setText("Individu");
        if (bTim != null) bTim.setText("Tim");

        // Set hint untuk button hadiah
        if (btnSertifikat != null) btnSertifikat.setText("Sertifikat");
        if (btnSertifikatDanTunai != null) btnSertifikatDanTunai.setText("Sertifikat dan Tunai");
        if (btnSertifikatDanHadiahMenarik != null) btnSertifikatDanHadiahMenarik.setText("Sertifikat dan Hadiah Menarik Lainnya");
        if (btnSertifikatTunaiDanHadiahMenarik != null) btnSertifikatTunaiDanHadiahMenarik.setText("Sertifikat, Tunai dan Hadiah Menarik Lainnya");
    }

    private void setupListeners() {
        BackButton.setOnClickListener(v -> finish());

        // Simpan posting button
        Button btnSimpanPosting = findViewById(R.id.btnSimpanPostingLomba);
        btnSimpanPosting.setOnClickListener(v -> validateAndSubmit());

        // Radio button listeners
        radioNasional.setOnClickListener(v -> {
            selectedScope = "nasional";
            radioInternasional.setChecked(false);
        });

        radioInternasional.setOnClickListener(v -> {
            selectedScope = "internasional";
            radioNasional.setChecked(false);
        });

        radioGratis.setOnClickListener(v -> {
            selectedBiayaType = "gratis";
            radioBerbayar.setChecked(false);
            etBiayaPendaftaran.setVisibility(View.GONE);
            etBiayaPendaftaran.setText("");
        });

        radioBerbayar.setOnClickListener(v -> {
            selectedBiayaType = "berbayar";
            radioGratis.setChecked(false);
            etBiayaPendaftaran.setVisibility(View.VISIBLE);
        });

        // Hadiah button listeners
        btnSertifikat.setOnClickListener(v -> selectHadiah("sertifikat"));
        btnSertifikatDanTunai.setOnClickListener(v -> selectHadiah("sertifikat_tunai"));
        btnSertifikatDanHadiahMenarik.setOnClickListener(v -> selectHadiah("sertifikat_hadiah"));
        btnSertifikatTunaiDanHadiahMenarik.setOnClickListener(v -> selectHadiah("sertifikat_tunai_hadiah"));
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imgProfile.setImageURI(uri);

                        // Sembunyikan teks panduan
                        View tvTitle = findViewById(R.id.tvTitle);
                        View tvSubtitle = findViewById(R.id.tvSubtitle);
                        if (tvTitle != null) tvTitle.setVisibility(View.GONE);
                        if (tvSubtitle != null) tvSubtitle.setVisibility(View.GONE);
                        iconPoster.setVisibility(View.GONE);
                    }
                }
        );

        iconPoster.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        iconCamera.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        imgProfile.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void setupProfileImagePicker() {
        pickProfileImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedProfileUri = uri;
                        ImageView profileImage = findViewById(R.id.profile);
                        if (profileImage != null) {
                            profileImage.setImageURI(uri);
                            Log.d("PROFILE_IMAGE", "Profile image selected: " + uri.toString());
                        }
                    }
                }
        );

        Button btnTambahHari = findViewById(R.id.btnTambahHari);
        if (btnTambahHari != null) {
            btnTambahHari.setOnClickListener(v -> {
                Log.d("PROFILE_IMAGE", "Opening gallery for profile image");
                pickProfileImageLauncher.launch("image/*");
            });
        }

        ImageView profileImageView = findViewById(R.id.profile);
        if (profileImageView != null) {
            profileImageView.setOnClickListener(v -> {
                Log.d("PROFILE_IMAGE", "Profile image clicked, opening gallery");
                pickProfileImageLauncher.launch("image/*");
            });
        }
    }

    private void setupDatePicker() {
        tglbataspendaftaran.setFocusable(false);
        tglbataspendaftaran.setOnCustomClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%04d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    tglbataspendaftaran.setText(formattedDate);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void setupCategorySelection() {
        bIndividu.setOnClickListener(v -> {
            selectedLombaType = "individual";
            bIndividu.setBackgroundResource(R.drawable.custom_selected_item_kategori_peserta);
            bTim.setBackgroundResource(R.drawable.custom_bbg_transparant_border);
        });

        bTim.setOnClickListener(v -> {
            selectedLombaType = "team";
            bTim.setBackgroundResource(R.drawable.custom_selected_item_kategori_peserta);
            bIndividu.setBackgroundResource(R.drawable.custom_bbg_transparant_border);
        });
    }

    private void setupTagSelection() {
        LinearLayout containerTags = findViewById(R.id.containerTags);
        for (int i = 0; i < containerTags.getChildCount(); i++) {
            View child = containerTags.getChildAt(i);
            if (child instanceof Button) {
                Button tagButton = (Button) child;
                tagButton.setOnClickListener(v -> {
                    String tag = tagButton.getText().toString();
                    if (selectedTags.contains(tag)) {
                        selectedTags.remove(tag);
                        tagButton.setBackgroundResource(R.drawable.year_item_background);
                    } else {
                        selectedTags.add(tag);
                        tagButton.setBackgroundResource(R.drawable.year_item_background_selected);
                    }
                });
            }
        }
    }

    private void selectHadiah(String hadiahType) {
        selectedHadiah = hadiahType;

        btnSertifikat.setBackgroundResource(R.drawable.custom_bbg_transparant_border);
        btnSertifikatDanTunai.setBackgroundResource(R.drawable.custom_bbg_transparant_border);
        btnSertifikatDanHadiahMenarik.setBackgroundResource(R.drawable.custom_bbg_transparant_border);
        btnSertifikatTunaiDanHadiahMenarik.setBackgroundResource(R.drawable.custom_bbg_transparant_border);

        switch (hadiahType) {
            case "sertifikat":
                btnSertifikat.setBackgroundResource(R.drawable.custom_bbg_transparant_border_selected);
                break;
            case "sertifikat_tunai":
                btnSertifikatDanTunai.setBackgroundResource(R.drawable.custom_bbg_transparant_border_selected);
                break;
            case "sertifikat_hadiah":
                btnSertifikatDanHadiahMenarik.setBackgroundResource(R.drawable.custom_bbg_transparant_border_selected);
                break;
            case "sertifikat_tunai_hadiah":
                btnSertifikatTunaiDanHadiahMenarik.setBackgroundResource(R.drawable.custom_bbg_transparant_border_selected);
                break;
        }
    }

    private void validateAndSubmit() {
        // Validasi input
        if (TextUtils.isEmpty(etNamaLomba.getText().toString().trim())) {
            etNamaLomba.setError("Nama lomba harus diisi");
            return;
        }

        if (TextUtils.isEmpty(etLokasiLomba.getText().toString().trim())) {
            etLokasiLomba.setError("Lokasi lomba harus diisi");
            return;
        }

        if (TextUtils.isEmpty(tglbataspendaftaran.getText().toString().trim())) {
            tglbataspendaftaran.setError("Tanggal batas pendaftaran harus diisi");
            return;
        }

        if (TextUtils.isEmpty(etNamaPenyelenggara.getText().toString().trim())) {
            etNamaPenyelenggara.setError("Nama penyelenggara harus diisi");
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Poster lomba harus diunggah", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading
        Toast.makeText(this, "Mengunggah data...", Toast.LENGTH_SHORT).show();

        // Konversi tag menjadi string
        String kategori = TextUtils.join(", ", selectedTags);
        if (kategori.isEmpty()) {
            kategori = "Umum";
        }

        // Ambil data hadiah
        String hadiah = "";
        if (!TextUtils.isEmpty(etMasukkanHadiahPerlombaan.getText().toString().trim())) {
            hadiah = etMasukkanHadiahPerlombaan.getText().toString().trim();
        } else {
            switch (selectedHadiah) {
                case "sertifikat":
                    hadiah = "Sertifikat";
                    break;
                case "sertifikat_tunai":
                    hadiah = "Sertifikat + Uang Tunai";
                    break;
                case "sertifikat_hadiah":
                    hadiah = "Sertifikat + Hadiah Menarik";
                    break;
                case "sertifikat_tunai_hadiah":
                    hadiah = "Sertifikat + Uang Tunai + Hadiah Menarik";
                    break;
                default:
                    hadiah = "Sertifikat";
            }
        }

        // Tentukan biaya dan harga
        String biaya = selectedBiayaType;
        String harga_lomba = "";

        if (selectedBiayaType.equals("berbayar") &&
                !TextUtils.isEmpty(etBiayaPendaftaran.getText().toString().trim())) {
            harga_lomba = etBiayaPendaftaran.getText().toString().trim();
        }

        // Dapatkan user_id dari ApiHelper
        String userId = ApiHelper.getSavedUserId();
        if (TextUtils.isEmpty(userId)) {
            userId = "A00847458";
        }

        try {
            // Konversi URI ke File untuk poster lomba
            File posterFile = createFileFromUri(selectedImageUri, "poster_lomba.jpg");

            // Kirim data ke API menggunakan metode yang sudah ada di ApiHelper
            sendKompetisiData(posterFile, kategori, hadiah, biaya, harga_lomba, userId);

        } catch (Exception e) {
            Log.e("VALIDATION", "Error creating file", e);
            Toast.makeText(this, "Terjadi kesalahan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendKompetisiData(File posterFile, String kategori, String hadiah,
                                   String biaya, String harga_lomba, String userId) {

        // Debug log
        Log.d("KOMPETISI_DATA", "User ID: " + userId);
        Log.d("KOMPETISI_DATA", "Lomba Type: " + selectedLombaType);
        Log.d("KOMPETISI_DATA", "Scope: " + selectedScope);
        Log.d("KOMPETISI_DATA", "Biaya: " + selectedBiayaType);
        Log.d("KOMPETISI_DATA", "Kategori: " + kategori);

        // Hitung harga sebagai integer
        int hargaInt = 0;
        if (!TextUtils.isEmpty(harga_lomba)) {
            try {
                // Hapus titik jika ada (misal: 100.000)
                String cleanHarga = harga_lomba.replace(".", "").replace(",", "");
                hargaInt = Integer.parseInt(cleanHarga);
                Log.d("KOMPETISI_DATA", "Harga parsed: " + hargaInt);
            } catch (NumberFormatException e) {
                Log.e("KOMPETISI_DATA", "Error parsing harga: " + e.getMessage());
                hargaInt = 0;
            }
        }

        // Validasi user_id
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_LONG).show();
            return;
        }

        // Gunakan method baru untuk kompetisi
        ApiHelper.createKompetisiWithImage(
                etNamaLomba.getText().toString().trim(), // nama_lomba
                etKeterangan.getText().toString().trim(), // deskripsi
                tglbataspendaftaran.getText().toString().trim(), // tanggal_lomba
                etLokasiLomba.getText().toString().trim(), // lokasi
                etNamaPenyelenggara.getText().toString().trim(), // penyelenggara
                kategori, // kategori
                hadiah, // hadiah
                selectedLombaType, // lomba_type (individual/team)
                selectedScope, // scope (nasional/internasional)
                selectedBiayaType, // biaya (gratis/berbayar)
                String.valueOf(hargaInt), // harga (integer)
                posterFile, // posterFile
                etLinkPendaftaran.getText().toString().trim(), // link_pendaftaran
                etLinkPanduanatauGuidebookLomba.getText().toString().trim(), // link_panduan
                etMasukkanNoWhatsApp.getText().toString().trim(), // whatsapp
                etMasukkanUsernameIG.getText().toString().trim(), // instagram
                etKeterangan.getText().toString().trim(), // keterangan
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        runOnUiThread(() -> {
                            Log.d("API_SUCCESS", "Response: " + result);
                            try {
                                JSONObject response = new JSONObject(result);
                                boolean success = response.optBoolean("success", false);
                                String message = response.optString("message", "");

                                if (success) {
                                    Toast.makeText(TambahPostingLomba.this,
                                            "✅ Lomba berhasil diposting! Menunggu konfirmasi admin.",
                                            Toast.LENGTH_LONG).show();

                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("posted", true);
                                    setResult(RESULT_OK, resultIntent);

                                    // Tunggu sebentar sebelum close
                                    new Handler().postDelayed(() -> finish(), 1500);
                                } else {
                                    Toast.makeText(TambahPostingLomba.this,
                                            "❌ Gagal memposting: " + message,
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Log.e("API_RESPONSE", "Error parsing JSON", e);
                                // Jika response bukan JSON, anggap berhasil
                                Toast.makeText(TambahPostingLomba.this,
                                        "✅ Berhasil diposting!",
                                        Toast.LENGTH_SHORT).show();

                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("posted", true);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Log.e("API_ERROR", "Upload failed: " + error);
                            Toast.makeText(TambahPostingLomba.this,
                                    "❌ Gagal mengunggah: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    private File createFileFromUri(Uri uri, String fileName) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File file = new File(getCacheDir(), fileName);
        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        Log.d("FILE_CREATION", "File created: " + file.getAbsolutePath() +
                ", size: " + file.length() + " bytes");

        return file;
    }
}