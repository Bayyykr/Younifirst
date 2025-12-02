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
        Log.d(TAG, "Loading teams data from API...");

        ApiHelper.fetchTeams(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                parseTeamData(result, callback);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "API Error: " + error);
                callback.onFailure(error);
            }
        });
    }

    private void parseTeamData(String result, TeamCallback callback) {
        try {
            Log.d(TAG, "Parsing team data...");
            Log.d(TAG, "Raw response: " + (result.length() > 500 ? result.substring(0, 500) + "..." : result));

            JSONObject response = new JSONObject(result);
            JSONArray teamsArray = null;

            // Debug: print all keys in response
            Log.d(TAG, "Response keys: " + response.toString());

            // Coba berbagai kemungkinan key untuk array teams
            if (response.has("data")) {
                teamsArray = response.getJSONArray("data");
                Log.d(TAG, "Found 'data' array with length: " + teamsArray.length());
            } else if (response.has("teams")) {
                teamsArray = response.getJSONArray("teams");
                Log.d(TAG, "Found 'teams' array with length: " + teamsArray.length());
            } else if (response.has("items")) {
                teamsArray = response.getJSONArray("items");
                Log.d(TAG, "Found 'items' array with length: " + teamsArray.length());
            } else {
                // Coba parsing langsung sebagai array
                try {
                    teamsArray = new JSONArray(result);
                    Log.d(TAG, "Response is direct array with length: " + teamsArray.length());
                } catch (JSONException e) {
                    Log.e(TAG, "Response is not a valid JSON array");
                }
            }

            if (teamsArray == null || teamsArray.length() == 0) {
                Log.w(TAG, "No teams array found or array is empty");
                callback.onSuccess(new ArrayList<>()); // Return empty list instead of failure
                return;
            }

            List<Team> teams = new ArrayList<>();
            int successfulParses = 0;

            for (int i = 0; i < teamsArray.length(); i++) {
                try {
                    JSONObject teamJson = teamsArray.getJSONObject(i);

                    // Debug: log JSON structure
                    Log.d(TAG, "Team JSON " + i + ": " + teamJson.toString());

                    Team team = new Team(teamJson);

                    // Filter hanya tim dengan status aktif
                    String status = team.getStatus();
                    if (status != null &&
                            (status.equalsIgnoreCase("confirm") ||
                                    status.equalsIgnoreCase("approved") ||
                                    status.equalsIgnoreCase("open"))) {

                        teams.add(team);
                        successfulParses++;
                        Log.d(TAG, "✓ Added team: " + team.getNamaTeam() +
                                " | Role: " + team.getRoleRequired() +
                                " | Max: " + team.getMaxAnggota());
                    } else {
                        Log.d(TAG, "✗ Skipped team (inactive): " + team.getNamaTeam() +
                                " | Status: " + status);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "❌ Error parsing team at index " + i, e);
                    Log.e(TAG, "Problematic JSON: " + teamsArray.optString(i, "{}"));
                } catch (Exception e) {
                    Log.e(TAG, "❌ Unexpected error parsing team at index " + i, e);
                }
            }

            Log.d(TAG, "✅ Successfully parsed " + successfulParses + " of " +
                    teamsArray.length() + " teams");

            if (teams.isEmpty()) {
                Log.w(TAG, "⚠️ No active teams found in response");
            }

            callback.onSuccess(teams);

        } catch (JSONException e) {
            Log.e(TAG, "❌ JSON Parsing Error: " + e.getMessage(), e);
            callback.onFailure("Error parsing team data: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Unexpected error: " + e.getMessage(), e);
            callback.onFailure("Unexpected error: " + e.getMessage());
        }
    }

    // Method untuk load hanya tim aktif
    public void loadActiveTeams(TeamCallback callback) {
        Log.d(TAG, "Loading active teams...");

        ApiHelper.fetchActiveTeams(new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                parseTeamData(result, callback);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "API Error for active teams: " + error);
                callback.onFailure(error);
            }
        });
    }
}