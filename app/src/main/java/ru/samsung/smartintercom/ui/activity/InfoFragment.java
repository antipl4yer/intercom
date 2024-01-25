package ru.samsung.smartintercom.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.textfield.TextInputEditText;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.framework.BaseFragmentDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.framework.ReactiveProperty;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.util.LoadStatus;

public class InfoFragment extends BaseFragmentDisposable {
    public static class Ctx {
        public AppState appState;
        public ReactiveCommand<Void> loadInfo;

        public ReactiveProperty<LoadStatus> takePhotoStatus;
    }

    private Ctx _ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    public void setCtx(Ctx ctx) {
        _ctx = ctx;

        View view = getView();

        Button loadInfoButton = view.findViewById(R.id.button_retry);
        loadInfoButton.setOnClickListener(v -> {
            _ctx.loadInfo.execute(null);
        });

        deferDispose(_ctx.appState.intercomModel.subscribe(value -> {
            TextInputEditText modelInput = view.findViewById(R.id.text_intercom_model);

            if (value.isEmpty()) {
                modelInput.setText(R.string.empty_intercom_model);
            } else {
                modelInput.setText(value);
            }
        }));

        deferDispose(_ctx.takePhotoStatus.subscribe(takePhotoStatus -> {
            Boolean isPhotoLoaderActive = takePhotoStatus == LoadStatus.LOADING;
            showPhotoLoading(isPhotoLoaderActive);
        }));
    }

    private void showPhotoLoading(Boolean value) {
        Class<? extends Fragment> fragmentClass = InfoImageFragment.class;
        if (value) {
            fragmentClass = LoadingFragment.class;
        }

        getChildFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.info_fragment_container_view, fragmentClass, null)
                .commit();
    }
}