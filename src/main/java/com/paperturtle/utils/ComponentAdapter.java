package com.paperturtle.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.paperturtle.components.TextLabel;
import com.paperturtle.data.GateData;

import java.lang.reflect.Type;

/**
 * The ComponentAdapter class is responsible for serializing and deserializing
 * CircuitComponent objects.
 * 
 * The class is used by the CircuitFileManager class to serialize and
 * deserialize CircuitComponent objects.
 */
public class ComponentAdapter implements JsonSerializer<CircuitComponent>, JsonDeserializer<CircuitComponent> {
    /**
     * Serializes a CircuitComponent object to a JSON element.
     * 
     * @param src       The CircuitComponent object to serialize.
     * @param typeOfSrc The type of the source object.
     * @param context   The serialization context.
     * @return A JSON element representing the serialized CircuitComponent object.
     */
    @Override
    public JsonElement serialize(CircuitComponent src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        if (src instanceof GateData) {
            result.addProperty("componentType", "gate");
            JsonObject gateData = context.serialize(src, GateData.class).getAsJsonObject();
            result.add("data", gateData);
        } else if (src instanceof TextLabel) {
            result.addProperty("componentType", "textLabel");
            JsonObject textData = context.serialize(src, TextLabel.class).getAsJsonObject();
            result.add("data", textData);
        }
        return result;
    }

    /**
     * Deserializes a JSON element to a CircuitComponent object.
     * 
     * @param json    The JSON element to deserialize.
     * @param typeOfT The type of the target object.
     * @param context The deserialization context.
     */
    @Override
    public CircuitComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("componentType").getAsString();
        JsonElement data = jsonObject.get("data");
        if ("gate".equals(type)) {
            return context.deserialize(data, GateData.class);
        } else if ("textLabel".equals(type)) {
            return context.deserialize(data, TextLabel.class);
        }
        throw new IllegalArgumentException("Unknown component type: " + type);
    }
}
