package ru.samsung.smartintercom.ui.activity.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.db.CallHistory;

import java.util.List;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.HistoryItemHolder> {

    private final Context _context;
    private List<CallHistory> _historyList;

    public HistoryListAdapter(Context context) {
        _context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setHistoryList(List<CallHistory> historyList) {
        _historyList = historyList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.fragment_calls_history_row, parent, false);

        return new HistoryItemHolder(view);
    }

    public void onBindViewHolder(@NonNull HistoryItemHolder holder, int position) {
        holder.date.setText(_historyList.get(position).date);
        holder.status.setText(_historyList.get(position).status);
    }

    @Override
    public int getItemCount() {
        return _historyList.size();
    }

    public static class HistoryItemHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView status;

        public HistoryItemHolder(View view) {
            super(view);
            date = view.findViewById(R.id.text_date);
            status = view.findViewById(R.id.text_status);
        }
    }
}
