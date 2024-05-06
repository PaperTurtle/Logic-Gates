package com.paperturtle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import javafx.geometry.Point2D;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CircuitFileManager {

    private Gson gson = new Gson();

    public CircuitFileManager() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Point2D.class, new Point2DSerializer())
                .registerTypeAdapter(Point2D.class, new Point2DDeserializer())
                .create();
    }

    /**
     * Saves the current circuit to a JSON file.
     * 
     * @param file  The file path where the circuit should be saved.
     * @param gates The list of LogicGates to save.
     * @throws IOException If an I/O error occurs.
     */
    public void saveCircuit(String file, List<GateData> gates) throws IOException {
        Type type = new TypeToken<List<GateData>>() {
        }.getType();
        String json = gson.toJson(gates, type);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }
    }

    /**
     * Loads a circuit from a JSON file.
     * 
     * @param filePath The file path from where to load the circuit.
     * @return A list of LogicGates reconstructed from the saved data.
     * @throws IOException If an I/O error occurs.
     */
    public List<GateData> loadCircuit(String filePath) throws IOException {
        Type type = new TypeToken<List<GateData>>() {
        }.getType();
        try (FileReader reader = new FileReader(filePath)) {
            JsonElement fileElement = JsonParser.parseReader(reader);
            if (!fileElement.isJsonArray()) {
                throw new IllegalArgumentException("Invalid JSON: Expected an array of gate data.");
            }
            JsonArray jsonArray = fileElement.getAsJsonArray();
            List<GateData> gatesData = new ArrayList<>();

            for (JsonElement element : jsonArray) {
                if (!isValidGateNode(element.getAsJsonObject())) {
                    throw new IllegalArgumentException("Invalid gate data in JSON.");
                }
                gatesData.add(gson.fromJson(element, GateData.class));
            }
            return gatesData;
        }
    }

    /**
     * Checks if the given JSON object is a valid gate node.
     * 
     * @param node The JSON object to check.
     * @return True if the JSON object is a valid gate node, false otherwise.
     */
    private boolean isValidGateNode(JsonObject node) {
        return node.has("id") && node.has("type") && node.has("position") &&
                node.get("position").isJsonObject() &&
                node.getAsJsonObject("position").has("x") &&
                node.getAsJsonObject("position").has("y");
    }
}
