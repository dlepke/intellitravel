package com.example.intellitravel;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CountryDetails extends AppCompatActivity implements OnMapReadyCallback {
    private final String url = "https://sub.yurtle.net/";
    String govURL = "https://travel.gc.ca/destinations/";
    TextView countryInfo;
    TextView riskLevel;
    TextView riskString;
    String country_advisories;
    String country_info;
    TextView diseasesTextview;
    TextView officialLanguagesTextview;
    TextView regionTextview;
    TextView capitalCityTextview;
    TextView currencyTextview;
    TextView callingCodeTextview;
    TextView govLink;
    TextView bestTimeToTravelText;
    String countryName;
    String countryPrettyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_country_details);

        Intent intent = getIntent();
        String countryName = intent.getStringExtra("country_name").toLowerCase(Locale.ROOT);
        String countryPrettyName = intent.getStringExtra("country_pretty_name");
        System.out.println("from country details");
        System.out.println(countryName);

        this.countryName = countryName;
        this.countryPrettyName = countryPrettyName;
        System.out.println(countryName);
        System.out.println(countryPrettyName);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        TextView countryNameView = findViewById(R.id.country_name);
        countryNameView.setText(countryPrettyName.toUpperCase(Locale.ROOT));
        navView.setSelectedItemId(R.id.nav_search_home);

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

        // Get a handle to the fragment and register the callback.
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.map_sub_view, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

        riskLevel = findViewById(R.id.risk_level);
        riskString = findViewById(R.id.risk_description);
        diseasesTextview = findViewById(R.id.disease_item);
        officialLanguagesTextview = findViewById(R.id.official_languages);
        regionTextview = findViewById(R.id.region);
        capitalCityTextview = findViewById(R.id.capital_city);
        currencyTextview = findViewById(R.id.currency);
        callingCodeTextview = findViewById(R.id.calling_code);
        String tempUrl = url + "countries/" + countryName;
        String countryInfoName = countryName.replace("-", " ");
        switch (countryInfoName) {
            case "cote-d-ivoire-ivory-coast": {
                countryInfoName = "ivory coast";
                break;
            }
            case "türkiye": {
                countryInfoName = "turkey";
                break;
            }
            case "": {
                countryInfoName = "";
                break;
            }
            default: break;
        }
        country_info = url + "countries/" + countryInfoName + "/info";
        System.out.println(country_info);

        govLink = findViewById(R.id.govLink);
        govURL += countryName.toLowerCase(Locale.ROOT);
        govLink.setText(govURL);

        govLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(govURL); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        bestTimeToTravelText = findViewById(R.id.bestTimeToTravel);
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(tempUrl);

        this.checkIfInFavourites(countryPrettyName);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> locations = geocoder.getFromLocationName(countryName, 1);
            LatLng country = new LatLng(locations.get(0).getLatitude(), locations.get(0).getLongitude());

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(country));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(2));

            googleMap.addMarker(new MarkerOptions()
                    .position(country)
                    .draggable(true)
                    .title(countryName));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }


    private class AsyncTaskRunner extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            RequestQueue queue = Volley.newRequestQueue(CountryDetails.this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, strings[0],
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String country_name = response.getString("prettyName");
                        String country_risk_level = response.getString("riskLevel");
                        String country_risk_string = response.getString("riskString");
                        country_advisories = response.getString("advisroyURI");

                        riskLevel.setText("Risk Level: " + country_risk_level);
                        riskString.setText(country_risk_string);

                        JsonObjectRequest request2 = new JsonObjectRequest(
                                Request.Method.GET, country_advisories, null,
                                new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    StringBuilder disease = new StringBuilder();
                                    JSONArray diseases = response.getJSONArray("diseases");
                                    String disease_index;
                                    for (int i = 0; i < diseases.length(); i++) {
                                        disease_index = diseases.getString(i);
                                        disease.append("- ").append(disease_index).append("\n");
                                    }

                                    if (disease.length() != 0) {
                                        diseasesTextview.setText(disease);
                                    }



                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, error -> Log.d("Error", error.toString()));

//                        error -> Toast.makeText(CountryDetails.this, error.toString(),
//                                Toast.LENGTH_SHORT).show());
                        queue.add(request2);


                        JsonObjectRequest request3 = new JsonObjectRequest(Request.Method.GET,
                                country_info, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String official_languages = "Official Language(s): ";
                                    JSONObject languages = response.getJSONObject("languages");
                                    JSONObject JSON_geo = response.getJSONObject("geo");
                                    JSONObject JSON_calling_code = response.getJSONObject("dialling");
                                    JSONArray calling_code_array = JSON_calling_code.getJSONArray("calling_code");
                                    String calling_code = calling_code_array.getString(0);

                                    String region = JSON_geo.getString("region");
                                    String capital_city = response.getString("capital");

                                    JSONObject JSON_currency_object = response.getJSONObject("currency");
                                    Iterator<String> currency_object = JSON_currency_object.keys();
                                    String currency_key = "";

                                    // iterates over official languages and adds them to the screen
                                    Iterator<String> keys = languages.keys();
                                    while (keys.hasNext()) {
                                        String key = keys.next();
                                        String value = (String) languages.get(key);
                                        official_languages = official_languages + value;
                                        if (keys.hasNext()) {
                                            official_languages = official_languages + ", ";
                                        }
                                    }

                                    // iterates over currency object
                                    while (currency_object.hasNext()) {
                                        currency_key = currency_object.next();
                                    }

                                    JSON_currency_object = JSON_currency_object.getJSONObject(currency_key);
                                    String currency = JSON_currency_object.getString("iso_4217_name");

                                    String bestTimeToTravel = "N/A";

                                    switch (region) {
                                        case "Asia":
                                        case "North America":
                                            bestTimeToTravel = "April to July";
                                            break;
                                        case "Europe":
                                            bestTimeToTravel = "June to September";
                                            break;
                                        case "Americas":
                                            bestTimeToTravel = "April to July and September to November";
                                            break;
                                        case "Africa":
                                            bestTimeToTravel = "September to December";
                                            break;
                                        case "Oceania":
                                            bestTimeToTravel = "September to November and March to May";
                                            break;
                                    }

                                    officialLanguagesTextview.setText(official_languages);
                                    regionTextview.setText(region);
                                    capitalCityTextview.setText("Capital City: " + capital_city);
                                    callingCodeTextview.setText("Calling Code: " + calling_code);
                                    currencyTextview.setText("Currency: " + currency);
                                    bestTimeToTravelText.setText("Best Time To Travel: " + bestTimeToTravel);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, error -> Log.d("Error", error.toString()));

                        queue.add(request3);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, error -> Log.d("Error", error.toString()));
            queue.add(request);

            return null;
        }
    }

    public void addToFavourites(View view) {
        String fileData = readFromLocalStorage();
        // add-write text into file

        if (!fileData.contains(countryPrettyName)) { // checks if country is already in favourites
            try {

                FileOutputStream fileout = openFileOutput("favouriteCountries.txt", MODE_PRIVATE);
                OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                outputWriter.write(fileData + countryPrettyName + "\n");
                outputWriter.close();

                //display file saved message
                String text = countryPrettyName.substring(0,1).toUpperCase() + countryPrettyName.substring(1).toLowerCase() + " has successfully been added to favourites";
                Toast.makeText(CountryDetails.this, text, Toast.LENGTH_SHORT).show();

                FloatingActionButton fab = findViewById(R.id.favourite_button);
                fab.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_check_24));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else {
            // if favorite button is clicked again the country will be removed from favourites
            removeFromFavourite(countryPrettyName);
            FloatingActionButton fab = findViewById(R.id.favourite_button);
            fab.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_favorite_24));
        }
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

    public void removeFromFavourite(String removedCountry) {
        String userFavourites = readFromLocalStorage();
        String updatedFavourites = userFavourites.replace(removedCountry + "\n" , "");
        updateFavourites(updatedFavourites, removedCountry);
    }

    public void updateFavourites(String userFav, String removedCountry) {

        try {
            FileOutputStream fileout = openFileOutput("favouriteCountries.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write(userFav);
            outputWriter.close();

            //display file saved message
            String text = removedCountry + " has successfully been removed from favourites";
            Toast.makeText(CountryDetails.this, text, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean checkIfInFavourites(String countryName) {
        String favourites = readFromLocalStorage();
        System.out.println("favourites: " + favourites);

        if (favourites.contains(countryName)) {
            System.out.println("country " + countryName + " is in favourites list");
            FloatingActionButton fab = findViewById(R.id.favourite_button);
            fab.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_check_24));
        } else {
            System.out.println("country " + countryName + " is NOT in favourites list");
        }

        return false;
    }

}