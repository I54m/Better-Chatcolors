package com.i54m.betterchatcolors;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.i54m.betterchatcolors.commands.BoldCommand;
import com.i54m.betterchatcolors.commands.ChatColorCommand;
import com.i54m.betterchatcolors.managers.PlayerDataManager;
import com.i54m.betterchatcolors.managers.WorkerManager;
import com.i54m.betterchatcolors.util.ChatColorGUI;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;

public final class BetterChatColors extends JavaPlugin {

    public static BetterChatColors instance;

    public static BetterChatColors getInstance() {
        return instance;
    }

    public void setInstance(BetterChatColors instance) {
        BetterChatColors.instance = instance;
    }

    private final WorkerManager WORKER_MANAGER = WorkerManager.getINSTANCE();
    private final PlayerDataManager PLAYERDATA_MANAGER = PlayerDataManager.getINSTANCE();
    @Getter
    public final TreeMap<Long, String> boldCooldowns = new TreeMap<>();
    @Getter
    public final ArrayList<UUID> boldPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        setInstance(this);
        if (!(new File(getDataFolder(), "config.yml").exists()))
            saveDefaultConfig();
        if (getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI"))
            PlaceholderAPI.registerPlaceholder(this, "betterchatcolors_color", (event) -> {
                if (event.isOnline() && event.getPlayer() != null) {
                    String color = PLAYERDATA_MANAGER.getPlayerData(event.getPlayer().getUniqueId(), true);
                    if (color.startsWith("&"))
                        if (boldPlayers.contains(event.getPlayer().getUniqueId())) {
                            boldPlayers.remove(event.getPlayer().getUniqueId());
                            return ChatColor.translateAlternateColorCodes('&', color) + ChatColor.BOLD + "";
                        } else
                            return ChatColor.translateAlternateColorCodes('&', color);
                    else {
                        if (boldPlayers.contains(event.getPlayer().getUniqueId())) {
                            boldPlayers.remove(event.getPlayer().getUniqueId());
                            return ChatColor.of(color) + "" + ChatColor.BOLD + "";
                        } else
                            return ChatColor.of(color) + "";
                    }
                }
                return ChatColor.WHITE + "";
            });
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            new PapiPlaceholder().register();
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") && !getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            getLogger().severe("Unable to find MVdWPlaceholderAPI or PlaceholderAPI!");
            getLogger().severe("This plugin largely relies on one of these two plugins to work and will not function correctly with out them!");
            getLogger().severe("Plugin disabled!");
            onDisable();
            this.setEnabled(false);
            return;
        }
        ChatColorGUI.preLoadGUI();
        loadBoldCooldowns();

        getServer().getPluginCommand("chatcolor").setExecutor(new ChatColorCommand());
        getServer().getPluginCommand("bold").setExecutor(new BoldCommand());

        WORKER_MANAGER.start();

        PLAYERDATA_MANAGER.start();
        PLAYERDATA_MANAGER.startCaching();
    }

    public void loadBoldCooldowns() {
        for (String key : getConfig().getConfigurationSection("BOLD-cooldowns").getKeys(false)) {
            boldCooldowns.put((getConfig().getLong("BOLD-cooldowns." + key) * 60000), "betterchatcolors.cooldown." + key);
        }
    }

    @Override
    public void onDisable() {
        PLAYERDATA_MANAGER.stop();
        WORKER_MANAGER.stop();
    }
}
