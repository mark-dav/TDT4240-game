package com.mygame.client.data.repository;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.mygame.client.domain.ports.PreferencesPort;

public final class PreferencesRepository implements PreferencesPort {

    private static final String PREFS_NAME   = "lootem";
    private static final String KEY_USERNAME = "username";

    // Preferences is backed by a file on desktop and SharedPreferences on Android
    private final Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);

    @Override
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    @Override
    public void saveUsername(String username) {
        prefs.putString(KEY_USERNAME, username);
        prefs.flush();
    }
}
