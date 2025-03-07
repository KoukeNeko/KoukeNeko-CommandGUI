package dev.doeshing.koukeNekoCommandGUI.ui;

import dev.doeshing.koukeNekoCommandGUI.KoukeNekoCommandGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilGUI implements Listener {

    private final KoukeNekoCommandGUI plugin;
    private final Player player;
    private final String title;
    private final String placeholder;
    private Inventory anvilInventory;
    private AnvilGUIListener listener;

    public AnvilGUI(KoukeNekoCommandGUI plugin, Player player, String title, String placeholder) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.placeholder = placeholder;
    }

    /**
     * 設定玩家操作結果的回呼
     */
    public void setListener(AnvilGUIListener listener) {
        this.listener = listener;
    }

    /**
     * 開啟鐵砧 GUI 並註冊事件監聽
     */
    public void open() {
        // 建立一個鐵砧介面
        anvilInventory = Bukkit.createInventory(player, InventoryType.ANVIL, title);

        // 在左邊插槽放入一個紙張，並設定提示文字
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(placeholder);
        paper.setItemMeta(meta);
        anvilInventory.setItem(0, paper);

        // 開啟介面給玩家
        player.openInventory(anvilInventory);

        // 註冊事件監聽器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private boolean completed = false;

    /**
     * 玩家點擊 GUI 時觸發
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getInventory().equals(anvilInventory)) return;
        event.setCancelled(true);

        // 當玩家點擊輸出欄位（通常 slot 2）
        if (event.getRawSlot() == 2) {
            ItemStack clickedItem = event.getCurrentItem();
            String input = "";
            if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                input = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            }
            completed = true; // 標記為已完成，避免 close 事件再次觸發 onCancel
            if (listener != null) {
                listener.onComplete(input);
            }
            player.closeInventory();
        }
    }

    /**
     * 玩家關閉 GUI 時觸發，視為取消操作
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!event.getInventory().equals(anvilInventory)) return;

        if (!completed && listener != null) {
            listener.onCancel();
        }
        HandlerList.unregisterAll(this);
    }

    /**
     * 回呼介面，用於接收玩家的輸入結果或取消操作
     */
    public interface AnvilGUIListener {
        /**
         * 當玩家完成輸入時呼叫
         * @param input 玩家輸入的內容
         */
        void onComplete(String input);

        /**
         * 當玩家關閉 GUI 或取消操作時呼叫
         */
        void onCancel();
    }
}
