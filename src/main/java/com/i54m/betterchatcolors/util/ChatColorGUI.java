package com.i54m.betterchatcolors.util;

import com.i54m.betterchatcolors.BetterChatColors;
import com.i54m.betterchatcolors.managers.PlayerDataManager;
import com.i54m.betterchatcolors.managers.WorkerManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChatColorGUI {

    private static final BetterChatColors PLUGIN = BetterChatColors.getInstance();
    private static final WorkerManager WORKER_MANAGER = WorkerManager.getINSTANCE();
    private static final PlayerDataManager PLAYER_DATA_MANAGER = PlayerDataManager.getINSTANCE();

    private static IconMenu GUI;

    public static void preLoadGUI() {
        GUI = new IconMenu(ChatColor.LIGHT_PURPLE + "Chat Color Selection", 4, (clicker, menu, slot, item) -> {
            String color = PLUGIN.getConfig().getString("GUI." + slot + ".color", "WHITE");
            PLAYER_DATA_MANAGER.setPlayerData(clicker.getUniqueId(), color);
            clicker.playSound(clicker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            clicker.sendMessage(ChatColor.GREEN + "Successfully set your chat color to: " + item.getItemMeta().getDisplayName());
            return true;
        });
        FileConfiguration config = PLUGIN.getConfig();
        for (String key : config.getConfigurationSection("GUI").getKeys(false)) {
            try {
                int position = Integer.parseInt(key);
                List<String> lore = new ArrayList<>();
                config.getStringList("GUI." + key + ".lore").forEach((i) -> {
                    lore.add(ChatColor.translateAlternateColorCodes('&', i));
                });
                GUI.addButton(position,
                        new ItemStack(Material.valueOf(config.getString("GUI." + key + ".item", "STONE")), config.getInt("GUI." + key + ".amount", 1)),
                        ChatColor.translateAlternateColorCodes('&', config.getString("GUI." + key + ".name", "&4Error Couldn't get name from config!!")), lore);
            } catch (NumberFormatException nfe) {
                if (!key.equalsIgnoreCase("FILL")) {
                    PLUGIN.getLogger().severe("Error occurred during GUI pre load: " + key + " is not a valid number or 'FILL'!");
                    PLUGIN.getLogger().severe("Error Message: " + nfe.getMessage());
                    return;
                }
            }
        }
        GUI.addFill(Material.valueOf(config.getString("GUI.FILL.item", "STONE")));
    }

    public static void open(Player player) {
        IconMenu menu = new IconMenu(GUI);
        FileConfiguration config = PLUGIN.getConfig();
        String playerdata = PLAYER_DATA_MANAGER.getPlayerData(player, true);
        if (playerdata == null || playerdata.isEmpty()) playerdata = "WHITE";
        for (int i = 0; i < menu.getSize(); i++) {
            ItemStack button = menu.getButton(i);
            if (!(button.getType() == menu.getFill().getType())) {
                String color = PLUGIN.getConfig().getString("GUI." + i + ".color", "WHITE");
                List<String> lore = new ArrayList<>();
                config.getStringList("GUI." + i + ".lore").forEach((line) -> {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                });
                if (playerdata.equals(color))
                    lore.add(ChatColor.translateAlternateColorCodes('&', config.getString("GUI." + i + ".selected", "&eCurrently Selected!")));
                else if (player.hasPermission("betterchatcolors." + color.toLowerCase()))
                    lore.add(ChatColor.translateAlternateColorCodes('&', config.getString("GUI." + i + ".permToUse", "&aClick To Select")));
                else
                    lore.add(ChatColor.translateAlternateColorCodes('&', config.getString("GUI." + i + ".noPermToUse", "&4Locked!")));
                menu.setButtonLore(i, lore);
            }
        }
        menu.open(player);
    }


}
