package com.capstone.mychatapp;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class ChatApp extends Application {

    private DatabaseReference databaseReferenceUser;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate() {
        super.onCreate();


        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        DatabaseReference myChatAppDatabase = FirebaseDatabase.getInstance().getReference();
        myChatAppDatabase.keepSynced(true);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {

            databaseReferenceUser = FirebaseDatabase.getInstance()
                    .getReference().child(getString(R.string.FB_Users_field)).child(firebaseAuth.getCurrentUser().getUid());

            databaseReferenceUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot != null) {

                        databaseReferenceUser.child(getString(R.string.FB_users_online_field)).onDisconnect().setValue(ServerValue.TIMESTAMP);

                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


    }


}