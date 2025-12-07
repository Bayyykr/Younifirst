package com.naufal.younifirst.Kompetisi;

import static androidx.core.util.TypedValueCompat.dpToPx;
import static com.naufal.younifirst.api.ApiHelper.createTeamForPhpApi;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;
import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.custom.CustomEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BuatTim extends AppCompatActivity {

    private static final String TAG = "BuatTim";

    private ImageView backButton;
    private CustomEditText etNamaTim, etMaksimalMember, tgltutuptim;
    private CustomEditText etNamaLomba, etNamaPenyelenggara, etLinkPostingan;
    private LinearLayout containerPosisiTim;
    private Button btnTambahPosisi, btnBuatTim;

    private List<Posisi> listPosisi = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_tim_buat_tim);

        ApiHelper.initialize(this);

        backButton = findViewById(R.id.back_to_mainactivity);
        etNamaTim = findViewById(R.id.et_namatim);
        etMaksimalMember = findViewById(R.id.Btn_maksimalMember);
        tgltutuptim = findViewById(R.id.et_bataspendaftaran);
        etNamaLomba = findViewById(R.id.et_namalomba);
        etNamaPenyelenggara = findViewById(R.id.et_namaPenyelenggara);
        etLinkPostingan = findViewById(R.id.et_linkpostinganlomba);
        containerPosisiTim = findViewById(R.id.containerPosisiTim);
        btnTambahPosisi = findViewById(R.id.btnTambahPosisi);
        btnBuatTim = findViewById(R.id.btnBuatTim);

        etNamaTim.setHint("Nama Tim");
        etMaksimalMember.setHint("Masukkan jumlah member maksimal");
        tgltutuptim.setHint("Masukkan deadline tanggal tutup");
        etNamaLomba.setHint("Nama Lomba");
        etNamaPenyelenggara.setHint("Nama Penyelenggara");
        etLinkPostingan.setHint("Link Postingan Lomba");

        setupDatePicker(tgltutuptim);

        backButton.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra("posted", false);
            setResult(RESULT_OK, data);
            finish();
        });

        btnTambahPosisi.setOnClickListener(v -> tambahPosisiDinamically());
        btnBuatTim.setOnClickListener(v -> {
            if (validateInput()) createTeam();
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
                                "%04d-%02d-%02d",
                                selectedYear, selectedMonth + 1, selectedDay
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
        TextView tvPosisi = posisiItem.findViewById(R.id.tv_posisi);
        CustomEditText etNamaPosisi = posisiItem.findViewById(R.id.et_nama_posisi);
        CustomEditText etJumlahOrang = posisiItem.findViewById(R.id.et_jumlah_orang);

        int posisiNumber = containerPosisiTim.getChildCount() + 1;
        tvPosisi.setText("Posisi " + posisiNumber);

        Posisi posisi = new Posisi();
        posisi.setNamaPosisiEditText(etNamaPosisi);
        posisi.setJumlahOrangEditText(etJumlahOrang);
        posisi.setContainerKetentuan(containerKetentuan);
        listPosisi.add(posisi);

        btnDelete.setOnClickListener(v -> {
            containerPosisiTim.removeView(posisiItem);
            listPosisi.remove(posisi);
            updateKeteranganPosisi();
        });

        btnTambahKetentuan.setOnClickListener(v -> {
            CustomEditText etKet = new CustomEditText(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(45)
            );
            params.setMargins(0, dpToPx(10), 0, 0);
            etKet.setLayoutParams(params);
            etKet.setHint("Ketentuan tambahan");

            containerKetentuan.addView(etKet);
        });

        containerPosisiTim.addView(posisiItem);
    }
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void updateKeteranganPosisi() {
        int count = containerPosisiTim.getChildCount();
        for (int i = 0; i < count; i++) {
            View item = containerPosisiTim.getChildAt(i);
            TextView tvPosisi = item.findViewById(R.id.tv_posisi);
            tvPosisi.setText("Posisi " + (i + 1));
        }
    }

    private boolean validateInput() {
        if (etNamaTim.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Nama tim tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etMaksimalMember.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Jumlah maksimal member tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int maxMember = Integer.parseInt(etMaksimalMember.getText().toString().trim());
            if (maxMember < 1) {
                Toast.makeText(this, "Minimal 1 member", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Jumlah member harus angka", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (tgltutuptim.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Tanggal tutup tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etNamaLomba.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Nama lomba tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (listPosisi.isEmpty()) {
            Toast.makeText(this, "Minimal tambahkan satu posisi", Toast.LENGTH_SHORT).show();
            return false;
        }

        for (Posisi posisi : listPosisi) {
            if (posisi.getNamaPosisi().isEmpty()) {
                Toast.makeText(this, "Nama posisi tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (posisi.getJumlahOrang().isEmpty()) {
                Toast.makeText(this, "Jumlah orang tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void createTeam() {
        try {
            String namaTim = etNamaTim.getText().toString().trim();
            String maxAnggota = etMaksimalMember.getText().toString().trim();
            String tenggatJoin = tgltutuptim.getText().toString().trim();
            String namaKegiatan = etNamaLomba.getText().toString().trim();
            String penyelenggara = etNamaPenyelenggara.getText().toString().trim();
            String linkPostingan = etLinkPostingan.getText().toString().trim();

            CustomEditText etBonus = findViewById(R.id.et_bonus_nilai_plus);
            String bonus = etBonus.getText().toString().trim();

            if (namaTim.isEmpty() || maxAnggota.isEmpty() || tenggatJoin.isEmpty() || namaKegiatan.isEmpty()) {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show();
                return;
            }

            int maxAnggotaInt = Integer.parseInt(maxAnggota);

            String userId = ApiHelper.getSavedUserId();
            if (userId == null) {
                Toast.makeText(this, "Silakan login dulu", Toast.LENGTH_SHORT).show();
                return;
            }

            String roleRequired = getRoleRequiredString();
            String ketentuanUser = getKetentuanDariUser();

            JSONObject teamJson = new JSONObject();

            teamJson.put("nama_team", namaTim);
            teamJson.put("nama_kegiatan", namaKegiatan);
            teamJson.put("max_anggota", maxAnggotaInt);
            teamJson.put("role_required", roleRequired);
            teamJson.put("tenggat_join", tenggatJoin);
            teamJson.put("user_id", userId);

            if (!ketentuanUser.isEmpty()) {
                teamJson.put("ketentuan", ketentuanUser);
            }

            if (!bonus.isEmpty()) {
                teamJson.put("keterangan_tambahan", bonus);
            }

            if (!penyelenggara.isEmpty()) teamJson.put("penyelenggara", penyelenggara);
            if (!linkPostingan.isEmpty()) teamJson.put("link_postingan", linkPostingan);

            teamJson.put("deskripsi_anggota", "");
            teamJson.put("status", "waiting");
            teamJson.put("role", "ketua");
            teamJson.put("current_members", 0);
            teamJson.put("total_slots", maxAnggotaInt);
            teamJson.put("ketua_id", userId);

            ApiHelper.createTeamForPhpApi(teamJson, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(String result) {
                    handleCreateTeamSuccess(result, namaTim, maxAnggotaInt);
                }

                @Override
                public void onFailure(String error) {
                    handleCreateTeamFailure(error);
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getKetentuanDariUser() {
        StringBuilder sb = new StringBuilder();
        for (Posisi posisi : listPosisi) {
            List<String> ket = posisi.getKetentuanList();
            for (String k : ket) {
                if (!k.isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(k);
                }
            }
        }
        return sb.toString();
    }

    private String getRoleRequiredString() {
        StringBuilder sb = new StringBuilder();
        for (Posisi p : listPosisi) {
            if (sb.length() > 0) sb.append(",");
            sb.append(p.getNamaPosisi());
        }
        return sb.toString();
    }

    private void handleCreateTeamSuccess(String result, String teamName, int maxAnggota) {
        runOnUiThread(() -> {
            try {
                JSONObject res = new JSONObject(result);
                boolean success = res.optBoolean("success", false);

                if (success) {
                    Toast.makeText(this, "Tim berhasil dibuat!", Toast.LENGTH_LONG).show();

                    Intent data = new Intent();
                    data.putExtra("posted", true);
                    data.putExtra("team_name", teamName);
                    data.putExtra("max_anggota", maxAnggota);
                    data.putExtra("current_members", 0);
                    data.putExtra("team_data", result);
                    setResult(RESULT_OK, data);
                    finish();

                } else {
                    Toast.makeText(this, res.optString("message", "Gagal"), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Toast.makeText(this, "Error parsing server response", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleCreateTeamFailure(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Gagal membuat tim: " + error, Toast.LENGTH_LONG).show();
        });
    }

    class Posisi {
        private CustomEditText etNamaPosisi;
        private CustomEditText etJumlahOrang;
        private LinearLayout containerKetentuan;

        public void setNamaPosisiEditText(CustomEditText et) {
            etNamaPosisi = et;
        }

        public void setJumlahOrangEditText(CustomEditText et) {
            etJumlahOrang = et;
        }

        public void setContainerKetentuan(LinearLayout c) {
            containerKetentuan = c;
        }

        public String getNamaPosisi() {
            return etNamaPosisi != null ? etNamaPosisi.getText().toString().trim() : "";
        }

        public String getJumlahOrang() {
            return etJumlahOrang != null ? etJumlahOrang.getText().toString().trim() : "";
        }

        public List<String> getKetentuanList() {
            List<String> list = new ArrayList<>();

            if (containerKetentuan != null) {
                for (int i = 0; i < containerKetentuan.getChildCount(); i++) {
                    View v = containerKetentuan.getChildAt(i);
                    if (v instanceof CustomEditText) {
                        String t = ((CustomEditText) v).getText().toString().trim();
                        if (!t.isEmpty()) list.add(t);
                    }
                }
            }
            return list;
        }
    }
}
