package ru.samsung.smartintercom.entity;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import ru.samsung.smartintercom.framework.BaseDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.framework.ReactiveProperty;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.ui.activity.SettingsFragment;

public class SettingsEntity extends BaseDisposable {
    public static class Ctx {
        public AppState appState;
        public ReactiveCommand<Integer> navigateToMenuItem;
        public ReactiveCommand<Void> flushAppState;
        public ReactiveCommand<Void> reconnectSocketServer;
        public ReactiveCommand<Void> reconnectAppServer;
        public ReactiveProperty<Boolean> isCurrentSettingsValid;
        public ReactiveCommand<Activity> onActivityStarted;
        public ReactiveCommand<Fragment> onFragmentViewCreated;
    }

    private final Ctx _ctx;

    public SettingsEntity(Ctx ctx) {
        _ctx = ctx;

        deferDispose(_ctx.onFragmentViewCreated.subscribe(fragment -> {
            if (fragment instanceof SettingsFragment) {
                SettingsFragment.Ctx settingsFragmentCtx = new SettingsFragment.Ctx();
                settingsFragmentCtx.appState = _ctx.appState;
                settingsFragmentCtx.flushAppState = _ctx.flushAppState;
                settingsFragmentCtx.navigateToMenuItem = _ctx.navigateToMenuItem;
                settingsFragmentCtx.isCurrentSettingsValid = _ctx.isCurrentSettingsValid;
                settingsFragmentCtx.reconnectAppServer = _ctx.reconnectAppServer;
                settingsFragmentCtx.reconnectSocketServer = _ctx.reconnectSocketServer;

                ((SettingsFragment) fragment).setCtx(settingsFragmentCtx);
                return;
            }
        }));
    }
}
