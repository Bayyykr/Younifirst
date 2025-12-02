package com.naufal.younifirst.api;

import android.util.Log;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiHelper {
    private static final String BASE_URL = "http://192.168.1.12:8000";
    private static final String TAG = "API_DEBUG";
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    public interface ApiCallback {
        void onSuccess(String result);
        void onFailure(String error);
    }

    // Method untuk mengambil teams dari endpoint API Anda
    public static void fetchTeams(ApiCallback callback) {
        executeApiCall("/api/teams", "teams", callback);
    }

    // Method baru: fetch teams dengan status tertentu
    public static void fetchTeamsByStatus(String status, ApiCallback callback) {
        String url = BASE_URL + "/api/teams?status=" + status;
        executeApiCallWithUrl(url, "teams_by_status", callback);
    }

    // Method baru: fetch teams aktif (default)
    public static void fetchActiveTeams(ApiCallback callback) {
        fetchTeamsByStatus("active", callback);
    }

    public static void fetchKompetisi(ApiCallback callback) {
        executeApiCall("/api/kompetisi", "kompetisi", callback);
    }

    public static void fetchEvent(ApiCallback callback) {
        executeApiCall("/api/events", "events", callback);
    }

    private static void executeApiCall(String endpoint, String apiName, ApiCallback callback) {
        String url = BASE_URL + endpoint;
        executeApiCallWithUrl(url, apiName, callback);
    }

    private static void executeApiCallWithUrl(String url, String apiName, ApiCallback callback) {
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

                    // Debug: print first 500 characters of response
                    if (result.length() > 500) {
                        Log.d(TAG, "📋 Response preview: " + result.substring(0, 500) + "...");
                    } else {
                        Log.d(TAG, "📋 Response: " + result);
                    }

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
}