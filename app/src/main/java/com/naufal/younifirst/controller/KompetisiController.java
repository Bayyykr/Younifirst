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

                    List<Kompetisi> allCompetitions = new ArrayList<>();
                    List<Kompetisi> confirmedCompetitions = new ArrayList<>();

                    for (int i = 0; i < competitionsArray.length(); i++) {
                        JSONObject compJson = competitionsArray.getJSONObject(i);
                        Kompetisi competition = new Kompetisi(compJson);
                        allCompetitions.add(competition);

                        // Filter hanya yang status "confirm"
                        if (competition.getStatus() != null &&
                                competition.getStatus().equalsIgnoreCase("confirm")) {
                            confirmedCompetitions.add(competition);
                            Log.d(TAG, "✓ Confirmed: " + competition.getNamaLomba());
                        } else {
                            Log.d(TAG, "✗ Skipped: " + competition.getNamaLomba() +
                                    " (Status: " + competition.getStatus() + ")");
                        }
                    }

                    Log.d(TAG, "Total competitions: " + allCompetitions.size());
                    Log.d(TAG, "Confirmed competitions: " + confirmedCompetitions.size());

                    // Return hanya kompetisi yang confirmed
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
}