package com.example.bookreaders;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class Login extends AppCompatActivity {
    EditText user_email, user_password;
    TextView register;
    Button login_button;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        checkDataInSharedPreferences ();

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.d("TAG", "User is already logged in");
            Toast.makeText(this, "User is already logged in", Toast.LENGTH_SHORT).show();

        }

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        user_email = (EditText) findViewById(R.id.user_roll_number);
        user_password = (EditText) findViewById(R.id.user_password);
        register = (TextView) findViewById(R.id.register);
        login_button = (Button) findViewById(R.id.user_login_btn);

        user_email.addTextChangedListener(UserLogin);
        user_password.addTextChangedListener(UserLogin);

        Toolbar toolbar = findViewById(R.id.loginToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login");

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail, password;
                mail = user_email.getText().toString().trim();
                password = user_password.getText().toString().trim();
                Authenticate(mail, password);
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));

            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            // updateUI(account);
            String url = String.valueOf(account.getPhotoUrl());
            if (TextUtils.equals(url, null)) {
                url = "NoImage";
                Log.d("TAG", "No Image of user in mail");
            }

            String data = "account id is " + account.getId() + "picture url is " + account.getPhotoUrl();
            Log.d("TAG", "Signed in successfully,user info is " + data);
            Toast.makeText(this, "Signed in successfully," + account.getDisplayName(), Toast.LENGTH_SHORT).show();
            SaveRecord(account.getDisplayName(), account.getEmail(), null, null, url);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d("TAG", "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
            Toast.makeText(this, "signInResult:failed code=" + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private TextWatcher UserLogin = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String a = user_email.getText().toString().trim();
            String b = user_password.getText().toString().trim();

            login_button.setEnabled(!(a.isEmpty()) && !(b.isEmpty()));

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void Authenticate(String roll, String Password) {
        ProgressDialog p = new ProgressDialog(this);
        p.setTitle("Logging In");
        p.setMessage("Please wait");
        p.setCanceledOnTouchOutside(false);
        p.show();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Data");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(roll)) {
                    String OriginalPassword;
                    OriginalPassword = snapshot.child(roll).child("Password").getValue(String.class);


                    if (TextUtils.equals(OriginalPassword, Password)) {
                        Intent i = new Intent(Login.this, LoggedIn.class);
                        i.putExtra("EmailPassed", roll);
                        Log.d("tag", "going to Logged In Activity");
                        saveDataInSharedPreference(roll);
                        startActivity(i);
                    } else {
                        user_password.setError("Password Incorrect");
                        Toast.makeText(Login.this, "Access Denied", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    user_email.setError("Email Does not Exist");
                    user_email.setText(null);
                    Toast.makeText(Login.this, "Email Does not exists", Toast.LENGTH_SHORT).show();

                }
                p.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                p.dismiss();

            }
        });
    }

    private void saveDataInSharedPreference(String roll) {
        Log.d("TAG","Saving data in shared preferences");
        SharedPreferences sp = getSharedPreferences("users",MODE_PRIVATE);
        SharedPreferences.Editor ed=sp.edit();
        ed.putString("userName",roll);
        ed.commit();

        Log.d("TAG","Dta committed in shared preferences");

    }

    private void checkDataInSharedPreferences ()
    {
        SharedPreferences sp = getSharedPreferences("users",MODE_PRIVATE);
        if(sp.contains("userName"))
        {
           String user =sp.getString("userName","");
           Log.d("TAG","User found in shared preferences i.e. "+user);
           Intent i = new Intent(Login.this,LoggedIn.class);
           i.putExtra("EmailPassed",user);
           Log.d("tag", "going to Logged In Activity");
            startActivity(i);
        }
        else
        {
            Log.d("TAG","No User found, in shared preferences");

        }
    }


    private void SaveRecord(String name, String email, String phone, String password, String ImageUrl) {

        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Adding Data");
        progress.setMessage("Please Wait...");
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        HashMap<String, Object> map = new HashMap<>();
        map.put("Name", name);
        map.put("Email", email);
        map.put("Phone", phone);
        map.put("Password", password);
        map.put("ImageUrl", ImageUrl);

        final DatabaseReference ref2=FirebaseDatabase.getInstance().getReference("Data").child(name);
        ref2.updateChildren(map).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                Log.d("TAG", "add listener");

                progress.dismiss();
                Toast.makeText(Login.this, "Successfully Registered", Toast.LENGTH_LONG).show();
                Log.d("TAG", "Successfully Registered");
                Intent i = new Intent(Login.this, LoggedIn.class);
                saveDataInSharedPreference(name);
                i.putExtra("EmailPassed", name);
                Log.d("tag", "going to Logged In Activity");
                startActivity(i);

                finish();
            } else {
                Toast.makeText(Login.this, "An Unknown Error occurred.", Toast.LENGTH_LONG).show();
                progress.dismiss();
                Log.d("TAG", "Task is not successful");

            }


        });

    }
}