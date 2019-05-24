package com.yuncan.pathfinder.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yuncan.pathfinder.ChatActivity;
import com.yuncan.pathfinder.clas.Friends;
import com.yuncan.pathfinder.R;
import com.yuncan.pathfinder.UserProfile;

public class FriendsFragment extends Fragment {

    private RecyclerView mFriendList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;

    public FriendsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_friends,container,false);
        mFriendList = (RecyclerView)mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            mCurrent_user_id = mAuth.getCurrentUser().getUid();
            mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
            mFriendsDatabase.keepSynced(true);
        }

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        mUsersDatabase.keepSynced(true);
        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                    Friends.class,
                    R.layout.users_single_layout,
                    FriendsViewHolder.class,
                    mFriendsDatabase
            ) {
                @Override
                protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, final int position) {
                    viewHolder.setDate(model.getDate());
                    String list_user_id = getRef(position).getKey();
                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("kullaniciadi").getValue().toString();
                            String userImage = dataSnapshot.child("image").getValue().toString();
                            if (dataSnapshot.hasChild("online")) {
                                String userOnline = dataSnapshot.child("online").getValue().toString();
                                viewHolder.setUserOnline(userOnline);
                            }
                            viewHolder.setName(userName);
                            viewHolder.setImage(userImage);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final String user_id = getRef(position).getKey();
                                    CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Select Options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                Intent ıntent = new Intent(getContext(), UserProfile.class);
                                                ıntent.putExtra("userid", user_id);
                                                startActivity(ıntent);
                                            }
                                            if (which == 1) {
                                                Intent ıntent = new Intent(getContext(), ChatActivity.class);
                                                ıntent.putExtra("userid", user_id);
                                                ıntent.putExtra("user_name", userName);
                                                startActivity(ıntent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            };
            mFriendList.setAdapter(friendsRecyclerViewAdapter);
        }
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDate(String date){
            TextView userDateView = (TextView)mView.findViewById(R.id.user_single_friendsdate);
            userDateView.setText(date);
        }
        public void setName(String name){
            TextView userNameView = (TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setImage(String image){
            ImageView userImageView = (ImageView)mView.findViewById(R.id.user_single_image);
            Picasso.get().load(image).placeholder(R.drawable.profile).into(userImageView);
        }

        public void setUserOnline(String online_status){
            ImageView userOnlineView = (ImageView)mView.findViewById(R.id.user_single_online);
            if (online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
