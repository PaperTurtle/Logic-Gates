package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.geometry.Point2D;

import java.io.FileWriter;
import java.io.File;
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
     * @param file The file path from where to load the circuit.
     * @return A list of LogicGates reconstructed from the saved data.
     * @throws IOException If an I/O error occurs.
     */
    public List<GateData> loadCircuit(String filePath) throws IOException, IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath);
        JsonNode rootNode = mapper.readTree(file);

        if (!rootNode.isArray()) {
            throw new IllegalArgumentException("Invalid JSON: Expected an array of gate data.");
        }

        List<GateData> gatesData = new ArrayList<>();
        for (JsonNode node : rootNode) {
            if (!isValidGateNode(node)) {
                throw new IllegalArgumentException("Invalid gate data in JSON.");
            }
            GateData gate = mapper.treeToValue(node, GateData.class);
            gatesData.add(gate);
        }
        return gatesData;
    }

    private boolean isValidGateNode(JsonNode node) {
        return node.has("id") && node.has("type") && node.has("position") &&
                node.get("position").has("x") && node.get("position").has("y");
    }
}
