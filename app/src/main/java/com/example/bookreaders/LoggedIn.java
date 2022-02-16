package com.example.bookreaders;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


//This activity will receive email

public class LoggedIn extends AppCompatActivity {
    NavigationView nav;
    ActionBarDrawerToggle toggle;//three vertical lines
    DrawerLayout drawer;
    String email;

    TextView name;
    ImageView image;
    TabLayout tabLayout;
    TabItem poetry,fiction,drama;
    ViewPager viewPager;
    pageAdapter PageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dashboard");

        Bundle bundle = getIntent().getExtras();
        email = bundle.getString("EmailPassed");

        Log.d("tag", "Email received is " + email);

        tabLayout= findViewById(R.id.tabLayout);
        poetry=findViewById(R.id.poetryTab);
        fiction=findViewById(R.id.fictionTab);
        drama=findViewById(R.id.dramaTab);
        viewPager=findViewById(R.id.viewPager);
        PageAdapter =new pageAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(PageAdapter);

        nav = findViewById(R.id.navMenu);
        drawer = findViewById(R.id.drawer);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        View navHeader = nav.getHeaderView(0);
        name= navHeader.findViewById(R.id.username);
        image = navHeader.findViewById(R.id.UserImage);

        fillNavigationDrawerInfo();

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if(tab.getPosition()==0 || tab.getPosition()==1 || tab.getPosition()==2 )
                {
                    PageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.dashboard:

                        drawer.closeDrawer(GravityCompat.START);// drawer will close and will goto to it,s start position.(Jaha say open (start) hua tha waha chala jaye ga
                        Log.d("tag", "You clicked dashboard");
                        break;
                    case R.id.book:
                        drawer.closeDrawer(GravityCompat.START);
                        startActivity(new Intent(LoggedIn.this, uploadBook.class));
                        Log.d("tag", "You clicked book");
                        break;
                    case R.id.profile:
                        drawer.closeDrawer(GravityCompat.START);
                        Intent intent=new Intent(LoggedIn.this, EditProfile.class);
                        intent.putExtra("EmailPassed",email);
                        Log.d("tag", "You clicked Profile");
                        startActivity(intent);
                        break;
                    case R.id.log_out:
                        drawer.closeDrawer(GravityCompat.START);
                        Log.d("tag", "You clicked logout");
                        onBackPressed();
                        break;
                }

                return true;
            }
        });
    }


    private  void fillNavigationDrawerInfo()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Data");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String tempName=snapshot.child(email).child("Name").getValue(String.class);
                String imageUrl=snapshot.child(email).child("ImageUrl").getValue(String.class);

                assert tempName != null;
                name.setText(tempName.trim());
                Log.d("tag","Name of user is "+tempName);
                Log.d("tag","ImageUrl is  "+imageUrl);

                if(TextUtils.equals(imageUrl,"NoImage"))
                {
                   // Toast.makeText(LoggedIn.this, "Your Profile Picture is missing.", Toast.LENGTH_SHORT).show();
                    Log.d("tag","Your Profile Picture is missing.");
                }
                else
                {
                    Glide.with(LoggedIn.this).load(imageUrl).into(image);
                    Log.d("tag","Profile picture loaded");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}