package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment {

    private static final String TAG = "UserManagementFragment";
    private TextView tvNoUsers;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private FirebaseFirestore db;
    private List<User> userList;

    public UserManagementFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);

        tvNoUsers = view.findViewById(R.id.tvNoUsers);
        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this::manageUser);
        recyclerView.setAdapter(userAdapter);

        fetchUsers();
        return view;
    }

    private void fetchUsers() {
        db.collection("users").get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        userList.add(user);
                    }
                    userAdapter.notifyDataSetChanged();

                    // Show "No users found" if list is empty
                    if (userList.isEmpty()) {
                        tvNoUsers.setVisibility(View.VISIBLE);
                    } else {
                        tvNoUsers.setVisibility(View.GONE);
                    }

                    Log.d(TAG, "Fetched " + userList.size() + " users");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch users", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching users", e);
                });
    }

    private void manageUser(User user, String action) {
        if (action.equals("delete")) {
            db.collection("users").document(user.getUserId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "User deleted", Toast.LENGTH_SHORT).show();
                        fetchUsers();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to delete user", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting user", e);
                    });
        } else {
            String newRole = action.equals("promote") ? "admin" : "user";
            db.collection("users").document(user.getUserId())
                    .update("role", newRole)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "User " + action + "d", Toast.LENGTH_SHORT).show();
                        fetchUsers();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to update user role", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating user role", e);
                    });
        }
    }
}
