package ru.samsung.smartintercom.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.framework.BaseFragmentDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.framework.ReactiveProperty;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.util.Converter;
import ru.samsung.smartintercom.util.FlatNumberValidator;
import ru.samsung.smartintercom.util.HouseNumberValidator;

public class SettingsFragment extends BaseFragmentDisposable {

    public static class Ctx {
        public AppState appState;
        public ReactiveCommand<Void> flushAppState;
        public ReactiveProperty<Boolean> isCurrentSettingsValid;
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

        _settingsHouseInput.addTextChangedListener(HouseNumberValidator.attachValidator(_settingsHouseInput, this::validateInput));
        _settingsFlatInput.addTextChangedListener(FlatNumberValidator.attachValidator(_settingsFlatInput, this::validateInput));

        _saveButton.setOnClickListener(v -> {
            String houseNumber = Converter.convertCharSequenceToString(_settingsHouseInput.getText().toString());
            String flatNumber = Converter.convertCharSequenceToString(_settingsFlatInput.getText().toString());

            //_ctx.navigateToMenuItem.execute(R.id.button_main);

            _ctx.appState.flatNumber.setValue(flatNumber);
            _ctx.appState.houseNumber.setValue(houseNumber);
            _ctx.appState.isSettingsValid.setValue(true);
            _ctx.appState.isFirstRun.setValue(false);
            _ctx.flushAppState.execute(null);

            _saveButton.setEnabled(false);
        });

        validateInput();
    }

    private void validateInput() {
        _ctx.isCurrentSettingsValid.setValue(false);
        _settingsHouseHint.setText("");

        String houseNumber = Converter.convertCharSequenceToString(_settingsHouseInput.getText().toString());
        String flatNumber = Converter.convertCharSequenceToString(_settingsFlatInput.getText().toString());
        if (!FlatNumberValidator.isValid(flatNumber)) {
            _settingsHouseHint.setText(R.string.settings_flat_number_hint);
            _saveButton.setEnabled(false);
            return;
        }

        if (!HouseNumberValidator.isValid(houseNumber)) {
            _saveButton.setEnabled(false);
            _settingsHouseHint.setText(R.string.settings_house_number_hint);
            return;
        }

        _ctx.isCurrentSettingsValid.setValue(true);

        if (houseNumber.equals(_ctx.appState.houseNumber.getValue()) && flatNumber.equals(_ctx.appState.flatNumber.getValue())){
            _saveButton.setEnabled(false);
        }else{
            _saveButton.setEnabled(true);
        }
    }
}