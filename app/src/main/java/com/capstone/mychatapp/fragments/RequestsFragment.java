package com.capstone.mychatapp.fragments;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.capstone.mychatapp.R;
import com.capstone.mychatapp.activities.ProfileActivity;
import com.capstone.mychatapp.data.Requests;
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

public class RequestsFragment extends Fragment {

    private RecyclerView friendRequestList;
    private ImageView imageViewEmptyView;
    private DatabaseReference databaseReferenceFriendReq, databaseReferenceUsers;
    private String currentUserId;
    private FirebaseAuth firebaseAuth;
    private Query friendsQuery;
    private FirebaseRecyclerAdapter<Requests, RequestsFragment.FriendReqViewHolder> firebaseRecyclerAdapter;
    Context context;


    public RequestsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getActivity();

        View rootView = inflater.inflate(R.layout.fragment_requests, container, false);

        friendRequestList = rootView.findViewById(R.id.friendRequestList);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        databaseReferenceFriendReq = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Friend_req_field)).child(currentUserId);
        databaseReferenceFriendReq.keepSynced(true);

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(getString(R.string.FB_Users_field));
        databaseReferenceUsers.keepSynced(true);

        friendsQuery = databaseReferenceFriendReq.orderByKey();
        friendRequestList.setHasFixedSize(true);
        friendRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        imageViewEmptyView = rootView.findViewById(R.id.request_empty_view);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FirebaseRecyclerOptions requestsOptions = new FirebaseRecyclerOptions.Builder<Requests>().setQuery(friendsQuery, Requests.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, FriendReqViewHolder>(requestsOptions) {
            @Override
            public void onDataChanged() {
                imageViewEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @NonNull
            @Override
            public FriendReqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_item, parent, false);

                return new FriendReqViewHolder(view, context);
            }

            @Override
            protected void onBindViewHolder(final FriendReqViewHolder holder, final int position, Requests model) {
                holder.setRequestType(model.getReq_type());
                final String list_user_id = getRef(position).getKey();
                databaseReferenceUsers.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child(getString(R.string.FB_name_field)).getValue().toString();
                        String userThumb = dataSnapshot.child(getString(R.string.FB_thumb_image_field)).getValue().toString();
                        if (dataSnapshot.hasChild(getString(R.string.FB_users_online_field))) {
                            String userOnline = dataSnapshot.child(getString(R.string.FB_users_online_field)).getValue().toString();
                            holder.setUserOnline(userOnline);
                        }
                        if (userName.length() < 27) {
                            holder.setName(userName);
                        } else {
                            holder.setName(userName.substring(0, 24) + getString(R.string.ellipsis));
                        }
                        holder.setImage(userThumb);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onClick(View v) {
                                Intent profileSharedIntent = new Intent(getContext(), ProfileActivity.class);

                                Pair[] pairs = new Pair[2];
                                pairs[0] = new Pair<View, String>(holder.mView.findViewById(R.id.user_single_image), getString(R.string.imageTransition));
                                pairs[1] = new Pair<View, String>(holder.mView.findViewById(R.id.user_single_name), getString(R.string.nameTransition));

                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs);

                                profileSharedIntent.putExtra(getString(R.string.intent_stringExtra_user_id), list_user_id);
                                startActivity(profileSharedIntent, options.toBundle());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        friendRequestList.setAdapter(firebaseRecyclerAdapter);
        friendRequestList.addItemDecoration(new SimpleDividerItemDecoration(Objects.requireNonNull(getContext())));
        firebaseRecyclerAdapter.startListening();

    }

    @Override
    public void onResume() {
        super.onResume();

        friendRequestList.setAdapter(firebaseRecyclerAdapter);
        friendRequestList.addItemDecoration(new SimpleDividerItemDecoration(Objects.requireNonNull(getContext())));
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public class FriendReqViewHolder extends RecyclerView.ViewHolder {

        View mView;
        Context mContext;

        public FriendReqViewHolder(View itemView, Context context) {
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

        public void setRequestType(String req_type) {
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(req_type);
        }

        public void setUserOnline(String userOnline) {

            ImageView imageView = mView.findViewById(R.id.user_single_online_icon);
            if (userOnline.equals(mContext.getString(R.string.boolean_true_string))) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.INVISIBLE);
            }
        }

    }
}