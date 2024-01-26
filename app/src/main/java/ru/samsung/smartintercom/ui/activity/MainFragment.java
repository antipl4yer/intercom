package ru.samsung.smartintercom.ui.activity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.framework.BaseFragmentDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.framework.ReactiveProperty;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.util.LoadStatus;

public class MainFragment extends BaseFragmentDisposable {
    public static class Ctx {
        public AppState appState;
        public ReactiveCommand<Void> flushAppState;
        public ReactiveProperty<LoadStatus> remoteActionStatus;
        public ReactiveProperty<LoadStatus> loadStatus;
        public ReactiveProperty<String> lastErrorDescription;
        public ReactiveProperty<Boolean> isCurrentSettingsValid;
    }

    private Ctx _ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void setCtx(Ctx ctx) {
        _ctx = ctx;

        deferDispose(_ctx.loadStatus.skip(1).subscribe(loadStatus -> {
            showActualState();
        }));
        deferDispose(_ctx.remoteActionStatus.skip(1).subscribe(loadStatus -> {
            showActualState();
        }));

        showActualState();
    }

    private void showActualState() {
        LoadStatus loadStatus = _ctx.loadStatus.getValue();

        if (loadStatus == LoadStatus.LOADING) {
            setFragment(LoadingFragment.class);
            return;
        }

        if (loadStatus == LoadStatus.FAIL) {
            setFragment(ErrorFragment.class);
            return;
        }
 
        if (_ctx.appState.isFirstRun.getValue()) {
            setFragment(FirstRunFragment.class);
            return;
        }

        if (!_ctx.isCurrentSettingsValid.getValue()) {
            _ctx.lastErrorDescription.setValue(getString(R.string.invalid_settings_text));
            setFragment(ErrorFragment.class);
            return;
        }

        setFragment(InfoFragment.class);
    }

    private void setFragment(Class<? extends Fragment> fragmentClass) {
        getChildFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.main_fragment_container_view, fragmentClass, null)
                .commit();
    }
}