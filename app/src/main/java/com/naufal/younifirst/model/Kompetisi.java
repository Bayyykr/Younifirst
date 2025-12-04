package com.naufal.younifirst.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Kompetisi {
    private String lomba_id;
    private String nama_lomba;
    private String tanggalLomba;
    private String lokasi;
    private String kategori;
    private String poster;
    private String status;
    private String scope;
    private String deskripsi;
    private String hadiah;
    private String user_id;
    private String lomba_type;
    private String biaya;
    private String penyelenggara;
    private String harga_lomba;
    private static final String BASE_URL = "http://10.131.218.36:8000";

    public Kompetisi() {}

    public Kompetisi(JSONObject jsonObject) throws JSONException {
        this.lomba_id = jsonObject.optString("lomba_id");
        this.nama_lomba = jsonObject.optString("nama_lomba", "");
        this.tanggalLomba = jsonObject.optString("tanggal_lomba", "");
        this.kategori = jsonObject.optString("kategori", "");
        this.lokasi = jsonObject.optString("lokasi", "");
        this.deskripsi = jsonObject.optString("deskripsi", "");
        this.hadiah = jsonObject.optString("hadiah", "0");
        this.status = jsonObject.optString("status", "");
        this.lomba_type = jsonObject.optString("lomba_type", "");
        this.biaya = jsonObject.optString("biaya", "");
        this.scope = jsonObject.optString("scope", "");
        this.penyelenggara = jsonObject.optString("penyelenggara", "");
        this.harga_lomba = jsonObject.optString("harga_lomba", "0");

        // AMBIL POSTER DARI BERBAGAI SUMBER
        if (jsonObject.has("poster")) {
            this.poster = jsonObject.optString("poster", "");
        }
        else if (jsonObject.has("poster_lomba")) {
            this.poster = jsonObject.optString("poster_lomba", "");
        }
        else if (jsonObject.has("8")) { // Index array
            this.poster = jsonObject.optString("8", "");
        }
        else {
            this.poster = "";
        }

        Log.d("KOMPETISI_MODEL", "Penyelenggara: " + this.penyelenggara);
        Log.d("KOMPETISI_MODEL", "Harga Lomba: " + this.harga_lomba);
        Log.d("KOMPETISI_MODEL", "Poster set to: " + this.poster);
    }

    // Getters and Setters
    public String getId() { return lomba_id; }
    public void setId(String id) { this.lomba_id = id; }

    public String getNamaLomba() { return nama_lomba; }
    public void setNamaLomba(String namaLomba) { this.nama_lomba = namaLomba; }

    public String getTanggalLomba() { return tanggalLomba; }
    public void setTanggalLomba(String tanggalLomba) { this.tanggalLomba = tanggalLomba; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getHadiah() {
        return hadiah;
    }

    public void setHadiah(String hadiah) {
        this.hadiah = hadiah;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getLomba_type() {
        return lomba_type;
    }

    public void setLomba_type(String lomba_type) {
        this.lomba_type = lomba_type;
    }

    public String getBiaya() {
        return biaya;
    }

    public void setBiaya(String biaya) {
        this.biaya = biaya;
    }

    public String getPenyelenggara() {
        return penyelenggara;
    }

    public void setPenyelenggara(String penyelenggara) {
        this.penyelenggara = penyelenggara;
    }

    public String getHargaLomba() {
        return harga_lomba;
    }

    public void setHargaLomba(String harga_lomba) {
        this.harga_lomba = harga_lomba;
    }

    public String getFullPosterUrl() {
        if (poster == null || poster.isEmpty()) {
            Log.d("POSTER_URL", "Poster is null or empty");
            return null;
        }

        Log.d("POSTER_URL", "Original poster path: " + poster);

        // Jika sudah full URL, return langsung
        if (poster.startsWith("http://") || poster.startsWith("https://")) {
            Log.d("POSTER_URL", "Already full URL: " + poster);
            return poster;
        }

        // Base URL - SESUAIKAN DENGAN SERVER ANDA
        String baseUrl = "http://10.10.4.249:8000";

        // Pastikan path tidak kosong dan memiliki nilai yang valid
        String path = poster.trim();

        // Hapus slash di awal jika ada
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // Bangun full URL
        String fullUrl = baseUrl + "/" + path;
        Log.d("POSTER_URL", "Constructed full URL: " + fullUrl);

        return fullUrl;
    }

    public boolean isConfirmed() {
        return "confirm".equalsIgnoreCase(this.status);
    }
}