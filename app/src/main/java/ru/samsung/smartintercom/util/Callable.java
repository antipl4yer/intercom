package ru.samsung.smartintercom.util;

public interface Callable<I, O> {
    public O call(I input);
}