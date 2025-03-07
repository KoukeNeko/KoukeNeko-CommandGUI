package dev.doeshing.koukeNekoCommandGUI.core;

import dev.doeshing.koukeNekoCommandGUI.KoukeNekoCommandGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 透過反射動態註冊指令、並支援「反註冊」功能的系統。
 */
public class CommandSystem {

    private final KoukeNekoCommandGUI plugin;

    /**
     * 用來記錄「已動態註冊」的指令名稱
     * 註冊時，Bukkit 會在 CommandMap 裏儲存 "pluginName:commandName" 與 "commandName" 兩個索引。
     */
    private final Set<String> registeredCommandKeys = new HashSet<>();

    public CommandSystem(KoukeNekoCommandGUI plugin) {
        this.plugin = plugin;
    }

    /**
     * 通用指令註冊方法
     *
     * @param commandName        指令名稱 (不含 "/"，例如 "setlobby")
     * @param executor           指令執行器，實作 CommandExecutor
     * @param commandPermission  權限字串 (可為 null 或 "")
     * @param commandDescription 指令說明 (可為 null)
     * @param commandUsage       使用方式 (可為 null)
     * @param commandAliases     指令別名 (可為空陣列)
     */
    public void registerCommand(
            String commandName,
            CommandExecutor executor,
            String commandPermission,
            String commandDescription,
            String commandUsage,
            String... commandAliases
    ) {
        try {
            plugin.getLogger().info("registerCommand: " + commandName
                    + " perm=" + commandPermission
                    + " aliases=" + Arrays.toString(commandAliases)
            );

            Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer());

            Constructor<PluginCommand> constructor =
                    PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(commandName, plugin);

            pluginCommand.setExecutor(executor);
            if (commandPermission != null && !commandPermission.isEmpty()) {
                pluginCommand.setPermission(commandPermission);
            }
            if (commandDescription != null) {
                pluginCommand.setDescription(commandDescription);
            }
            if (commandUsage != null) {
                pluginCommand.setUsage(commandUsage);
            }
            if (commandAliases != null && commandAliases.length > 0) {
                pluginCommand.setAliases(Arrays.asList(commandAliases));
            }

            commandMap.register(plugin.getName(), pluginCommand);

            String lowerPluginName = plugin.getName().toLowerCase();
            registeredCommandKeys.add(lowerPluginName + ":" + commandName.toLowerCase());
            registeredCommandKeys.add(commandName.toLowerCase());

            plugin.getLogger().info("registerCommand done. recorded keys => " + registeredCommandKeys);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterAll() {
        plugin.getLogger().info("unregisterAll - removing from knownCommands...");
        try {
            Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer());

            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            for (String key : registeredCommandKeys) {
                Command removed = knownCommands.remove(key);
                if (removed != null) {
                    plugin.getLogger().info("  -> removed command key: " + key);
                }
            }

            registeredCommandKeys.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}