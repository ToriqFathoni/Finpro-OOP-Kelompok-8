package com.fernanda.finpro.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.fernanda.finpro.components.ItemType;
import com.fernanda.finpro.entities.Player;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class NetworkManager {
    private static final String BASE_URL = "http://localhost:8080/api/players";
    private static NetworkManager instance;
    private String currentUsername;
    private boolean miniBossDefeated = false;

    private NetworkManager() {}

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public interface LoginCallback {
        void onSuccess(String username, Map<String, Integer> inventoryData, boolean miniBossDefeated, boolean bossKilled);
        void onFailure(Throwable t);
    }

    public void login(String username, final LoginCallback callback) {
        String encodedUsername = username;
        try {
            encodedUsername = URLEncoder.encode(username, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Gdx.app.error("NetworkManager", "Encoding error", e);
        }

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(BASE_URL + "/login?username=" + encodedUsername);
        
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                final String result = httpResponse.getResultAsString();
                final int statusCode = httpResponse.getStatus().getStatusCode();

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (statusCode != 200) {
                            callback.onFailure(new RuntimeException("Server returned error: " + statusCode));
                            return;
                        }

                        try {
                            JsonValue root = new JsonReader().parse(result);
                            String user = root.getString("username");
                            currentUsername = user;
                            
                            Map<String, Integer> inventoryData = new HashMap<>();
                            if (root.has("inventory") && !root.get("inventory").isNull()) {
                                JsonValue inv = root.get("inventory");
                                for (JsonValue item : inv) {
                                    inventoryData.put(item.name, item.asInt());
                                }
                            }
                            
                            // Load MiniBoss defeated state
                            boolean loadedMiniBossState = false;
                            if (root.has("miniBossDefeated")) {
                                loadedMiniBossState = root.getBoolean("miniBossDefeated");
                            }
                            miniBossDefeated = loadedMiniBossState;

                            // Load Boss Killed state
                            boolean loadedBossKilled = false;
                            if (root.has("bossKilled")) {
                                loadedBossKilled = root.getBoolean("bossKilled");
                            }
                            
                            callback.onSuccess(user, inventoryData, loadedMiniBossState, loadedBossKilled);
                        } catch (Exception e) {
                            callback.onFailure(e);
                        }
                    }
                });
            }

            @Override
            public void failed(final Throwable t) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(t);
                    }
                });
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(new RuntimeException("Request cancelled"));
                    }
                });
            }
        });
    }

    public boolean isMiniBossDefeated() {
        return miniBossDefeated;
    }

    public void setMiniBossDefeated(boolean defeated) {
        this.miniBossDefeated = defeated;
        saveMiniBossState();
    }

    private void saveMiniBossState() {
        if (currentUsername == null) return;

        String requestContent = "{\"miniBossDefeated\":" + miniBossDefeated + "}";
        System.out.println("Saving MiniBoss State: " + requestContent);

        String encodedUsername = currentUsername;
        try {
            encodedUsername = URLEncoder.encode(currentUsername, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Gdx.app.error("NetworkManager", "Encoding error", e);
        }

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(BASE_URL + "/" + encodedUsername + "/miniboss");
        request.setHeader("Content-Type", "application/json");
        request.setContent(requestContent);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                System.out.println("MiniBoss state saved. Status: " + httpResponse.getStatus().getStatusCode());
            }

            @Override
            public void failed(Throwable t) {
                System.err.println("Failed to save MiniBoss state: " + t.getMessage());
            }

            @Override
            public void cancelled() {
            }
        });
    }

    public void saveInventory(Player player) {
        if (currentUsername == null) return;

        // Manual JSON construction to ensure correct format
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        int i = 0;
        for (Map.Entry<ItemType, Integer> entry : player.inventory.getItems().entrySet()) {
            if (i > 0) jsonBuilder.append(",");
            jsonBuilder.append("\"").append(entry.getKey().name()).append("\":").append(entry.getValue());
            i++;
        }
        jsonBuilder.append("}");
        String requestContent = jsonBuilder.toString();

        System.out.println("Sending Inventory JSON: " + requestContent);

        String encodedUsername = currentUsername;
        try {
            encodedUsername = URLEncoder.encode(currentUsername, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Gdx.app.error("NetworkManager", "Encoding error", e);
        }

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(BASE_URL + "/" + encodedUsername + "/inventory");
        request.setHeader("Content-Type", "application/json");
        request.setContent(requestContent);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                System.out.println("Inventory saved successfully. Status: " + httpResponse.getStatus().getStatusCode());
            }

            @Override
            public void failed(Throwable t) {
                System.err.println("Failed to save inventory: " + t.getMessage());
            }

            @Override
            public void cancelled() {
            }
        });
    }

    public void updateScore(String username, int cookingScore, int monsterKillScore, boolean bossKilled, final Runnable onSuccess) {
        if (username == null) {
            System.err.println("Cannot update score: Username is null");
            return;
        }
        String encodedUsername = username;
        try {
            encodedUsername = URLEncoder.encode(username, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Gdx.app.error("NetworkManager", "Encoding error", e);
        }

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(BASE_URL + "/" + encodedUsername + "/score");
        request.setHeader("Content-Type", "application/json");

        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("cookingScore", new JsonValue(cookingScore));
        json.addChild("monsterKillScore", new JsonValue(monsterKillScore));
        json.addChild("bossKilled", new JsonValue(bossKilled));

        request.setContent(json.toJson(JsonWriter.OutputType.json));

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                System.out.println("Score updated: " + httpResponse.getStatus().getStatusCode());
                if (onSuccess != null) {
                    Gdx.app.postRunnable(onSuccess);
                }
            }

            @Override
            public void failed(Throwable t) {
                System.err.println("Failed to update score: " + t.getMessage());
            }

            @Override
            public void cancelled() {
            }
        });
    }

    public interface LeaderboardCallback {
        void onSuccess(java.util.List<LeaderboardEntry> entries);
        void onFailure(Throwable t);
    }

    public static class LeaderboardEntry {
        public String username;
        public int cookingScore;
        public int monsterKillScore;
        public boolean bossKilled;
    }

    public void getLeaderboard(final LeaderboardCallback callback) {
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(BASE_URL + "/leaderboard");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                final String result = httpResponse.getResultAsString();
                Gdx.app.postRunnable(() -> {
                    try {
                        JsonValue root = new JsonReader().parse(result);
                        java.util.List<LeaderboardEntry> entries = new java.util.ArrayList<>();
                        for (JsonValue entry : root) {
                            LeaderboardEntry le = new LeaderboardEntry();
                            le.username = entry.getString("username");
                            le.cookingScore = entry.getInt("cookingScore", 0);
                            le.monsterKillScore = entry.getInt("monsterKillScore", 0);
                            le.bossKilled = entry.getBoolean("bossKilled", false);
                            entries.add(le);
                        }
                        callback.onSuccess(entries);
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> callback.onFailure(t));
            }

            @Override
            public void cancelled() {
            }
        });
    }
}
