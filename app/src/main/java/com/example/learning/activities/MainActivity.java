package com.example.learning.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.learning.R;
import com.example.learning.adapter.WeatherRVAdapter;
import com.example.learning.local.LocationHandler;
import com.example.learning.modal.WeatherRVModal;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private LinearLayout llHumidity;
    private ProgressBar loadingPB;
    private TextView cityNameTV,temperatureTV,conditionTV,windTV,humidityTV,rainTV,latitudeTV;
    private RecyclerView weatherRV;
    private EditText cityEdt;
    private ImageView backIV,iconIV,searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE=1;
    private String cityName;
    private LocationHandler locationHandler;
    private SwipeRefreshLayout swipeRefreshLayout;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        findId();

        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        locationHandler = new LocationHandler(this, new LocationHandler.LocationResultListener() {
            @Override
            public void onLocationResult(double latitude, double longitude) {
                getWeatherInfo(latitude,longitude);
            }
        });
        locationHandler.getLastLocation();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateContent();
            }
        });

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    String cityName = cityEdt.getText().toString().trim();
                    if (!cityName.isEmpty()) {
                        getLatLngFromCityName(cityName, MainActivity.this);
                    } else {
                        Toast.makeText(MainActivity.this, "Enter a city name", Toast.LENGTH_SHORT).show();
                    }
                }
        });

        llHumidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HumidityActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Please Provide Permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);

            for(Address adr : addresses){
                if(adr!= null){
                    String city = adr.getLocality();
                    if(city!=null && !city.equals("")){
                        cityName=city;
                    }else{
//                        Log.d("TAG","City Not Found");
                        Toast.makeText(this, "City Not Found", Toast.LENGTH_SHORT);
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(double lat,double lon){
        latitudeTV.setText("Lat: "+lat+" | Lon: "+lon);
        cityName=getCityName(lon,lat);
        cityNameTV.setText(cityName);
        String url = "http://api.weatherapi.com/v1/forecast.json?key=16a93b04712248b987d163622232812&q="+lat+","+lon+"&days=1&aqi=yes&alerts=yes";

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°c");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String windStatus = response.getJSONObject("current").getString("wind_kph");
                    String rain = response.getJSONObject("current").getString("cloud");
                    String humidity = response.getJSONObject("current").getString("humidity");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    rainTV.setText(rain+"%");
                    windTV.setText(windStatus+"Km/h");
                    humidityTV.setText(humidity+"%");
//                    if(isDay==1){
//                        Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.freepik.com%2Fpremium-vector%2Fday-sun-weather-app-screen-mobile-interface-design-forecast-weather-background-time-concept-vector-banner_34671984.htm&psig=AOvVaw2sf3n35EN0E02C-tdjbtI2&ust=1704006128539000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCLi5qLvLtoMDFQAAAAAdAAAAABAw").into(backIV);
//                    }else{
//                        Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.freepik.com%2Fpremium-vector%2Fnight-with-clouds-weather-app-screen-mobile-interface-design-forecast-weather-background-time-concept-vector-banner_35828856.htm&psig=AOvVaw2sf3n35EN0E02C-tdjbtI2&ust=1704006128539000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCLi5qLvLtoMDFQAAAAAdAAAAABAI").into(backIV);
//                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastO.getJSONArray("hour");
                    for(int i=0;i<hourArray.length();i++){
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModalArrayList.add(new WeatherRVModal(wind,time,temper,img));
                    }
                    weatherRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please Enter Valid City Name", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }


    private void getLatLngFromCityName(String cityName,Context context) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    double latitude = address.getLatitude();
                    double longitude = address.getLongitude();
                    // Use latitude and longitude values here as needed
//                    Toast.makeText(context, "Latitude: " + latitude + "\nLongitude: " + longitude, Toast.LENGTH_LONG).show();
                    getWeatherInfo(latitude,longitude);
                } else {
                    Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    private void updateContent(){
        locationHandler = new LocationHandler(this, new LocationHandler.LocationResultListener() {
            @Override
            public void onLocationResult(double latitude, double longitude) {
                getWeatherInfo(latitude,longitude);
            }
        });
        locationHandler.getLastLocation();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update is complete, stop the refreshing indicator
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }, 1000);// Simulated 2 seconds delay, replace with your update duration
    }
    private void findId() {
        homeRL = findViewById(R.id.idRLHome);
//        backIV=findViewById(R.id.idIVBack);
        loadingPB = findViewById(R.id.idPBLoading);
        latitudeTV=findViewById(R.id.idTVLatiLongi);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        rainTV=findViewById(R.id.idTVRain);
        humidityTV=findViewById(R.id.idTVHumidity);
        windTV=findViewById(R.id.idTVWind);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        swipeRefreshLayout = findViewById(R.id.idSRLHome);
        llHumidity = findViewById(R.id.idLLHumidity);
    }
}
