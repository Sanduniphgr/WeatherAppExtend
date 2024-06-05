package com.example.weatherapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponsee {
    @SerializedName("main")
    public Main main;

    @SerializedName("weather")
    public List<Weather> weather;

    public static class Main {
        @SerializedName("temp")
        public float temp;

        @SerializedName("humidity")
        public int humidity;
    }

    public static class Weather {
        @SerializedName("description")
        public String description;
    }
}
