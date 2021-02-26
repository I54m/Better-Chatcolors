package com.i54m.betterchatcolors;

import com.i54m.betterchatcolors.managers.PlayerDataManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PapiPlaceholderPreHex extends PlaceholderExpansion {
    private BetterChatColors plugin;
    public PapiPlaceholderPreHex() {
        plugin = BetterChatColors.getInstance();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "betterchatcolors";
    }

    @Override
    public @NotNull String getAuthor() {
        return "I54m";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        if (identifier.equals("color")) {
            String color = PlayerDataManager.getINSTANCE().getPlayerData(player.getUniqueId(), true);
            if (color.startsWith("&"))
                if (plugin.getBoldPlayers().contains(player.getUniqueId())) {
                    plugin.getBoldPlayers().remove(player.getUniqueId());
                    return ChatColor.translateAlternateColorCodes('&', color) + ChatColor.BOLD + "";
                } else
                    return ChatColor.translateAlternateColorCodes('&', color);
        }

        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

}
