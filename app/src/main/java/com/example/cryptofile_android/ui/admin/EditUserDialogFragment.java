package com.example.cryptofile_android.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.cryptofile_android.R;
import com.example.cryptofile_android.models.UserInfo;

public class EditUserDialogFragment extends DialogFragment {

    public interface OnUserEditedListener {
        void onUserEdited(UserInfo userInfo);
    }

    private OnUserEditedListener listener;
    private UserInfo userInfo;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText fullNameEditText;
    private EditText passwordEditText;
    private CheckBox resetPasswordCheckBox;
    private Spinner roleSpinner;

    public EditUserDialogFragment(UserInfo userInfo, OnUserEditedListener listener) {
        this.userInfo = userInfo;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_edit_user, null);

        usernameEditText = view.findViewById(R.id.et_username);
        emailEditText = view.findViewById(R.id.et_email);
        fullNameEditText = view.findViewById(R.id.et_full_name);
        passwordEditText = view.findViewById(R.id.et_password);
        resetPasswordCheckBox = view.findViewById(R.id.cb_reset_password);
        roleSpinner = view.findViewById(R.id.spinner_role);

        usernameEditText.setText(userInfo.getUsername());
        emailEditText.setText(userInfo.getEmail());
        fullNameEditText.setText(userInfo.getFullName() != null ? userInfo.getFullName() : "");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireActivity(),
                R.array.user_roles,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        int rolePosition = adapter.getPosition(userInfo.getRole());
        roleSpinner.setSelection(rolePosition);

        passwordEditText.setEnabled(false);
        resetPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            passwordEditText.setEnabled(isChecked);
        });

        builder.setView(view)
                .setTitle("Edit User")
                .setPositiveButton("Save", (dialog, id) -> {
                    userInfo.setUsername(usernameEditText.getText().toString().trim());
                    userInfo.setEmail(emailEditText.getText().toString().trim());
                    userInfo.setFullName(fullNameEditText.getText().toString().trim());

                    if (resetPasswordCheckBox.isChecked()) {
                        userInfo.setPassword(passwordEditText.getText().toString());
                    }

                    userInfo.setRole(roleSpinner.getSelectedItem().toString());

                    if (listener != null) {
                        listener.onUserEdited(userInfo);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.cancel();
                });

        return builder.create();
    }
}
