package ru.samsung.smartintercom.entity;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.framework.BaseDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.framework.ReactiveProperty;
import ru.samsung.smartintercom.service.notification.SystemNotificationService;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.ui.activity.*;
import ru.samsung.smartintercom.util.LoadStatus;

public class MainEntity extends BaseDisposable {
    public static class Ctx {
        public AppState appState;

        public SystemNotificationService systemNotificationService;
        public ReactiveCommand<Integer> navigateToMenuItem;

        public ReactiveCommand<Activity> onActivityStarted;
        public ReactiveCommand<Fragment> onFragmentViewCreated;
        public ReactiveCommand<Void> flushAppState;

        public ReactiveCommand<Void> onIncomingCall;
        public ReactiveCommand<Void> loadInfo;
        public ReactiveCommand<Void> takeRemotePhoto;

        public ReactiveProperty<LoadStatus> takePhotoStatus;
        public ReactiveProperty<LoadStatus> remoteActionStatus;
        public ReactiveProperty<LoadStatus> loadStatus;
        public ReactiveProperty<String> lastErrorDescription;
        public ReactiveCommand<Activity> onActivityResumed;
        public ReactiveProperty<Boolean> isCurrentSettingsValid;
    }

    private final Ctx _ctx;

    public MainEntity(Ctx ctx) {
        _ctx = ctx;

        deferDispose(_ctx.onIncomingCall.subscribe(unused -> {
            _ctx.navigateToMenuItem.execute(R.id.button_call);
        }));

        deferDispose(ctx.onActivityStarted.subscribe(activity -> {
            if (activity instanceof MainActivity) {
                MainActivity.Ctx mainActivityCtx = new MainActivity.Ctx();
                mainActivityCtx.navigateToMenuItem = _ctx.navigateToMenuItem;
                mainActivityCtx.systemNotificationService = _ctx.systemNotificationService;

                ((MainActivity) activity).setCtx(mainActivityCtx);
                return;
            }
        }));

        deferDispose(ctx.onFragmentViewCreated.subscribe(fragment -> {
            if (fragment instanceof MainFragment) {
                MainFragment.Ctx mainFragmentCtx = new MainFragment.Ctx();
                mainFragmentCtx.appState = _ctx.appState;
                mainFragmentCtx.flushAppState = _ctx.flushAppState;
                mainFragmentCtx.remoteActionStatus = _ctx.remoteActionStatus;
                mainFragmentCtx.loadStatus = _ctx.loadStatus;
                mainFragmentCtx.lastErrorDescription = _ctx.lastErrorDescription;
                mainFragmentCtx.isCurrentSettingsValid = _ctx.isCurrentSettingsValid;

                ((MainFragment) fragment).setCtx(mainFragmentCtx);
                return;
            }
            if (fragment instanceof FirstRunFragment) {
                FirstRunFragment.Ctx firstRunFragmentCtx = new FirstRunFragment.Ctx();
                firstRunFragmentCtx.navigateToMenuItem = _ctx.navigateToMenuItem;

                ((FirstRunFragment) fragment).setCtx(firstRunFragmentCtx);
                return;
            }
            if (fragment instanceof InfoFragment) {
                InfoFragment.Ctx infoFragmentCtx = new InfoFragment.Ctx();
                infoFragmentCtx.appState = _ctx.appState;
                infoFragmentCtx.loadInfo = _ctx.loadInfo;
                infoFragmentCtx.takePhotoStatus = _ctx.takePhotoStatus;

                ((InfoFragment) fragment).setCtx(infoFragmentCtx);
                return;
            }
            if (fragment instanceof InfoImageFragment) {
                InfoImageFragment.Ctx infoImageFragmentCtx = new InfoImageFragment.Ctx();
                infoImageFragmentCtx.appState = _ctx.appState;
                infoImageFragmentCtx.takeRemotePhoto = _ctx.takeRemotePhoto;
                infoImageFragmentCtx.takePhotoStatus = _ctx.takePhotoStatus;
                infoImageFragmentCtx.lastErrorDescription = _ctx.lastErrorDescription;

                ((InfoImageFragment) fragment).setCtx(infoImageFragmentCtx);
                return;
            }
            if (fragment instanceof ErrorFragment) {
                ErrorFragment.Ctx errorFragmentCtx = new ErrorFragment.Ctx();
                errorFragmentCtx.appState = _ctx.appState;
                errorFragmentCtx.loadInfo = _ctx.loadInfo;
                errorFragmentCtx.lastErrorDescription = _ctx.lastErrorDescription;

                ((ErrorFragment) fragment).setCtx(errorFragmentCtx);
                return;
            }
        }));
    }
}
