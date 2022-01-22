package com.example.bookreaders;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Login extends AppCompatActivity {
    EditText user_email, user_password;
    TextView register;
    Button login_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user_email = (EditText) findViewById(R.id.user_roll_number);
        user_password= (EditText) findViewById(R.id.user_password);
        register = (TextView)findViewById(R.id.register);
        login_button= (Button)findViewById(R.id.user_login_btn);

        user_email.addTextChangedListener(UserLogin);
        user_password.addTextChangedListener(UserLogin);

        Toolbar toolbar= findViewById(R.id.loginToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login");

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail,password;
                mail=user_email.getText().toString().trim();
                password=user_password.getText().toString().trim();
                Authenticate(mail,password);
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,Register.class));

            }
        });
    }

    private TextWatcher UserLogin = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            String a= user_email.getText().toString().trim();
            String b = user_password.getText().toString().trim();

            login_button.setEnabled(!(a.isEmpty()) && !(b.isEmpty()));

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    } ;

    private void Authenticate(String roll,String Password)
    {
        ProgressDialog p=new ProgressDialog(this);
        p.setTitle("Logging In");
        p.setMessage("Please wait");
        p.setCanceledOnTouchOutside(false);
        p.show();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Data");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(roll))
                {String OriginalPassword;
                    OriginalPassword = snapshot.child(roll).child("Password").getValue(String.class);


                    if(TextUtils.equals(OriginalPassword,Password))
                    {
                        Intent i = new Intent(Login.this,LoggedIn.class);
                        i.putExtra("EmailPassed",roll);
                        Log.d("tag","going to Logged In Activity");
                        startActivity(i);
                    }
                    else
                    {
                        user_password.setError("Password Incorrect");
                        Toast.makeText(Login.this, "Access Denied", Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {
                    user_email.setError("This Email Does not Exist");
                    user_email.setText(null);
                    Toast.makeText(Login.this,"Email Does not exists",Toast.LENGTH_SHORT).show();

                }
                p.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {          p.dismiss();

            }
        });
    }
}