package dev.doeshing.koukeNekoCommandGUI.commands;

import dev.doeshing.koukeNekoCommandGUI.KoukeNekoCommandGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final KoukeNekoCommandGUI plugin;

    public ReloadCommand(KoukeNekoCommandGUI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 重新載入配置文件
        plugin.reloadConfig();

        // 重新插件讀取後的操作
        // plugin.reloadConfig();

        sender.sendMessage("§a設定文件已重新載入！");
        return true;
    }
}
