package com.karina.alicesadventures.model;

/**
 * Created by Karina Nishimura on 15-09-30.
 */
public class User {
    private Integer _id;
    private String name;
    private String code;
    private Integer lastCompletedLessonId;


    public User(){
    }

    public User(Integer _id, String name, String code,Integer lastCompletedLessonId){
        this._id = _id;
        this.name = name;
        this.code = code;
        this.lastCompletedLessonId=lastCompletedLessonId;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getLastCompletedLessonId() {
        return lastCompletedLessonId;
    }

    public void setLastCompletedLessonId(Integer lastCompletedLessonId) {
        this.lastCompletedLessonId = lastCompletedLessonId;
    }
}
