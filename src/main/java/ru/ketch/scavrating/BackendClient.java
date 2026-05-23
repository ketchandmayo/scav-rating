package ru.ketch.scavrating;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;

public class BackendClient {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final String SECRET = "ScavRatingSecret2026_xyz";

    private static String generateSignature(String playerName, long timeTicks) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String payload = playerName + ":" + timeTicks + ":" + SECRET;
            byte[] hash = digest.digest(payload.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static void submitRun(String playerName, String playerUuid, String itemId, String modifierId, long timeTicks, String seed) {
        JsonObject json = new JsonObject();
        json.addProperty("player_name", playerName);
        json.addProperty("player_uuid", playerUuid);
        json.addProperty("item_id", itemId);
        json.addProperty("modifier_id", modifierId);
        json.addProperty("time_ticks", timeTicks);
        json.addProperty("seed", seed);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ScavRating.BACKEND_URL + "/api/runs"))
                .header("Content-Type", "application/json")
                .header("X-Scav-Signature", generateSignature(playerName, timeTicks))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(json)))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 201) {
                        ScavRating.LOGGER.info("Successfully submitted run to backend!");
                    } else {
                        ScavRating.LOGGER.error("Failed to submit run: " + response.statusCode() + " " + response.body());
                    }
                })
                .exceptionally(ex -> {
                    ScavRating.LOGGER.error("Error submitting run", ex);
                    return null;
                });
    }

    public static CompletableFuture<JsonArray> getLeaderboard(String itemId, String modifierId) {
        String url = ScavRating.BACKEND_URL + "/api/leaderboard?";
        if (itemId != null && !itemId.isEmpty()) {
            url += "item_id=" + itemId + "&";
        }
        if (modifierId != null && !modifierId.isEmpty()) {
            url += "modifier_id=" + modifierId;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return JsonParser.parseString(response.body()).getAsJsonArray();
                    }
                    return new JsonArray();
                });
    }

    public static CompletableFuture<JsonObject> getFilters() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ScavRating.BACKEND_URL + "/api/filters"))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return JsonParser.parseString(response.body()).getAsJsonObject();
                    }
                    return new JsonObject();
                });
    }
}
