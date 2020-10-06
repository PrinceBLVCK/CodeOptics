package com.example.codeoptics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class register extends AppCompatActivity {

    EditText username,phone, password, email;
    Button register;
    TextView uploadPic;
    CircleImageView img_preview;

    private static final int GalleryPick = 1;
    private Uri imageUri;
    private String downloadUrl = "";
    private StorageReference mStorageRef;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mStorageRef = FirebaseStorage.getInstance().getReference().child("Profile_Picture"); //creating database storage object

        img_preview = (CircleImageView)findViewById(R.id.img_preview);
        uploadPic = (TextView)findViewById(R.id.uploadPic);

        username = (EditText)findViewById(R.id.username);
        phone = (EditText)findViewById(R.id.phone);
        password = (EditText)findViewById(R.id.password);
        email = (EditText)findViewById(R.id.email);

        img_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        register = (Button)findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
    }

    private void openGallery() {
        CropImage.activity(imageUri)
                .setAspectRatio(1,1)
                .start(register.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK  && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            img_preview.setImageURI(imageUri);
        }
        else{
            startActivity(new Intent(register.this, register.class));
            Toast.makeText(this, "Error occurred, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void createAccount() {

        final String Username = username.getText().toString();
        final String PhoneNum = phone.getText().toString();
        final String Password = password.getText().toString();
        final String Email = email.getText().toString();

        if (Username.isEmpty()){
            Toast.makeText(register.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (PhoneNum.isEmpty()){
            Toast.makeText(register.this, "Phone Number cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (Password.isEmpty()){
            Toast.makeText(register.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else if (Email.isEmpty()){
            Toast.makeText(register.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else{
            validate(Username, PhoneNum, Password, Email);
        }

    }

    private void validate(final String username, final String phone_num, final String password, final String email) {
        final ProgressDialog loading = new ProgressDialog(this);
        loading.setCancelable(true);//you can cancel it by pressing back button
        loading.setMessage("SETTING ACCOUNT...");
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.show();//displays the progress bar

        Toast.makeText(this, "About to create an account", Toast.LENGTH_SHORT).show();


        if (imageUri != null){
            Toast.makeText(this, "setting your account", Toast.LENGTH_SHORT).show();

            final StorageReference fileRef = mStorageRef.child(phone_num + ".jpg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful())
                            {
                                Uri result = task.getResult();
                                downloadUrl = result.toString();

                                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

                                HashMap<String,Object>userData = new HashMap<>();
                                userData.put("username", username);
                                userData.put("phone", phone_num);
                                userData.put("password", password);
                                userData.put("email", email);
                                userData.put("image", downloadUrl);
                                databaseRef.child(username).updateChildren(userData);

                                loading.dismiss();

                                startActivity(new Intent(register.this, MainActivity.class));
                                Toast.makeText(register.this, "Successfully Updated!", Toast.LENGTH_SHORT).show();
                                finish();
                            }else{
                                loading.dismiss();
                                Toast.makeText(register.this, "Check Your Internet Connectivity", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else{
            Toast.makeText(this, "Upload img first", Toast.LENGTH_SHORT).show();
            loading.dismiss();
        }
    }




}