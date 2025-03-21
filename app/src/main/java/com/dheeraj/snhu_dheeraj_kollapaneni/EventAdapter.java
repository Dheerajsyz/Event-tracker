package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private final OnEventClickListener onEventClickListener;
    private boolean isAdmin;

    public EventAdapter(List<Event> eventList, OnEventClickListener listener, boolean isAdmin) {
        this.eventList = eventList;
        this.onEventClickListener = listener;
        this.isAdmin = isAdmin;
    }

    // Call this if the user role changes after adapter creation
    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.eventName.setText(event.getName());
        holder.eventDate.setText(event.getDate());
        holder.eventTime.setText(event.getTime());

        // Show or hide the delete button based on isAdmin
        if (isAdmin) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setVisibility(View.VISIBLE); // If you want admins to edit from here too
        } else {
            holder.btnDelete.setVisibility(View.GONE);
            // If you also want to hide edit for normal users, do:
            // holder.btnEdit.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(v -> onEventClickListener.onEditClick(event));
        holder.btnDelete.setOnClickListener(v -> onEventClickListener.onDeleteClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventName, eventDate, eventTime;
        ImageButton btnEdit, btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.tvEventName);
            eventDate = itemView.findViewById(R.id.tvEventDate);
            eventTime = itemView.findViewById(R.id.tvEventTime);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public interface OnEventClickListener {
        void onEditClick(Event event);
        void onDeleteClick(Event event);
    }
}
