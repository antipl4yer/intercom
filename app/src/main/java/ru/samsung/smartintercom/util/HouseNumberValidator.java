package ru.samsung.smartintercom.util;


import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputEditText;

public class HouseNumberValidator {
    private static final String template = "^(\\d{1,3}(?:/[a-e])?\\d?[a-e]?|\\d{1,4})$";

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
                boolean hasSlash = false;
                boolean hasLetter = false;

                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);

                    if (Character.isDigit(c) && (i == 0 || !hasLetter && !hasSlash)) {
                        filtered.append(c);
                    } else if (c == '/' && !hasSlash && filtered.length() > 0 && filtered.length() < 3) {
                        hasSlash = true;
                        filtered.append(c);
                    } else if (c >= 'a' && c <= 'e' && !hasLetter && filtered.length() > 0) {
                        hasLetter = true;
                        filtered.append(c);
                    }

                    if (filtered.length() == 4) {
                        break;
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
