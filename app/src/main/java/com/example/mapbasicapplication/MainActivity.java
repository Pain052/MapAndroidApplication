package com.example.mapbasicapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    SupportMapFragment mapFragment;
    GoogleMap map;
    SearchView searchView;
    FusedLocationProviderClient providerClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        checkPermission();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> list = null;
                Geocoder geocoder = new Geocoder(MainActivity.this);
                try {
                    list = geocoder.getFromLocationName(location, 1);
                }catch(Exception e){
                    e.printStackTrace();
                }
                Address address = list.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                map.addMarker(new MarkerOptions().position(latLng).title(location));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);
    }

    private void initView(){
        searchView = findViewById(R.id.svLocation);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fgMap);
        providerClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //when permission granted then call this method
            getCurrentLocation();
        }else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }
    }

    private void getCurrentLocation() {
        //Check permission after when get location position
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            return;
        }
        Task<Location> task = providerClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            //when success
            if (location != null){
                mapFragment.getMapAsync(googleMap -> {
                    //init LatLng
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //Create maker options
                    MarkerOptions options = new MarkerOptions().position(latLng).title("Location position!");
                    //Zoom map
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    //Add marker on map
                    googleMap.addMarker(options);

                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //when permission granted then call this method
                getCurrentLocation();
            }
        }
    }
}