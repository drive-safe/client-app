package com.example.drivesafe;

public class User {
    private int id;
    private String name, email, gender, mobile;

    public User(int id, String name, String email, String gender, String mobile) {
        this.id = id;
        this.email = email;
        this.mobile = mobile;
        this.gender = gender;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhoneno() {
        return mobile;
    }

    public void setPhoneno(String phoneno) {
        this.mobile = phoneno;
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


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}


