package com.paperturtle;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TextLabelSerializer implements JsonSerializer<TextLabel> {
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

        jsonObject.add("data", data);
        return jsonObject;
    }
}
