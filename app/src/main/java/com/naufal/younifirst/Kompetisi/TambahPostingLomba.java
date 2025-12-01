package com.naufal.younifirst.Kompetisi;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.naufal.younifirst.R;
import com.naufal.younifirst.custom.CustomEditText;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TambahPostingLomba extends AppCompatActivity {

    private ImageView BackButton, iconCamera, iconPoster;
    private ShapeableImageView imgProfile;

    private CustomEditText etNamaLomba, etLokasiLomba,
            tglAwal, tglAkhir, etBiayaPendaftaran, etLinkPendaftaran, etLinkPanduanatauGuidebookLomba,
            etMasukkanHadiahPerlombaan, etNamaPenyelenggara, etLinkProfilePenyelenggara,
            etMasukkanNoWhatsApp, etMasukkanUsernameIG, etKeterangan;

    private Button bIndividu, bTim, bTimdanIndividu;
    private Button btnSertifikat, btnSertifikatDanTunai, btnSertifikatDanHadiahMenarik,
            btnSertifikatTunaiDanHadiahMenarik;

    private RadioButton radioGratis, radioBerbayar, radioNasional, radioInternasional;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> pickExtraImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_kompetisi_buat_postingan_lomba);

        etNamaLomba = findViewById(R.id.et_namalomba);
        etLokasiLomba = findViewById(R.id.et_lokasiLomba);
        tglAwal = findViewById(R.id.tglAwal);
        tglAkhir = findViewById(R.id.tglAkhir);
        BackButton = findViewById(R.id.back_to_mainactivity);
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
        bTimdanIndividu = findViewById(R.id.bTimdanIndividu);

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

        BackButton.setOnClickListener(v -> finish());

        setupImagePicker();

        pickExtraImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imgProfile.setImageURI(uri);
                    }
                }
        );

        iconPoster.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        iconCamera.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        imgProfile.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        etNamaLomba.setHint("Nama lomba");
        etLokasiLomba.setHint("Lokasi lomba");
        tglAwal.setHint("Tanggal Awal");
        tglAkhir.setHint("Tanggal Akhir");
        etBiayaPendaftaran.setHint("Biaya pendaftaran");
        etLinkPendaftaran.setHint("Link pendaftaran");
        etLinkPanduanatauGuidebookLomba.setHint("Link Panduan/Guidebook Lomba");
        etMasukkanHadiahPerlombaan.setHint("Masukkan Hadiah Perlombaan");
        etNamaPenyelenggara.setHint("Nama Penyelenggara");
        etLinkProfilePenyelenggara.setHint("Link Profil Penyelenggara");
        etMasukkanNoWhatsApp.setHint("Masukkan nomor WhatsApp");
        etMasukkanUsernameIG.setHint("Username Instagram (Opsional)");
        etKeterangan.setHint("Keterangan tambahan");

        bIndividu.setText("Individu");
        bTim.setText("Tim");
        bTimdanIndividu.setText("Tim & Individu");

        Button btnSimpanPosting = findViewById(R.id.btnSimpanPostingLomba);

        btnSimpanPosting.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("posted", true);
            setResult(RESULT_OK, result);
            finish();
        });

        setupCategoryButtons();
        setupHadiahButtons();
        setupRadioButtons();
        setupRadioButtonWilayah();
        setupDatePicker(tglAwal);
        setupDatePicker(tglAkhir);

        LinearLayout containerTags = findViewById(R.id.containerTags);
        for (int i = 0; i < containerTags.getChildCount(); i++) {
            View child = containerTags.getChildAt(i);
            if (child instanceof Button) {
                Button tagButton = (Button) child;
                tagButton.setOnClickListener(v -> {
                    for (int j = 0; j < containerTags.getChildCount(); j++) {
                        View c = containerTags.getChildAt(j);
                        if (c instanceof Button) {
                            c.setBackgroundResource(R.drawable.year_item_background);
                        }
                    }
                    tagButton.setBackgroundResource(R.drawable.year_item_background_selected);
                });
            }
        }

        Button btnTambahHari = findViewById(R.id.btnTambahHari);
        ImageView profileImage = findViewById(R.id.profile);
        ActivityResultLauncher<String> pickImageLauncher2 = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImage.setImageURI(uri);
                    }
                }
        );

        btnTambahHari.setOnClickListener(v -> pickImageLauncher2.launch("image/*"));
    }

    private void setupCategoryButtons() {
        List<Button> categoryButtons = Arrays.asList(bIndividu, bTim, bTimdanIndividu);

        for (Button btn : categoryButtons) {
            btn.setOnClickListener(v -> {
                for (Button b : categoryButtons) {
                    b.setBackgroundResource(R.drawable.custom_bbg_transparant_border);
                }
                btn.setBackgroundResource(R.drawable.custom_selected_item_kategori_peserta);
            });
        }
    }

    private void setupHadiahButtons() {
        List<Button> hadiahButtons = Arrays.asList(
                btnSertifikat,
                btnSertifikatDanTunai,
                btnSertifikatDanHadiahMenarik,
                btnSertifikatTunaiDanHadiahMenarik
        );

        for (Button btn : hadiahButtons) {
            btn.setOnClickListener(v -> {
                for (Button b : hadiahButtons) {
                    b.setBackgroundResource(R.drawable.custom_bbg_transparant_border);
                }
                btn.setBackgroundResource(R.drawable.custom_bbg_transparant_border_selected);
            });
        }
    }

    private void setupRadioButtons() {
        etBiayaPendaftaran.setVisibility(View.GONE);

        View.OnClickListener radioClickListener = v -> {
            if (v.getId() == R.id.Gratis) {
                radioGratis.setChecked(true);
                radioBerbayar.setChecked(false);
                etBiayaPendaftaran.setVisibility(View.GONE);

            } else if (v.getId() == R.id.Berbayar) {
                radioBerbayar.setChecked(true);
                radioGratis.setChecked(false);
                etBiayaPendaftaran.setVisibility(View.VISIBLE);
            }
        };

        radioGratis.setOnClickListener(radioClickListener);
        radioBerbayar.setOnClickListener(radioClickListener);
    }

    private void setupRadioButtonWilayah() {
        radioNasional.setOnClickListener(v -> radioInternasional.setChecked(false));
        radioInternasional.setOnClickListener(v -> radioNasional.setChecked(false));
    }

    private void setupDatePicker(CustomEditText targetEditText) {
        targetEditText.setFocusable(false);

        targetEditText.setOnCustomClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%02d/%02d/%04d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        targetEditText.setText(formattedDate);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imgProfile.setImageURI(uri);

                        View tvTitle = findViewById(R.id.tvTitle);
                        View tvSubtitle = findViewById(R.id.tvSubtitle);

                        if (tvTitle != null) tvTitle.setVisibility(View.GONE);
                        if (tvSubtitle != null) tvSubtitle.setVisibility(View.GONE);

                        iconPoster.setVisibility(View.GONE);
                    }
                }
        );
    }
}