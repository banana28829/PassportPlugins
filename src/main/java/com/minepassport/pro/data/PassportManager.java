package com.minepassport.pro.data;

import com.minepassport.pro.MinePassportPro;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PassportManager {

    private final MinePassportPro plugin;
    private final NamespacedKey passportKey;
    private final NamespacedKey uniqueIdKey;
    private final Random random;
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public PassportManager(MinePassportPro plugin) {
        this.plugin = plugin;
        this.passportKey = new NamespacedKey(plugin, "passport");
        this.uniqueIdKey = new NamespacedKey(plugin, "passport_id");
        this.random = new Random();
    }

    public Passport createPassport(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (plugin.getDatabaseManager().hasPassport(uuid)) {
            return null;
        }

        Passport passport = new Passport(uuid, player.getName());
        
        // Генеруємо унікальну серію та номер
        String series;
        String number;
        do {
            series = generateSeries();
            number = generateNumber();
        } while (plugin.getDatabaseManager().seriesNumberExists(series, number));

        passport.setSeries(series);
        passport.setNumber(number);

        // Генеруємо унікальний ID
        String uniqueId;
        do {
            uniqueId = generateUniqueId();
        } while (plugin.getDatabaseManager().uniqueIdExists(uniqueId));

        passport.setUniqueId(uniqueId);
        passport.setCreatedDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        plugin.getDatabaseManager().createPassport(passport);

        return passport;
    }

    public Passport createDuplicate(Player player) {
        UUID uuid = player.getUniqueId();
        Passport existing = plugin.getDatabaseManager().getPassport(uuid);
        
        if (existing == null) {
            return null;
        }

        existing.setDuplicate(true);
        existing.setDuplicateCount(existing.getDuplicateCount() + 1);
        existing.setValid(true);
        
        plugin.getDatabaseManager().updatePassport(existing);
        
        return existing;
    }

    public ItemStack createPassportItem(Passport passport) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Назва паспорта
            String name = "§6§lПАСПОРТ ГРОМАДЯНИНА";
            if (passport.isDuplicate()) {
                name += " §c[ДУБЛІКАТ]";
            }
            if (!passport.isValid()) {
                name = "§c§l§mПАСПОРТ ГРОМАДЯНИНА§r §4[АНУЛЬОВАНО]";
            }
            meta.setDisplayName(name);

            // Лор
            List<String> lore = new ArrayList<>();
            lore.add("§7Серія: §f" + passport.getSeries());
            lore.add("§7Номер: §f" + passport.getNumber());
            lore.add("");
            lore.add("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            lore.add("§7Власник: §e" + passport.getPlayerName());
            lore.add("§7Статус: " + passport.getStatusColor() + passport.getStatus());
            lore.add("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            lore.add("");
            lore.add("§8Власність сервера");
            lore.add("§7ПКМ — відкрити паспорт");
            
            meta.setLore(lore);

            // Прихований унікальний ID
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(passportKey, PersistentDataType.BOOLEAN, true);
            container.set(uniqueIdKey, PersistentDataType.STRING, passport.getUniqueId());

            item.setItemMeta(meta);
        }

        return item;
    }

    public boolean isPassport(ItemStack item) {
        if (item == null || item.getType() != Material.WRITTEN_BOOK) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(passportKey, PersistentDataType.BOOLEAN);
    }

    public String getPassportUniqueId(ItemStack item) {
        if (!isPassport(item)) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(uniqueIdKey, PersistentDataType.STRING);
    }

    public Passport getPassportFromItem(ItemStack item) {
        String uniqueId = getPassportUniqueId(item);
        if (uniqueId == null) {
            return null;
        }
        return plugin.getDatabaseManager().getPassportByUniqueId(uniqueId);
    }

    public void givePassport(Player player, Passport passport) {
        ItemStack passportItem = createPassportItem(passport);
        
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(passportItem);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), passportItem);
        }
    }

    public void revokePassport(Player player) {
        plugin.getDatabaseManager().revokePassport(player.getUniqueId());
        
        // Видаляємо паспорт з інвентаря
        for (ItemStack item : player.getInventory().getContents()) {
            if (isPassport(item)) {
                Passport passport = getPassportFromItem(item);
                if (passport != null && passport.getUuid().equals(player.getUniqueId())) {
                    player.getInventory().remove(item);
                }
            }
        }
    }

    public void updatePassportItem(Player player, Passport passport) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isPassport(item)) {
                String uniqueId = getPassportUniqueId(item);
                if (uniqueId != null && uniqueId.equals(passport.getUniqueId())) {
                    player.getInventory().setItem(i, createPassportItem(passport));
                    break;
                }
            }
        }
    }

    private String generateSeries() {
        char first = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        char second = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        return String.valueOf(first) + second;
    }

    private String generateNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateUniqueId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    public NamespacedKey getPassportKey() {
        return passportKey;
    }

    public NamespacedKey getUniqueIdKey() {
        return uniqueIdKey;
    }
}
