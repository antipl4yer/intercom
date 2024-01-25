package ru.samsung.smartintercom.entity;

import androidx.fragment.app.Fragment;
import ru.samsung.smartintercom.db.CallHistory;
import ru.samsung.smartintercom.db.IntercomDatabase;
import ru.samsung.smartintercom.framework.BaseDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.ui.activity.CallsHistoryFragment;

import java.util.ArrayList;
import java.util.List;

public class CallsHistoryEntity extends BaseDisposable {
    public static class Ctx {
        public IntercomDatabase database;
        public ReactiveCommand<Fragment> onFragmentViewCreated;
        public ReactiveCommand<Void> onMissedCall;
    }

    private final Ctx _ctx;
    private final List<CallHistory> _history;
    private final ReactiveCommand<Void> _updateCallsHistoryDataset;

    public CallsHistoryEntity(Ctx ctx){
        _ctx = ctx;

        _history = new ArrayList<CallHistory>();

        ReactiveCommand<Void> loadCallsHistoryData = ReactiveCommand.create();
        _updateCallsHistoryDataset = ReactiveCommand.create();

        deferDispose(_ctx.onMissedCall.subscribe(unused -> {
            loadCallsHistoryData();
        }));

        deferDispose(loadCallsHistoryData.subscribe(unused -> {
            loadCallsHistoryData();
        }));

        deferDispose(ctx.onFragmentViewCreated.subscribe(fragment -> {
            if (fragment instanceof CallsHistoryFragment) {
                CallsHistoryFragment.Ctx callsHistoryFragmentCtx = new CallsHistoryFragment.Ctx();
                callsHistoryFragmentCtx.history = _history;
                callsHistoryFragmentCtx.loadCallsHistoryData = loadCallsHistoryData;
                callsHistoryFragmentCtx.updateCallsHistoryDataset = _updateCallsHistoryDataset;

                ((CallsHistoryFragment) fragment).setCtx(callsHistoryFragmentCtx);
                return;
            }
        }));
    }

    private void loadCallsHistoryData(){
        _history.clear();
        _history.addAll(_ctx.database.callHistoryDao().getAll());
        _updateCallsHistoryDataset.execute(null);
    }
}
