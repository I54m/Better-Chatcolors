package com.i54m.betterchatcolors.commands;

import com.i54m.betterchatcolors.managers.PlayerDataManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ViewCacheCommand implements SubCommand {

    private final PlayerDataManager PLAYER_DATA_MANAGER = PlayerDataManager.getINSTANCE();

    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage("Player data cache: ");
        for (UUID uuid : PLAYER_DATA_MANAGER.getPlayerDataCache().keySet()) {
            String playerdata = PLAYER_DATA_MANAGER.getPlayerDataCache().get(uuid);
            player.sendMessage(uuid + ": " + playerdata);
        }
    }
}