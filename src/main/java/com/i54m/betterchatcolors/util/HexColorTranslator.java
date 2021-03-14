package com.i54m.betterchatcolors.util;

import com.i54m.betterchatcolors.BetterChatColors;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.md_5.bungee.api.ChatColor.COLOR_CHAR;

public class HexColorTranslator {

    @Getter
    private static final HexColorTranslator INSTANCE = new HexColorTranslator();

    private final TreeMap<Integer, String> OrderedHexValues = new TreeMap<>();
    private final HashMap<String, ChatColor> HexToChatColorTranslations = new HashMap<>();
    private final HashMap<DyeColor, ChatColor> dyeChatMap = new HashMap<>();

    private HexColorTranslator() {

        /* Ordered hex values tree map setup */

        OrderedHexValues.put(1, "000000");
        OrderedHexValues.put(2, "0000AA");
        OrderedHexValues.put(3, "00AA00");
        OrderedHexValues.put(4, "00AAAA");
        OrderedHexValues.put(5, "555555");
        OrderedHexValues.put(6, "5555FF");
        OrderedHexValues.put(7, "55FF55");
        OrderedHexValues.put(8, "55FFFF");
        OrderedHexValues.put(9, "AA0000");
        OrderedHexValues.put(10, "AA00AA");
        OrderedHexValues.put(11, "AAAAAA");
        OrderedHexValues.put(12, "FF5555");
        OrderedHexValues.put(13, "FF55FF");
        OrderedHexValues.put(14, "FFAA00");
        OrderedHexValues.put(15, "FFFF55");
        OrderedHexValues.put(16, "FFFFFF");

        /* Hex String to ChatColor map setup */

        HexToChatColorTranslations.put("000000", ChatColor.BLACK);
        HexToChatColorTranslations.put("0000AA", ChatColor.DARK_BLUE);
        HexToChatColorTranslations.put("00AA00", ChatColor.DARK_GREEN);
        HexToChatColorTranslations.put("00AAAA", ChatColor.DARK_AQUA);
        HexToChatColorTranslations.put("555555", ChatColor.DARK_GRAY);
        HexToChatColorTranslations.put("5555FF", ChatColor.BLUE);
        HexToChatColorTranslations.put("55FF55", ChatColor.GREEN);
        HexToChatColorTranslations.put("55FFFF", ChatColor.AQUA);
        HexToChatColorTranslations.put("AA0000", ChatColor.DARK_RED);
        HexToChatColorTranslations.put("AA00AA", ChatColor.DARK_PURPLE);
        HexToChatColorTranslations.put("AAAAAA", ChatColor.GRAY);
        HexToChatColorTranslations.put("FF5555", ChatColor.RED);
        HexToChatColorTranslations.put("FF55FF", ChatColor.LIGHT_PURPLE);
        HexToChatColorTranslations.put("FFAA00", ChatColor.GOLD);
        HexToChatColorTranslations.put("FFFF55", ChatColor.YELLOW);
        HexToChatColorTranslations.put("FFFFFF", ChatColor.WHITE);

        dyeChatMap.put(DyeColor.BLACK, ChatColor.DARK_GRAY);
        dyeChatMap.put(DyeColor.BLUE, ChatColor.DARK_BLUE);
        dyeChatMap.put(DyeColor.BROWN, ChatColor.GOLD);
        dyeChatMap.put(DyeColor.CYAN, ChatColor.AQUA);
        dyeChatMap.put(DyeColor.GRAY, ChatColor.GRAY);
        dyeChatMap.put(DyeColor.GREEN, ChatColor.DARK_GREEN);
        dyeChatMap.put(DyeColor.LIGHT_BLUE, ChatColor.BLUE);
        dyeChatMap.put(DyeColor.LIME, ChatColor.GREEN);
        dyeChatMap.put(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE);
        dyeChatMap.put(DyeColor.ORANGE, ChatColor.GOLD);
        dyeChatMap.put(DyeColor.PINK, ChatColor.LIGHT_PURPLE);
        dyeChatMap.put(DyeColor.PURPLE, ChatColor.DARK_PURPLE);
        dyeChatMap.put(DyeColor.RED, ChatColor.RED);
        if (BetterChatColors.getInstance().isLegacy())
            dyeChatMap.put(DyeColor.valueOf("SILVER"), ChatColor.GRAY);
        else
            dyeChatMap.put(DyeColor.valueOf("LIGHT_GRAY"), ChatColor.GRAY);
        dyeChatMap.put(DyeColor.WHITE, ChatColor.WHITE);
        dyeChatMap.put(DyeColor.YELLOW, ChatColor.YELLOW);
    }

    @Nullable
    public ChatColor translate(String hexString) {
        hexString = hexString.toUpperCase();
        if (hexString.startsWith("#"))
            hexString = hexString.replace("#", "");
        if (hexString.length() == 6  || hexString.length() == 3) {
            java.awt.Color jColor = java.awt.Color.decode("0x" + hexString);
            DyeColor dColor = DyeColor.getByColor(Color.fromRGB(jColor.getRed(), jColor.getGreen(), jColor.getBlue())); // null
            if (dyeChatMap.containsKey(dColor))
                return dyeChatMap.get(dColor);
            return null;
        } else throw new IllegalArgumentException("hexstring must be 6 characters long excluding the #");
    }


    public String translateHexColorCodes(String startTag, String endTag, String message)
    {
        final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }
}
