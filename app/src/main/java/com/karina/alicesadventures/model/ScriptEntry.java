package com.karina.alicesadventures.model;

/**
 * Created by Karina Nishimura on 15-09-30.
 */
public class ScriptEntry implements Comparable {

    private Integer _id;
    private String textToShow;
    private String textToRead;
    private String textToCheck;
    private Integer scriptIndex;
    private Integer functionId;
    private Exercise exercise;//belongs to one exercise

    public ScriptEntry(Integer id, String textToShow, String textToRead, String textToCheck, Integer scriptIndex, Integer functionId, Exercise exercise) {
        _id = id;
        this.textToShow = textToShow;
        this.textToRead = textToRead;
        this.textToCheck = textToCheck;
        this.scriptIndex = scriptIndex;
        this.functionId = functionId;
        this.exercise = exercise;
    }

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public String getTextToShow() {
        return textToShow;
    }

    public void setTextToShow(String textToShow) {
        this.textToShow = textToShow;
    }

    public String getTextToRead() {
        return textToRead;
    }

    public void setTextToRead(String textToRead) {
        this.textToRead = textToRead;
    }

    public Integer getScriptIndex() {
        return scriptIndex;
    }

    public void setScriptIndex(Integer scriptIndex) {
        this.scriptIndex = scriptIndex;
    }

    public Integer getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Integer functionId) {
        this.functionId = functionId;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    @Override
    public int compareTo(Object another) {
        int compareRunningOrder = ((ScriptEntry) another).getScriptIndex();
        return this.scriptIndex - compareRunningOrder;
    }

    @Override
    public String toString() {
        return textToShow;
    }

    public String getTextToCheck() {
        return textToCheck;
    }

    public void setTextToCheck(String textToCheck) {
        this.textToCheck = textToCheck;
    }
}
