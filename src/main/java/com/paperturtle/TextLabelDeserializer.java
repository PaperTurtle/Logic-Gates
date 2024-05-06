package com.paperturtle;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import javafx.scene.text.TextAlignment;

public class TextLabelDeserializer implements JsonDeserializer<TextLabel> {
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
