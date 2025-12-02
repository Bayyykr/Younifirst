package com.naufal.younifirst.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event implements Parcelable {
    private String eventId;
    private String nameEvent;
    private String tanggalMulai;
    private String tanggalSelesai;
    private String lokasi;
    private String organizer;
    private int kapasitas;
    private String status;
    private String posterEvent;
    private String description;
    private String kategori;
    private String harga;
    private String dlPendaftaran;

    private static final String BASE_URL = "http://10.0.2.2/you-ni-first-web/public/";

    public Event(JSONObject json) throws JSONException {
        this.eventId = json.optString("event_id", "").trim();
        this.nameEvent = json.optString("nama_event", "");
        this.tanggalMulai = json.optString("tanggal_mulai", "");
        this.tanggalSelesai = json.optString("tanggal_selsai", "");
        this.lokasi = json.optString("lokasi", "");
        this.organizer = json.optString("organizer", "");

        try {
            String kapasitasStr = json.optString("kapasitas", "0");
            this.kapasitas = Integer.parseInt(kapasitasStr);
        } catch (NumberFormatException e) {
            this.kapasitas = 0;
        }

        this.status = json.optString("status", "");
        this.posterEvent = json.optString("poster_event", "");
        this.description = json.optString("deskripsi", "");

        // PERBAIKAN: Ambil kategori dari JSON
        this.kategori = json.optString("kategori", "");

        // Ambil harga dan dl_pendaftaran dari JSON
        this.harga = json.optString("harga", "");
        this.dlPendaftaran = json.optString("dl_pendaftaran", "");

        android.util.Log.d("EventModel",
                "Created Event: " + this.nameEvent +
                        ", ID: " + this.eventId +
                        ", Kategori: " + this.kategori +
                        ", Harga: " + this.harga +
                        ", DL Pendaftaran: " + this.dlPendaftaran);
    }

    // ========== GETTER & SETTER ==========
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getNameEvent() { return nameEvent; }
    public void setNameEvent(String nameEvent) { this.nameEvent = nameEvent; }

    public String getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(String tanggalMulai) { this.tanggalMulai = tanggalMulai; }

    public String getTanggalSelesai() { return tanggalSelesai; }
    public void setTanggalSelesai(String tanggalSelesai) { this.tanggalSelesai = tanggalSelesai; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public int getKapasitas() { return kapasitas; }
    public void setKapasitas(int kapasitas) { this.kapasitas = kapasitas; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPosterEvent() { return posterEvent; }
    public void setPosterEvent(String posterEvent) { this.posterEvent = posterEvent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getHarga() { return harga; }
    public void setHarga(String harga) { this.harga = harga; }

    public String getDlPendaftaran() { return dlPendaftaran; }
    public void setDlPendaftaran(String dlPendaftaran) { this.dlPendaftaran = dlPendaftaran; }

    // ========== FORMATTING METHODS ==========
    public String getFormattedHarga() {
        if (harga == null || harga.isEmpty() || "null".equals(harga) || "0".equals(harga)) {
            return "Gratis";
        }

        try {
            double nominal = Double.parseDouble(harga);
            if (nominal == 0) {
                return "Gratis";
            }

            java.text.DecimalFormat formatter = new java.text.DecimalFormat("###,###,###");
            return "Rp. " + formatter.format(nominal);
        } catch (NumberFormatException e) {
            return "Rp. " + harga;
        }
    }

    public String getFormattedDlPendaftaran() {
        try {
            if (dlPendaftaran == null || dlPendaftaran.isEmpty() || "null".equals(dlPendaftaran)) {
                return "Jumat, 21 November 2025 | 12:00"; // Default sesuai layout
            }

            // Format timestamp dari database: "2025-12-13 12:00:00"
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy | HH:mm", new Locale("id", "ID"));

            Date date = inputFormat.parse(dlPendaftaran);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Jika format tidak sesuai, coba format lain
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy | HH:mm", new Locale("id", "ID"));
                Date date = inputFormat.parse(dlPendaftaran);
                return outputFormat.format(date);
            } catch (ParseException e2) {
                return dlPendaftaran;
            }
        }
    }

    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            Date date = inputFormat.parse(tanggalMulai);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return tanggalMulai;
        }
    }

    public String getShortDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM", new Locale("id", "ID"));
            Date date = inputFormat.parse(tanggalMulai);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return tanggalMulai;
        }
    }

    // ========== UTILITY METHODS ==========
    public String getFullPosterUrl() {
        if (posterEvent == null || posterEvent.isEmpty() || "null".equals(posterEvent)) {
            return null;
        }

        if (posterEvent.startsWith("http://") || posterEvent.startsWith("https://")) {
            return posterEvent;
        }

        if (posterEvent.startsWith("storage/") || posterEvent.startsWith("public/")) {
            return BASE_URL + posterEvent;
        }

        return BASE_URL + "storage/uploads/events/" + posterEvent;
    }

    public String getSafePosterUrl() {
        String url = getFullPosterUrl();
        return (url != null && !url.isEmpty()) ? url : null;
    }

    public boolean isConfirmed() {
        return "confirm".equalsIgnoreCase(status);
    }

    public boolean isTrending() {
        return kapasitas > 300;
    }

    // Method untuk mendapatkan warna badge berdasarkan kategori
    public int getBadgeColor() {
        if (kategori == null || kategori.isEmpty()) {
            return generateColorFromString(nameEvent != null ? nameEvent : "Event");
        }
        return generateColorFromString(kategori);
    }

    // Method untuk mendapatkan teks badge
    public String getBadgeText() {
        if (kategori == null || kategori.isEmpty()) {
            return "EVENT";
        }

        // Ambil kata pertama dari kategori jika ada koma
        String[] parts = kategori.split(",");
        return parts[0].trim().toUpperCase();
    }

    private int generateColorFromString(String input) {
        // List warna menarik untuk badge
        int[] badgeColors = {
                Color.parseColor("#FF6B6B"),  // Merah muda
                Color.parseColor("#4ECDC4"),  // Turquoise
                Color.parseColor("#FFD166"),  // Kuning
                Color.parseColor("#06D6A0"),  // Hijau mint
                Color.parseColor("#118AB2"),  // Biru
                Color.parseColor("#EF476F"),  // Pink
                Color.parseColor("#7209B7"),  // Ungu
                Color.parseColor("#3A86FF"),  // Biru cerah
                Color.parseColor("#FB5607"),  // Orange
                Color.parseColor("#8338EC")   // Ungu tua
        };

        // Hash string untuk mendapatkan index yang konsisten
        int hash = Math.abs((input != null ? input : "").hashCode());
        int index = hash % badgeColors.length;

        return badgeColors[index];
    }

    // ========== PARCELABLE IMPLEMENTATION ==========
    protected Event(Parcel in) {
        eventId = in.readString();
        nameEvent = in.readString();
        tanggalMulai = in.readString();
        tanggalSelesai = in.readString();
        lokasi = in.readString();
        organizer = in.readString();
        kapasitas = in.readInt();
        status = in.readString();
        posterEvent = in.readString();
        description = in.readString();
        kategori = in.readString(); // PERBAIKAN: Baca kategori
        harga = in.readString();
        dlPendaftaran = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eventId);
        dest.writeString(nameEvent);
        dest.writeString(tanggalMulai);
        dest.writeString(tanggalSelesai);
        dest.writeString(lokasi);
        dest.writeString(organizer);
        dest.writeInt(kapasitas);
        dest.writeString(status);
        dest.writeString(posterEvent);
        dest.writeString(description);
        dest.writeString(kategori);
        dest.writeString(harga);
        dest.writeString(dlPendaftaran);
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + nameEvent + '\'' +
                ", kategori='" + kategori + '\'' +
                ", harga='" + harga + '\'' +
                ", dlPendaftaran='" + dlPendaftaran + '\'' +
                '}';
    }
}