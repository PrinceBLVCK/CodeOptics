package com.example.codeoptics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import Prevelant.Prevelant;
import de.hdodenhof.circleimageview.CircleImageView;

public class updateUserInfo extends AppCompatActivity {

    Button updateInfo;
    EditText Username, Password, Email;
    CircleImageView profilePic;
    TextView upload_pic;

    private StorageTask uploadTask;
    private StorageReference mStorageRef;
    private String checker;
    private Uri imageUri;
    private String myUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_info);

        Username = (EditText)findViewById(R.id.username);
        Password = (EditText)findViewById(R.id.password);
        Email = (EditText)findViewById(R.id.email);
        profilePic = (CircleImageView)findViewById(R.id.img_preview);
        upload_pic = (TextView)findViewById(R.id.uploadPic);
        updateInfo = (Button)findViewById(R.id.update);

        mStorageRef = FirebaseStorage.getInstance().getReference().child("ProfilePicture");

        userInfoDisplay(Username,Password,Email,profilePic);

        upload_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checker = "clicked";
                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(updateUserInfo.this);
            }
        });

        updateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (checker.equals("clicked")){
                userInfoSaved();
            }
            else{
                updateOnlyUserInfo();
            }
            }
        });

    }


    private void updateOnlyUserInfo() {
        DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String,Object>userData = new HashMap<>();
        userData.put("username", Username.getText().toString());
        userData.put("password", Password.getText().toString());
        userData.put("email", Email.getText().toString());

        RootRef.child(Prevelant.currentOnlineUser.getUsername()).updateChildren(userData);
        startActivity(new Intent(updateUserInfo.this,dashboard.class));
        Toast.makeText(this, "Profile Successfully Updated", Toast.LENGTH_SHORT).show();
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            CropImage.ActivityResult results = CropImage.getActivityResult(data);
            imageUri = results.getUri();
            profilePic.setImageURI(imageUri);
        }
        else {
            Toast.makeText(this, "APP failed to open gallery", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(updateUserInfo.this, updateUserInfo.class ));
        }
    }

    private void userInfoSaved() {
        if (Username.getText().toString().isEmpty()){
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Password.getText().toString())){
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(Email.getText().toString())){
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (checker.equals("clicked")){
            updateImg();
        }
    }

    private void updateImg() {
        final ProgressDialog loading = new ProgressDialog(this);
        loading.setTitle("Updating Profile");
        loading.setMessage("Please wait, Loading...");
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        if (imageUri != null){
            final StorageReference imgStorage = mStorageRef.child(Prevelant.currentOnlineUser.getUsername() + ".jpg");
            uploadTask = imgStorage.getFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if (!(task.isSuccessful())){
                        throw task.getException();
                    }
                    return imgStorage.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();
                        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

                        HashMap<String,Object>userData = new HashMap<>();
                        userData.put("username", Username.getText().toString());
                        userData.put("password", Password.getText().toString());
                        userData.put("email", Email.getText().toString());
                        userData.put("Image", myUrl);

                        databaseRef.child(Prevelant.currentOnlineUser.getUsername()).updateChildren(userData);
                        loading.dismiss();

                        startActivity(new Intent(updateUserInfo.this, dashboard.class));
                        Toast.makeText(updateUserInfo.this, "Profile was successfully updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        loading.dismiss();
                        Toast.makeText(updateUserInfo.this, "Connection Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void userInfoDisplay(final EditText username, final EditText password, final EditText email, final CircleImageView profilePic) {

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(Prevelant.currentOnlineUser.getUsername());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.child("Image").exists()){
                        String Image = snapshot.child("Image").getValue().toString();
                        String Username = snapshot.child("username").getValue().toString();
                        String Password = snapshot.child("password").getValue().toString();
                        String Email = snapshot.child("email").getValue().toString();


                        Picasso.get().load(Image).into(profilePic);
                        username.setText(Username);
                        email.setText(Email);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}