package com.naufal.younifirst.controller;

import android.util.Log;

import com.naufal.younifirst.api.ApiHelper;
import com.naufal.younifirst.model.Team;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TeamController {

    private static final String TAG = "TeamController";

    public interface TeamCallback {
        void onSuccess(List<Team> teams);
        void onFailure(String error);
    }

    public void loadTeamsData(final TeamCallback callback) {
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
            int skippedCount = 0;

            for (int i = 0; i < teamsArray.length(); i++) {
                try {
                    JSONObject teamJson = teamsArray.getJSONObject(i);

                    Log.d(TAG, "Team JSON " + i + ": " + teamJson.toString());

                    Team team = new Team(teamJson);

                    String status = team.getStatus();
                    boolean isConfirmed = false;

                    // 🔥 FILTER: Hanya tampilkan yang statusnya "confirm"
                    if (status != null) {
                        String statusLower = status.toLowerCase().trim();
                        // Cek berbagai variasi penulisan "confirm"
                        isConfirmed = statusLower.equals("confirm") ||
                                statusLower.equals("confirmed") ||
                                statusLower.equals("konfirm") ||
                                statusLower.equals("konfirmasi");

                        Log.d(TAG, "Team: " + team.getNamaTeam() +
                                " | Status: " + status +
                                " | isConfirmed: " + isConfirmed);
                    }

                    if (isConfirmed) {
                        teams.add(team);
                        successfulParses++;
                        Log.d(TAG, "✅ Added team: " + team.getNamaTeam() +
                                " | Status: " + status +
                                " | Role: " + team.getRoleRequired() +
                                " | Max: " + team.getMaxAnggota());
                    } else {
                        skippedCount++;
                        Log.d(TAG, "❌ Skipped team (not confirmed): " + team.getNamaTeam() +
                                " | Status: " + status);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "❌ Error parsing team at index " + i, e);
                    Log.e(TAG, "Problematic JSON: " + teamsArray.optString(i, "{}"));
                } catch (Exception e) {
                    Log.e(TAG, "❌ Unexpected error parsing team at index " + i, e);
                }
            }

            Log.d(TAG, "✅ Successfully parsed " + successfulParses + " confirmed teams of " +
                    teamsArray.length() + " total teams (skipped " + skippedCount + " non-confirmed teams)");

            if (teams.isEmpty()) {
                Log.w(TAG, "⚠️ No confirmed teams found in response");

                // 🔥 DEBUG: Tampilkan semua data untuk troubleshooting
                Log.d(TAG, "=== DEBUG: ALL TEAMS DATA ===");
                for (int i = 0; i < teamsArray.length(); i++) {
                    try {
                        JSONObject teamJson = teamsArray.getJSONObject(i);
                        String teamName = teamJson.optString("nama_team", "N/A");
                        String teamStatus = teamJson.optString("status", "N/A");
                        Log.d(TAG, "Team " + i + ": " + teamName +
                                " | Status: " + teamStatus);
                    } catch (Exception e) {
                        Log.e(TAG, "Error logging team " + i, e);
                    }
                }
                Log.d(TAG, "=== END DEBUG ===");
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

    // 🔥 Method untuk load hanya tim dengan status "confirm"
    public void loadConfirmedTeams(TeamCallback callback) {
        Log.d(TAG, "Loading confirmed teams...");
        loadTeamsData(callback); // Gunakan method yang sama dengan filter confirm
    }
    // Tambahkan interface ini di dalam TeamController class
    public interface TeamDetailCallback {
        void onSuccess(Team team);
        void onFailure(String error);
    }

    // Tambahkan method ini ke dalam TeamController class
    public void getTeamById(String teamId, TeamDetailCallback callback) {
        if (teamId == null || teamId.isEmpty()) {
            callback.onFailure("ID tim tidak valid");
            return;
        }

        Log.d(TAG, "Fetching team detail for ID: " + teamId);

        // Gunakan ApiHelper untuk fetch data tim
        ApiHelper.fetchTeamById(teamId, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.d(TAG, "Team detail response: " + result);

                    JSONObject response = new JSONObject(result);
                    JSONObject teamJson = null;

                    // Cari data tim dalam response
                    if (response.has("data")) {
                        teamJson = response.getJSONObject("data");
                    } else if (response.has("team")) {
                        teamJson = response.getJSONObject("team");
                    } else {
                        // Jika response langsung berisi data tim
                        teamJson = response;
                    }

                    if (teamJson != null) {
                        Team team = new Team(teamJson);
                        callback.onSuccess(team);
                    } else {
                        callback.onFailure("Data tim tidak ditemukan");
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing team detail: " + e.getMessage());
                    callback.onFailure("Error parsing data");
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error fetching team detail: " + error);
                callback.onFailure(error);
            }
        });
    }
}