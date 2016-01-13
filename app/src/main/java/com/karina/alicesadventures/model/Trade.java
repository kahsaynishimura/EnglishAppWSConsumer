package com.karina.alicesadventures.model;

/**
 * Created by karina on 2016-01-09.
 */
public class Trade {
    private Integer id;
    private Product product;
    private User user;
    private String qr_code;
    private Integer validated;

    public Trade(Integer id,   String qr_code, Integer validated) {
        this.id = id;
        this.qr_code = qr_code;
        this.validated = validated;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getQr_code() {
        return qr_code;
    }

    public void setQr_code(String qr_code) {
        this.qr_code = qr_code;
    }

    public Integer getValidated() {
        return validated;
    }

    public void setValidated(Integer validated) {
        this.validated = validated;
    }
}