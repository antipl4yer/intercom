package ru.samsung.smartintercom.pm;

import android.util.Log;
import ru.samsung.smartintercom.framework.BaseDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.service.socket.server.SocketServerWrapperService;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.util.MainThreadPeriodicTimer;
import ru.samsung.smartintercom.util.MainThreadTimer;
import io.reactivex.rxjava3.disposables.Disposable;
import ru.samsung.smartintercom.data.call.CallDataSource;

import java.time.Instant;

public class SocketServerPm extends BaseDisposable {
    public static class Ctx {
        public AppState appState;
        public SocketServerWrapperService socketServerWrapperService;
        public ReactiveCommand<Void> reconnectSocketServer;
        public ReactiveCommand<Void> onIncomingCall;
        public ReactiveCommand<Void> onMissedCall;
    }

    private final Ctx _ctx;

    private SocketServerWrapperService.Data _missedCallMonitorData;
    private Disposable _missedCallTimeoutTimer;
    private Disposable _missedCallMonitorTimer;

    private static final long MISSED_CALL_TIMEOUT_SECONDS = 15;
    private static final long MISSED_CALL_MONITOR_INTERVAL_MILLISECONDS = 2000;

    public SocketServerPm(Ctx ctx) {
        _ctx = ctx;

        _ctx.socketServerWrapperService.registerIncomingCallHandler(() -> {
            _missedCallMonitorData = new SocketServerWrapperService.Data();
            _missedCallMonitorData.house = _ctx.appState.houseNumber.getValue();
            _missedCallMonitorData.flat = _ctx.appState.flatNumber.getValue();

            _missedCallTimeoutTimer = new MainThreadTimer(() -> {
                SocketServerWrapperService.Data data = new SocketServerWrapperService.Data();
                data.house = _ctx.appState.houseNumber.getValue();
                data.flat = _ctx.appState.flatNumber.getValue();

                if (!_missedCallMonitorData.equals(data)){
                    Log.e("SocketServerPm", "not our missed call, data has changed");
                    return;
                }

                _ctx.onMissedCall.execute(null);
            }, MISSED_CALL_TIMEOUT_SECONDS * 1000);

            _missedCallMonitorTimer = new MainThreadPeriodicTimer(() -> {
                Instant lastIncomingCallTime = _ctx.appState.lastIncomingCallTime.getValue();
                if (lastIncomingCallTime == null) {
                    return;
                }

                Instant lastReceivedCallTime = _ctx.appState.lastReceivedCallTime.getValue();
                if (lastReceivedCallTime == null) {
                    return;
                }

                long secondsElapsedFromCallStart = lastReceivedCallTime.getEpochSecond() - lastIncomingCallTime.getEpochSecond();
                if (secondsElapsedFromCallStart > 0 && secondsElapsedFromCallStart < MISSED_CALL_TIMEOUT_SECONDS) {
                    disposeMissedCallTimers();
                }
            }, MISSED_CALL_MONITOR_INTERVAL_MILLISECONDS);


            _ctx.appState.lastIncomingCallTime.setValue(Instant.now());
            _ctx.onIncomingCall.execute(null);
        });

        _ctx.socketServerWrapperService.registerChangedStatusHandler(status -> {
            _ctx.appState.isIncomingChannelEstablished.setValue(status instanceof CallDataSource.Status.Connect);
            return null;
        });

        deferDispose(_ctx.reconnectSocketServer.subscribe(isValid -> {
            tryConnect();
        }));

        tryConnect();
    }

    @Override
    public void dispose() {
        disposeMissedCallTimers();

        super.dispose();
    }

    private void tryConnect(){
        if (!_ctx.appState.isSettingsValid.getValue()) {
            return;
        }

        SocketServerWrapperService.Data data = new SocketServerWrapperService.Data();
        data.house = _ctx.appState.houseNumber.getValue();
        data.flat = _ctx.appState.flatNumber.getValue();

        _ctx.socketServerWrapperService.setData(data);
    }

    private void disposeMissedCallTimers(){
        if (_missedCallMonitorTimer != null) {
            _missedCallMonitorTimer.dispose();
            _missedCallMonitorTimer = null;
        }

        if (_missedCallTimeoutTimer != null) {
            _missedCallTimeoutTimer.dispose();
            _missedCallTimeoutTimer = null;
        }
    }
}
