package ru.samsung.smartintercom.entity;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import ru.samsung.smartintercom.framework.BaseDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.view.*;

public class SettingsEntity extends BaseDisposable {
    public static class Ctx {
        public AppState appState;
        public ReactiveCommand<Integer> navigateToMenuItem;

        public ReactiveCommand<Activity> onActivityStarted;
        public ReactiveCommand<Fragment> onFragmentViewCreated;
        public ReactiveCommand<Void> flushAppState;
    }

    private final Ctx _ctx;

    public SettingsEntity(Ctx ctx) {
        _ctx = ctx;

        deferDispose(ctx.onFragmentViewCreated.subscribe(fragment -> {
            if (fragment instanceof SettingsFragment) {
                SettingsFragment.Ctx settingsFragmentCtx = new SettingsFragment.Ctx();
                settingsFragmentCtx.appState = _ctx.appState;
                settingsFragmentCtx.flushAppState = _ctx.flushAppState;
                settingsFragmentCtx.navigateToMenuItem = _ctx.navigateToMenuItem;

                ((SettingsFragment) fragment).setCtx(settingsFragmentCtx);
                return;
            }
        }));
    }
}
