package dev.doeshing.koukeNekoCommandGUI;

import dev.doeshing.koukeNekoCommandGUI.commands.ReloadCommand;
import dev.doeshing.koukeNekoCommandGUI.core.CommandDefinition;
import dev.doeshing.koukeNekoCommandGUI.core.CommandSystem;
import dev.doeshing.koukeNekoCommandGUI.core.DynamicCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

/**
 * 主插件類
 * - 複製預設 config.yml
 * - 動態註冊指令
 * - 提供重載方法
 */
public final class KoukeNekoCommandGUI extends JavaPlugin implements Listener {

    private CommandSystem commandSystem;

    @Override
    public void onEnable() {
        // 若 config.yml 不存在，複製預設檔案
        saveDefaultConfig();

        // 初始化動態指令系統
        commandSystem = new CommandSystem(this);

        // 註冊 /koukenekocommandguireload 指令
        commandSystem.registerCommand(
                "koukenekocommandguireload",
                new ReloadCommand(this),
                "commandgui.admin",
                "重新載入設定並重註冊指令",
                "/koukenekocommandguireload",
                "kncguireload"
        );

        // 從 config.yml 讀取並註冊指令
        loadCommandsFromConfig();
    }

    /**
     * 讀取 config.yml 中的 "commands" 子節點，並動態註冊指令。
     */
    public void loadCommandsFromConfig() {
        FileConfiguration config = getConfig();
        ConfigurationSection commandsSec = config.getConfigurationSection("commands");
        if (commandsSec == null) {
            getLogger().warning("找不到 'commands' 子節點，請確認 config.yml！");
            return;
        }

        Set<String> cmdKeys = commandsSec.getKeys(false);
        if (cmdKeys.isEmpty()) {
            getLogger().info("commands 清單為空，沒有可註冊的指令。");
            return;
        }

        for (String cmdKey : cmdKeys) {
            ConfigurationSection section = commandsSec.getConfigurationSection(cmdKey);
            if (section == null) {
                getLogger().warning("無法解析指令節點: " + cmdKey);
                continue;
            }

            registerDynamicCommand(section);
        }
    }

    /**
     * 依據指令子節點 (ConfigurationSection) 建立並註冊 DynamicCommand
     */
    private void registerDynamicCommand(ConfigurationSection section) {
        CommandDefinition def = new CommandDefinition(section);
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

    /**
     * 供 ReloadCommand 呼叫，用於重載配置並重新註冊指令。
     */
    public void reloadCommandsFromConfig() {
        // 重新載入 config.yml
        reloadConfig();

        // 反註冊所有舊指令
        commandSystem.unregisterAll();

        // 重新讀取並註冊
        loadCommandsFromConfig();
    }

    @Override
    public void onDisable() {
        // 伺服器關閉或插件卸載時，取消註冊所有動態指令
        commandSystem.unregisterAll();
    }

    public CommandSystem getCommandSystem() {
        return commandSystem;
    }
}
