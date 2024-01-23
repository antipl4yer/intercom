package ru.samsung.smartintercom.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Converter {
    public static Integer convertCharSequenceToInteger(CharSequence value){
        return Integer.parseInt((String) value);
    }

    public static String convertCharSequenceToString(CharSequence value){
        return (String) value;
    }

    public static CharSequence convertStringToCharSequence(String value){
        return value;
    }

    public static String convertInstantToString(Instant value){
        LocalDateTime localDateTime = LocalDateTime.ofInstant(value, ZoneOffset.systemDefault());

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("HH:mm dd.MM.yyyy");
        return formatter.format(localDateTime);
    }
}
