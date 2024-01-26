package ru.samsung.smartintercom.entity;

import android.content.Context;
import androidx.fragment.app.Fragment;
import ru.samsung.smartintercom.db.IntercomDatabase;
import ru.samsung.smartintercom.framework.BaseDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.pm.CallPm;
import ru.samsung.smartintercom.service.notification.SystemNotificationService;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.ui.activity.CallFragment;

public class CallEntity extends BaseDisposable {
    public static class Ctx {
        public Context appContext;
        public AppState appState;
        public IntercomDatabase database;
        public SystemNotificationService systemNotificationService;

        public ReactiveCommand<Fragment> onFragmentViewCreated;
        public ReactiveCommand<Integer> navigateToMenuItem;
        public ReactiveCommand<Boolean> setRemoteIsOpen;
        public ReactiveCommand<Void> onIncomingCall;
        public ReactiveCommand<Void> onMissedCall;
    }

    private final Ctx _ctx;

    public CallEntity(Ctx ctx) {
        _ctx = ctx;

        ReactiveCommand<Void> onAcceptedCall = ReactiveCommand.create();
        ReactiveCommand<Void> onDeclinedCall = ReactiveCommand.create();

        deferDispose(ctx.onFragmentViewCreated.subscribe(fragment -> {
            if (fragment instanceof CallFragment) {
                CallFragment.Ctx callFragmentCtx = new CallFragment.Ctx();
                callFragmentCtx.appState = _ctx.appState;
                callFragmentCtx.navigateToMenuItem = _ctx.navigateToMenuItem;
                callFragmentCtx.setRemoteIsOpen = _ctx.setRemoteIsOpen;
                callFragmentCtx.onMissedCall = _ctx.onMissedCall;
                callFragmentCtx.onAcceptedCall = onAcceptedCall;
                callFragmentCtx.onDeclinedCall = onDeclinedCall;
                callFragmentCtx.onIncomingCall = _ctx.onIncomingCall;

                ((CallFragment) fragment).setCtx(callFragmentCtx);
                return;
            }
        }));

        CallPm.Ctx callPmCtx = new CallPm.Ctx();
        callPmCtx.database = _ctx.database;
        callPmCtx.onDeclinedCall = onDeclinedCall;
        callPmCtx.onAcceptedCall = onAcceptedCall;
        callPmCtx.onMissedCall = _ctx.onMissedCall;
        callPmCtx.appContext = _ctx.appContext;
        callPmCtx.systemNotificationService = _ctx.systemNotificationService;

        CallPm callPm = new CallPm(callPmCtx);

        deferDispose(callPm);
    }
}
