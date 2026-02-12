package com.minepassport.pro.listeners;

import com.minepassport.pro.MinePassportPro;
import com.minepassport.pro.data.Passport;
import com.minepassport.pro.gui.GUIManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final MinePassportPro plugin;

    public GUIListener(MinePassportPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (!(holder instanceof GUIManager)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        int slot = event.getRawSlot();

        // Кнопка закриття
        if (slot == 53 && clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Кнопка перевірки справжності
        if (slot == 49 && clicked.getType() == Material.EMERALD) {
            // Знаходимо паспорт за даними в GUI
            String title = event.getView().getTitle();
            if (title.equals(GUIManager.GUI_TITLE)) {
                // Отримуємо паспорт з інвентаря гравця
                for (ItemStack item : player.getInventory().getContents()) {
                    if (plugin.getPassportManager().isPassport(item)) {
                        Passport passport = plugin.getPassportManager().getPassportFromItem(item);
                        if (passport != null) {
                            player.closeInventory();
                            plugin.getGuiManager().showVerificationResult(player, passport);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof GUIManager) {
            event.setCancelled(true);
        }
    }
}
