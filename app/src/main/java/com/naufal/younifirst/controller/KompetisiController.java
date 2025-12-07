package com.naufal.younifirst.controller;

import android.util.Log;

import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.model.Kompetisi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class KompetisiController {

    private static final String TAG = "KompetisiController";

    // 🔥 PERBAIKI IP YANG SALAH (titik bukan titik dua)
    private static final String BASE_URL = "http://192.168.1.11:8000";

    public interface KompetisiCallback {
        void onSuccess(List<Kompetisi> competitions);
        void onFailure(String error);
    }

    public void loadKompetisiData(KompetisiCallback callback) {
        ApiHelper.fetchKompetisi(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.d(TAG, "Raw API Response: " + result);

                    List<Kompetisi> confirmedCompetitions = new ArrayList<>();

                    // Coba berbagai format response
                    JSONArray competitionsArray = null;

                    try {
                        JSONObject response = new JSONObject(result);

                        // Kemungkinan 1: Response ada "data" field
                        if (response.has("data")) {
                            competitionsArray = response.getJSONArray("data");
                            Log.d(TAG, "Found 'data' array, size: " + competitionsArray.length());
                        }
                        // Kemungkinan 2: Response ada "competitions" field
                        else if (response.has("competitions")) {
                            competitionsArray = response.getJSONArray("competitions");
                            Log.d(TAG, "Found 'competitions' array, size: " + competitionsArray.length());
                        }
                        // Kemungkinan 3: Response langsung array
                        else if (response.has("kompetisi")) {
                            competitionsArray = response.getJSONArray("kompetisi");
                            Log.d(TAG, "Found 'kompetisi' array, size: " + competitionsArray.length());
                        }
                    } catch (JSONException e1) {
                        // Coba parse langsung sebagai array
                        try {
                            competitionsArray = new JSONArray(result);
                            Log.d(TAG, "Response is direct array, size: " + competitionsArray.length());
                        } catch (JSONException e2) {
                            Log.e(TAG, "Response is not a valid JSON object or array", e2);
                        }
                    }

                    if (competitionsArray != null && competitionsArray.length() > 0) {
                        processCompetitionsArray(competitionsArray, confirmedCompetitions);
                    } else {
                        Log.w(TAG, "No competitions data found in response");
                    }

                    Log.d(TAG, "Total confirmed competitions: " + confirmedCompetitions.size());
                    callback.onSuccess(confirmedCompetitions);

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing JSON", e);
                    callback.onFailure("Error parsing data: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "API Error: " + error);
                callback.onFailure(error);
            }
        });
    }

    private void processCompetitionsArray(JSONArray competitionsArray, List<Kompetisi> confirmedCompetitions) {
        try {
            for (int i = 0; i < competitionsArray.length(); i++) {
                JSONObject compJson = competitionsArray.getJSONObject(i);

                // 🔥 LOG SEMUA DATA UNTUK DEBUG
                logCompetitionData(compJson, i);

                // Pastikan field penting ada
                ensureRequiredFields(compJson);

                try {
                    // Buat objek Kompetisi
                    Kompetisi competition = new Kompetisi(compJson);

                    // Log data setelah parsing
                    Log.d(TAG, "Parsed competition " + i + ":");
                    Log.d(TAG, "  - Nama: " + competition.getNamaLomba());
                    Log.d(TAG, "  - Poster URL: " + competition.getPoster());
                    Log.d(TAG, "  - Status: " + competition.getStatus());

                    // Filter hanya yang status "confirm"
                    if (competition.isConfirmed()) {
                        confirmedCompetitions.add(competition);
                        Log.d(TAG, "  ✅ Ditambahkan ke confirmed competitions");
                    } else {
                        Log.d(TAG, "  ⏭ Lewati - status: " + competition.getStatus());
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error creating Kompetisi object for index " + i, e);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error processing competitions array", e);
        }
    }

    private void logCompetitionData(JSONObject compJson, int index) {
        try {
            Log.d(TAG, "=== Competition Data [" + index + "] ===");
            for (String key : new String[]{
                    "lomba_id",
                    "nama_lomba",
                    "tanggal_lomba",
                    "lokasi",
                    "kategori",
                    "poster",
                    "poster_lomba",
                    "status",
                    "scope",
                    "deskripsi",
                    "hadiah",
                    "lomba_type",
                    "biaya",
                    "penyelenggara",
                    "harga_lomba"
            }) {
                if (compJson.has(key)) {
                    String value = compJson.optString(key, "NULL");
                    Log.d(TAG, key + ": " + value);
                }
            }
            Log.d(TAG, "=== END Competition Data ===");
        } catch (Exception e) {
            Log.e(TAG, "Error logging competition data", e);
        }
    }

    private void ensureRequiredFields(JSONObject compJson) throws JSONException {
        // Tambahkan field jika tidak ada
        if (!compJson.has("penyelenggara")) {
            compJson.put("penyelenggara", "Tidak diketahui");
        }

        if (!compJson.has("harga_lomba")) {
            // Coba ambil dari biaya jika ada
            String biaya = compJson.optString("biaya", "0");
            compJson.put("harga_lomba", biaya);
        }

        if (!compJson.has("scope")) {
            compJson.put("scope", "Nasional");
        }

        if (!compJson.has("lomba_type")) {
            compJson.put("lomba_type", "Individu");
        }
    }

    private void logAllData(JSONObject compJson, int index) {
        try {
            Log.d(TAG, "=== Competition Data [" + index + "] ===");
            for (String key : new String[]{
                    "lomba_id",
                    "nama_lomba",
                    "tanggal_lomba",
                    "lokasi",
                    "kategori",
                    "poster",
                    "poster_lomba",
                    "status",
                    "scope",
                    "deskripsi",
                    "hadiah",
                    "lomba_type",
                    "biaya",
                    "penyelenggara",    // 🔥 CEK INI
                    "harga_lomba"      // 🔥 CEK INI
            }) {
                if (compJson.has(key)) {
                    String value = compJson.optString(key, "NULL");
                    Log.d(TAG, key + ": " + value);
                } else {
                    Log.d(TAG, key + ": NOT FOUND");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error logging data", e);
        }
    }

    private void fixPosterUrl(JSONObject compJson) throws JSONException {
        String posterPath = null;

        // Cari field poster dengan berbagai nama
        if (compJson.has("poster_lomba")) {
            posterPath = compJson.getString("poster_lomba");
        } else if (compJson.has("poster")) {
            posterPath = compJson.getString("poster");
        }

        if (posterPath != null && !posterPath.isEmpty()) {
            String fullPosterUrl = getFullImageUrl(posterPath);
            // Simpan di field "poster" yang dibaca oleh model
            compJson.put("poster", fullPosterUrl);
        } else {
            compJson.put("poster", "");
        }
    }

    private String getFullImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }

        // Cek jika sudah full URL
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }

        String baseUrl = "http://192.168.1.11:8000"; // 🔥 PERBAIKI IP

        // Hapus slash di awal jika ada
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        String result = baseUrl + "/" + relativePath;
        Log.d(TAG, "Converted Poster URL: " + result);
        return result;
    }

    // 🔥 TAMBAHKAN METHOD UNTUK DEBUG API RESPONSE
    public void testApiResponse() {
        ApiHelper.fetchKompetisi(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "=== RAW API RESPONSE ===");
                Log.d(TAG, result);
                Log.d(TAG, "=== END RAW RESPONSE ===");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "API Test Failed: " + error);
            }
        });
    }
}