package dev.doeshing.koukeNekoCommandGUI.core;

import java.util.Map;

/**
 * 用於封裝每一個執行動作的設定
 */
public class ExecutionDefinition {

    private String executor;             // PLAYER 或 CONSOLE
    private String executeCommand;       // 要執行的指令
    private boolean requireConfirmation; // 是否需要二次確認
    private String confirmationMessage;  // 確認訊息
    private String successMessage;       // 執行成功訊息

    public ExecutionDefinition(Map<?, ?> map) {
        this.executor = getString(map, "executor", "PLAYER");
        this.executeCommand = getString(map, "execute-command", "");
        this.requireConfirmation = getBoolean(map, "require-confirmation", false);
        this.confirmationMessage = getString(map, "confirmation-message", "");
        this.successMessage = getString(map, "success-message", "");
    }

    private String getString(Map<?, ?> map, String key, String def) {
        Object val = map.get(key);
        return (val instanceof String) ? (String) val : def;
    }

    private boolean getBoolean(Map<?, ?> map, String key, boolean def) {
        Object val = map.get(key);
        return (val instanceof Boolean) ? (Boolean) val : def;
    }

    // ===== Getter =====

    public String getExecutor() { return executor; }
    public String getExecuteCommand() { return executeCommand; }
    public boolean isRequireConfirmation() { return requireConfirmation; }
    public String getConfirmationMessage() { return confirmationMessage; }
    public String getSuccessMessage() { return successMessage; }
}
