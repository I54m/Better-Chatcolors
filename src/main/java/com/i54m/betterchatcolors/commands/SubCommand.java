package com.i54m.betterchatcolors.commands;

import com.i54m.betterchatcolors.BetterChatColors;
import com.i54m.betterchatcolors.managers.PlayerDataManager;
import com.i54m.betterchatcolors.managers.WorkerManager;
import org.bukkit.entity.Player;

public interface SubCommand {

    BetterChatColors PLUGIN = BetterChatColors.getInstance();
    WorkerManager WORKER_MANAGER = WorkerManager.getINSTANCE();
    PlayerDataManager PLAYER_DATA_MANAGER = PlayerDataManager.getINSTANCE();

    void execute(Player player, String[] args);
}
