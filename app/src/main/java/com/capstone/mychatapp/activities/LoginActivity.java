package com.capstone.mychatapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.capstone.mychatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;

    private Button buttonLogin;

    private ProgressDialog progressDialogLogin;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceUser;

    private ConstraintLayout constraintLayout;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        constraintLayout = findViewById(R.id.constraintLoginLayout);

        if(!isConnectedToInternet(this)){
            showSnackBar(getString(R.string.chck_internet_connection),constraintLayout);
        }

        firebaseAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.login);

        progressDialogLogin = new ProgressDialog(this);

        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field));

        textInputLayoutEmail = findViewById(R.id.login_email);
        textInputLayoutPassword = findViewById(R.id.login_password);
        buttonLogin = findViewById(R.id.login_btn);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = textInputLayoutEmail.getEditText().getText().toString();
                String password = textInputLayoutPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                    progressDialogLogin.setTitle(R.string.logging_in);
                    progressDialogLogin.setMessage(getString(R.string.logging_in_message));
                    progressDialogLogin.setCanceledOnTouchOutside(false);
                    progressDialogLogin.show();

                    loginUser(email, password);

                }else{
                    Toast.makeText(LoginActivity.this, R.string.Toast_register_form_fields_empty, Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(getString(R.string.LoginEmailSavedKey), textInputLayoutEmail.getEditText().getText().toString());
        outState.putString(getString(R.string.LoginPasswordSavedKey),textInputLayoutPassword.getEditText().getText().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){

           String email = savedInstanceState.getString(getString(R.string.LoginEmailSavedKey));
            textInputLayoutEmail.getEditText().setText(email);

            String password = savedInstanceState.getString(getString(R.string.LoginPasswordSavedKey));
            textInputLayoutPassword.getEditText().setText(password);

        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void loginUser(String email, String password) {

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    progressDialogLogin.dismiss();

                    String current_user_id = firebaseAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    databaseReferenceUser.child(current_user_id).child(getString(R.string.FB_device_token_field)).setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent mainIntent = new Intent(LoginActivity.this, com.capstone.mychatapp.activities.MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }
                    });

                } else {

                    progressDialogLogin.hide();

                    String task_result = task.getException().getMessage().toString();

                    Toast.makeText(LoginActivity.this, R.string.Toast_error + task_result, Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    private boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void showSnackBar(String message, ConstraintLayout constraintLayout)
    {
        snackbar = Snackbar
                .make(constraintLayout, message, Snackbar.LENGTH_INDEFINITE).
                        setAction((R.string.snackbar_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                snackbar.dismiss();
                            }
                        });
        snackbar.show();
    }
}