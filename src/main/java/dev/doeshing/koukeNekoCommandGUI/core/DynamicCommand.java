package dev.doeshing.koukeNekoCommandGUI.core;

import dev.doeshing.koukeNekoCommandGUI.KoukeNekoCommandGUI;
import dev.doeshing.koukeNekoCommandGUI.utils.PlaceholderUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 動態指令類別，根據 CommandDefinition 建立並執行 AnvilGUI 流程 (單階段版)
 *
 * 目標：若玩家已成功執行指令，就不再呼叫取消動作。
 */
public class DynamicCommand implements CommandExecutor {

    private final KoukeNekoCommandGUI plugin;
    private final CommandDefinition definition;

    // 共用訊息
    private final String prefix;
    private final String noPermissionMsg;
    private final String commandDisabledMsg;
    private final String playerOnlyMsg;
    private final String cancelMsg;

    // 用來標記「是否已經成功執行動作」
    private boolean executed = false;

    public DynamicCommand(KoukeNekoCommandGUI plugin, CommandDefinition definition) {
        this.plugin = plugin;
        this.definition = definition;

        // 從 config.yml 取出共用訊息
        this.prefix = plugin.getConfig().getString("settings.prefix", "&8[&bCommandGUI&8]&r");
        this.noPermissionMsg = plugin.getConfig().getString("messages.no-permission", "&c你沒有權限使用此指令！");
        this.commandDisabledMsg = plugin.getConfig().getString("messages.command-disabled", "&c此指令目前已被禁用！");
        this.playerOnlyMsg = plugin.getConfig().getString("messages.player-only", "&c此指令只能由玩家執行！");
        this.cancelMsg = plugin.getConfig().getString("messages.cancel-message", "&c操作已取消");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 若在 config.yml 被標記為 disabled
        if (!definition.isEnabled()) {
            sender.sendMessage(color(prefix + " " + commandDisabledMsg));
            return true;
        }

        // 權限檢查
        if (!definition.getPermission().isEmpty() && !sender.hasPermission(definition.getPermission())) {
            sender.sendMessage(color(prefix + " " + noPermissionMsg));
            return true;
        }

        // 僅允許玩家執行
        if (!(sender instanceof Player)) {
            sender.sendMessage(color(prefix + " " + playerOnlyMsg));
            return true;
        }

        Player player = (Player) sender;
        openInputGUI(player);

        return true;
    }

    /**
     * 開啟 AnvilGUI，讓玩家輸入參數 (單階段)
     */
    private void openInputGUI(Player player) {
        // 顯示 guiDescription 到聊天
        if (definition.getGuiDescription() != null && !definition.getGuiDescription().isEmpty()) {
            player.sendMessage(color(definition.getGuiDescription()));
        }

        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(color(definition.getGuiTitle()))
                .text(color(definition.getGuiPlaceholder()))
                .onClose(stateSnapshot -> {
                    // 只有當尚未執行任何動作時，才呼叫取消
                    if (!executed) {
                        runCancelExecutions(player);
                    }
                })
                .onClick((slot, snapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }

                    String inputText = snapshot.getText();
                    if (!validateInput(player, inputText)) {
                        return Collections.singletonList(
                                AnvilGUI.ResponseAction.replaceInputText(
                                        color(definition.getInvalidFormatMessage())
                                )
                        );
                    }

                    // 驗證通過 -> 執行動作
                    runExecutions(player, inputText);

                    // 設定已執行成功
                    executed = true;

                    // 關閉 GUI
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .open(player);
    }

    /**
     * 執行主動作清單
     */
    private void runExecutions(Player player, String inputText) {
        List<ExecutionDefinition> execList = definition.getExecutions();
        for (ExecutionDefinition exec : execList) {
            String cmdToRun = PlaceholderUtil.replacePlaceholders(exec.getExecuteCommand(), player, inputText);

            if ("PLAYER".equalsIgnoreCase(exec.getExecutor())) {
                player.performCommand(cmdToRun);
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdToRun);
            }

            String success = exec.getSuccessMessage();
            if (success != null && !success.isEmpty()) {
                success = PlaceholderUtil.replacePlaceholders(success, player, inputText);
                player.sendMessage(color(success));
            }
        }
    }

    /**
     * 執行「取消」動作清單
     */
    private void runCancelExecutions(Player player) {
        List<ExecutionDefinition> list = definition.getCancelExecutions();
        if (list != null && !list.isEmpty()) {
            for (ExecutionDefinition exec : list) {
                String cmdToRun = PlaceholderUtil.replacePlaceholders(exec.getExecuteCommand(), player, "");
                if ("PLAYER".equalsIgnoreCase(exec.getExecutor())) {
                    player.performCommand(cmdToRun);
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdToRun);
                }
            }
        } else {
            // 沒定義任何 cancel-executions，則顯示預設「取消」訊息
            player.sendMessage(color(cancelMsg));
        }
    }

    /**
     * 驗證輸入文字：長度 & Regex
     */
    private boolean validateInput(Player player, String input) {
        int len = input.length();
        if (len < definition.getMinLength() || len > definition.getMaxLength()) {
            return false;
        }
        Pattern pattern = Pattern.compile(definition.getInputRegex());
        return pattern.matcher(input).matches();
    }

    private String color(String msg) {
        return msg.replace("&", "§");
    }
}
