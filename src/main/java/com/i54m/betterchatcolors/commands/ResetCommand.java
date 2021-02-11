package com.i54m.betterchatcolors.commands;

import com.i54m.betterchatcolors.util.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ResetCommand implements SubCommand {

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "You must provide a players name!");
            player.sendMessage(ChatColor.RED + "Usage: /chatcolor reset <player>");
            return;
        }
        UUIDFetcher uuidFetcher = new UUIDFetcher();
        uuidFetcher.fetch(args[0]);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<UUID> future = executorService.submit(uuidFetcher);
        UUID uuid;
        try {
            uuid = future.get(5, TimeUnit.SECONDS);
        }catch (Exception e) {
            e.printStackTrace();
            PLUGIN.getLogger().severe("ERROR: Unable to fetch uuid for player: " + args[0] +" within 5 seconds!");
            player.sendMessage(ChatColor.RED + "We were unable to fetch that players uuid within the allocated time, please try again later!");
            return;
        }
        PLAYER_DATA_MANAGER.setPlayerData(uuid, "WHITE");
        player.sendMessage(ChatColor.GREEN + "Successfully reset " + args[0] + "'s chat color!");
    }
}