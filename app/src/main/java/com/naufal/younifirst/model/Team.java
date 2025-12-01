package com.naufal.younifirst.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Team {
    private static final String TAG = "TeamModel";

    private String id;
    private String namaTeam;
    private String pemilik;
    private String avatarPemilik;
    private String poster;
    private String roleDibutuhkan;
    private String memberSaatIni;
    private String deskripsi;
    private String waktuPost;
    private String jumlahLike;
    private String jumlahKomentar;
    private String jumlahShare;

    public String getMaxanggota() {
        return maxanggota;
    }

    public void setMaxanggota(String maxanggota) {
        this.maxanggota = maxanggota;
    }

    private String maxanggota;

    public Team() {}

    public Team(JSONObject jsonObject) throws JSONException {
        try {
            this.id = jsonObject.optString("id", "");
            this.namaTeam = jsonObject.optString("nama_team", "");
            this.maxanggota = jsonObject.optString("max_anggota", "");
            this.pemilik = jsonObject.optString("pemilik", "");
            this.avatarPemilik = jsonObject.optString("avatar_pemilik", "");
            this.poster = jsonObject.optString("poster", "");
            this.roleDibutuhkan = jsonObject.optString("role_dibutuhkan", "");
            this.memberSaatIni = jsonObject.optString("member_saat_ini", "");
            this.deskripsi = jsonObject.optString("deskripsi", "");
            this.waktuPost = jsonObject.optString("waktu_post", "");
            this.jumlahLike = jsonObject.optString("jumlah_like", "");
            this.jumlahKomentar = jsonObject.optString("jumlah_komentar", "");
            this.jumlahShare = jsonObject.optString("jumlah_share", "");

            Log.d(TAG, "Team created: " + this.namaTeam);

        } catch (Exception e) {
            Log.e(TAG, "Error creating Team from JSON: " + e.getMessage());
            Log.e(TAG, "Problematic JSON: " + jsonObject.toString());
            throw new JSONException("Failed to create Team: " + e.getMessage());
        }
    }

    // Getters and Setters tetap sama...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNamaTeam() { return namaTeam; }
    public void setNamaTeam(String namaTeam) { this.namaTeam = namaTeam; }

    public String getPemilik() { return pemilik; }
    public void setPemilik(String pemilik) { this.pemilik = pemilik; }

    public String getAvatarPemilik() { return avatarPemilik; }
    public void setAvatarPemilik(String avatarPemilik) { this.avatarPemilik = avatarPemilik; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getRoleDibutuhkan() { return roleDibutuhkan; }
    public void setRoleDibutuhkan(String roleDibutuhkan) { this.roleDibutuhkan = roleDibutuhkan; }

    public String getMemberSaatIni() { return memberSaatIni; }
    public void setMemberSaatIni(String memberSaatIni) { this.memberSaatIni = memberSaatIni; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public String getWaktuPost() { return waktuPost; }
    public void setWaktuPost(String waktuPost) { this.waktuPost = waktuPost; }

    public String getJumlahLike() { return jumlahLike; }
    public void setJumlahLike(String jumlahLike) { this.jumlahLike = jumlahLike; }

    public String getJumlahKomentar() { return jumlahKomentar; }
    public void setJumlahKomentar(String jumlahKomentar) { this.jumlahKomentar = jumlahKomentar; }

    public String getJumlahShare() { return jumlahShare; }
    public void setJumlahShare(String jumlahShare) { this.jumlahShare = jumlahShare; }
}