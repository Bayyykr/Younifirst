package com.naufal.younifirst.Event;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.naufal.younifirst.LognReg.login;
import com.naufal.younifirst.R;
import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.controller.EventController;
import com.naufal.younifirst.custom.CustomEditText;
import com.naufal.younifirst.model.Event;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class BuatEvent extends AppCompatActivity {

    private static final String TAG = "BuatEvent";
    private File selectedImageFile;

    private CustomEditText etNamaEvent, etLokasiEvent, etLinkPendaftaran,
            etTglMulai, etWktMulai,
            etKeterangan, etHargaTiket, etKapasitas, etOrganizer,
            etWA, etInstagram;

    private LinearLayout containerTags;
    private Set<String> selectedTags = new HashSet<>();
    private ShapeableImageView imgProfile;
    private ImageView iconCamera, iconPoster, backToMain;
    private RadioButton rbGratis, rbBerbayar;
    private Button btnSimpan;
    private ImageButton btnInstagram, btnWhatsApp;

    private LinearLayout containerHariEvent;
    private Button btnTambahHari;
    private LayoutInflater inflater;

    private ActivityResultLauncher<String> pickImageLauncher;
    private EventController eventController;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_event);

        Log.d(TAG, "🚀 BuatEvent Activity dimulai");

        // Initialize ApiHelper
        ApiHelper.initialize(getApplicationContext());

        // Check if user is logged in (has user_id)
        checkUserLoggedIn();

        initViews();
        setupImagePicker();
        setupRadioButtons();
        setupDatePickers();
        setupTimePickers();
        setupTagButtons();
        setupSimpanButton();
        setupBackButton();
        setupSocialMediaButtons();
    }

    private void checkUserLoggedIn() {
        Log.d(TAG, "🔍 Memeriksa apakah user sudah login...");

        String userId = ApiHelper.getSavedUserId();
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "⚠ User belum login atau tidak ada user_id");
            showLoginRequiredDialog();
        } else {
            Log.d(TAG, "✅ User sudah login, user_id: " + userId);
            eventController = new EventController();
        }
    }

    private void showLoginRequiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Login Diperlukan")
                .setMessage("Anda harus login terlebih dahulu untuk membuat event.")
                .setPositiveButton("Login", (dialog, which) -> {
                    Intent intent = new Intent(this, login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void initViews() {
        Log.d(TAG, "🔄 Menginisialisasi views...");

        etNamaEvent = findViewById(R.id.et_namaevent);
        etLokasiEvent = findViewById(R.id.et_lokasievent);
        etLinkPendaftaran = findViewById(R.id.et_linkPendaftaran);
        etTglMulai = findViewById(R.id.tglmulai);
        etWktMulai = findViewById(R.id.wktmulai);
        etKeterangan = findViewById(R.id.et_keterangan);
        etHargaTiket = findViewById(R.id.et_hargatiket);
        etKapasitas = findViewById(R.id.et_kapasitas);
        etOrganizer = findViewById(R.id.et_penyelenggara);
        etWA = findViewById(R.id.et_wa);
        etInstagram = findViewById(R.id.et_instagram);
        rbGratis = findViewById(R.id.Gratis);
        rbBerbayar = findViewById(R.id.Berbayar);
        imgProfile = findViewById(R.id.img_profile);
        iconCamera = findViewById(R.id.icon_camera);
        iconPoster = findViewById(R.id.icon_poster);
        backToMain = findViewById(R.id.back_to_mainactivity);
        btnSimpan = findViewById(R.id.btnBuatEvent);
        btnInstagram = findViewById(R.id.BtnInstagram);
        btnWhatsApp = findViewById(R.id.BtnWhatsApp);

        // Set hints
        if (etNamaEvent != null) etNamaEvent.setHint("Nama Event");
        if (etLokasiEvent != null) etLokasiEvent.setHint("Lokasi Event");
        if (etLinkPendaftaran != null) etLinkPendaftaran.setHint("Link Pendaftaran (Opsional)");
        if (etTglMulai != null) etTglMulai.setHint("Tanggal Mulai Event");
        if (etWktMulai != null) etWktMulai.setHint("Waktu Mulai Event");
        if (etKeterangan != null) etKeterangan.setHint("Deskripsi Event");
        if (etHargaTiket != null) etHargaTiket.setHint("Harga Tiket (Rp)");
        if (etKapasitas != null) etKapasitas.setHint("Kapasitas Peserta");
        if (etOrganizer != null) etOrganizer.setHint("Nama Penyelenggara");
        if (etWA != null) etWA.setHint("Nomor WhatsApp (Opsional)");
        if (etInstagram != null) etInstagram.setHint("Username Instagram (Opsional)");

        // Setup input types
        if (etHargaTiket != null) {
            etHargaTiket.setVisibility(View.GONE);
            etHargaTiket.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        if (etKapasitas != null) {
            etKapasitas.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        if (etWA != null) etWA.setVisibility(View.GONE);
        if (etInstagram != null) etInstagram.setVisibility(View.GONE);

        containerHariEvent = findViewById(R.id.containerHariEvent);
        btnTambahHari = findViewById(R.id.btnTambahHari);
        inflater = LayoutInflater.from(this);

        if (btnTambahHari != null) {
            btnTambahHari.setOnClickListener(v -> tambahHariEvent());
        }

        containerTags = findViewById(R.id.containerTags);
        if (containerTags != null) {
            Log.d(TAG, "🏷 Container tags ditemukan: " + containerTags.getChildCount() + " tags");
        }

        Log.d(TAG, "✅ Semua views berhasil diinisialisasi");
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imgProfile.setImageURI(uri);
                        View tvTitle = findViewById(R.id.tvTitle);
                        View tvSubtitle = findViewById(R.id.tvSubtitle);
                        View iconPosterView = findViewById(R.id.icon_poster);

                        if (tvTitle != null) tvTitle.setVisibility(View.GONE);
                        if (tvSubtitle != null) tvSubtitle.setVisibility(View.GONE);
                        if (iconPosterView != null) iconPosterView.setVisibility(View.GONE);

                        Log.d(TAG, "📸 Gambar poster dipilih: " + uri.toString());
                    }
                }
        );

        if (iconPoster != null) {
            iconPoster.setOnClickListener(v -> {
                Log.d(TAG, "🖼 Icon poster diklik");
                pickImageLauncher.launch("image/*");
            });
        }

        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> {
                Log.d(TAG, "🖼 Image profile diklik");
                pickImageLauncher.launch("image/*");
            });
        }

        if (iconCamera != null) {
            iconCamera.setOnClickListener(v -> {
                Log.d(TAG, "📷 Icon camera diklik");
                pickImageLauncher.launch("image/*");
            });
        }
    }

    private void setupRadioButtons() {
        View.OnClickListener radioClickListener = v -> {
            if (v.getId() == R.id.Gratis) {
                rbGratis.setChecked(true);
                rbBerbayar.setChecked(false);
                if (etHargaTiket != null) {
                    etHargaTiket.setVisibility(View.GONE);
                    etHargaTiket.setText("");
                }
                Log.d(TAG, "💰 Radio Gratis dipilih");
            } else if (v.getId() == R.id.Berbayar) {
                rbBerbayar.setChecked(true);
                rbGratis.setChecked(false);
                if (etHargaTiket != null) {
                    etHargaTiket.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "💰 Radio Berbayar dipilih");
            }
        };

        if (rbGratis != null) rbGratis.setOnClickListener(radioClickListener);
        if (rbBerbayar != null) rbBerbayar.setOnClickListener(radioClickListener);

        if (rbGratis != null) {
            rbGratis.setChecked(true);
        }
    }

    private void setupSocialMediaButtons() {
        if (btnInstagram == null || btnWhatsApp == null) return;

        final boolean[] isInstagramSelected = {false};
        final boolean[] isWASelected = {false};

        btnInstagram.setOnClickListener(v -> {
            isInstagramSelected[0] = !isInstagramSelected[0];

            if (isInstagramSelected[0]) {
                btnInstagram.setBackgroundResource(R.drawable.year_item_background_selected);
                if (etInstagram != null) {
                    etInstagram.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "📱 Instagram button selected");
            } else {
                btnInstagram.setBackgroundResource(R.drawable.custom_button_chat_forum);
                if (etInstagram != null) {
                    etInstagram.setVisibility(View.GONE);
                    etInstagram.setText("");
                }
                Log.d(TAG, "📱 Instagram button deselected");
            }
        });

        btnWhatsApp.setOnClickListener(v -> {
            isWASelected[0] = !isWASelected[0];

            if (isWASelected[0]) {
                btnWhatsApp.setBackgroundResource(R.drawable.year_item_background_selected);
                if (etWA != null) {
                    etWA.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "📱 WhatsApp button selected");
            } else {
                btnWhatsApp.setBackgroundResource(R.drawable.custom_button_chat_forum);
                if (etWA != null) {
                    etWA.setVisibility(View.GONE);
                    etWA.setText("");
                }
                Log.d(TAG, "📱 WhatsApp button deselected");
            }
        });
    }

    private void setupDatePickers() {
        if (etTglMulai != null) setupDatePicker(etTglMulai);
    }

    private void setupTimePickers() {
        if (etWktMulai != null) setupTimePicker(etWktMulai);
    }

    private void setupDatePicker(CustomEditText customEditText) {
        if (customEditText == null) return;

        customEditText.setFocusable(false);
        customEditText.setOnCustomClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (dateView, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        customEditText.setText(formattedDate);
                        Log.d(TAG, "📅 Tanggal dipilih: " + formattedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupTimePicker(CustomEditText customEditText) {
        if (customEditText == null) return;

        customEditText.setFocusable(false);
        customEditText.setOnCustomClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (timeView, selectedHour, selectedMinute) -> {
                        String formattedTime = String.format("%02d:%02d:00", selectedHour, selectedMinute);
                        customEditText.setText(formattedTime);
                        Log.d(TAG, "⏰ Waktu dipilih: " + formattedTime);
                    }, hour, minute, true);
            timePickerDialog.show();
        });
    }

    private void tambahHariEvent() {
        if (containerHariEvent == null || inflater == null) return;

        View item = inflater.inflate(R.layout.custom_tambah_hari_buat_event, containerHariEvent, false);

        int nomorHari = containerHariEvent.getChildCount() + 1;

        CustomEditText etTanggal = item.findViewById(R.id.et_tanggalEvent);
        CustomEditText etMulai = item.findViewById(R.id.et_waktuMulai);
        ImageView close = item.findViewById(R.id.close);
        TextView tvHari = item.findViewById(R.id.hari);

        if (nomorHari == 1) {
            tvHari.setText("Hari pertama");
        } else if (nomorHari == 2) {
            tvHari.setText("Hari kedua");
        } else {
            tvHari.setText("Hari ke-" + nomorHari);
        }

        if (etTanggal != null) etTanggal.setHint("Tanggal event");
        if (etMulai != null) etMulai.setHint("Waktu Mulai");

        if (nomorHari == 1 && etTglMulai != null && !etTglMulai.getText().toString().isEmpty()) {
            if (etTanggal != null) {
                etTanggal.setText(etTglMulai.getText().toString());
            }
        }

        setupDatePicker(etTanggal);
        setupTimePicker(etMulai);

        if (close != null) {
            close.setOnClickListener(v -> {
                containerHariEvent.removeView(item);
                updateHariTitles();
                Log.d(TAG, "🗑 Hari event dihapus");
            });
        }

        containerHariEvent.addView(item);
        Log.d(TAG, "📅 Hari event ke-" + nomorHari + " ditambahkan");
    }

    private void updateHariTitles() {
        if (containerHariEvent == null) return;

        for (int i = 0; i < containerHariEvent.getChildCount(); i++) {
            View item = containerHariEvent.getChildAt(i);
            TextView tvHari = item.findViewById(R.id.hari);

            if (tvHari != null) {
                int nomorHari = i + 1;
                if (nomorHari == 1) {
                    tvHari.setText("Hari pertama");
                } else if (nomorHari == 2) {
                    tvHari.setText("Hari kedua");
                } else {
                    tvHari.setText("Hari ke-" + nomorHari);
                }
            }
        }
    }

    private void setupTagButtons() {
        containerTags = findViewById(R.id.containerTags);
        if (containerTags == null) {
            Log.e(TAG, "❌ containerTags tidak ditemukan");
            return;
        }

        for (int i = 0; i < containerTags.getChildCount(); i++) {
            View child = containerTags.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                String tagText = btn.getText().toString();

                if (selectedTags.contains(tagText)) {
                    btn.setBackgroundResource(R.drawable.year_item_background_selected);
                    btn.setTextColor(getResources().getColor(R.color.primary_color));
                } else {
                    btn.setBackgroundResource(R.drawable.year_item_background);
                    btn.setTextColor(getResources().getColor(android.R.color.white));
                }

                btn.setOnClickListener(v -> {
                    if (selectedTags.contains(tagText)) {
                        selectedTags.remove(tagText);
                        btn.setBackgroundResource(R.drawable.year_item_background);
                        btn.setTextColor(getResources().getColor(android.R.color.white));
                        Log.d(TAG, "🏷 Tag '" + tagText + "' di-deselect");
                    } else {
                        selectedTags.add(tagText);
                        btn.setBackgroundResource(R.drawable.year_item_background_selected);
                        btn.setTextColor(getResources().getColor(R.color.primary_color));
                        Log.d(TAG, "🏷 Tag '" + tagText + "' di-select");
                    }

                    Log.d(TAG, "📌 Selected tags: " + selectedTags.toString());
                });
            }
        }
    }

    private void setupSimpanButton() {
        if (btnSimpan != null) {
            btnSimpan.setOnClickListener(v -> {
                Log.d(TAG, "🔘 Tombol simpan diklik");
                simpanEvent();
            });
        } else {
            Log.e(TAG, "❌ Tombol simpan tidak ditemukan!");
        }
    }

    private void setupBackButton() {
        if (backToMain != null) {
            backToMain.setOnClickListener(v -> {
                Log.d(TAG, "⬅ Tombol back diklik");
                onBackPressed();
            });
        }
    }

    private void simpanEvent() {
        Log.d(TAG, "💾 ============ SIMPAN EVENT ============");

        // Validasi input
        if (!validateInput()) {
            return;
        }

        if (selectedTags.isEmpty()) {
            Toast.makeText(this, "Pilih minimal 1 tag untuk kategori event", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ambil data dari form
        String namaEvent = etNamaEvent != null ? etNamaEvent.getText().toString().trim() : "";
        String deskripsi = etKeterangan != null ? etKeterangan.getText().toString().trim() : "";
        String tanggalMulai = etTglMulai != null ? etTglMulai.getText().toString().trim() : "";
        String lokasi = etLokasiEvent != null ? etLokasiEvent.getText().toString().trim() : "";
        String organizer = etOrganizer != null ? etOrganizer.getText().toString().trim() : "";
        String linkPendaftaran = etLinkPendaftaran != null ? etLinkPendaftaran.getText().toString().trim() : "";
        String whatsapp = etWA != null ? etWA.getText().toString().trim() : "";
        String instagram = etInstagram != null ? etInstagram.getText().toString().trim() : "";

        // Kapasitas
        int kapasitas = 0;
        if (etKapasitas != null && !etKapasitas.getText().toString().trim().isEmpty()) {
            try {
                kapasitas = Integer.parseInt(etKapasitas.getText().toString().trim());
                if (kapasitas <= 0) {
                    Toast.makeText(this, "Kapasitas harus lebih dari 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Kapasitas harus berupa angka", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "Kapasitas harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kategori dari tags yang dipilih
        String kategori = buildKategoriFromTags();

        // Harga - Sesuaikan dengan controller
        String harga = "0";
        if (rbBerbayar != null && rbBerbayar.isChecked() && etHargaTiket != null) {
            harga = etHargaTiket.getText().toString().trim();
            // Controller membolehkan harga kosong meski berbayar
            if (harga.isEmpty()) {
                harga = "";
            }
        } else if (rbGratis != null && rbGratis.isChecked()) {
            harga = "0";
        }

        // Waktu pelaksanaan
        String waktuPelaksanaan = etWktMulai != null ? etWktMulai.getText().toString().trim() : "";

        // Deadline pendaftaran (gunakan tanggalMulai sebagai default)
        // Controller mengharapkan parameter deadlinePendaftaran
        String deadlinePendaftaran = tanggalMulai;

        String deadlineFull = "";
        if (!deadlinePendaftaran.isEmpty() && !waktuPelaksanaan.isEmpty()) {
            // Pastikan format waktu benar: HH:MM:SS
            String waktuFormatted = formatWaktuUntukDeadline(waktuPelaksanaan);
            deadlineFull = deadlinePendaftaran + " " + waktuFormatted;
            Log.d(TAG, "🕒 Deadline dengan waktu: " + deadlineFull);
        } else if (!deadlinePendaftaran.isEmpty()) {
            deadlineFull = deadlinePendaftaran + " 23:59:59";
            Log.d(TAG, "🕒 Deadline default waktu: " + deadlineFull);
        }

        // Cek apakah user sudah login
        String userId = ApiHelper.getSavedUserId();
        if (userId == null || userId.isEmpty()) {
            showLoginRequiredDialog();
            return;
        }

        Log.d(TAG, "📝 Data yang akan dikirim ke API:");
        Log.d(TAG, "  - Nama Event: " + namaEvent);
        Log.d(TAG, "  - Deskripsi: " + (deskripsi.length() > 50 ? deskripsi.substring(0, 50) + "..." : deskripsi));
        Log.d(TAG, "  - Tanggal Mulai: " + tanggalMulai);
        Log.d(TAG, "  - Lokasi: " + lokasi);
        Log.d(TAG, "  - Organizer: " + organizer);
        Log.d(TAG, "  - Link Pendaftaran: " + linkPendaftaran);
        Log.d(TAG, "  - Kapasitas: " + kapasitas);
        Log.d(TAG, "  - Kategori: " + kategori);
        Log.d(TAG, "  - Harga: " + (harga.isEmpty() ? "NULL/GRATIS" : harga));
        Log.d(TAG, "  - Waktu Pelaksanaan: " + waktuPelaksanaan);
        Log.d(TAG, "  - Deadline Pendaftaran: " + deadlineFull);
        Log.d(TAG, "  - WhatsApp: " + (whatsapp.isEmpty() ? "null" : whatsapp));
        Log.d(TAG, "  - Instagram: " + (instagram.isEmpty() ? "null" : instagram));
        Log.d(TAG, "  - User ID: " + userId);
        Log.d(TAG, "  - Poster Image: " + (selectedImageFile != null ? selectedImageFile.getAbsolutePath() : "null"));

        // Tampilkan loading
        if (btnSimpan != null) {
            btnSimpan.setEnabled(false);
            btnSimpan.setText("Menyimpan...");
        }

        Log.d(TAG, "📤 Mengirim data event ke server...");

        // Buat event menggunakan controller
        EventController eventController = new EventController();
        eventController.createEventWithCompleteData(
                namaEvent,
                deskripsi,
                tanggalMulai,
                lokasi,
                organizer,
                kapasitas,
                kategori,
                harga,
                selectedImageFile, // File gambar (bisa null)
                waktuPelaksanaan,
                deadlinePendaftaran, // Controller mengharapkan deadlinePendaftaran (tanpa waktu)
                whatsapp,
                instagram,
                new EventController.CreateEventCallback() {
                    @Override
                    public void onSuccess(Event event) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "✅ ============ SUCCESS ============");
                            Log.d(TAG, "Event created: " + event.getNameEvent());
                            Log.d(TAG, "Event ID: " + event.getEventId());
                            Log.d(TAG, "Poster Path: " + event.getPosterEvent());

                            if (btnSimpan != null) {
                                btnSimpan.setEnabled(true);
                                btnSimpan.setText("Buat Event");
                            }

                            // Tampilkan dialog sukses
                            AlertDialog.Builder builder = new AlertDialog.Builder(BuatEvent.this);
                            builder.setTitle("🎉 Berhasil!")
                                    .setMessage("Event '" + event.getNameEvent() + "' berhasil dibuat!")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        // Kembali ke halaman sebelumnya
                                        finish();
                                    })
                                    .setCancelable(false)
                                    .show();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "❌ ============ ERROR ============");
                            Log.e(TAG, "Error message: " + message);

                            if (btnSimpan != null) {
                                btnSimpan.setEnabled(true);
                                btnSimpan.setText("Buat Event");
                            }

                            // Handle error spesifik berdasarkan controller
                            String userMessage = "Gagal membuat event: ";

                            if (message.contains("User ID diperlukan")) {
                                userMessage = "Silakan login terlebih dahulu untuk membuat event.";
                                showLoginRequiredDialog();
                            } else if (message.contains("harus diisi")) {
                                userMessage = message; // Langsung gunakan pesan dari controller
                            } else if (message.contains("Kapasitas harus lebih dari 0")) {
                                userMessage = message;
                            } else if (message.contains("Harga tidak boleh negatif")) {
                                userMessage = message;
                            } else if (message.contains("Format response tidak valid")) {
                                userMessage = "Terjadi kesalahan pada server. Coba lagi nanti.";
                            } else if (message.contains("Gagal membuat event:")) {
                                userMessage = message;
                            } else if (message.contains("Network error") || message.contains("Gagal terhubung")) {
                                userMessage = "Gagal terhubung ke server. Periksa koneksi internet Anda.";
                            } else if (message.contains("timeout")) {
                                userMessage = "Server terlalu lama merespon. Coba lagi nanti.";
                            } else {
                                userMessage += message;
                            }

                            Toast.makeText(BuatEvent.this, userMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    private String formatWaktuUntukDeadline(String waktuInput) {
        if (waktuInput == null || waktuInput.isEmpty()) {
            return "23:59:59";
        }

        try {
            // Hapus spasi
            String waktu = waktuInput.trim();

            // Jika sudah format HH:MM:SS
            if (waktu.matches("\\d{2}:\\d{2}:\\d{2}")) {
                return waktu;
            }
            // Jika format HH:MM
            else if (waktu.matches("\\d{2}:\\d{2}")) {
                return waktu + ":00";
            }
            // Jika format HH:MM:SS dengan detik berlebih (masalah: 05:19:00:00)
            else if (waktu.split(":").length > 3) {
                String[] parts = waktu.split(":");
                if (parts.length >= 3) {
                    // Ambil hanya HH:MM:SS
                    return parts[0] + ":" + parts[1] + ":" + parts[2];
                }
            }

            // Default
            return "23:59:59";

        } catch (Exception e) {
            Log.e(TAG, "Error formatting waktu: " + e.getMessage());
            return "23:59:59";
        }
    }

    private boolean validateInput() {
        clearErrors();
        boolean isValid = true;

        if (etNamaEvent == null || etNamaEvent.getText().toString().trim().isEmpty()) {
            if (etNamaEvent != null) etNamaEvent.setError("Nama event harus diisi");
            isValid = false;
        }

        if (etLokasiEvent == null || etLokasiEvent.getText().toString().trim().isEmpty()) {
            if (etLokasiEvent != null) etLokasiEvent.setError("Lokasi harus diisi");
            isValid = false;
        }

        if (etTglMulai == null || etTglMulai.getText().toString().trim().isEmpty()) {
            if (etTglMulai != null) etTglMulai.setError("Tanggal mulai harus diisi");
            isValid = false;
        }

        if (etWktMulai == null || etWktMulai.getText().toString().trim().isEmpty()) {
            if (etWktMulai != null) etWktMulai.setError("Waktu mulai harus diisi");
            isValid = false;
        }

        if (etOrganizer == null || etOrganizer.getText().toString().trim().isEmpty()) {
            if (etOrganizer != null) etOrganizer.setError("Penyelenggara harus diisi");
            isValid = false;
        }

        if (etKapasitas == null || etKapasitas.getText().toString().trim().isEmpty()) {
            if (etKapasitas != null) etKapasitas.setError("Kapasitas harus diisi");
            isValid = false;
        } else {
            try {
                int kapasitas = Integer.parseInt(etKapasitas.getText().toString().trim());
                if (kapasitas <= 0) {
                    if (etKapasitas != null) etKapasitas.setError("Kapasitas harus lebih dari 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                if (etKapasitas != null) etKapasitas.setError("Kapasitas harus berupa angka");
                isValid = false;
            }
        }

        if (etKeterangan == null || etKeterangan.getText().toString().trim().isEmpty()) {
            if (etKeterangan != null) etKeterangan.setError("Deskripsi harus diisi");
            isValid = false;
        }

        if (rbBerbayar != null && rbBerbayar.isChecked()) {
            if (etHargaTiket == null || etHargaTiket.getText().toString().trim().isEmpty()) {
                if (etHargaTiket != null) etHargaTiket.setError("Harga harus diisi");
                isValid = false;
            } else {
                try {
                    double harga = Double.parseDouble(etHargaTiket.getText().toString().trim());
                    if (harga <= 0) {
                        if (etHargaTiket != null) etHargaTiket.setError("Harga harus lebih dari 0");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    if (etHargaTiket != null) etHargaTiket.setError("Harga harus berupa angka");
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private void clearErrors() {
        if (etNamaEvent != null) etNamaEvent.setError(null);
        if (etLokasiEvent != null) etLokasiEvent.setError(null);
        if (etTglMulai != null) etTglMulai.setError(null);
        if (etWktMulai != null) etWktMulai.setError(null);
        if (etOrganizer != null) etOrganizer.setError(null);
        if (etKapasitas != null) etKapasitas.setError(null);
        if (etKeterangan != null) etKeterangan.setError(null);
        if (etHargaTiket != null) etHargaTiket.setError(null);
    }

    private String buildKategoriFromTags() {
        if (selectedTags.isEmpty()) {
            return "";
        }

        StringBuilder kategoriBuilder = new StringBuilder();
        for (String tag : selectedTags) {
            if (kategoriBuilder.length() > 0) {
                kategoriBuilder.append(", ");
            }
            kategoriBuilder.append(tag);
        }

        return kategoriBuilder.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 BuatEvent onResume()");

        // Re-initialize ApiHelper jika diperlukan
        ApiHelper.initialize(getApplicationContext());

        // Cek ulang user login status
        String userId = ApiHelper.getSavedUserId();
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "⚠ User belum login di onResume");
        } else {
            Log.d(TAG, "✅ User masih login di onResume, user_id: " + userId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "⏸ BuatEvent onPause()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔚 BuatEvent destroyed");

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}