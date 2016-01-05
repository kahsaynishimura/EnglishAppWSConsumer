package com.karina.alicesadventures.model;

import java.util.List;

/**
 * Created by Karina Nishimura on 15-09-30.
 */
public class Lesson {
    private Integer _id;
    private String name;
    private Integer bookId;
    private List<Exercise> exerciseList;


    public Lesson(){
    }

    public Lesson(Integer _id, String name,Integer bookId ){
        this._id = _id;
        this.name = name;
        this.bookId = bookId;
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

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public List<Exercise> getExerciseList() {
        return exerciseList;
    }

    public void setExerciseList(List<Exercise> exerciseList) {
        this.exerciseList = exerciseList;
    }
}
