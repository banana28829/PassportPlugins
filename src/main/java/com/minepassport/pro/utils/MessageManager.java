package com.minepassport.pro.utils;

import com.minepassport.pro.MinePassportPro;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final MinePassportPro plugin;
    private final Map<String, String> messages;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public MessageManager(MinePassportPro plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            createDefaultMessages();
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        for (String key : messagesConfig.getKeys(false)) {
            messages.put(key, ChatColor.translateAlternateColorCodes('&', 
                messagesConfig.getString(key, key)));
        }
    }

    private void createDefaultMessages() {
        FileConfiguration config = new YamlConfiguration();
        
        // Загальні повідомлення
        config.set("prefix", "&8[&6MinePassport&8] ");
        config.set("no-permission", "&c✖ Недостатньо прав!");
        config.set("player-not-found", "&c✖ Гравця не знайдено!");
        config.set("passport-given", "&a✔ Паспорт видано гравцю &e{player}");
        config.set("passport-received", "&a✔ Вам видано паспорт громадянина!");
        config.set("passport-revoked", "&c✔ Паспорт гравця &e{player} &cанульовано!");
        config.set("your-passport-revoked", "&c⚠ Ваш паспорт було анульовано!");
        config.set("already-has-passport", "&c✖ У гравця вже є паспорт!");
        config.set("no-passport", "&c✖ У гравця немає паспорта!");
        config.set("cannot-drop", "&c✖ Ви не можете викинути паспорт!");
        config.set("cannot-transfer", "&c✖ Ви не можете перемістити паспорт!");
        config.set("config-reloaded", "&a✔ Конфігурацію перезавантажено!");
        config.set("duplicate-given", "&e✔ Дублікат паспорта видано гравцю &e{player}");
        config.set("status-changed", "&a✔ Статус змінено на &f{status}");
        
        // Статуси документа
        config.set("validity-valid", "&a✔ Дійсний");
        config.set("validity-invalid", "&c⚠ Анульований");
        config.set("validity-duplicate", "&e✔ Дублікат #{count}");
        
        try {
            config.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Не вдалося зберегти messages.yml");
        }
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, key);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public void reload() {
        messages.clear();
        loadMessages();
    }
}
