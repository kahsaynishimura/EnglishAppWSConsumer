package com.karina.alicesadventures.model;

/**
 * Created by karina on 2016-01-10.
 */
public class GeneralResponse {
    private String data;
    private String status;
    private String message;

    public GeneralResponse(String data, String status, String message) {
        this.data = data;
        this.status = status;
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
