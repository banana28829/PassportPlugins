package com.minepassport.pro.listeners;

import com.minepassport.pro.MinePassportPro;
import com.minepassport.pro.data.Passport;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class PassportListener implements Listener {

    private final MinePassportPro plugin;

    public PassportListener(MinePassportPro plugin) {
        this.plugin = plugin;
    }

    // ПКМ на паспорт — відкриває GUI
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT")) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !plugin.getPassportManager().isPassport(item)) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        Passport passport = plugin.getPassportManager().getPassportFromItem(item);

        if (passport != null) {
            plugin.getGuiManager().openPassportGUI(player, passport);
        } else {
            player.sendMessage("§c✖ Помилка читання паспорта!");
        }
    }

    // Заборона викидання паспорта
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        if (plugin.getPassportManager().isPassport(item)) {
            if (!event.getPlayer().hasPermission("minepassport.drop")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§c✖ Ви не можете викинути паспорт!");
            }
        }
    }

    // Заборона класти паспорт в сундуки та інші інвентарі
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }

        // Якщо гравець кликає по верхньому інвентарю (сундук тощо)
        if (event.getClickedInventory().getType() != InventoryType.PLAYER) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // Перевіряємо, чи намагається гравець перемістити паспорт
        if (event.getView().getTopInventory().getType() != InventoryType.CRAFTING &&
            event.getView().getTopInventory().getType() != InventoryType.CREATIVE) {

            if (plugin.getPassportManager().isPassport(currentItem)) {
                // Shift-клік або переміщення паспорта в інший інвентар
                if (event.isShiftClick()) {
                    if (!event.getWhoClicked().hasPermission("minepassport.transfer")) {
                        event.setCancelled(true);
                        event.getWhoClicked().sendMessage("§c✖ Ви не можете перемістити паспорт!");
                    }
                }
            }
        }

        // Якщо кликаємо по чужому інвентарю з паспортом на курсорі
        if (event.getClickedInventory() != event.getWhoClicked().getInventory()) {
            if (plugin.getPassportManager().isPassport(cursorItem)) {
                if (!event.getWhoClicked().hasPermission("minepassport.transfer")) {
                    event.setCancelled(true);
                    event.getWhoClicked().sendMessage("§c✖ Ви не можете перемістити паспорт в цей інвентар!");
                }
            }
        }
    }

    // Заборона переміщення паспорта між руками (F)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        ItemStack mainHand = event.getMainHandItem();
        ItemStack offHand = event.getOffHandItem();

        if (plugin.getPassportManager().isPassport(mainHand) || 
            plugin.getPassportManager().isPassport(offHand)) {
            // Дозволяємо переміщення між руками
        }
    }

    // При смерті паспорт не випадає
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.getConfig().getBoolean("keep-on-death", true)) {
            event.getDrops().removeIf(item -> plugin.getPassportManager().isPassport(item));
        }
    }
}
