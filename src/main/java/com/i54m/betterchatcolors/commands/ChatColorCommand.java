package com.i54m.betterchatcolors.commands;

import com.i54m.betterchatcolors.util.ChatColorGUI;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatColorCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command!");
            return false;
        }
        Player player = (Player) sender;
        if (args.length <= 0) {
            if (player.hasPermission("betterchatcolors.gui"))
                ChatColorGUI.open(player);
            else
                player.sendMessage(ChatColor.RED + "You do not have permission to change your chat color!");
            return true;
        } else {
            switch (args[0].toLowerCase()) {
                default:
                case "help": {
                    new HelpCommand().execute(player, (String[]) ArrayUtils.remove(args, 0));
                    return true;
                }
                case "hex":
                case "manual": {
                    if (!player.hasPermission("betterchatcolors.hex")){
                        player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    new ManualCommand().execute(player, (String[]) ArrayUtils.remove(args, 0));
                    return true;
                }
                case "reset": {
                    if (!player.hasPermission("betterchatcolors.admin")){
                        player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    new ResetCommand().execute(player, (String[]) ArrayUtils.remove(args, 0));
                    return true;
                }
                case "set": {
                    if (!player.hasPermission("betterchatcolors.admin")){
                        player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    new SetCommand().execute(player, (String[]) ArrayUtils.remove(args, 0));
                    return true;
                }
                case "reload": {
                    if (!player.hasPermission("betterchatcolors.admin")){
                        player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    new ReloadCommand().execute(player, (String[]) ArrayUtils.remove(args, 0));
                    return true;
                }
            }
        }
    }
}
