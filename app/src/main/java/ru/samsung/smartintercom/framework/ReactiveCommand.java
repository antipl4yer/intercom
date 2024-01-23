package ru.samsung.smartintercom.framework;

import java.util.concurrent.atomic.*;

import io.reactivex.rxjava3.annotations.*;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.Subject;

public final class ReactiveCommand<T> extends Subject<T> {
    /** The terminated indicator for the subscribers array. */
    @SuppressWarnings("rawtypes")
    static final ReactiveCommandDisposable[] TERMINATED = new ReactiveCommandDisposable[0];
    /** An empty subscribers array to avoid allocating it all the time. */
    @SuppressWarnings("rawtypes")
    static final ReactiveCommandDisposable[] EMPTY = new ReactiveCommandDisposable[0];

    /** The array of currently subscribed subscribers. */
    final AtomicReference<ReactiveCommandDisposable<T>[]> subscribers;

    /** The error, write before terminating and read after checking subscribers. */
    Throwable error;

    /**
     * Constructs a PublishSubject.
     * @param <T> the value type
     * @return the new PublishSubject
     */
    @CheckReturnValue
    @NonNull
    public static <T> ReactiveCommand<T> create() {
        return new ReactiveCommand<>();
    }

    /**
     * Constructs a PublishSubject.
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    ReactiveCommand() {
        subscribers = new AtomicReference<>(EMPTY);
    }

    @Override
    protected void subscribeActual(Observer<? super T> t) {
        ReactiveCommandDisposable<T> ps = new ReactiveCommandDisposable<>(t, this);
        t.onSubscribe(ps);
        if (add(ps)) {
            // if cancellation happened while a successful add, the remove() didn't work
            // so we need to do it again
            if (ps.isDisposed()) {
                remove(ps);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                t.onError(ex);
            } else {
                t.onComplete();
            }
        }
    }

    /**
     * Tries to add the given subscriber to the subscribers array atomically
     * or returns false if the subject has terminated.
     * @param ps the subscriber to add
     * @return true if successful, false if the subject has terminated
     */
    boolean add(ReactiveCommandDisposable<T> ps) {
        for (;;) {
            ReactiveCommandDisposable<T>[] a = subscribers.get();
            if (a == TERMINATED) {
                return false;
            }

            int n = a.length;
            @SuppressWarnings("unchecked")
            ReactiveCommandDisposable<T>[] b = new ReactiveCommandDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = ps;

            if (subscribers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    /**
     * Atomically removes the given subscriber if it is subscribed to the subject.
     * @param ps the subject to remove
     */
    @SuppressWarnings("unchecked")
    void remove(ReactiveCommandDisposable<T> ps) {
        for (;;) {
            ReactiveCommandDisposable<T>[] a = subscribers.get();
            if (a == TERMINATED || a == EMPTY) {
                return;
            }

            int n = a.length;
            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == ps) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }

            ReactiveCommandDisposable<T>[] b;

            if (n == 1) {
                b = EMPTY;
            } else {
                b = new ReactiveCommandDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (subscribers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (subscribers.get() == TERMINATED) {
            d.dispose();
        }
    }

    public void execute(T t) {
        this.onNext(t);
    }

    @Override
    public void onNext(T t) {
        for (ReactiveCommandDisposable<T> pd : subscribers.get()) {
            pd.onNext(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onError(Throwable t) {
        ExceptionHelper.nullCheck(t, "onError called with a null Throwable.");
        if (subscribers.get() == TERMINATED) {
            RxJavaPlugins.onError(t);
            return;
        }
        error = t;

        for (ReactiveCommandDisposable<T> pd : subscribers.getAndSet(TERMINATED)) {
            pd.onError(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onComplete() {
        if (subscribers.get() == TERMINATED) {
            return;
        }
        for (ReactiveCommandDisposable<T> pd : subscribers.getAndSet(TERMINATED)) {
            pd.onComplete();
        }
    }

    @Override
    @CheckReturnValue
    public boolean hasObservers() {
        return subscribers.get().length != 0;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public Throwable getThrowable() {
        if (subscribers.get() == TERMINATED) {
            return error;
        }
        return null;
    }

    @Override
    @CheckReturnValue
    public boolean hasThrowable() {
        return subscribers.get() == TERMINATED && error != null;
    }

    @Override
    @CheckReturnValue
    public boolean hasComplete() {
        return subscribers.get() == TERMINATED && error == null;
    }

    /**
     * Wraps the actual subscriber, tracks its requests and makes cancellation
     * to remove itself from the current subscribers array.
     *
     * @param <T> the value type
     */
    static final class ReactiveCommandDisposable<T> extends AtomicBoolean implements Disposable {

        private static final long serialVersionUID = 3562861878281475070L;
        /** The actual subscriber. */
        final Observer<? super T> downstream;
        /** The subject state. */
        final ReactiveCommand<T> parent;

        /**
         * Constructs a PublishSubscriber, wraps the actual subscriber and the state.
         * @param actual the actual subscriber
         * @param parent the parent PublishProcessor
         */
        ReactiveCommandDisposable(Observer<? super T> actual, ReactiveCommand<T> parent) {
            this.downstream = actual;
            this.parent = parent;
        }

        public void onNext(T t) {
            if (!get()) {
                downstream.onNext(t);
            }
        }

        public void onError(Throwable t) {
            if (get()) {
                RxJavaPlugins.onError(t);
            } else {
                downstream.onError(t);
            }
        }

        public void onComplete() {
            if (!get()) {
                downstream.onComplete();
            }
        }

        @Override
        public void dispose() {
            if (compareAndSet(false, true)) {
                parent.remove(this);
            }
        }

        @Override
        public boolean isDisposed() {
            return get();
        }
    }
}
