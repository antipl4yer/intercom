package ru.samsung.smartintercom.util;


import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputEditText;

public class FlatNumberValidator {
    private static final String template = "^([0-9]{0,6})$";

    public static Boolean isValid(String value) {
        if (value.isEmpty()){
            return false;
        }
        return value.matches(template);
    }

    public static TextWatcher attachValidator(TextInputEditText inputEditText, Runnable onTextChangedCallback) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                StringBuilder filtered = new StringBuilder();
                for (int i = 0; i < s.length() && filtered.length() < 6; i++) {
                    char c = s.charAt(i);
                    if (Character.isDigit(c)) {
                        filtered.append(c);
                    }
                }

                if (!s.toString().equals(filtered.toString())) {
                    inputEditText.setText(filtered);
                    inputEditText.setSelection(filtered.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                onTextChangedCallback.run();
            }
        };
    }
}
