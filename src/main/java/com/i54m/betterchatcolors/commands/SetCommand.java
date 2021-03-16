package com.i54m.betterchatcolors.commands;

import com.i54m.betterchatcolors.util.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SetCommand implements SubCommand {

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "You must provide a players name then either a hex code (#xxxxxx) or a color code (&x).");
            player.sendMessage(ChatColor.RED + "Usage: /chatcolor set <player> <hex or color code>");
            return;
        }
        UUIDFetcher uuidFetcher = new UUIDFetcher();
        uuidFetcher.fetch(args[0]);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<UUID> future = executorService.submit(uuidFetcher);
        UUID uuid;
        try {
            uuid = future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            PLUGIN.getLogger().severe("ERROR: Unable to fetch uuid for player: " + args[0] + " within 5 seconds!");
            player.sendMessage(ChatColor.RED + "We were unable to fetch that players uuid within the allocated time, please try again later!");
            return;
        }
        if (args[1].startsWith("#")) {
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
                    ChatColor.of(args[1]);
                } catch (IllegalArgumentException illegalArgumentException) {
                    player.sendMessage(ChatColor.RED + "Unable to parse chatcolor!");
                    player.sendMessage(ChatColor.RED + "A hex color code must be 7 characters long starting with a #");
                    player.sendMessage(ChatColor.RED + "and must only contain numbers 0-9 and letters a-f!");
                    return;
                }
                PLAYER_DATA_MANAGER.setPlayerData(uuid, args[1]);
                player.sendMessage(ChatColor.GREEN + "Successfully set " + args[0] + "'s chat color to: " + ChatColor.of(args[1]) + args[1]);
            }
        } else if (args[1].startsWith("&")) {
            if ((args[1].contains("&k") ||
                    args[1].contains("&n") ||
                    args[1].contains("&l") ||
                    args[1].contains("&o") ||
                    args[1].contains("&m") ||
                    args[1].contains("&r"))) {
                player.sendMessage(ChatColor.RED + "You may not using formatting characters in your color code!");
                return;
            }
            try {
                ChatColor.getByChar(args[1].charAt(1));
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "That is not a valid chat color code!");
                return;
            }
            PLAYER_DATA_MANAGER.setPlayerData(uuid, ChatColor.getByChar(args[1].charAt(1)).getName());
            player.sendMessage(ChatColor.GREEN + "Successfully set " + args[0] + "'s chat color to: " + ChatColor.getByChar(args[1].charAt(1)) + ChatColor.getByChar(args[1].charAt(1)).getName());
        } else {
            if (PLUGIN.preHex)
                player.sendMessage(ChatColor.RED + "Hex colors are currently not supported on this version!");
            else
                player.sendMessage(ChatColor.RED + "Your chat color must start with either '#' for hex codes or & for color codes!");
        }
    }
}