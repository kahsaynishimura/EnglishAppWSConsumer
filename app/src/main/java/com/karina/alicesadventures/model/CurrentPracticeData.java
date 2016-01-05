package com.karina.alicesadventures.model;

/**
 * Created by karina on 15-10-03.
 */
public class CurrentPracticeData {
    public static final int REQ_CODE_SPEECH_INPUT = 100;//class to hold practice screen current information

    private Exercise currentExercise = null;
    private Integer currentScriptIndex = 0;
    private ScriptEntry currentScriptEntry = null;
    private Boolean shouldRunScript = true;

    public CurrentPracticeData() {
    }

    public void selectNextScript() {
        setCurrentScriptIndex(getCurrentScriptEntry().getScriptIndex() + 1);

        if (hasMoreScripts()) {
            setCurrentScriptEntry(getCurrentExercise().getScriptEntries().get(getCurrentScriptIndex()));
        }
    }
    public void setCurrentExercise(Exercise currentExercise) {
        this.currentExercise = currentExercise;
    }

    public Exercise getCurrentExercise() {
        return currentExercise;
    }

    public void setCurrentScriptEntry(ScriptEntry currentScriptEntry) {
        this.currentScriptEntry = currentScriptEntry;
    }

    public ScriptEntry getCurrentScriptEntry() {
        return currentScriptEntry;
    }
    public Boolean getShouldRunScript() {
        return shouldRunScript;
    }

    public void setShouldRunScript(Boolean shouldRunScript) {
        this.shouldRunScript = shouldRunScript;
    }

    public Integer getCurrentScriptIndex() {
        return currentScriptIndex;
    }

    public void setCurrentScriptIndex(Integer currentScriptIndex) {
        this.currentScriptIndex = currentScriptIndex;

    }

    public boolean hasMoreScripts() {
       return getCurrentExercise().getScriptEntries().size() > getCurrentScriptIndex();
    }
}
