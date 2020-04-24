package com.raad.converter.model.beans;

import com.google.gson.Gson;

import java.io.Serializable;

public class Location implements Serializable {

    private String country;
    private String city;

    public Location() { }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
