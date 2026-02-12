package com.minepassport.pro.gui;

import com.minepassport.pro.MinePassportPro;
import com.minepassport.pro.data.Passport;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIManager implements InventoryHolder {

    private final MinePassportPro plugin;
    public static final String GUI_TITLE = "§8§l« §6§lПАСПОРТ §8§l»";

    public GUIManager(MinePassportPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public void openPassportGUI(Player viewer, Passport passport) {
        Inventory gui = Bukkit.createInventory(this, 54, GUI_TITLE);

        // Заповнюємо фон
        fillBackground(gui);

        // Центр — голова гравця (слот 22)
        gui.setItem(22, createPlayerHead(passport));

        // Ліва частина — основна інформація
        gui.setItem(10, createInfoItem(Material.NAME_TAG, "§e§lНікнейм", 
            "§7" + passport.getPlayerName()));
        
        gui.setItem(11, createInfoItem(Material.ENDER_EYE, "§d§lUUID", 
            "§7" + passport.getUuid().toString().substring(0, 8) + "..."));
        
        gui.setItem(19, createInfoItem(Material.CLOCK, "§a§lДата створення", 
            "§7" + passport.getCreatedDate()));
        
        gui.setItem(20, createInfoItem(Material.CAKE, "§c§lДата народження", 
            passport.getBirthDate().isEmpty() ? "§8Не вказано" : "§7" + passport.getBirthDate()));
        
        gui.setItem(28, createInfoItem(Material.COMPASS, "§b§lМісце проживання", 
            passport.getCity().isEmpty() ? "§8Не вказано" : "§7" + passport.getCity()));

        // Права частина — статуси
        gui.setItem(14, createInfoItem(Material.SHIELD, "§6§lСтатус", 
            passport.getStatusColor() + passport.getStatus()));
        
        gui.setItem(15, createInfoItem(Material.CHEST, "§e§lОрганізація", 
            passport.getOrganization().isEmpty() ? "§8Немає" : "§7" + passport.getOrganization()));
        
        gui.setItem(23, createInfoItem(Material.EXPERIENCE_BOTTLE, "§a§lРівень довіри", 
            "§7" + getTrustBar(passport.getTrustLevel())));
        
        gui.setItem(24, createBalanceItem(passport));
        
        gui.setItem(32, createInfoItem(Material.ROSE_BUSH, "§d§lОдруження", 
            passport.getMarried().isEmpty() ? "§8Ні" : "§a" + passport.getMarried()));

        // Серія та номер
        gui.setItem(4, createSeriesNumberItem(passport));

        // Нижня частина
        gui.setItem(48, createSealItem());
        gui.setItem(49, createVerifyButton(passport));
        gui.setItem(50, createValidityItem(passport));

        // Кнопка закриття
        gui.setItem(53, createCloseButton());

        // Звук відкриття книги
        viewer.playSound(viewer.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);

        // Частинки
        viewer.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE,
            viewer.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);

        viewer.openInventory(gui);
    }

    private void fillBackground(Inventory gui) {
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        
        // Заповнюємо все чорним склом
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, glass);
        }
        
        // Рамка
        int[] borderSlots = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 51, 52};
        for (int slot : borderSlots) {
            gui.setItem(slot, border);
        }
    }

    private ItemStack createPlayerHead(Passport passport) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(passport.getUuid());
            meta.setOwningPlayer(owner);
            meta.setDisplayName("§f§l" + passport.getPlayerName());
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            lore.add("§7Фото власника документа");
            lore.add("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            meta.setLore(lore);
            
            head.setItemMeta(meta);
        }
        
        return head;
    }

    private ItemStack createSeriesNumberItem(Passport passport) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§l" + passport.getSeries() + " §f" + passport.getNumber());
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Серія та номер паспорта");
            lore.add("§8ID: " + passport.getUniqueId());
            meta.setLore(lore);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createInfoItem(Material material, String name, String value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(value);
            meta.setLore(lore);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createBalanceItem(Passport passport) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§e§lБаланс");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            
            if (plugin.isVaultEnabled()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(passport.getUuid());
                double balance = plugin.getEconomy().getBalance(player);
                lore.add("§a$" + String.format("%.2f", balance));
            } else {
                lore.add("§8Vault не підключено");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createSealItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§b§l✦ ПЕЧАТКА СЕРВЕРА ✦");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Офіційний документ");
            lore.add("§7Підтверджений адміністрацією");
            lore.add("");
            lore.add("§8" + plugin.getConfig().getString("server-name", "Minecraft Server"));
            meta.setLore(lore);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createVerifyButton(Passport passport) {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a§lПеревірити справжність");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Натисніть для перевірки");
            lore.add("§7документа в базі даних");
            meta.setLore(lore);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createValidityItem(Passport passport) {
        Material material;
        String status = passport.getValidityStatus();
        
        if (!passport.isValid()) {
            material = Material.RED_WOOL;
        } else if (passport.isDuplicate()) {
            material = Material.YELLOW_WOOL;
        } else {
            material = Material.LIME_WOOL;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§f§lСтатус документа");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(status);
            meta.setLore(lore);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createCloseButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§c§lЗакрити");
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private String getTrustBar(int level) {
        StringBuilder bar = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            if (i <= level) {
                bar.append("§a■");
            } else {
                bar.append("§8□");
            }
        }
        return bar.toString() + " §7(" + level + "/10)";
    }

    public void showVerificationResult(Player player, Passport passport) {
        if (!passport.isValid()) {
            player.sendTitle("§c§l⚠ НЕДІЙСНИЙ", "§7Паспорт анульовано", 10, 40, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        } else if (passport.isDuplicate()) {
            player.sendTitle("§e§l✔ ДУБЛІКАТ", "§7Копія #" + passport.getDuplicateCount(), 10, 40, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
        } else {
            player.sendTitle("§a§l✔ ДІЙСНИЙ", "§7Документ справжній", 10, 40, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        }
    }
}
