package ru.samsung.smartintercom.entity;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.framework.BaseDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.framework.ReactiveProperty;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.util.LoadStatus;
import ru.samsung.smartintercom.view.*;

public class MainEntity extends BaseDisposable {
    public static class Ctx {
        public AppState appState;

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
        public ReactiveProperty<Boolean> isAppLoaded;
        public ReactiveCommand<Activity> onActivityResumed;
    }

    private final Ctx _ctx;

    public MainEntity(Ctx ctx) {
        _ctx = ctx;

        deferDispose(_ctx.onActivityResumed.subscribe(activity -> {
            if (activity instanceof MainActivity) {
                _ctx.isAppLoaded.setValue(true);
                return;
            }
        }));

        deferDispose(_ctx.onIncomingCall.subscribe(unused -> {
            _ctx.navigateToMenuItem.execute(R.id.button_call);
        }));

        deferDispose(ctx.onActivityStarted.subscribe(activity -> {
            if (activity instanceof MainActivity) {
                MainActivity.Ctx mainActivityCtx = new MainActivity.Ctx();
                mainActivityCtx.navigateToMenuItem = _ctx.navigateToMenuItem;

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
                mainFragmentCtx.isAppLoaded = _ctx.isAppLoaded;

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
                infoFragmentCtx.isAppLoaded = _ctx.isAppLoaded;

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