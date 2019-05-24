package com.yuncan.pathfinder.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yuncan.pathfinder.ChatActivity;
import com.yuncan.pathfinder.clas.Conv;
import com.yuncan.pathfinder.R;

import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private TextToSpeech t1;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;


    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private  String data;

    private View mMainView;

    private String engel = "";

    int sayac = 0;

    // TODO: Rename and change types and number of parameters
    public ChatsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);
        mConvList = mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            mCurrent_user_id = mAuth.getCurrentUser().getUid();
            mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
            mConvDatabase.keepSynced(true);
            mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
            mMessageDatabase.keepSynced(true);
        }



        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);
        t1 = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                t1.setLanguage(new Locale("tr","TR"));
            }
        });
        if (mAuth.getCurrentUser() != null) {
            mUsersDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    engel = dataSnapshot.child("engel").getValue().toString();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            Query conversationQuery = mConvDatabase.orderByChild("timestamp");
            final FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                    Conv.class,
                    R.layout.users_single_layout,
                    ConvViewHolder.class,
                    conversationQuery
            ) {
                @Override
                protected void populateViewHolder(final ConvViewHolder viewHolder, final Conv model, int position) {
                    final String list_user_id = getRef(position).getKey();
                    Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);
                    lastMessageQuery.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            data = dataSnapshot.child("message").getValue().toString();
                            viewHolder.setMessage(data, model.isSeen());
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

                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("kullaniciadi").getValue().toString();
                            String image = dataSnapshot.child("image").getValue().toString();
                            if (dataSnapshot.hasChild("online")) {
                                String userOnline = dataSnapshot.child("online").getValue().toString();
                                viewHolder.setUserOnline(userOnline);
                            }
                            viewHolder.setName(userName);
                            viewHolder.setUserImage(image, getContext());
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent ıntent = new Intent(getContext(), ChatActivity.class);
                                    ıntent.putExtra("userid", list_user_id);
                                    ıntent.putExtra("user_name", userName);
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
            mConvList.setAdapter(firebaseConvAdapter);
        }

        sayac = 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK){

        }
    }

    public void dinle(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Bir kullanıcı seçiniz.");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"tr");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ConvViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen){
            TextView userStatusView = mView.findViewById(R.id.user_single_friendsdate);
            userStatusView.setText(message);
            userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
        }

        public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setUserImage(String image, Context ctx){
            ImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(image).placeholder(R.drawable.profile).into(userImageView);
        }

        public void setUserOnline(String online_status){
            ImageView userOnlineView = mView.findViewById(R.id.user_single_online);
            if (online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
