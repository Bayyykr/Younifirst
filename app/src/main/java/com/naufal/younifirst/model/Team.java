package com.naufal.younifirst.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Team {
    private static final String TAG = "TeamModel";

    // Field utama dari database
    private String teamId;
    private String namaKegiatan;
    private String namaTeam;
    private String deskripsiAnggota;
    private String roleRequired;
    private String maxAnggota;
    private String role;
    private String status;

    // Field tambahan
    private String avatarPemilik;
    private String poster;
    private String waktuPost;
    private String jumlahLike;
    private String jumlahKomentar;
    private String jumlahShare;

    // Field opsional (jika ada di response)
    private String pemilik;
    private String memberSaatIni;

    public Team() {}

    public Team(JSONObject jsonObject) throws JSONException {
        try {
            Log.d(TAG, "Creating Team from JSON: " + jsonObject.toString());

            // Mapping field dari response API
            // Coba berbagai kemungkinan nama field
            this.teamId = getStringValue(jsonObject,
                    new String[]{"team_id", "id", "teamId"});

            this.namaKegiatan = getStringValue(jsonObject,
                    new String[]{"nama_kegiatan", "namaKegiatan", "kegiatan"});

            this.namaTeam = getStringValue(jsonObject,
                    new String[]{"nama_team", "namaTeam", "team_name", "nama_tim"});

            this.deskripsiAnggota = getStringValue(jsonObject,
                    new String[]{"deskripsi_anggota", "deskripsiAnggota", "deskripsi", "description"});

            this.roleRequired = getStringValue(jsonObject,
                    new String[]{"role_required", "roleRequired", "role_dibutuhkan", "required_role"});

            this.maxAnggota = getStringValue(jsonObject,
                    new String[]{"max_anggota", "maxAnggota", "max_members", "max_member"});

            this.role = getStringValue(jsonObject,
                    new String[]{"role", "user_role"});

            this.status = getStringValue(jsonObject,
                    new String[]{"status", "team_status"});

            this.avatarPemilik = getStringValue(jsonObject,
                    new String[]{"avatar_pemilik", "avatarPemilik", "avatar", "logo"});

            this.poster = getStringValue(jsonObject,
                    new String[]{"poster", "image", "cover_image"});

            this.waktuPost = getStringValue(jsonObject,
                    new String[]{"waktu_post", "waktuPost", "created_at", "createdAt", "timestamp"});

            this.jumlahLike = getStringValue(jsonObject,
                    new String[]{"jumlah_like", "jumlahLike", "likes", "like_count"});

            this.jumlahKomentar = getStringValue(jsonObject,
                    new String[]{"jumlah_komentar", "jumlahKomentar", "comments", "comment_count"});

            this.jumlahShare = getStringValue(jsonObject,
                    new String[]{"jumlah_share", "jumlahShare", "shares", "share_count"});

            this.pemilik = getStringValue(jsonObject,
                    new String[]{"pemilik", "owner", "creator"});

            this.memberSaatIni = getStringValue(jsonObject,
                    new String[]{"member_saat_ini", "memberSaatIni", "current_members", "members_count"});

            // Log mapping hasil
            Log.d(TAG, "Team created - Name: " + this.namaTeam +
                    ", RoleReq: " + this.roleRequired +
                    ", MaxAnggota: " + this.maxAnggota +
                    ", Status: " + this.status);

            // Set default values if empty
            if (this.maxAnggota == null || this.maxAnggota.isEmpty()) {
                this.maxAnggota = "4"; // Default value
            }

            if (this.status == null || this.status.isEmpty()) {
                this.status = "confirm  "; // Default status
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating Team from JSON: " + e.getMessage());
            Log.e(TAG, "Problematic JSON: " + jsonObject.toString());
            throw new JSONException("Failed to create Team: " + e.getMessage());
        }
    }

    // Helper method untuk mencoba berbagai kemungkinan key
    private String getStringValue(JSONObject json, String[] possibleKeys) {
        for (String key : possibleKeys) {
            try {
                if (json.has(key) && !json.isNull(key)) {
                    String value = json.getString(key);
                    if (value != null && !value.isEmpty()) {
                        return value;
                    }
                }
            } catch (JSONException e) {
                // Continue to next key
            }
        }
        return "";
    }

    // Getters and Setters
    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getNamaKegiatan() { return namaKegiatan; }
    public void setNamaKegiatan(String namaKegiatan) { this.namaKegiatan = namaKegiatan; }

    public String getNamaTeam() { return namaTeam; }
    public void setNamaTeam(String namaTeam) { this.namaTeam = namaTeam; }

    public String getDeskripsiAnggota() { return deskripsiAnggota; }
    public void setDeskripsiAnggota(String deskripsiAnggota) { this.deskripsiAnggota = deskripsiAnggota; }

    public String getRoleRequired() { return roleRequired; }
    public void setRoleRequired(String roleRequired) { this.roleRequired = roleRequired; }

    public String getMaxAnggota() { return maxAnggota; }
    public void setMaxAnggota(String maxAnggota) { this.maxAnggota = maxAnggota; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAvatarPemilik() { return avatarPemilik; }
    public void setAvatarPemilik(String avatarPemilik) { this.avatarPemilik = avatarPemilik; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getWaktuPost() { return waktuPost; }
    public void setWaktuPost(String waktuPost) { this.waktuPost = waktuPost; }

    public String getJumlahLike() { return jumlahLike; }
    public void setJumlahLike(String jumlahLike) { this.jumlahLike = jumlahLike; }

    public String getJumlahKomentar() { return jumlahKomentar; }
    public void setJumlahKomentar(String jumlahKomentar) { this.jumlahKomentar = jumlahKomentar; }

    public String getJumlahShare() { return jumlahShare; }
    public void setJumlahShare(String jumlahShare) { this.jumlahShare = jumlahShare; }

    public String getPemilik() { return pemilik; }
    public void setPemilik(String pemilik) { this.pemilik = pemilik; }

    public String getMemberSaatIni() {
        if (memberSaatIni == null || memberSaatIni.isEmpty()) {
            return "0"; // Default jika kosong
        }
        return memberSaatIni;
    }
    public void setMemberSaatIni(String memberSaatIni) { this.memberSaatIni = memberSaatIni; }
}