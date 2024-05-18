package com.paperturtle.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.paperturtle.components.TextLabel;
import com.paperturtle.serializers.Point2DDeserializer;
import com.paperturtle.serializers.Point2DSerializer;
import com.paperturtle.serializers.TextLabelDeserializer;
import com.paperturtle.serializers.TextLabelSerializer;
import com.paperturtle.utils.CircuitComponent;
import com.paperturtle.utils.ComponentAdapter;

import javafx.geometry.Point2D;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * The CircuitFileManager class is responsible for saving and loading circuits
 * 
 * The class uses the Gson library to serialize and deserialize the circuit data
 * to and from JSON format.
 */
public class CircuitFileManager {

    private Gson gson = new Gson();

    public CircuitFileManager() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Point2D.class, new Point2DSerializer())
                .registerTypeAdapter(Point2D.class, new Point2DDeserializer())
                .registerTypeAdapter(TextLabel.class, new TextLabelSerializer())
                .registerTypeAdapter(TextLabel.class, new TextLabelDeserializer())
                .registerTypeAdapter(CircuitComponent.class, new ComponentAdapter())
                .create();
    }

    /**
     * Saves the current circuit to a JSON file.
     * 
     * @param file  The file path where the circuit should be saved.
     * @param gates The list of LogicGates to save.
     * @throws IOException If an I/O error occurs.
     */
    public void saveCircuit(String file, List<CircuitComponent> components) throws IOException {
        Type type = new TypeToken<List<CircuitComponent>>() {
        }.getType();
        String json = gson.toJson(components, type);
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
    public List<CircuitComponent> loadCircuit(String filePath) throws IOException {
        Type type = new TypeToken<List<CircuitComponent>>() {
        }.getType();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, type);
        }
    }
}
