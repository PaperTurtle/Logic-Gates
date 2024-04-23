package com.paperturtle;

import com.google.gson.JsonDeserializer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javafx.geometry.Point2D;

public class Point2DDeserializer implements JsonDeserializer<Point2D> {
    @Override
    public Point2D deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        return new Point2D(x, y);
    }
}
