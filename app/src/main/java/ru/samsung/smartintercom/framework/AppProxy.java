package ru.samsung.smartintercom.framework;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import org.jetbrains.annotations.NotNull;

public class AppProxy extends BaseDisposable {
    public static class Ctx {
        public Application application;
        public ReactiveCommand<Activity> onActivityStarted;
        public ReactiveCommand<Activity> onActivityResumed;
        public ReactiveCommand<Fragment> onFragmentViewCreated;
    }

    private final Application.ActivityLifecycleCallbacks _activityLifecycleCallbacks;
    private final FragmentManager.FragmentLifecycleCallbacks _fragmentLifecycleCallbacks;
    private FragmentManager _fragmentManager;

    private final Ctx _ctx;

    public AppProxy(Ctx ctx) {
        _ctx = ctx;

        _fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {

            @Override
            public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
                super.onFragmentCreated(fm, f, savedInstanceState);
            }

            @Override
            public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentDestroyed(fm, f);
            }

            @Override
            public void onFragmentViewCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @NonNull @NotNull View v, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                _ctx.onFragmentViewCreated.execute(f);
            }
        };

        _activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                if (activity instanceof AppCompatActivity) {
                    if (_fragmentManager != null) {
                        _fragmentManager.unregisterFragmentLifecycleCallbacks(_fragmentLifecycleCallbacks);
                    }

                    _fragmentManager = ((AppCompatActivity) activity).getSupportFragmentManager();
                    _fragmentManager.registerFragmentLifecycleCallbacks(_fragmentLifecycleCallbacks, true);
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                _ctx.onActivityStarted.execute(activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                _ctx.onActivityResumed.execute(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        };

        _ctx.application.registerActivityLifecycleCallbacks(_activityLifecycleCallbacks);
    }

    @Override
    public void dispose() {
        if (_ctx.application != null) {
            _ctx.application.unregisterActivityLifecycleCallbacks(_activityLifecycleCallbacks);
        }

        if (_fragmentManager != null) {
            _fragmentManager.unregisterFragmentLifecycleCallbacks(_fragmentLifecycleCallbacks);
        }

        super.dispose();
    }
}
