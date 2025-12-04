package com.naufal.younifirst.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.graphics.Color;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
    private String waktu_pelaksanaan;

    // ========== FIELD BARU YANG DIPERLUKAN ==========
    private String user_id;           // WAJIB untuk create event
    private String contact_person;    // WAJIB untuk API (bisa kosong)
    private String url_instagram;     // WAJIB untuk API (bisa kosong)
    // ===============================================

    private static final String BASE_URL = "http://10.131.218.36:8000";

    public Event() {
        // Constructor kosong untuk parsing manual
    }

    public Event(JSONObject json) throws JSONException {
        parseFromJSON(json);
    }

    private void parseFromJSON(JSONObject json) throws JSONException {
        // Debug untuk melihat semua field yang ada di JSON
        Log.d("EVENT_MODEL", "📊 Parsing JSON dengan keys: " + json.toString());

        // Field utama
        this.eventId = json.optString("event_id", "");
        this.nameEvent = json.optString("nama_event", "");
        this.tanggalMulai = json.optString("tanggal_mulai", "");
        this.tanggalSelesai = json.optString("tanggal_selesai", "");
        this.lokasi = json.optString("lokasi", "");
        this.organizer = json.optString("organizer", "");
        this.kapasitas = json.optInt("kapasitas", 0);
        this.status = json.optString("status", "");
        this.description = json.optString("deskripsi", "");
        this.harga = json.optString("harga", "");
        this.kategori = json.optString("kategori", "");

        // ========== FIELD YANG DIBUTUHKAN OLEH API ==========
        // 1. user_id (WAJIB)
        this.user_id = json.optString("user_id", "");
        Log.d("EVENT_MODEL", "👤 user_id from JSON: '" + this.user_id + "'");
        Log.d("EVENT_MODEL", "   JSON has 'user_id': " + json.has("user_id"));

        // 2. contact_person (WhatsApp)
        this.contact_person = json.optString("contact_person", "");
        Log.d("EVENT_MODEL", "📱 contact_person from JSON: '" + this.contact_person + "'");
        Log.d("EVENT_MODEL", "   JSON has 'contact_person': " + json.has("contact_person"));

        // 3. url_instagram
        this.url_instagram = json.optString("url_instagram", "");
        Log.d("EVENT_MODEL", "📸 url_instagram from JSON: '" + this.url_instagram + "'");
        Log.d("EVENT_MODEL", "   JSON has 'url_instagram': " + json.has("url_instagram"));

        // 4. dl_pendaftaran (deadline pendaftaran)
        this.dlPendaftaran = json.optString("dl_pendaftaran", "");
        Log.d("EVENT_MODEL", "🕒 dl_pendaftaran from JSON: '" + this.dlPendaftaran + "'");
        Log.d("EVENT_MODEL", "   JSON has 'dl_pendaftaran': " + json.has("dl_pendaftaran"));

        // 5. waktu_pelaksanaan
        this.waktu_pelaksanaan = json.optString("waktu_pelaksanaan", "");
        Log.d("EVENT_MODEL", "⏰ waktu_pelaksanaan from JSON: '" + this.waktu_pelaksanaan + "'");
        Log.d("EVENT_MODEL", "   JSON has 'waktu_pelaksanaan': " + json.has("waktu_pelaksanaan"));
        // ====================================================

        // Cari poster dari berbagai kemungkinan nama field
        findPosterPath(json);

        // Debug semua field untuk memastikan
        logAllFields();
    }

    private void findPosterPath(JSONObject json) {
        Log.d("EVENT_MODEL", "🔍 Looking for poster in JSON keys:");

        String posterPath = "";

        // Cari dari berbagai kemungkinan field name
        if (json.has("poster_event")) {
            posterPath = json.optString("poster_event", "");
            Log.d("EVENT_MODEL", "✅ Found poster_event: " + posterPath);
        }
        else if (json.has("poster")) {
            posterPath = json.optString("poster", "");
            Log.d("EVENT_MODEL", "✅ Found poster: " + posterPath);
        }
        else if (json.has("image")) {
            posterPath = json.optString("image", "");
            Log.d("EVENT_MODEL", "✅ Found image: " + posterPath);
        }
        else if (json.has("poster_path")) {
            posterPath = json.optString("poster_path", "");
            Log.d("EVENT_MODEL", "✅ Found poster_path: " + posterPath);
        }
        else {
            Log.d("EVENT_MODEL", "❌ No poster field found in JSON");
        }

        this.posterEvent = posterPath;
    }

    private void logAllFields() {
        Log.d("EVENT_MODEL", "🎯 =========== ALL EVENT FIELDS ===========");
        Log.d("EVENT_MODEL", "  event_id: " + this.eventId);
        Log.d("EVENT_MODEL", "  nama_event: " + this.nameEvent);
        Log.d("EVENT_MODEL", "  tanggal_mulai: " + this.tanggalMulai);
        Log.d("EVENT_MODEL", "  tanggal_selesai: " + this.tanggalSelesai);
        Log.d("EVENT_MODEL", "  lokasi: " + this.lokasi);
        Log.d("EVENT_MODEL", "  organizer: " + this.organizer);
        Log.d("EVENT_MODEL", "  kapasitas: " + this.kapasitas);
        Log.d("EVENT_MODEL", "  status: " + this.status);
        Log.d("EVENT_MODEL", "  deskripsi: " + this.description);
        Log.d("EVENT_MODEL", "  kategori: " + this.kategori);
        Log.d("EVENT_MODEL", "  harga: " + this.harga);
        Log.d("EVENT_MODEL", "  poster_event: " + this.posterEvent);
        Log.d("EVENT_MODEL", "  user_id: " + this.user_id);
        Log.d("EVENT_MODEL", "  contact_person: " + this.contact_person);
        Log.d("EVENT_MODEL", "  url_instagram: " + this.url_instagram);
        Log.d("EVENT_MODEL", "  dl_pendaftaran: " + this.dlPendaftaran);
        Log.d("EVENT_MODEL", "  waktu_pelaksanaan: " + this.waktu_pelaksanaan);
        Log.d("EVENT_MODEL", "==========================================");
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

    public String getWaktu_pelaksanaan() { return waktu_pelaksanaan; }
    public void setWaktu_pelaksanaan(String waktu_pelaksanaan) { this.waktu_pelaksanaan = waktu_pelaksanaan; }

    // ========== GETTER & SETTER BARU ==========
    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getContact_person() { return contact_person; }
    public void setContact_person(String contact_person) { this.contact_person = contact_person; }

    public String getUrl_instagram() { return url_instagram; }
    public void setUrl_instagram(String url_instagram) { this.url_instagram = url_instagram; }

    // ========== CONVENIENCE METHODS ==========
    /**
     * Get WhatsApp number (alias for contact_person)
     */
    public String getWhatsApp() {
        return contact_person;
    }

    /**
     * Get Instagram username (alias for url_instagram)
     */
    public String getInstagram() {
        return url_instagram;
    }

    /**
     * Check if WhatsApp is available
     */
    public boolean hasWhatsApp() {
        return contact_person != null && !contact_person.isEmpty() && !"null".equals(contact_person);
    }

    /**
     * Check if Instagram is available
     */
    public boolean hasInstagram() {
        return url_instagram != null && !url_instagram.isEmpty() && !"null".equals(url_instagram);
    }

    /**
     * Check if user_id is available
     */
    public boolean hasUser() {
        return user_id != null && !user_id.isEmpty() && !"null".equals(user_id);
    }

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

    public String getFormattedWaktuPelaksanaan() {
        if (waktu_pelaksanaan == null || waktu_pelaksanaan.isEmpty() || "null".equals(waktu_pelaksanaan)) {
            // Coba extract dari tanggalMulai jika ada format waktu
            if (tanggalMulai != null && !tanggalMulai.isEmpty()) {
                try {
                    if (tanggalMulai.contains(" ")) {
                        // Format: "2025-12-13 12:00:00"
                        String[] parts = tanggalMulai.split(" ");
                        if (parts.length > 1 && parts[1].length() >= 5) {
                            return parts[1].substring(0, 5); // HH:mm
                        }
                    } else if (tanggalMulai.contains("T")) {
                        // Format: "2025-12-13T12:00:00"
                        String[] parts = tanggalMulai.split("T");
                        if (parts.length > 1 && parts[1].length() >= 5) {
                            return parts[1].substring(0, 5); // HH:mm
                        }
                    }
                } catch (Exception e) {
                    Log.e("EVENT_MODEL", "Error extracting time from tanggalMulai", e);
                }
            }
            return "Waktu akan diumumkan";
        }

        try {
            // Waktu dari database biasanya format HH:mm:ss
            // Contoh: "17:15:00" -> kita ambil "17:15"
            String waktu = waktu_pelaksanaan.trim();

            // Jika format HH:mm:ss
            if (waktu.matches("\\d{2}:\\d{2}:\\d{2}")) {
                return waktu.substring(0, 5); // Ambil HH:mm
            }
            // Jika format HH:mm
            else if (waktu.matches("\\d{2}:\\d{2}")) {
                return waktu;
            }
            // Format lain, return as is
            else {
                return waktu;
            }
        } catch (Exception e) {
            Log.e("EVENT_MODEL", "Error formatting waktu_pelaksanaan", e);
            return waktu_pelaksanaan;
        }
    }

    public String getFormattedDateForCard() {
        try {
            // Jika dlPendaftaran tidak null/empty, gunakan itu
            if (dlPendaftaran != null && !dlPendaftaran.isEmpty() && !"null".equals(dlPendaftaran)) {
                return formatDateForCard(dlPendaftaran);
            }

            // Jika tanggalMulai tidak null/empty, gunakan itu
            if (tanggalMulai != null && !tanggalMulai.isEmpty() && !"null".equals(tanggalMulai)) {
                return formatDateForCard(tanggalMulai);
            }

            // Jika masih kosong, beri default
            return "Date not set";

        } catch (Exception e) {
            Log.e("EVENT_MODEL", "Error formatting date for card: " + e.getMessage());
            return "Invalid date";
        }
    }

    private String formatDateForCard(String dateString) {
        try {
            // Coba format pertama: yyyy-MM-dd HH:mm:ss
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateString);

            // Format untuk tampilan card: dd MMM yyyy
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
            return outputFormat.format(date);

        } catch (ParseException e1) {
            try {
                // Coba format kedua: yyyy-MM-dd
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(dateString);

                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
                return outputFormat.format(date);

            } catch (ParseException e2) {
                // Jika semua format gagal, return asli
                return dateString;
            }
        }
    }

    public String getFormattedDlPendaftaran() {
        try {
            if (dlPendaftaran == null || dlPendaftaran.isEmpty() || "null".equals(dlPendaftaran)) {
                return "Deadline: Belum ditentukan";
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

    public String getFullPosterUrl() {
        if (posterEvent == null || posterEvent.isEmpty() || "null".equals(posterEvent)) {
            Log.d("EVENT_POSTER", "❌ Poster kosong atau null");
            return null;
        }

        Log.d("EVENT_POSTER", "🖼 Original poster path: " + posterEvent);

        // Jika sudah URL langsung
        if (posterEvent.startsWith("http://") || posterEvent.startsWith("https://")) {
            Log.d("EVENT_POSTER", "✅ Already a full URL");
            return posterEvent;
        }

        // PERBAIKAN: Hapus "/" di awal jika ada
        String path = posterEvent.trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
            Log.d("EVENT_POSTER", "🔄 Removed leading slash: " + path);
        }

        // PERBAIKAN: Pastikan BASE_URL tidak berakhir dengan slash ganda
        String baseUrl = BASE_URL;
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // Bentuk full URL
        String fullUrl = baseUrl + "/" + path;
        Log.d("EVENT_POSTER", "🔗 Converted full URL: " + fullUrl);

        return fullUrl;
    }

    public String getSafePosterUrl() {
        String url = getFullPosterUrl();
        return (url != null && !url.isEmpty() && !"null".equals(url)) ? url : null;
    }

    public String getPosterUrlOrPlaceholder() {
        String url = getSafePosterUrl();
        if (url != null) {
            return url;
        }

        // Jika tidak ada poster, return placeholder image name
        return "tryposter";
    }

    // ========== STATUS METHODS ==========
    public boolean isConfirmed() {
        return "confirm".equalsIgnoreCase(status);
    }

    public boolean isTrending() {
        return kapasitas > 300;
    }

    // ========== DEADLINE METHODS ==========
    public boolean isPastDeadline() {
        return getDaysUntilDeadline() < 0;
    }

    public boolean isAlmostEnding() {
        return getDaysUntilDeadline() >= 0 && getDaysUntilDeadline() <= 7;
    }

    public int getDaysUntilDeadline() {
        if (dlPendaftaran == null || dlPendaftaran.isEmpty() || "null".equals(dlPendaftaran)) {
            return 999; // Jika tidak ada deadline, anggap masih lama
        }

        try {
            // Parse deadline date
            Date deadlineDate = null;

            // Coba format yyyy-MM-dd HH:mm:ss
            try {
                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                deadlineDate = format1.parse(dlPendaftaran);
            } catch (ParseException e1) {
                // Coba format yyyy-MM-dd
                try {
                    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    deadlineDate = format2.parse(dlPendaftaran);
                } catch (ParseException e2) {
                    Log.e("EVENT_MODEL", "Error parsing deadline date: " + dlPendaftaran);
                    return 999;
                }
            }

            if (deadlineDate == null) {
                return 999;
            }

            // Get current date
            Date currentDate = new Date();

            // Calculate difference in days
            long diffInMillis = deadlineDate.getTime() - currentDate.getTime();
            long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

            return (int) diffInDays;

        } catch (Exception e) {
            Log.e("EVENT_MODEL", "Error calculating days until deadline", e);
            return 999;
        }
    }

    public String getDeadlineStatusText() {
        int days = getDaysUntilDeadline();

        if (days < 0) {
            return "Berakhir"; // Sudah lewat deadline
        } else if (days == 0) {
            return "Hari ini\nBerakhir"; // Deadline hari ini
        } else if (days == 1) {
            return "Besok\nBerakhir"; // Deadline besok
        } else if (days <= 7) {
            return "Hampir\nBerakhir"; // H-7 atau kurang
        } else {
            return null; // Tidak perlu badge
        }
    }

    // ========== BADGE METHODS ==========
    public int getBadgeColor() {
        if (kategori == null || kategori.isEmpty()) {
            return generateColorFromString(nameEvent != null ? nameEvent : "Event");
        }
        return generateColorFromString(kategori);
    }

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
        kategori = in.readString();
        harga = in.readString();
        dlPendaftaran = in.readString();
        waktu_pelaksanaan = in.readString();
        user_id = in.readString();
        contact_person = in.readString();
        url_instagram = in.readString();
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
        dest.writeString(waktu_pelaksanaan);
        dest.writeString(user_id);
        dest.writeString(contact_person);
        dest.writeString(url_instagram);
    }

    // ========== DEBUG METHODS ==========
    public void printDebugInfo() {
        Log.d("EVENT_DEBUG", "=== EVENT DEBUG INFO ===");
        Log.d("EVENT_DEBUG", "ID: " + eventId);
        Log.d("EVENT_DEBUG", "Name: " + nameEvent);
        Log.d("EVENT_DEBUG", "Description: " + description);
        Log.d("EVENT_DEBUG", "Date: " + tanggalMulai);
        Log.d("EVENT_DEBUG", "Location: " + lokasi);
        Log.d("EVENT_DEBUG", "Organizer: " + organizer);
        Log.d("EVENT_DEBUG", "Capacity: " + kapasitas);
        Log.d("EVENT_DEBUG", "Category: " + kategori);
        Log.d("EVENT_DEBUG", "Price: " + harga);
        Log.d("EVENT_DEBUG", "Poster: " + posterEvent);
        Log.d("EVENT_DEBUG", "User ID: " + user_id);
        Log.d("EVENT_DEBUG", "WhatsApp: " + contact_person);
        Log.d("EVENT_DEBUG", "Instagram: " + url_instagram);
        Log.d("EVENT_DEBUG", "Waktu Pelaksanaan: " + waktu_pelaksanaan);
        Log.d("EVENT_DEBUG", "DL Pendaftaran: " + dlPendaftaran);
        Log.d("EVENT_DEBUG", "Status: " + status);
        Log.d("EVENT_DEBUG", "Confirmed: " + isConfirmed());
        Log.d("EVENT_DEBUG", "========================");
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", nameEvent='" + nameEvent + '\'' +
                ", kategori='" + kategori + '\'' +
                ", harga='" + harga + '\'' +
                ", user_id='" + user_id + '\'' +
                ", contact_person='" + contact_person + '\'' +
                ", url_instagram='" + url_instagram + '\'' +
                ", dlPendaftaran='" + dlPendaftaran + '\'' +
                ", waktu_pelaksanaan='" + waktu_pelaksanaan + '\'' +
                '}';
    }
}