package com.example.agarwal.feedback;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static android.provider.SettingsSlicesContract.KEY_LOCATION;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private CameraPosition mCameraPosition;
    private GoogleMap mMap;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private boolean mLocationGranted = true;
    private LatLng mDefaultLocation = new LatLng(18.732, 73.234);
    private int DefaultZoom = 20;
    private Spinner mSpinner;

    private ArrayList<String> terraintypes = new ArrayList() {{
        add("None");
        add("Traffic");
    }};
    private boolean GpsOn;

    public MapsActivity() {
    }

    public MapsActivity(CameraPosition mCameraPosition, GeoDataClient mGeoDataClient, PlaceDetectionClient mPlaceDetectionClient) {
        this.mCameraPosition = mCameraPosition;
        this.mGeoDataClient = mGeoDataClient;
        this.mPlaceDetectionClient = mPlaceDetectionClient;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSpinner = (Spinner) findViewById(R.id.layers_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, terraintypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mDefaultLocation = new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

    /*    FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mCurrentUser = firebaseAuth.getCurrentUser();
                if (mCurrentUser != null) {
                    // User is signed in
                    String UserId = mCurrentUser.getUid();
                    user = mCurrentUser;
                    Toast.makeText(MapsActivity.this, "USER ID\n" + UserId, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, "no id got", Toast.LENGTH_SHORT).show();
                }

            }
        };
*/
        mGeoDataClient = Places.getGeoDataClient(this);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       // mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));

        getlocationpermission();
        RequestGps();
        getdevicelocation();
        requestLocationUpdates();
    }

    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, "Map not ready", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateMapType() {
        if (mMap == null) {
            return;
        }

        String layerName = ((String) mSpinner.getSelectedItem());
        if (layerName.equals("Traffic")) {
            updateTraffic();
        } else if (layerName.equals("None")) {
            mMap.setTrafficEnabled(false);
        }

    }

    private void updateTraffic() {
        if (!checkReady()) {
            return;
        }
        mMap.setTrafficEnabled(true);
    }
    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            // final String finalEmail = email;
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        // Toast.makeText(MapsActivity.this, "jkdnlkdnglkdng", Toast.LENGTH_SHORT).show();
                        Log.d("Update to fb", "location update " + location);
                        pushLocationToFirebase(location);
                    }
                }
            }, null);
        }
    }

    void pushLocationToFirebase(Location lo){

        try{
            FirebaseFirestore fb = FirebaseFirestore.getInstance();
            if(fb == null ){
                // Toast.makeText(this, "Null returned from firestore", Toast.LENGTH_SHORT).show();
                Log.d("NULL" , "NULL VALUES RETURNED");
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user == null)
            { Toast.makeText(this, "null returned from user", Toast.LENGTH_SHORT).show();
                return;}
            String email = user.getEmail();

            DocumentReference ref = fb.collection("Users").document(email);
                ref.set(lo);
        }
        catch (Exception e){
            Log.d("error", e.getMessage());
        }
    }

    void getlocationpermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationGranted = true;
            mMap.setMyLocationEnabled(true);
        } else {
            mLocationGranted = false;
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    protected void RequestGps() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        builder.setAlwaysShow(true); //this is the key ingredient

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                GpsOn = true;
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                GpsOn = false;
                if (e instanceof ResolvableApiException) {
                    // DeviceLocation settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                0);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    }


    void getdevicelocation() {
        try {
            if (mLocationGranted) {
                mMap.setMyLocationEnabled(true);
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            LatLng latlang = new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlang
                                    , DefaultZoom));

                        } else {
                            Log.d("Trial", "Current location is null. Using defaults.");
                            Log.e("Trial", "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DefaultZoom));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationGranted = true;
                }
            }
        }
        updateLocationUI();
    }


    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (!checkReady()) {
            Toast.makeText(this, "Exit from updateLocationUI", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (mLocationGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                getlocationpermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        updateMapType();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
