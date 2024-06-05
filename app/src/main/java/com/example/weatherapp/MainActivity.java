package com.example.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private EditText etCityName;
    private TextView tvLocation, tvTime, tvWeather;
    private CardView cardViewWeather;
    private Button btnFetchWeather;
    private WeatherServicee weatherService;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "WeatherApp";
    private static final String API_KEY = "a34858d73e94528496d0d8f0cec5ea81"; // Replace with your actual API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCityName = findViewById(R.id.etCityName);
        tvLocation = findViewById(R.id.tvLocation);
        tvTime = findViewById(R.id.tvTime);
        tvWeather = findViewById(R.id.tvWeather);
        cardViewWeather = findViewById(R.id.cardViewWeather);
        btnFetchWeather = findViewById(R.id.btnFetchWeather);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        sharedPreferences = getSharedPreferences("WeatherApp", Context.MODE_PRIVATE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherService = retrofit.create(WeatherServicee.class);

        String lastCity = sharedPreferences.getString("last_city", null);
        if (lastCity != null) {
            etCityName.setText(lastCity);
            fetchWeatherByCityName(lastCity);
        }

        btnFetchWeather.setOnClickListener(v -> {
            String cityName = etCityName.getText().toString().trim();
            if (!cityName.isEmpty()) {
                fetchWeatherByCityName(cityName);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("last_city", cityName);
                editor.apply();
            } else {
                Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    tvLocation.setText(address.getAddressLine(0));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // Get current time
                            String currentTime = java.text.DateFormat.getTimeInstance().format(new java.util.Date());
                            tvTime.setText(currentTime);
                        } else {
                            Log.e(TAG, "Location task was not successful or location result is null");
                            Toast.makeText(MainActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void fetchWeatherByCityName(String cityName) {
        weatherService.getWeatherByCityName(cityName, API_KEY).enqueue(new Callback<WeatherResponsee>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponsee> call, @NonNull Response<WeatherResponsee> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponsee weatherResponse = response.body();
                    double tempCelsius = weatherResponse.main.temp - 273.15;
                    String weatherInfo = String.format(Locale.US, "Temp: %.2f°C\nHumidity: %d%%\nDescription: %s",
                            tempCelsius, weatherResponse.main.humidity, weatherResponse.weather.get(0).description);
                    tvWeather.setText(weatherInfo);
                    cardViewWeather.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "Response unsuccessful: " + response.message());
                    Toast.makeText(MainActivity.this, "Failed to get weather data. Response Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponsee> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Failed to get weather data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
