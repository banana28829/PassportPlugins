package com.minepassport.pro;

import com.minepassport.pro.commands.PassportCommand;
import com.minepassport.pro.commands.PassportTabCompleter;
import com.minepassport.pro.data.DatabaseManager;
import com.minepassport.pro.data.PassportManager;
import com.minepassport.pro.gui.GUIManager;
import com.minepassport.pro.listeners.PassportListener;
import com.minepassport.pro.listeners.GUIListener;
import com.minepassport.pro.utils.MessageManager;
import com.minepassport.pro.utils.MinePassportPlaceholderExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MinePassportPro extends JavaPlugin {

    private static MinePassportPro instance;
    private DatabaseManager databaseManager;
    private PassportManager passportManager;
    private GUIManager guiManager;
    private MessageManager messageManager;
    private Economy economy;
    private boolean vaultEnabled = false;
    private boolean placeholderAPIEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        
        // Зберігаємо конфіг
        saveDefaultConfig();
        
        // Ініціалізація менеджерів
        messageManager = new MessageManager(this);
        databaseManager = new DatabaseManager(this);
        passportManager = new PassportManager(this);
        guiManager = new GUIManager(this);
        
        // Підключення до Vault
        setupVault();
        
        // Підключення PlaceholderAPI
        setupPlaceholderAPI();
        
        // Реєстрація команд
        PassportCommand command = new PassportCommand(this);
        getCommand("passport").setExecutor(command);
        getCommand("passport").setTabCompleter(new PassportTabCompleter(this));
        
        // Реєстрація слухачів
        getServer().getPluginManager().registerEvents(new PassportListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        
        getLogger().info("§a═══════════════════════════════════════");
        getLogger().info("§a  MinePassportPro v" + getDescription().getVersion());
        getLogger().info("§a  Плагін успішно завантажено!");
        getLogger().info("§a  Vault: " + (vaultEnabled ? "§aПідключено" : "§cНе знайдено"));
        getLogger().info("§a  PlaceholderAPI: " + (placeholderAPIEnabled ? "§aПідключено" : "§cНе знайдено"));
        getLogger().info("§a═══════════════════════════════════════");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("§cMinePassportPro вимкнено!");
    }

    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault не знайдено! Баланс не буде відображатися.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("Economy провайдер не знайдено!");
            return;
        }
        economy = rsp.getProvider();
        vaultEnabled = true;
    }

    private void setupPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MinePassportPlaceholderExpansion(this).register();
            placeholderAPIEnabled = true;
        }
    }

    public void reload() {
        reloadConfig();
        messageManager.reload();
        getLogger().info("§aКонфігурацію перезавантажено!");
    }

    // Getters
    public static MinePassportPro getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PassportManager getPassportManager() {
        return passportManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
}
