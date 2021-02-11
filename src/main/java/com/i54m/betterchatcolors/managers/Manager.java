package com.i54m.betterchatcolors.managers;

import com.i54m.betterchatcolors.BetterChatColors;

public interface Manager {

    BetterChatColors PLUGIN = BetterChatColors.getInstance();

    void start();
    boolean isStarted();
    void stop();
}
