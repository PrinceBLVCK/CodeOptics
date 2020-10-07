package com.example.codeoptics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Model.Users;
import Prevelant.Prevelant;

public class MainActivity extends AppCompatActivity {

    Button signUp, loginPop, login, fb;
    LinearLayout loginView, btn;
    EditText Username, Password;
    ProgressDialog loading;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (Button)findViewById(R.id.login);
        fb = (Button)findViewById(R.id.fb);
        loginPop = (Button)findViewById(R.id.loginPop);
        loginView = (LinearLayout)findViewById(R.id.loginView);
        btn = (LinearLayout)findViewById(R.id.btnView);
        signUp = (Button)findViewById(R.id.signUp);

        Username = (EditText)findViewById(R.id.login_username);
        Password = (EditText)findViewById(R.id.login_password);

        loading = new ProgressDialog(this);

        loginPop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            btn.setVisibility(View.INVISIBLE);
            loginView.setVisibility(View.VISIBLE);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = Username.getText().toString();
                String password = Password.getText().toString();

                if (username.isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter Username", Toast.LENGTH_SHORT).show();
                }
                else if(password.isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter Password to login", Toast.LENGTH_SHORT).show();
                }
                else{
                    loading.setTitle("Logging In");
                    loading.setMessage("Loading...");
                    loading.setCanceledOnTouchOutside(false);
                    loading.show();
                    validate(username,password);
                }
            }

            private void validate(final String username, final String password) {
                final DatabaseReference RootRef;
                RootRef = FirebaseDatabase.getInstance().getReference();

                RootRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("Users").child(username).exists()){
                           Users userData = snapshot.child("Users").child(username).getValue(Users.class);

                           if(username.equals(userData.getUsername()) && password.equals(userData.getPassword())){
                               Prevelant.currentOnlineUser = userData;
                               loading.dismiss();
                               Intent intent = new Intent(MainActivity.this, dashboard.class );
                               startActivity(intent);
                               loading.dismiss();
                           }
                           else{
                               Toast.makeText(MainActivity.this, "Check if you entered the correct Username and Password", Toast.LENGTH_SHORT).show();
                               loading.dismiss();
                           }

                        }
                        else{
                            Toast.makeText(MainActivity.this, "Username do not exist", Toast.LENGTH_SHORT).show();
                            loading.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loading.dismiss();
                        Toast.makeText(MainActivity.this, ""+error, Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, register.class);
                startActivity(intent);
            }
        });
    }


}
