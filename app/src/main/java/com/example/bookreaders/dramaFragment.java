package com.example.bookreaders;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookreaders.ModelClasses.BookDetails;
import com.example.bookreaders.viewHolders.DataAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link dramaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class dramaFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    RecyclerView recyclerView;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public dramaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment dramaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static dramaFragment newInstance(String param1, String param2) {
        dramaFragment fragment = new dramaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_drama, container, false);

        recyclerView = view.findViewById(R.id.dramaRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 3));
        onStart();
        return view;
    }


    public void onStart() {
        ProgressDialog progress = new ProgressDialog(getContext());
        progress.setTitle("Getting Books");
        progress.setMessage("Please wait..");
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        Log.d("tag","This is drama progress bar (Start)");

        super.onStart();

        FirebaseRecyclerOptions<BookDetails> options = new FirebaseRecyclerOptions.Builder<BookDetails>()
                .setQuery(FirebaseDatabase.getInstance().getReference("Books").orderByChild("bookGenre").startAt("drama").endAt("drama"+"\uf8ff"), BookDetails.class)
                .build();

        FirebaseRecyclerAdapter<BookDetails, DataAdapter> adapter = new FirebaseRecyclerAdapter<BookDetails, DataAdapter>(options) {
            @Override
            protected void onBindViewHolder(@NonNull DataAdapter dataViewHolder, int i, @NonNull final BookDetails book) {

                dataViewHolder.nameOfBook.setText(book.getBookName());
                String Author = "by "+book.getAuthorName();
                dataViewHolder.nameOfAuthor.setText(Author);
                Glide.with(getContext()).load(book.getLogoUrl()).placeholder(R.drawable.book_logo).into(dataViewHolder.imageOfBook);


                dataViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getContext(), showPDF.class);
                        intent.putExtra("urlOfBook", book.getPdfUrl());
                        Log.d("tag", "Going to ShowPDF Class and passing this url of book " + book.getPdfUrl());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public DataAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_book, parent, false);
                DataAdapter holder = new DataAdapter(view);
                return holder;
            }
        };


        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addListenerForSingleValueEvent(new  ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));
                Log.d("tag", "Total boos are " + i);
                if (i == 0) {
                    Toast.makeText(getContext(), "No Book Found", Toast.LENGTH_LONG).show();
                    progress.dismiss();
                } else {
                    recyclerView.setAdapter(adapter);
                    adapter.startListening();
                }
                Log.d("tag","This is drama progress bar (End)");
                progress.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}