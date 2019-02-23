package com.xpressy.rider.pojo;

import java.io.Serializable;

/**
 * Created by ${Ican} on 12/5/18.
 */
public class Tracking implements Serializable {
    String ride_id;
    String driver_id;
    String client_id;

    double client_latitude;
    double client_longitude;

    double driver_latitude;
    double driver_longitude;

    String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Tracking() {
    }

    public String getRide_id() {
        return ride_id;
    }

    public void setRide_id(String ride_id) {
        this.ride_id = ride_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public double getClient_latitude() {
        return client_latitude;
    }

    public void setClient_latitude(double client_latitude) {
        this.client_latitude = client_latitude;
    }

    public double getClient_longitude() {
        return client_longitude;
    }

    public void setClient_longitude(double client_longitude) {
        this.client_longitude = client_longitude;
    }

    public double getDriver_latitude() {
        return driver_latitude;
    }

    public void setDriver_latitude(double driver_latitude) {
        this.driver_latitude = driver_latitude;
    }

    public double getDriver_longitude() {
        return driver_longitude;
    }

    public void setDriver_longitude(double driver_longitude) {
        this.driver_longitude = driver_longitude;
    }
}
