package ru.samsung.smartintercom;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.fragment.app.Fragment;
import ru.samsung.smartintercom.db.IntercomDatabase;
import ru.samsung.smartintercom.entity.CallEntity;
import ru.samsung.smartintercom.entity.CallsHistoryEntity;
import ru.samsung.smartintercom.entity.MainEntity;
import ru.samsung.smartintercom.entity.SettingsEntity;
import ru.samsung.smartintercom.framework.AppProxy;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.framework.ReactiveProperty;
import ru.samsung.smartintercom.framework.serialization.Json;
import ru.samsung.smartintercom.pm.AppServerPm;
import ru.samsung.smartintercom.pm.AppStatePersistentPm;
import ru.samsung.smartintercom.pm.SocketServerPm;
import ru.samsung.smartintercom.service.http.server.AppServerService;
import ru.samsung.smartintercom.service.notification.SystemNotificationService;
import ru.samsung.smartintercom.service.socket.server.SocketServerWrapperService;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.util.LoadStatus;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import ru.samsung.smartintercom.core.CoreConstants;
import ru.samsung.smartintercom.util.MainThreadTimer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RootEntity extends Application {
    private CompositeDisposable _disposables;
    private File _appStateFile;

    private static final String appStateFileName = "app_state.json";

    @Override
    public void onCreate() {
        super.onCreate();

        Context appContext = this.getApplicationContext();

        _disposables = new CompositeDisposable();

        SystemNotificationService systemNotificationService = new SystemNotificationService(appContext);

        Json.setup();

        IntercomDatabase database = IntercomDatabase.getDatabase(this.getApplicationContext());

        _appStateFile = Paths.get(getApplicationContext().getFilesDir().getAbsolutePath(), appStateFileName).toFile();

        AppState appState = getAppState();

        ReactiveCommand<Void> flushAppState = ReactiveCommand.create();

        AppStatePersistentPm.Ctx appStatePersistentPmCtx = new AppStatePersistentPm.Ctx();
        appStatePersistentPmCtx.appState = appState;
        appStatePersistentPmCtx.appStateFile = _appStateFile;
        appStatePersistentPmCtx.flushAppState = flushAppState;

        AppStatePersistentPm appStatePersistentPm = new AppStatePersistentPm(appStatePersistentPmCtx);
        _disposables.add(appStatePersistentPm);

        ReactiveCommand<Activity> onActivityStarted = ReactiveCommand.create();
        ReactiveCommand<Activity> onActivityResumed = ReactiveCommand.create();
        ReactiveCommand<Fragment> onFragmentViewCreated = ReactiveCommand.create();

        AppProxy.Ctx appProxyContext = new AppProxy.Ctx();
        appProxyContext.application = this;
        appProxyContext.onActivityStarted = onActivityStarted;
        appProxyContext.onFragmentViewCreated = onFragmentViewCreated;
        appProxyContext.onActivityResumed = onActivityResumed;

        AppProxy app = new AppProxy(appProxyContext);
        _disposables.add(app);

        ReactiveCommand<Void> onIncomingCall = ReactiveCommand.create();
        ReactiveCommand<Void> onMissedCall = ReactiveCommand.create();
        ReactiveCommand<Void> reconnectSocketServer = ReactiveCommand.create();

        SocketServerWrapperService socketServerWrapperService = new SocketServerWrapperService();

        SocketServerPm.Ctx socketServerPmCtx = new SocketServerPm.Ctx();
        socketServerPmCtx.onIncomingCall = onIncomingCall;
        socketServerPmCtx.appState = appState;
        socketServerPmCtx.socketServerWrapperService = socketServerWrapperService;
        socketServerPmCtx.reconnectSocketServer = reconnectSocketServer;
        socketServerPmCtx.onMissedCall = onMissedCall;

        SocketServerPm socketServerPm = new SocketServerPm(socketServerPmCtx);
        _disposables.add(socketServerPm);

        ReactiveCommand<Void> takeRemotePhoto = ReactiveCommand.create();
        ReactiveCommand<Boolean> setRemoteIsOpen = ReactiveCommand.create();
        ReactiveCommand<Void> reconnectAppServer = ReactiveCommand.create();

        ReactiveProperty<LoadStatus> takePhotoStatus = ReactiveProperty.create();
        takePhotoStatus.setValue(LoadStatus.NEVER);

        ReactiveProperty<LoadStatus> remoteActionStatus = ReactiveProperty.create();
        remoteActionStatus.setValue(LoadStatus.NEVER);

        ReactiveProperty<LoadStatus> loadStatus = ReactiveProperty.create();
        loadStatus.setValue(LoadStatus.NEVER);

        ReactiveProperty<String> lastErrorDescription = ReactiveProperty.create();
        lastErrorDescription.setValue("");

        AppServerService.Ctx appServerServiceCtx = new AppServerService.Ctx();
        appServerServiceCtx.appContext = appContext;
        appServerServiceCtx.endpoint = CoreConstants.HOST;
        appServerServiceCtx.flatHeaderName = CoreConstants.HEADER_FLAT;
        appServerServiceCtx.houseHeaderName = CoreConstants.HEADER_HOUSE;

        AppServerService appServerService = new AppServerService(appServerServiceCtx);

        AppServerPm.Ctx appServerPmCtx = new AppServerPm.Ctx();
        appServerPmCtx.appState = appState;
        appServerPmCtx.appServerService = appServerService;
        appServerPmCtx.takeRemotePhoto = takeRemotePhoto;
        appServerPmCtx.setRemoteIsOpen = setRemoteIsOpen;
        appServerPmCtx.reconnectAppServer = reconnectAppServer;
        appServerPmCtx.takePhotoStatus = takePhotoStatus;
        appServerPmCtx.remoteActionStatus = remoteActionStatus;
        appServerPmCtx.loadStatus = loadStatus;
        appServerPmCtx.lastErrorDescription = lastErrorDescription;

        AppServerPm appServerPm = new AppServerPm(appServerPmCtx);
        _disposables.add(appServerPm);

        ReactiveCommand<Integer> navigateToMenuItem = ReactiveCommand.create();
        ReactiveProperty<Boolean> isCurrentSettingsValid = ReactiveProperty.create();
        isCurrentSettingsValid.setValue(true);

        MainEntity.Ctx mainEntityContext = new MainEntity.Ctx();
        mainEntityContext.appContext = appContext;
        mainEntityContext.flushAppState = flushAppState;
        mainEntityContext.appState = appState;
        mainEntityContext.navigateToMenuItem = navigateToMenuItem;
        mainEntityContext.takeRemotePhoto = takeRemotePhoto;
        mainEntityContext.takePhotoStatus = takePhotoStatus;
        mainEntityContext.remoteActionStatus = remoteActionStatus;
        mainEntityContext.loadStatus = loadStatus;
        mainEntityContext.lastErrorDescription = lastErrorDescription;
        mainEntityContext.isCurrentSettingsValid = isCurrentSettingsValid;
        mainEntityContext.systemNotificationService = systemNotificationService;
        mainEntityContext.reconnectAppServer = reconnectAppServer;
        mainEntityContext.onActivityStarted = onActivityStarted;
        mainEntityContext.onFragmentViewCreated = onFragmentViewCreated;
        mainEntityContext.onIncomingCall = onIncomingCall;
        mainEntityContext.onActivityResumed = onActivityResumed;

        MainEntity mainEntity = new MainEntity(mainEntityContext);
        _disposables.add(mainEntity);

        CallEntity.Ctx callEntityCtx = new CallEntity.Ctx();
        callEntityCtx.appContext = appContext;
        callEntityCtx.database = database;
        callEntityCtx.appState = appState;
        callEntityCtx.navigateToMenuItem = navigateToMenuItem;
        callEntityCtx.setRemoteIsOpen = setRemoteIsOpen;
        callEntityCtx.systemNotificationService = systemNotificationService;
        callEntityCtx.onFragmentViewCreated = onFragmentViewCreated;
        callEntityCtx.onIncomingCall = onIncomingCall;
        callEntityCtx.onMissedCall = onMissedCall;

        CallEntity callEntity = new CallEntity(callEntityCtx);
        _disposables.add(callEntity);

        SettingsEntity.Ctx settingsEntityContext = new SettingsEntity.Ctx();
        settingsEntityContext.flushAppState = flushAppState;
        settingsEntityContext.appState = appState;
        settingsEntityContext.navigateToMenuItem = navigateToMenuItem;
        settingsEntityContext.isCurrentSettingsValid = isCurrentSettingsValid;
        settingsEntityContext.reconnectAppServer = reconnectAppServer;
        settingsEntityContext.reconnectSocketServer = reconnectSocketServer;
        settingsEntityContext.onFragmentViewCreated = onFragmentViewCreated;
        settingsEntityContext.onActivityStarted = onActivityStarted;

        SettingsEntity settingsEntity = new SettingsEntity(settingsEntityContext);
        _disposables.add(settingsEntity);

        CallsHistoryEntity.Ctx callsHistoryEntityCtx = new CallsHistoryEntity.Ctx();
        callsHistoryEntityCtx.database = database;
        callsHistoryEntityCtx.onFragmentViewCreated = onFragmentViewCreated;
        callsHistoryEntityCtx.onMissedCall = onMissedCall;

        CallsHistoryEntity callsHistoryEntity = new CallsHistoryEntity(callsHistoryEntityCtx);
        _disposables.add(callsHistoryEntity);
    }

    @Override
    public void onTerminate() {
        _disposables.dispose();
        _disposables = null;
        super.onTerminate();
    }

    private AppState getAppState() {
        if (!_appStateFile.exists()) {
            return new AppState();
        }

        try {
            String jsonData = new String(Files.readAllBytes(_appStateFile.toPath()));
            if (jsonData.isEmpty()) {
                return new AppState();
            }
            return Json.deserialize(jsonData, AppState.class);
        } catch (Exception ex) {
            Log.e("AppRoot", ex.toString());
        }

        return new AppState();
    }
}
