package com.example.cryptofile_android.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.cryptofile_android.R;
import com.example.cryptofile_android.models.UserInfo;

import java.util.Date;

public class AddUserDialogFragment extends DialogFragment {

    public interface OnUserAddedListener {
        void onUserAdded(UserInfo userInfo);
    }

    private OnUserAddedListener listener;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Spinner roleSpinner;

    public AddUserDialogFragment(OnUserAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        View view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_add_user, null);

        usernameEditText = view.findViewById(R.id.et_username);
        emailEditText = view.findViewById(R.id.et_email);
        passwordEditText = view.findViewById(R.id.et_password);
        roleSpinner = view.findViewById(R.id.spinner_role);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireActivity(),
                R.array.user_roles,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        builder.setView(view)
                .setTitle("Add New User")
                .setPositiveButton("Add", (dialog, id) -> {
                    String username = usernameEditText.getText().toString().trim();
                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    String role = roleSpinner.getSelectedItem().toString();

                    if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUsername(username);
                        userInfo.setEmail(email);
                        userInfo.setPassword(password);
                        userInfo.setRole(role);
                        userInfo.setCreatedAt(new Date(System.currentTimeMillis()));


                        if (listener != null) {
                            listener.onUserAdded(userInfo);
                        }
                    } else {
                        Toast.makeText(
                                view.getContext(),
                                "Username, email, and password are required.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.cancel();
                });

        return builder.create();
    }
}
