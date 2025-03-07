package dev.doeshing.koukeNekoCommandGUI.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 簡單的佔位符替換工具
 * 替換 %player% 與 {input}
 * 你也可以在這裡加上更多自定義的佔位符
 */
public class PlaceholderUtil {

    public static String replacePlaceholders(String original, Player player, String input) {
        if (original == null) return "";

        // 替換 {input} 和 %player% 佔位符 (這裡只是一個簡單的例子)
        original = original.replace("{input}", input).replace("%player%", player.getName());

        // 使用 PlaceholderAPI 來替換佔位符
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            original =  me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, original);
        }

        return original;
    }
}
