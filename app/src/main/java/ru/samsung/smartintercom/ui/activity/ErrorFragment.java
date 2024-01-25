package ru.samsung.smartintercom.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.framework.BaseFragmentDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.framework.ReactiveProperty;
import ru.samsung.smartintercom.state.AppState;

public class ErrorFragment extends BaseFragmentDisposable {
    public static class Ctx {
        public AppState appState;
        public ReactiveCommand<Void> loadInfo;

        public ReactiveProperty<String> lastErrorDescription;
    }

    private Ctx _ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_error, container, false);
    }

    public void setCtx(Ctx ctx){
        _ctx = ctx;

        View view = getView();

        TextView errorDescription = view.findViewById(R.id.error_description);
        Button retryButton = view.findViewById(R.id.button_retry);

        retryButton.setOnClickListener(v -> {
            _ctx.loadInfo.execute(null);
        });

        deferDispose(_ctx.lastErrorDescription.subscribe(message -> {
            errorDescription.setText(_ctx.lastErrorDescription.getValue());
        }));
    }
}