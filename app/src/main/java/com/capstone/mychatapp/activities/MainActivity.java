package com.capstone.mychatapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.capstone.mychatapp.R;
import com.capstone.mychatapp.utils.adapters.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.karan.churi.PermissionManager.PermissionManager;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private TabLayout tabLayout;
    private DatabaseReference databaseReferenceUser;

    PermissionManager permissionManager;

    private RelativeLayout relativeLayout;
    private Snackbar snackbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout = findViewById(R.id.relativeMainLayout);
        if(!isConnectedToInternet(this)){
            showSnackBar(getString(R.string.chck_internet_connection), relativeLayout);
        }

        firebaseAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        if (firebaseAuth.getCurrentUser() != null) {


            databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field)).child(firebaseAuth.getCurrentUser().getUid());

        }

        int fragmentId = 1;

        viewPager = findViewById(R.id.main_tabPager);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),this);

        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(fragmentId);

        tabLayout = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

        permissionManager = new PermissionManager() {};
        permissionManager.checkAndRequestPermissions(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {

            sendToStart();

        } else {

            databaseReferenceUser.child(getString(R.string.FB_users_online_field)).setValue(getString(R.string.boolean_true_string));

        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {

            databaseReferenceUser.child(getString(R.string.FB_users_online_field)).setValue(ServerValue.TIMESTAMP);

        }

    }

    private void sendToStart() {

        Intent startIntent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(startIntent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_btn) {

            FirebaseAuth.getInstance().signOut();
            sendToStart();

        }

        if (item.getItemId() == R.id.main_settings_btn) {

            Intent settingsIntent = new Intent(MainActivity.this, com.capstone.mychatapp.activities.SettingsActivity.class);
            startActivity(settingsIntent);

        }

        if (item.getItemId() == R.id.main_all_btn) {

            Intent usersIntent = new Intent(MainActivity.this, com.capstone.mychatapp.activities.UsersActivity.class);
            startActivity(usersIntent);

        }

        if (item.getItemId() == R.id.action_search) {

            Intent searchIntent = new Intent(MainActivity.this, com.capstone.mychatapp.activities.SearchActivity.class);
            startActivity(searchIntent);

        }

        return true;
    }

    private boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void showSnackBar(String message, RelativeLayout relativeLayout)
    {
        snackbar = Snackbar
                .make(relativeLayout, message, Snackbar.LENGTH_INDEFINITE).
                        setAction((R.string.snackbar_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                snackbar.dismiss();
                            }
                        });
        snackbar.show();
    }
}
