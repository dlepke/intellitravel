package com.example.intellitravel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;

public class UserFavourites extends AppCompatActivity {
    String[] favouriteCountries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_favourites);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        navView.setSelectedItemId(R.id.nav_my_list);

        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Class destination = null;

                switch (item.toString()) {
                    case "Search":
                        destination = SearchPage.class;
                        break;
                    case "Map":
                        destination = MapView.class;
                        break;
                    case "My List":
                        destination = UserFavourites.class;
                        break;
                }

                Intent intent = new Intent(getApplicationContext(), destination);
                intent.putExtra("clicked", item.toString());
                startActivity(intent);
                return false;
            }
        });

        Bundle bundle = new Bundle();

        String countriesFromLocalStorage = readFromLocalStorage();
        favouriteCountries  = countriesFromLocalStorage.split("\n");

        System.out.println(favouriteCountries[0]);

        if (Objects.equals(favouriteCountries[0], "")) {
            NoFavouritesFragment noFavouritesFragment = new NoFavouritesFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.search_items_list, noFavouritesFragment)
                    .commit();
        } else {
            bundle.putStringArray("countries", favouriteCountries);

            CountryListFragment countryListFragment = new CountryListFragment();
            countryListFragment.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.search_items_list, countryListFragment)
                    .commit();
        }
    }


    @Override
    protected void onResume() {
        String countriesFromLocalStorage = readFromLocalStorage();
        favouriteCountries = countriesFromLocalStorage.split("\n");
        Bundle bundle = new Bundle();
        if (!Objects.equals(favouriteCountries[0], "")) {
            System.out.println("No Countries!");
            bundle.putStringArray("countries", favouriteCountries);
            CountryListFragment countryListFragment = new CountryListFragment();
            countryListFragment.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.search_items_list, countryListFragment)
                    .commit();
        } else {
            NoFavouritesFragment noFavouritesFragment = new NoFavouritesFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.search_items_list, noFavouritesFragment)
                    .commit();
        }
        super.onResume();
    }

    // Read text from file
    public String readFromLocalStorage() {
        //reading text from file
        try {
            FileInputStream fileIn = openFileInput("favouriteCountries.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            int c;
            StringBuilder temp= new StringBuilder();
            while( (c = fileIn.read()) != -1){
                temp.append((char) c);
            }

            InputRead.close();
            return temp.toString();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

}