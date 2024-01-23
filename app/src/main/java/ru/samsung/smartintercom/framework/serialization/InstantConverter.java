package ru.samsung.smartintercom.framework.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ru.samsung.smartintercom.framework.ReactiveProperty;

import java.lang.reflect.Type;
import java.time.Instant;

public class InstantConverter {
    public static void registerConverter(GsonBuilder builder) {
        Type typeOfContainer = new TypeToken<Instant>() {
        }.getType();

        JsonSerializer<Instant> serializer = new JsonSerializer<Instant>() {
            @Override
            public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
                return context.serialize(src.toString());
            }
        };
        builder.registerTypeAdapter(typeOfContainer, serializer);

        JsonDeserializer<Instant> deserializer = new JsonDeserializer<Instant>() {
            @Override
            public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return Instant.parse(context.deserialize(json, String.class));
            }
        };
        builder.registerTypeAdapter(typeOfContainer, deserializer);
    }
}