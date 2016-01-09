package com.karina.alicesadventures.model;

/**
 * Created by Karina Nishimura on 15-09-30.
 */
public class User {
    private Integer _id;
    private String name;
    private String role;
    private String username;//email
    private Integer lastCompletedExercise;//TODO:change to exercise_id
    private Integer total_points;

    public User() {
    }

    //TODO add lastcompleted exercise
    //  public User(Integer _id, String name, String code,Integer lastCompletedLessonId){
    public User(Integer _id, String name, String role, String username,Integer lastCompletedExercise,Integer total_points) {
        this._id = _id;
        this.name = name;
        this.role = role;
        this.username = username;
        this.lastCompletedExercise = lastCompletedExercise;
        this.total_points=total_points;
    }

    //legacy method for local database
    public User(int id, String name, String userCode, int lastCompletedExercise) {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getLastCompletedExercise() {
        return lastCompletedExercise;
    }

    public void setLastCompletedExercise(Integer lastCompletedExercise) {
        this.lastCompletedExercise = lastCompletedExercise;
    }

    public Integer getTotal_points() {
        return total_points;
    }

    public void setTotal_points(Integer total_points) {
        this.total_points = total_points;
    }
}
