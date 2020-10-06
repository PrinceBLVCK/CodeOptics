package com.example.codeoptics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import Prevelant.Prevelant;
import de.hdodenhof.circleimageview.CircleImageView;

public class dashboard extends AppCompatActivity {
    TextView username, p;
    Uri imgUrl;
    CircleImageView Profile_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        username = (TextView)findViewById(R.id.username);
        Profile_img = (CircleImageView)findViewById(R.id.img_preview);


        username = (TextView)findViewById(R.id.username);
        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(dashboard.this, test.class));
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();

        username.setText(Prevelant.currentOnlineUser.getUsername());





    }

}