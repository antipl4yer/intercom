package ru.samsung.smartintercom.framework;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.rxjava3.disposables.Disposable;

public class BaseAppCompatActivityDisposable extends AppCompatActivity {
    private ReverseOrderDisposable _disposable;

    public BaseAppCompatActivityDisposable() {
        _disposable = new ReverseOrderDisposable();
    }

    protected void deferDispose(Disposable disposable) {
        _disposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        _disposable.dispose();
        _disposable = null;

        super.onDestroy();
    }
}
