package com.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.geometry.Point2D;

import java.lang.reflect.Type;

public class Point2DSerializer implements JsonSerializer<Point2D> {
    @Override
    public JsonElement serialize(Point2D src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("x", src.getX());
        json.addProperty("y", src.getY());
        return json;
    }
}
