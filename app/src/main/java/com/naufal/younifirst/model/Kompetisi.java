package com.naufal.younifirst.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Kompetisi {
    private String id;
    private String namaLomba;
    private String tanggalLomba;
    private String lokasi;
    private String kategori;
    private String poster;
    private String status;
    private String scope;
    private String jumlahPeserta;

    public Kompetisi() {}

    public Kompetisi(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.optString("id", "");
        this.namaLomba = jsonObject.optString("nama_lomba", "");
        this.tanggalLomba = jsonObject.optString("tanggal_lomba", "");
        this.lokasi = jsonObject.optString("lokasi", "");
        this.kategori = jsonObject.optString("kategori", "");
        this.poster = jsonObject.optString("poster", "");
        this.status = jsonObject.optString("status", "");
        this.scope = jsonObject.optString("scope", "");
        this.jumlahPeserta = jsonObject.optString("jumlah_peserta", "0");
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNamaLomba() { return namaLomba; }
    public void setNamaLomba(String namaLomba) { this.namaLomba = namaLomba; }

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

    public String getJumlahPeserta() { return jumlahPeserta; }
    public void setJumlahPeserta(String jumlahPeserta) { this.jumlahPeserta = jumlahPeserta; }
    public boolean isConfirmed() {
        return "confirm".equalsIgnoreCase(this.status);
    }
}