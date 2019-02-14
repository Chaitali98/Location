package com.example.agarwal.feedback;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.protobuf.DescriptorProtos;

import java.util.HashMap;
import java.util.Map;


public class construction extends Activity {

    FusedLocationProviderClient fusedLocationProviderClient;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static String description = "description";
    public static String location;

    public String descp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_construction);

        DisplayMetrics dp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);

        int width = dp.widthPixels;
        int height = dp.heightPixels;

        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.4));


      EditText editText   = findViewById(R.id.editText);
        Button btn = findViewById(R.id.submit);

       descp =   editText.getText().toString();
    }
    public void add_to_db(String desc) {

        Map<String, Object> values = new HashMap<>();

        desc = "traffic";
        if (!desc.isEmpty())
            values.put(description, desc);
        // values.put(description,location);

        if (db != null) {

            db.collection("user").add(values)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(construction.this, "Added to firebase successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(construction.this, "Error uploading to firebase", Toast.LENGTH_SHORT).show();
                        }
                    });

            Toast.makeText(this, "after button click", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Firestore null", Toast.LENGTH_SHORT).show();
        }
    }

    public void Add_Desp(View view) {
      //  Toast.makeText(this, "after click", Toast.LENGTH_SHORT).show();
        add_to_db(descp);
    }
}
