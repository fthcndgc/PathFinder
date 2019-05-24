package com.yuncan.pathfinder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class GirisYap extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button girisYap;
    TextInputLayout kullaniciadi, sifre;
    TextView sayactext, iptaltext;
    CountDownTimer sayac;
    TextToSpeech t1;
    boolean seslekontrol = false;
    String dinle = "";
    String dinle1 = "";
    String dinle2 = "";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;
    private static final int VOICE_RECOGNITION_REQUEST_CODE1 = 2;
    private static final int VOICE_RECOGNITION_REQUEST_CODE2 = 3;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    DatabaseReference myRef;
    FirebaseDatabase database;
    TelephonyManager telephonyManager;
    String imei = "";
    DatabaseReference mNotificationDatabase;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris_yap);

        mAuth = FirebaseAuth.getInstance();
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notification");

        kullaniciadi = findViewById(R.id.giris_kullaniciadi);
        sifre = findViewById(R.id.giris_sifre);
        girisYap = findViewById(R.id.giris_girisyap);
        sayactext = findViewById(R.id.giris_sayactext);
        iptaltext = findViewById(R.id.giris_iptaltext);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                t1.setLanguage(new Locale("tr","TR"));
            }
        });

        sayac = new CountDownTimer(4000,1000){

            @Override
            public void onTick(long millisUntilFinished) {
                sayactext.setText(String.valueOf(millisUntilFinished/1000) + " saniye içerisinde sesli komuta geçilecek.");
            }

            @Override
            public void onFinish() {
                seslekontrol = true;
                String ses = "Lütfen yapmak istediğiniz işlemi söyleyiniz. Giriş Yap, Kayıt Ol ve Çıkış";
                t1.speak(ses, TextToSpeech.QUEUE_FLUSH, null);
                new CountDownTimer(6000,1000){

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        dinle();
                    }
                }.start();
            }
        }.start();

        iptaltext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sayac.cancel();
                sayactext.setText("Sesli komut iptal edildi.");
                iptaltext.setVisibility(View.INVISIBLE);
            }
        });

        girisYap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GirisYap(kullaniciadi.getEditText().getText().toString(),sifre.getEditText().getText().toString());
            }
        });
    }
    public void KayitOl(View v){
        seslekontrol = false;
        Intent ıntent = new Intent(getApplicationContext(),KayitOl.class);
        ıntent.putExtra("seslekontrol",String.valueOf(seslekontrol));
        startActivity(ıntent);
    }

    public void GirisYap(final String kullaniciadi, String sifre){
        if (kullaniciadi.isEmpty() || sifre.isEmpty()){
            if (seslekontrol)
            {
                String ses = "Kullanıcı adı veya şifre boş olamaz.";
                t1.speak(ses, TextToSpeech.QUEUE_FLUSH, null);
            }else{
                Toast.makeText(this, "Kullanıcı adı veya şifre boş olamaz.", Toast.LENGTH_SHORT).show();
            }
        }else {
            mAuth.signInWithEmailAndPassword(kullaniciadi + "@pathfinder.com", sifre)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                                    @Override
                                    public void idsAvailable(final String userId, String registrationId) {
                                        mNotificationDatabase.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                dataSnapshot.getRef().child("cihazID").setValue(userId);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                });
                                FirebaseUser user = mAuth.getCurrentUser();
                                finish();
                                if (seslekontrol){
                                    Intent ıntent = new Intent(getApplicationContext(), MapsActivity.class);
                                    ıntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    ıntent.putExtra("kullaniciadi",kullaniciadi);
                                    ıntent.putExtra("seslekontrol",String.valueOf(seslekontrol));
                                    startActivity(ıntent);
                                }else{
                                    Intent ıntent = new Intent(getApplicationContext(), MainActivity.class);
                                    ıntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    ıntent.putExtra("kullaniciadi",kullaniciadi);
                                    ıntent.putExtra("seslekontrol",String.valueOf(seslekontrol));
                                    startActivity(ıntent);
                                }

                            } else {
                                if (seslekontrol){
                                    String ses = "Bir hata oluştu. Lütfen tekrar deneyin.";
                                    t1.speak(ses, TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GirisYap.this, e.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void dinle(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Giriş Yap / Kayıt Ol / Çıkış");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"tr");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    public void dinle1() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Onaylıyor musunuz? Evet veya Hayır.");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE1);
    }

    public void dinle2() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Kullanıcı adınızı söyleyiniz.");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE2);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            seslekontrol = true;
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            dinle = matches.get(0).toString().toLowerCase();
            Toast.makeText(this, dinle, Toast.LENGTH_SHORT).show();
            if (dinle.equals("giriş yap")){
                String text = "Lütfen giriş yapmak istediğiniz kullanıcı adını giriniz.";
                t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                new CountDownTimer(5000,1000){

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        dinle2();
                    }
                }.start();
            }
            else if (dinle.equals("kayıt ol")){
                Intent ıntent = new Intent(getApplicationContext(),KayitOl.class);
                ıntent.putExtra("seslekontrol",String.valueOf(seslekontrol));
                startActivity(ıntent);
            }
            else if (dinle.equals("çıkış")){
                seslekontrol = false;
            }
            else{
                dinle();
                Toast.makeText(this, "Anlaşılamadı.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE2 && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            dinle2 = matches.get(0).toString().toLowerCase();
            String text = "Kullanıcı adınız " + dinle2 + ". Onaylıyor musunuz? Evet veya Hayır.";
            t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            new CountDownTimer(5000,1000){

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    dinle1();
                }
            }.start();
        }
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE1 && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            dinle1 = matches.get(0).toString().toLowerCase();
            if (dinle1.equals("evet")) {
                final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(GirisYap.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GirisYap.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                }
                GirisYap(dinle2,String.valueOf(telephonyManager.getDeviceId()));
                String ses = "Lütfen gideceğiniz yeri söyleyiniz.";
                t1.speak(ses, TextToSpeech.QUEUE_FLUSH, null);
            } else if (dinle1.equals("hayır")) {
                String text = "Giriş yapmak istediğiniz kullanıcı adını giriniz.";
                t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                new CountDownTimer(3000,1000){

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        dinle();
                    }
                }.start();
            } else {
                String text = "Geçersiz seçim. Lütfen tekrar belirtin. Evet veya hayır.";
                t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                new CountDownTimer(3000,1000){

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        dinle1();
                    }
                }.start();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
