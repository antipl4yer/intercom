package ru.samsung.smartintercom.view;

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
import ru.samsung.smartintercom.view.adapter.HistoryListAdapter;

import java.util.List;

public class CallsHistoryFragment extends BaseFragmentDisposable {
    public static class Ctx {
        public List<CallHistory> history;
        public ReactiveCommand<Void> loadData;
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

        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        _historyListAdapter = new HistoryListAdapter(context);

        recyclerView.setAdapter(_historyListAdapter);

        return view;
    }

    public void setCtx(Ctx ctx) {
        _ctx = ctx;

        _ctx.loadData.execute(null);

        _historyListAdapter.setHistoryList(_ctx.history);
    }
}