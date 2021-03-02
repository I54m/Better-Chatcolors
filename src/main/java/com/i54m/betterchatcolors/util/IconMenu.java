package com.i54m.betterchatcolors.util;

import com.i54m.betterchatcolors.BetterChatColors;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IconMenu implements Listener {

    private List<String> viewing = new ArrayList<>();
    private String name;
    @Getter
    private int size;
    private onClick click;
    private ItemStack[] items;
    @Getter
    private ItemStack fill;

    public IconMenu(String name, int size, onClick click) {
        this.name = name;
        this.size = size * 9;
        items = new ItemStack[this.size];
        this.click = click;
        Bukkit.getPluginManager().registerEvents(this, BetterChatColors.getInstance());
    }

    public IconMenu(IconMenu menu) {
        this.name = menu.name;
        this.size = menu.size;
        this.items = menu.items;
        this.click = menu.click;
        this.fill = menu.fill;
        Bukkit.getPluginManager().registerEvents(this, BetterChatColors.getInstance());
    }

    public void setSize(int size) {
        this.size = size * 9;
        this.items = Arrays.copyOf(items, this.size);
    }

    public void setName(String name) {
        this.name = name;
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        for (Player p : this.getViewers())
            close(p);
    }

    public void open(Player p) {
        p.openInventory(getInventory(p));
        viewing.add(p.getName());
    }

    private Inventory getInventory(Player p) {
        Inventory inv = Bukkit.createInventory(p, size, name);
        for (int i = 0; i < items.length; i++)
            if (items[i] != null)
                inv.setItem(i, items[i]);
        return inv;
    }

    public void close(Player p) {
        if (p.getOpenInventory().getTitle().equals(name))
            p.closeInventory();
    }

    private List<Player> getViewers() {
        List<Player> viewers = new ArrayList<>();
        for (String s : viewing)
            viewers.add(Bukkit.getPlayer(s));
        return viewers;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (viewing.contains(event.getWhoClicked().getName())) {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null) {
                if (event.getCurrentItem().isSimilar(fill)) return;
                if (click.click(p, this, event.getSlot(), event.getCurrentItem())) {
                    close(p);
                    this.items = null;
                    this.name = null;
                    this.click = null;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (viewing.contains(event.getPlayer().getName()))
            viewing.remove(event.getPlayer().getName());
    }

    public void addButton(int position, ItemStack item, String name, List<String> lore) {
        items[position] = getItem(item, name, lore);
    }

    public ItemStack getButton(int position) {
        return items[position];
    }

    public void addButton(int position, ItemStack item) {
        items[position] = item;
    }

    public void setButtonLore(int position, List<String> lore) {
        if (items[position].isSimilar(fill)) return;
        ItemMeta im = items[position].getItemMeta();
        im.setLore(lore);
        items[position].setItemMeta(im);
    }

    public void addFill(Material material) {
        this.fill = getItem(new ItemStack(material, 1), " ", " ");
        for (int i = 0; i < size; i ++) {
            if (items[i] == null)
                items[i] = fill;
        }
    }

    public void addFill(Material material, short data) {
        this.fill = getItem(new ItemStack(material, 1, data), " ", " ");
        for (int i = 0; i < size; i ++) {
            if (items[i] == null)
                items[i] = fill;
        }
    }

    private ItemStack getItem(ItemStack item, String name, String... lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }

    private ItemStack getItem(ItemStack item, String name, List<String> lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(lore);
        item.setItemMeta(im);
        return item;
    }

    public interface onClick {
        boolean click(Player clicker, IconMenu menu, int slot, ItemStack item);
    }

}
