package ru.samsung.smartintercom.util;


import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputEditText;

public class FlatNumberValidator {
    public static Boolean isValid(String value) {
        if (value.isEmpty()){
            return false;
        }

        return filter(value).equals(value);
    }

    public static TextWatcher attachValidator(TextInputEditText inputEditText, Runnable onTextChangedCallback) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String filtered = filter(s.toString());

                if (!s.toString().equals(filtered)) {
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

    private static String filter(String value){
        StringBuilder filtered = new StringBuilder();
        for (int i = 0; i < value.length() && filtered.length() < 6; i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c)) {
                filtered.append(c);
            }
        }

        return filtered.toString();
    }
}
