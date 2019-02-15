package com.example.agarwal.feedback;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crowd_sourcing);
        btn = findViewById(R.id.Post);
        editText = (EditText) findViewById(R.id.Descp);

        final TextView textView = findViewById(R.id.Label);


        list = findViewById(R.id.TypeOfIncidence);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, TypeOfIncidents);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        list.setAdapter(adapter);

        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Incidence = TypeOfIncidents[(int) id];

               // Toast.makeText(CrowdSourcing.this, Incidence, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(CrowdSourcing.this, "Please Select Something", Toast.LENGTH_SHORT).show();
            }
        });


        getLocationPermission();

        getDeviceLocation();

       btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Incidence != null) {
                    Map<String, Object> values = new HashMap<>();
                    values.put("Incidence", Incidence);
                    if (location != null) {
                        values.put("Lat", location.getLatitude());
                        values.put("Lng", location.getLongitude());
                    } else {
                        // Toast.makeText(this, "Location empty", Toast.LENGTH_SHORT).show();
                    }

                    Description = editText.getText().toString();

                    if (Description != null) {
                        values.put("Desc", Description);
                        textView.setText(Description);
                    }
                    Post(values);
                }

            }
        });

       }


    public void Post(Map<String, Object> values) {
          /*  db.collection("users")
                    .document("1").set(values)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(CrowdSourcing.this, "Successful", Toast.LENGTH_SHORT).show();
                        }
                    })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CrowdSourcing.this, "failed", Toast.LENGTH_SHORT).show();
                }
            }); */

            db.collection("users")
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

                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            location = task.getResult();

                        } else {
                            Toast.makeText(CrowdSourcing.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
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

}
