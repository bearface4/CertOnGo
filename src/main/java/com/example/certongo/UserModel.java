package com.example.certongo;


public class UserModel {
    private String name;
    private String address;
    private String mobileNo;
    private String email;
    private String password;
    private String dateOfBirth;
    private int age; // New field for age

    // Default constructor (required for Firestore)
    public UserModel() {
    }

    // Parameterized constructor
    public UserModel(String name, String address, String mobileNo, String email, String password, String dateOfBirth, int age) {
        this.name = name;
        this.address = address;
        this.mobileNo = mobileNo;
        this.email = email;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.age = age; // Assign age
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
