package com.paperturtle.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.paperturtle.components.TextLabel;

import javafx.scene.text.TextAlignment;

/**
 * Deserializer for TextLabel objects from JSON.
 * Implements the JsonDeserializer interface to provide custom deserialization
 * logic.
 * 
 * @see TextLabel
 * @see JsonDeserializer
 * @see com.google.gson.Gson
 * @see TextLabelSerializer
 * 
 * @author Seweryn Czabanowski
 */
public class TextLabelDeserializer implements JsonDeserializer<TextLabel> {
    /**
     * Default constructor for TextLabelDeserializer.
     */
    public TextLabelDeserializer() {
    }

    /**
     * Deserializes a JSON element into a TextLabel object.
     * 
     * @param json    the JSON element to deserialize
     * @param typeOfT the type of the Object to deserialize to
     * @param context the context of the deserialization
     * @return the deserialized TextLabel object
     * @throws JsonParseException if the JSON is not in the expected format
     */
    @Override
    public TextLabel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        TextLabel textLabel = new TextLabel(
                jsonObject.get("label").getAsString(),
                jsonObject.get("width").getAsDouble(),
                jsonObject.get("height").getAsDouble());
        textLabel.setLayoutX(jsonObject.get("x").getAsDouble());
        textLabel.setLayoutY(jsonObject.get("y").getAsDouble());
        textLabel.setFontFamily(jsonObject.get("fontFamily").getAsString());
        textLabel.setFontSize(jsonObject.get("fontSize").getAsInt());
        textLabel.setFillColor(jsonObject.get("fillColor").getAsString());
        textLabel.setTextAlignment(TextAlignment.valueOf(jsonObject.get("textAlignment").getAsString()));
        return textLabel;
    }
}