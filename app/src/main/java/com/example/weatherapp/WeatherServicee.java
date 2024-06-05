package com.example.weatherapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherServicee {
    @GET("weather")
    Call<WeatherResponsee> getWeather(@Query("lat") double latitude, @Query("lon") double longitude, @Query("appid") String apiKey);

    @GET("weather")
    Call<WeatherResponsee> getWeatherByCityName(@Query("q") String cityName, @Query("appid") String apiKey);
}
