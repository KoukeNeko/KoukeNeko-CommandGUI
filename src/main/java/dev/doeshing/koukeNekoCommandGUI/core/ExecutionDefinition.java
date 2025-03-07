package dev.doeshing.koukeNekoCommandGUI.core;

import java.util.Map;

/**
 * 用於封裝每一個執行動作的設定
 */
public class ExecutionDefinition {

    private String executor;       // PLAYER 或 CONSOLE
    private String executeCommand; // 要執行的指令
    private String successMessage; // 執行成功訊息

    public ExecutionDefinition(Map<?, ?> map) {
        this.executor = getString(map, "executor", "PLAYER");
        this.executeCommand = getString(map, "execute-command", "");
        this.successMessage = getString(map, "success-message", "");
    }

    private String getString(Map<?, ?> map, String key, String def) {
        Object val = map.get(key);
        return (val instanceof String) ? (String) val : def;
    }

    // ===== Getter =====

    public String getExecutor() {
        return executor;
    }

    public String getExecuteCommand() {
        return executeCommand;
    }

    public String getSuccessMessage() {
        return successMessage;
    }
}
