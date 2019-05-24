package com.yuncan.pathfinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class Profil extends AppCompatActivity {

    TextView kullaniciadi, imei;
    ImageView mProfileImage;
    Button profilFotoDegis;
    private FirebaseAuth mAuth;
    DatabaseReference myRef;
    FirebaseDatabase database;
    private ProgressDialog mProgressDialog;
    private static final int GALERY_PICK = 1;
    private StorageReference mImageStorage;
    byte[] thumb_byte;
    String image;

    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        mToolbar = findViewById(R.id.userprofil_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        kullaniciadi = findViewById(R.id.userprofile_kullaniciadi);
        imei = findViewById(R.id.profile_imei);
        profilFotoDegis = findViewById(R.id.profile_profileimagechange);
        mProfileImage = findViewById(R.id.profile_imageview);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Kullanicilar").child(mAuth.getUid());
        mImageStorage = FirebaseStorage.getInstance().getReference();
        myRef.keepSynced(true);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Yükleniyor.");
        mProgressDialog.setMessage("Kullanıcı bilgilerinizi yüklerken bekleyiniz.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                kullaniciadi.setText(dataSnapshot.child("kullaniciadi").getValue().toString());
                imei.setText(dataSnapshot.child("imei").getValue().toString());
                image = dataSnapshot.child("image").getValue().toString();
                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(mProfileImage);
                    }
                });
                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        profilFotoDegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"),GALERY_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mProgressDialog.show();
        if (requestCode == GALERY_PICK && resultCode ==  RESULT_OK){
            Uri imageUri = data.getData();
            File thumb_filePath = new File(imageUri.toString());
            try {
                Bitmap thumb_bitmap = new Compressor((Profil.this))
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                thumb_byte = baos.toByteArray();
            }catch (IOException e){
                e.printStackTrace();
            }
            String current_userid = mAuth.getUid();
            final StorageReference filepath = mImageStorage.child("profile_images").child(current_userid+".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    String thumb_downloadUrl = task.getResult().getDownloadUrl().toString();
                    if (task.isSuccessful()){
                        Map update_hashMap = new HashMap();
                        update_hashMap.put("image",thumb_downloadUrl);
                        myRef.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(Profil.this, "Güncelleme Başarılı.", Toast.LENGTH_SHORT).show();
                                    mProgressDialog.dismiss();
                                }
                            }
                        });
                    }
                }
            });
        }
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
