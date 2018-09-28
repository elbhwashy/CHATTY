package com.capstone.mychatapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.mychatapp.utils.adapters.MessageAdapter;
import com.capstone.mychatapp.R;
import com.capstone.mychatapp.data.Messages;
import com.capstone.mychatapp.utils.GetTime;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chatUserId;
    private String chatUserName;
    private String currentUserId;
    private Toolbar toolbarChat;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceUsers;
    private DatabaseReference databaseReferenceRoot;
    private TextView textViewChatUserName;
    private TextView textViewChatUserLastSeen;
    private CircleImageView circleImageViewChatUser;
    private EditText editTextChatMessageEditText;
    private ImageButton imageButtonChatMessageAdd;
    private ImageButton imageButtonchatMessageSend;
    private RecyclerView recyclerViewMessageList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private LinearLayout linearLayoutMessage;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private static final int GALLERY_PICK = 1;
    private int currentPage = 1;

    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";

    private StorageReference storageReferenceImage;
    private RelativeLayout relativeLayout;
    private Snackbar snackbar;
    private String TAG;


    @Override
    protected void onSaveInstanceState(Bundle outState) {

        String saved_message = editTextChatMessageEditText.getText().toString();
        outState.putString("messageSavedKey", saved_message);

        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: messageSavedKey: " + saved_message );

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatUserId = getIntent().getStringExtra(getString(R.string.intent_stringExtra_user_id));
        chatUserName = getIntent().getStringExtra(getString(R.string.intent_stringExtra_chatUserName));
        toolbarChat = findViewById(R.id.chat_bar_layout);
        storageReferenceImage = FirebaseStorage.getInstance().getReference();

        setSupportActionBar(toolbarChat);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        relativeLayout = findViewById(R.id.relativeChatLayout);

        if(!isConnectedToInternet(this)){
            showSnackBar(getString(R.string.chck_internet_connection),relativeLayout);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth != null) {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View custom_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(custom_view);

        textViewChatUserName = findViewById(R.id.custom_bar_display_name);
        textViewChatUserLastSeen = findViewById(R.id.custom_bar_last_seen);
        circleImageViewChatUser = findViewById(R.id.custom_bar_image);
        if (chatUserName.length() < 20) {
            textViewChatUserName.setText(chatUserName);
        } else {
            chatUserName = chatUserName.substring(0, 17) + getString(R.string.ellipsis);
            textViewChatUserName.setText(chatUserName);
        }

        messageAdapter = new MessageAdapter(messagesList, this);
        recyclerViewMessageList = findViewById(R.id.message_list_recycler_view);
        linearLayoutMessage = findViewById(R.id.chat_message_linear_layout);
        linearLayoutManager = new LinearLayoutManager(this);

        recyclerViewMessageList.setHasFixedSize(true);
        recyclerViewMessageList.setLayoutManager(linearLayoutManager);
        recyclerViewMessageList.setAdapter(messageAdapter);

        databaseReferenceRoot = FirebaseDatabase.getInstance().getReference();
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field));
        databaseReferenceRoot.keepSynced(true);

        databaseReferenceUsers.keepSynced(true);
        loadMessages();
        databaseReferenceRoot.child(getString(R.string.FB_Friends_field)).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                linearLayoutMessage.setVisibility(View.VISIBLE);
                int optionId = dataSnapshot.child(chatUserId).exists() ? R.layout.send_message : R.layout.unable_to_send_message;

                View C = findViewById(R.id.chat_message_linear_layout);
                ViewGroup parent = (ViewGroup) C.getParent();
                int index = parent.indexOfChild(C);
                parent.removeView(C);
                C = getLayoutInflater().inflate(optionId, parent, false);
                parent.addView(C, index);

                if (optionId == R.layout.send_message) {
                    editTextChatMessageEditText = C.findViewById(R.id.chat_message_edit_text);

                    if ( savedInstanceState != null ) {
                        String saved_message = savedInstanceState.getString("messageSavedKey");
                        editTextChatMessageEditText.setText( saved_message );
                    }

                    imageButtonChatMessageAdd = C.findViewById(R.id.chat_message_add_btn);
                    imageButtonchatMessageSend = C.findViewById(R.id.chat_message_send_btn);

                    imageButtonchatMessageSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendMessage();
                        }
                    });


                    imageButtonChatMessageAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent galleryIntent = new Intent();
                            galleryIntent.setType(getString(R.string.gallery_intent_setType));
                            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(galleryIntent, getString(R.string.gallery_intent_title)), GALLERY_PICK);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReferenceUsers.child(chatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String thumbImage = dataSnapshot.child(getString(R.string.FB_thumb_image_field)).getValue().toString();
                Picasso.get().load(thumbImage).placeholder(R.drawable.profile_pic).into(circleImageViewChatUser);
                String online = dataSnapshot.child(getString(R.string.FB_users_online_field)).getValue().toString();

                if (online.equals(getString(R.string.boolean_true_string))) {
                    textViewChatUserLastSeen.setText(getString(R.string.status_online_text));
                } else {
                    GetTime getTimeAgo = new GetTime();
                    long last_seen = Long.parseLong(online);
                    String last_seen_time = getTimeAgo.getTimeAgo(last_seen, getApplicationContext());
                    textViewChatUserLastSeen.setText(last_seen_time);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            final String currentUserRef = getString(R.string.FB_Messages_field) + "/" + currentUserId + "/" + chatUserId;
            final String chatUserRef = getString(R.string.FB_Messages_field) + "/" + chatUserId + "/" + currentUserId;

            final String currentUserMessageRef = getString(R.string.FB_lastMessage_field) + "/" + currentUserId + "/" + chatUserId;
            final String chatUserMessageRef = getString(R.string.FB_lastMessage_field) + "/" + chatUserId + "/" + currentUserId;

            DatabaseReference messageUserRef = databaseReferenceRoot.child(getString(R.string.FB_Messages_field)).child(currentUserId).child(chatUserId).push();
            final String pushId = messageUserRef.getKey();

            final StorageReference filePath = storageReferenceImage.child(getString(R.string.FB_storage_message_images_field)).child(pushId + getString(R.string.jpg_extension));

            filePath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult();
                        String downloadUrl = downloadUri.toString();

                        Map messageMap = new HashMap();
                        messageMap.put(getString(R.string.FB_message_field), downloadUrl);
                        messageMap.put(getString(R.string.FB_message_seen_field), false);
                        messageMap.put(getString(R.string.FB_message_type_field), getString(R.string.FB_message_type_image_field));
                        messageMap.put(getString(R.string.FB_message_time_field), ServerValue.TIMESTAMP);
                        messageMap.put(getString(R.string.FB_message_from_field), currentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
                        messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

                        Map lastMessageMap = new HashMap();
                        lastMessageMap.put(getString(R.string.FB_lastMessageKey_field), pushId);

                        Map lastMessageUserMap = new HashMap();
                        lastMessageUserMap.put(currentUserMessageRef, lastMessageMap);
                        lastMessageUserMap.put(chatUserMessageRef, lastMessageMap);

                        editTextChatMessageEditText.setText("");

                        databaseReferenceRoot.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Toast.makeText(ChatActivity.this, databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                } else {
                                    loadMessages();
                                }
                            }
                        });

                        databaseReferenceRoot.updateChildren(lastMessageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Toast.makeText(ChatActivity.this, databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void loadMessages() {

        DatabaseReference messagesRef = databaseReferenceRoot.child(getString(R.string.FB_Messages_field)).child(currentUserId).child(chatUserId);
        Query messageQuery = messagesRef.limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);

        messagesList.clear();

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

                recyclerViewMessageList.scrollToPosition(messagesList.size() - 1);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    private void sendMessage() {
        String message = editTextChatMessageEditText.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            String currentUserRef = getString(R.string.FB_Messages_field) + "/" + currentUserId + "/" + chatUserId;
            String chatUserRef = getString(R.string.FB_Messages_field) + "/" + chatUserId + "/" + currentUserId;

            String currentUserMessageRef = getString(R.string.FB_lastMessage_field) + "/" + currentUserId + "/" + chatUserId;
            String chatUserMessageRef = getString(R.string.FB_lastMessage_field) + "/" + chatUserId + "/" + currentUserId;

            DatabaseReference messageUserRef = databaseReferenceRoot.child(getString(R.string.FB_Messages_field)).child(currentUserId).child(chatUserId).push();
            String pushId = messageUserRef.getKey();

            Map messageMap = new HashMap();
            messageMap.put(getString(R.string.FB_message_field), message);
            messageMap.put(getString(R.string.FB_message_seen_field), false);
            messageMap.put(getString(R.string.FB_message_type_field), getString(R.string.FB_message_type_text_field));
            messageMap.put(getString(R.string.FB_message_time_field), ServerValue.TIMESTAMP);
            messageMap.put(getString(R.string.FB_message_from_field), currentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
            messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

            Map lastMessageMap = new HashMap();
            lastMessageMap.put(getString(R.string.FB_lastMessageKey_field), pushId);

            Map lastMessageUserMap = new HashMap();
            lastMessageUserMap.put(currentUserMessageRef, lastMessageMap);
            lastMessageUserMap.put(chatUserMessageRef, lastMessageMap);

            editTextChatMessageEditText.setText("");

            databaseReferenceRoot.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast.makeText(ChatActivity.this, databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        loadMessages();
                    }
                }
            });

            databaseReferenceRoot.updateChildren(lastMessageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast.makeText(ChatActivity.this, databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            sendToStart();
        } else {
            databaseReferenceUsers.child(currentUser.getUid()).child(getString(R.string.FB_users_online_field)).setValue(getString(R.string.boolean_true_string));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            databaseReferenceUsers.child(currentUser.getUid()).child(getString(R.string.FB_users_online_field)).setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {

        Intent startIntent = new Intent(ChatActivity.this, com.capstone.mychatapp.activities.HomeActivity.class);
        startActivity(startIntent);
        finish();
    }

    private boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void showSnackBar(String message, RelativeLayout relativeLayout){

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