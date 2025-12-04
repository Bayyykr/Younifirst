package com.naufal.younifirst.controller;

import android.util.Log;

import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.model.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventController {
    private static final String TAG = "EventController";

    public interface EventCallback {
        void onSuccess(List<Event> events);
        void onError(String message);
    }

    public interface CreateEventCallback {
        void onSuccess(Event event);
        void onError(String message);
    }

    // ==================== CREATE EVENT WITH COMPLETE DATA (INCLUDING IMAGE) ====================
    public void createEventWithCompleteData(
            String namaEvent, String deskripsi, String tanggalMulai,
            String lokasi, String organizer, int kapasitas,
            String kategori, String harga, File imageFile,
            String waktuPelaksanaan, String deadlinePendaftaran,
            String whatsapp, String instagram,
            CreateEventCallback callback) {

        Log.d(TAG, "🎯 Membuat event dengan data lengkap...");

        // Validasi input dasar
        if (namaEvent == null || namaEvent.isEmpty()) {
            callback.onError("Nama event harus diisi");
            return;
        }

        if (deskripsi == null || deskripsi.isEmpty()) {
            callback.onError("Deskripsi harus diisi");
            return;
        }

        if (tanggalMulai == null || tanggalMulai.isEmpty()) {
            callback.onError("Tanggal mulai harus diisi");
            return;
        }

        if (lokasi == null || lokasi.isEmpty()) {
            callback.onError("Lokasi harus diisi");
            return;
        }

        if (organizer == null || organizer.isEmpty()) {
            callback.onError("Organizer harus diisi");
            return;
        }

        if (kapasitas <= 0) {
            callback.onError("Kapasitas harus lebih dari 0");
            return;
        }

        if (kategori == null || kategori.isEmpty()) {
            callback.onError("Kategori harus diisi");
            return;
        }

        // Pastikan harga berupa integer (default 0 jika kosong)
        String hargaFinal = "0";
        if (harga != null && !harga.isEmpty()) {
            try {
                // Konversi ke integer
                double hargaDouble = Double.parseDouble(harga);
                int hargaInt = (int) Math.round(hargaDouble);
                hargaFinal = String.valueOf(Math.max(0, hargaInt)); // Pastikan tidak negatif
            } catch (NumberFormatException e) {
                Log.w(TAG, "⚠ Format harga tidak valid, menggunakan default 0");
                hargaFinal = "0";
            }
        }

        // Pastikan contact fields tidak null
        if (whatsapp == null) whatsapp = "";
        if (instagram == null) instagram = "";

        Log.d(TAG, "📊 Data yang akan dikirim:");
        Log.d(TAG, "  - Nama: " + namaEvent);
        Log.d(TAG, "  - Deskripsi: " + deskripsi.substring(0, Math.min(50, deskripsi.length())) + "...");
        Log.d(TAG, "  - Tanggal Mulai: " + tanggalMulai);
        Log.d(TAG, "  - Lokasi: " + lokasi);
        Log.d(TAG, "  - Organizer: " + organizer);
        Log.d(TAG, "  - Kapasitas: " + kapasitas);
        Log.d(TAG, "  - Kategori: " + kategori);
        Log.d(TAG, "  - Harga: " + hargaFinal);
        Log.d(TAG, "  - Waktu Pelaksanaan: " + waktuPelaksanaan);
        Log.d(TAG, "  - Deadline Pendaftaran: " + deadlinePendaftaran);
        Log.d(TAG, "  - WhatsApp: " + whatsapp);
        Log.d(TAG, "  - Instagram: " + instagram);
        Log.d(TAG, "  - Image File: " + (imageFile != null ? imageFile.getAbsolutePath() : "null"));

        // Panggil API Helper
        ApiHelper.createEventWithImage(
                namaEvent, deskripsi, tanggalMulai,
                lokasi, organizer, kapasitas,
                kategori, hargaFinal, imageFile,
                waktuPelaksanaan, deadlinePendaftaran,
                whatsapp, instagram,
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d(TAG, "✅ Response API diterima: " + result);
                        try {
                            JSONObject jsonResponse = new JSONObject(result);
                            boolean success = jsonResponse.optBoolean("success", false);

                            if (success) {
                                JSONObject eventJson;
                                if (jsonResponse.has("data")) {
                                    JSONObject dataObj = jsonResponse.getJSONObject("data");
                                    if (dataObj.has("event")) {
                                        eventJson = dataObj.getJSONObject("event");
                                    } else {
                                        eventJson = dataObj;
                                    }
                                } else {
                                    eventJson = jsonResponse;
                                }

                                Event createdEvent = new Event(eventJson);
                                Log.d(TAG, "✅ Event berhasil dibuat: " + createdEvent.getNameEvent());
                                Log.d(TAG, "  - Event ID: " + createdEvent.getEventId());
                                Log.d(TAG, "  - Poster Path: " + createdEvent.getPosterEvent());
                                Log.d(TAG, "  - DL Pendaftaran: " + createdEvent.getDlPendaftaran());
                                Log.d(TAG, "  - Waktu Pelaksanaan: " + createdEvent.getWaktu_pelaksanaan());

                                callback.onSuccess(createdEvent);
                            } else {
                                String message = jsonResponse.optString("message", "Gagal membuat event");
                                Log.e(TAG, "❌ API mengembalikan error: " + message);
                                callback.onError("Gagal membuat event: " + message);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "❌ Gagal parsing JSON response: " + e.getMessage());
                            callback.onError("Format response tidak valid");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "❌ Panggilan API gagal: " + error);

                        if (error.contains("User ID diperlukan")) {
                            callback.onError("Silakan login terlebih dahulu untuk membuat event.");
                        } else if (error.contains("Missing required fields")) {
                            callback.onError("Ada field yang harus diisi: " + error);
                        } else if (error.contains("Network error")) {
                            callback.onError("Gagal terhubung ke server. Periksa koneksi internet Anda.");
                        } else {
                            callback.onError("Gagal membuat event: " + error);
                        }
                    }
                }
        );
    }

    // ==================== CREATE EVENT - SIMPLIFIED (FOR BACKWARD COMPATIBILITY) ====================
    public void createEvent(String namaEvent, String deskripsi, String tanggalMulai, String lokasi, String organizer,
                            int kapasitas, String kategori, String harga,
                            String posterEvent, String waktuPelaksanaan,
                            String deadlinePendaftaran, CreateEventCallback callback) {

        Log.d(TAG, "🎯 Membuat event baru (simplified)...");

        // Validasi input dasar
        if (namaEvent == null || namaEvent.isEmpty()) {
            callback.onError("Nama event harus diisi");
            return;
        }

        if (deskripsi == null || deskripsi.isEmpty()) {
            callback.onError("Deskripsi harus diisi");
            return;
        }

        if (tanggalMulai == null || tanggalMulai.isEmpty()) {
            callback.onError("Tanggal mulai harus diisi");
            return;
        }

        if (lokasi == null || lokasi.isEmpty()) {
            callback.onError("Lokasi harus diisi");
            return;
        }

        if (organizer == null || organizer.isEmpty()) {
            callback.onError("Organizer harus diisi");
            return;
        }

        if (kapasitas <= 0) {
            callback.onError("Kapasitas harus lebih dari 0");
            return;
        }

        if (kategori == null || kategori.isEmpty()) {
            callback.onError("Kategori harus diisi");
            return;
        }

        try {
            JSONObject eventData = new JSONObject();
            eventData.put("nama_event", namaEvent);
            eventData.put("deskripsi", deskripsi);
            eventData.put("tanggal_mulai", tanggalMulai);
            eventData.put("lokasi", lokasi);
            eventData.put("organizer", organizer);
            eventData.put("kapasitas", kapasitas);
            eventData.put("kategori", kategori);

            // Parse harga ke integer (default 0 jika kosong/invalid)
            int hargaInt = 0;
            if (harga != null && !harga.isEmpty()) {
                try {
                    if (harga.contains(".")) {
                        double hargaDouble = Double.parseDouble(harga);
                        hargaInt = (int) Math.round(hargaDouble);
                    } else {
                        hargaInt = Integer.parseInt(harga);
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "⚠ Format harga tidak valid, menggunakan 0");
                    hargaInt = 0;
                }
            }
            eventData.put("harga", hargaInt);

            // Tambahkan poster_event (bisa kosong)
            eventData.put("poster_event", posterEvent != null ? posterEvent : "");

            // Tambahkan waktu_pelaksanaan jika ada
            if (waktuPelaksanaan != null && !waktuPelaksanaan.isEmpty()) {
                eventData.put("waktu_pelaksanaan", waktuPelaksanaan);
            }

            // Tambahkan dl_pendaftaran jika ada
            if (deadlinePendaftaran != null && !deadlinePendaftaran.isEmpty()) {
                eventData.put("dl_pendaftaran", deadlinePendaftaran);
            }

            // Tambahkan contact_person dan url_instagram (default empty string)
            eventData.put("contact_person", "");
            eventData.put("url_instagram", "");

            // Tambahkan user_id (WAJIB)
            String userId = ApiHelper.getSavedUserId();
            if (userId != null && !userId.isEmpty()) {
                eventData.put("user_id", userId);
                Log.d(TAG, "👤 Menambahkan user_id: " + userId);
            } else {
                Log.e(TAG, "❌ user_id kosong, tidak bisa membuat event");
                callback.onError("User ID diperlukan. Silakan login terlebih dahulu.");
                return;
            }

            String jsonData = eventData.toString();
            Log.d(TAG, "📦 JSON data yang akan dikirim: " + jsonData);

            // Panggil API untuk create event
            ApiHelper.createEvent(jsonData, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(String result) {
                    Log.d(TAG, "✅ Response API diterima: " + result);
                    try {
                        JSONObject jsonResponse = new JSONObject(result);
                        boolean success = jsonResponse.optBoolean("success", false);

                        if (success) {
                            JSONObject eventJson;
                            if (jsonResponse.has("data")) {
                                JSONObject dataObj = jsonResponse.getJSONObject("data");
                                if (dataObj.has("event")) {
                                    eventJson = dataObj.getJSONObject("event");
                                } else {
                                    eventJson = dataObj;
                                }
                            } else {
                                eventJson = jsonResponse;
                            }

                            Event createdEvent = new Event(eventJson);
                            Log.d(TAG, "✅ Event berhasil dibuat: " + createdEvent.getNameEvent());
                            callback.onSuccess(createdEvent);
                        } else {
                            String message = jsonResponse.optString("message", "Gagal membuat event");
                            Log.e(TAG, "❌ API mengembalikan error: " + message);
                            callback.onError(message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "❌ Gagal parsing JSON response: " + e.getMessage());
                        Log.d(TAG, "Raw response: " + result);
                        callback.onError("Format response tidak valid");
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "❌ Panggilan API gagal: " + error);

                    if (error.contains("USER_ID_REQUIRED")) {
                        callback.onError("User ID diperlukan. Silakan login terlebih dahulu.");
                    } else if (error.contains("MISSING_FIELD")) {
                        String field = error.replace("MISSING_FIELD: ", "");
                        callback.onError("Field " + field + " harus diisi");
                    } else if (error.contains("INVALID_PRICE_FORMAT")) {
                        callback.onError("Format harga tidak valid. Harap masukkan angka.");
                    } else if (error.contains("INVALID_CAPACITY_FORMAT")) {
                        callback.onError("Format kapasitas tidak valid. Harap masukkan angka.");
                    } else if (error.contains("VALIDATION_ERROR")) {
                        String message = error.replace("VALIDATION_ERROR: ", "");
                        callback.onError("Validasi gagal: " + message);
                    } else {
                        callback.onError("Gagal terhubung ke server: " + error);
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "❌ Error membuat JSON: " + e.getMessage());
            callback.onError("Error menyiapkan data: " + e.getMessage());
        }
    }

    // ==================== CREATE EVENT WITH FORM DATA ====================
    public void createEventWithFormData(String namaEvent, String deskripsi, String tanggalMulai,
                                        String lokasi, String organizer, int kapasitas,
                                        String kategori, String harga, String waktuPelaksanaan,
                                        String deadlinePendaftaran, String whatsapp, String instagram,
                                        CreateEventCallback callback) {

        Log.d(TAG, "🎯 Membuat event dengan form data...");

        // Validasi input
        if (namaEvent == null || namaEvent.isEmpty()) {
            callback.onError("Nama event harus diisi");
            return;
        }

        if (deskripsi == null || deskripsi.isEmpty()) {
            callback.onError("Deskripsi harus diisi");
            return;
        }

        if (tanggalMulai == null || tanggalMulai.isEmpty()) {
            callback.onError("Tanggal mulai harus diisi");
            return;
        }

        if (lokasi == null || lokasi.isEmpty()) {
            callback.onError("Lokasi harus diisi");
            return;
        }

        if (organizer == null || organizer.isEmpty()) {
            callback.onError("Organizer harus diisi");
            return;
        }

        if (kapasitas <= 0) {
            callback.onError("Kapasitas harus lebih dari 0");
            return;
        }

        if (kategori == null || kategori.isEmpty()) {
            callback.onError("Kategori harus diisi");
            return;
        }

        // Harga default 0 jika kosong/invalid
        String hargaFinal = "0";
        if (harga != null && !harga.isEmpty()) {
            try {
                double hargaDouble = Double.parseDouble(harga);
                int hargaInt = (int) Math.round(hargaDouble);
                hargaFinal = String.valueOf(Math.max(0, hargaInt));
            } catch (NumberFormatException e) {
                Log.w(TAG, "⚠ Format harga tidak valid, menggunakan 0");
                hargaFinal = "0";
            }
        }

        // Pastikan contact fields tidak null
        if (whatsapp == null) whatsapp = "";
        if (instagram == null) instagram = "";

        ApiHelper.createEventWithFormData(
                namaEvent, deskripsi, tanggalMulai,
                lokasi, organizer, kapasitas,
                kategori, hargaFinal, waktuPelaksanaan,
                deadlinePendaftaran, whatsapp, instagram,
                new ApiHelper.ApiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jsonResponse = new JSONObject(result);
                            if (jsonResponse.getBoolean("success")) {
                                JSONObject data = jsonResponse.getJSONObject("data");
                                JSONObject eventJson = data.getJSONObject("event");

                                Event createdEvent = new Event(eventJson);
                                callback.onSuccess(createdEvent);
                            } else {
                                callback.onError("Gagal membuat event: " + jsonResponse.getString("message"));
                            }
                        } catch (JSONException e) {
                            callback.onError("Format response tidak valid");
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onError(error);
                    }
                }
        );
    }

    // ==================== FETCH EVENT METHODS ====================
    public void fetchEvent(EventCallback callback) {
        Log.d(TAG, "🎯 Mengambil events dari API...");

        ApiHelper.fetchEvent(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    List<Event> events = parseEventData(result);
                    Log.d(TAG, "✅ Berhasil parsing " + events.size() + " events");
                    callback.onSuccess(events);
                } catch (JSONException e) {
                    Log.e(TAG, "❌ Gagal parsing JSON: " + e.getMessage());
                    callback.onError("Format data tidak valid: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Gagal mengambil events: " + error);
                callback.onError("Gagal terhubung ke server: " + error);
            }
        });
    }

    public void fetchUpcomingEvents(EventCallback callback) {
        Log.d(TAG, "📅 Mengambil upcoming events...");

        fetchEvent(new EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                try {
                    List<Event> upcomingEvents = filterUpcomingEvents(events);
                    Log.d(TAG, "📊 Total upcoming events: " + upcomingEvents.size());

                    // Sort by date
                    upcomingEvents.sort(Comparator.comparing(Event::getTanggalMulai));

                    // Limit to 5 events for upcoming
                    int count = Math.min(5, upcomingEvents.size());
                    List<Event> limitedUpcoming = new ArrayList<>();
                    if (count > 0) {
                        limitedUpcoming = upcomingEvents.subList(0, count);
                    }

                    Log.d(TAG, "✅ Final upcoming events: " + limitedUpcoming.size());
                    callback.onSuccess(limitedUpcoming);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error dalam fetchUpcomingEvents: " + e.getMessage());
                    callback.onError("Terjadi kesalahan saat memfilter events");
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void fetchTrendingEvents(EventCallback callback) {
        Log.d(TAG, "🔥 Mengambil trending events...");

        fetchEvent(new EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                try {
                    List<Event> trendingEvents = filterTrendingEvents(events);
                    Log.d(TAG, "✅ Ditemukan " + trendingEvents.size() + " trending events");
                    callback.onSuccess(trendingEvents);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error dalam fetchTrendingEvents: " + e.getMessage());
                    callback.onError("Terjadi kesalahan saat memfilter trending events");
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // ==================== HELPER METHODS ====================
    private List<Event> parseEventData(String jsonString) throws JSONException {
        List<Event> events = new ArrayList<>();

        try {
            JSONObject jsonResponse = new JSONObject(jsonString);
            boolean success = jsonResponse.optBoolean("success", false);

            if (!success) {
                Log.e(TAG, "API mengembalikan success: false");
                return events;
            }

            JSONArray jsonArray = jsonResponse.optJSONArray("data");

            if (jsonArray == null) {
                Log.e(TAG, "Data array null");
                return events;
            }

            Log.d(TAG, "📦 Ditemukan " + jsonArray.length() + " events di data array");

            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Event event = new Event(jsonObject);

                    if (event.isConfirmed()) {
                        events.add(event);
                    } else {
                        Log.d(TAG, "❌ Melewati event yang belum dikonfirmasi: " + event.getNameEvent());
                    }

                } catch (JSONException e) {
                    Log.w(TAG, "⚠ Gagal parsing event #" + i + ": " + e.getMessage());
                } catch (Exception e) {
                    Log.w(TAG, "⚠ Error tak terduga saat parsing event #" + i + ": " + e.getMessage());
                }
            }

        } catch (JSONException e) {
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error tak terduga dalam parseEventData: " + e.getMessage());
        }

        Log.d(TAG, "✅ Total confirmed events yang diparsing: " + events.size());
        return events;
    }

    private List<Event> filterUpcomingEvents(List<Event> events) {
        List<Event> upcoming = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date now = new Date();

        for (Event event : events) {
            try {
                Date eventDate = dateFormat.parse(event.getTanggalMulai());
                if (eventDate != null && eventDate.after(now)) {
                    upcoming.add(event);
                }
            } catch (ParseException e) {
                Log.w(TAG, "⚠ Gagal parsing tanggal event: " + event.getTanggalMulai());
            } catch (Exception e) {
                Log.w(TAG, "⚠ Error tak terduga saat filtering event: " + e.getMessage());
            }
        }

        Log.d(TAG, "📅 Upcoming events yang difilter: " + upcoming.size());
        return upcoming;
    }

    private List<Event> filterTrendingEvents(List<Event> events) {
        List<Event> trending = new ArrayList<>();

        Log.d(TAG, "🔥 Memfilter trending events dari " + events.size() + " total events");

        try {
            events.sort(new Comparator<Event>() {
                @Override
                public int compare(Event e1, Event e2) {
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date date1 = dateFormat.parse(e1.getTanggalMulai());
                        Date date2 = dateFormat.parse(e2.getTanggalMulai());
                        return date2.compareTo(date1); // Terbaru di depan
                    } catch (ParseException e) {
                        return 0;
                    } catch (Exception e) {
                        Log.e(TAG, "Error comparing dates: " + e.getMessage());
                        return 0;
                    }
                }
            });

            int maxTrending = Math.min(10, events.size());
            trending = new ArrayList<>(events.subList(0, maxTrending));

        } catch (Exception e) {
            Log.e(TAG, "❌ Error dalam filterTrendingEvents: " + e.getMessage());
        }

        Log.d(TAG, "✅ Trending events yang dipilih: " + trending.size());
        return trending;
    }

    // ==================== VALIDATE USER LOGIN ====================
    public boolean isUserLoggedIn() {
        return ApiHelper.hasUserData();
    }

    public String getUserId() {
        return ApiHelper.getSavedUserId();
    }

    public String getUserName() {
        return ApiHelper.getSavedUserName();
    }
}