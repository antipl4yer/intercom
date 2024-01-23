package ru.samsung.smartintercom.util;


public class HouseNumberValidator {
    private static final String template = "^(\\d{1,4}|\\d+/[a-e]{1,3})$";

    public static Boolean isValid(String houseValue) {
        return houseValue.matches(template);
    }
}
