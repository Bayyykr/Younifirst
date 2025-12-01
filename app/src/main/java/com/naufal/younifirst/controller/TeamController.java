package com.naufal.younifirst.controller;

import android.util.Log;

import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.model.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TeamController {

    private static final String TAG = "TeamController";

    public interface TeamCallback {
        void onSuccess(List<Team> teams);
        void onFailure(String error);
    }

    public void loadTeamsData(TeamCallback callback) {
        ApiHelper.fetchTeams(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.d(TAG, "Raw API Response: " + result);

                    // Coba parse sebagai JSONObject dulu
                    JSONObject response = new JSONObject(result);

                    // Debug: lihat keys yang ada di response
                    Log.d(TAG, "Response keys: " + response.toString());

                    // Cek berbagai kemungkinan key untuk array teams
                    JSONArray teamsArray = null;

                    if (response.has("teams")) {
                        teamsArray = response.getJSONArray("teams");
                        Log.d(TAG, "Found 'teams' array with length: " + teamsArray.length());
                    } else if (response.has("data")) {
                        teamsArray = response.getJSONArray("data");
                        Log.d(TAG, "Found 'data' array with length: " + teamsArray.length());
                    } else {
                        // Jika response langsung array
                        try {
                            teamsArray = new JSONArray(result);
                            Log.d(TAG, "Response is direct array with length: " + teamsArray.length());
                        } catch (JSONException e) {
                            Log.e(TAG, "Response is not a valid JSON array");
                        }
                    }

                    if (teamsArray == null) {
                        callback.onFailure("No valid teams array found in response");
                        return;
                    }

                    List<Team> teams = new ArrayList<>();
                    for (int i = 0; i < teamsArray.length(); i++) {
                        try {
                            JSONObject teamJson = teamsArray.getJSONObject(i);
                            Team team = new Team(teamJson);
                            teams.add(team);
                            Log.d(TAG, "✓ Added team: " + team.getNamaTeam());
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing team at index " + i, e);
                        }
                    }

                    Log.d(TAG, "Successfully loaded " + teams.size() + " teams");
                    callback.onSuccess(teams);

                } catch (JSONException e) {
                    Log.e(TAG, "JSON Parsing Error: " + e.getMessage(), e);
                    callback.onFailure("Error parsing team data: " + e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
                    callback.onFailure("Unexpected error: " + e.getMessage());
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