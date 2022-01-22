package com.example.bookreaders;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookreaders.ModelClasses.BookDetails;
import com.example.bookreaders.viewHolders.DataAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.barteksc.pdfviewer.PDFView;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

//This activity is called by  LoggedIn.
//It will get details of book from user and will upload it.
//After uploading book(successfully), it will come back to LoggedIn activity otherwise it will stay here

public class uploadBook extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner genre;
    EditText bookName,authorName,phone;
    String bookGenre;
    TextView selectedFile,selectedLogo;
    Button chooseFile,uploadBook,chooseLogo;
    private static final int PDF_CODE = 20;
    private static final int LOGO_CODE = 30;
    Uri bookPath,logoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_book);

        //Initialization
        genre=findViewById(R.id.spinnerGetGenre);
        authorName=findViewById(R.id.getBookAuthorName);
        bookName=findViewById(R.id.getBookName);
        chooseFile=findViewById(R.id.choosePdf);
        uploadBook=findViewById(R.id.UploadBook);
        phone=findViewById(R.id.getPhone);
        selectedFile=findViewById(R.id.fileName);
        chooseLogo=findViewById(R.id.chooseLogo);
        selectedLogo=findViewById(R.id.imageName);

        Toolbar toolbar = findViewById(R.id.upload_book_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Upload Book");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //For Enabling Upload Button
        authorName.addTextChangedListener(uploadButton);
        phone.addTextChangedListener(uploadButton);
        bookName.addTextChangedListener(uploadButton);


        //For genre spinner
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,R.array.genre, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genre.setAdapter(adapter2);
        genre.setOnItemSelectedListener(this);

        //choose file/Image button onClick Listeners
        chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //user will choose a file and lastPathSegment of file will be set in selected file textView
                ChooseFile();
            }
        });

        chooseLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //user will choose an Image and lastPathSegment of Image will be set in selected Image textView
                ChooseLogo();
            }
        });

        //upload button onClick Listeners
        uploadBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFile.getText().toString().equals("No file selected"))
                {
                    Toast.makeText(uploadBook.this, "Please select a file ", Toast.LENGTH_SHORT).show();
                    Log.d("tag","Please select a file (No pdf is selected )");
                    selectedFile.setError("Please Select Pdf file");
                }
                else if(selectedLogo.getText().toString().equals("No logo selected"))
                {
                    Toast.makeText(uploadBook.this, "Book Logo Missing", Toast.LENGTH_SHORT).show();
                    Log.d("tag","Book Logo Missing");
                    selectedLogo.setError("Please Select Book Logo");
                }

                else{
                    ProgressDialog progress = new ProgressDialog(uploadBook.this);
                    progress.setTitle("Uploading Book");
                    progress.setMessage("Please Wait");
                    progress.setCanceledOnTouchOutside(false);
                    progress.show();

                    final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Books");
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(bookName.getText().toString()))
                            {
                                //check if the book already exists in dataBase or not
                                progress.dismiss();
                                Toast.makeText(uploadBook.this, "This book already exists", Toast.LENGTH_LONG).show();
                                Log.d("tag","The Book already exists");
                            }
                            else
                            {
                                Log.d("tag","Uploading book by calling uploadBookInFireBase(bookPath) function ");
                                progress.dismiss();
                                uploadBookInFireBase(bookPath,logoPath);
                                Log.d("tag","Call to  uploadBookInFireBase(bookPath) finished");

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progress.dismiss();
                            Log.d("tag","error while checking book Name (Inside upload button on click listener)"+error);
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
       if(parent.getId()==R.id.spinnerGetGenre)
        {
            bookGenre =parent.getItemAtPosition(position).toString();
            Log.d("tag","You Selected "+ bookGenre);
        }

    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //for Enabling upload button
    TextWatcher uploadButton = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if(authorName.getText().toString().trim().isEmpty() || phone.getText().toString().trim().isEmpty() || bookName.getText().toString().trim().isEmpty())
            {
                uploadBook.setEnabled(false);

            }
            else
            {
                uploadBook.setEnabled(true);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    //for selecting pdf file from mobile
    private void ChooseFile()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PDF_CODE);
    }

    private void ChooseLogo()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, LOGO_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode == PDF_CODE)
        {
            Log.d("tag","PDF uri is "+data.getData());
            bookPath=data.getData();
            selectedFile.setText(bookPath.getLastPathSegment());
        }
        else if(resultCode==RESULT_OK && requestCode == LOGO_CODE)
        {
            Log.d("tag","Logo uri is  "+data.getData());
            logoPath=data.getData();
            selectedLogo.setText(logoPath.getLastPathSegment());
        }
    }

    //for uploading book in storage and saving link in realtime database
    private void uploadBookInFireBase(Uri bookUri,Uri logoUri)
    {
        //Progress Bar
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Uploading Book");
        progress.setMessage("Please Wait");
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        //Uploading File
        StorageReference folder= FirebaseStorage.getInstance().getReference("Books").child(bookName.getText().toString().trim());
        folder.putFile(bookUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("tag","Book Successfully Uploaded in Storage");

                //Getting download link from storage and saving(download link + other details ) in realtime
                folder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        StorageReference storageReference= FirebaseStorage.getInstance().getReference("Logos").child(bookName.getText().toString().trim());
                        storageReference.putFile(logoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storageReference .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri logoUri) {

                                        //Save Book details in realTime
                                        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books").child(bookName.getText().toString());
                                        Log.d("tag","Successfully got Download URL of PDF  i.e "+ uri);
                                        Log.d("tag","Successfully got Download URL of Logo  i.e "+ logoUri);
                                        HashMap<String,Object> hashMap = new HashMap<>();
                                        hashMap.put("pdfUrl",String.valueOf(uri));
                                        hashMap.put("logoUrl",String.valueOf(logoUri));
                                        hashMap.put("phone",phone.getText().toString());
                                        hashMap.put("authorName",authorName.getText().toString());
                                        hashMap.put("bookName",bookName.getText().toString());
                                        hashMap.put("bookGenre",bookGenre);

                                        //saving book details in realtime
                                        ref.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(uploadBook.this, "Book Successfully Uploaded", Toast.LENGTH_LONG).show();
                                                Log.d("tag","Successfully Saved Book Details in real time dataBase");
                                               // startActivity(new Intent (uploadBook.this,LoggedIn.class));
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("tag","failed to  Save Book Details in real time");
                                                e.printStackTrace();
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("tag","Book Saving in realtime database completed");
                                                progress.dismiss();
                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("tag","Failed to get Download URL of LOGO");
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        Log.d("tag","Downloading URL of Logo completed ");
                                        progress.dismiss();
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("tag","Failed to put Logo in storage");
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("tag","Failed to get Download URL of PDF");
                        e.printStackTrace();
                        progress.dismiss();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Log.d("tag","Downloading URL completed of PDF");

                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(uploadBook.this, "Failed to upload book", Toast.LENGTH_SHORT).show();
                Log.d("tag","Failed to upload book in storage");
                progress.dismiss();
                e.printStackTrace();
            }
        });

        Log.d("tag","UploadBookInFirebase functions last line executed");

    }


}