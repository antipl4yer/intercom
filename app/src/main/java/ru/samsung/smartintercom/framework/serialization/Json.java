package ru.samsung.smartintercom.framework.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ru.samsung.smartintercom.framework.ReactiveProperty;
import ru.samsung.smartintercom.util.LoadStatus;

import java.time.Instant;

public class Json {
    private static GsonBuilder _gsonBuilder;

    public static void setup() {
        _gsonBuilder = new GsonBuilder();

        registerConverters();
    }

    private static void registerConverters() {
        InstantConverter.registerConverter(_gsonBuilder);

        ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<String>>() {
        });
        ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<Number>>() {
        });
        ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<Boolean>>() {
        });
        ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<Integer>>() {
        });
        ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<LoadStatus>>() {
        });
        ReactivePropertyConverter.registerConverter(_gsonBuilder, new TypeToken<ReactiveProperty<Instant>>() {
        });

        ReactivePropertyBitmapConverter.registerConverter(_gsonBuilder);
    }

    public static String serialize(Object object) {
        Gson gson = _gsonBuilder.create();
        return gson.toJson(object);
    }

    public static <T> T deserialize(String json, Class<T> toClass) {
        Gson gson = _gsonBuilder.create();
        return gson.fromJson(json, toClass);
    }
}
