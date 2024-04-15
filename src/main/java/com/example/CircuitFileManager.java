package com.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class CircuitFileManager {

    private Gson gson = new Gson();

    /**
     * Saves the current circuit to a JSON file.
     * 
     * @param file  The file path where the circuit should be saved.
     * @param gates The list of LogicGates to save.
     * @throws IOException If an I/O error occurs.
     */
    public void saveCircuit(String file, List<LogicGate> gates) throws IOException {
        Type type = new TypeToken<List<LogicGate>>() {
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
    public List<LogicGate> loadCircuit(String file) throws IOException {
        Type type = new TypeToken<List<LogicGate>>() {
        }.getType();
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, type);
        }
    }
}
