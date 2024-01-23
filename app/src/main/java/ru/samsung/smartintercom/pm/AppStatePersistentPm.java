package ru.samsung.smartintercom.pm;

import android.util.Log;
import ru.samsung.smartintercom.framework.BaseDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.framework.serialization.Json;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.util.MainThreadPeriodicTimer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class AppStatePersistentPm extends BaseDisposable {
    public static class Ctx {
        public AppState appState;
        public File appStateFile;
        public ReactiveCommand<Void> flushAppState;
    }

    private final static long SAVE_INTERVAL_MILLISECONDS = 5000;
    private final Ctx _ctx;

    public AppStatePersistentPm(Ctx ctx) {
        _ctx = ctx;

        MainThreadPeriodicTimer mainThreadPeriodicTimer = new MainThreadPeriodicTimer(this::saveToPersistentPath, SAVE_INTERVAL_MILLISECONDS);
        deferDispose(mainThreadPeriodicTimer);

        deferDispose(_ctx.flushAppState.subscribe(unused -> {
            saveToPersistentPath();
        }));
    }

    private void saveToPersistentPath() {
        Log.i("AppStatePersistentPm", String.format("saveToPersistentPath: %s", _ctx.appStateFile.getPath()));

        String serialisedData = Json.serialize(_ctx.appState);

        try (PrintWriter out = new PrintWriter(new FileWriter(_ctx.appStateFile))) {
            out.write(serialisedData);
        } catch (Exception e) {
            Log.e("AppStatePersistentPm", String.format("error while writing file to path: %s, error: %s", _ctx.appStateFile.getPath(), e.toString()));
        }
    }


}
