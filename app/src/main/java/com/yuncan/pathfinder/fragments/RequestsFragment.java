package com.yuncan.pathfinder.fragments;

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
import com.yuncan.pathfinder.R;
import com.yuncan.pathfinder.clas.Requests;
import com.yuncan.pathfinder.UserProfile;

public class RequestsFragment extends Fragment {

    private RecyclerView mRequestList;
    private DatabaseReference mFriendsReqDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_requests,container,false);
        mRequestList = (RecyclerView)mMainView.findViewById(R.id.requests_list);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mCurrent_user_id = mAuth.getCurrentUser().getUid();
            mFriendsReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request").child(mCurrent_user_id);
            mFriendsReqDatabase.keepSynced(true);
        }
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        mUsersDatabase.keepSynced(true);
        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            FirebaseRecyclerAdapter<Requests, RequestViewHolder> requestsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(
                    Requests.class,
                    R.layout.users_single_layout,
                    RequestViewHolder.class,
                    mFriendsReqDatabase
            ) {
                @Override
                protected void populateViewHolder(final RequestViewHolder viewHolder, final Requests model, final int position) {
                    String list_user_id = getRef(position).getKey();
                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (model.getRequest_type().equals("received")) {
                                String userName = dataSnapshot.child("kullaniciadi").getValue().toString();
                                userName += " /Gelen";
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                viewHolder.setName(userName);
                                viewHolder.setImage(userImage);
                            }
                            if (model.getRequest_type().equals("sent")) {
                                String userName = dataSnapshot.child("kullaniciadi").getValue().toString();
                                userName += " /Gönderilen";
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                viewHolder.setName(userName);
                                viewHolder.setImage(userImage);
                            }
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final String user_id = getRef(position).getKey();
                                    Intent ıntent = new Intent(getContext(), UserProfile.class);
                                    ıntent.putExtra("userid", user_id);
                                    startActivity(ıntent);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            };
            mRequestList.setAdapter(requestsRecyclerViewAdapter);
        }
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setName(String name){
            TextView userNameView = (TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setImage(String image){
            ImageView userImageView = (ImageView)mView.findViewById(R.id.user_single_image);
            Picasso.get().load(image).placeholder(R.drawable.profile).into(userImageView);
        }
    }

}
