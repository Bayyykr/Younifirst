package com.naufal.younifirst.Event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.naufal.younifirst.R;
import com.naufal.younifirst.custom.CustomEditText;

import java.util.Calendar;

public class BuatEvent extends AppCompatActivity {

    private CustomEditText etNamaEvent, etLokasiEvent, etLinkPendaftaran,
            etTglMulai, etWktMulai, tglTutup, wktTutup,
            etKeterangan, etHargaTiket;

    private LinearLayout containerTags;
    private String selectedTag = null;
    private ShapeableImageView imgProfile;
    private ImageView iconCamera, iconPoster, backToMain;
    private RadioButton rbGratis, rbBerbayar;

    private LinearLayout containerHariEvent;
    private Button btnTambahHari;
    private LayoutInflater inflater;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_event);

        etNamaEvent = findViewById(R.id.et_namaevent);
        etLokasiEvent = findViewById(R.id.et_lokasievent);
        etLinkPendaftaran = findViewById(R.id.et_linkPendaftaran);
        etTglMulai = findViewById(R.id.tglmulai);
        etWktMulai = findViewById(R.id.wktmulai);
        tglTutup = findViewById(R.id.tgltutup);
        wktTutup = findViewById(R.id.wkttutup);
        etKeterangan = findViewById(R.id.et_keterangan);
        etHargaTiket = findViewById(R.id.et_hargatiket);
        rbGratis = findViewById(R.id.Gratis);
        rbBerbayar = findViewById(R.id.Berbayar);
        imgProfile = findViewById(R.id.img_profile);
        iconCamera = findViewById(R.id.icon_camera);
        iconPoster = findViewById(R.id.icon_poster);
        backToMain = findViewById(R.id.back_to_mainactivity);

        ImageButton btnInstagram = findViewById(R.id.BtnInstagram);
        ImageButton btnWhatsApp = findViewById(R.id.BtnWhatsApp);
        CustomEditText etWA = findViewById(R.id.et_wa);
        CustomEditText etInstagram = findViewById(R.id.et_instagram);

        etWA.setVisibility(View.GONE);
        etInstagram.setVisibility(View.GONE);

        final boolean[] isInstagramSelected = {false};
        final boolean[] isWASelected = {false};

        btnInstagram.setOnClickListener(v -> {
            isInstagramSelected[0] = !isInstagramSelected[0];

            if (isInstagramSelected[0]) {
                btnInstagram.setBackgroundResource(R.drawable.year_item_background_selected);
                etInstagram.setVisibility(View.VISIBLE);
                etInstagram.setHint("Masukkan username instagram Anda");
            } else {
                btnInstagram.setBackgroundResource(R.drawable.custom_button_chat_forum);
                etInstagram.setVisibility(View.GONE);
            }
        });

        btnWhatsApp.setOnClickListener(v -> {
            isWASelected[0] = !isWASelected[0];

            if (isWASelected[0]) {
                btnWhatsApp.setBackgroundResource(R.drawable.year_item_background_selected);
                etWA.setVisibility(View.VISIBLE);
                etWA.setHint("Masukkan nomor WhatsApp Anda");
            } else {
                btnWhatsApp.setBackgroundResource(R.drawable.custom_button_chat_forum);
                etWA.setVisibility(View.GONE);
            }
        });

        etNamaEvent.setHint("Nama Event");
        etLokasiEvent.setHint("Lokasi");
        etLinkPendaftaran.setHint("Link Pendaftaran");
        etTglMulai.setHint("Tanggal Mulai");
        etWktMulai.setHint("Waktu Mulai");
        tglTutup.setHint("Tanggal Tutup");
        wktTutup.setHint("Waktu Tutup");
        etKeterangan.setHint("Keterangan");
        etHargaTiket.setHint("Harga Tiket");
        etHargaTiket.setVisibility(View.GONE);
        etHargaTiket.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

        setupImagePicker();
        setupRadioButtons();

        iconPoster.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        imgProfile.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        iconCamera.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        backToMain.setOnClickListener(v -> onBackPressed());

        containerHariEvent = findViewById(R.id.containerHariEvent);
        btnTambahHari = findViewById(R.id.btnTambahHari);
        inflater = LayoutInflater.from(this);
        btnTambahHari.setOnClickListener(v -> tambahHariEvent());

        setupDatePicker(etTglMulai);
        setupDatePicker(tglTutup);
        setupTimePicker(etWktMulai);
        setupTimePicker(wktTutup);

        setupTagButtons();
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imgProfile.setImageURI(uri);
                        findViewById(R.id.tvTitle).setVisibility(View.GONE);
                        findViewById(R.id.tvSubtitle).setVisibility(View.GONE);
                        findViewById(R.id.icon_poster).setVisibility(View.GONE);
                    }
                }
        );
    }

    private void setupRadioButtons() {
        View.OnClickListener radioClickListener = v -> {
            if (v.getId() == R.id.Gratis) {
                rbGratis.setChecked(true);
                rbBerbayar.setChecked(false);
                etHargaTiket.setVisibility(View.GONE);
            } else if (v.getId() == R.id.Berbayar) {
                rbBerbayar.setChecked(true);
                rbGratis.setChecked(false);
                etHargaTiket.setVisibility(View.VISIBLE);
            }
        };
        rbGratis.setOnClickListener(radioClickListener);
        rbBerbayar.setOnClickListener(radioClickListener);
    }

    private void tambahHariEvent() {
        View item = inflater.inflate(R.layout.custom_tambah_hari_buat_event, containerHariEvent, false);

        int nomorHari = containerHariEvent.getChildCount() + 1;

        CustomEditText etTanggal = item.findViewById(R.id.et_tanggalEventHariKe1);
        CustomEditText etMulai = item.findViewById(R.id.et_waktuMulai);
        CustomEditText etSelesai = item.findViewById(R.id.et_waktuSelesai);
        ImageView close = item.findViewById(R.id.close);
        android.widget.TextView tvHari = item.findViewById(R.id.hari);

        etTanggal.setHint("Tanggal event hari ke-" + nomorHari);
        etMulai.setHint("Waktu Mulai");
        etSelesai.setHint("Waktu Selesai");
        tvHari.setText("Hari " + nomorHari);

        setupDatePicker(etTanggal);
        setupTimePicker(etMulai);
        setupTimePicker(etSelesai);

        close.setOnClickListener(v -> {
            containerHariEvent.removeView(item);
            updateNomorHari();
        });

        containerHariEvent.addView(item);
    }

    private void updateNomorHari() {
        for (int i = 0; i < containerHariEvent.getChildCount(); i++) {
            View item = containerHariEvent.getChildAt(i);
            CustomEditText etTanggal = item.findViewById(R.id.et_tanggalEventHariKe1);
            android.widget.TextView tvHari = item.findViewById(R.id.hari);

            int nomorHari = i + 1;
            tvHari.setText("Hari " + nomorHari);
            etTanggal.setHint("Tanggal event hari ke-" + nomorHari);
        }
    }

    private void setupDatePicker(CustomEditText customEditText) {
        customEditText.setFocusable(false);
        customEditText.setOnCustomClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (dateView, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear);
                        customEditText.setText(formattedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupTimePicker(CustomEditText customEditText) {
        customEditText.setFocusable(false);
        customEditText.setOnCustomClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (timeView, selectedHour, selectedMinute) -> {
                        String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                        customEditText.setText(formattedTime);
                    }, hour, minute, true);
            timePickerDialog.show();
        });
    }

    private void setupTagButtons() {
        containerTags = findViewById(R.id.containerTags);

        for (int i = 0; i < containerTags.getChildCount(); i++) {
            View child = containerTags.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                btn.setOnClickListener(v -> {
                    for (int j = 0; j < containerTags.getChildCount(); j++) {
                        View otherChild = containerTags.getChildAt(j);
                        if (otherChild instanceof Button) {
                            Button otherBtn = (Button) otherChild;
                            otherBtn.setBackgroundResource(R.drawable.year_item_background);
                            otherBtn.setTextColor(getResources().getColor(android.R.color.white));
                        }
                    }

                    btn.setBackgroundResource(R.drawable.year_item_background_selected);
                    btn.setTextColor(getResources().getColor(R.color.primary_color));

                    selectedTag = btn.getText().toString();
                });
            }
        }
    }

}
