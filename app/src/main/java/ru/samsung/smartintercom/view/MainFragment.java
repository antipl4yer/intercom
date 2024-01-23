package ru.samsung.smartintercom.view;

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
        public ReactiveProperty<Boolean> isAppLoaded;
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

        deferDispose(_ctx.loadStatus.subscribe(this::processLoadStatus));
        deferDispose(_ctx.remoteActionStatus.subscribe(this::processLoadStatus));

        _ctx.appState.isFirstRun.setValue(false);
        _ctx.flushAppState.execute(null);
    }

    private void processLoadStatus(LoadStatus loadStatus) {
        if (loadStatus == LoadStatus.LOADING) {
            setFragment(LoadingFragment.class);
        } else {
            showActualState();
        }
    }

    private void showActualState() {
        if (_ctx.appState.isFirstRun.getValue()) {
            setFragment(FirstRunFragment.class);
        } else {
            if (!_ctx.appState.isSettingsValid.getValue()) {
                _ctx.lastErrorDescription.setValue(getString(R.string.invalid_settings_text));
                setFragment(ErrorFragment.class);
                return;
            }

            if (_ctx.loadStatus.getValue() == LoadStatus.FAIL) {
                setFragment(ErrorFragment.class);
            } else {
                setFragment(InfoFragment.class);
            }
        }
    }

    private void setFragment(Class<? extends Fragment> fragmentClass) {
        if (_ctx.isAppLoaded.getValue()){
            if (!isVisible()){
                return;
            }
        }

        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.main_fragment_container_view, fragmentClass, null)
                .commit();
    }
}