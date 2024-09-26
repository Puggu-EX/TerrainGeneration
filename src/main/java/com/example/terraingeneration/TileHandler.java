package com.example.terraingeneration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TileHandler {

    private static JsonObject tiles;

    // Load JSON data from a file
    public static void loadTiles(File dataFolder, String fileName) {

        File jsonFile = new File(dataFolder.getAbsolutePath(), fileName);

        if (!jsonFile.exists()) {
            System.out.println("JSON file not found: " + fileName);
            return;
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            Gson gson = new Gson();
            tiles = gson.fromJson(reader, JsonObject.class);
            tiles = tiles.get("tiles").getAsJsonObject();
            System.out.println("Successfully loaded JSON data from " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JsonObject getTiles() {
        return tiles;
    }

    public static JsonObject getTile(String tileName) {
        if (tiles != null && tiles.has(tileName)) {
            return tiles.getAsJsonObject(tileName);
        }
        return null;
    }

    public static String[][] getLayout(JsonObject tile) {
        if (tile == null || !tile.has("layout")) {
            return null; // Handle cases where the layout is missing
        }

        JsonArray layoutJson = tile.getAsJsonArray("layout");
        int rows = layoutJson.size();
        int cols = layoutJson.get(0).getAsJsonArray().size();
        String[][] layout = new String[rows][cols];

        // Iterate over the JSON array to fill the 2D integer array
        for (int i = 0; i < rows; i++) {
            JsonArray row = layoutJson.get(i).getAsJsonArray();
            for (int j = 0; j < cols; j++) {
                layout[i][j] = row.get(j).getAsString();
            }
        }

        return layout;
    }
}
