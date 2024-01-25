package ru.samsung.smartintercom.framework;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.*;

import io.reactivex.rxjava3.annotations.*;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.util.*;
import io.reactivex.rxjava3.internal.util.AppendOnlyLinkedArrayList.NonThrowingPredicate;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.Subject;

public final class ReactiveProperty<T> extends Subject<T> {

    final AtomicReference<Object> value;

    final AtomicReference<BehaviorDisposable<T>[]> observers;

    @SuppressWarnings("rawtypes")
    static final BehaviorDisposable[] EMPTY = new BehaviorDisposable[0];

    @SuppressWarnings("rawtypes")
    static final BehaviorDisposable[] TERMINATED = new BehaviorDisposable[0];
    final ReadWriteLock lock;
    final Lock readLock;
    final Lock writeLock;

    final AtomicReference<Throwable> terminalEvent;

    long index;

    /**
     * Creates a {@link ReactiveProperty} without a default item.
     *
     * @param <T>
     *            the type of item the Subject will emit
     * @return the constructed {@link ReactiveProperty}
     */
    @CheckReturnValue
    @NonNull
    public static <T> ReactiveProperty<T> create() {
        return new ReactiveProperty<>(null);
    }

    /**
     * Creates a {@link ReactiveProperty} that emits the last item it observed and all subsequent items to each
     * {@link Observer} that subscribes to it.
     *
     * @param <T>
     *            the type of item the Subject will emit
     * @param defaultValue
     *            the item that will be emitted first to any {@link Observer} as long as the
     *            {@link ReactiveProperty} has not yet observed any items from its source {@code Observable}
     * @return the constructed {@link ReactiveProperty}
     * @throws NullPointerException if {@code defaultValue} is {@code null}
     */
    @CheckReturnValue
    @NonNull
    public static <@NonNull T> ReactiveProperty<T> createDefault(T defaultValue) {
        Objects.requireNonNull(defaultValue, "defaultValue is null");
        return new ReactiveProperty<>(defaultValue);
    }

    /**
     * Constructs an empty BehaviorSubject.
     * @param defaultValue the initial value, not null (verified)
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    ReactiveProperty(T defaultValue) {
        this.lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
        this.observers = new AtomicReference<>(EMPTY);
        this.value = new AtomicReference<>(defaultValue);
        this.terminalEvent = new AtomicReference<>();
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        BehaviorDisposable<T> bs = new BehaviorDisposable<>(observer, this);
        observer.onSubscribe(bs);
        if (add(bs)) {
            if (bs.cancelled) {
                remove(bs);
            } else {
                bs.emitFirst();
            }
        } else {
            Throwable ex = terminalEvent.get();
            if (ex == ExceptionHelper.TERMINATED) {
                observer.onComplete();
            } else {
                observer.onError(ex);
            }
        }
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (terminalEvent.get() != null) {
            d.dispose();
        }
    }

    @Override
    public void onNext(T t) {
        ExceptionHelper.nullCheck(t, "onNext called with a null value.");

        if (terminalEvent.get() != null) {
            return;
        }
        Object o = NotificationLite.next(t);
        setCurrent(o);
        for (BehaviorDisposable<T> bs : observers.get()) {
            bs.emitNext(o, index);
        }
    }

    @Override
    public void onError(Throwable t) {
        ExceptionHelper.nullCheck(t, "onError called with a null Throwable.");
        if (!terminalEvent.compareAndSet(null, t)) {
            RxJavaPlugins.onError(t);
            return;
        }
        Object o = NotificationLite.error(t);
        for (BehaviorDisposable<T> bs : terminate(o)) {
            bs.emitNext(o, index);
        }
    }

    @Override
    public void onComplete() {
        if (!terminalEvent.compareAndSet(null, ExceptionHelper.TERMINATED)) {
            return;
        }
        Object o = NotificationLite.complete();
        for (BehaviorDisposable<T> bs : terminate(o)) {
            bs.emitNext(o, index);  // relaxed read okay since this is the only mutator thread
        }
    }

    @Override
    @CheckReturnValue
    public boolean hasObservers() {
        return observers.get().length != 0;
    }

    @CheckReturnValue
        /* test support*/ int subscriberCount() {
        return observers.get().length;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public Throwable getThrowable() {
        Object o = value.get();
        if (NotificationLite.isError(o)) {
            return NotificationLite.getError(o);
        }
        return null;
    }

    /**
     * Returns a single value the Subject currently has or null if no such value exists.
     * <p>The method is thread-safe.
     * @return a single value the Subject currently has or null if no such value exists
     */
    @Nullable
    @CheckReturnValue
    public T getValue() {
        Object o = value.get();
        if (NotificationLite.isComplete(o) || NotificationLite.isError(o)) {
            return null;
        }
        return NotificationLite.getValue(o);
    }

    public void setValue(T t){
        onNext(t);
    }

    @Override
    @CheckReturnValue
    public boolean hasComplete() {
        Object o = value.get();
        return NotificationLite.isComplete(o);
    }

    @Override
    @CheckReturnValue
    public boolean hasThrowable() {
        Object o = value.get();
        return NotificationLite.isError(o);
    }

    /**
     * Returns true if the subject has any value.
     * <p>The method is thread-safe.
     * @return true if the subject has any value
     */
    @CheckReturnValue
    public boolean hasValue() {
        Object o = value.get();
        return o != null && !NotificationLite.isComplete(o) && !NotificationLite.isError(o);
    }

    boolean add(BehaviorDisposable<T> rs) {
        for (;;) {
            BehaviorDisposable<T>[] a = observers.get();
            if (a == TERMINATED) {
                return false;
            }
            int len = a.length;
            @SuppressWarnings("unchecked")
            BehaviorDisposable<T>[] b = new BehaviorDisposable[len + 1];
            System.arraycopy(a, 0, b, 0, len);
            b[len] = rs;
            if (observers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    void remove(BehaviorDisposable<T> rs) {
        for (;;) {
            BehaviorDisposable<T>[] a = observers.get();
            int len = a.length;
            if (len == 0) {
                return;
            }
            int j = -1;
            for (int i = 0; i < len; i++) {
                if (a[i] == rs) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }
            BehaviorDisposable<T>[] b;
            if (len == 1) {
                b = EMPTY;
            } else {
                b = new BehaviorDisposable[len - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, len - j - 1);
            }
            if (observers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    BehaviorDisposable<T>[] terminate(Object terminalValue) {

        setCurrent(terminalValue);

        return observers.getAndSet(TERMINATED);
    }

    void setCurrent(Object o) {
        writeLock.lock();
        index++;
        value.lazySet(o);
        writeLock.unlock();
    }

    static final class BehaviorDisposable<T> implements Disposable, AppendOnlyLinkedArrayList.NonThrowingPredicate<Object> {

        final Observer<? super T> downstream;
        final ReactiveProperty<T> state;

        boolean next;
        boolean emitting;
        AppendOnlyLinkedArrayList<Object> queue;

        boolean fastPath;

        volatile boolean cancelled;

        long index;

        BehaviorDisposable(Observer<? super T> actual, ReactiveProperty<T> state) {
            this.downstream = actual;
            this.state = state;
        }

        @Override
        public void dispose() {
            if (!cancelled) {
                cancelled = true;

                state.remove(this);
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled;
        }

        void emitFirst() {
            if (cancelled) {
                return;
            }
            Object o;
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                if (next) {
                    return;
                }

                ReactiveProperty<T> s = state;
                Lock lock = s.readLock;

                lock.lock();
                index = s.index;
                o = s.value.get();
                lock.unlock();

                emitting = o != null;
                next = true;
            }

            if (o != null) {
                if (test(o)) {
                    return;
                }

                emitLoop();
            }
        }

        void emitNext(Object value, long stateIndex) {
            if (cancelled) {
                return;
            }
            if (!fastPath) {
                synchronized (this) {
                    if (cancelled) {
                        return;
                    }
                    if (index == stateIndex) {
                        return;
                    }
                    if (emitting) {
                        AppendOnlyLinkedArrayList<Object> q = queue;
                        if (q == null) {
                            q = new AppendOnlyLinkedArrayList<>(4);
                            queue = q;
                        }
                        q.add(value);
                        return;
                    }
                    next = true;
                }
                fastPath = true;
            }

            test(value);
        }

        @Override
        public boolean test(Object o) {
            return cancelled || NotificationLite.accept(o, downstream);
        }

        void emitLoop() {
            for (;;) {
                if (cancelled) {
                    return;
                }
                AppendOnlyLinkedArrayList<Object> q;
                synchronized (this) {
                    q = queue;
                    if (q == null) {
                        emitting = false;
                        return;
                    }
                    queue = null;
                }

                q.forEachWhile(this);
            }
        }
    }
}
