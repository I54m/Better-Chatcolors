package com.i54m.betterchatcolors.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class HelpCommand implements SubCommand {

    @Override
    public void execute(Player player, String[] args) {
        if (player.hasPermission("betterchatcolors.hex")) {
            player.sendMessage(ChatColor.RED + "/chatcolor manual <hex code or color code>" + ChatColor.WHITE + "- manually set your chat color using hex or color codes");
            player.sendMessage(ChatColor.RED + "/chatcolor hex <hex code or color code>" + ChatColor.WHITE + "- manually set your chat color using hex or color codes");
        }
        if (player.hasPermission("betterchatcolors.admin")) {
            player.sendMessage(ChatColor.RED + "/chatcolor reload " + ChatColor.WHITE + "- Reload the plugin");
            player.sendMessage(ChatColor.RED + "/chatcolor reset <player> " + ChatColor.WHITE + "- Reset a player's chat color");
            player.sendMessage(ChatColor.RED + "/chatcolor set <player> <hex code or color code>" + ChatColor.WHITE + "- set a player's chat color");
        }
    }
}
