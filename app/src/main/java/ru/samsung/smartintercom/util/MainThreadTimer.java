package ru.samsung.smartintercom.util;

import android.os.Handler;
import android.os.Looper;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.Timer;
import java.util.TimerTask;

public class MainThreadTimer implements Disposable {
    private Boolean _isDisposed;
    private final Timer _timer;

    public MainThreadTimer(Runnable callback, long delay) {
        _isDisposed = false;

        _timer = new Timer();

        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.run();
                    }
                });
            }
        }, delay);
    }

    @Override
    public void dispose() {
        _isDisposed = true;

        _timer.cancel();
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }
}
