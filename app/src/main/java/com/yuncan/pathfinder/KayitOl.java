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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class KayitOl extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextInputLayout kullaniciadi, sifre;
    Button girisyap, kayitol;
    DatabaseReference myRef;
    FirebaseDatabase database;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    String seslekontrol;
    TextToSpeech t1;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;
    private static final int VOICE_RECOGNITION_REQUEST_CODE1 = 2;
    String dinle = "";
    String dinle1 = "";
    private DatabaseReference mNotificationDatabase;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kayit_ol);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        mAuth = FirebaseAuth.getInstance();
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notification");

        kullaniciadi = findViewById(R.id.kayit_kullaniciadi);
        sifre = findViewById(R.id.kayit_sifre);
        kayitol = findViewById(R.id.kayit_kayitol);

        kayitol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sifre.getEditText().getText().toString().length() < 6){
                    Toast.makeText(KayitOl.this, "Şifre 6 karakterden küçük olamaz.", Toast.LENGTH_SHORT).show();
                }else{
                    KayitOl(kullaniciadi.getEditText().getText().toString(),sifre.getEditText().getText().toString());
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        seslekontrol = extras.getString("seslekontrol");

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                t1.setLanguage(new Locale("tr", "TR"));
            }
        });

        if (seslekontrol.equals("true")){
            new CountDownTimer(3000,1000){

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    String text = "Lütfen kayıt olmak istediğiniz kullanıcı adını giriniz.";
                    t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    new CountDownTimer(4000,1000){

                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            dinle("Lütfen kayıt olmak istediğiniz kullanıcı adını giriniz.");
                        }
                    }.start();
                }
            }.start();
        }
    }

    public void KayitOl(final String kullaniciadi, String sifre){
        if (kullaniciadi.isEmpty() || sifre.isEmpty()){
            if (seslekontrol.equals("true")){
                String ses = "Kullanıcıa adı veya şifre boş olamaz.";
                t1.speak(ses, TextToSpeech.QUEUE_FLUSH, null);
            }else{
                Toast.makeText(this, "Kullanıcı adı veya şifre boş olamaz.", Toast.LENGTH_SHORT).show();
            }
        }else {
            mAuth.createUserWithEmailAndPassword(kullaniciadi + "@pathfinder.com", sifre)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                                if (ActivityCompat.checkSelfPermission(KayitOl.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(KayitOl.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                                }
                                myRef = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(mAuth.getUid());
                                HashMap<String, String> userMap = new HashMap<>();
                                userMap.put("kullaniciadi", kullaniciadi);
                                userMap.put("image","default");
                                userMap.put("imei", telephonyManager.getDeviceId());
                                userMap.put("engel",seslekontrol);
                                myRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(KayitOl.this, "Kayıt başarı ile gerçekleşti.", Toast.LENGTH_SHORT).show();
                                        if (seslekontrol.equals("true")) {
                                            String text = "Kayıt başarı ile gerçekleşti.";
                                            t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                                        }else
                                            Toast.makeText(KayitOl.this, "Kayıt başarı ile gerçekleşti.", Toast.LENGTH_SHORT).show();
                                        finish();
                                        Intent ıntent = new Intent(getApplicationContext(), GirisYap.class);
                                        ıntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(ıntent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(KayitOl.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                                    @Override
                                    public void idsAvailable(final String userId, String registrationId) {
                                        HashMap<String, String> userMap = new HashMap<>();
                                        userMap.put("cihazID", userId);
                                        mNotificationDatabase.child(mAuth.getUid()).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(KayitOl.this, "Kayıt başarı ile gerçekleşti.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            } else {
                                if (seslekontrol.equals("true")){
                                    String ses = "Bir hata oluştu. Lütfen tekrar deneyin.";
                                    t1.speak(ses, TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(KayitOl.this, e.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    public void dinle(String text) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, text);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    public void dinle1(String text) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, text);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            dinle = matches.get(0).toString();
            String text = "Kullanıcı adınız " + dinle + " . Onaylıyor musunuz? Evet veya hayır.";
            t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            new CountDownTimer(5000,1000){

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    dinle1("Kullanıcı adınız doğru mu? Evet/Hayır");
                }
            }.start();
        }
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE1 && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            dinle1 = matches.get(0).toString();
            if (dinle1.equals("Evet")) {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(KayitOl.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(KayitOl.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                }
                KayitOl(dinle,String.valueOf(telephonyManager.getDeviceId()));
            } else if (dinle1.equals("Hayır")) {
                String text = "Kayıt olmak istediğiniz kullanıcı adını giriniz..";
                t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                new CountDownTimer(3000,1000){

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        dinle("Kayıt olmak istediğiniz kullanıcı adını giriniz.");
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
                        dinle1("Kullanıcı adınız doğru mu? Evet/Hayır");
                    }
                }.start();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
