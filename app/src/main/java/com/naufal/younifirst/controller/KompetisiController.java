package com.naufal.younifirst.controller;

import android.util.Log;

import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.model.Kompetisi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KompetisiController {

    private static final String TAG = "KompetisiController";

    private static final String BASE_URL = "http://192.168.1.12:8000";

    public interface KompetisiCallback {
        void onSuccess(List<Kompetisi> competitions);
        void onFailure(String error);
    }

    public void loadKompetisiData(KompetisiCallback callback) {
        ApiHelper.fetchKompetisi(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject response = new JSONObject(result);
                    JSONArray competitionsArray = response.getJSONArray("competitions");
                    List<Kompetisi> confirmedCompetitions = new ArrayList<>();

                    for (int i = 0; i < competitionsArray.length(); i++) {
                        JSONObject compJson = competitionsArray.getJSONObject(i);

                        // DEBUG: Print semua keys yang ada
                        Log.d(TAG, "All keys in object:");
                        Iterator<String> keys = compJson.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            Log.d(TAG, "Key: " + key + " = " + compJson.optString(key, "NULL"));
                        }

                        // CEK SEMUA KEMUNGKINAN NAMA FIELD POSTER
                        String posterPath = null;

                        // Coba ambil dari berbagai kemungkinan field name
                        if (compJson.has("poster_lomba")) {
                            posterPath = compJson.getString("poster_lomba");
                            Log.d(TAG, "Found poster_lomba: " + posterPath);
                        }
                        else if (compJson.has("poster")) {
                            posterPath = compJson.getString("poster");
                            Log.d(TAG, "Found poster: " + posterPath);
                        }
                        else if (compJson.has("8")) { // Index dari array
                            posterPath = compJson.getString("8");
                            Log.d(TAG, "Found index 8: " + posterPath);
                        }

                        // Jika ditemukan path, perbaiki URL
                        if (posterPath != null && !posterPath.isEmpty()) {
                            String fullPosterUrl = getFullImageUrl(posterPath);
                            // Simpan ke field yang benar di JSON
                            compJson.put("poster", fullPosterUrl);
                            Log.d(TAG, "Fixed Poster URL: " + fullPosterUrl);
                        } else {
                            Log.d(TAG, "No poster path found!");
                            // Tambahkan field poster kosong jika tidak ada
                            compJson.put("poster", "");
                        }

                        Kompetisi competition = new Kompetisi(compJson);

                        // Filter hanya yang status "confirm"
                        if ("confirm".equalsIgnoreCase(competition.getStatus())) {
                            confirmedCompetitions.add(competition);
                        }
                    }

                    callback.onSuccess(confirmedCompetitions);

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing competition data", e);
                    callback.onFailure("Error parsing competition data: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "API Error: " + error);
                callback.onFailure(error);
            }
        });
    }

    // Method untuk memperbaiki URL
    private String getFullImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }

        // Cek jika sudah full URL
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }

        String baseUrl = "http://192.168.1.12:8000";

        // Hapus slash di awal jika ada
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        // Tambahkan slash antara baseUrl dan path jika perlu
        String result = baseUrl + "/" + relativePath;
        Log.d(TAG, "Converted URL: " + result);
        return result;
    }
}