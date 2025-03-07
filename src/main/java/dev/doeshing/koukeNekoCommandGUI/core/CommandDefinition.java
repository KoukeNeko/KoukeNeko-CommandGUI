package dev.doeshing.koukeNekoCommandGUI.core;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用於封裝 config.yml 中 "commands" 節點下的指令資料
 */
public class CommandDefinition {

    private String command;             // 主指令
    private List<String> aliases;       // 指令別名
    private String description;         // 指令描述
    private String permission;          // 權限
    private boolean enabled;            // 是否啟用

    private String guiTitle;            // AnvilGUI 的標題
    private String guiDescription;      // 顯示給玩家的提示描述
    private String guiPlaceholder;      // AnvilGUI 預設文字

    private int minLength;              // 輸入最小長度
    private int maxLength;              // 輸入最大長度
    private String inputRegex;          // 輸入驗證用 Regex
    private String invalidFormatMessage;// 驗證失敗時訊息

    private List<ExecutionDefinition> executions;       // 執行動作列表
    private List<ExecutionDefinition> cancelExecutions; // 取消動作列表

    public CommandDefinition(ConfigurationSection section) {
        this.command = section.getString("command", "");
        this.aliases = section.getStringList("aliases");
        this.description = section.getString("description", "");
        this.permission = section.getString("permission", "");
        this.enabled = section.getBoolean("enabled", true);

        ConfigurationSection guiSec = section.getConfigurationSection("gui");
        if (guiSec != null) {
            this.guiTitle       = guiSec.getString("title", "&9CommandGUI");
            this.guiDescription = guiSec.getString("description", "");
            this.guiPlaceholder = guiSec.getString("placeholder", "");
        }

        ConfigurationSection valSec = section.getConfigurationSection("validation");
        if (valSec != null) {
            this.minLength            = valSec.getInt("min-length", 1);
            this.maxLength            = valSec.getInt("max-length", 100);
            this.inputRegex           = valSec.getString("input-regex", ".*");
            this.invalidFormatMessage = valSec.getString("invalid-format-message", "&c格式錯誤！");
        }

        this.executions = loadExecutions(section, "executions");
        this.cancelExecutions = loadExecutions(section, "cancel-executions");
    }

    private List<ExecutionDefinition> loadExecutions(ConfigurationSection parent, String path) {
        List<ExecutionDefinition> list = new ArrayList<>();

        // 直接拿 MapList，不要檢查 isConfigurationSection
        List<?> execList = parent.getMapList(path);
        if (execList == null || execList.isEmpty()) {
            return list; // 沒定義或是空清單
        }

        for (Object obj : execList) {
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                list.add(new ExecutionDefinition(map));
            }
        }
        return list;
    }


    // ===== Getter =====

    public String getCommand() { return command; }
    public List<String> getAliases() { return aliases; }
    public String getDescription() { return description; }
    public String getPermission() { return permission; }
    public boolean isEnabled() { return enabled; }

    public String getGuiTitle() { return guiTitle; }
    public String getGuiDescription() { return guiDescription; }
    public String getGuiPlaceholder() { return guiPlaceholder; }

    public int getMinLength() { return minLength; }
    public int getMaxLength() { return maxLength; }
    public String getInputRegex() { return inputRegex; }
    public String getInvalidFormatMessage() { return invalidFormatMessage; }

    public List<ExecutionDefinition> getExecutions() { return executions; }
    public List<ExecutionDefinition> getCancelExecutions() { return cancelExecutions; }
}
