package com.example.bookreaders;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    CircleImageView profilePicture;
    TextView mail;
    EditText name, phone, password;
    String userName, userMail, userPhone, userPassword, email;
    FloatingActionButton floatingActionButton;
    Button updateButton;
    private static boolean PROFILE_PICTURE_CHANGED=false;
    private static final int IMAGE_CODE=20;
    public Uri ImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);

        //Receive email of user
        Bundle bundle = getIntent().getExtras();
        email = bundle.getString("EmailPassed");


        //Initializations
        name = findViewById(R.id.editName);
        mail = findViewById(R.id.editMail);
        password = findViewById(R.id.editPassword);
        phone = findViewById(R.id.editPhone);
        profilePicture = findViewById(R.id.editPicture);
        floatingActionButton = findViewById(R.id.floatingButton);
        updateButton = findViewById(R.id.profileUpdateButton);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = name.getText().toString().trim();
                userPhone = phone.getText().toString().trim();
                userPassword = password.getText().toString().trim();
                userMail = mail.getText().toString().trim();

                if (correctInfo(userName, userPassword, userPhone)) {
                    updateInfo();
                }

            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicture();
            }
        });

        //display user info
        displayUserInfo();
    }

    private void getPicture() {
        Intent gallery = new Intent();
        gallery.setAction(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(gallery,IMAGE_CODE);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==IMAGE_CODE)
        {
            PROFILE_PICTURE_CHANGED=true;
            assert data != null;
            ImageUri=data.getData();
            Glide.with(this).load(ImageUri).into(profilePicture);
            //profilePicture .setImageURI(ImageUri);
            Log.d("tag","Image URI obtained is "+ImageUri.toString());
        }
    }

    void UpdatePictureInDataBase(String RollNum)
    {

        StorageReference ref = FirebaseStorage.getInstance().getReference("Images").child(RollNum);


        ref.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            // Image Successfully Uploaded (Now Save the url of image in user data)
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                {
                    @Override
                    public void onSuccess(Uri uri)
                    {
                        //It will save image url direct in database

                        final FirebaseDatabase da = FirebaseDatabase.getInstance();
                        final DatabaseReference ref2=da.getReference("Data").child(RollNum);

                        HashMap<String,Object> h=new HashMap<>();
                        String url;
                        url= String.valueOf(uri);
                        h.put("ImageUrl",url);
                        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ref2.updateChildren(h);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }

        });

    }

    private void displayUserInfo() {

        //Progress Bar
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("GETTING DETAILS");
        progress.setMessage("Please Wait...");
        progress.setCanceledOnTouchOutside(false);
        progress.show();
        Toast.makeText(EditProfile.this, "Your Profile Picture will be loaded shortly(Depending on your internet speed)", Toast.LENGTH_LONG).show();


        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Data");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                password.setText(snapshot.child(email).child("Password").getValue(String.class));
                name.setText(snapshot.child(email).child("Name").getValue(String.class));
                mail.setText(snapshot.child(email).child("Email").getValue(String.class));
                phone.setText(snapshot.child(email).child("Phone").getValue(String.class));
                String image = snapshot.child(email).child("ImageUrl").getValue(String.class);
                if (TextUtils.equals(image, "NoImage")) {
                    Toast.makeText(EditProfile.this, "Your Profile Picture is missing.", Toast.LENGTH_SHORT).show();
                    Log.d("tag", "Your Profile Picture is missing.");
                } else {
                    Glide.with(getApplicationContext()).load(image).into(profilePicture);
                }
                progress.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progress.dismiss();

            }
        });
    }

    private boolean correctInfo(String userName, String userPassword, String userPhone) {
        if (userName.isEmpty()) {
            name.setError("Empty field");
            return false;
        }
        else if (userPassword.isEmpty()) {
            password.setError("Empty field");
            return false;
        } else if (userPhone.isEmpty()) {
            phone.setError("Empty field");
            return false;
        } else {
            return true;
        }
    }


    private void updateInfo() {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("UPDATING YOUR INFO");
        progress.setMessage("Please wait...");
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Data");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                HashMap<String, Object> hashMap= new HashMap<>();
                hashMap.put("Name",name.getText().toString().trim());
                hashMap.put("Phone",phone.getText().toString().trim());
                hashMap.put("Password",password.getText().toString().trim());

                if(PROFILE_PICTURE_CHANGED)
                {
                    Log.d("tag","Profile picture is changed. Uri is  "+ImageUri.toString());
                    UpdatePictureInDataBase(email);
                }else
                {
                    Log.d("tag","Profile picture is not changed.");
                }
                ref.child(email).updateChildren(hashMap).addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        progress.dismiss();
                        Toast.makeText(EditProfile.this,"Successfully Updated.Your Picture will be Updated on next Login",Toast.LENGTH_LONG).show();
                        Log.d("tag", " successfully Updated profile");
                        Intent i = new Intent(EditProfile.this,LoggedIn.class);
                        i.putExtra("EmailPassed",email);
                        startActivity(i);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(EditProfile.this,"An Unknown Error occurred.",Toast.LENGTH_LONG).show();
                        Log.d("tag", "Task is not successful");
                        progress.dismiss();
                    }
                });

                progress.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("tag", error.toString());
                progress.dismiss();
            }
        });

    }


}