package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PendingEventAdapter extends RecyclerView.Adapter<PendingEventAdapter.PendingEventViewHolder> {

    private List<Event> eventList;
    private OnPendingEventActionListener listener;

    public interface OnPendingEventActionListener {
        void onApprove(Event event);
        void onDecline(Event event);
    }

    public PendingEventAdapter(List<Event> eventList, OnPendingEventActionListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PendingEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_event_list_item, parent, false);
        return new PendingEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingEventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvEventName.setText(event.getName());
        holder.tvEventDate.setText(event.getDate());
        holder.tvEventTime.setText(event.getTime());

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(event));
        holder.btnDecline.setOnClickListener(v -> listener.onDecline(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class PendingEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate, tvEventTime;
        Button btnApprove, btnDecline;

        public PendingEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }
}
