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
import java.util.Calendar;
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
                double hargaDouble = Double.parseDouble(harga);
                int hargaInt = (int) Math.round(hargaDouble);
                hargaFinal = String.valueOf(Math.max(0, hargaInt));
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

    // ==================== CREATE EVENT - SIMPLIFIED ====================
    public void createEvent(String namaEvent, String deskripsi, String tanggalMulai, String lokasi, String organizer,
                            int kapasitas, String kategori, String harga,
                            String posterEvent, String waktuPelaksanaan,
                            String deadlinePendaftaran, CreateEventCallback callback) {

        Log.d(TAG, "🎯 Membuat event baru (simplified)...");

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

            eventData.put("poster_event", posterEvent != null ? posterEvent : "");

            if (waktuPelaksanaan != null && !waktuPelaksanaan.isEmpty()) {
                eventData.put("waktu_pelaksanaan", waktuPelaksanaan);
            }

            if (deadlinePendaftaran != null && !deadlinePendaftaran.isEmpty()) {
                eventData.put("dl_pendaftaran", deadlinePendaftaran);
            }

            eventData.put("contact_person", "");
            eventData.put("url_instagram", "");

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

    // ==================== FETCH ALL EVENTS ====================
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
                    Log.d(TAG, "Raw response: " + result);
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

    // ==================== FETCH UPCOMING EVENTS (DIPERBAIKI) ====================
    public void fetchUpcomingEvents(EventCallback callback) {
        Log.d(TAG, "📅 Mengambil upcoming events...");

        fetchEvent(new EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                try {
                    Log.d(TAG, "📊 Total events ditemukan: " + events.size());

                    // Debug: tampilkan semua event yang diterima
                    for (Event event : events) {
                        Log.d(TAG, "📝 Event: " + event.getNameEvent() +
                                " | Tanggal: " + event.getTanggalMulai() +
                                " | Status: " + event.getStatus() +
                                " | Confirmed: " + event.isConfirmed());
                    }

                    List<Event> upcomingEvents = filterUpcomingEvents(events);

                    // Batasi maksimal 5 event
                    int limit = Math.min(5, upcomingEvents.size());
                    List<Event> limitedUpcoming = upcomingEvents.subList(0, limit);

                    Log.d(TAG, "✅ Final upcoming events: " + limitedUpcoming.size() + " events");
                    for (int i = 0; i < limitedUpcoming.size(); i++) {
                        Event e = limitedUpcoming.get(i);
                        Log.d(TAG, "📅 Upcoming #" + (i + 1) + ": " + e.getNameEvent() + " | " + e.getTanggalMulai());
                    }

                    callback.onSuccess(limitedUpcoming);

                } catch (Exception e) {
                    Log.e(TAG, "❌ Error dalam fetchUpcomingEvents: " + e.getMessage());
                    e.printStackTrace();
                    callback.onError("Terjadi kesalahan saat memfilter events");
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // ==================== FETCH TRENDING EVENTS ====================
    public void fetchTrendingEvents(EventCallback callback) {
        Log.d(TAG, "🔥 Mengambil trending events...");

        fetchEvent(new EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                try {
                    List<Event> trendingEvents = filterTrendingEvents(events);
                    Log.d(TAG, "✅ Ditemukan " + trendingEvents.size() + " trending events dari total " + events.size() + " events");
                    callback.onSuccess(trendingEvents);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error dalam fetchTrendingEvents: " + e.getMessage());
                    e.printStackTrace();
                    callback.onError("Terjadi kesalahan saat memfilter trending events");
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // ==================== PARSE EVENT DATA ====================
    private List<Event> parseEventData(String jsonString) throws JSONException {
        List<Event> events = new ArrayList<>();

        try {
            Log.d(TAG, "📦 Parsing JSON response...");

            JSONObject jsonResponse = new JSONObject(jsonString);
            JSONArray jsonArray = null;

            if (jsonResponse.has("data")) {
                Object dataObj = jsonResponse.get("data");
                if (dataObj instanceof JSONArray) {
                    jsonArray = (JSONArray) dataObj;
                    Log.d(TAG, "📊 Data adalah JSONArray dengan " + jsonArray.length() + " items");
                } else if (dataObj instanceof JSONObject) {
                    JSONObject dataObjJson = (JSONObject) dataObj;
                    if (dataObjJson.has("events")) {
                        jsonArray = dataObjJson.getJSONArray("events");
                        Log.d(TAG, "📊 Data adalah JSONObject dengan events array: " + jsonArray.length() + " items");
                    } else if (dataObjJson.has("event")) {
                        // Jika hanya satu event
                        jsonArray = new JSONArray();
                        jsonArray.put(dataObjJson.getJSONObject("event"));
                        Log.d(TAG, "📊 Data adalah JSONObject dengan single event");
                    }
                }
            } else if (jsonResponse.has("events")) {
                jsonArray = jsonResponse.getJSONArray("events");
                Log.d(TAG, "📊 Events array langsung ditemukan: " + jsonArray.length() + " items");
            }

            if (jsonArray == null || jsonArray.length() == 0) {
                Log.d(TAG, "ℹ Tidak ada data events ditemukan");
                return events;
            }

            Log.d(TAG, "📦 Mulai parsing " + jsonArray.length() + " events");

            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Event event = new Event(jsonObject);
                    events.add(event);

                    Log.d(TAG, "✅ Parsed event #" + (i+1) + ": " + event.getNameEvent() +
                            " | Date: " + event.getTanggalMulai() +
                            " | Status: " + event.getStatus());

                } catch (JSONException e) {
                    Log.w(TAG, "⚠ Gagal parsing event #" + i + ": " + e.getMessage());
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "❌ JSON parsing error: " + e.getMessage());
            throw e;
        }

        Log.d(TAG, "✅ Total events yang berhasil diparsing: " + events.size());
        return events;
    }

    // ==================== FILTER UPCOMING EVENTS (DIPERBAIKI) ====================
    private List<Event> filterUpcomingEvents(List<Event> events) {
        List<Event> upcoming = new ArrayList<>();
        Date today = removeTime(new Date());

        Log.d(TAG, "🔍 Memfilter upcoming events dari " + events.size() + " total events");
        Log.d(TAG, "📅 Tanggal sekarang (tanpa waktu): " + formatDateForLog(today));

        for (Event event : events) {
            try {
                String tanggalMulai = event.getTanggalMulai();

                // Debug detail
                Log.d(TAG, "🔍 Checking event: " + event.getNameEvent() +
                        " | Date String: " + tanggalMulai +
                        " | Status: " + event.getStatus() +
                        " | Confirmed: " + event.isConfirmed());

                // 1. Cek apakah event sudah dikonfirmasi
                if (!event.isConfirmed()) {
                    Log.d(TAG, "  ⏭ Lewati - belum dikonfirmasi");
                    continue;
                }

                // 2. Parse tanggal mulai
                Date eventDate = parseDate(tanggalMulai);
                if (eventDate == null) {
                    Log.d(TAG, "  ⚠ Lewati - tanggal tidak valid: " + tanggalMulai);
                    continue;
                }

                // Hapus waktu dari event date untuk perbandingan yang akurat
                Date eventDateWithoutTime = removeTime(eventDate);
                Log.d(TAG, "  📅 Event date (parsed): " + formatDateForLog(eventDateWithoutTime));

                // 3. PERBAIKAN: Tambahkan event jika tanggalnya >= hari ini
                if (eventDateWithoutTime.after(today) || isSameDay(eventDateWithoutTime, today)) {
                    upcoming.add(event);
                    Log.d(TAG, "  ✅ Ditambahkan sebagai upcoming");
                } else {
                    Log.d(TAG, "  ⏭ Lewati - tanggal sudah lewat");
                }

            } catch (Exception e) {
                Log.w(TAG, "⚠ Error filtering event '" + event.getNameEvent() + "': " + e.getMessage());
            }
        }

        Log.d(TAG, "📅 Upcoming events yang difilter: " + upcoming.size());

        // Sort by date ascending (terdekat di depan)
        upcoming.sort(new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                try {
                    Date date1 = parseDate(e1.getTanggalMulai());
                    Date date2 = parseDate(e2.getTanggalMulai());

                    if (date1 == null || date2 == null) return 0;

                    // Terdekat di depan
                    return date1.compareTo(date2);
                } catch (Exception e) {
                    return 0;
                }
            }
        });

        return upcoming;
    }

    // ==================== FILTER TRENDING EVENTS ====================
    private List<Event> filterTrendingEvents(List<Event> events) {
        List<Event> trending = new ArrayList<>();

        Log.d(TAG, "🔥 Memfilter trending events dari " + events.size() + " total events");

        try {
            // Ambil semua events yang confirmed
            List<Event> validEvents = new ArrayList<>();
            for (Event event : events) {
                if (event.isConfirmed()) {
                    validEvents.add(event);
                }
            }

            Log.d(TAG, "🔥 Valid events untuk trending: " + validEvents.size());

            // Sort by date descending (terbaru di depan) dan kapasitas (terbesar di depan)
            validEvents.sort(new Comparator<Event>() {
                @Override
                public int compare(Event e1, Event e2) {
                    try {
                        Date date1 = parseDate(e1.getTanggalMulai());
                        Date date2 = parseDate(e2.getTanggalMulai());

                        if (date1 == null || date2 == null) {
                            return 0;
                        }

                        // Terbaru di depan
                        int dateCompare = date2.compareTo(date1);

                        // Jika tanggal sama, sort by kapasitas (terbesar di depan)
                        if (dateCompare == 0) {
                            return Integer.compare(e2.getKapasitas(), e1.getKapasitas());
                        }

                        return dateCompare;
                    } catch (Exception e) {
                        Log.e(TAG, "Error comparing dates for trending: " + e.getMessage());
                        return 0;
                    }
                }
            });

            // Ambil maksimal 10 event terbaru
            int maxTrending = Math.min(10, validEvents.size());
            trending = new ArrayList<>(validEvents.subList(0, maxTrending));

            // Debug: tampilkan trending events
            for (int i = 0; i < trending.size(); i++) {
                Event e = trending.get(i);
                Log.d(TAG, "🔥 Trending #" + (i+1) + ": " + e.getNameEvent() +
                        " | Date: " + e.getTanggalMulai() +
                        " | Capacity: " + e.getKapasitas());
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error dalam filterTrendingEvents: " + e.getMessage());
            e.printStackTrace();
        }

        Log.d(TAG, "✅ Trending events yang dipilih: " + trending.size());
        return trending;
    }

    // ==================== UTILITY METHODS ====================
    private Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private String formatDateForLog(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    private Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty() || "null".equals(dateString)) {
            return null;
        }

        // Coba berbagai format tanggal
        String[] dateFormats = {
                "yyyy-MM-dd HH:mm:ss",      // Format MySQL datetime
                "yyyy-MM-dd'T'HH:mm:ss",    // Format ISO
                "yyyy-MM-dd",               // Format tanggal saja
                "dd-MM-yyyy HH:mm:ss",      // Format Indonesia dengan waktu
                "dd-MM-yyyy",               // Format Indonesia tanpa waktu
                "dd/MM/yyyy HH:mm:ss",      // Format Indonesia dengan slash
                "dd/MM/yyyy"                // Format Indonesia dengan slash tanpa waktu
        };

        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setLenient(false);
                Date parsedDate = sdf.parse(dateString);

                // Jika berhasil parse, coba format ulang untuk debug
                if (parsedDate != null) {
                    Log.d(TAG, "✅ Berhasil parse tanggal '" + dateString +
                            "' dengan format '" + format + "' -> " +
                            new SimpleDateFormat("yyyy-MM-dd").format(parsedDate));
                }

                return parsedDate;
            } catch (ParseException e) {
                // Continue to next format
            }
        }

        Log.w(TAG, "⚠ Gagal parse tanggal dengan semua format: " + dateString);
        return null;
    }

    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date1).equals(sdf.format(date2));
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