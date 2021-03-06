package com.capstone.mychatapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.capstone.mychatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextInputLayout textInputLayoutStatus;
    private Button buttonSave;

    private DatabaseReference databaseReferenceStatus;
    private FirebaseUser currentUser;

    private ProgressDialog progressDialog;

    private Snackbar snackbar;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        relativeLayout = findViewById(R.id.relativeStatusLayout);

        if(!isConnectedToInternet(this)){
            showSnackBar(getString(R.string.chck_internet_connection), relativeLayout);
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = currentUser.getUid();

        databaseReferenceStatus = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field)).child(current_uid);

        toolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.account_status);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra(getString(R.string.status_value));

        textInputLayoutStatus = findViewById(R.id.status_input);
        buttonSave = findViewById(R.id.status_save_btn);

        textInputLayoutStatus.getEditText().setText(status_value);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog = new ProgressDialog(StatusActivity.this);
                progressDialog.setTitle(getString(R.string.saving_changes));
                progressDialog.setMessage(getString(R.string.saving_changes_message));
                progressDialog.show();

                String status = textInputLayoutStatus.getEditText().getText().toString();

                databaseReferenceStatus.child(getString(R.string.FB_status_field)).setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            progressDialog.dismiss();

                        } else {

                            Toast.makeText(getApplicationContext(), R.string.Saving_changes_error_toast, Toast.LENGTH_LONG).show();

                        }

                    }
                });

            }
        });

    }

    private boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void showSnackBar(String message, RelativeLayout relativeLayout) {
        snackbar = Snackbar
                .make(relativeLayout, message, Snackbar.LENGTH_INDEFINITE).
                        setAction(R.string.snackbar_ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                snackbar.dismiss();
                            }
                        });
        snackbar.show();
    }
}