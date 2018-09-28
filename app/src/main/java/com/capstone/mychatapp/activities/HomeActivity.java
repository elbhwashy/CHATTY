package com.capstone.mychatapp.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.capstone.mychatapp.R;

public class HomeActivity extends AppCompatActivity {

    private Button buttonRegister;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        buttonRegister = findViewById(R.id.start_reg_btn);
        buttonLogin = findViewById(R.id.start_login_btn);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent registerIntent = new Intent(HomeActivity.this, com.capstone.mychatapp.activities.RegisterActivity.class);
                startActivity(registerIntent);

            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent loginIntent = new Intent(HomeActivity.this, com.capstone.mychatapp.activities.LoginActivity.class);
                startActivity(loginIntent);

            }
        });

    }
}