package com.i54m.betterchatcolors.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class ManualCommand implements SubCommand {

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "You must provide either a hex code (#xxxxxx) or a color code (&x).");
            return;
        }
        if (args[0].startsWith("#")) {
            if (args[0].length() != 7) {
                if (PLUGIN.preHex) {
                    player.sendMessage(ChatColor.RED + "Hex colors are currently not supported on this version!");
                    return;
                } else {
                    player.sendMessage(ChatColor.RED + "A hex color code must be 7 characters long starting with a #");
                    player.sendMessage(ChatColor.RED + "and must only contain numbers 0-9 and letters a-f!");
                    return;
                }
            }
            if (PLUGIN.preHex) {
                player.sendMessage(ChatColor.RED + "Hex colors are currently not supported on this version!");
                return;
            } else {
                try {
                    ChatColor.of(args[0]);
                } catch (IllegalArgumentException illegalArgumentException) {
                    player.sendMessage(ChatColor.RED + "Unable to parse chatcolor!");
                    player.sendMessage(ChatColor.RED + "A hex color code must be 7 characters long starting with a #");
                    player.sendMessage(ChatColor.RED + "and must only contain numbers 0-9 and letters a-f!");
                    return;
                }
                PLAYER_DATA_MANAGER.setPlayerData(player.getUniqueId(), args[0]);
                player.sendMessage(ChatColor.GREEN + "Successfully set your chat color to: " + ChatColor.of(args[0]) + args[0]);
            }
        } else if (args[0].startsWith("&")) {
            if ((args[0].contains("&k") ||
                    args[0].contains("&n") ||
                    args[0].contains("&l") ||
                    args[0].contains("&o") ||
                    args[0].contains("&m") ||
                    args[0].contains("&r")) && !player.hasPermission("betterchatcolors.admin")) {
                player.sendMessage(ChatColor.RED + "You may not using formatting characters in your color code!");
                return;
            }
            PLAYER_DATA_MANAGER.setPlayerData(player.getUniqueId(), args[0]);
            player.sendMessage(ChatColor.GREEN + "Successfully set your chat color to: " + ChatColor.translateAlternateColorCodes('&', args[0]) + args[0]);
        } else {
            if (PLUGIN.preHex)
                player.sendMessage(ChatColor.RED + "Hex colors are currently not supported on this version!");
            else
                player.sendMessage(ChatColor.RED + "Your chat color must start with either '#' for hex codes or '&' for color codes!");
        }
    }
}