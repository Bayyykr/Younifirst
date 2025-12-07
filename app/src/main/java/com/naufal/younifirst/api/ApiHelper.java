    package com.naufal.younifirst.api;

    import android.content.Context;
    import android.content.SharedPreferences;
    import android.util.Base64;
    import android.util.Log;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.io.File;
    import java.io.FileInputStream;
    import java.io.IOException;
    import java.io.UnsupportedEncodingException;
    import java.net.URLEncoder;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.Iterator;
    import java.util.List;
    import java.util.Locale;
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
        private static final String BASE_URL = "http://192.168.0.104:8000";
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

        // ==================== LOST & FOUND ENDPOINTS ====================

        // Get all Lost & Found items
        public static void fetchLostFound(ApiCallback callback) {
            String url = BASE_URL + "/api/lostfound";
            Log.d(TAG, "🌐 Fetching Lost & Found from: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "Android-App")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "❌ Failed to fetch lostfound: " + e.getMessage());
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    int statusCode = response.code();
                    String result = response.body() != null ? response.body().string() : "";

                    Log.d(TAG, "📡 LostFound Response code: " + statusCode);
                    Log.d(TAG, "📡 LostFound Response: " + result.substring(0, Math.min(200, result.length())));

                    if (response.isSuccessful()) {
                        callback.onSuccess(result);
                    } else {
                        callback.onFailure("Server error: " + statusCode + " - " + response.message());
                    }
                }
            });
        }

        // ==================== CREATE LOST & FOUND ITEM ====================
        public static void createLostFound(String description, String location,
                                           String kategori, File imageFile,
                                           String phone, String email,
                                           ApiCallback callback, String status) {

            Log.d(TAG, "🎯 CREATE LOST & FOUND ITEM");

            if (!isInitialized) {
                callback.onFailure("ApiHelper not initialized");
                return;
            }

            String url = BASE_URL + "/api/lostfound/create";

            try {
                // Buat JSON object
                JSONObject jsonData = new JSONObject();

                // User ID (WAJIB)
                String userId = getSavedUserId();
                if (userId == null || userId.isEmpty()) {
                    callback.onFailure("User ID diperlukan. Silakan login terlebih dahulu.");
                    return;
                }
                jsonData.put("user_id", userId);

                // Field wajib
                jsonData.put("deskripsi", description);
                jsonData.put("lokasi", location);
                jsonData.put("kategori", kategori);
                jsonData.put("status", status);

                // Optional fields
                if (phone != null && !phone.isEmpty()) {
                    jsonData.put("no_hp", phone);
                } else {
                    jsonData.put("no_hp", "");
                }

                if (email != null && !email.isEmpty()) {
                    jsonData.put("email", email);
                } else {
                    jsonData.put("email", "");
                }

                // Tanggal
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String tanggal = sdf.format(new Date());
                jsonData.put("tanggal", tanggal);

                // Handle image file (encode to base64 if exists)
                if (imageFile != null && imageFile.exists()) {
                    // Konversi file ke base64
                    String base64Image = encodeFileToBase64(imageFile);
                    if (base64Image != null) {
                        jsonData.put("foto_barang", base64Image);
                        Log.d(TAG, "📸 Image encoded to base64, size: " + base64Image.length());
                    } else {
                        jsonData.put("foto_barang", "");
                        Log.w(TAG, "⚠ Failed to encode image to base64");
                    }
                } else {
                    jsonData.put("foto_barang", "");
                    Log.d(TAG, "📸 No image file provided");
                }

                // Log JSON data sebelum dikirim
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
                            try {
                                JSONObject jsonResponse = new JSONObject(result);
                                boolean success = jsonResponse.optBoolean("success", false);

                                if (success) {
                                    callback.onSuccess(result);
                                } else {
                                    String message = jsonResponse.optString("message", "Unknown error");
                                    callback.onFailure(message);
                                }
                            } catch (JSONException e) {
                                callback.onFailure("Invalid response format");
                            }
                        } else if (responseCode == 400) {
                            try {
                                JSONObject errorJson = new JSONObject(result);
                                String errorMsg = errorJson.optString("message", "Bad Request");

                                // Log detail error jika ada
                                if (errorJson.has("errors")) {
                                    String errors = errorJson.optString("errors", "");
                                    Log.e(TAG, "❌ Validation errors: " + errors);
                                }

                                callback.onFailure(errorMsg);
                            } catch (JSONException e) {
                                callback.onFailure("Bad Request: " + result);
                            }
                        } else {
                            callback.onFailure("Server error: " + responseCode);
                        }
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "❌ JSON creation error: " + e.getMessage());
                callback.onFailure("Error creating JSON: " + e.getMessage());
            }
        }

        // ==================== HELPER METHOD: ENCODE FILE TO BASE64 ====================
        private static String encodeFileToBase64(File file) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bytes = new byte[(int) file.length()];
                fileInputStream.read(bytes);
                fileInputStream.close();

                // Encode ke base64 string
                String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);

                // Bersihkan newline characters
                base64 = base64.replace("\n", "").replace("\r", "");

                return base64;
            } catch (IOException e) {
                Log.e(TAG, "❌ Error encoding file to base64: " + e.getMessage());
                return null;
            }
        }

        public static void fetchPostinganByKategori(String kategori, ApiCallback callback) {
            executeApiCall("/api/postingan/kategori/" + kategori, "GET", null, "postingan_kategori", callback);
        }

        public static void fetchAllPostingan(ApiCallback callback) {
            executeApiCall("/api/postingan", "GET", null, "all_postingan", callback);
        }

        // ==================== CREATE POSTINGAN ====================
        public static void createPostingan(String judul, String deskripsi, String kategori,
                                           String gambarUrl, ApiCallback callback) {

            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("judul", judul);
                jsonData.put("deskripsi", deskripsi);
                jsonData.put("kategori", kategori);
                jsonData.put("gambar", gambarUrl != null ? gambarUrl : "");
                jsonData.put("user_id", getSavedUserId());

                executeApiCall("/api/postingan/create", "POST", jsonData.toString(), "create_postingan", callback);

            } catch (JSONException e) {
                callback.onFailure("JSON Error: " + e.getMessage());
            }
        }

        public static void fetchPostinganSimple(String kategori, ApiCallback callback) {
            if (!isInitialized) {
                callback.onFailure("ApiHelper not initialized");
                return;
            }

            // Gunakan endpoint lostfound yang sudah ada di router
            String endpoint = "/api/lostfound";

            Log.d(TAG, "📥 Fetching postingan from: " + endpoint);

            Request request = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .get()
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "Android-App")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "❌ Network error: " + e.getMessage());
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    int responseCode = response.code();

                    Log.d(TAG, "📡 Response Code: " + responseCode);
                    Log.d(TAG, "📄 Response (first 500 chars): " +
                            responseBody.substring(0, Math.min(500, responseBody.length())));

                    if (responseCode == 200) {
                        callback.onSuccess(responseBody);
                    } else {
                        callback.onFailure("Server error: " + responseCode);
                    }
                }
            });
        }

        /**
         * Try multiple endpoints with fallback
         */
        private static void tryEndpointWithFallback(String[] endpoints, String jsonData, int index, ApiCallback callback) {
            if (index >= endpoints.length) {
                Log.e(TAG, "❌ All endpoints failed");
                callback.onFailure("Failed to create team. All endpoints returned error.");
                return;
            }

            String endpoint = endpoints[index];
            String url = BASE_URL + endpoint;

            Log.d(TAG, "🔄 Trying endpoint " + (index + 1) + "/" + endpoints.length + ": " + url);

            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                Log.d(TAG, "📦 JSON Data for endpoint " + endpoint + ":");
                Log.d(TAG, jsonObject.toString(2));

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
                Log.d(TAG, "📤 Full URL: " + request.url());

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "❌ Network error for " + url + ": " + e.getMessage());
                        // Coba endpoint berikutnya
                        if (index + 1 < endpoints.length) {
                            Log.w(TAG, "⚠ Trying next endpoint...");
                            tryEndpointWithFallback(endpoints, jsonData, index + 1, callback);
                        } else {
                            callback.onFailure("Network error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        int responseCode = response.code();
                        String result = response.body() != null ? response.body().string() : "";

                        Log.d(TAG, "📡 Response from " + url);
                        Log.d(TAG, "📡 Response code: " + responseCode);
                        Log.d(TAG, "📡 Response headers: " + response.headers());

                        // TANGANI 302 REDIRECT
                        if (responseCode == 302 || responseCode == 301) {
                            String location = response.header("Location", "");
                            Log.e(TAG, "⚠ REDIRECT DETECTED to: " + location);

                            if (location.contains("login") || location.contains("Login")) {
                                callback.onFailure("REDIRECT_TO_LOGIN: Endpoint requires session authentication. Please login first.");
                            } else if (location.contains("csrf") || location.contains("token")) {
                                callback.onFailure("REDIRECT_CSRF: CSRF token missing or invalid.");
                            } else {
                                callback.onFailure("REDIRECT_UNKNOWN: Server redirected to: " + location);
                            }
                            return;
                        }

                        if (responseCode == 201 || responseCode == 200) {
                            try {
                                JSONObject jsonResponse = new JSONObject(result);
                                boolean success = jsonResponse.optBoolean("success", false);

                                if (success) {
                                    Log.d(TAG, "✅ Team created successfully via endpoint: " + endpoint);
                                    callback.onSuccess(result);
                                } else {
                                    String message = jsonResponse.optString("message", "Unknown error");
                                    Log.e(TAG, "❌ API returned error: " + message);
                                    callback.onFailure("API Error: " + message);
                                }
                            } catch (JSONException e) {
                                Log.w(TAG, "⚠ Response is not JSON, but status is OK: " + result);
                                callback.onSuccess(result);
                            }
                        } else if (responseCode == 400) {
                            String errorMsg = "Bad Request (400)";
                            try {
                                JSONObject errorJson = new JSONObject(result);
                                errorMsg = errorJson.optString("message", errorMsg);

                                if (errorJson.has("errors")) {
                                    String errors = errorJson.optString("errors", "");
                                    Log.e(TAG, "❌ Validation errors: " + errors);
                                }
                            } catch (JSONException e) {
                                // ignore
                            }
                            callback.onFailure(errorMsg);
                        } else if (responseCode == 401 || responseCode == 403) {
                            callback.onFailure("Authentication required. Please login again.");
                        } else if (responseCode == 405) {
                            Log.w(TAG, "⚠ Method not allowed (405) for endpoint: " + endpoint);
                            // Coba endpoint berikutnya
                            if (index + 1 < endpoints.length) {
                                tryEndpointWithFallback(endpoints, jsonData, index + 1, callback);
                            } else {
                                callback.onFailure("Method not allowed. Server expects POST method.");
                            }
                        } else if (responseCode >= 500) {
                            callback.onFailure("Server error: " + responseCode);
                        } else {
                            Log.e(TAG, "❌ Unexpected response: " + responseCode);
                            callback.onFailure("Unexpected response: " + responseCode);
                        }
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "❌ JSON parsing error: " + e.getMessage());
                callback.onFailure("JSON_PARSING_ERROR: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "❌ Unexpected error: " + e.getMessage());
                callback.onFailure("Unexpected error: " + e.getMessage());
            }
        }
        public static String getBaseUrl() {
            return BASE_URL;
        }
        public static void createTeamForPhpApi(JSONObject teamData, ApiCallback callback) {
            try {
                // Convert semua value menjadi String agar JSON aman untuk PHP
                JSONObject safeJson = new JSONObject();
                Iterator<String> keys = teamData.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    safeJson.put(key, String.valueOf(teamData.get(key)));
                }

                String payload = safeJson.toString();
                Log.d(TAG, "📦 Final JSON SEND: " + payload);

                // Endpoint pasti
                String url = BASE_URL + "/api/teams/create";

                RequestBody body = RequestBody.create(
                        payload,
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("Content-Type", "application/json") // FIX
                        .addHeader("Accept", "application/json")
                        .build();

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
                            callback.onFailure("Server error " + response.code() + ": " + result);
                        }
                    }
                });

            } catch (Exception e) {
                callback.onFailure("Error: " + e.getMessage());
            }
        }


        private static void tryJsonEndpoints(String[] endpoints, String jsonData, int index, ApiCallback callback) {
            if (index >= endpoints.length) {
                Log.e(TAG, "❌ All JSON endpoints failed");
                callback.onFailure("All endpoints failed. Please check server configuration.");
                return;
            }

            String endpoint = endpoints[index];
            String url = BASE_URL + endpoint;

            Log.d(TAG, "🔄 Trying JSON endpoint " + (index + 1) + "/" + endpoints.length + ": " + url);

            try {
                // Validasi JSON
                JSONObject jsonObject = new JSONObject(jsonData);

                // Buat RequestBody dengan JSON
                RequestBody body = RequestBody.create(jsonData, JSON);

                // Buat request dengan headers untuk JSON
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .addHeader("Accept", "application/json")
                        .addHeader("User-Agent", "Android-App")
                        .addHeader("X-Requested-With", "XMLHttpRequest")
//                        .addHeader("Cookie", sessionCookie) // Jika butuh session
                        .build();

                Log.d(TAG, "🚀 Sending JSON POST to: " + url);
                Log.d(TAG, "📦 JSON Body size: " + jsonData.length() + " chars");
                Log.d(TAG, "📤 Headers: Content-Type=application/json");

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "❌ Network error for " + url + ": " + e.getMessage());

                        // Coba endpoint berikutnya
                        if (index + 1 < endpoints.length) {
                            Log.w(TAG, "⚠ Trying next endpoint...");
                            tryJsonEndpoints(endpoints, jsonData, index + 1, callback);
                        } else {
                            callback.onFailure("Network error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        int responseCode = response.code();
                        String result = response.body() != null ? response.body().string() : "";

                        Log.d(TAG, "📡 Response from " + url);
                        Log.d(TAG, "📡 Response code: " + responseCode);

                        // Log headers untuk debugging
                        Log.d(TAG, "📥 Response headers:");
                        for (String name : response.headers().names()) {
                            Log.d(TAG, "  " + name + ": " + response.header(name));
                        }

                        // Log body (first 1000 chars)
                        if (result.length() > 1000) {
                            Log.d(TAG, "📡 Response body (first 1000 chars): " + result.substring(0, 1000) + "...");
                        } else {
                            Log.d(TAG, "📡 Response body: " + result);
                        }

                        // Tangani redirect
                        if (responseCode == 302 || responseCode == 301) {
                            String location = response.header("Location", "");
                            Log.e(TAG, "⚠ Redirected to: " + location);

                            if (location.contains("login")) {
                                callback.onFailure("SESSION_EXPIRED: Please login first");
                            } else {
                                callback.onFailure("REDIRECT: Server redirected to " + location);
                            }
                            return;
                        }

                        if (responseCode == 200 || responseCode == 201) {
                            // Success
                            callback.onSuccess(result);
                        } else if (responseCode == 400) {
                            // Bad Request - biasanya karena validasi gagal
                            String errorMsg = "Bad Request (400)";
                            try {
                                JSONObject errorJson = new JSONObject(result);
                                errorMsg = errorJson.optString("message", errorMsg);

                                // Tampilkan detail error jika ada
                                if (errorJson.has("errors")) {
                                    JSONObject errors = errorJson.getJSONObject("errors");
                                    errorMsg += "\nValidation errors:\n";
                                    java.util.Iterator<String> errorKeys = errors.keys();
                                    while (errorKeys.hasNext()) {
                                        String key = errorKeys.next();
                                        errorMsg += "- " + key + ": " + errors.getString(key) + "\n";
                                    }
                                }
                            } catch (JSONException e) {
                                // Jika bukan JSON
                            }
                            callback.onFailure(errorMsg);
                        } else if (responseCode == 404) {
                            // Endpoint tidak ditemukan, coba yang lain
                            if (index + 1 < endpoints.length) {
                                Log.w(TAG, "⚠ Endpoint not found, trying next...");
                                tryJsonEndpoints(endpoints, jsonData, index + 1, callback);
                            } else {
                                callback.onFailure("Endpoint not found (404): " + url);
                            }
                        } else if (responseCode == 415) {
                            // Unsupported Media Type - coba ganti Content-Type
                            Log.w(TAG, "⚠ Unsupported Media Type (415), trying different format...");
                            tryDifferentFormat(endpoints, jsonData, index, callback);
                        } else {
                            callback.onFailure("Server error " + responseCode + ": " + result);
                        }
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "❌ JSON error: " + e.getMessage());
                callback.onFailure("Invalid JSON data: " + e.getMessage());
            }
        }

        private static void tryDifferentFormat(String[] endpoints, String jsonData, int index, ApiCallback callback) {
            // Coba format berbeda jika 415 Unsupported Media Type
            String endpoint = endpoints[index];
            String url = BASE_URL + endpoint;

            Log.d(TAG, "🔄 Trying different format for: " + url);

            try {
                // Coba dengan Content-Type yang berbeda
                RequestBody body = RequestBody.create(jsonData, JSON);

                // Coba dengan Content-Type yang berbeda
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("Content-Type", "application/json") // Tanpa charset
                        .addHeader("Accept", "application/json")
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.onFailure("Network error: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            callback.onSuccess(response.body().string());
                        } else {
                            // Masih gagal, coba endpoint lain
                            if (index + 1 < endpoints.length) {
                                tryJsonEndpoints(endpoints, jsonData, index + 1, callback);
                            } else {
                                callback.onFailure("Format error: " + response.code());
                            }
                        }
                    }
                });

            } catch (Exception e) {
                callback.onFailure("Error: " + e.getMessage());
            }
        }
        // Tambahkan method ini di ApiHelper.java
        // Perbaiki method fetchTeamById di ApiHelper.java
        public static void fetchTeamById(String teamId, ApiCallback callback) {
            if (client == null) {
                callback.onFailure("ApiHelper not initialized");
                return;
            }

            if (teamId == null || teamId.isEmpty()) {
                callback.onFailure("ID tim tidak valid");
                return;
            }

            // Encode ID untuk URL (penting jika ID mengandung karakter khusus)
            String encodedId = "";
            try {
                encodedId = URLEncoder.encode(teamId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                encodedId = teamId;
            }

            String url = BASE_URL + "/api/teams/" + encodedId;
            Log.d(TAG, "🔍 Fetching from: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "Android-App")
                    .build();

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
                    Log.d(TAG, "📡 Response headers: ");
                    for (String name : response.headers().names()) {
                        Log.d(TAG, "  " + name + ": " + response.header(name));
                    }

                    // Log body untuk debugging
                    if (result.length() > 500) {
                        Log.d(TAG, "📡 Response (first 500 chars): " + result.substring(0, 500));
                    } else {
                        Log.d(TAG, "📡 Response: " + result);
                    }

                    if (responseCode == 200) {
                        callback.onSuccess(result);
                    } else if (responseCode == 404) {
                        // Debug lebih detail untuk 404
                        try {
                            JSONObject errorJson = new JSONObject(result);
                            String message = errorJson.optString("message", "Team not found");
                            Log.e(TAG, "❌ Team not found: " + message);
                            callback.onFailure("Team tidak ditemukan: " + message);
                        } catch (JSONException e) {
                            callback.onFailure("Tim tidak ditemukan (ID: " + teamId + ")");
                        }
                    } else {
                        callback.onFailure("Server error: " + responseCode);
                    }
                }
            });
        }
    }