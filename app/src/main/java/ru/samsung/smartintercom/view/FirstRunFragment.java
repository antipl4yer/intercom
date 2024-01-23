package ru.samsung.smartintercom.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.framework.BaseFragmentDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;

public class FirstRunFragment extends BaseFragmentDisposable {
    public static class Ctx {
        public ReactiveCommand<Integer> navigateToMenuItem;
    }

    private Ctx _ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_first_run, container, false);
    }

    public void setCtx(Ctx ctx){
        _ctx = ctx;

        View view = getView();

        Button settingsButton = view.findViewById(R.id.button_start);
        settingsButton.setOnClickListener(v -> {
            _ctx.navigateToMenuItem.execute(R.id.button_settings);
        });
    }
}