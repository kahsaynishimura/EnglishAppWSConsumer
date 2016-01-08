package com.karina.alicesadventures.model;

/**
 * Created by karina on 2016-01-08.
 */
public class Partner {
    private int id;
    private String companyName;
    private String phone;
    private String address;
    private User user;

    public Partner(){}
    public Partner(int id, String companyName, String phone, String address, User user) {
        this.id = id;
        this.companyName = companyName;
        this.phone = phone;
        this.address = address;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
