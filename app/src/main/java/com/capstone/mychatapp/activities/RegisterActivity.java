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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout textInputLayoutDisplayName;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutConfirmPassword;
    private Button buttonCreate;

    private Toolbar toolbar;

    private DatabaseReference databaseReference;

    private ProgressDialog progressDialogReg;

    private FirebaseAuth firebaseAuth;

    private ConstraintLayout constraintLayout;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        constraintLayout = findViewById(R.id.constraintRegisterLayout);

        if(!isConnectedToInternet(this)){
            showSnackBar(getString(R.string.chck_internet_connection),constraintLayout);
        }

        toolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.create_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialogReg = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        textInputLayoutDisplayName = findViewById(R.id.register_display_name);
        textInputLayoutEmail = findViewById(R.id.register_email);
        textInputLayoutPassword = findViewById(R.id.register_password);
        textInputLayoutConfirmPassword = findViewById(R.id.register_confirm_password);
        buttonCreate = findViewById(R.id.register_create_btn);

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String display_name = textInputLayoutDisplayName.getEditText().getText().toString();
                String email = textInputLayoutEmail.getEditText().getText().toString();
                String password = textInputLayoutPassword.getEditText().getText().toString();
                String confirmPassword = textInputLayoutConfirmPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)  ) {

                    if (password.equals(confirmPassword)) {
                        progressDialogReg.setTitle(getString(R.string.registering_user));
                        progressDialogReg.setMessage(getString(R.string.registering_user_message));
                        progressDialogReg.setCanceledOnTouchOutside(false);
                        progressDialogReg.show();

                        register_user(display_name, email, password);
                    }else {
                        textInputLayoutConfirmPassword.setError(getString(R.string.not_match));
                    }
                }else{
                    Toast.makeText(RegisterActivity.this, R.string.Toast_register_form_fields_empty, Toast.LENGTH_LONG).show();
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

    public void showSnackBar(String message, ConstraintLayout constraintLayout) {
        snackbar = Snackbar
                .make(constraintLayout, message, Snackbar.LENGTH_INDEFINITE).
                        setAction(R.string.snackbar_ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                snackbar.dismiss();
                            }
                        });
        snackbar.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(getString(R.string.RegisterNameSaved), textInputLayoutDisplayName.getEditText().getText().toString());
        outState.putString(getString(R.string.RegisterEmailSaved), textInputLayoutEmail.getEditText().getText().toString());
        outState.putString(getString(R.string.RegisterPasswordSaved), textInputLayoutPassword.getEditText().getText().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){

            String display_name = savedInstanceState.getString(getString(R.string.RegisterNameSaved));
            textInputLayoutDisplayName.getEditText().setText(display_name);

            String email = savedInstanceState.getString(getString(R.string.RegisterEmailSaved));
            textInputLayoutEmail.getEditText().setText(email);

            String password = savedInstanceState.getString(getString(R.string.RegisterPasswordSaved));
            textInputLayoutPassword.getEditText().setText(password);

        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    private void register_user(final String display_name, String email, String password) {

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    databaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field)).child(uid);
                    String device_token = FirebaseInstanceId.getInstance().getToken();
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put(getString(R.string.FB_name_field), display_name);
                    userMap.put(getString(R.string.FB_status_field), getString(R.string.default_status));
                    userMap.put(getString(R.string.FB_image_field), getString(R.string.default_image));
                    userMap.put(getString(R.string.FB_thumb_image_field), getString(R.string.default_thumb_image));
                    userMap.put(getString(R.string.FB_device_token_field), device_token);

                    databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                progressDialogReg.dismiss();

                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            }

                        }
                    });

                } else {

                    progressDialogReg.hide();
                    Toast.makeText(RegisterActivity.this, R.string.registration_error_toast, Toast.LENGTH_LONG).show();

                }

            }
        });

    }
}