package ru.samsung.smartintercom.framework.serialization;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ru.samsung.smartintercom.framework.ReactiveProperty;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.time.Instant;

public class BitmapConverter {
    public static void registerConverter(GsonBuilder builder) {
        Type typeOfContainer = new TypeToken<Bitmap>() {
        }.getType();

        JsonSerializer<Bitmap> serializer = new JsonSerializer<Bitmap>() {
            @Override
            public JsonElement serialize(Bitmap src, Type typeOfSrc, JsonSerializationContext context) {
                String serializedString = "";
                if (src != null) {
                    serializedString = bitmapToString(src);
                }

                return context.serialize(serializedString);
            }
        };
        builder.registerTypeAdapter(typeOfContainer, serializer);

        JsonDeserializer<Bitmap> deserializer = new JsonDeserializer<Bitmap>() {
            @Override
            public Bitmap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                String deserializedString = context.deserialize(json, String.class);
                return stringToBitmap(deserializedString);
            }
        };
        builder.registerTypeAdapter(typeOfContainer, deserializer);
    }

    private static String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private static Bitmap stringToBitmap(String value) {
        byte[] byteArray = Base64.decode(value, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}