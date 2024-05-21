package com.paperturtle.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.paperturtle.components.TextLabel;

import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Serializer for TextLabel objects to JSON.
 * Implements the JsonSerializer interface to provide custom serialization
 * logic.
 * 
 * @see TextLabel
 * @see JsonSerializer
 * @see com.google.gson.Gson
 * @see TextLabelDeserializer
 * 
 * @author Seweryn Czabanowski
 */
public class TextLabelSerializer implements JsonSerializer<TextLabel> {
    /**
     * Default constructor for TextLabelSerializer.
     */
    public TextLabelSerializer() {
    }

    /**
     * Serializes a TextLabel object into a JSON element.
     * 
     * @param src       the TextLabel object to serialize
     * @param typeOfSrc the type of the source Object
     * @param context   the context of the serialization
     * @return the serialized JSON element
     */
    @Override
    public JsonElement serialize(TextLabel src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("componentType", "textLabel");
        JsonObject data = new JsonObject();
        data.addProperty("label", src.getLabel());
        data.addProperty("width", src.getWidth());
        data.addProperty("height", src.getHeight());
        data.addProperty("x", src.getLayoutX());
        data.addProperty("y", src.getLayoutY());
        data.addProperty("fontFamily", src.getFontFamily());
        data.addProperty("fontSize", src.getFontSize());
        data.addProperty("fillColor", src.getFillColor().toString());
        data.addProperty("textAlignment", src.getTextAlignment().toString());
        data.addProperty("isBold", src.getFontWeight() == FontWeight.BOLD);
        data.addProperty("isItalic", src.getFontPosture() == FontPosture.ITALIC);
        data.addProperty("isUnderline", src.isUnderline());
        data.addProperty("isStrikethrough", src.isStrikethrough());

        jsonObject.add("data", data);
        return jsonObject;
    }
}
