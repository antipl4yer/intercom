package ru.samsung.smartintercom.framework.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ru.samsung.smartintercom.framework.ReactiveProperty;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReactivePropertyConverter {
    public static void registerConverter(GsonBuilder builder, TypeToken<?> typeToken) {

        Type typeOfContainer = typeToken.getType();
        Type[] parametersType = ((ParameterizedType) typeOfContainer).getActualTypeArguments();
        if (parametersType.length != 1) {
            throw new IllegalArgumentException("Invalid parameters length, expected 1");
        }
        Type parameterType = parametersType[0];

        JsonSerializer<ReactiveProperty<?>> serializer = new JsonSerializer<ReactiveProperty<?>>() {
            @Override
            public JsonElement serialize(ReactiveProperty<?> src, Type typeOfSrc, JsonSerializationContext context) {
                return context.serialize(src.getValue());
            }
        };
        builder.registerTypeAdapter(typeOfContainer, serializer);

        JsonDeserializer<ReactiveProperty<?>> deserializer = new JsonDeserializer<ReactiveProperty<?>>() {
            @Override
            public ReactiveProperty<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                ReactiveProperty<?> newReactiveProperty = ReactiveProperty.create();
                newReactiveProperty.setValue(context.deserialize(json, parameterType));

                return newReactiveProperty;
            }
        };
        builder.registerTypeAdapter(typeOfContainer, deserializer);
    }
}