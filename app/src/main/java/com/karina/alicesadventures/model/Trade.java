package com.karina.alicesadventures.model;

/**
 * Created by karina on 2016-01-09.
 */
public class Trade {

    private Product product;
    private User user;
    private String qr_code;
    private Integer validated;
    private String created;
    private String modified;

    public Trade(Product product, User user, String qr_code, Integer validated, String created, String modified) {
        this.product = product;
        this.user = user;
        this.qr_code = qr_code;
        this.validated = validated;
        this.created = created;
        this.modified = modified;
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

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }
}
