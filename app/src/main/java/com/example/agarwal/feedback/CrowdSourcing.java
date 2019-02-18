package com.example.agarwal.feedback;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.*;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
//import com.google.android.gms.maps.model.LatLng;

public class CrowdSourcing extends AppCompatActivity {

    private String[] TypeOfIncidents = {"Unusual Traffic", "Rallies", "Construction"};
    private Button btn;
    private EditText editText;
    private Spinner list;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String Incidence;
    private String Description;
    private Location location;


    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private boolean GpsOn = false;
    private String TAG = CrowdSourcing.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crowd_sourcing);
        btn = findViewById(R.id.Post);
        editText = (EditText) findViewById(R.id.Descp);
        list = findViewById(R.id.TypeOfIncidence);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, TypeOfIncidents);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        list.setAdapter(adapter);

        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Incidence = TypeOfIncidents[(int) id];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(CrowdSourcing.this, "Please Select Something", Toast.LENGTH_SHORT).show();
            }
        });

        getLocationPermission();
        LocationRequest();
        getDeviceLocation();

       btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Incidence != null) {
                    Map<String, Object> values = new HashMap<>();
                    values.put("Incidence", Incidence);
                    //DeviceLocation deviceLocation = new DeviceLocation(CrowdSourcing.this);
                    //location = deviceLocation.getDeviceLocation();

                    if (location != null) {
                        values.put("Lat", location.getLatitude());
                        values.put("Lng", location.getLongitude());
                    } else {
                        // Toast.makeText(this, "DeviceLocation empty", Toast.LENGTH_SHORT).show();
                    }

                    Description = editText.getText().toString();

                    if (Description != null) {
                        values.put("Desc", Description);
                    }
                    Post(values);
                    startActivity(new Intent(CrowdSourcing.this, MapsActivity.class));

                }

            }
        });

       }

    public void onResume(){
        super.onResume();

        if(!GpsOn)
            LocationRequest();
        //requestLocationUpdates();
    }

    public void Post(Map<String, Object> values) {

            db.collection("CrowdSourceData")
                    .add(values)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(CrowdSourcing.this, "Successful", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CrowdSourcing.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {

                LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                List<String> providers = mLocationManager.getProviders(true);
                Location bestLocation = null;
                for (String provider : providers) {
                    Location l = mLocationManager.getLastKnownLocation(provider);
                    if (l == null) {
                        continue;
                    }
                    if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                        // Found best last known location: %s", l);
                        bestLocation = l;
                    }
                }
                location  =  bestLocation;
               /* Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            location = task.getResult();

                        } else {
                            Toast.makeText(CrowdSourcing.this, "Please Enable GPS", Toast.LENGTH_SHORT).show();
                            LocationRequest();
                        }
                    }
                });*/
                Toast.makeText(this, "Location = " + location.getLongitude(), Toast.LENGTH_SHORT).show();

            }
            else
                LocationRequest();
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }
    protected void LocationRequest() {
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
                        resolvable.startResolutionForResult(CrowdSourcing.this,
                                0);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    }

  /*  private void requestLocationUpdates() {

        if(!GpsOn)
            LocationRequest();

        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      //  final String path = getString(R.string.firebase_path) + "/" + getString(R.string.transport_id);


        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase

            mFusedLocationProviderClient.requestLocationUpdates(request, new LocationCallback() {
                @Override

                public void onLocationResult(LocationResult locationResult) {
                    Location loc = locationResult.getLastLocation();
                    if (loc != null) {
                        location = loc;
                        Log.d(TAG, "location update " + location);
                    }
                }
            }, null);
        }
    }*/

}
