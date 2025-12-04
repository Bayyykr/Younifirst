package com.naufal.younifirst.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiHelper {
    private static final String BASE_URL = "http://10.131.218.36:8000";
    private static final String TAG = "API_DEBUG";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // SharedPreferences keys
    private static final String PREFS_NAME = "YouniFirstPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";

    private static SharedPreferences prefs;
    private static boolean isInitialized = false;
    private static OkHttpClient client;

    public interface ApiCallback {
        void onSuccess(String result);
        void onFailure(String error);
    }

    // ==================== INITIALIZATION ====================
    public static void initialize(Context context) {
        if (!isInitialized) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .followRedirects(false)
                    .addInterceptor(chain -> {
                        Request request = chain.request();
                        Log.d(TAG, "➡ SENDING: " + request.method() + " " + request.url());
                        Response response = chain.proceed(request);
                        Log.d(TAG, "⬅ RESPONSE: " + response.code() + " " + response.message());
                        return response;
                    })
                    .build();

            isInitialized = true;
        }
    }

    // ==================== CREATE EVENT WITH IMAGE ====================
    public static void createEventWithImage(
            String namaEvent, String deskripsi, String tanggalMulai,
            String lokasi, String organizer, int kapasitas,
            String kategori, String harga, File imageFile,
            String waktuPelaksanaan, String deadlinePendaftaran,
            String whatsapp, String instagram,
            ApiCallback callback) {

        Log.d(TAG, "🎯 CREATE EVENT WITH IMAGE");

        if (!isInitialized) {
            callback.onFailure("ApiHelper not initialized");
            return;
        }

        String url = BASE_URL + "/api/events/create";

        try {
            // Tentukan apakah akan mengirim sebagai JSON atau Multipart
            boolean hasImage = (imageFile != null && imageFile.exists());

            if (hasImage) {
                // Kirim sebagai Multipart (dengan gambar)
                sendMultipartRequest(
                        namaEvent, deskripsi, tanggalMulai,
                        lokasi, organizer, kapasitas,
                        kategori, harga, imageFile,
                        waktuPelaksanaan, deadlinePendaftaran,
                        whatsapp, instagram, url, callback
                );
            } else {
                // Kirim sebagai JSON (tanpa gambar)
                sendJsonRequest(
                        namaEvent, deskripsi, tanggalMulai,
                        lokasi, organizer, kapasitas,
                        kategori, harga,
                        waktuPelaksanaan, deadlinePendaftaran,
                        whatsapp, instagram, url, callback
                );
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error preparing request: " + e.getMessage());
            callback.onFailure("Error: " + e.getMessage());
        }
    }

    // ==================== SEND MULTIPART REQUEST (WITH IMAGE) ====================
    private static void sendMultipartRequest(
            String namaEvent, String deskripsi, String tanggalMulai,
            String lokasi, String organizer, int kapasitas,
            String kategori, String harga, File imageFile,
            String waktuPelaksanaan, String deadlinePendaftaran,
            String whatsapp, String instagram, String url,
            ApiCallback callback) {

        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);

            // Tambahkan semua data sebagai form fields
            builder.addFormDataPart("nama_event", namaEvent);
            builder.addFormDataPart("deskripsi", deskripsi);
            builder.addFormDataPart("tanggal_mulai", tanggalMulai);
            builder.addFormDataPart("lokasi", lokasi);
            builder.addFormDataPart("organizer", organizer);
            builder.addFormDataPart("kapasitas", String.valueOf(kapasitas));
            builder.addFormDataPart("kategori", kategori);

            // Harga sebagai integer (default 0)
            int hargaInt = 0;
            if (harga != null && !harga.isEmpty()) {
                try {
                    hargaInt = Integer.parseInt(harga);
                } catch (NumberFormatException e) {
                    hargaInt = 0;
                }
            }
            builder.addFormDataPart("harga", String.valueOf(hargaInt));

            // User ID (WAJIB)
            String userId = getSavedUserId();
            if (userId != null && !userId.isEmpty()) {
                builder.addFormDataPart("user_id", userId);
            } else {
                callback.onFailure("User ID diperlukan. Silakan login terlebih dahulu.");
                return;
            }

            // Contact fields (bisa kosong)
            builder.addFormDataPart("contact_person", whatsapp != null ? whatsapp : "");
            builder.addFormDataPart("url_instagram", instagram != null ? instagram : "");

            // Optional fields (jika ada)
            if (waktuPelaksanaan != null && !waktuPelaksanaan.isEmpty()) {
                builder.addFormDataPart("waktu_pelaksanaan", waktuPelaksanaan);
            }

            if (deadlinePendaftaran != null && !deadlinePendaftaran.isEmpty()) {
                // Gabungkan dengan waktu jika ada
                String dlFull = deadlinePendaftaran;
                if (waktuPelaksanaan != null && !waktuPelaksanaan.isEmpty()) {
                    dlFull += " " + waktuPelaksanaan + ":00";
                } else {
                    dlFull += " 23:59:59";
                }
                builder.addFormDataPart("dl_pendaftaran", dlFull);
            }

            // Tambahkan file gambar
            if (imageFile.exists()) {
                MediaType mediaType = MediaType.parse("image/jpeg");
                if (mediaType == null) {
                    mediaType = MediaType.parse("image/*");
                }

                String fileName = "poster_" + System.currentTimeMillis() + ".jpg";
                RequestBody fileBody = RequestBody.create(mediaType, imageFile);
                builder.addFormDataPart("poster_event", fileName, fileBody);

                Log.d(TAG, "📸 Adding image file: " + fileName);
            } else {
                builder.addFormDataPart("poster_event", "");
            }

            RequestBody requestBody = builder.build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "Android-App")
                    .build();

            Log.d(TAG, "🚀 Sending multipart request to: " + url);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleResponse(response, callback);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error in multipart request: " + e.getMessage());
            callback.onFailure("Error: " + e.getMessage());
        }
    }

    // ==================== SEND JSON REQUEST (NO IMAGE) ====================
    private static void sendJsonRequest(
            String namaEvent, String deskripsi, String tanggalMulai,
            String lokasi, String organizer, int kapasitas,
            String kategori, String harga,
            String waktuPelaksanaan, String deadlinePendaftaran,
            String whatsapp, String instagram, String url,
            ApiCallback callback) {

        try {
            JSONObject jsonData = new JSONObject();

            // Field yang diperlukan
            jsonData.put("nama_event", namaEvent);
            jsonData.put("deskripsi", deskripsi);
            jsonData.put("tanggal_mulai", tanggalMulai);
            jsonData.put("lokasi", lokasi);
            jsonData.put("organizer", organizer);
            jsonData.put("kapasitas", kapasitas);
            jsonData.put("kategori", kategori);

            // Harga sebagai integer (default 0)
            int hargaInt = 0;
            if (harga != null && !harga.isEmpty()) {
                try {
                    hargaInt = Integer.parseInt(harga);
                } catch (NumberFormatException e) {
                    hargaInt = 0;
                }
            }
            jsonData.put("harga", hargaInt);

            // User ID (WAJIB)
            String userId = getSavedUserId();
            if (userId != null && !userId.isEmpty()) {
                jsonData.put("user_id", userId);
            } else {
                callback.onFailure("User ID diperlukan. Silakan login terlebih dahulu.");
                return;
            }

            // Contact fields (bisa kosong tapi harus ada)
            jsonData.put("contact_person", whatsapp != null ? whatsapp : "");
            jsonData.put("url_instagram", instagram != null ? instagram : "");
            jsonData.put("poster_event", ""); // Kosong karena tidak ada gambar

            // Optional fields (jika ada)
            if (waktuPelaksanaan != null && !waktuPelaksanaan.isEmpty()) {
                jsonData.put("waktu_pelaksanaan", waktuPelaksanaan);
            }

            if (deadlinePendaftaran != null && !deadlinePendaftaran.isEmpty()) {
                // Gabungkan dengan waktu jika ada
                String dlFull = deadlinePendaftaran;
                if (waktuPelaksanaan != null && !waktuPelaksanaan.isEmpty()) {
                    dlFull += " " + waktuPelaksanaan + ":00";
                } else {
                    dlFull += " 23:59:59";
                }
                jsonData.put("dl_pendaftaran", dlFull);
            }

            Log.d(TAG, "📦 JSON Data: " + jsonData.toString(2));

            RequestBody body = RequestBody.create(jsonData.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "Android-App")
                    .build();

            Log.d(TAG, "🚀 Sending JSON request to: " + url);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleResponse(response, callback);
                }
            });

        } catch (Exception e) {
            callback.onFailure("Error: " + e.getMessage());
        }
    }

    // ==================== HANDLE RESPONSE ====================
    private static void handleResponse(Response response, ApiCallback callback) throws IOException {
        int responseCode = response.code();
        String result = response.body() != null ? response.body().string() : "";

        Log.d(TAG, "📡 Response code: " + responseCode);
        Log.d(TAG, "📡 Response body: " + result);

        if (responseCode == 201 || responseCode == 200) {
            callback.onSuccess(result);
        } else {
            String errorMsg = "Server error: " + responseCode;
            try {
                JSONObject errorJson = new JSONObject(result);
                errorMsg = errorJson.optString("message", errorMsg);
            } catch (JSONException e) {
                // Jika bukan JSON, gunakan response body
                if (!result.isEmpty()) {
                    errorMsg = result;
                }
            }
            callback.onFailure(errorMsg);
        }
    }

    // Metode untuk upload gambar terpisah
    private static void uploadImageFirst(File imageFile, ApiCallback callback) {
        String url = BASE_URL + "/api/upload-image"; // Buat endpoint upload gambar terpisah

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        MediaType mediaType = MediaType.parse("image/jpeg");
        if (mediaType == null) {
            mediaType = MediaType.parse("image/*");
        }

        RequestBody fileBody = RequestBody.create(mediaType, imageFile);
        builder.addFormDataPart("image", imageFile.getName(), fileBody);
        builder.addFormDataPart("type", "event");

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Upload gagal: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    callback.onSuccess(result);
                } else {
                    callback.onFailure("Upload gagal: " + response.code());
                }
            }
        });
    }

    // Metode untuk mengirim data event
    private static void sendEventData(JSONObject jsonData, ApiCallback callback) {
        String url = BASE_URL + "/api/events/create";

        try {
            // Log data yang akan dikirim
            Log.d(TAG, "📦 JSON Data: " + jsonData.toString(2));

            RequestBody body = RequestBody.create(jsonData.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Accept", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    int responseCode = response.code();
                    String result = response.body() != null ? response.body().string() : "";

                    Log.d(TAG, "📡 Response code: " + responseCode);
                    Log.d(TAG, "📡 Response body: " + result);

                    if (responseCode == 201 || responseCode == 200) {
                        callback.onSuccess(result);
                    } else {
                        // Coba parse error message
                        try {
                            JSONObject errorJson = new JSONObject(result);
                            String message = errorJson.optString("message", "Unknown error");
                            callback.onFailure("Server error " + responseCode + ": " + message);
                        } catch (JSONException e) {
                            callback.onFailure("Server error " + responseCode + ": " + result);
                        }
                    }
                }
            });

        } catch (Exception e) {
            callback.onFailure("Error: " + e.getMessage());
        }
    }

    // Helper method untuk generate random string
    private static String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    // ==================== CREATE EVENT (FOR BACKWARD COMPATIBILITY) ====================
    public static void createEvent(String jsonData, ApiCallback callback) {
        Log.d(TAG, "🎯 CREATE EVENT REQUEST");

        if (!isInitialized) {
            callback.onFailure("ApiHelper not initialized");
            return;
        }

        String url = BASE_URL + "/api/events/create";

        try {
            // Parse JSON untuk memastikan semua field yang diperlukan ada
            JSONObject jsonObject = new JSONObject(jsonData);

            // Validasi field yang diperlukan sesuai dengan PHP API
            String[] requiredFields = {"nama_event", "deskripsi", "tanggal_mulai",
                    "lokasi", "organizer", "kapasitas", "kategori", "user_id"};

            for (String field : requiredFields) {
                if (!jsonObject.has(field) ||
                        jsonObject.isNull(field) ||
                        jsonObject.getString(field).isEmpty()) {
                    callback.onFailure("MISSING_FIELD: " + field);
                    return;
                }
            }

            // Harga boleh kosong/null
            if (!jsonObject.has("harga") || jsonObject.isNull("harga")) {
                jsonObject.put("harga", "");
            }

            // Pastikan kapasitas adalah integer
            if (jsonObject.has("kapasitas")) {
                try {
                    int kapasitas = jsonObject.getInt("kapasitas");
                    jsonObject.put("kapasitas", kapasitas);
                } catch (JSONException e) {
                    callback.onFailure("INVALID_CAPACITY_FORMAT");
                    return;
                }
            }

            // Tambahkan poster_event jika ada
            if (!jsonObject.has("poster_event")) {
                jsonObject.put("poster_event", "");
            }

            // Tambahkan user_id jika belum ada
            if (!jsonObject.has("user_id")) {
                jsonObject.put("user_id", getSavedUserId());
            }

            jsonData = jsonObject.toString();
            Log.d(TAG, "📦 Final JSON data: " + jsonData);

        } catch (JSONException e) {
            Log.e(TAG, "❌ JSON parsing error: " + e.getMessage());
            callback.onFailure("JSON_PARSING_ERROR: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(jsonData, JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "Android-App")
                .addHeader("Authorization", "Bearer " + getSavedToken())
                .build();

        Log.d(TAG, "🚀 Sending POST to: " + url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String error = "Network error: " + e.getMessage();
                Log.e(TAG, error);
                callback.onFailure(error);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int responseCode = response.code();
                String result = response.body() != null ? response.body().string() : "";

                Log.d(TAG, "📡 Response code: " + responseCode);
                Log.d(TAG, "📡 Response body: " + result);

                // Cek header Location untuk tahu kemana redirect
                if (responseCode == 302 || responseCode == 301) {
                    String location = response.header("Location", "");
                    Log.e(TAG, "⚠ REDIRECT DETECTED to: " + location);

                    if (location.contains("login") || location.contains("Login")) {
                        callback.onFailure("REDIRECT_TO_LOGIN: Endpoint requires session");
                    } else {
                        callback.onFailure("REDIRECT_UNKNOWN: " + location);
                    }
                    return;
                }

                try {
                    if (responseCode == 201) {
                        // Success created
                        JSONObject jsonResponse = new JSONObject(result);
                        boolean success = jsonResponse.optBoolean("success", false);

                        if (success) {
                            Log.d(TAG, "✅ Event created successfully");
                            callback.onSuccess(result);
                        } else {
                            String message = jsonResponse.optString("message", "Unknown error");
                            Log.e(TAG, "❌ API error: " + message);
                            callback.onFailure("API_ERROR: " + message);
                        }
                    } else if (responseCode == 400) {
                        // Bad request
                        JSONObject jsonResponse = new JSONObject(result);
                        String message = jsonResponse.optString("message", "Bad request");
                        Log.e(TAG, "❌ Validation error: " + message);
                        callback.onFailure("VALIDATION_ERROR: " + message);
                    } else if (responseCode == 405) {
                        Log.e(TAG, "❌ Method not allowed");
                        callback.onFailure("METHOD_NOT_ALLOWED");
                    } else if (responseCode >= 500) {
                        Log.e(TAG, "❌ Server error");
                        callback.onFailure("SERVER_ERROR: " + responseCode);
                    } else {
                        Log.e(TAG, "❌ Unexpected response: " + responseCode);
                        callback.onFailure("UNEXPECTED_RESPONSE: " + responseCode);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "❌ Invalid JSON response: " + result);
                    callback.onFailure("INVALID_RESPONSE_FORMAT");
                }
            }
        });
    }

    // ==================== CREATE EVENT WITH FORM DATA ====================
    public static void createEventWithFormData(String namaEvent, String deskripsi, String tanggalMulai,
                                               String lokasi, String organizer, int kapasitas,
                                               String kategori, String harga, String waktuPelaksanaan,
                                               String deadlinePendaftaran, String whatsapp, String instagram,
                                               ApiCallback callback) {

        Log.d(TAG, "🎯 CREATE EVENT WITH FORM DATA");

        String url = BASE_URL + "/api/events/create";

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("nama_event", namaEvent)
                .add("deskripsi", deskripsi)
                .add("tanggal_mulai", tanggalMulai)
                .add("lokasi", lokasi)
                .add("organizer", organizer)
                .add("kapasitas", String.valueOf(kapasitas))
                .add("kategori", kategori)
                .add("user_id", getSavedUserId());

        // PERBAIKAN: Harga boleh null/kosong
        if (harga != null && !harga.isEmpty()) {
            try {
                if (harga.contains(".")) {
                    double hargaDouble = Double.parseDouble(harga);
                    formBuilder.add("harga", String.valueOf((int) Math.round(hargaDouble)));
                } else {
                    formBuilder.add("harga", harga);
                }
            } catch (NumberFormatException e) {
                // Jika bukan angka, kosongkan
                formBuilder.add("harga", "");
            }
        } else {
            formBuilder.add("harga", "");
        }

        // Gabungkan tanggal dan waktu untuk dl_pendaftaran
        if (deadlinePendaftaran != null && !deadlinePendaftaran.isEmpty() &&
                waktuPelaksanaan != null && !waktuPelaksanaan.isEmpty()) {
            String dlPendaftaranFull = deadlinePendaftaran + " " + waktuPelaksanaan + ":00";
            formBuilder.add("dl_pendaftaran", dlPendaftaranFull);
        } else if (deadlinePendaftaran != null && !deadlinePendaftaran.isEmpty()) {
            String dlPendaftaranFull = deadlinePendaftaran + " 23:59:59";
            formBuilder.add("dl_pendaftaran", dlPendaftaranFull);
        }

        if (waktuPelaksanaan != null && !waktuPelaksanaan.isEmpty()) {
            formBuilder.add("waktu_pelaksanaan", waktuPelaksanaan);
        }

        // Tambahkan contact person dan instagram
        if (whatsapp != null && !whatsapp.isEmpty()) {
            formBuilder.add("contact_person", whatsapp);
        }

        if (instagram != null && !instagram.isEmpty()) {
            formBuilder.add("url_instagram", instagram);
        }

        RequestBody formBody = formBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "Android-App")
                .addHeader("Authorization", "Bearer " + getSavedToken())
                .build();

        Log.d(TAG, "🚀 Sending form data to: " + url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int responseCode = response.code();
                String result = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    callback.onSuccess(result);
                } else {
                    callback.onFailure("Server error: " + responseCode);
                }
            }
        });
    }

    // ==================== LOGIN METHOD ====================
    public static void login(String email, String password, ApiCallback callback) {
        new Thread(() -> {
            try {
                RequestBody formBody = new FormBody.Builder()
                        .add("email", email)
                        .add("password", password)
                        .build();

                Request request = new Request.Builder()
                        .url(BASE_URL + "/api/login")
                        .post(formBody)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .addHeader("Accept", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                int responseCode = response.code();
                String result = response.body() != null ? response.body().string() : "";

                if (responseCode == 200) {
                    try {
                        JSONObject jsonResponse = new JSONObject(result);
                        String status = jsonResponse.optString("status", "");

                        if ("success".equals(status)) {
                            JSONObject userObj = jsonResponse.optJSONObject("user");
                            String userId = "";
                            String userEmail = email;
                            String userName = "";

                            if (userObj != null) {
                                userId = userObj.optString("id", "");
                                userEmail = userObj.optString("email", email);
                                userName = userObj.optString("name", "");
                            }

                            // Simpan data user
                            if (prefs != null) {
                                prefs.edit()
                                        .putString(KEY_EMAIL, userEmail)
                                        .putString(KEY_USER_ID, userId)
                                        .putString(KEY_USER_NAME, userName)
                                        .apply();
                            }

                            callback.onSuccess(result);
                        } else {
                            String message = jsonResponse.optString("message", "Login failed");
                            callback.onFailure(message);
                        }
                    } catch (Exception e) {
                        callback.onFailure("Invalid response format");
                    }
                } else if (responseCode == 401) {
                    callback.onFailure("Email atau password salah");
                } else {
                    callback.onFailure("Server error: " + responseCode);
                }

            } catch (Exception e) {
                callback.onFailure("Network error: " + e.getMessage());
            }
        }).start();
    }

    // ==================== SAVE USER DATA ====================
    public static void saveUserData(String userId, String userName, String email) {
        if (prefs != null) {
            prefs.edit()
                    .putString(KEY_USER_ID, userId)
                    .putString(KEY_USER_NAME, userName)
                    .putString(KEY_EMAIL, email)
                    .apply();
            Log.d(TAG, "💾 User data saved - ID: " + userId + ", Name: " + userName);
        }
    }

    // ==================== GETTER METHODS ====================
    public static String getSavedEmail() {
        return prefs != null ? prefs.getString(KEY_EMAIL, "") : "";
    }

    public static String getSavedUserId() {
        return prefs != null ? prefs.getString(KEY_USER_ID, "") : "";
    }

    public static String getSavedUserName() {
        return prefs != null ? prefs.getString(KEY_USER_NAME, "") : "";
    }

    public static String getSavedToken() {
        return prefs != null ? prefs.getString("token", "") : "";
    }

    public static boolean hasUserData() {
        return prefs != null && !getSavedUserId().isEmpty();
    }

    // ==================== EXISTING METHODS ====================
    public static void executeApiCall(String endpoint, String method, String jsonData,
                                      String apiName, ApiCallback callback) {
        if (!isInitialized) {
            callback.onFailure("ApiHelper not initialized");
            return;
        }

        String url = BASE_URL + endpoint;

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "Android-App")
                .addHeader("Authorization", "Bearer " + getSavedToken());

        if ("POST".equalsIgnoreCase(method) && jsonData != null) {
            requestBuilder.addHeader("Content-Type", "application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonData, JSON);
            requestBuilder.post(body);
        } else if ("POST".equalsIgnoreCase(method)) {
            requestBuilder.post(RequestBody.create("", null));
        } else {
            requestBuilder.get();
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    callback.onSuccess(result);
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    public static void fetchTeams(ApiCallback callback) {
        executeApiCall("/api/teams", "GET", null, "teams", callback);
    }

    public static void fetchTeamsByStatus(String status, ApiCallback callback) {
        executeApiCall("/api/teams?status=" + status, "GET", null, "teams_by_status", callback);
    }

    public static void fetchActiveTeams(ApiCallback callback) {
        fetchTeamsByStatus("active", callback);
    }


    public static void fetchEvent(ApiCallback callback) {
        executeApiCall("/api/events", "GET", null, "events", callback);
    }

    public static void loginWithUsername(String username, String password, ApiCallback callback) {
        login(username, password, callback);
    }

    public static String getSavedUsername() {
        return getSavedEmail();
    }

    // ==================== CLEAR USER DATA ====================
    public static void clearUserData() {
        if (prefs != null) {
            prefs.edit()
                    .remove(KEY_EMAIL)
                    .remove(KEY_USER_ID)
                    .remove(KEY_USER_NAME)
                    .remove("token")
                    .apply();
            Log.d(TAG, "🗑 User data cleared");
        }
    }

    // ==================== DEBUG INFO ====================
    public static String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== API HELPER DEBUG INFO ===\n");
        sb.append("Initialized: ").append(isInitialized).append("\n");
        sb.append("Base URL: ").append(BASE_URL).append("\n");
        sb.append("User ID: ").append(getSavedUserId()).append("\n");
        sb.append("User Name: ").append(getSavedUserName()).append("\n");
        sb.append("Email: ").append(getSavedEmail()).append("\n");
        sb.append("Has User Data: ").append(hasUserData()).append("\n");
        return sb.toString();
    }

    // ==================== SIMPLE AUTO LOGIN ====================
    public static void autoLogin(ApiCallback callback) {
        if (!isInitialized || prefs == null) {
            callback.onFailure("ApiHelper not initialized");
            return;
        }

        String email = getSavedEmail();
        if (email.isEmpty()) {
            callback.onFailure("No saved email");
            return;
        }

        if (hasUserData()) {
            callback.onSuccess("{\"status\":\"success\",\"message\":\"Already logged in\"}");
        } else {
            callback.onFailure("Not logged in");
        }
    }

    // ==================== SIMPLE SESSION CHECK ====================
    public static boolean isLoggedIn() {
        return hasUserData();
    }

    // ==================== CREATE KOMPETISI (LOMBA) ====================
    public static void createKompetisiWithImage(
            String namaLomba, String deskripsi, String tanggalLomba,
            String lokasi, String penyelenggara, String kategori,
            String hadiah, String lombaType, String scope,
            String biaya, String harga, File imageFile,
            String linkPendaftaran, String linkPanduan,
            String whatsapp, String instagram, String keterangan,
            ApiCallback callback) {

        Log.d(TAG, "🎯 CREATE KOMPETISI WITH IMAGE");

        if (!isInitialized) {
            callback.onFailure("ApiHelper not initialized");
            return;
        }

        // Gunakan endpoint sesuai route: /api/kompetisi/create-lomba
        String url = BASE_URL + "/api/kompetisi/create-lomba";

        try {
            boolean hasImage = (imageFile != null && imageFile.exists());

            if (hasImage) {
                // Kirim sebagai Multipart (dengan gambar)
                sendKompetisiMultipartRequest(
                        namaLomba, deskripsi, tanggalLomba,
                        lokasi, penyelenggara, kategori,
                        hadiah, lombaType, scope, biaya,
                        harga, imageFile, linkPendaftaran,
                        linkPanduan, whatsapp, instagram,
                        keterangan, url, callback
                );
            } else {
                // Kirim sebagai JSON (tanpa gambar)
                sendKompetisiJsonRequest(
                        namaLomba, deskripsi, tanggalLomba,
                        lokasi, penyelenggara, kategori,
                        hadiah, lombaType, scope, biaya,
                        harga, linkPendaftaran, linkPanduan,
                        whatsapp, instagram, keterangan,
                        url, callback
                );
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error preparing request: " + e.getMessage());
            callback.onFailure("Error: " + e.getMessage());
        }
    }

    // ==================== FETCH KOMPETISI DATA ====================
    public static void fetchKompetisi(ApiCallback callback) {
        executeApiCall("/api/kompetisi", "GET", null, "kompetisi", callback);
    }

    public static void fetchKompetisiDetail(String lombaId, ApiCallback callback) {
        executeApiCall("/api/kompetisi/" + lombaId, "GET", null, "kompetisi_detail", callback);
    }

    // ==================== SEND KOMPETISI MULTIPART REQUEST ====================
    private static void sendKompetisiMultipartRequest(
            String namaLomba, String deskripsi, String tanggalLomba,
            String lokasi, String penyelenggara, String kategori,
            String hadiah, String lombaType, String scope,
            String biaya, String harga, File imageFile,
            String linkPendaftaran, String linkPanduan,
            String whatsapp, String instagram, String keterangan,
            String url, ApiCallback callback) {

        try {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);

            // Tambahkan semua field untuk kompetisi
            builder.addFormDataPart("nama_lomba", namaLomba);
            builder.addFormDataPart("deskripsi", deskripsi);
            builder.addFormDataPart("tanggal_lomba", tanggalLomba);
            builder.addFormDataPart("lokasi", lokasi);
            builder.addFormDataPart("penyelenggara", penyelenggara);
            builder.addFormDataPart("kategori", kategori);
            builder.addFormDataPart("hadiah", hadiah);
            builder.addFormDataPart("lomba_type", lombaType);
            builder.addFormDataPart("scope", scope);
            builder.addFormDataPart("biaya", biaya);
            builder.addFormDataPart("link_pendaftaran", linkPendaftaran);
            builder.addFormDataPart("link_panduan", linkPanduan);
            builder.addFormDataPart("keterangan", keterangan);

            // User ID (WAJIB) - gunakan getSavedUserId()
            String userId = getSavedUserId();
            if (userId != null && !userId.isEmpty()) {
                builder.addFormDataPart("user_id", userId);
                Log.d(TAG, "👤 User ID untuk kompetisi: " + userId);
            } else {
                callback.onFailure("User ID diperlukan. Silakan login terlebih dahulu.");
                return;
            }

            // Harga sebagai integer (default 0)
            int hargaInt = 0;
            if (harga != null && !harga.isEmpty()) {
                try {
                    // Hapus titik atau koma
                    String cleanHarga = harga.replace(".", "").replace(",", "");
                    hargaInt = Integer.parseInt(cleanHarga);
                    Log.d(TAG, "💰 Harga setelah parsing: " + hargaInt);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "⚠ Harga tidak valid, menggunakan default 0");
                    hargaInt = 0;
                }
            }
            builder.addFormDataPart("harga", String.valueOf(hargaInt));

            // Contact fields
            if (whatsapp != null && !whatsapp.isEmpty()) {
                builder.addFormDataPart("contact_person", whatsapp);
            }

            if (instagram != null && !instagram.isEmpty()) {
                builder.addFormDataPart("instagram", instagram);
            }

            // Tambahkan file gambar poster
            if (imageFile != null && imageFile.exists()) {
                MediaType mediaType = MediaType.parse("image/jpeg");
                if (mediaType == null) {
                    mediaType = MediaType.parse("image/*");
                }

                String fileName = "poster_lomba_" + System.currentTimeMillis() + ".jpg";
                RequestBody fileBody = RequestBody.create(mediaType, imageFile);
                builder.addFormDataPart("poster_lomba", fileName, fileBody);

                Log.d(TAG, "📸 Adding competition poster: " + fileName);
            } else {
                builder.addFormDataPart("poster_lomba", "");
                Log.w(TAG, "⚠ No image file provided");
            }

            RequestBody requestBody = builder.build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "Android-App")
                    .build();

            Log.d(TAG, "🚀 Sending kompetisi multipart request to: " + url);
            Log.d(TAG, "📤 Request body size: " + requestBody.contentLength() + " bytes");

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "❌ Network error: " + e.getMessage());
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    int responseCode = response.code();
                    String result = response.body() != null ? response.body().string() : "";

                    Log.d(TAG, "📡 Response code: " + responseCode);
                    Log.d(TAG, "📡 Response body: " + result);

                    if (responseCode == 201 || responseCode == 200) {
                        callback.onSuccess(result);
                    } else {
                        String errorMsg = "Server error: " + responseCode;
                        try {
                            JSONObject errorJson = new JSONObject(result);
                            errorMsg = errorJson.optString("message", errorMsg);
                        } catch (JSONException e) {
                            // Jika bukan JSON, gunakan response body
                            if (!result.isEmpty()) {
                                errorMsg = result;
                            }
                        }
                        callback.onFailure(errorMsg);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error in kompetisi multipart request: " + e.getMessage());
            callback.onFailure("Error: " + e.getMessage());
        }
    }

    // ==================== SEND KOMPETISI JSON REQUEST ====================
    private static void sendKompetisiJsonRequest(
            String namaLomba, String deskripsi, String tanggalLomba,
            String lokasi, String penyelenggara, String kategori,
            String hadiah, String lombaType, String scope,
            String biaya, String harga, String linkPendaftaran,
            String linkPanduan, String whatsapp, String instagram,
            String keterangan, String url, ApiCallback callback) {

        try {
            JSONObject jsonData = new JSONObject();

            // Field yang diperlukan
            jsonData.put("nama_lomba", namaLomba);
            jsonData.put("deskripsi", deskripsi);
            jsonData.put("tanggal_lomba", tanggalLomba);
            jsonData.put("lokasi", lokasi);
            jsonData.put("penyelenggara", penyelenggara);
            jsonData.put("kategori", kategori);
            jsonData.put("hadiah", hadiah);
            jsonData.put("lomba_type", lombaType);
            jsonData.put("scope", scope);
            jsonData.put("biaya", biaya);
            jsonData.put("link_pendaftaran", linkPendaftaran);
            jsonData.put("link_panduan", linkPanduan);
            jsonData.put("keterangan", keterangan);

            // Harga sebagai integer (default 0)
            int hargaInt = 0;
            if (harga != null && !harga.isEmpty()) {
                try {
                    String cleanHarga = harga.replace(".", "").replace(",", "");
                    hargaInt = Integer.parseInt(cleanHarga);
                } catch (NumberFormatException e) {
                    hargaInt = 0;
                }
            }
            jsonData.put("harga", hargaInt);

            // User ID (WAJIB)
            String userId = getSavedUserId();
            if (userId != null && !userId.isEmpty()) {
                jsonData.put("user_id", userId);
            } else {
                callback.onFailure("User ID diperlukan. Silakan login terlebih dahulu.");
                return;
            }

            // Contact fields
            if (whatsapp != null && !whatsapp.isEmpty()) {
                jsonData.put("contact_person", whatsapp);
            }

            if (instagram != null && !instagram.isEmpty()) {
                jsonData.put("instagram", instagram);
            }

            jsonData.put("poster_lomba", ""); // Kosong karena tidak ada gambar

            Log.d(TAG, "📦 Kompetisi JSON Data: " + jsonData.toString(2));

            RequestBody body = RequestBody.create(jsonData.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "Android-App")
                    .build();

            Log.d(TAG, "🚀 Sending Kompetisi JSON request to: " + url);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleResponse(response, callback);
                }
            });

        } catch (Exception e) {
            callback.onFailure("Error: " + e.getMessage());
        }
    }
}