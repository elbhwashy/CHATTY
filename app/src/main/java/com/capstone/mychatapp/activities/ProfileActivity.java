package com.capstone.mychatapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.mychatapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imageViewProfile;
    private TextView textViewProfile;
    private TextView textViewProfileStatus;
    private TextView textViewProfileFriendsCount;
    private Button buttonProfileSendReq;
    private Button buttonDecline;

    private DatabaseReference databaseReferenceUsers;

    private ProgressDialog progressDialog;

    private DatabaseReference databaseReferenceFriendReq;
    private DatabaseReference databaseReferenceFriend;
    private DatabaseReference databaseReferenceNotification;

    private DatabaseReference databaseReferenceRoot;

    private FirebaseUser currentUser;

    private String currentState;

    private RelativeLayout relativeLayout;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        relativeLayout = findViewById(R.id.relativeProfileLayout);

        if(!isConnectedToInternet(this)){
            showSnackBar(getString(R.string.chck_internet_connection),relativeLayout);
        }

        final String user_id = getIntent().getStringExtra(getString(R.string.intent_stringExtra_user_id));

        databaseReferenceRoot = FirebaseDatabase.getInstance().getReference();

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field)).child(user_id);
        databaseReferenceFriendReq = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Friend_req_field));
        databaseReferenceFriend = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Friends_field));
        databaseReferenceNotification = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_notifications_field));
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        imageViewProfile = findViewById(R.id.profile_image);
        textViewProfile = findViewById(R.id.profile_displayName);
        textViewProfileStatus = findViewById(R.id.profile_status);
        textViewProfileFriendsCount = findViewById(R.id.profile_totalFriends);
        buttonProfileSendReq = findViewById(R.id.profile_send_req_btn);
        buttonDecline = findViewById(R.id.profile_decline_btn);

        currentState = getString(R.string.Friendship_status_notFriends);

       buttonDecline.setVisibility(View.INVISIBLE);
       buttonDecline.setEnabled(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.progress_dialog_user_data_title);
        progressDialog.setMessage(getString(R.string.progress_dialog_user_data_message));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child(getString(R.string.FB_name_field)).getValue().toString();
                String status = dataSnapshot.child(getString(R.string.FB_status_field)).getValue().toString();
                final String image = dataSnapshot.child(getString(R.string.FB_image_field)).getValue().toString();

                textViewProfile.setText(display_name);
                textViewProfileStatus.setText(status);

                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile_pic).into(imageViewProfile, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.profile_pic).into(imageViewProfile);

                    }
                });

                if (currentUser.getUid().equals(user_id)) {

                    buttonDecline.setEnabled(false);
                    buttonDecline.setVisibility(View.INVISIBLE);

                    buttonProfileSendReq.setEnabled(false);
                    buttonProfileSendReq.setVisibility(View.INVISIBLE);

                }

                databaseReferenceFriendReq.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child(getString(R.string.FB_friendship_request_type_field)).getValue().toString();

                            if (req_type.equals(getString(R.string.FB_notifications_type_received_value))) {

                                currentState = getString(R.string.Friendship_request_status_req_received);
                                buttonProfileSendReq.setText(getString(R.string.accept_friend_request_text));

                                buttonDecline.setVisibility(View.VISIBLE);
                                buttonDecline.setEnabled(true);

                                // DECLINE FRIEND METHOD / BUTTON

                                buttonDecline.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        databaseReferenceFriendReq.child(currentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                databaseReferenceFriendReq.child(user_id).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        buttonProfileSendReq.setEnabled(true);
                                                        currentState = getString(R.string.Friendship_status_notFriends);
                                                        buttonProfileSendReq.setText(getString(R.string.send_friend_request_text));

                                                        buttonDecline.setVisibility(View.INVISIBLE);
                                                        buttonDecline.setEnabled(false);

                                                    }
                                                });

                                            }
                                        });


                                    }
                                });


                            } else if (req_type.equals(getString(R.string.FB_notifications_type_sent_value))) {

                                currentState = getString(R.string.Friendship_request_status_req_sent);
                                buttonProfileSendReq.setText(getString(R.string.cancel_friend_request_text));

                                buttonDecline.setVisibility(View.INVISIBLE);
                                buttonDecline.setEnabled(false);

                            }

                            progressDialog.dismiss();

                        } else {

                            databaseReferenceFriend.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {

                                        currentState = getString(R.string.Friendship_status_friends);
                                        buttonProfileSendReq.setText(getString(R.string.Unfriend_this_person_text));
                                        buttonDecline.setVisibility(View.INVISIBLE);
                                        buttonDecline.setEnabled(false);

                                    }

                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    progressDialog.dismiss();

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        buttonProfileSendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonProfileSendReq.setEnabled(false);

                if (currentState.equals(getString(R.string.Friendship_status_notFriends))) {

                    DatabaseReference newNotificationRef = databaseReferenceRoot.child(getString(R.string.FB_notifications_field)).child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put(getString(R.string.FB_notifications_from_field), currentUser.getUid());
                    notificationData.put(getString(R.string.FB_notifications_type_field), getString(R.string.FB_notifications_type_request_value));

                    Map requestMap = new HashMap();
                    requestMap.put(getString(R.string.FB_Friend_req_field) + "/" + currentUser.getUid() + "/" + user_id + "/" + getString(R.string.FB_friendship_request_type_field), getString(R.string.FB_notifications_type_sent_value));
                    requestMap.put(getString(R.string.FB_Friend_req_field) + "/" + user_id + "/" + currentUser.getUid() + "/" + getString(R.string.FB_friendship_request_type_field), getString(R.string.FB_notifications_type_received_value));
                    requestMap.put(getString(R.string.Notifications) + user_id + "/" + newNotificationId, notificationData);

                    databaseReferenceRoot.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Toast.makeText(ProfileActivity.this, R.string.Toast_error_request_sending, Toast.LENGTH_SHORT).show();

                            } else {

                                currentState = getString(R.string.Friendship_request_status_req_sent);
                                buttonProfileSendReq.setText(getString(R.string.cancel_friend_request_text));

                            }

                            buttonProfileSendReq.setEnabled(true);


                        }
                    });

                }


                if (currentState.equals(getString(R.string.Friendship_request_status_req_sent))) {

                    databaseReferenceFriendReq.child(currentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            databaseReferenceFriendReq.child(user_id).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    buttonProfileSendReq.setEnabled(true);
                                    currentState = getString(R.string.Friendship_status_notFriends);
                                    buttonProfileSendReq.setText(getString(R.string.send_friend_request_text));

                                    buttonDecline.setVisibility(View.INVISIBLE);
                                    buttonDecline.setEnabled(false);

                                }
                            });

                        }
                    });

                }

                if (currentState.equals(getString(R.string.Friendship_request_status_req_received))) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put(getString(R.string.FB_Friends_field) + "/" + currentUser.getUid() + "/" + user_id + "/" + getString(R.string.FB_Friends_date_field), currentDate);
                    friendsMap.put(getString(R.string.FB_Friends_field) + "/" + user_id + "/" + currentUser.getUid() + "/" + getString(R.string.FB_Friends_date_field), currentDate);

                    friendsMap.put(getString(R.string.FB_Friend_req_field) + "/" + currentUser.getUid() + "/" + user_id, null);
                    friendsMap.put(getString(R.string.FB_Friend_req_field) + "/" + user_id + "/" + currentUser.getUid(), null);

                    databaseReferenceRoot.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                buttonProfileSendReq.setEnabled(true);
                                currentState = getString(R.string.Friendship_status_friends);
                                buttonProfileSendReq.setText(getString(R.string.Unfriend_this_person_text));

                                buttonDecline.setVisibility(View.INVISIBLE);
                                buttonDecline.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }

                if (currentState.equals(getString(R.string.Friendship_status_friends))) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put(getString(R.string.FB_Friends_field) + "/" + currentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put(getString(R.string.FB_Friends_field) + "/" + user_id + "/" + currentUser.getUid(), null);

                    databaseReferenceRoot.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                currentState = getString(R.string.Friendship_status_notFriends);
                                buttonProfileSendReq.setText(getString(R.string.send_friend_request_text));

                                buttonDecline.setVisibility(View.INVISIBLE);
                                buttonDecline.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            buttonProfileSendReq.setEnabled(true);

                        }
                    });

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