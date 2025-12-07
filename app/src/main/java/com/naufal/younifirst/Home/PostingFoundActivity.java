package com.naufal.younifirst.Home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.naufal.younifirst.R;
import com.naufal.younifirst.api.ApiHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostingFoundActivity extends AppCompatActivity {

    private TextView backButtonMain, tambahLokasi, tambahTelepon, tambahEmail;

    // Form fields
    private EditText etDeskripsi;
    private TextView tvSelectPhoto;
    private LinearLayout photoPreviewContainer;
    private ImageView ivPhotoPreview, btnRemovePhoto;
    private TextView tvPhotoName;
    private Button btnChangePhoto, btnSimpan, btnDraft;

    // Selected values
    private String selectedLokasi = "";
    private String selectedTelepon = "";
    private String selectedEmail = "";
    private File selectedImageFile = null;
    private String selectedImageName = "";

    // Request codes
    private static final int REQUEST_IMAGE_PICK = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_lostnfound_menemukan);

        // Initialize ApiHelper
        ApiHelper.initialize(this);

        // Initialize views
        etDeskripsi = findViewById(R.id.et_deskripsi);
        tvSelectPhoto = findViewById(R.id.tv_select_photo);
        photoPreviewContainer = findViewById(R.id.photo_preview_container);
        ivPhotoPreview = findViewById(R.id.iv_photo_preview);
        tvPhotoName = findViewById(R.id.tv_photo_name);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        btnRemovePhoto = findViewById(R.id.btn_remove_photo);
        btnSimpan = findViewById(R.id.btn_share);
        btnDraft = findViewById(R.id.btn_draft);

        backButtonMain = findViewById(R.id.back_to_mainactivity);
        tambahLokasi = findViewById(R.id.tambah_lokasi_textview);
        tambahTelepon = findViewById(R.id.tambah_telepon_textview);
        tambahEmail = findViewById(R.id.tambah_email_textview);

        // Setup click listeners
        backButtonMain.setOnClickListener(v -> finish());
        tambahLokasi.setOnClickListener(v -> showTambahLokasiPopup());
        tambahTelepon.setOnClickListener(v -> showTambahTeleponPopup());
        tambahEmail.setOnClickListener(v -> showTambahEmailPopup());

        // Photo selection
        tvSelectPhoto.setOnClickListener(v -> pickImage());
        btnChangePhoto.setOnClickListener(v -> pickImage());
        btnRemovePhoto.setOnClickListener(v -> removeSelectedImage());

        // Button actions
        btnSimpan.setOnClickListener(v -> submitForm());
        btnDraft.setOnClickListener(v -> saveAsDraft());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    // Get file name
                    String fileName = getFileNameFromUri(imageUri);
                    selectedImageName = fileName != null ? fileName : "foto_barang.jpg";

                    // Convert URI to File
                    selectedImageFile = uriToFile(imageUri);

                    // Update UI
                    showPhotoPreview(imageUri);

                } catch (IOException e) {
                    Toast.makeText(this, "Gagal memuat gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private File uriToFile(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Cannot open input stream from URI");
        }

        // Create cache file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "BARANG_" + timeStamp + ".jpg";
        File file = new File(getCacheDir(), fileName);

        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        return file;
    }

    private void showPhotoPreview(Uri imageUri) {
        // Show preview container
        photoPreviewContainer.setVisibility(View.VISIBLE);

        // Load image with Glide
        Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.icon_galeri)
                .error(R.drawable.icon_silang)
                .centerCrop()
                .into(ivPhotoPreview);

        // Set file name
        tvPhotoName.setText(selectedImageName);

        // Hide select photo text
        tvSelectPhoto.setVisibility(View.GONE);
    }

    private void removeSelectedImage() {
        // Reset photo selection
        selectedImageFile = null;
        selectedImageName = "";

        // Hide preview
        photoPreviewContainer.setVisibility(View.GONE);

        // Show select photo text
        tvSelectPhoto.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Foto dihapus", Toast.LENGTH_SHORT).show();
    }

    private void submitForm() {
        // Validasi form
        String deskripsi = etDeskripsi.getText().toString().trim();

        if (TextUtils.isEmpty(deskripsi)) {
            Toast.makeText(this, "Deskripsi harus diisi", Toast.LENGTH_SHORT).show();
            etDeskripsi.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(selectedLokasi)) {
            Toast.makeText(this, "Lokasi harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Mengirim...");

        // Kirim ke API
        ApiHelper.createLostFound(
                deskripsi,
                selectedLokasi,
                "menemukan", // kategori sesuai dengan tabel
                selectedImageFile,
                selectedTelepon, // no_hp
                selectedEmail,   // email
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        runOnUiThread(() -> {
                            Toast.makeText(PostingFoundActivity.this,
                                    "Item berhasil diposting", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            btnSimpan.setEnabled(true);
                            btnSimpan.setText("Bagikan");

                            Toast.makeText(PostingFoundActivity.this,
                                    "Gagal memposting: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                , "post");
    }

    private void saveAsDraft() {
        String deskripsi = etDeskripsi.getText().toString().trim();

        if (TextUtils.isEmpty(deskripsi)) {
            Toast.makeText(this, "Deskripsi harus diisi", Toast.LENGTH_SHORT).show();
            etDeskripsi.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(selectedLokasi)) {
            Toast.makeText(this, "Lokasi harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Mengirim...");

        // Kirim ke API
        ApiHelper.createLostFound(
                deskripsi,
                selectedLokasi,
                "menemukan", // kategori sesuai dengan tabel
                selectedImageFile,
                selectedTelepon, // no_hp
                selectedEmail,   // email
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        runOnUiThread(() -> {
                            Toast.makeText(PostingFoundActivity.this,
                                    "Item berhasil dimasukkan draft", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            btnSimpan.setEnabled(true);
                            btnSimpan.setText("Bagikan");

                            Toast.makeText(PostingFoundActivity.this,
                                    "Gagal memposting: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                , "draft");
    }

    private void showTambahLokasiPopup() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(this)
                .inflate(R.layout.fragment_lostnfound_tambahkan_lokasi, null);

        bottomSheetDialog.setContentView(bottomSheetView);
        makeBottomSheetTransparent(bottomSheetDialog);

        EditText etLokasi = bottomSheetView.findViewById(R.id.et_lokasi);
        Button btnSimpanLokasi = bottomSheetView.findViewById(R.id.ButtonTambahkanLokasi);

        // Set current value if any
        if (!TextUtils.isEmpty(selectedLokasi)) {
            etLokasi.setText(selectedLokasi);
        }

        btnSimpanLokasi.setOnClickListener(v -> {
            String lokasi = etLokasi.getText().toString().trim();
            if (!TextUtils.isEmpty(lokasi)) {
                selectedLokasi = lokasi;
                // Ganti teks menjadi lokasi yang diinput
                tambahLokasi.setText(lokasi);
                // Ubah warna teks menjadi putih untuk menunjukkan sudah diisi
                tambahLokasi.setTextColor(getResources().getColor(R.color.white));
                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(this, "Masukkan lokasi", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }

    private void showTambahTeleponPopup() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(this)
                .inflate(R.layout.fragment_lostnfound_tambahkan_nomor_telepon, null);

        bottomSheetDialog.setContentView(bottomSheetView);
        makeBottomSheetTransparent(bottomSheetDialog);

        EditText etTelepon = bottomSheetView.findViewById(R.id.et_telepons);
        Button btnSimpanTelepon = bottomSheetView.findViewById(R.id.ButtonTambahkanNomor);

        // Hapus riwayat section
        TextView tvRiwayat = bottomSheetView.findViewById(R.id.tv_riwayat);
        tvRiwayat.setVisibility(View.GONE);

        // Set current value if any
        if (!TextUtils.isEmpty(selectedTelepon)) {
            etTelepon.setText(selectedTelepon);
        }

        btnSimpanTelepon.setOnClickListener(v -> {
            String telepon = etTelepon.getText().toString().trim();
            if (!TextUtils.isEmpty(telepon)) {
                selectedTelepon = telepon;
                // Ganti teks menjadi nomor telepon yang diinput
                tambahTelepon.setText(telepon);
                // Ubah warna teks menjadi putih untuk menunjukkan sudah diisi
                tambahTelepon.setTextColor(getResources().getColor(R.color.white));
                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(this, "Masukkan nomor telepon", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }

    private void showTambahEmailPopup() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(this)
                .inflate(R.layout.fragment_lostnfound_tambahkan_alamat_email, null);

        bottomSheetDialog.setContentView(bottomSheetView);
        makeBottomSheetTransparent(bottomSheetDialog);

        EditText etEmail = bottomSheetView.findViewById(R.id.et_emails);
        Button btnSimpanEmail = bottomSheetView.findViewById(R.id.ButtonTambahEmails);

        // Hapus text tentang SSO terdaftar
        TextView tvSsoTerdaftar = bottomSheetView.findViewById(R.id.tv_sso_terdaftar);
        tvSsoTerdaftar.setVisibility(View.GONE);

        // Set current value if any
        if (!TextUtils.isEmpty(selectedEmail)) {
            etEmail.setText(selectedEmail);
        }

        btnSimpanEmail.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                selectedEmail = email;
                // Ganti teks menjadi email yang diinput
                tambahEmail.setText(email);
                // Ubah warna teks menjadi putih untuk menunjukkan sudah diisi
                tambahEmail.setTextColor(getResources().getColor(R.color.white));
                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(this, "Masukkan email yang valid", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }

    private void makeBottomSheetTransparent(BottomSheetDialog dialog) {
        View parent = (View) dialog.getWindow().getDecorView().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (parent != null) {
            parent.setBackgroundColor(Color.TRANSPARENT);

            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(parent);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetBehavior.setSkipCollapsed(true);
        }
    }
}