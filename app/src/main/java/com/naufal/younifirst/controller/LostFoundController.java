package com.naufal.younifirst.controller;

import android.content.Context;
import android.util.Log;

import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.model.LostFound;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LostFoundController {

    private static final String TAG = "LostFoundController";
    private Context context;
    private static final String BASE_URL = "http://10.10.182.83:8000"; // Tambahkan konstanta BASE_URL

    public LostFoundController(Context context) {
        this.context = context;
        ApiHelper.initialize(context);
    }

    public interface LostFoundCallback {
        void onSuccess(List<LostFound> lostFoundList);
        void onFailure(String error);
    }

    // Fetch all Lost & Found items
    public void getAllLostFound(LostFoundCallback callback) {
        Log.d(TAG, "Fetching all lost found items");

        ApiHelper.fetchLostFound(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "API Response received, length: " + result.length());

                try {
                    List<LostFound> lostFoundList = new ArrayList<>();

                    // Parse response
                    JSONObject jsonResponse = new JSONObject(result);

                    // Check for success field
                    if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                        JSONArray data = jsonResponse.getJSONArray("data");
                        lostFoundList = parseLostFoundData(data);
                    }
                    // Jika response langsung berupa data array
                    else if (jsonResponse.has("data")) {
                        JSONArray data = jsonResponse.getJSONArray("data");
                        lostFoundList = parseLostFoundData(data);
                    }
                    // Jika response berupa array langsung
                    else {
                        try {
                            JSONArray dataArray = new JSONArray(result);
                            lostFoundList = parseLostFoundData(dataArray);
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to parse as direct array: " + e.getMessage());
                        }
                    }

                    if (lostFoundList.isEmpty()) {
                        Log.d(TAG, "No data parsed from response");
                        callback.onFailure("Tidak ada data yang dapat diparsing");
                    } else {
                        Log.d(TAG, "Successfully parsed " + lostFoundList.size() + " items");
                        callback.onSuccess(lostFoundList);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON: " + e.getMessage());
                    // Coba parsing sebagai array langsung
                    try {
                        JSONArray dataArray = new JSONArray(result);
                        List<LostFound> lostFoundList = parseLostFoundData(dataArray);
                        if (!lostFoundList.isEmpty()) {
                            callback.onSuccess(lostFoundList);
                        } else {
                            callback.onFailure("Data kosong");
                        }
                    } catch (JSONException e2) {
                        Log.e(TAG, "Failed to parse response: " + e2.getMessage());
                        callback.onFailure("Error parsing data: " + e.getMessage());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error: " + e.getMessage());
                    e.printStackTrace();
                    callback.onFailure("Error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "API Failure: " + error);
                callback.onFailure(error);
            }
        });
    }

    // Parse JSON data to LostFound objects
    private List<LostFound> parseLostFoundData(JSONArray data) throws JSONException {
        List<LostFound> lostFoundList = new ArrayList<>();

        if (data == null || data.length() == 0) {
            return lostFoundList;
        }

        Log.d(TAG, "Parsing " + data.length() + " items");

        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject item = data.getJSONObject(i);
                Log.d(TAG, "Parsing item " + i + ": " + item.toString());

                LostFound lostFound = new LostFound();

                // Parse ID
                lostFound.setId(getStringFromJSON(item, "id", "id_barang", "lostfound_id"));

                // Parse item name/title
                lostFound.setItemName(getStringFromJSON(item, "nama_barang", "item_name", "name", "title", "judul"));

                // Parse description
                lostFound.setDescription(getStringFromJSON(item, "deskripsi", "description", "detail"));

                // Parse location
                lostFound.setLocation(getStringFromJSON(item, "lokasi", "location", "tempat", "place"));

                // Parse category (kategori)
                lostFound.setCategory(getStringFromJSON(item, "kategori", "category", "type", "jenis"));

                // Parse status (lost/found)
                String status = getStringFromJSON(item, "status", "status_barang");
                lostFound.setStatus(status != null && !status.isEmpty() ? status :
                        (lostFound.getCategory().equalsIgnoreCase("found") ? "found" : "lost"));

                // Parse user ID
                lostFound.setUserId(getStringFromJSON(item, "user_id", "id_user", "userId"));

                // Parse user name
                lostFound.setUserName(getStringFromJSON(item, "username", "user_name", "nama", "name", "nama_user"));

                // Parse email and phone
                String email = getStringFromJSON(item, "email", "user_email", "contact_email");
                String phone = getStringFromJSON(item, "no_hp", "phone", "telepon", "contact_phone", "whatsapp");

                // Tambahkan email dan phone ke dalam deskripsi jika ada
                String originalDescription = lostFound.getDescription();
                StringBuilder fullDescription = new StringBuilder();

                if (originalDescription != null && !originalDescription.isEmpty()) {
                    fullDescription.append(originalDescription).append("\n\n");
                }

                if (phone != null && !phone.isEmpty()) {
                    fullDescription.append("📱 Kontak: ").append(phone).append("\n");
                }

                if (email != null && !email.isEmpty()) {
                    fullDescription.append("📧 Email: ").append(email);
                }

                // Set deskripsi lengkap
                lostFound.setDescription(fullDescription.toString().trim());

                // PARSE IMAGE URL - PERBAIKAN UTAMA
                parseAndSetImageUrl(lostFound, item);

                // Parse tanggal
                lostFound.setCreatedAt(getStringFromJSON(item, "created_at", "created", "tanggal", "tanggal_dibuat"));

                // Parse likes dan comments
                lostFound.setLikes(getIntFromJSON(item, "likes", "like_count", "total_likes"));
                lostFound.setComments(getIntFromJSON(item, "comments", "comment_count", "total_comments"));

                // Parse claimed status
                lostFound.setClaimed(getBooleanFromJSON(item, "is_claimed", "claimed", "diklaim"));

                Log.d(TAG, "✅ Parsed item: " + lostFound.getItemName() +
                        ", Email: " + (email != null ? "yes" : "no") +
                        ", Phone: " + (phone != null ? "yes" : "no") +
                        ", Image URL: " + (lostFound.getItemImage() != null ? lostFound.getItemImage() : "no"));

                lostFoundList.add(lostFound);

            } catch (Exception e) {
                Log.e(TAG, "❌ Error parsing item at index " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return lostFoundList;
    }

    // METHOD BARU: Parse dan set image URL dengan logika yang lebih baik
    private void parseAndSetImageUrl(LostFound lostFound, JSONObject item) {
        String fotoBarang = getStringFromJSON(item, "foto_barang", "item_image", "gambar", "image", "photo_url", "photo");

        Log.d(TAG, "🖼️ Original photo value: " + fotoBarang);

        if (fotoBarang == null || fotoBarang.isEmpty() || fotoBarang.equals("null")) {
            lostFound.setItemImage(null);
            Log.d(TAG, "⚠️ No image found in data");
            return;
        }

        // Jika foto sudah berupa URL lengkap
        if (fotoBarang.startsWith("http://") || fotoBarang.startsWith("https://")) {
            lostFound.setItemImage(fotoBarang);
            Log.d(TAG, "✅ Already full URL: " + fotoBarang);
            return;
        }

        // Format yang mungkin dari database:
        // 1. File path dengan format path: "uploads/lostfound/lostfound_12345_abc.jpg"
        // 2. File name saja: "lostfound_12345_abc.jpg"
        // 3. Path dengan slash di awal: "/uploads/lostfound/lostfound_12345_abc.jpg"
        // 4. Path storage Laravel: "storage/uploads/lostfound/lostfound_12345_abc.jpg"
        // 5. Base64 string (jika dari API create)

        String finalUrl;

        // Hapus awalan 'storage/' jika ada (format Laravel)
        if (fotoBarang.startsWith("storage/")) {
            fotoBarang = fotoBarang.substring(8); // Hapus "storage/"
        }

        // Hapus slash di awal jika ada
        if (fotoBarang.startsWith("/")) {
            fotoBarang = fotoBarang.substring(1);
        }

        // Cek jika sudah merupakan path lengkap
        if (fotoBarang.startsWith("uploads/lostfound/")) {
            finalUrl = BASE_URL + "/" + fotoBarang;
        }
        // Cek jika hanya nama file
        else if (fotoBarang.startsWith("lostfound_") || fotoBarang.startsWith("barang_")) {
            finalUrl = BASE_URL + "/uploads/lostfound/" + fotoBarang;
        }
        // Default: anggap sebagai nama file di folder uploads/lostfound
        else {
            finalUrl = BASE_URL + "/uploads/lostfound/" + fotoBarang;
        }

        // Bersihkan URL dari spasi atau karakter aneh
        finalUrl = finalUrl.replace(" ", "%20").trim();

        lostFound.setItemImage(finalUrl);
        Log.d(TAG, "🔄 Converted to URL: " + finalUrl);
    }

    // Helper method untuk mendapatkan string dari JSON dengan multiple key options
    private String getStringFromJSON(JSONObject json, String... keys) {
        for (String key : keys) {
            try {
                if (json.has(key) && !json.isNull(key)) {
                    Object valueObj = json.get(key);
                    if (valueObj == null) {
                        continue;
                    }

                    String value = valueObj.toString();
                    if (value != null && !value.equals("null") && !value.isEmpty()) {
                        return value.trim();
                    }
                }
            } catch (JSONException e) {
                // Continue to next key
            }
        }
        return "";
    }

    // Helper method untuk mendapatkan integer dari JSON
    private int getIntFromJSON(JSONObject json, String... keys) {
        for (String key : keys) {
            try {
                if (json.has(key) && !json.isNull(key)) {
                    Object valueObj = json.get(key);
                    if (valueObj instanceof Integer) {
                        return (Integer) valueObj;
                    } else if (valueObj instanceof String) {
                        try {
                            return Integer.parseInt((String) valueObj);
                        } catch (NumberFormatException e) {
                            // Continue
                        }
                    }
                }
            } catch (JSONException e) {
                // Continue to next key
            }
        }
        return 0;
    }

    // Helper method untuk mendapatkan boolean dari JSON
    private boolean getBooleanFromJSON(JSONObject json, String... keys) {
        for (String key : keys) {
            try {
                if (json.has(key) && !json.isNull(key)) {
                    Object valueObj = json.get(key);
                    if (valueObj instanceof Boolean) {
                        return (Boolean) valueObj;
                    } else if (valueObj instanceof String) {
                        String str = (String) valueObj;
                        return str.equalsIgnoreCase("true") || str.equals("1");
                    } else if (valueObj instanceof Integer) {
                        return (Integer) valueObj == 1;
                    }
                }
            } catch (JSONException e) {
                // Continue to next key
            }
        }
        return false;
    }
}