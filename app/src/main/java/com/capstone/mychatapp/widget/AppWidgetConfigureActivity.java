package com.capstone.mychatapp.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.capstone.mychatapp.R;
import com.capstone.mychatapp.data.Chat;
import com.capstone.mychatapp.utils.GetMessageTime;
import com.capstone.mychatapp.utils.SimpleDividerItemDecoration;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class AppWidgetConfigureActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "com.example.alessandro.mychatapp.widget.AppWidget";

    private static final String PREF_PREFIX_KEY = "appwidget_";
    int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Toolbar toolbar;
    private RecyclerView chatList;

    private ImageView imageViewEmptyView;
    private DatabaseReference databaseReferenceMessages;
    private DatabaseReference databaseReferenceUsers;
    private DatabaseReference databaseReferenceRoot;
    private String currentUserId;
    private FirebaseAuth firebaseAuth;
    private Query chatsQuery;
    private static final String IMAGE_MESSAGE = "  Image";
    private FirebaseRecyclerAdapter<Chat, AppWidgetConfigureActivity.ChatsViewHolder> firebaseRecyclerAdapter;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_chat_app_widget_configure);

        context = getApplicationContext();

        Paper.init(context);

        toolbar = findViewById(R.id.Widget_users_appBar2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.AppWidgetConfig_choose_chat);

        chatList = findViewById(R.id.widget_chatList);
        chatList.setHasFixedSize(true);
        chatList.setLayoutManager(new LinearLayoutManager(this));

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        databaseReferenceRoot = FirebaseDatabase.getInstance().getReference();
        databaseReferenceMessages = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Messages_field)).child(currentUserId);
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field));
        databaseReferenceRoot.keepSynced(true);
        databaseReferenceMessages.keepSynced(true);
        databaseReferenceUsers.keepSynced(true);
        chatsQuery = databaseReferenceRoot.child(getString(R.string.FB_lastMessage_field)).child(currentUserId).orderByChild(getString(R.string.FB_lastMessageKey_field));

        imageViewEmptyView = findViewById(R.id.widget_chats_empty_view);

        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions chatsOptions = new FirebaseRecyclerOptions.Builder<Chat>().setQuery(chatsQuery, Chat.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chat, AppWidgetConfigureActivity.ChatsViewHolder>(chatsOptions) {
            @Override
            public void onDataChanged() {
                imageViewEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @NonNull
            @Override
            public AppWidgetConfigureActivity.ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_single_chat_item_fragment, parent, false);

                return new AppWidgetConfigureActivity.ChatsViewHolder(view, context);
            }

            @Override
            protected void onBindViewHolder(final AppWidgetConfigureActivity.ChatsViewHolder holder, final int position, Chat model) {
                final String list_user_id = getRef(position).getKey();
                databaseReferenceUsers.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child(getString(R.string.FB_name_field)).getValue().toString();
                        final String userThumb = dataSnapshot.child(getString(R.string.FB_thumb_image_field)).getValue().toString();
                        if (dataSnapshot.hasChild(getString(R.string.FB_users_online_field))) {
                            String userOnline = dataSnapshot.child(getString(R.string.FB_users_online_field)).getValue().toString();
                            holder.setUserOnline(userOnline);
                        }
                        if (userName.length() < 20) {
                            holder.setName(userName);
                        } else {
                            holder.setName(userName.substring(0, 17) + getString(R.string.ellipsis));
                        }
                        holder.setImage(userThumb);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onClick(View v) {

                                final Context context = AppWidgetConfigureActivity.this;

                                saveTitlePref(context, appWidgetId, list_user_id);

                                Paper.book(String.valueOf(appWidgetId)).write("user_name", userName);
                                Paper.book(String.valueOf(appWidgetId)).write("image_thumb", userThumb);

                                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                                AppWidget.updateAppWidget(context, appWidgetManager, appWidgetId, list_user_id, userName,userThumb);

                                Intent resultValue = new Intent();
                                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                                setResult(RESULT_OK, resultValue);
                                finish();

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                databaseReferenceMessages.child(list_user_id).child(model.getLastMessageKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String message = dataSnapshot.child(getString(R.string.FB_message_field)).getValue().toString();
                        String type = dataSnapshot.child(getString(R.string.FB_message_type_field)).getValue().toString();
                        Long time = (Long) dataSnapshot.child(getString(R.string.FB_message_time_field)).getValue();
                        GetMessageTime gmt = new GetMessageTime();
                        String date = gmt.getMessageTime(time, getApplicationContext());
                        holder.setDate(date);

                        String filteredMessage = filterMessage(message);

                        if (type.equals(getString(R.string.FB_message_type_text_field))) {
                            if (filteredMessage.length() < 35) {
                                holder.setLastMessageKey(filteredMessage, type);
                            } else {
                                holder.setLastMessageKey(filteredMessage.substring(0, 32).trim() + "...", type);
                            }
                        } else if (type.equals(getString(R.string.FB_message_type_image_field))) {
                            holder.setLastMessageKey(IMAGE_MESSAGE, type);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        chatList.setAdapter(firebaseRecyclerAdapter);
        chatList.addItemDecoration(new SimpleDividerItemDecoration(Objects.requireNonNull(this)));
        firebaseRecyclerAdapter.startListening();
    }

    private String filterMessage(String message) {
        String filteredMessage = message.replaceAll("\\r\\n|\\r|\\n", " ");
        return filteredMessage;
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        Context mContext;

        public ChatsViewHolder(View itemView, Context context) {
            super(itemView);

            mView = itemView;
            mContext = context;
        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setImage(String thumb_image) {
            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);

            Picasso.get().load(thumb_image).placeholder(R.drawable.profile_pic).into(userImageView);

        }

        public void setDate(String date) {
            TextView timeView = mView.findViewById(R.id.user_single_message_time);
            timeView.setText(date);
        }

        public void setUserOnline(String userOnline) {

            ImageView imageView = mView.findViewById(R.id.user_single_online_image);
            if (userOnline.equals(mContext.getString(R.string.boolean_true_string))) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.INVISIBLE);
            }
        }

        public void setLastMessageKey(String lastMessageKey, String messageType) {
            TextView userMessageView = mView.findViewById(R.id.user_single_status);
            if (messageType.equals(mContext.getString(R.string.FB_message_type_image_field))) {
                userMessageView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_image_black_24dp, 0, 0, 0);
            }
            userMessageView.setText(lastMessageKey);
        }
    }

    public AppWidgetConfigureActivity() {
        super();
    }

    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }
}