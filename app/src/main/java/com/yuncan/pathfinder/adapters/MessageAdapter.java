package com.yuncan.pathfinder.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yuncan.pathfinder.clas.Messages;
import com.yuncan.pathfinder.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    String current_user_id;
    private DatabaseReference mChatUser, mCurrentUser;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        mAuth = FirebaseAuth.getInstance();
        Messages c = mMessageList.get(i);
        String from_user = c.getFrom();
        String message_type = c.getType();
        messageViewHolder.messageText.setText(c.getMessage());
        mCurrentUser = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        current_user_id = mAuth.getCurrentUser().getUid();
        if (from_user.equals(current_user_id)){
            mCurrentUser.child(current_user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    messageViewHolder.fromtext.setText("Siz");
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.profile).into(messageViewHolder.profileImage);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            messageViewHolder.messageText.setBackgroundResource(R.drawable.current_message_text_background);
            messageViewHolder.messageText.setTextColor(Color.BLACK);
        }else{
            mCurrentUser.child(from_user).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    messageViewHolder.fromtext.setText(dataSnapshot.child("kullaniciadi").getValue().toString());
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.profile).into(messageViewHolder.profileImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            messageViewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            messageViewHolder.messageText.setTextColor(Color.WHITE);
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText, fromtext;
        public CircleImageView profileImage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text_layout);
            fromtext = itemView.findViewById(R.id.name_text_layout);
            profileImage = itemView.findViewById(R.id.message_profile_layout);
        }
    }
}
