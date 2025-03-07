package dev.doeshing.koukeNekoCommandGUI;

import dev.doeshing.koukeNekoCommandGUI.commands.ReloadCommand;
import dev.doeshing.koukeNekoCommandGUI.core.CommandDefinition;
import dev.doeshing.koukeNekoCommandGUI.core.CommandSystem;
import dev.doeshing.koukeNekoCommandGUI.core.DynamicCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

/**
 * 主插件類
 */
public final class KoukeNekoCommandGUI extends JavaPlugin implements Listener {
    private CommandSystem commandSystem;

    @Override
    public void onEnable() {
        getLogger().info("Plugin onEnable - Starting debug...");
        getLogger().info("Plugin name is: " + getName());

        saveDefaultConfig();

        commandSystem = new CommandSystem(this);

        // 測試：註冊 /koukenekocommandguireload
        getLogger().info("Registering reload command...");
        commandSystem.registerCommand(
                "koukenekocommandguireload",
                new ReloadCommand(this),
                "commandgui.admin",
                "重新載入設定並重註冊指令",
                "/koukenekocommandguireload",
                "kncguireload"
        );

        loadCommandsFromConfig();
    }

    public void loadCommandsFromConfig() {
        FileConfiguration config = getConfig();
        ConfigurationSection commandsSec = config.getConfigurationSection("commands");
        if (commandsSec == null) {
            getLogger().warning("找不到 commands 區段，請確認 config.yml！");
            return;
        }
        getLogger().info("Found commands section. Keys = " + commandsSec.getKeys(false));

        for (String cmdKey : commandsSec.getKeys(false)) {
            ConfigurationSection section = commandsSec.getConfigurationSection(cmdKey);
            if (section == null) {
                getLogger().warning("Commands." + cmdKey + " 不是一個子節點？跳過。");
                continue;
            }
            registerDynamicCommand(section, cmdKey);
        }
    }

    private void registerDynamicCommand(ConfigurationSection section, String cmdKey) {
        getLogger().info("Reading command node: " + cmdKey);

        CommandDefinition def = new CommandDefinition(section);
        getLogger().info("Parsed command = " + def.getCommand()
                + ", aliases=" + def.getAliases()
                + ", enabled=" + def.isEnabled()
                + ", permission=" + def.getPermission()
        );

        DynamicCommand dynamicCmd = new DynamicCommand(this, def);

        commandSystem.registerCommand(
                def.getCommand(),
                dynamicCmd,
                def.getPermission(),
                def.getDescription(),
                "/" + def.getCommand(),
                def.getAliases().toArray(new String[0])
        );
    }

    public void reloadCommandsFromConfig() {
        getLogger().info("Reloading config...");
        reloadConfig();
        commandSystem.unregisterAll();
        loadCommandsFromConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("onDisable - Unregister all commands.");
        commandSystem.unregisterAll();
    }
}

