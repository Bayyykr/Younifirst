package com.naufal.younifirst.utils;

public class UrlUtils {
    private static final String BASE_URL = "http://192.168.1.12:8000";

    public static String getFullUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }

        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        return BASE_URL + relativePath;
    }

    public static String getCompetitionImageUrl(String posterPath) {
        return getFullUrl(posterPath);
    }

    public static String getTeamImageUrl(String imagePath) {
        return getFullUrl(imagePath);
    }
}