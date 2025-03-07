package dev.doeshing.koukeNekoCommandGUI.core;

import dev.doeshing.koukeNekoCommandGUI.KoukeNekoCommandGUI;
import dev.doeshing.koukeNekoCommandGUI.utils.PlaceholderUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 動態指令類別，根據 CommandDefinition 建立並執行 AnvilGUI 流程
 */
public class DynamicCommand implements CommandExecutor {

    private final KoukeNekoCommandGUI plugin;
    private final CommandDefinition definition;
    private final String prefix;
    private final String noPermissionMsg;
    private final String commandDisabledMsg;
    private final String playerOnlyMsg;
    private final String cancelMsg;

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
        // 如果該指令在 config.yml 被標記為 disabled
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

        // 開啟 AnvilGUI 讓玩家輸入
        openInputGUI(player);

        return true;
    }

    /**
     * 開啟第一階段的 AnvilGUI，讓玩家輸入參數
     */
    private void openInputGUI(Player player) {
        // 顯示 guiDescription 到聊天（可選）
        if (definition.getGuiDescription() != null && !definition.getGuiDescription().isEmpty()) {
            player.sendMessage(color(definition.getGuiDescription()));
        }

        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(color(definition.getGuiTitle())) // 1.14+ 顯示
                .text(color(definition.getGuiPlaceholder()))
                .onClose(stateSnapshot -> {
                    // 若玩家直接關閉介面，可視需求執行 "cancel-executions"
                    runCancelExecutions(player);
                })
                .onClick((slot, snapshot) -> {
                    Bukkit.getLogger().info("[DEBUG] openInputGUI clicked slot=" + slot);

                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }

                    String inputText = snapshot.getText();

                    // 驗證輸入
                    if (!validateInput(player, inputText)) {
                        // 驗證失敗，更新 AnvilGUI 的文字
                        return Collections.singletonList(
                                AnvilGUI.ResponseAction.replaceInputText(
                                        color(definition.getInvalidFormatMessage())
                                )
                        );
                    }

                    // 驗證通過 -> 執行動作
                    // 先檢查是否有任一執行動作需要「二次確認」
                    boolean needConfirm = definition.getExecutions().stream()
                            .anyMatch(ExecutionDefinition::isRequireConfirmation);

                    if (needConfirm) {
                        // 開啟「確認 AnvilGUI」
                        return Collections.singletonList(
                                AnvilGUI.ResponseAction.run(() -> openConfirmGUI(player, inputText))
                        );
                    } else {
                        // 直接執行
                        runExecutions(player, inputText, false);
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    }
                })
                .preventClose() // 玩家不能按 ESC 關閉，但可以按 X 關閉
                .open(player);
    }

    /**
     * 若有任一動作需要二次確認，則開啟第二個 AnvilGUI，顯示 confirmation-message
     *
     * @param player    目標玩家
     * @param inputText 上一階段的輸入
     */
    private void openConfirmGUI(Player player, String inputText) {
        // 找到第一個需要確認的執行動作，顯示它的 confirmation-message
        ExecutionDefinition confirmExec = definition.getExecutions().stream()
                .filter(ExecutionDefinition::isRequireConfirmation)
                .findFirst().orElse(null);

        String confirmText = (confirmExec != null) ? confirmExec.getConfirmationMessage() : "&e確定執行嗎？";

        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(color("&6確認執行？"))
                .text(color(confirmText))
                .onClose(stateSnapshot -> {
                    // 關閉即取消
                    runCancelExecutions(player);
                })
                .onClick((slot, snapshot) -> {
                    Bukkit.getLogger().info("[DEBUG] openConfirmGUI clicked slot=" + slot);

                    if (slot == AnvilGUI.Slot.OUTPUT) {
                        // 按下輸出 => 確定執行
                        runExecutions(player, inputText, true);
                    } else {
                        // 按下左/右槽 => 取消
                        runCancelExecutions(player);
                    }
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .open(player);
    }

    /**
     * 執行主動作清單
     *
     * @param player    執行指令的玩家
     * @param inputText 使用者在 AnvilGUI 輸入的文字
     * @param confirmed 是否已經過二次確認
     */
    private void runExecutions(Player player, String inputText, boolean confirmed) {
        for (ExecutionDefinition exec : definition.getExecutions()) {
            // 如果此執行動作需要確認但 confirmed=false，則跳過
            if (exec.isRequireConfirmation() && !confirmed) {
                continue;
            }
            // 執行指令
            String cmdToRun = PlaceholderUtil.replacePlaceholders(exec.getExecuteCommand(), player, inputText);

            // 依據 executor 決定用誰來執行
            if ("PLAYER".equalsIgnoreCase(exec.getExecutor())) {
                player.performCommand(cmdToRun);
            } else {
                // CONSOLE
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdToRun);
            }

            // 若有成功訊息，發送給玩家
            if (!exec.getSuccessMessage().isEmpty()) {
                String success = PlaceholderUtil.replacePlaceholders(exec.getSuccessMessage(), player, inputText);
                player.sendMessage(color(success));
            }
        }
    }

    /**
     * 執行「取消」動作清單
     */
    private void runCancelExecutions(Player player) {
        // 若定義了取消動作
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
     * 驗證輸入文字，符合長度 & Regex
     */
    private boolean validateInput(Player player, String input) {
        int len = input.length();
        if (len < definition.getMinLength() || len > definition.getMaxLength()) {
            return false;
        }
        // Regex 驗證
        Pattern pattern = Pattern.compile(definition.getInputRegex());
        if (!pattern.matcher(input).matches()) {
            return false;
        }
        return true;
    }

    private String color(String msg) {
        return msg.replace("&", "§");
    }
}
