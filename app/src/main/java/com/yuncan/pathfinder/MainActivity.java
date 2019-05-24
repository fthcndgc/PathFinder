package com.yuncan.pathfinder;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.yuncan.pathfinder.adapters.ViewPagerAdapter;
import com.yuncan.pathfinder.fragments.ChatsFragment;
import com.yuncan.pathfinder.fragments.FriendsFragment;
import com.yuncan.pathfinder.fragments.RequestsFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater ınflater = getMenuInflater();
        ınflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_exit){
            mNotificationDatabase.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dataSnapshot.getRef().child("cihazID").setValue("cevrimdisi");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mAuth.signOut();
            Intent ıntent = new Intent(getApplicationContext(),GirisYap.class);
            startActivity(ıntent);
        }else if (item.getItemId() == R.id.menu_users){
            Intent ıntent = new Intent(getApplicationContext(),UsersActivity.class);
            startActivity(ıntent);
        }else if (item.getItemId() == R.id.menu_profile){
            Intent ıntent = new Intent(getApplicationContext(),Profil.class);
            startActivity(ıntent);
        }else if (item.getItemId() == R.id.menu_harita){
            Intent ıntent = new Intent(getApplicationContext(),MapsActivity.class);
            startActivity(ıntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private FirebaseAuth mAuth;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    DatabaseReference mNotificationDatabase;
    private DatabaseReference mUserRef;
    private Toolbar mToolbar;
    String seslekontrol;
    TextToSpeech t1;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //seslekontrol = getIntent().getStringExtra("seslekontrol");
        mToolbar = findViewById(R.id.users_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("PathFinder");
        mAuth = FirebaseAuth.getInstance();
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notification");
        if (mAuth.getCurrentUser() != null)
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(mAuth.getCurrentUser().getUid());
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0);
        tabLayout.getTabAt(2);
        tabLayout.getTabAt(1);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                t1.setLanguage(new Locale("tr","TR"));
            }
        });

    }

    @Override
    protected void onStart() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            Intent ıntent = new Intent(getApplicationContext(),GirisYap.class);
            startActivity(ıntent);
        }else{
            mUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("engel").getValue().toString().equals("true")){
                        String ses = "Lütfen gideceğiniz yeri söyleyiniz.";
                        t1.speak(ses, TextToSpeech.QUEUE_FLUSH, null);
                        Intent ıntent = new Intent(getApplicationContext(),MapsActivity.class);
                        ıntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(ıntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mUserRef.child("online").setValue("true");
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatsFragment(),"Chat");
        adapter.addFragment(new FriendsFragment(), "Arkadaşlar");
        adapter.addFragment(new RequestsFragment(), "İstekler");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }
}
