package com.example.ramadhan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.VolumeShaper;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ramadhan.model.Items;
import com.example.ramadhan.model.Jadwal;
import com.example.ramadhan.network.ApiClient;
import com.example.ramadhan.network.ApiInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    String zuhur, ashar, magrib, isya, subuh, tanggal;
    List<Jadwal> jadwal;
    Location location;
    String lokasi;

//    @BindView(R.id.tv_nama_daerah)
    TextView tvNamaDaerah;

//   LocationUtils locationUtils; Ga usah dinyalain

    String text;
//    @BindView(R.id.img_subuh)
    ImageView imgSubuh;
//    @BindView(R.id.img_zuhur)
    ImageView imgZuhur;
//    @BindView(R.id.img_ashar)
    ImageView imgAshar;
//    @BindView(R.id.img_magrhib)
    ImageView imgMagrhib;
//    @BindView(R.id.img_isya)
    ImageView imgIsya;
//    @BindView(R.id.tv_tanngal)
    TextView tvTangal;
//    @BindView(R.id.textView2)
    TextView textView2;
//    @BindView(R.id.txtSubuh)
    TextView txtSubuh;
//    @BindView(R.id.textView4)
    TextView textView4;
//    @BindView(R.id.txtDzuhur)
    TextView txtDzuhur;
//    @BindView(R.id.textView5)
    TextView textView5;
//    @BindView(R.id.txtAshar)
    TextView txtAshar;
//    @BindView(R.id.textView6)
    TextView textView6;
//    @BindView(R.id.txtMaghrib)
    TextView txtMaghrib;
//    @BindView(R.id.textView7)
    TextView textView7;
//    @BindView(R.id.txtIsya)
    TextView txtIsya;
//    @BindView(R.id.swipe_id)
    SwipeRefreshLayout swipeId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        actionLoad();
    }

    private void actionLoad() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (!EasyPermissions.hasPermissions(getApplicationContext(), perms)) {
            EasyPermissions.requestPermissions(MainActivity.this, "Need Location Permission", 10, perms);
        } else {
            FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Log.d("My current Location", "Lat: " + location.getLatitude() + "Long: " + location.getLongitude());
                        Geocoder gcd3 = new Geocoder(getBaseContext(), Locale.getDefault());
                        List<Address> addresses;

                        try {
                            addresses = gcd3.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses.size() > 0) {
                                Log.d("Cek lokasi", "1 :" + addresses.get(0).getLocality().toString());
                                lokasi = addresses.get(0).getLocality().toString();
                                tvNamaDaerah.setText(lokasi);
                                swipeId.setRefreshing(false);
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            if (lokasi != null) {
                Log.d("Location", "lokasi: " + lokasi);

                ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                Call<Items> call = apiInterface.getJadwalSholat(lokasi);
                call.enqueue(new Callback<Items>() {
                    @Override
                    public void onResponse(Call<Items> call, Response<Items> response) {
                        Log.d("Data ", " respon" + response.body().getItems());
                        jadwal = response.body().getItems();
                        Log.d("respons data", "" + new Gson().toJson(jadwal));

                        if (jadwal != null) {
                            zuhur = jadwal.get(0).getZuhur();
                            ashar = jadwal.get(0).getAshar();
                            magrib = jadwal.get(0).getMaghrib();
                            isya = jadwal.get(0).getIsya();
                            subuh = jadwal.get(0).getSubuh();
                            tanggal = jadwal.get(0).getTanggal();
                            Log.d("respons: ", "" + zuhur);
                            txtDzuhur.setText(zuhur);
                            txtAshar.setText(ashar);
                            txtMaghrib.setText(magrib);
                            txtIsya.setText(isya);
                            txtSubuh.setText(subuh);
                            tvNamaDaerah.setText(lokasi);
                            tvTangal.setText(tanggal);
                            swipeId.setRefreshing(false);
                        } else {
                            Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                            swipeId.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<Items> call, Throwable t) {
                        Log.d("Data", "" + t.getMessage());
                        swipeId.setRefreshing(false);
                    }
                });
            } else {
                Toast.makeText(this, "Swipe Layar Untuk Refresh", Toast.LENGTH_SHORT).show();
                swipeId.setRefreshing(false);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == 10) {
            actionLoad();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

//    @OnClick(R.id.img_zuhur)
//    public void onImgZuhurClicked() {
//    }
//
//    @OnClick(R.id.img_ashar)
//    public void onImgAsharClicked() {
//    }
//
//    @OnClick(R.id.img_magrhib)
//    public void onImgMagrhibClicked() {
//    }
//
//    @OnClick(R.id.img_isya)
//    public void onImgIsyaClicked() {
//    }
//
//    @OnClick(R.id.img_arah_kabah)
//    public void onImgArahKabahClicked() {
//        startActivity(new Intent(MainActivity.this, ArahKiblatActivity.class));
//    }
//
//    @OnClick(R.id.img_al_quran)
//    public void onImgAlQuranClicked() {
//    }
//
//    @OnClick(R.id.img_status_ramadhan)
//    public void onImgStatusRamadhanClicked() {
//    }
}