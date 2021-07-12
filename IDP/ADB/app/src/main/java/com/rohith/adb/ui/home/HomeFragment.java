package com.rohith.adb.ui.home;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rohith.adb.MainActivity;
import com.rohith.adb.R;

public class HomeFragment extends Fragment  implements OnMapReadyCallback {

    private MapView mapView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mapView = root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        root.findViewById(R.id.startLocButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = true;
            }
        });
        root.findViewById(R.id.destLocBut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = false;
            }
        });
        mapView.getMapAsync(this);
        return root;
    }
    private boolean isStart = true;
    public static Location locStart = new Location(""), locDest = new Location(""), locBot = new Location("");
    private Marker startMark, destMark, botMark;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (marker == startMark){
                    locStart.setLatitude(marker.getPosition().latitude);
                    locStart.setLongitude(marker.getPosition().longitude);
                    DatabaseReference lat = MainActivity.mDatabase.getReference("sloc/lat");
                    lat.setValue(marker.getPosition().latitude);
                    DatabaseReference lon = MainActivity.mDatabase.getReference("sloc/lon");
                    lon.setValue(marker.getPosition().longitude);
                } else {
                    locDest.setLatitude(marker.getPosition().latitude);
                    locDest.setLongitude(marker.getPosition().longitude);
                    DatabaseReference lat = MainActivity.mDatabase.getReference("dloc/lat");
                    lat.setValue(marker.getPosition().latitude);
                    DatabaseReference lon = MainActivity.mDatabase.getReference("dloc/lon");
                    lon.setValue(marker.getPosition().longitude);
                }
                Toast.makeText(HomeFragment.this.getContext(), (isStart ? "Start : " : "Dest : ") + " (" + marker.getPosition().latitude + ", " + marker.getPosition().longitude + ")", Toast.LENGTH_SHORT).show();
            }
        });
        startMark = googleMap.addMarker(new MarkerOptions().title("Start").position(new LatLng(locStart.getLatitude(), locStart.getLatitude())).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).alpha(0.9f));
        destMark = googleMap.addMarker(new MarkerOptions().title("Destination").position(new LatLng(locDest.getLatitude(), locDest.getLatitude())).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).alpha(0.9f));
        botMark = googleMap.addMarker(new MarkerOptions().title("Bot location").position(new LatLng(locBot.getLatitude(), locBot.getLatitude())).draggable(false).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).alpha(0.9f));
        DatabaseReference botLat = MainActivity.mDatabase.getReference("cloc/lat");
        DatabaseReference botLon = MainActivity.mDatabase.getReference("cloc/lon");
        botLat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LatLng loc = new LatLng(locBot.getLatitude(), locBot.getLatitude());
                locBot.setLatitude((double)dataSnapshot.getValue());
                botMark.setPosition(loc);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        botLon.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LatLng loc = new LatLng(locBot.getLatitude(), locBot.getLatitude());
                locBot.setLongitude((double)dataSnapshot.getValue());
                botMark.setPosition(loc);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isStart){
                    locStart.setLatitude(latLng.latitude);
                    locStart.setLongitude(latLng.longitude);
                    DatabaseReference lat = MainActivity.mDatabase.getReference("sloc/lat");
                    lat.setValue(latLng.latitude);
                    DatabaseReference lon = MainActivity.mDatabase.getReference("sloc/lon");
                    lon.setValue(latLng.longitude);
                    startMark.setPosition(latLng);
                } else {
                    locDest.setLatitude(latLng.latitude);
                    locDest.setLongitude(latLng.longitude);
                    DatabaseReference lat = MainActivity.mDatabase.getReference("dloc/lat");
                    lat.setValue(latLng.latitude);
                    DatabaseReference lon = MainActivity.mDatabase.getReference("dloc/lon");
                    lon.setValue(latLng.longitude);
                    destMark.setPosition(latLng);
                }
                Toast.makeText(HomeFragment.this.getContext(), (isStart ? "Start : " : "Dest : ") + " (" + latLng.latitude + ", " + latLng.longitude + ")", Toast.LENGTH_SHORT).show();
            }
        });
       /*
       //in old Api Needs to call MapsInitializer before doing any CameraUpdateFactory call
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
       */

        // Updates the location and zoom of the MapView
        /*CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
        map.animateCamera(cameraUpdate);*/
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(17.59762662, 78.1260464), 18));


    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}