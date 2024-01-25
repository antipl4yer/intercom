package ru.samsung.smartintercom.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.db.CallHistory;
import ru.samsung.smartintercom.framework.BaseFragmentDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;

import java.util.List;

public class CallsHistoryFragment extends BaseFragmentDisposable {
    public static class Ctx {
        public List<CallHistory> history;
        public ReactiveCommand<Void> loadCallsHistoryData;
        public ReactiveCommand<Void> updateCallsHistoryDataset;
    }

    private Ctx _ctx;
    private HistoryListAdapter _historyListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calls_history, container, false);

        Context context = getContext();

        if (context == null){
            return view;
        }

        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        _historyListAdapter = new HistoryListAdapter(context);

        recyclerView.setAdapter(_historyListAdapter);

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCtx(Ctx ctx) {
        _ctx = ctx;

        _ctx.loadCallsHistoryData.execute(null);

        _historyListAdapter.setHistoryList(_ctx.history);
        deferDispose(_ctx.updateCallsHistoryDataset.subscribe(unused -> {
            _historyListAdapter.notifyDataSetChanged();
        }));
    }
}