package ru.samsung.smartintercom.framework;

import android.util.Log;
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

    @Override
    public void onDestroyView() {
        _disposable.disposeDisposables();

        super.onDestroyView();
    }
}
