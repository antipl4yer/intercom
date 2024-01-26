package ru.samsung.smartintercom.util;


import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputEditText;

public class HouseNumberValidator {
    public static Boolean isValid(String value) {
        if (value.isEmpty()){
            return false;
        }

        return filter(value).equals(value) && value.charAt(value.length()-1) != '/';
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
        boolean hasSlash = false;
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (hasDigit && hasLetter){
                break;
            }

            if (Character.isDigit(c) && (i == 0 || !hasLetter && !hasSlash)) {
                hasDigit = true;
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

        return filtered.toString();
    }
}
