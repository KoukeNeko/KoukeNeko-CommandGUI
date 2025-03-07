# KoukeNeko Command GUI

## 🌟 Introduction

KoukeNeko Command GUI is a simple, flexible, and user-friendly Minecraft plugin that allows players to execute custom commands through an intuitive Anvil GUI interface. It supports custom placeholders, command validation, and dynamic execution through both player and console.

## 🔧 Features

- ✅ Intuitive GUI using Minecraft Anvil inventory
- ✅ Fully customizable commands and GUI messages
- ✅ Supports placeholders to dynamically execute commands
- ✅ Advanced input validation with custom regex support
- ✅ Permissions and aliases support for commands
- ✅ Easy reload command to update configurations

## 🔌 Dependencies

- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (optional but recommended)

## 📂 Installation

1. Place the plugin (`KoukeNekoCommandGUI.jar`) into your server's `plugins` folder.
2. (Optional) Install PlaceholderAPI for extended placeholder support.
3. Restart your server to activate the plugin.

## ⚙️ Configuration

Example configuration (`config.yml`):

```yaml
commands:
  teleport:
    aliases: ["tp", "tele"]
    description: "Open GUI to teleport to a player"
    permission: "commandgui.teleport"
    enabled: true
    gui:
      title: "&9Teleport GUI"
      description: "&7Enter player name to teleport"
      placeholder: "PlayerName"
    executions:
      - executor: "PLAYER"
        execute-command: "tp {input}"
        success-message: "&aTeleported to &b{input}!"
    validation:
      min-length: 3
      max-length: 16
      input-regex: "^[a-zA-Z0-9_]{3,16}$"
      invalid-format-message: "&cInvalid player name!"
```

Customize commands, placeholders, and validation rules as per your server needs.

## 🚀 Commands

- `/koukenekocommandguireload` (Permission: `commandgui.admin`)
  - Reloads the plugin's configuration.

## 🔑 Permissions

| Permission                 | Description                           |
|----------------------------|---------------------------------------|
| `commandgui.admin`         | Allows use of the reload command      |
| Custom-defined permissions | Defined per-command in `config.yml`   |

## ⚠️ Important Note

- Make sure you provide the correct input format as per your command's regex configuration to avoid input validation errors.

---

## 🌟 簡介

KoukeNeko Command GUI 是一款簡單、靈活且易於使用的 Minecraft 插件，透過直覺的鐵砧 GUI 介面讓玩家能夠快速執行客製化的指令。插件支援動態替換佔位符、指令驗證及透過玩家或控制台動態執行。

## 🔧 功能特色

- ✅ 使用 Minecraft 原生的鐵砧介面，直覺易用
- ✅ 完全可自訂指令與 GUI 提示訊息
- ✅ 支援佔位符動態執行指令
- ✅ 進階輸入驗證功能，自訂正則表達式支援
- ✅ 指令支援別名及權限控制
- ✅ 提供快速重新載入設定的指令

## 📌 相依插件

- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (可選但推薦)

## 📂 安裝方法

1. 將插件 (`KoukeNekoCommandGUI.jar`) 放入伺服器的 `plugins` 資料夾。
2. (選擇性) 安裝 PlaceholderAPI 以獲得更多佔位符支援。
3. 重啟伺服器以啟用插件。

## 🚀 指令

- `/koukenekocommandguireload` (權限: `commandgui.admin`)
  - 重新載入插件的設定檔。

## 🔑 權限

| 權限                        | 說明                    |
|-----------------------------|-------------------------|
| `commandgui.admin`          | 允許使用重新載入指令    |
| 自訂的權限                  | 透過 `config.yml` 個別設定 |

## ⚠️ 注意事項

- 請確保玩家輸入符合你設定的正則表達式，避免輸入驗證錯誤。

---

🚀 Enjoy your game! 祝你遊戲愉快！

