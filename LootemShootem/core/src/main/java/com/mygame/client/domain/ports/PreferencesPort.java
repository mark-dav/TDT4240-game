package com.mygame.client.domain.ports;

public interface PreferencesPort {
    String getUsername();
    void saveUsername(String username);
}
