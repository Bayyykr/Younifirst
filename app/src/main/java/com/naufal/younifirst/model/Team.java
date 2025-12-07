package com.naufal.younifirst.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Team {
    private static final String TAG = "TeamModel";

    private Date tenggatJoin;
    private String teamId;
    private String namaKegiatan;
    private String namaTeam;
    private String deskripsiAnggota;
    private String roleRequired;
    private String maxAnggota;
    private String role;
    private String status;

    // Field tambahan
    private String penyelenggara;
    private String linkPostingan;
    private String namaLomba;
    private String avatarPemilik;
    private String poster;
    private Date waktuPost;
    private String jumlahLike;
    private String jumlahKomentar;
    private String jumlahShare;
    private String pemilik;
    private String memberSaatIni;

    private String ketentuan;
    private String keteranganTambahan;
    private String hadiah;

    public Team(String namaTeam, String namaKegiatan, String maxAnggota,
                String roleRequired, String keteranganTambahan, Date tenggatJoin,
                String penyelenggara, String linkPostingan) {
        this.namaTeam = namaTeam;
        this.namaKegiatan = namaKegiatan;
        this.maxAnggota = maxAnggota;
        this.roleRequired = roleRequired;
        this.keteranganTambahan = keteranganTambahan;
        this.tenggatJoin = tenggatJoin;
        this.penyelenggara = penyelenggara;
        this.linkPostingan = linkPostingan;
        this.status = "waiting";
        this.role = "ketua";
    }

    // Getter & Setter tambahan
    public String getPenyelenggara() {
        return penyelenggara != null ? penyelenggara : "";
    }

    public void setPenyelenggara(String penyelenggara) {
        this.penyelenggara = penyelenggara;
    }

    public String getLinkPostingan() {
        return linkPostingan != null ? linkPostingan : "";
    }

    public void setLinkPostingan(String linkPostingan) {
        this.linkPostingan = linkPostingan;
    }

    public String getNamaLomba() {
        return namaLomba != null ? namaLomba : "";
    }

    public void setNamaLomba(String namaLomba) {
        this.namaLomba = namaLomba;
    }

    // Method untuk mengonversi ke JSONObject untuk API
    public JSONObject toJsonForCreate() throws JSONException {
        JSONObject json = new JSONObject();

        // Data dasar dari layout
        json.put("nama_team", namaTeam);
        json.put("nama_kegiatan", namaKegiatan);
        json.put("max_anggota", maxAnggota);
        json.put("role_required", roleRequired);
        json.put("keterangan_tambahan", keteranganTambahan);
        json.put("tenggat_join", new SimpleDateFormat("yyyy-MM-dd").format(tenggatJoin));

        // Data tambahan untuk kompetisi
        if (penyelenggara != null && !penyelenggara.isEmpty()) {
            json.put("penyelenggara", penyelenggara);
        }

        if (linkPostingan != null && !linkPostingan.isEmpty()) {
            json.put("link_postingan", linkPostingan);
        }

        // Data default
        json.put("status", "waiting");
        json.put("role", "ketua");
        json.put("deskripsi_anggota", deskripsiAnggota != null ? deskripsiAnggota : "");

        // Data posisi dan ketentuan (akan diolah terpisah)
        if (ketentuan != null && !ketentuan.isEmpty()) {
            json.put("ketentuan", ketentuan);
        }

        return json;
    }

    public Team(JSONObject jsonObject) throws JSONException {
        try {
            Log.d(TAG, "Creating Team from JSON: " + jsonObject.toString());

            this.teamId = getStringValue(jsonObject,
                    new String[]{"team_id", "id", "teamId"});

            this.namaKegiatan = getStringValue(jsonObject,
                    new String[]{"nama_kegiatan", "namaKegiatan", "kegiatan"});

            this.namaTeam = getStringValue(jsonObject,
                    new String[]{"nama_team", "namaTeam", "team_name", "nama_tim"});

            this.deskripsiAnggota = getStringValue(jsonObject,
                    new String[]{"deskripsi_anggota", "deskripsiAnggota", "deskripsi", "description"});

            this.ketentuan = getStringValue(jsonObject,
                    new String[]{"ketentuan", "requirements", "syarat", "terms"});

            this.keteranganTambahan = getStringValue(jsonObject,
                    new String[]{"keterangan_tambahan", "additional_info", "bonus_info", "catatan_tambahan"});

            // 🔥 PARSE HADIAH
            this.hadiah = getStringValue(jsonObject,
                    new String[]{"hadiah", "prize", "reward", "prizes"});

            Log.d(TAG, "Hadiah from DB: " + this.hadiah);

            this.roleRequired = getStringValue(jsonObject,
                    new String[]{"role_required", "roleRequired", "role_dibutuhkan", "required_role"});

            this.maxAnggota = getStringValue(jsonObject,
                    new String[]{"max_anggota", "maxAnggota", "max_members", "max_member"});

            this.role = getStringValue(jsonObject,
                    new String[]{"role", "user_role"});

            this.status = getStringValue(jsonObject,
                    new String[]{"status", "team_status"});

            this.tenggatJoin = parseDateValue(jsonObject,
                    new String[]{"tenggat_join", "deadline_join", "deadline_join_team"});

            this.avatarPemilik = getStringValue(jsonObject,
                    new String[]{"avatar_pemilik", "avatarPemilik", "avatar", "logo"});

            this.poster = getStringValue(jsonObject,
                    new String[]{
                            "poster_lomba",      // 1. Utamakan poster_lomba dari database
                            "poster",            // 2. Alternatif 1
                            "image",             // 3. Alternatif 2
                            "cover_image",       // 4. Alternatif 3
                            "poster_url",        // 5. Alternatif 4
                            "image_url"          // 6. Alternatif 5
                    });

            Log.d(TAG, "Poster path parsed: " + this.poster);

            this.waktuPost = parseDateValue(jsonObject,
                    new String[]{"waktu_post", "waktuPost", "created_at", "createdAt", "timestamp", "post_time"});

            if (this.waktuPost == null) {
                this.waktuPost = new Date(); // default
            }

            this.jumlahLike = getStringValue(jsonObject,
                    new String[]{"jumlah_like", "jumlahLike", "likes", "like_count"});

            this.jumlahKomentar = getStringValue(jsonObject,
                    new String[]{"jumlah_komentar", "jumlahKomentar", "comments", "comment_count"});

            this.jumlahShare = getStringValue(jsonObject,
                    new String[]{"jumlah_share", "jumlahShare", "shares", "share_count"});

            this.pemilik = getStringValue(jsonObject,
                    new String[]{"pemilik", "owner", "creator"});

            this.memberSaatIni = getStringValue(jsonObject,
                    new String[]{
                            "member_saat_ini",
                            "memberSaatIni",
                            "current_members",
                            "members_count",
                            "current_member_count",
                            "joined_members",
                            "active_members",
                            "total_members",
                            "jumlah_anggota",
                            "anggota_saat_ini",
                            "jumlah_member",
                            "member_count",
                            "team_members",
                            "members"
                    });

            if (this.memberSaatIni == null || this.memberSaatIni.isEmpty() || this.memberSaatIni.equals("0")) {
                if (jsonObject.has("members") && jsonObject.get("members") instanceof JSONArray) {
                    this.memberSaatIni = String.valueOf(jsonObject.getJSONArray("members").length());
                } else if (jsonObject.has("anggota") && jsonObject.get("anggota") instanceof JSONArray) {
                    this.memberSaatIni = String.valueOf(jsonObject.getJSONArray("anggota").length());
                }
            }

            if (this.maxAnggota == null || this.maxAnggota.isEmpty()) {
                this.maxAnggota = "4";
            }

            if (this.status == null || this.status.isEmpty()) {
                this.status = "confirm";
            }

            if (this.memberSaatIni == null || this.memberSaatIni.isEmpty() || this.memberSaatIni.equals("0")) {
                this.memberSaatIni = "1";
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating Team: " + e.getMessage());
            throw new JSONException("Failed to create Team: " + e.getMessage());
        }
    }

    private String getStringValue(JSONObject json, String[] keys) {
        for (String key : keys) {
            try {
                if (json.has(key) && !json.isNull(key)) {
                    String v = json.getString(key);
                    if (v != null && !v.isEmpty()) return v;
                }
            } catch (JSONException ignored) {}
        }
        return "";
    }

    private Date parseDateValue(JSONObject json, String[] keys) {
        for (String key : keys) {
            try {
                if (json.has(key) && !json.isNull(key)) {
                    String raw = json.getString(key).trim();

                    if (raw.matches("\\d+")) {
                        long ts = Long.parseLong(raw);
                        if (ts < 10000000000L) ts *= 1000;
                        return new Date(ts);
                    }

                    try {
                        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(raw);
                    } catch (Exception ignored) {}

                    try {
                        return new SimpleDateFormat("yyyy-MM-dd").parse(raw);
                    } catch (Exception ignored) {}

                    try {
                        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(raw);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    // =========================================
    // 🔥 HADIAH METHODS
    // =========================================
    public String getHadiah() {
        return hadiah != null ? hadiah : "";
    }

    public void setHadiah(String hadiah) {
        this.hadiah = hadiah;
    }

    public List<String> getHadiahList() {
        List<String> list = new ArrayList<>();
        if (hadiah != null && !hadiah.trim().isEmpty() && !hadiah.equalsIgnoreCase("null")) {
            String[] items = hadiah.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty() && !trimmed.equalsIgnoreCase("null")) {
                    list.add(trimmed);
                }
            }
        }
        return list;
    }

    public String getFormattedHadiah() {
        List<String> list = getHadiahList();
        if (list.isEmpty()) {
            return "Tidak ada hadiah"; // 🔥 UBAH INI
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append("• ").append(list.get(i));
            if (i < list.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    // 🔥 Method baru untuk cek apakah ada hadiah
    public boolean hasHadiah() {
        List<String> list = getHadiahList();
        return !list.isEmpty();
    }

    public String getFormattedTenggatJoin() {
        if (tenggatJoin == null) return "Belum ditentukan";
        return new SimpleDateFormat("dd MMMM yyyy HH:mm", new Locale("id", "ID"))
                .format(tenggatJoin);
    }


    public String getFormattedTenggatJoinWithTime() {
        if (tenggatJoin == null) return "Belum ditentukan";
        return new SimpleDateFormat("dd MMMM yyyy 'pukul' HH:mm", new Locale("id", "ID"))
                .format(tenggatJoin);
    }

    public String getFormattedWaktuPost() {
        if (waktuPost == null) return "Baru saja";

        long diff = System.currentTimeMillis() - waktuPost.getTime();
        long sec = diff / 1000;
        long min = sec / 60;
        long hour = min / 60;
        long day = hour / 24;

        if (sec < 60) return "Baru saja";
        if (min < 60) return min + " menit lalu";
        if (hour < 24) return hour + " jam lalu";
        if (day < 7) return day + " hari lalu";

        return new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"))
                .format(waktuPost);
    }

    public Date getWaktuPost() { return waktuPost; }
    public String getWaktuPostString() {
        if (waktuPost == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(waktuPost);
    }

    public String getKetentuan() { return ketentuan != null ? ketentuan : ""; }
    public void setKetentuan(String k) { this.ketentuan = k; }

    public List<String> getKetentuanList() {
        List<String> list = new ArrayList<>();
        if (ketentuan != null && !ketentuan.trim().isEmpty()) {
            for (String s : ketentuan.split(",")) {
                if (!s.trim().isEmpty()) list.add(s.trim());
            }
        }
        return list;
    }

    public String getKeteranganTambahan() {
        return keteranganTambahan != null ? keteranganTambahan : "";
    }

    public void setKeteranganTambahan(String keteranganTambahan) {
        this.keteranganTambahan = keteranganTambahan;
    }

    public String getFormattedBonus() {
        if (keteranganTambahan == null || keteranganTambahan.trim().isEmpty())
            return "Tidak ada bonus khusus";
        return keteranganTambahan.trim();
    }

    public String getTeamId() { return teamId; }
    public String getNamaKegiatan() { return namaKegiatan; }
    public String getNamaTeam() { return namaTeam; }
    public String getDeskripsiAnggota() { return deskripsiAnggota; }
    public String getRoleRequired() { return roleRequired; }
    public String getMaxAnggota() { return maxAnggota; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getAvatarPemilik() { return avatarPemilik; }
    public String getPoster() { return poster; }
    public String getJumlahLike() { return jumlahLike; }
    public String getJumlahKomentar() { return jumlahKomentar; }
    public String getJumlahShare() { return jumlahShare; }
    public String getPemilik() { return pemilik; }
    public String getMemberSaatIni() {
        return (memberSaatIni == null || memberSaatIni.isEmpty()) ? "0" : memberSaatIni;
    }
}
