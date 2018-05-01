package com.example.hhoa.trackme;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Activity c;
    private Dialog d;
    private EditText emailEditText;
    private EditText passwordEditText;
    private FirebaseAuth mFirebaseAuth;

    public SignUpDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_signup);

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

        Button btnSignup = findViewById(R.id.signupButton);
        btnSignup.setOnClickListener(this);

        emailEditText = findViewById(R.id.emailFieldSignup);
        passwordEditText = findViewById(R.id.passwordFieldSignup);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signupButton:
                final String email = emailEditText.getText().toString().trim();
                final String password = passwordEditText.getText().toString().trim();

                if (password.isEmpty() || email.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(c);
                    builder.setMessage(R.string.signup_error_message)
                            .setTitle(R.string.signup_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(c, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Tracker.mFirebaseAuth = FirebaseAuth.getInstance();
                                        Tracker.mFirebaseUser = mFirebaseAuth.getCurrentUser();
                                        Toast.makeText(c, "Your account: " + email, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(c, Tracker.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        c.startActivity(intent);
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                                        builder.setMessage(task.getException().getMessage())
                                                .setTitle(R.string.login_error_title)
                                                .setPositiveButton(android.R.string.ok, null);
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            });
                }
                break;
            default:
                break;
        }
        dismiss();
    }
}