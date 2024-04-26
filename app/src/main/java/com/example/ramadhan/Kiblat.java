package com.example.ramadhan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.ramadhan.kiblat.Compass;
import com.example.ramadhan.kiblat.GPSTracker;
import android.Manifest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Kiblat extends AppCompatActivity {

    private static final String TAG = "CompasActivity";
    private Compass compass;
    private RelativeLayout arrowViewQiblat;
    private ImageView imageDial;
    private float currentAzimuth;
    SharedPreferences prefs;
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kiblat);

        prefs = getSharedPreferences("", MODE_PRIVATE);
        arrowViewQiblat = (RelativeLayout) findViewById(R.id.main_image_qiblat);
        gps = new GPSTracker(this);
        imageDial = (ImageView) findViewById(R.id.main_image_dial);

        arrowViewQiblat.setVisibility(View.INVISIBLE);
        arrowViewQiblat.setVisibility(View.GONE);

        setupCompass();
        fetch_GPS();
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG, "start compas");
        if (compass != null){
            compass.start();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "stop compass");
        if (compass != null){
            compass.stop();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (compass != null){
            compass.stop();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (compass != null){
            compass.start();
        }
    }

    private void setupCompass() {
        Boolean permission_granted = GetBoolean("permission_granted");
        if(permission_granted) {
            getBearing();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
        }

        compass = new Compass(this);
        Compass.CompassListener c1 = new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(float azimuth) {
                adjustGambarDial(azimuth);
                adjustArrowQiblat(azimuth);
            }
        };
        compass.setListener(c1);
    }

    public void adjustGambarDial(float azimuth) {
        Log.d(TAG, "will set rotation from " + currentAzimuth + " to "                + azimuth);

        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = (azimuth);
        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        imageDial.startAnimation(an);
    }
    public void adjustArrowQiblat(float azimuth) {
        Log.d(TAG, "will set rotation from " + currentAzimuth + " to "                + azimuth);

        float kiblat_derajat = GetFloat("kiblat_derajat");
        Animation an = new RotateAnimation(-(currentAzimuth)+kiblat_derajat, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = (azimuth);
        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        arrowViewQiblat.startAnimation(an);
        if(kiblat_derajat > 0){
            arrowViewQiblat.setVisibility(View.VISIBLE);
        }else{
            arrowViewQiblat.setVisibility(View.INVISIBLE);
            arrowViewQiblat.setVisibility(View.GONE);
        }
    }

    @SuppressLint("Missing Permission")
    public void getBearing(){
        float kiblat_derajat = GetFloat("kiblat_derajat");
        if (kiblat_derajat > 0.0001){
            arrowViewQiblat.setVisibility(View.VISIBLE);
        } else {
            fetch_GPS();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResult) {
        switch (requestCode) {
            case 1: {
                if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    SaveBoolean("permission_granted", true);
//                    onStart();
                    arrowViewQiblat.setVisibility(View.INVISIBLE);
                    arrowViewQiblat.setVisibility(View.GONE);
                    setupCompass();
                } else {
                    Toast.makeText(getApplicationContext(), "Permision not allowed!", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
    }

    public void SaveBoolean(String Judul, Boolean bbb){
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(Judul, bbb);
        edit.apply();
    }

    public Boolean GetBoolean(String Judul) {
        Boolean result = prefs.getBoolean(Judul, false);
        return result;
    }

    public void SaveFloat(String Judul, Float bbb){
        SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat(Judul, bbb);
        edit.apply();
    }

    public Float GetFloat(String Judul){
        Float xxxxxx = prefs.getFloat(Judul, 0);
        return xxxxxx;
    }

    public void fetch_GPS(){
        double result = 0;
        gps = new GPSTracker(this);
        if (gps.canGetLocation()){
            Log.e("Tag", "GPS is On");
            double lat_saya = gps.getLatitude();
            double lon_saya = gps.getLongtitude();

            if (lat_saya < 0.001 && lon_saya < 0.001){
                arrowViewQiblat.setVisibility(View.INVISIBLE);
                arrowViewQiblat.setVisibility(View.GONE);
            } else {
                double longitude2 = 39.826206;
                double longitude1 = lon_saya;
                double latitude2 = Math.toRadians(21.422487);

                double latitude1 = Math.toRadians(lat_saya);
                double longDiff = Math.toRadians(longitude2 - longitude1);
                double y = Math.sin(longDiff) * Math.cos(latitude2);
                double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);
                result = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
                float result2 = (float) result;
                SaveFloat("kiblat_derajat", result2);
                arrowViewQiblat.setVisibility(View.VISIBLE);
            }
        } else {
            gps.showSettingsAlert();

            arrowViewQiblat.setVisibility(View.INVISIBLE);
            arrowViewQiblat.setVisibility(View.GONE);
        }
    }
}