package com.karina.alicesadventures.model;

/**
 * Created by karina on 2016-01-08.
 */
public class Product {
    private Integer id;
    private String name;
    private String description;
    private Partner partner;
    private Integer quantity_available;
    private Integer points_value;
    private Integer payment_status;
    private String thumb;
    private String created;
    private String modified;

    public Product() {
    }

    public Product(int id, String name, String description,
                   int quantity_available, int points_value, int payment_status,
                   String thumb, String created, String modified) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.quantity_available = quantity_available;
        this.points_value = points_value;
        this.payment_status = payment_status;
        this.thumb = thumb;
        this.created = created;
        this.modified = modified;
    }

    public Product(int position, String s, String s1) {
    }

    public Product(Integer id, String name, String description, Integer quantity_available, Integer points_value) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.quantity_available = quantity_available;
        this.points_value = points_value;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuantity_available() {
        return quantity_available;
    }

    public void setQuantity_available(Integer quantity_available) {
        this.quantity_available = quantity_available;
    }

    public Integer getPoints_value() {
        return points_value;
    }

    public void setPoints_value(Integer points_value) {
        this.points_value = points_value;
    }

    public Integer getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(Integer payment_status) {
        this.payment_status = payment_status;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
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
