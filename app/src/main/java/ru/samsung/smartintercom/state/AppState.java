package ru.samsung.smartintercom.state;

import android.graphics.Bitmap;
import ru.samsung.smartintercom.framework.ReactiveProperty;

import java.time.Instant;

public class AppState {

    public final ReactiveProperty<String> intercomModel;
    public final ReactiveProperty<String> houseNumber;
    public final ReactiveProperty<String> flatNumber;
    public final ReactiveProperty<Bitmap> lastPhoto;
    public final ReactiveProperty<Instant> lastPhotoReceivedTime;
    public final ReactiveProperty<Instant> takePhotoTime;
    public final ReactiveProperty<Instant> lastAppServerConnectTime;
    public final ReactiveProperty<Instant> lastIncomingCallTime;
    public final ReactiveProperty<Instant> lastReceivedCallTime;
    public final ReactiveProperty<Boolean> isSettingsValid;
    public final ReactiveProperty<Boolean> isIncomingChannelEstablished;
    public final ReactiveProperty<Boolean> isFirstRun;

    public AppState(){

        intercomModel = ReactiveProperty.create();
        intercomModel.setValue("");

        houseNumber = ReactiveProperty.create();
        houseNumber.setValue("");

        flatNumber = ReactiveProperty.create();
        flatNumber.setValue("");

        lastPhoto = ReactiveProperty.create();

        Bitmap.Config bitmapConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(512, 512, bitmapConfig);
        lastPhoto.setValue(bitmap);

        lastPhotoReceivedTime = ReactiveProperty.create();

        takePhotoTime = ReactiveProperty.create();
        takePhotoTime.setValue(Instant.now());

        lastAppServerConnectTime = ReactiveProperty.create();
        lastAppServerConnectTime.setValue(Instant.now());

        isSettingsValid = ReactiveProperty.create();
        isSettingsValid.setValue(false);

        isFirstRun = ReactiveProperty.create();
        isFirstRun.setValue(true);

        isIncomingChannelEstablished = ReactiveProperty.create();
        isIncomingChannelEstablished.setValue(false);

        lastIncomingCallTime = ReactiveProperty.create();
        lastReceivedCallTime = ReactiveProperty.create();
    }
}
