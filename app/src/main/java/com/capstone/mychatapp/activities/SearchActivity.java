package com.capstone.mychatapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.mychatapp.R;
import com.capstone.mychatapp.data.Users;
import com.capstone.mychatapp.utils.SimpleDividerItemDecoration;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView searchList;

    private EditText editTextSearchField;
    private ImageButton imageButtonSearch;

    FirebaseRecyclerAdapter<Users, SearchViewHolder> firebaseRecyclerAdapter;
    private DatabaseReference databaseReferenceUsers;

    private Snackbar snackbar;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        relativeLayout = findViewById(R.id.relativeSearchLayout);
        if(!isConnectedToInternet(this)){
            showSnackBar(getString(R.string.chck_internet_connection), relativeLayout);
        }

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field));

        toolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.search_appBar_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextSearchField = findViewById(R.id.search_field);

        imageButtonSearch = findViewById(R.id.search_btn);
        imageButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String searchText = editTextSearchField.getText().toString();

                firebaseUserSearch(searchText);

            }
        });

        searchList = findViewById(R.id.result_list);
        searchList.setHasFixedSize(true);
        searchList.setLayoutManager(new LinearLayoutManager(this));

    }


    private void firebaseUserSearch(String searchText) {
        Toast.makeText(SearchActivity.this, R.string.Toast_started_search, Toast.LENGTH_LONG).show();

        Query firebaseSearchQuery = databaseReferenceUsers.orderByChild(getString(R.string.FB_name_field)).startAt(searchText.toUpperCase()).endAt(searchText.toLowerCase() + "\uf8ff");
        FirebaseRecyclerOptions searchOptions = new FirebaseRecyclerOptions.Builder<Users>().setQuery(firebaseSearchQuery, Users.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, SearchViewHolder>(searchOptions) {
            @NonNull
            @Override
            public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_item, parent, false);

                return new SearchViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull SearchViewHolder holder, int position, @NonNull Users model) {
                holder.setDisplayName(model.getName());
                holder.setUserStatus(model.getStatus());
                holder.setUserImage(model.getThumb_image());

                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
                        profileIntent.putExtra(getString(R.string.intent_stringExtra_user_id), user_id);
                        startActivity(profileIntent);

                    }
                });
            }

        };

        searchList.setAdapter(firebaseRecyclerAdapter);
        searchList.addItemDecoration(new SimpleDividerItemDecoration(this));
        firebaseRecyclerAdapter.startListening();

    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public SearchViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDisplayName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserStatus(String status) {

            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);

        }

        public void setUserImage(final String thumb_image) {

            final CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.get()
                    .load(thumb_image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile_pic)
                    .into(userImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(thumb_image)
                                    .placeholder(R.drawable.profile_pic)
                                    .into(userImageView);

                        }
                    });

        }

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
                        setAction(R.string.snackbar_ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                snackbar.dismiss();
                            }
                        });
        snackbar.show();
    }
}
