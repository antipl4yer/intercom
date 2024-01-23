package ru.samsung.smartintercom.util;

import android.os.Handler;
import android.os.Looper;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.Timer;
import java.util.TimerTask;

public class MainThreadPeriodicTimer implements Disposable {
    private Boolean _isDisposed;
    private final Timer _timer;

    public MainThreadPeriodicTimer(Runnable callback, long period) {
        _isDisposed = false;

        _timer = new Timer();

        _timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.run();
                    }
                });
            }
        }, period, period);
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
