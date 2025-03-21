package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userList;
    private final OnUserActionListener userActionListener;

    public interface OnUserActionListener {
        void onUserAction(User user, String action);
    }

    public UserAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.userActionListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.username.setText(user.getUsername());
        holder.email.setText(user.getEmail());
        holder.role.setText(user.getRole());

        holder.btnPromote.setOnClickListener(v -> userActionListener.onUserAction(user, "promote"));
        holder.btnDemote.setOnClickListener(v -> userActionListener.onUserAction(user, "demote"));
        holder.btnDelete.setOnClickListener(v -> userActionListener.onUserAction(user, "delete"));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username, email, role;
        Button btnPromote, btnDemote, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tvUsername);
            email = itemView.findViewById(R.id.tvEmail);
            role = itemView.findViewById(R.id.tvRole);
            btnPromote = itemView.findViewById(R.id.btnPromote);
            btnDemote = itemView.findViewById(R.id.btnDemote);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
