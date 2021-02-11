package com.i54m.betterchatcolors.commands;

import com.i54m.betterchatcolors.util.ChatColorGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ReloadCommand implements SubCommand {

    @Override
    public void execute(Player player, String[] args) {
        PLUGIN.reloadConfig();
        ChatColorGUI.preLoadGUI();
        PLUGIN.loadBoldCooldowns();
        player.sendMessage(ChatColor.GREEN + "Config and menu reloaded!");
    }
}
