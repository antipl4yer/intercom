package ru.samsung.smartintercom.framework;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava3.annotations.*;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.functions.ObjectHelper;
import io.reactivex.rxjava3.internal.util.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.Subject;

public final class ReactiveProperty<T> extends Subject<T> {
    final ReplayBuffer<T> buffer;

    final AtomicReference<ReactivePropertyDisposable<T>[]> observers;

    @SuppressWarnings("rawtypes")
    static final ReactivePropertyDisposable[] EMPTY = new ReactivePropertyDisposable[0];

    @SuppressWarnings("rawtypes")
    static final ReactivePropertyDisposable[] TERMINATED = new ReactivePropertyDisposable[0];

    boolean done;

    /**
     * Creates an unbounded replay subject.
     * <p>
     * The internal buffer is backed by an {@link ArrayList} and starts with an initial capacity of 16. Once the
     * number of items reaches this capacity, it will grow as necessary (usually by 50%). However, as the
     * number of items grows, this causes frequent array reallocation and copying, and may hurt performance
     * and latency. This can be avoided with the {@link #create(int)} overload which takes an initial capacity
     * parameter and can be tuned to reduce the array reallocation frequency as needed.
     *
     * @param <T> the type of items observed and emitted by the Subject
     * @return the created subject
     */
    @CheckReturnValue
    @NonNull
    public static <T> ReactiveProperty<T> create() {
        return new ReactiveProperty<>(new UnboundedReplayBuffer<>(16));
    }

    /**
     * Creates an unbounded replay subject with the specified initial buffer capacity.
     * <p>
     * Use this method to avoid excessive array reallocation while the internal buffer grows to accommodate new
     * items. For example, if you know that the buffer will hold 32k items, you can ask the
     * {@code ReactiveProperty} to preallocate its internal array with a capacity to hold that many items. Once
     * the items start to arrive, the internal array won't need to grow, creating less garbage and no overhead
     * due to frequent array-copying.
     *
     * @param <T>          the type of items observed and emitted by the Subject
     * @param capacityHint the initial buffer capacity
     * @return the created subject
     * @throws IllegalArgumentException if {@code capacityHint} is non-positive
     */
    @CheckReturnValue
    @NonNull
    public static <T> ReactiveProperty<T> create(int capacityHint) {
        ObjectHelper.verifyPositive(capacityHint, "capacityHint");
        return new ReactiveProperty<>(new UnboundedReplayBuffer<>(capacityHint));
    }

    /**
     * Creates a size-bounded replay subject.
     * <p>
     * In this setting, the {@code ReactiveProperty} holds at most {@code size} items in its internal buffer and
     * discards the oldest item.
     * <p>
     * When observers subscribe to a terminated {@code ReactiveProperty}, they are guaranteed to see at most
     * {@code size} {@code onNext} events followed by a termination event.
     * <p>
     * If an observer subscribes while the {@code ReactiveProperty} is active, it will observe all items in the
     * buffer at that point in time and each item observed afterwards, even if the buffer evicts items due to
     * the size constraint in the mean time. In other words, once an Observer subscribes, it will receive items
     * without gaps in the sequence.
     *
     * @param <T>     the type of items observed and emitted by the Subject
     * @param maxSize the maximum number of buffered items
     * @return the created subject
     * @throws IllegalArgumentException if {@code maxSize} is non-positive
     */
    @CheckReturnValue
    @NonNull
    public static <T> ReactiveProperty<T> createWithSize(int maxSize) {
        ObjectHelper.verifyPositive(maxSize, "maxSize");
        return new ReactiveProperty<>(new SizeBoundReplayBuffer<>(maxSize));
    }

    /**
     * Creates an unbounded replay subject with the bounded-implementation for testing purposes.
     * <p>
     * This variant behaves like the regular unbounded {@code ReactiveProperty} created via {@link #create()} but
     * uses the structures of the bounded-implementation. This is by no means intended for the replacement of
     * the original, array-backed and unbounded {@code ReactiveProperty} due to the additional overhead of the
     * linked-list based internal buffer. The sole purpose is to allow testing and reasoning about the behavior
     * of the bounded implementations without the interference of the eviction policies.
     *
     * @param <T> the type of items observed and emitted by the Subject
     * @return the created subject
     */
    /* test */
    static <T> ReactiveProperty<T> createUnbounded() {
        return new ReactiveProperty<>(new SizeBoundReplayBuffer<>(Integer.MAX_VALUE));
    }

    /**
     * Creates a time-bounded replay subject.
     * <p>
     * In this setting, the {@code ReactiveProperty} internally tags each observed item with a timestamp value
     * supplied by the {@link Scheduler} and keeps only those whose age is less than the supplied time value
     * converted to milliseconds. For example, an item arrives at T=0 and the max age is set to 5; at T&gt;=5
     * this first item is then evicted by any subsequent item or termination event, leaving the buffer empty.
     * <p>
     * Once the subject is terminated, observers subscribing to it will receive items that remained in the
     * buffer after the terminal event, regardless of their age.
     * <p>
     * If an observer subscribes while the {@code ReactiveProperty} is active, it will observe only those items
     * from within the buffer that have an age less than the specified time, and each item observed thereafter,
     * even if the buffer evicts items due to the time constraint in the mean time. In other words, once an
     * observer subscribes, it observes items without gaps in the sequence except for any outdated items at the
     * beginning of the sequence.
     * <p>
     * Note that terminal notifications ({@code onError} and {@code onComplete}) trigger eviction as well. For
     * example, with a max age of 5, the first item is observed at T=0, then an {@code onComplete} notification
     * arrives at T=10. If an observer subscribes at T=11, it will find an empty {@code ReactiveProperty} with just
     * an {@code onComplete} notification.
     *
     * @param <T>       the type of items observed and emitted by the Subject
     * @param maxAge    the maximum age of the contained items
     * @param unit      the time unit of {@code time}
     * @param scheduler the {@link Scheduler} that provides the current time
     * @return the created subject
     * @throws NullPointerException     if {@code unit} or {@code scheduler} is {@code null}
     * @throws IllegalArgumentException if {@code maxAge} is non-positive
     */
    @CheckReturnValue
    @NonNull
    public static <T> ReactiveProperty<T> createWithTime(long maxAge, @NonNull TimeUnit unit, @NonNull Scheduler scheduler) {
        ObjectHelper.verifyPositive(maxAge, "maxAge");
        Objects.requireNonNull(unit, "unit is null");
        Objects.requireNonNull(scheduler, "scheduler is null");
        return new ReactiveProperty<>(new SizeAndTimeBoundReplayBuffer<>(Integer.MAX_VALUE, maxAge, unit, scheduler));
    }

    /**
     * Creates a time- and size-bounded replay subject.
     * <p>
     * In this setting, the {@code ReactiveProperty} internally tags each received item with a timestamp value
     * supplied by the {@link Scheduler} and holds at most {@code size} items in its internal buffer. It evicts
     * items from the start of the buffer if their age becomes less-than or equal to the supplied age in
     * milliseconds or the buffer reaches its {@code size} limit.
     * <p>
     * When observers subscribe to a terminated {@code ReactiveProperty}, they observe the items that remained in
     * the buffer after the terminal notification, regardless of their age, but at most {@code size} items.
     * <p>
     * If an observer subscribes while the {@code ReactiveProperty} is active, it will observe only those items
     * from within the buffer that have age less than the specified time and each subsequent item, even if the
     * buffer evicts items due to the time constraint in the mean time. In other words, once an observer
     * subscribes, it observes items without gaps in the sequence except for the outdated items at the beginning
     * of the sequence.
     * <p>
     * Note that terminal notifications ({@code onError} and {@code onComplete}) trigger eviction as well. For
     * example, with a max age of 5, the first item is observed at T=0, then an {@code onComplete} notification
     * arrives at T=10. If an observer subscribes at T=11, it will find an empty {@code ReactiveProperty} with just
     * an {@code onComplete} notification.
     *
     * @param <T>       the type of items observed and emitted by the Subject
     * @param maxAge    the maximum age of the contained items
     * @param unit      the time unit of {@code time}
     * @param maxSize   the maximum number of buffered items
     * @param scheduler the {@link Scheduler} that provides the current time
     * @return the created subject
     * @throws NullPointerException     if {@code unit} or {@code scheduler} is {@code null}
     * @throws IllegalArgumentException if {@code maxAge} or {@code maxSize} is non-positive
     */
    @CheckReturnValue
    @NonNull
    public static <T> ReactiveProperty<T> createWithTimeAndSize(long maxAge, @NonNull TimeUnit unit, @NonNull Scheduler scheduler, int maxSize) {
        ObjectHelper.verifyPositive(maxSize, "maxSize");
        ObjectHelper.verifyPositive(maxAge, "maxAge");
        Objects.requireNonNull(unit, "unit is null");
        Objects.requireNonNull(scheduler, "scheduler is null");
        return new ReactiveProperty<>(new SizeAndTimeBoundReplayBuffer<>(maxSize, maxAge, unit, scheduler));
    }

    /**
     * Constructs a ReplayProcessor with the given custom ReplayBuffer instance.
     *
     * @param buffer the ReplayBuffer instance, not null (not verified)
     */
    @SuppressWarnings("unchecked")
    ReactiveProperty(ReplayBuffer<T> buffer) {
        this.buffer = buffer;
        this.observers = new AtomicReference<>(EMPTY);
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        ReactivePropertyDisposable<T> rs = new ReactivePropertyDisposable<>(observer, this);
        observer.onSubscribe(rs);

        if (add(rs)) {
            if (rs.cancelled) {
                remove(rs);
                return;
            }
        }
        buffer.replay(rs);
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (done) {
            d.dispose();
        }
    }

    public void setValue(T t) {
        this.onNext(t);
    }

    @Override
    public void onNext(T t) {
        ExceptionHelper.nullCheck(t, "onNext called with a null value.");
        if (done) {
            return;
        }

        ReplayBuffer<T> b = buffer;
        b.add(t);

        for (ReactivePropertyDisposable<T> rs : observers.get()) {
            b.replay(rs);
        }
    }

    @Override
    public void onError(Throwable t) {
        ExceptionHelper.nullCheck(t, "onError called with a null Throwable.");
        if (done) {
            RxJavaPlugins.onError(t);
            return;
        }
        done = true;

        Object o = NotificationLite.error(t);

        ReplayBuffer<T> b = buffer;

        b.addFinal(o);

        for (ReactivePropertyDisposable<T> rs : terminate(o)) {
            b.replay(rs);
        }
    }

    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        done = true;

        Object o = NotificationLite.complete();

        ReplayBuffer<T> b = buffer;

        b.addFinal(o);

        for (ReactivePropertyDisposable<T> rs : terminate(o)) {
            b.replay(rs);
        }
    }

    @Override
    @CheckReturnValue
    public boolean hasObservers() {
        return observers.get().length != 0;
    }

    @CheckReturnValue
        /* test */ int observerCount() {
        return observers.get().length;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public Throwable getThrowable() {
        Object o = buffer.get();
        if (NotificationLite.isError(o)) {
            return NotificationLite.getError(o);
        }
        return null;
    }

    /**
     * Returns a single value the Subject currently has or null if no such value exists.
     * <p>The method is thread-safe.
     *
     * @return a single value the Subject currently has or null if no such value exists
     */
    @Nullable
    @CheckReturnValue
    public T getValue() {
        return buffer.getValue();
    }

    /**
     * Makes sure the item cached by the head node in a bounded
     * ReactiveProperty is released (as it is never part of a replay).
     * <p>
     * By default, live bounded buffers will remember one item before
     * the currently receivable one to ensure subscribers can always
     * receive a continuous sequence of items. A terminated ReactiveProperty
     * automatically releases this inaccessible item.
     * <p>
     * The method must be called sequentially, similar to the standard
     * {@code onXXX} methods.
     * <p>History: 2.1.11 - experimental
     *
     * @since 2.2
     */
    public void cleanupBuffer() {
        buffer.trimHead();
    }

    /**
     * An empty array to avoid allocation in getValues().
     */
    private static final Object[] EMPTY_ARRAY = new Object[0];

    /**
     * Returns an Object array containing snapshot all values of the Subject.
     * <p>The method is thread-safe.
     *
     * @return the array containing the snapshot of all values of the Subject
     */
    @CheckReturnValue
    public Object[] getValues() {
        @SuppressWarnings("unchecked")
        T[] a = (T[]) EMPTY_ARRAY;
        T[] b = getValues(a);
        if (b == EMPTY_ARRAY) {
            return new Object[0];
        }
        return b;

    }

    /**
     * Returns a typed array containing a snapshot of all values of the Subject.
     * <p>The method follows the conventions of Collection.toArray by setting the array element
     * after the last value to null (if the capacity permits).
     * <p>The method is thread-safe.
     *
     * @param array the target array to copy values into if it fits
     * @return the given array if the values fit into it or a new array containing all values
     */
    @CheckReturnValue
    public T[] getValues(T[] array) {
        return buffer.getValues(array);
    }

    @Override
    @CheckReturnValue
    public boolean hasComplete() {
        Object o = buffer.get();
        return NotificationLite.isComplete(o);
    }

    @Override
    @CheckReturnValue
    public boolean hasThrowable() {
        Object o = buffer.get();
        return NotificationLite.isError(o);
    }

    /**
     * Returns true if the subject has any value.
     * <p>The method is thread-safe.
     *
     * @return true if the subject has any value
     */
    @CheckReturnValue
    public boolean hasValue() {
        return buffer.size() != 0; // NOPMD
    }

    @CheckReturnValue
        /* test*/ int size() {
        return buffer.size();
    }

    boolean add(ReactivePropertyDisposable<T> rs) {
        for (; ; ) {
            ReactivePropertyDisposable<T>[] a = observers.get();
            if (a == TERMINATED) {
                return false;
            }
            int len = a.length;
            @SuppressWarnings("unchecked")
            ReactivePropertyDisposable<T>[] b = new ReactivePropertyDisposable[len + 1];
            System.arraycopy(a, 0, b, 0, len);
            b[len] = rs;
            if (observers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    void remove(ReactivePropertyDisposable<T> rs) {
        for (; ; ) {
            ReactivePropertyDisposable<T>[] a = observers.get();
            if (a == TERMINATED || a == EMPTY) {
                return;
            }
            int len = a.length;
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
            ReactivePropertyDisposable<T>[] b;
            if (len == 1) {
                b = EMPTY;
            } else {
                b = new ReactivePropertyDisposable[len - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, len - j - 1);
            }
            if (observers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    ReactivePropertyDisposable<T>[] terminate(Object terminalValue) {
        buffer.compareAndSet(null, terminalValue);
        return observers.getAndSet(TERMINATED);
    }

    /**
     * Abstraction over a buffer that receives events and replays them to
     * individual Observers.
     *
     * @param <T> the value type
     */
    interface ReplayBuffer<T> {

        void add(T value);

        void addFinal(Object notificationLite);

        void replay(ReactivePropertyDisposable<T> rs);

        int size();

        @Nullable
        T getValue();

        T[] getValues(T[] array);

        /**
         * Returns the terminal NotificationLite object or null if not yet terminated.
         *
         * @return the terminal NotificationLite object or null if not yet terminated
         */
        Object get();

        /**
         * Atomically compares and sets the next terminal NotificationLite object if the
         * current equals to the expected NotificationLite object.
         *
         * @param expected the expected NotificationLite object
         * @param next     the next NotificationLite object
         * @return true if successful
         */
        boolean compareAndSet(Object expected, Object next);

        /**
         * Make sure an old inaccessible head value is released
         * in a bounded buffer.
         */
        void trimHead();
    }

    static final class ReactivePropertyDisposable<T> extends AtomicInteger implements Disposable {

        private static final long serialVersionUID = 466549804534799122L;
        final Observer<? super T> downstream;
        final ReactiveProperty<T> state;

        Object index;

        volatile boolean cancelled;

        ReactivePropertyDisposable(Observer<? super T> actual, ReactiveProperty<T> state) {
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
    }

    static final class UnboundedReplayBuffer<T>
            extends AtomicReference<Object>
            implements ReplayBuffer<T> {

        private static final long serialVersionUID = -733876083048047795L;

        final List<Object> buffer;

        volatile boolean done;

        volatile int size;

        UnboundedReplayBuffer(int capacityHint) {
            this.buffer = new ArrayList<>(capacityHint);
        }

        @Override
        public void add(T value) {
            buffer.add(value);
            size++;
        }

        @Override
        public void addFinal(Object notificationLite) {
            buffer.add(notificationLite);
            trimHead();
            size++;
            done = true;
        }

        @Override
        public void trimHead() {
            // no-op in this type of buffer
        }

        @Override
        @Nullable
        @SuppressWarnings("unchecked")
        public T getValue() {
            int s = size;
            if (s != 0) {
                List<Object> b = buffer;
                Object o = b.get(s - 1);
                if (NotificationLite.isComplete(o) || NotificationLite.isError(o)) {
                    if (s == 1) {
                        return null;
                    }
                    return (T) b.get(s - 2);
                }
                return (T) o;
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T[] getValues(T[] array) {
            int s = size;
            if (s == 0) {
                if (array.length != 0) {
                    array[0] = null;
                }
                return array;
            }
            List<Object> b = buffer;
            Object o = b.get(s - 1);

            if (NotificationLite.isComplete(o) || NotificationLite.isError(o)) {
                s--;
                if (s == 0) {
                    if (array.length != 0) {
                        array[0] = null;
                    }
                    return array;
                }
            }

            if (array.length < s) {
                array = (T[]) Array.newInstance(array.getClass().getComponentType(), s);
            }
            for (int i = 0; i < s; i++) {
                array[i] = (T) b.get(i);
            }
            if (array.length > s) {
                array[s] = null;
            }

            return array;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void replay(ReactivePropertyDisposable<T> rs) {
            if (rs.getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            final List<Object> b = buffer;
            final Observer<? super T> a = rs.downstream;

            Integer indexObject = (Integer) rs.index;
            int index;
            if (indexObject != null) {
                index = indexObject;
            } else {
                index = 0;
                rs.index = 0;
            }

            for (; ; ) {

                if (rs.cancelled) {
                    rs.index = null;
                    return;
                }

                int s = size;

                while (s != index) {

                    if (rs.cancelled) {
                        rs.index = null;
                        return;
                    }

                    Object o = b.get(index);

                    if (done) {
                        if (index + 1 == s) {
                            s = size;
                            if (index + 1 == s) {
                                if (NotificationLite.isComplete(o)) {
                                    a.onComplete();
                                } else {
                                    a.onError(NotificationLite.getError(o));
                                }
                                rs.index = null;
                                rs.cancelled = true;
                                return;
                            }
                        }
                    }

                    a.onNext((T) o);
                    index++;
                }

                if (index != size) {
                    continue;
                }

                rs.index = index;

                missed = rs.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        @Override
        public int size() {
            int s = size;
            if (s != 0) {
                Object o = buffer.get(s - 1);
                if (NotificationLite.isComplete(o) || NotificationLite.isError(o)) {
                    return s - 1;
                }
                return s;
            }
            return 0;
        }
    }

    static final class Node<T> extends AtomicReference<Node<T>> {

        private static final long serialVersionUID = 6404226426336033100L;

        final T value;

        Node(T value) {
            this.value = value;
        }
    }

    static final class TimedNode<T> extends AtomicReference<TimedNode<T>> {

        private static final long serialVersionUID = 6404226426336033100L;

        final T value;
        final long time;

        TimedNode(T value, long time) {
            this.value = value;
            this.time = time;
        }
    }

    static final class SizeBoundReplayBuffer<T>
            extends AtomicReference<Object>
            implements ReplayBuffer<T> {

        private static final long serialVersionUID = 1107649250281456395L;

        final int maxSize;
        int size;

        volatile Node<Object> head;

        Node<Object> tail;

        volatile boolean done;

        SizeBoundReplayBuffer(int maxSize) {
            this.maxSize = maxSize;
            Node<Object> h = new Node<>(null);
            this.tail = h;
            this.head = h;
        }

        void trim() {
            if (size > maxSize) {
                size--;
                Node<Object> h = head;
                head = h.get();
            }
        }

        @Override
        public void add(T value) {
            Node<Object> n = new Node<>(value);
            Node<Object> t = tail;

            tail = n;
            size++;
            t.set(n); // releases both the tail and size

            trim();
        }

        @Override
        public void addFinal(Object notificationLite) {
            Node<Object> n = new Node<>(notificationLite);
            Node<Object> t = tail;

            tail = n;
            size++;
            t.lazySet(n); // releases both the tail and size

            trimHead();
            done = true;
        }

        /**
         * Replace a non-empty head node with an empty one to
         * allow the GC of the inaccessible old value.
         */
        @Override
        public void trimHead() {
            Node<Object> h = head;
            if (h.value != null) {
                Node<Object> n = new Node<>(null);
                n.lazySet(h.get());
                head = n;
            }
        }

        @Override
        @Nullable
        @SuppressWarnings("unchecked")
        public T getValue() {
            Node<Object> prev = null;
            Node<Object> h = head;

            for (; ; ) {
                Node<Object> next = h.get();
                if (next == null) {
                    break;
                }
                prev = h;
                h = next;
            }

            Object v = h.value;
            if (v == null) {
                return null;
            }
            if (NotificationLite.isComplete(v) || NotificationLite.isError(v)) {
                return (T) prev.value;
            }

            return (T) v;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T[] getValues(T[] array) {
            Node<Object> h = head;
            int s = size();

            if (s == 0) {
                if (array.length != 0) {
                    array[0] = null;
                }
            } else {
                if (array.length < s) {
                    array = (T[]) Array.newInstance(array.getClass().getComponentType(), s);
                }

                int i = 0;
                while (i != s) {
                    Node<Object> next = h.get();
                    array[i] = (T) next.value;
                    i++;
                    h = next;
                }
                if (array.length > s) {
                    array[s] = null;
                }
            }

            return array;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void replay(ReactivePropertyDisposable<T> rs) {
            if (rs.getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            final Observer<? super T> a = rs.downstream;

            Node<Object> index = (Node<Object>) rs.index;
            if (index == null) {
                index = head;
            }

            for (; ; ) {

                for (; ; ) {
                    if (rs.cancelled) {
                        rs.index = null;
                        return;
                    }

                    Node<Object> n = index.get();

                    if (n == null) {
                        break;
                    }

                    Object o = n.value;

                    if (done) {
                        if (n.get() == null) {

                            if (NotificationLite.isComplete(o)) {
                                a.onComplete();
                            } else {
                                a.onError(NotificationLite.getError(o));
                            }
                            rs.index = null;
                            rs.cancelled = true;
                            return;
                        }
                    }

                    a.onNext((T) o);

                    index = n;
                }

                if (index.get() != null) {
                    continue;
                }

                rs.index = index;

                missed = rs.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        @Override
        public int size() {
            int s = 0;
            Node<Object> h = head;
            while (s != Integer.MAX_VALUE) {
                Node<Object> next = h.get();
                if (next == null) {
                    Object o = h.value;
                    if (NotificationLite.isComplete(o) || NotificationLite.isError(o)) {
                        s--;
                    }
                    break;
                }
                s++;
                h = next;
            }

            return s;
        }
    }

    static final class SizeAndTimeBoundReplayBuffer<T>
            extends AtomicReference<Object>
            implements ReplayBuffer<T> {

        private static final long serialVersionUID = -8056260896137901749L;

        final int maxSize;
        final long maxAge;
        final TimeUnit unit;
        final Scheduler scheduler;
        int size;

        volatile TimedNode<Object> head;

        TimedNode<Object> tail;

        volatile boolean done;

        SizeAndTimeBoundReplayBuffer(int maxSize, long maxAge, TimeUnit unit, Scheduler scheduler) {
            this.maxSize = maxSize;
            this.maxAge = maxAge;
            this.unit = unit;
            this.scheduler = scheduler;
            TimedNode<Object> h = new TimedNode<>(null, 0L);
            this.tail = h;
            this.head = h;
        }

        void trim() {
            if (size > maxSize) {
                size--;
                TimedNode<Object> h = head;
                head = h.get();
            }
            long limit = scheduler.now(unit) - maxAge;

            TimedNode<Object> h = head;

            for (; ; ) {
                if (size <= 1) {
                    head = h;
                    break;
                }
                TimedNode<Object> next = h.get();

                if (next.time > limit) {
                    head = h;
                    break;
                }

                h = next;
                size--;
            }

        }

        void trimFinal() {
            long limit = scheduler.now(unit) - maxAge;

            TimedNode<Object> h = head;

            for (; ; ) {
                TimedNode<Object> next = h.get();
                if (next.get() == null) {
                    if (h.value != null) {
                        TimedNode<Object> lasth = new TimedNode<>(null, 0L);
                        lasth.lazySet(h.get());
                        head = lasth;
                    } else {
                        head = h;
                    }
                    break;
                }

                if (next.time > limit) {
                    if (h.value != null) {
                        TimedNode<Object> lasth = new TimedNode<>(null, 0L);
                        lasth.lazySet(h.get());
                        head = lasth;
                    } else {
                        head = h;
                    }
                    break;
                }

                h = next;
            }
        }

        @Override
        public void add(T value) {
            TimedNode<Object> n = new TimedNode<>(value, scheduler.now(unit));
            TimedNode<Object> t = tail;

            tail = n;
            size++;
            t.set(n); // releases both the tail and size

            trim();
        }

        @Override
        public void addFinal(Object notificationLite) {
            TimedNode<Object> n = new TimedNode<>(notificationLite, Long.MAX_VALUE);
            TimedNode<Object> t = tail;

            tail = n;
            size++;
            t.lazySet(n); // releases both the tail and size
            trimFinal();

            done = true;
        }

        /**
         * Replace a non-empty head node with an empty one to
         * allow the GC of the inaccessible old value.
         */
        @Override
        public void trimHead() {
            TimedNode<Object> h = head;
            if (h.value != null) {
                TimedNode<Object> n = new TimedNode<>(null, 0);
                n.lazySet(h.get());
                head = n;
            }
        }

        @Override
        @Nullable
        @SuppressWarnings("unchecked")
        public T getValue() {
            TimedNode<Object> prev = null;
            TimedNode<Object> h = head;

            for (; ; ) {
                TimedNode<Object> next = h.get();
                if (next == null) {
                    break;
                }
                prev = h;
                h = next;
            }

            long limit = scheduler.now(unit) - maxAge;
            if (h.time < limit) {
                return null;
            }

            Object v = h.value;
            if (v == null) {
                return null;
            }
            if (NotificationLite.isComplete(v) || NotificationLite.isError(v)) {
                return (T) prev.value;
            }

            return (T) v;
        }

        TimedNode<Object> getHead() {
            TimedNode<Object> index = head;
            // skip old entries
            long limit = scheduler.now(unit) - maxAge;
            TimedNode<Object> next = index.get();
            while (next != null) {
                long ts = next.time;
                if (ts > limit) {
                    break;
                }
                index = next;
                next = index.get();
            }
            return index;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T[] getValues(T[] array) {
            TimedNode<Object> h = getHead();
            int s = size(h);

            if (s == 0) {
                if (array.length != 0) {
                    array[0] = null;
                }
            } else {
                if (array.length < s) {
                    array = (T[]) Array.newInstance(array.getClass().getComponentType(), s);
                }

                int i = 0;
                while (i != s) {
                    TimedNode<Object> next = h.get();
                    array[i] = (T) next.value;
                    i++;
                    h = next;
                }
                if (array.length > s) {
                    array[s] = null;
                }
            }

            return array;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void replay(ReactivePropertyDisposable<T> rs) {
            if (rs.getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            final Observer<? super T> a = rs.downstream;

            TimedNode<Object> index = (TimedNode<Object>) rs.index;
            if (index == null) {
                index = getHead();
            }

            for (; ; ) {

                for (; ; ) {
                    if (rs.cancelled) {
                        rs.index = null;
                        return;
                    }

                    TimedNode<Object> n = index.get();

                    if (n == null) {
                        break;
                    }

                    Object o = n.value;

                    if (done) {
                        if (n.get() == null) {

                            if (NotificationLite.isComplete(o)) {
                                a.onComplete();
                            } else {
                                a.onError(NotificationLite.getError(o));
                            }
                            rs.index = null;
                            rs.cancelled = true;
                            return;
                        }
                    }

                    a.onNext((T) o);

                    index = n;
                }

                rs.index = index;

                missed = rs.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        @Override
        public int size() {
            return size(getHead());
        }

        int size(TimedNode<Object> h) {
            int s = 0;
            while (s != Integer.MAX_VALUE) {
                TimedNode<Object> next = h.get();
                if (next == null) {
                    Object o = h.value;
                    if (NotificationLite.isComplete(o) || NotificationLite.isError(o)) {
                        s--;
                    }
                    break;
                }
                s++;
                h = next;
            }

            return s;
        }
    }
}
