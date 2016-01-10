package com.karina.alicesadventures.Util;

import java.util.HashMap;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.karina.alicesadventures.SelectUserActivity;
import com.karina.alicesadventures.model.User;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;
    // Context
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;
    // Sharedpref file name
    private static final String PREF_NAME = "SharedPreferences";

    // User id (make variable public to access from outside)
    public static final String KEY_ID = "id";

    private static final String IS_LOGIN = "is_logged_in";
    public static final String KEY_NAME = "name";
    public static final String KEY_ROLE = "role";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_LAST_COMPLETED_EXERCISE = "last_completed_exercise";

    // Constructor
    @SuppressLint("CommitPrefEdits")
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(User user) {

        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_ID, (user.get_id().toString()));
        editor.putString(KEY_ROLE, user.getRole());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_LAST_COMPLETED_EXERCISE, user.getLastCompletedExercise().toString());

        // commit changes
        editor.commit();
    }

    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_ID, pref.getString(KEY_ID, null));
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_ROLE, pref.getString(KEY_ROLE, null));
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
        user.put(KEY_LAST_COMPLETED_EXERCISE, pref.getString(KEY_LAST_COMPLETED_EXERCISE, null));
        return user;
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    /**
     * Clear session details
     * */
    public void logoutUser() {
		// Clearing all data from Shared Preferences
		editor.clear();
		editor.commit();

		Intent i = new Intent(_context, SelectUserActivity.class);
		// Closing all the Activities
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// Add new Flag to start new Activity
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		_context.startActivity(i);
	}
    /**
     * Check login method will check user login status If false it will redirect
     * user to login page Else won't do anything
     * */
	public void checkLogin() {
		// Check login status
		if (!this.isLoggedIn()) {
			// user is not logged in redirect him to Login Activity
			Intent i = new Intent(_context, SelectUserActivity.class);
			// Closing all the Activities
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			// Add new Flag to start new Activity
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// Staring Login Activity
			_context.startActivity(i);
		}

	}

    /**
     * Quick check for login
     * **/
    // Get Login State
}
