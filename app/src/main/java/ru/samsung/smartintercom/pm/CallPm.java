package ru.samsung.smartintercom.pm;

import android.content.Context;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.db.CallHistory;
import ru.samsung.smartintercom.db.IntercomDatabase;
import ru.samsung.smartintercom.framework.BaseDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.service.notification.SystemNotificationService;
import ru.samsung.smartintercom.util.Converter;

import java.time.Instant;

public class CallPm extends BaseDisposable {
    public static class Ctx {
        public Context appContext;
        public IntercomDatabase database;
        public SystemNotificationService systemNotificationService;
        public ReactiveCommand<Void> onMissedCall;
        public ReactiveCommand<Void> onAcceptedCall;
        public ReactiveCommand<Void> onDeclinedCall;
    }

    private final Ctx _ctx;

    public CallPm(Ctx ctx) {
        _ctx = ctx;

        String acceptedCallStatusText = _ctx.appContext.getString(R.string.accepted_call_status_text);
        String declinedCallStatusText = _ctx.appContext.getString(R.string.declined_call_status_text);
        String missedCallStatusText = _ctx.appContext.getString(R.string.missed_call_status_text);

        deferDispose(_ctx.onMissedCall.subscribe(unused -> {
            insertDataToHistory(missedCallStatusText);

            _ctx.systemNotificationService.send(
                    _ctx.appContext.getString(R.string.missed_call_notification_title),
                    _ctx.appContext.getString(R.string.missed_call_notification_text)
            );
        }));
        deferDispose(_ctx.onAcceptedCall.subscribe(unused -> {
            insertDataToHistory(acceptedCallStatusText);
        }));
        deferDispose(_ctx.onDeclinedCall.subscribe(unused -> {
            insertDataToHistory(declinedCallStatusText);
        }));
    }

    private void insertDataToHistory(String statusText) {
        Instant now = Instant.now();

        CallHistory history = new CallHistory();
        history.status = statusText;
        history.date = Converter.convertInstantToString(now);
        history.dateStamp = now.getEpochSecond();

        _ctx.database.callHistoryDao().insert(history);
    }
}
