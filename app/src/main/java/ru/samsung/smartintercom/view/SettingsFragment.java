package ru.samsung.smartintercom.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.framework.BaseFragmentDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.util.Converter;
import ru.samsung.smartintercom.util.HouseNumberValidator;

public class SettingsFragment extends BaseFragmentDisposable {

    public static class Ctx {
        public AppState appState;
        public ReactiveCommand<Void> flushAppState;
        public ReactiveCommand<Integer> navigateToMenuItem;
    }

    private Ctx _ctx;
    private TextInputEditText _settingsFlatInput;
    private TextInputEditText _settingsHouseInput;
    private TextView _settingsHouseHint;
    private Button _saveButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void setCtx(Ctx ctx) {
        _ctx = ctx;

        View view = getView();

        _settingsFlatInput = view.findViewById(R.id.input_flat);
        _settingsHouseInput = view.findViewById(R.id.input_house);
        _settingsHouseHint = view.findViewById(R.id.input_house_hint);

        _saveButton = view.findViewById(R.id.button_save);
        _settingsFlatInput.setText(Converter.convertStringToCharSequence(_ctx.appState.flatNumber.getValue().toString()));
        _settingsHouseInput.setText(Converter.convertStringToCharSequence(_ctx.appState.houseNumber.getValue()));

        _settingsHouseInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateInput();
            }
        });

        _settingsFlatInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateInput();
            }
        });

        _saveButton.setOnClickListener(v -> {
            String houseNumber = Converter.convertCharSequenceToString(_settingsHouseInput.getText().toString());
            Integer flatNumber = Converter.convertCharSequenceToInteger(_settingsFlatInput.getText().toString());

            _ctx.navigateToMenuItem.execute(R.id.button_main);

            _ctx.appState.flatNumber.setValue(flatNumber);
            _ctx.appState.houseNumber.setValue(houseNumber);
            _ctx.appState.isSettingsValid.setValue(true);
            _ctx.flushAppState.execute(null);
        });
    }

    private void validateInput() {
        _settingsHouseHint.setText("");

        String houseNumber = Converter.convertCharSequenceToString(_settingsHouseInput.getText().toString());
        String flatNumber = Converter.convertCharSequenceToString(_settingsFlatInput.getText().toString());
        if (flatNumber.isEmpty()){
            _settingsHouseHint.setText(R.string.settings_flat_number_hint);
            _saveButton.setEnabled(false);
            return;
        }

        if (!HouseNumberValidator.isValid(houseNumber)) {
            _saveButton.setEnabled(false);
            _settingsHouseHint.setText(R.string.settings_house_number_hint);
            return;
        }

        _saveButton.setEnabled(true);
    }
}