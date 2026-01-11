package com.example.cryptofile_android.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cryptofile_android.R;
import com.example.cryptofile_android.firebase.UserDAO;
import com.example.cryptofile_android.ui.admin.AdminUserManagementActivity;
import com.example.cryptofile_android.ui.admin.EditUserDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<UserInfo> userList = new ArrayList<>();
    private UserDAO userDAO;
    private OnUserActionListener onUserActionListener;

    public interface OnUserActionListener {
        void onUserListUpdated(List<UserInfo> updatedList);
    }

    public UserAdapter(Context context, OnUserActionListener onUserActionListener) {
        this.context = context;
        this.userDAO = new UserDAO();
        this.onUserActionListener = onUserActionListener;
    }

    public void setUserList(List<UserInfo> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserInfo user = userList.get(position);

        holder.usernameTextView.setText(user.getUsername());
        holder.emailTextView.setText(user.getEmail());
        holder.roleTextView.setText(user.getRole());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Date dateMillis = user.getCreatedAt();
        if (dateMillis != null) {
            holder.joinDateTextView.setText(dateFormat.format(dateMillis));
        } else {
            holder.joinDateTextView.setText("N/A");
        }

        holder.editButton.setOnClickListener(v -> {
            EditUserDialogFragment dialog = new EditUserDialogFragment(user, (updatedUser) -> {
                userDAO.updateUserInfo(updatedUser);
                if (onUserActionListener != null) {
                    onUserActionListener.onUserListUpdated(new ArrayList<>());
                }
            });
            dialog.show(((AdminUserManagementActivity) context).getSupportFragmentManager(), "edit_user_dialog");
        });

        holder.deleteButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete User");
            builder.setMessage("Are you sure you want to delete user: " + user.getUsername() + "?\nThis action cannot be undone.");
            builder.setPositiveButton("Delete", (dialog, which) -> {
                userDAO.deleteUser(user.getUserId());
                if (onUserActionListener != null) {
                    onUserActionListener.onUserListUpdated(new ArrayList<>());
                }
                dialog.dismiss();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView emailTextView;
        TextView roleTextView;
        TextView joinDateTextView;
        Button editButton;
        Button deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username_text);
            emailTextView = itemView.findViewById(R.id.email_text);
            roleTextView = itemView.findViewById(R.id.role_text);
            joinDateTextView = itemView.findViewById(R.id.join_date_text);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
