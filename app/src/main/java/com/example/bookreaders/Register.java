package com.example.bookreaders;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

//Remove the comments of test class
//It will collect all the info from user and will save it in database and will come back to login activity


public class Register extends AppCompatActivity {

    EditText user_name,roll_no,phone_num,password,repeat_password;
    CircleImageView imageView;
    Button sign_up;
    TextView log_in;
    ProgressDialog progress;
    boolean ImageInserted;
    Uri ImageData;
    StorageReference folder;
    private static final int IMAGE_PICK_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Initialization
        progress = new ProgressDialog(this);
        user_name= (EditText) findViewById(R.id.user_name);
        roll_no= (EditText)findViewById(R.id.roll_no);
        phone_num= (EditText)findViewById(R.id.user_phone);
        password= (EditText)findViewById(R.id.user_password);
        repeat_password= (EditText)findViewById(R.id.repeat_password);
        imageView=(CircleImageView)findViewById(R.id.get_user_image);
        sign_up=(Button)findViewById(R.id.sign_up_button);
        log_in = (TextView) findViewById(R.id.already_registered);
        ImageInserted =false;

        Toolbar toolbar = findViewById(R.id.registerToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Register");

        //for checking if users has entered everything or not

        user_name.addTextChangedListener(SignUpButton);
        roll_no.addTextChangedListener(SignUpButton);
        phone_num.addTextChangedListener(SignUpButton);
        password.addTextChangedListener(SignUpButton);
        repeat_password.addTextChangedListener(SignUpButton);

        //If user is already registered he will be redirected to login page

        log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        //For Image

        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ImageInserted = true;
                TakeImageFromGallery();
            }
        });

        sign_up.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String Name= user_name.getText().toString().trim();
                String Roll_no= roll_no.getText().toString().trim();
                String Phone= phone_num.getText().toString().trim();
                String Password= password.getText().toString().trim();
                String RepeatPassword= repeat_password.getText().toString().trim();

                if (InputCheck(Password,RepeatPassword,Roll_no))
                {
                    progress.setTitle("Adding Data");
                    progress.setMessage("Please Wait...");
                    progress.setCanceledOnTouchOutside(false);
                    progress.show();

                    SaveRecord(Name,Roll_no,Phone,Password);

                }

            }
        });
    }


    private boolean InputCheck(String UserPassword,String UserRepeatPassword,String EMAIL)
    {
         if(!(TextUtils.equals(UserPassword,UserRepeatPassword)))
         {
                password.setError("Password and repeat password must be same");
                repeat_password.setError("Password and repeat password must be same");
                return false;
         }
         else return true;
    }



    private final TextWatcher SignUpButton = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            String a= user_name.getText().toString().trim();
            String b= roll_no.getText().toString().trim();
            String c= phone_num.getText().toString().trim();
            String d= password.getText().toString().trim();
            String e= repeat_password.getText().toString().trim();

            if(a.isEmpty() || b.isEmpty()  || c.isEmpty() || d.isEmpty() || e.isEmpty())
            {
                sign_up.setEnabled(false);
            }
            else
            {
                sign_up.setEnabled(true);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void TakeImageFromGallery()
    {
        Intent i= new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i,IMAGE_PICK_CODE);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //

        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            assert data != null;
            ImageData = data.getData();
            Glide.with(this).load(ImageData).into(imageView);
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    void UpdatePictureInDataBase(String RollNum)
    {

        folder = FirebaseStorage.getInstance().getReference("Images");

        StorageReference ref = folder.child(RollNum);//"image" + ImageData.getLastPathSegment());


        ref.putFile(ImageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
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

    private void SaveRecord(String name, String rollnum, String phone, String password)
    {

        final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Data");

        HashMap<String,Object> map=new HashMap<>();
        map.put("Name",name);
        map.put("Email",rollnum);
        map.put("Phone",phone);
        map.put("Password",password);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                if(snapshot.hasChild(rollnum))
                {
                    roll_no.setError("This Email is already registered");
                    roll_no.setText(null);
                    roll_no.setHint("Enter a valid Email");
                    Toast.makeText(Register.this,"Email exists",Toast.LENGTH_LONG).show();
                    progress.dismiss();
                }
                else
                {
                    if(ImageInserted)
                    {
                        UpdatePictureInDataBase(rollnum);
                    }
                    else
                    {
                        map.put("ImageUrl","NoImage");
                    }

                    ref.child(rollnum).updateChildren(map).addOnCompleteListener(task -> {
                        if(task.isSuccessful())
                        {
                            progress.dismiss();
                            Toast.makeText(Register.this,"Successfully Registered",Toast.LENGTH_LONG).show();
                            Intent i = new Intent(Register.this,Login.class);
                            startActivity(i);
                            Log.d("tag", " successfully registered");

                            finish();
                        }
                        else
                        {
                            Toast.makeText(Register.this,"An Unknown Error occurred.",Toast.LENGTH_LONG).show();
                            progress.dismiss();
                            Log.d("tag", "Task is not successful");

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error){

                Log.d("tag", "Failed to read value."+ error.toException());

            }
        });

    }

}