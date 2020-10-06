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

    private Uri imageUri;
    private String myUrl = "";
    private StorageTask uploadTask;
    private StorageReference mStorage;
    private String checker = "";

    CircleImageView displayProfilePic;
    TextView chooseProfilePic;
    EditText username, phone, password, email;
    Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_info);

        mStorage = FirebaseStorage.getInstance().getReference().child("Profile_Picture");

        displayProfilePic = (CircleImageView)findViewById(R.id.img_preview);
        chooseProfilePic = (TextView)findViewById(R.id.uploadPic);

        username = (EditText)findViewById(R.id.username);
        phone = (EditText)findViewById(R.id.phone);
        password = (EditText)findViewById(R.id.password);
        email = (EditText)findViewById(R.id.email);

        updateButton = (Button)findViewById(R.id.update);

        UserInfoDisplay(username,phone,email,displayProfilePic);

        chooseProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checker = "clicked";
                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(updateUserInfo.this);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
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
        userData.put("username", username.getText().toString());
        userData.put("phone", phone.getText().toString());
        userData.put("password", password.getText().toString());
        userData.put("email", email.getText().toString());

        RootRef.child(Prevelant.currentOnlineUser.getUsername()).updateChildren(userData);

        startActivity(new Intent(updateUserInfo.this, dashboard.class));
        Toast.makeText(this, "Profile successfully updated", Toast.LENGTH_SHORT).show();
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            displayProfilePic.setImageURI(imageUri);
        }
        else {
            startActivity(new Intent(updateUserInfo.this, updateUserInfo.class));
            Toast.makeText(this, "Error has occurred, please try again", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void userInfoSaved() {
        if (TextUtils.isEmpty(username.getText().toString())){
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phone.getText().toString())){
            Toast.makeText(this, "Phone Number cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password.getText().toString())){
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(email.getText().toString())){
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
        }

        else if (checker.equals("clicked")){
            uploadImg();
        }
    }

    private void uploadImg() {
        final ProgressDialog loading = new ProgressDialog(this);
        loading.setTitle("Updating Profile");
        loading.setMessage("Please wait while updating your profile");
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        if (imageUri != null){

            final  StorageReference fileRef = mStorage.child(Prevelant.currentOnlineUser.getPhone() + ".jpg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();
                        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference().child("Users");

                        HashMap<String,Object>userData = new HashMap<>();
                        userData.put("username", username.getText().toString());
                        userData.put("phone", phone.getText().toString());
                        userData.put("password", password.getText().toString());
                        userData.put("email", email.getText().toString());
                        userData.put("image", myUrl);

                        RootRef.child(Prevelant.currentOnlineUser.getUsername()).updateChildren(userData);
                        loading.dismiss();
                        startActivity(new Intent(updateUserInfo.this, dashboard.class));
                        finish();
                    }
                    else {
                        loading.dismiss();
                        Toast.makeText(updateUserInfo.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Choose a profile picture", Toast.LENGTH_SHORT).show();
        }

    }


    private void UserInfoDisplay(final EditText username, final EditText phone, final EditText email, final CircleImageView displayProfilePic) {
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(Prevelant.currentOnlineUser.getUsername());

        RootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.child("image").exists()){
                        String Image = snapshot.child("image").getValue().toString();
                        String name = snapshot.child("username").getValue().toString();
                        String phoneNum = snapshot.child("phone").getValue().toString();
                        String passCode = snapshot.child("password").getValue().toString();
                        String emailAddress = snapshot.child("email").getValue().toString();

                        Picasso.get()
                                .load(Image)
                                .into(displayProfilePic);

                        username.setText(name);
                        phone.setText(phoneNum);
                        password.setText(passCode);
                        email.setText(emailAddress);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(updateUserInfo.this, ""+ error, Toast.LENGTH_SHORT).show();
            }
        });
    }

}