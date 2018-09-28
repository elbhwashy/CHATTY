package com.capstone.mychatapp.utils.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.capstone.mychatapp.R;
import com.capstone.mychatapp.data.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> messagesList;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceUser;
    private Context context;

    public MessageAdapter(List<Messages> messagesList, Context context) {
        this.messagesList = messagesList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_image_layout, parent, false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView mMessageTextView, mUserTextView, mTimeTextView;
        public CircleImageView mMessageCircleImageView;
        public RelativeLayout messageLayout;
        public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            mMessageTextView = itemView.findViewById(R.id.message_single_text_view);
            mUserTextView = itemView.findViewById(R.id.chat_userName_single_text_view);
            mTimeTextView = itemView.findViewById(R.id.time_single_text_view);
            mMessageCircleImageView = itemView.findViewById(R.id.message_single_profile_image);
            messageLayout = itemView.findViewById(R.id.message_layout);
            messageImage = itemView.findViewById(R.id.message_image);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        Messages c = messagesList.get(position);
        firebaseAuth = FirebaseAuth.getInstance();
        String current_user_id = firebaseAuth.getCurrentUser().getUid();

        String from = c.getFrom();
        String messageType = c.getType();
        long timeOfMessage = c.getTime();

        if (messageType.equals(context.getString(R.string.FB_message_type_text_field))) {
            holder.mMessageTextView.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.INVISIBLE);
        } else {
            holder.mMessageTextView.setVisibility(View.INVISIBLE);
            Picasso.get().load(c.getMessage())
                    .placeholder(R.drawable.square_image_placeholder).into(holder.messageImage);
        }

        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child(context.getString(R.string.FB_Users_field)).child(from);
        databaseReferenceUser.keepSynced(true);
        databaseReferenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image_url = dataSnapshot.child(context.getString(R.string.FB_thumb_image_field)).getValue().toString();
                String name = dataSnapshot.child(context.getString(R.string.FB_name_field)).getValue().toString();

                holder.mUserTextView.setText(name);
                Picasso.get().load(image_url).placeholder(R.drawable.profile_pic).into(holder.mMessageCircleImageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (current_user_id.equals(from)) {
            holder.messageLayout.setBackgroundResource(R.drawable.bkg_message_view_from);
            holder.mMessageTextView.setTextColor(Color.BLACK);
            holder.mUserTextView.setTextColor(Color.BLACK);
            holder.mTimeTextView.setTextColor(Color.BLACK);
            holder.mMessageCircleImageView.setVisibility(View.INVISIBLE);
        } else {
            holder.messageLayout.setBackgroundResource(R.drawable.bkg_message_view);
            holder.mMessageTextView.setTextColor(Color.WHITE);
            holder.mTimeTextView.setTextColor(Color.WHITE);
            holder.mUserTextView.setTextColor(Color.WHITE);
        }
        holder.mMessageTextView.setText(c.getMessage());
        holder.mTimeTextView.setText(getMessageTime(timeOfMessage));
    }

    private String getMessageTime(long timeOfMessage) {
        String time;

        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        time = sfd.format(new Date(timeOfMessage)).toString();
        return time;
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

}