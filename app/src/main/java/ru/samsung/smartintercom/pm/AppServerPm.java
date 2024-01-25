package ru.samsung.smartintercom.pm;

import android.util.Log;
import ru.samsung.smartintercom.framework.BaseDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.framework.ReactiveProperty;
import ru.samsung.smartintercom.service.http.server.AppServerService;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.util.LoadStatus;
import ru.samsung.smartintercom.util.MainThreadTimer;

import java.time.Instant;

public class AppServerPm extends BaseDisposable {
    public static class Ctx {
        public AppState appState;
        public AppServerService appServerService;
        public ReactiveCommand<Void> takeRemotePhoto;
        public ReactiveCommand<Boolean> setRemoteIsOpen;
        public ReactiveCommand<Void> loadInfo;

        public ReactiveProperty<LoadStatus> takePhotoStatus;
        public ReactiveProperty<LoadStatus> remoteActionStatus;
        public ReactiveProperty<LoadStatus> loadStatus;
        public ReactiveProperty<String> lastErrorDescription;
    }

    private final Ctx _ctx;

    public AppServerPm(Ctx ctx) {
        _ctx = ctx;

        _ctx.appServerService.setErrorHandler(errorDescription -> {
            _ctx.lastErrorDescription.setValue(errorDescription);
            return null;
        });

        _ctx.appServerService.setSuccessHandler(() -> {
            _ctx.lastErrorDescription.setValue("");
        });

        deferDispose(_ctx.loadInfo.subscribe(unused -> {
            tryConnect();
        }));

        deferDispose(_ctx.appState.isSettingsValid.skip(1).subscribe(isValid -> {
            if (!isValid) {
                Log.e("AppServerPm", "invalid settings");
                return;
            }

            Log.i("AppServerPm", "settings is valid try connect");
            tryConnect();
        }));

        deferDispose(_ctx.takeRemotePhoto.subscribe(unused -> {
            if (!_ctx.appState.isSettingsValid.getValue()) {
                Log.e("AppServerPm", "settings is not valid while try take photo");
                return;
            }

            _ctx.takePhotoStatus.setValue(LoadStatus.LOADING);

            _ctx.appServerService.getImage(bitmap -> {
                _ctx.appState.lastPhoto.setValue(bitmap);
                _ctx.takePhotoStatus.setValue(LoadStatus.SUCCESS);
                _ctx.appState.lastPhotoReceivedTime.setValue(Instant.now());

                return null;
            }, errorDescription -> {
                _ctx.takePhotoStatus.setValue(LoadStatus.FAIL);
                Log.e("AppServerPm", String.format("error occurred while getImage, description: %s", errorDescription));
                return null;
            });

        }));

        deferDispose(_ctx.setRemoteIsOpen.subscribe(value -> {
            AppServerService.CallAction callAction;
            if (value) {
                callAction = AppServerService.CallAction.OPEN;
            } else {
                callAction = AppServerService.CallAction.CLOSE;
            }

            _ctx.remoteActionStatus.setValue(LoadStatus.LOADING);
            _ctx.appServerService.call(callAction, () -> {
                _ctx.remoteActionStatus.setValue(LoadStatus.SUCCESS);
            }, errorDescription -> {
                _ctx.remoteActionStatus.setValue(LoadStatus.FAIL);
                Log.e("AppServerPm", String.format("error occurred while call, description: %s", errorDescription));
                return null;
            });
        }));
    }

    private void tryConnect() {
        if (!_ctx.appState.isSettingsValid.getValue()) {
            Log.e("AppServerPm", "invalid settings, skip connect");
            return;
        }

        AppServerService.DataHeaders dataHeaders = new AppServerService.DataHeaders();
        dataHeaders.house = _ctx.appState.houseNumber.getValue();
        dataHeaders.flat = _ctx.appState.flatNumber.getValue();

        _ctx.appServerService.setDataHeaders(dataHeaders);

        _ctx.loadStatus.setValue(LoadStatus.LOADING);
        _ctx.appServerService.getInfo(info -> {
            _ctx.lastErrorDescription.setValue("");
            _ctx.appState.intercomModel.setValue(info.model);
            _ctx.loadStatus.setValue(LoadStatus.SUCCESS);

            return null;
        }, errorDescription -> {
            _ctx.loadStatus.setValue(LoadStatus.FAIL);
            return null;
        });
    }
}
