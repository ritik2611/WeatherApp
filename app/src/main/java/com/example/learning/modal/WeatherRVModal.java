package com.example.learning.modal;

public class WeatherRVModal {

    private  String time;
    private String temperature;
    private String icon;
    private String windSpeed;

    public WeatherRVModal(String windSpeed,String time,String temperature,String icon) {
        this.windSpeed = windSpeed;
        this.time = time;
        this.icon = icon;
        this.temperature = temperature;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
}
