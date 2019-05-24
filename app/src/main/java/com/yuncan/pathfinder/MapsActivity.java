package com.yuncan.pathfinder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, GoogleMap.OnMapLongClickListener{

    private GoogleMap mMap;
    private DatabaseReference myRef;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private FirebaseAuth mAuth;
    Button signout;
    Circle circle;
    LocationListener locationListener;
    LocationManager locationManager;
    String provider,hedefstring;
    double enlem, boylam;
    Marker eskikonum;
    Marker hedef;
    String value;
    String durum;
    Vibrator titre;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 2;
    private static final int VOICE_RECOGNITION_REQUEST_CODE2 = 3;
    private static final int VOICE_RECOGNITION_REQUEST_CODE3 = 4;
    TextToSpeech t1;
    CountDownTimer sayac;
    int cikissayac = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mAuth = FirebaseAuth.getInstance();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                t1.setLanguage(new Locale("tr","TR"));
            }
        });
        if (mAuth.getCurrentUser() != null){
            myRef = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(mAuth.getUid()).child("engel");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    value = dataSnapshot.getValue().toString();
                    if (value.equals("true")){
                        sayac = new CountDownTimer(2000,1000){

                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
                                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Lütfen gideceğiniz yeri söyleyiniz.");
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"tr");
                                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                                startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
                            }
                        }.start();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        titre = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            //locationListener.onLocationChanged(location);
        } else {
            Toast.makeText(this, "Not Avaible", Toast.LENGTH_SHORT).show();
        }
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //eskikonum.remove();
                circle.remove();
                enlem = location.getLatitude();
                boylam = location.getLongitude();
                LatLng sydney = new LatLng(enlem, boylam);
                //eskikonum = mMap.addMarker(new MarkerOptions().position(sydney).title("Konum"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15));
                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(enlem,boylam))
                        .radius(100)
                        .strokeColor(Color.RED));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(MapsActivity.this, "Enable provider.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(MapsActivity.this, "Disable provider", Toast.LENGTH_SHORT).show();
            }

        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            locationManager.requestLocationUpdates(provider,1000,1,locationListener);
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(enlem, boylam);
        //eskikonum = mMap.addMarker(new MarkerOptions().position(sydney).title("Konum"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15));

        circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(enlem,boylam))
                .radius(100)
                .strokeColor(Color.RED));
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        final LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        if (circle != null)
            circle.remove();
        circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(latLng.latitude,latLng.longitude))
                .radius(100)
                .strokeColor(Color.RED));
        if (hedef != null) {
            //Toast.makeText(MapsActivity.this, String.valueOf(hedef.getPosition().latitude), Toast.LENGTH_SHORT).show();
            if ((hedef.getPosition().latitude + 0.0009 > mLastLocation.getLatitude()) && (hedef.getPosition().latitude - 0.0009 < mLastLocation.getLatitude()) && (hedef.getPosition().longitude + 0.0009 > mLastLocation.getLongitude()) && (hedef.getPosition().longitude - 0.0009 < mLastLocation.getLongitude())) {
                hedef.remove();
                //Toast.makeText(this, "dettkejthergfdfghjmkmnhbgvc", Toast.LENGTH_SHORT).show();
                titre.vibrate(800);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 1000, 1, locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (value.equals("true")){
            String ses = "Lütfen gideceğiniz yeri söyleyiniz.";
            t1.speak(ses, TextToSpeech.QUEUE_FLUSH, null);
            sayac = new CountDownTimer(4000,1000){

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Lütfen gideceğiniz yeri söyleyiniz.");
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"tr");
                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                    startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
                }
            }.start();
        }else{
            if (hedef != null)
                hedef.remove();
            hedef = mMap.addMarker(new MarkerOptions().position(latLng).title("Hedef"));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            hedefstring = matches.get(0).toString();
            Toast.makeText(this, hedefstring, Toast.LENGTH_SHORT).show();
            String ses = "Gitmek istediğiniz yer "+hedefstring+" onaylıyor musunuz? Evet veya hayır!";
            t1.speak(ses, TextToSpeech.QUEUE_FLUSH, null);
            sayac = new CountDownTimer(6000,1000){

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Gitmek istediğiniz yer "+hedefstring+" onaylıyor musunuz? Evet veya hayır!");
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"tr");
                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                    startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE2);
                }
            }.start();
        }
        else if (requestCode == VOICE_RECOGNITION_REQUEST_CODE2 && resultCode == RESULT_OK){
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            durum = matches.get(0).toString().toLowerCase();
            if (durum.equals("evet")){
                Toast.makeText(this, hedefstring, Toast.LENGTH_SHORT).show();
                if (hedef != null)
                    hedef.remove();
                List<Address> addressList = null;
                if (hedefstring != null || !hedefstring.equals("")) {
                    Geocoder geocoder = new Geocoder(this);
                    try {
                        addressList = geocoder.getFromLocationName(hedefstring, 1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    hedef = mMap.addMarker(new MarkerOptions().position(latLng).title("Hedef"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }else{
                String ses = "Lütfen gideceğiniz yeri söyleyiniz.";
                t1.speak(ses, TextToSpeech.QUEUE_FLUSH, null);
                sayac = new CountDownTimer(4000,1000){

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Lütfen gideceğiniz yeri söyleyiniz.");
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"tr");
                        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
                    }
                }.start();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onMapSearch(View view) {
        if (hedef != null)
            hedef.remove();
        EditText locationSearch = (EditText) findViewById(R.id.editText);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            hedef = mMap.addMarker(new MarkerOptions().position(latLng).title("Hedef"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }
    @Override
    public void onBackPressed() {
        if (value.equals("true")){
            System.exit(0);
            super.onBackPressed();
        }else{
            Intent ıntent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(ıntent);
            super.onBackPressed();
        }

    }
}
