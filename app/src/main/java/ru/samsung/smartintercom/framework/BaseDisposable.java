package ru.samsung.smartintercom.framework;

import io.reactivex.rxjava3.disposables.Disposable;

public class BaseDisposable implements Disposable {

    private ReverseOrderDisposable _disposable;

    public BaseDisposable() {
        _disposable = new ReverseOrderDisposable();
    }

    protected void deferDispose(Disposable disposable){
        _disposable.add(disposable);
    }

    @Override
    public void dispose() {
        _disposable.dispose();
        _disposable = null;
    }

    @Override
    public boolean isDisposed() {
        return false;
    }
}
