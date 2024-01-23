package ru.samsung.smartintercom.framework.serialization;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ru.samsung.smartintercom.framework.ReactiveProperty;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;

public class ReactivePropertyBitmapConverter {
    public static void registerConverter(GsonBuilder builder) {
        Type typeOfContainer = new TypeToken<ReactiveProperty<Bitmap>>() {
        }.getType();

        JsonSerializer<ReactiveProperty<Bitmap>> serializer = new JsonSerializer<ReactiveProperty<Bitmap>>() {
            @Override
            public JsonElement serialize(ReactiveProperty<Bitmap> src, Type typeOfSrc, JsonSerializationContext context) {
                Bitmap bitmap = src.getValue();
                String serializedString = "";
                if (bitmap != null) {
                    serializedString = bitmapToString(bitmap);
                }

                return context.serialize(serializedString);
            }
        };
        builder.registerTypeAdapter(typeOfContainer, serializer);

        JsonDeserializer<ReactiveProperty<Bitmap>> deserializer = new JsonDeserializer<ReactiveProperty<Bitmap>>() {
            @Override
            public ReactiveProperty<Bitmap> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                ReactiveProperty<Bitmap> newReactiveProperty = ReactiveProperty.create();

                String deserializedString = context.deserialize(json, String.class);

                newReactiveProperty.setValue(stringToBitmap(deserializedString));

                return newReactiveProperty;
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