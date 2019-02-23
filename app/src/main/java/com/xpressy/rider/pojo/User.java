package com.xpressy.rider.pojo;

import java.io.Serializable;

/**
 * Created by ${Ican} on 12/5/18.
 */
public class User implements Serializable {
    String name;
    String email;
    String user_id;
    String avatar;
    String mobile;
    String key;

    String cost;
    String unit;

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", avatar='" + avatar + '\'' +
                ", mobile='" + mobile + '\'' +
                ", key='" + key + '\'' +
                ", cost='" + cost + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
