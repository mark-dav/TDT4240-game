package com.mygame.client.data.repository;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.mygame.client.domain.model.HudSlot;
import com.mygame.client.domain.model.HudWidget;
import com.mygame.client.domain.ports.PreferencesPort;

public final class PreferencesRepository implements PreferencesPort {

    private static final String PREFS_NAME   = "lootem";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_HUD      = "hud_slot_";

    /** Default widget for each slot ordinal: LEFT→KILL_FEED, CENTER→TIME_ALIVE, RIGHT→LEADERBOARD */
    private static final HudWidget[] HUD_DEFAULTS = {
            HudWidget.KILL_FEED,
            HudWidget.TIME_ALIVE,
            HudWidget.LEADERBOARD
    };

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

    @Override
    public HudWidget getHudWidget(HudSlot slot) {
        String stored = prefs.getString(KEY_HUD + slot.ordinal(),
                HUD_DEFAULTS[slot.ordinal()].name());
        try {
            return HudWidget.valueOf(stored);
        } catch (Exception e) {
            return HUD_DEFAULTS[slot.ordinal()];
        }
    }

    @Override
    public void saveHudWidget(HudSlot slot, HudWidget widget) {
        prefs.putString(KEY_HUD + slot.ordinal(), widget.name());
        prefs.flush();
    }
}
