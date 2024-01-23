package ru.samsung.smartintercom.framework;

import androidx.fragment.app.Fragment;
import io.reactivex.rxjava3.disposables.Disposable;

public class BaseFragmentDisposable extends Fragment {
    private ReverseOrderDisposable _disposable;

    public BaseFragmentDisposable() {
        _disposable = new ReverseOrderDisposable();
    }

    protected void deferDispose(Disposable disposable) {
        _disposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        _disposable.dispose();
        _disposable = null;

        super.onDestroy();
    }
}
