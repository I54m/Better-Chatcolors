package com.i54m.betterchatcolors.commands;

import com.i54m.betterchatcolors.BetterChatColors;
import com.i54m.betterchatcolors.managers.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BoldCommand implements CommandExecutor {

    private final PlayerDataManager PLAYERDATA_MANAGER = PlayerDataManager.getINSTANCE();
    private final BetterChatColors PLUGIN = BetterChatColors.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to execute this command!");
            return false;
        }
        Player player = (Player) sender;
        if (player.hasPermission("betterchatcolors.bold")) {
            if (player.hasPermission("betterchatcolors.cooldown.bypass")) {
                doBoldCommand(player, args, false);
            } else {
                if (PLAYERDATA_MANAGER.getBoldData(player.getUniqueId()) > System.currentTimeMillis()) {
                    long millis = PLAYERDATA_MANAGER.getBoldData(player.getUniqueId()) - System.currentTimeMillis();
                    int yearsleft = (int) (millis / 3.154e+10);
                    int monthsleft = (int) (millis / 2.628e+9 % 12);
                    int weeksleft = (int) (millis / 6.048e+8 % 4.34524);
                    int daysleft = (int) (millis / 8.64e+7 % 7);
                    int hoursleft = (int) (millis / 3.6e+6 % 24);
                    int minutesleft = (int) (millis / 60000 % 60);
                    int secondsleft = (int) (millis / 1000 % 60);
                    String timeLeft;
                    if (secondsleft <= 0) {
                        doBoldCommand(player, args, true);
                    }
                    if (yearsleft != 0)
                        timeLeft = yearsleft + "Yr " + monthsleft + "M " + weeksleft + "w " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s";
                    else if (monthsleft != 0)
                        timeLeft = monthsleft + "M " + weeksleft + "w " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s";
                    else if (weeksleft != 0)
                        timeLeft = weeksleft + "w " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s";
                    else if (daysleft != 0)
                        timeLeft = daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s";
                    else if (hoursleft != 0)
                        timeLeft = hoursleft + "h " + minutesleft + "m " + secondsleft + "s";
                    else if (minutesleft != 0)
                        timeLeft = minutesleft + "m " + secondsleft + "s";
                    else
                        timeLeft = secondsleft + "s";
                    player.sendMessage(ChatColor.RED + "You may not do this yet! Please wait another " + timeLeft + "!");
                    return true;
                } else {
                    doBoldCommand(player, args, true);
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        return false;
    }

    private void doBoldCommand(Player player, String[] msg, boolean applyCooldown) {
        StringBuilder msgToSend = new StringBuilder();
        for (String arg: msg) {
            msgToSend.append(arg).append(" ");
        }
        PLUGIN.getBoldPlayers().add(player.getUniqueId());
        player.chat(msgToSend.toString());
        PLUGIN.getBoldPlayers().remove(player.getUniqueId());
        if (applyCooldown) {
            long cooldown = 0L;
            for (long cooldowns : PLUGIN.getBoldCooldowns().keySet()) {
                if (player.hasPermission(PLUGIN.getBoldCooldowns().get(cooldowns))){
                    cooldown = cooldowns;
                    break;
                }
            }
            PLAYERDATA_MANAGER.setBoldData(player.getUniqueId(), cooldown);
        }
    }
}
