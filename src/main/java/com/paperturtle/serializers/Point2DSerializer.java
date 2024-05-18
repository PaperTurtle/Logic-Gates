package com.paperturtle.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.geometry.Point2D;

import java.lang.reflect.Type;

/**
 * Serializer for Point2D objects to JSON.
 * Implements the JsonSerializer interface to provide custom serialization
 * logic.
 * 
 * @see JsonSerializer
 * @see com.google.gson.Gson
 * @see Point2DDeserializer
 * 
 * @author Seweryn Czabanowski
 */
public class Point2DSerializer implements JsonSerializer<Point2D> {
    /**
     * Serializes a Point2D object into a JSON element.
     * 
     * @param src       the Point2D object to serialize
     * @param typeOfSrc the type of the source Object
     * @param context   the context of the serialization
     * @return the serialized JSON element
     */
    @Override
    public JsonElement serialize(Point2D src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("x", src.getX());
        json.addProperty("y", src.getY());
        return json;
    }
}
