package com.karina.alicesadventures.model;

/**
 * Created by Karina Nishimura on 15-09-30.
 */
public class Book {
    private Integer _id;
    private String name;


    public Book(){
    }

    public Book(Integer _id, String name){
        this._id = _id;
        this.name = name;
    }



    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
