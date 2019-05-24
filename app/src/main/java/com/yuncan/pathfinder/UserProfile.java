package com.yuncan.pathfinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity {

    private ImageView profileImage;
    private TextView kullaniciadi, arkadassayisi;
    private Button arkadasEkle, istekIptal;

    private DatabaseReference mUsersDatabase;

    private DatabaseReference mUserDatabaseGiren;

    private DatabaseReference mFriendReqDatabase;

    private DatabaseReference mNotificationDatabase;

    private DatabaseReference mFriendDatabase;

    private FirebaseUser mCurrent_User;

    private ProgressDialog mProgressDialog;

    private String mCurrent_state;

    Toolbar mToolbar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        final String user_id = getIntent().getStringExtra("userid");

        mToolbar = findViewById(R.id.userprofil_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Kullanıcı Profili");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        mAuth = FirebaseAuth.getInstance();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user_id);
        mUsersDatabase.keepSynced(true);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        mFriendReqDatabase.keepSynced(true);
        mCurrent_User = FirebaseAuth.getInstance().getCurrentUser();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendDatabase.keepSynced(true);
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notification").child(user_id);
        mNotificationDatabase.keepSynced(true);

        profileImage = findViewById(R.id.userprofile_image);
        kullaniciadi = findViewById(R.id.userprofile_kullaniciadi);
        arkadasEkle = findViewById(R.id.userprofile_sendfriend);
        istekIptal = findViewById(R.id.userprofile_declinerequest);

        mCurrent_state = "not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        istekIptal.setVisibility(View.INVISIBLE);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String strkullaniciadi = dataSnapshot.child("kullaniciadi").getValue().toString();
                String strimage = dataSnapshot.child("image").getValue().toString();

                kullaniciadi.setText(strkullaniciadi);
                Picasso.get().load(strimage).placeholder(R.drawable.profile).into(profileImage);

                //-----------FRIENDS LIST / REQUEST FEATURE ------------------
                mFriendReqDatabase.child(mCurrent_User.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")){
                                mCurrent_state = "req_received";
                                arkadasEkle.setText("Accept Friend Request");
                                istekIptal.setVisibility(View.VISIBLE);

                            }else if (req_type.equals("sent")){
                                mCurrent_state = "req_sent";
                                arkadasEkle.setText("Cancel Friend Request");
                                istekIptal.setVisibility(View.INVISIBLE);
                            }
                            mProgressDialog.dismiss();
                        }else{
                            mFriendDatabase.child(mCurrent_User.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)){
                                        mCurrent_state = "friends";
                                        arkadasEkle.setText("Unfriend this Person");
                                        istekIptal.setVisibility(View.INVISIBLE);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

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

        arkadasEkle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arkadasEkle.setEnabled(false);
                if (mCurrent_state.equals("friends")){
                    mFriendDatabase.child(mCurrent_User.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrent_User.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(UserProfile.this, "Başarı ile silindi.", Toast.LENGTH_SHORT).show();
                                    arkadasEkle.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    arkadasEkle.setText("Send Friend Request");
                                }
                            });
                        }
                    });
                }
                if (mCurrent_state.equals("req_sent")){
                    mFriendReqDatabase.child(mCurrent_User.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_User.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    arkadasEkle.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    arkadasEkle.setText("Send Friend Request");
                                }
                            });
                        }
                    });
                }

                if (mCurrent_state.equals("req_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrent_User.getUid()).child(user_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrent_User.getUid()).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendReqDatabase.child(mCurrent_User.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendReqDatabase.child(user_id).child(mCurrent_User.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    arkadasEkle.setEnabled(true);
                                                    mCurrent_state = "friends";
                                                    arkadasEkle.setText("Unfriend this Person");
                                                    istekIptal.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }


                if (mCurrent_state.equals("not_friends")){
                    mFriendReqDatabase.child(mCurrent_User.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mFriendReqDatabase.child(user_id).child(mCurrent_User.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mCurrent_state = "req_sent";
                                        arkadasEkle.setText("Cancel Friend Request");

                                        Toast.makeText(UserProfile.this, "İstek gönderildi.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                Toast.makeText(UserProfile.this, "Arkadaş ekleme başarısız.", Toast.LENGTH_SHORT).show();
                            }
                            arkadasEkle.setEnabled(true);
                        }
                    });
                    mNotificationDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                try {
                                    OneSignal.postNotification(new JSONObject("{'contents': {'en':'Bir yeni arkadaşlık isteği'}, 'include_player_ids': ['" + ds.getValue() + "']}"), null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
        
        istekIptal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendReqDatabase.child(mCurrent_User.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendReqDatabase.child(user_id).child(mCurrent_User.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(UserProfile.this, "Istek iptal edildi.", Toast.LENGTH_SHORT).show();
                                arkadasEkle.setEnabled(true);
                                mCurrent_state = "not_friends";
                                arkadasEkle.setText("Send Friend Request");
                                istekIptal.setVisibility(View.INVISIBLE);
                            }
                        });  
                    }
                });
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent ıntent = new Intent(getApplicationContext(),MainActivity.class);
        ıntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(ıntent);
    }
}
