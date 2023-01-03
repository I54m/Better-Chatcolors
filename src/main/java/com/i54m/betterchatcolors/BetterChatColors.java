package com.i54m.betterchatcolors;

import com.i54m.betterchatcolors.commands.BoldCommand;
import com.i54m.betterchatcolors.commands.ChatColorCommand;
import com.i54m.betterchatcolors.managers.PlayerDataManager;
import com.i54m.betterchatcolors.managers.WorkerManager;
import com.i54m.betterchatcolors.util.ChatColorGUI;
import com.i54m.betterchatcolors.util.HexColorTranslator;
import lombok.Getter;
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
    private HexColorTranslator HEX_COLOR_TRANSLATOR;
    @Getter
    public final TreeMap<Long, String> boldCooldowns = new TreeMap<>();
    @Getter
    public final ArrayList<UUID> boldPlayers = new ArrayList<>();
    @Getter
    public boolean preHex;
    @Getter
    public boolean legacy = false;

    @Override
    public void onEnable() {
        setInstance(this);
        if (getServer().getVersion().contains("1.8") ||
                getServer().getVersion().contains("1.9") ||
                getServer().getVersion().contains("1.10") ||
                getServer().getVersion().contains("1.11") ||
                getServer().getVersion().contains("1.12")) {
            getLogger().severe("Support for pre 1.13 minecraft versions is in beta at the moment!");
            getLogger().severe("Please consider updating to minecraft 1.13!");
            legacy = true;
            preHex = true;
            getLogger().warning("This version of minecraft does not support hex color codes, this feature will be disabled!");
        } else if (getServer().getVersion().contains("1.13") ||
                getServer().getVersion().contains("1.14") ||
                getServer().getVersion().contains("1.15")) {
            preHex = true;
            getLogger().warning("This version of minecraft does not support hex color codes, this feature will be disabled!");
        } else {
            preHex = false;
        }
        HEX_COLOR_TRANSLATOR = HexColorTranslator.getINSTANCE();
        if (!(new File(getDataFolder(), "config.yml").exists()))
            saveDefaultConfig();
//        if (getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI"))
//            if (preHex)
//                PlaceholderAPI.registerPlaceholder(this, "betterchatcolors_color", (event) -> {
//                    if (event.isOnline() && event.getPlayer() != null) {
//                        String color = PLAYERDATA_MANAGER.getPlayerData(event.getPlayer().getUniqueId(), true);
//                        if (color.startsWith("#")) {
//                            if (boldPlayers.contains(event.getPlayer().getUniqueId())) {
//                                boldPlayers.remove(event.getPlayer().getUniqueId());
//                                return HEX_COLOR_TRANSLATOR.translate(color) == null ? ChatColor.WHITE + "" + ChatColor.BOLD : HEX_COLOR_TRANSLATOR.translate(color) + "" + ChatColor.BOLD;
//                            } else
//                                return HEX_COLOR_TRANSLATOR.translate(color) == null ? ChatColor.WHITE + "" : HEX_COLOR_TRANSLATOR.translate(color) + "";
//                        } else {
//                            if (boldPlayers.contains(event.getPlayer().getUniqueId())) {
//                                boldPlayers.remove(event.getPlayer().getUniqueId());
//                                return ChatColor.valueOf(color) + "" + ChatColor.BOLD + "";
//                            } else
//                                return ChatColor.valueOf(color) + "";
//                        }
//                    }
//                    return ChatColor.WHITE + "";
//                });
//            else
//                PlaceholderAPI.registerPlaceholder(this, "betterchatcolors_color", (event) -> {
//                    if (event.isOnline() && event.getPlayer() != null) {
//                        String color = PLAYERDATA_MANAGER.getPlayerData(event.getPlayer().getUniqueId(), true);
//                        if (boldPlayers.contains(event.getPlayer().getUniqueId())) {
//                            boldPlayers.remove(event.getPlayer().getUniqueId());
//                            return ChatColor.of(color) + "" + ChatColor.BOLD + "";
//                        } else
//                            return ChatColor.of(color) + "";
//                    }
//                    return ChatColor.WHITE + "";
//                });
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            if (preHex)
                new PapiPlaceholderPreHex().register();
            else
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
