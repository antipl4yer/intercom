package ru.samsung.smartintercom.service.socket.server;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.gson.Gson;
import ru.samsung.smartintercom.util.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.flow.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.samsung.smartintercom.data.call.CallDataSource;
import ru.samsung.smartintercom.data.call.CallRepositoryImpl;
import ru.samsung.smartintercom.domain.auth.AuthRepository;
import ru.samsung.smartintercom.domain.auth.model.AuthEntity;

import java.util.HashMap;
import java.util.Objects;

public class SocketServerWrapperService {
    public static class Data {
        public String house;
        public String flat;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Data that = (Data) o;
            return Objects.equals(house, that.house) && Objects.equals(flat, that.flat);
        }

        @Override
        public int hashCode() {
            return Objects.hash(house, flat);
        }
    }

    private Runnable _incomingCallHandler;
    private Callable<CallDataSource.Status, Void> _statusChangedHandler;
    private Data _data;

    private final HashMap<Data, Data> _connectionPool;

    public SocketServerWrapperService() {
        _connectionPool = new HashMap<Data, Data>();
    }

    public void setData(Data data) {
        if (_connectionPool.containsKey(data)) {
            _data = _connectionPool.get(data);
            Log.i("SocketServerWrapperService", "socket connection restored from pool");
        } else {
            _data = data;
        }

        AuthRepository authRepository = new AuthRepository() {
            @NotNull
            @Override
            public Flow<AuthEntity> getAuthData() {
                return new Flow<AuthEntity>() {
                    @Nullable
                    @Override
                    public Object collect(@NotNull FlowCollector<? super AuthEntity> flowCollector, @NotNull Continuation<? super Unit> continuation) {
                        AuthEntity authEntity = new AuthEntity(_data.house, _data.flat);
                        Log.i("SocketServerWrapperService", String.format("getAuthData, house: %s, flat: %s", _data.house, _data.flat));

                        flowCollector.emit(authEntity, continuation);

                        return null;
                    }
                };
            }
        };

        CallDataSource callDataSource = new CallDataSource(new Gson());
        callDataSource.getStatus().collect((status, continuation) -> {
            Log.i("SocketServerWrapperService", String.format("status is: %s", status.toString()));

            if (!_data.equals(data)) {
                Log.i("SocketServerWrapperService", "it's not my current config status, skip");
                return null;
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (_statusChangedHandler == null) {
                        return;
                    }
                    _statusChangedHandler.call(status);
                }
            });
            return null;
        }, new Continuation<Unit>() {
            @NotNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NotNull Object o) {
            }
        });

        CallRepositoryImpl callRepository = new CallRepositoryImpl(authRepository, callDataSource);

        callRepository.getIntercomCallStart().collect((unit, continuation) -> {
            Log.i("SocketServerWrapperService", "incoming call");

            if (!_data.equals(data)) {
                Log.i("SocketServerWrapperService", "it's not my current config call, skip");
                return null;
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (_incomingCallHandler == null) {
                        return;
                    }
                    _incomingCallHandler.run();
                }
            });

            return null;
        }, new Continuation<Unit>() {
            @NotNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NotNull Object o) {
            }
        });

        _connectionPool.put(_data, _data);
    }

    public void registerIncomingCallHandler(Runnable callback) {
        _incomingCallHandler = callback;
    }

    public void registerChangedStatusHandler(Callable<CallDataSource.Status, Void> callback) {
        _statusChangedHandler = callback;
    }
}