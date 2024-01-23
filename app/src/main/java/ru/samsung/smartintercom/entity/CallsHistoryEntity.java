package ru.samsung.smartintercom.entity;

import androidx.fragment.app.Fragment;
import ru.samsung.smartintercom.db.CallHistory;
import ru.samsung.smartintercom.db.IntercomDatabase;
import ru.samsung.smartintercom.framework.BaseDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.view.CallsHistoryFragment;

import java.util.ArrayList;
import java.util.List;

public class CallsHistoryEntity extends BaseDisposable {
    public static class Ctx {
        public IntercomDatabase database;
        public ReactiveCommand<Fragment> onFragmentViewCreated;
    }

    private final Ctx _ctx;

    public CallsHistoryEntity(Ctx ctx){
        _ctx = ctx;

        List<CallHistory> history = new ArrayList<CallHistory>();

        ReactiveCommand<Void> loadData = ReactiveCommand.create();
        deferDispose(loadData.subscribe(unused -> {
            history.clear();
            history.addAll(_ctx.database.callHistoryDao().getAll());
        }));

        deferDispose(ctx.onFragmentViewCreated.subscribe(fragment -> {
            if (fragment instanceof CallsHistoryFragment) {
                CallsHistoryFragment.Ctx callsHistoryFragmentCtx = new CallsHistoryFragment.Ctx();
                callsHistoryFragmentCtx.history = history;
                callsHistoryFragmentCtx.loadData = loadData;

                ((CallsHistoryFragment) fragment).setCtx(callsHistoryFragmentCtx);
                return;
            }
        }));
    }
}
