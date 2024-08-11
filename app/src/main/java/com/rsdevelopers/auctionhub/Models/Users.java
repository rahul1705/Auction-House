package com.rsdevelopers.auctionhub.Models;

import java.util.Date;

public class Users {
    String name, mobile, email, pass, balance = "0", userImage;
    Date createdAt;

    public Users() {
    }

    public Users(String balance) {
        this.balance = balance;
    }

    public Users(String name, String mobile, String email, String pass, Date createdAt) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.pass = pass;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
