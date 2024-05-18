package com.paperturtle.serializers;

import com.google.gson.JsonDeserializer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javafx.geometry.Point2D;

/**
 * Deserializer for Point2D objects from JSON.
 * Implements the JsonDeserializer interface to provide custom deserialization
 * logic.
 * 
 * @see JsonDeserializer
 * @see com.google.gson.Gson
 * @see Point2DSerializer
 * 
 * @author Seweryn Czabanowski
 */
public class Point2DDeserializer implements JsonDeserializer<Point2D> {
    /**
     * Default constructor for Point2DDeserializer.
     */
    public Point2DDeserializer() {
    }

    /**
     * Deserializes a JSON element into a Point2D object.
     * 
     * @param json    the JSON element to deserialize
     * @param typeOfT the type of the Object to deserialize to
     * @param context the context of the deserialization
     * @return the deserialized Point2D object
     * @throws JsonParseException if the JSON is not in the expected format
     */
    @Override
    public Point2D deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        return new Point2D(x, y);
    }
}
