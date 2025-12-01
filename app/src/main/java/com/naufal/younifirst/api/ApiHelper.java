package com.naufal.younifirst.api;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

public class ApiHelper {

    private static final String BASE_URL = "http://192.168.1.13:8000";
    private static final String TAG = "API_DEBUG";

    // Client dengan konfigurasi yang lebih robust
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(0, TimeUnit.SECONDS)  // Kurangi timeout
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(0, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) // Tambah retry
            .build();

    public interface ApiCallback {
        void onSuccess(String result);
        void onFailure(String error);
    }

    public static void fetchTeams(ApiCallback callback) {
        executeApiCall("/api/teams", "teams", callback);
    }

    public static void fetchKompetisi(ApiCallback callback) {
        executeApiCall("/api/kompetisi", "kompetisi", callback);
    }

    private static void executeApiCall(String endpoint, String apiName, ApiCallback callback) {
        String url = BASE_URL + endpoint;
        Log.d(TAG, "🔗 Fetching " + apiName + " from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "Android-App")
                .build();

        new Thread(() -> {
            try {
                Log.d(TAG, "🚀 Starting API call to " + apiName + "...");
                Response response = client.newCall(request).execute();

                Log.d(TAG, "📡 Response code: " + response.code());
                Log.d(TAG, "📡 Response message: " + response.message());

                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().string();
                    Log.d(TAG, "✅ " + apiName + " API success, data length: " + result.length());
                    callback.onSuccess(result);
                } else {
                    String error = "❌ HTTP Error: " + response.code() + " - " + response.message();
                    Log.e(TAG, error);
                    callback.onFailure(error);
                }
                response.close(); // Penting: close response

            } catch (SocketTimeoutException e) {
                String error = "⏰ Timeout: Server tidak merespons dalam 15 detik";
                Log.e(TAG, error, e);
                callback.onFailure(error);
            } catch (ConnectException e) {
                String error = "🔌 Connection Error: Tidak dapat terhubung ke server. Periksa koneksi jaringan dan IP server.";
                Log.e(TAG, error, e);
                callback.onFailure(error);
            } catch (Exception e) {
                String error = "❌ Network Error: " + e.getMessage();
                Log.e(TAG, error, e);
                callback.onFailure(error);
            }
        }).start();
    }

    // Method untuk test koneksi dengan timeout lebih singkat
    public static void testConnection(ApiCallback callback) {
        String url = BASE_URL + "/";
        Log.d(TAG, "🧪 Testing connection to: " + url);

        // Client khusus untuk test dengan timeout lebih pendek
        OkHttpClient testClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                .build();

        new Thread(() -> {
            try {
                Response response = testClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    callback.onSuccess("✅ Server is reachable! Status: " + response.code());
                } else {
                    callback.onSuccess("⚠️ Server responded but with error: " + response.code());
                }
                response.close();
            } catch (SocketTimeoutException e) {
                callback.onFailure("⏰ Timeout: Server tidak merespons dalam 10 detik");
            } catch (ConnectException e) {
                callback.onFailure("🔌 Connection refused: Pastikan server berjalan dan IP benar");
            } catch (Exception e) {
                callback.onFailure("❌ Cannot reach server: " + e.getMessage());
            }
        }).start();
    }
}