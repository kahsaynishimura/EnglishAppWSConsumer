package com.karina.alicesadventures.model;

import java.util.ArrayList;

/**
 * Created by Karina Nishimura on 15-09-30.
 */
public class Exercise {
    private Integer _id;
    private String name;
    private String transitionImage;
    private Lesson Lesson; //belongs to one lesson
    private ArrayList<ScriptEntry> scriptEntries;//has many script entries

    public Exercise(Integer id, String name,String transitionImage, Lesson lesson) {
        _id = id;
        this.name = name;
        this.transitionImage = transitionImage;
        Lesson = lesson;
    }

    public Exercise() {

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

    public Lesson getLesson() {
        return Lesson;
    }

    public void setLesson(Lesson lesson) {
        Lesson = lesson;
    }

    public ArrayList<ScriptEntry> getScriptEntries() {
        return scriptEntries;
    }

    public void setScriptEntries(ArrayList<ScriptEntry> scriptEntries) {
        this.scriptEntries = scriptEntries;
    }

    public String getTransitionImage() {
        return transitionImage;
    }

    public void setTransitionImage(String transitionImage) {
        this.transitionImage = transitionImage;
    }
}
