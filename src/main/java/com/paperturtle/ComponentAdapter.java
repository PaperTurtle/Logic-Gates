package com.paperturtle;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class ComponentAdapter implements JsonSerializer<CircuitComponent>, JsonDeserializer<CircuitComponent> {
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
