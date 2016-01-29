package com.karina.alicesadventures.model;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karina Nishimura on 15-09-30.
 */
public class Exercise {
    private Integer _id;
    private String name;
    private String transitionImage;
    private Lesson lesson; //belongs to one lesson
    private List<SpeechScript> scriptEntries;//has many script entries
    private ArrayList<Practice> practices;

    public Exercise(Integer id, String name, String transitionImage, Integer lessonId) {
        _id = id;
        this.name = name;
        this.transitionImage = transitionImage;
        this.lesson = new Lesson();
        lesson.set_id(lessonId);
        practices=new ArrayList<>();
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
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public List<SpeechScript> getScriptEntries() {
        return scriptEntries;
    }

    public void setScriptEntries(List<SpeechScript> scriptEntries) {
        this.scriptEntries = scriptEntries;
    }

    public String getTransitionImage() {
        return transitionImage;
    }

    public void setTransitionImage(String transitionImage) {
        this.transitionImage = transitionImage;
    }

    public ArrayList<Practice> getPractices() {
        return practices;
    }

    public void setPractices(
            ArrayList<Practice> practices) {
        this.practices = practices;
    }
}