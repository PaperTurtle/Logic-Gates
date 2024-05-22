package com.paperturtle.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.paperturtle.components.utilities.TextLabel;
import com.paperturtle.data.GateData;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * The ComponentAdapter class is responsible for serializing and deserializing
 * CircuitComponent objects.
 * 
 * The class is used by the CircuitFileManager class to serialize and
 * deserialize CircuitComponent objects.
 */
public class ComponentAdapter implements JsonSerializer<CircuitComponent>, JsonDeserializer<CircuitComponent> {
    /**
     * The key for the component type in the serialized data.
     */
    private static final String COMPONENT_TYPE = "componentType";

    /**
     * The key for the component data in the serialized data.
     */
    private static final String DATA = "data";

    /**
     * The value for the gate type in the serialized data.
     */
    private static final String GATE_TYPE = "gate";

    /**
     * The value for the text label type in the serialized data.
     */
    private static final String TEXT_LABEL_TYPE = "textLabel";

    /**
     * A map from component type strings to the corresponding class objects.
     */
    private static final Map<String, Class<? extends CircuitComponent>> componentTypeMap = new HashMap<>();

    static {
        /**
         * Populates the component type map with the gate and text label types.
         */
        componentTypeMap.put(GATE_TYPE, GateData.class);
        componentTypeMap.put(TEXT_LABEL_TYPE, TextLabel.class);
    }

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
        if (src != null) {
            String componentType = null;
            JsonObject data = null;

            if (src instanceof GateData) {
                componentType = GATE_TYPE;
                data = context.serialize(src, GateData.class).getAsJsonObject();
            } else if (src instanceof TextLabel) {
                componentType = TEXT_LABEL_TYPE;
                data = context.serialize(src, TextLabel.class).getAsJsonObject();
            }

            if (componentType != null && data != null) {
                result.addProperty(COMPONENT_TYPE, componentType);
                result.add(DATA, data);
            }
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
        String type = jsonObject.get(COMPONENT_TYPE).getAsString();
        JsonElement data = jsonObject.get(DATA);

        Class<? extends CircuitComponent> componentClass = componentTypeMap.get(type);
        if (componentClass != null) {
            return context.deserialize(data, componentClass);
        }

        throw new JsonParseException("Unknown component type: " + type);
    }
}
