package com.fernanda.finpro.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
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

    public interface LoginCallback {
        void onSuccess(String username, Map<String, Integer> inventoryData, boolean miniBossDefeated);
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
                            
                            callback.onSuccess(user, inventoryData, loadedMiniBossState);
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
}
