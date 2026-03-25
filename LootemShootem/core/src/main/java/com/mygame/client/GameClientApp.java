package com.mygame.client;

import com.badlogic.gdx.Game;
import com.mygame.client.presentation.navigation.Navigator;
import com.mygame.client.presentation.screens.MainMenuScreen;

public final class GameClientApp extends Game {

    private final String serverUrl;
    private final String defaultUsername;

    public GameClientApp(String serverUrl, String defaultUsername) {
        this.serverUrl = serverUrl;
        this.defaultUsername = defaultUsername;
    }

    @Override
    public void create() {
        Navigator navigator = new Navigator(this);
        setScreen(new MainMenuScreen(navigator, serverUrl, defaultUsername));
    }
}
