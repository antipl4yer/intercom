package ru.samsung.smartintercom.state;

import android.graphics.Bitmap;
import ru.samsung.smartintercom.framework.ReactiveProperty;

import java.time.Instant;

public class AppState {

    public ReactiveProperty<String> intercomModel;
    public ReactiveProperty<String> houseNumber;
    public ReactiveProperty<Integer> flatNumber;
    public ReactiveProperty<Boolean> isFirstRun;
    public ReactiveProperty<Bitmap> lastPhoto;
    public ReactiveProperty<Instant> lastPhotoReceivedTime;
    public ReactiveProperty<Instant> takePhotoTime;
    public ReactiveProperty<Instant> lastAppServerConnectTime;
    public ReactiveProperty<Instant> lastIncomingCallTime;
    public ReactiveProperty<Instant> lastReceivedCallTime;
    public ReactiveProperty<Boolean> isSettingsValid;
    public ReactiveProperty<Boolean> isIncomingChannelEstablished;

    public AppState(){

        intercomModel = ReactiveProperty.create();
        intercomModel.setValue("");

        houseNumber = ReactiveProperty.create();
        houseNumber.setValue("");

        flatNumber = ReactiveProperty.create();
        flatNumber.setValue(0);

        isFirstRun = ReactiveProperty.create();
        isFirstRun.setValue(true);

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

        isIncomingChannelEstablished = ReactiveProperty.create();
        isIncomingChannelEstablished.setValue(false);

        lastIncomingCallTime = ReactiveProperty.create();
        lastReceivedCallTime = ReactiveProperty.create();
    }
}
