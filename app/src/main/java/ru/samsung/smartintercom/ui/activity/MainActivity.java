package ru.samsung.smartintercom.ui.activity;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.navigation.NavController;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.databinding.ActivityMainBinding;
import ru.samsung.smartintercom.framework.BaseAppCompatActivityDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.service.notification.SystemNotificationService;

import java.util.Objects;

public class MainActivity extends BaseAppCompatActivityDisposable {
    public static class Ctx {
        public SystemNotificationService systemNotificationService;
        public ReactiveCommand<Integer> navigateToMenuItem;
    }

    private Ctx _ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.button_main, R.id.button_history, R.id.button_settings, R.id.button_call)
                .build();

        FragmentManager fragmentManager = getSupportFragmentManager();

        NavHostFragment navHostFragment = (NavHostFragment) fragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();


        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    public void setCtx(Ctx ctx) {
        _ctx = ctx;

        _ctx.systemNotificationService.requestPermissions(this);

        deferDispose(_ctx.navigateToMenuItem.subscribe(itemId -> {
            BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
            bottomNavigationView.setSelectedItemId(itemId);
        }));
    }
}