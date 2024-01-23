package ru.samsung.smartintercom.framework;

import io.reactivex.rxjava3.disposables.Disposable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ReverseOrderDisposable implements Disposable {
    private final List<Disposable> _disposables;
    private boolean _isDisposed;

    public ReverseOrderDisposable() {
        _isDisposed = false;
        _disposables = new ArrayList<Disposable>();
    }

    public void add(Disposable disposable){
        _disposables.add(disposable);
    }

    @Override
    public void dispose() {
        if (_isDisposed) {
            return;
        }

        ListIterator<Disposable> disposableListIterator = _disposables.listIterator(_disposables.size());
        while(disposableListIterator.hasPrevious()) {
            Disposable disposableCandidate = disposableListIterator.previous();
            if (disposableCandidate.isDisposed()){
                continue;
            }

            disposableCandidate.dispose();
        }

        _disposables.clear();
        _isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }
}
