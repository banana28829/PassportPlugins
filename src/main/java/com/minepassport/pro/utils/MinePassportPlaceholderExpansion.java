package com.minepassport.pro.utils;

import com.minepassport.pro.MinePassportPro;
import com.minepassport.pro.data.Passport;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class MinePassportPlaceholderExpansion extends PlaceholderExpansion {

    private final MinePassportPro plugin;

    public MinePassportPlaceholderExpansion(MinePassportPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "minepassport";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || player.getUniqueId() == null) {
            return "";
        }

        Passport passport = plugin.getDatabaseManager().getPassport(player.getUniqueId());
        if (passport == null) {
            return "";
        }

        return switch (params.toLowerCase()) {
            case "series" -> passport.getSeries();
            case "number" -> passport.getNumber();
            case "fullnumber" -> passport.getFullNumber();
            case "status" -> passport.getStatus();
            case "city" -> passport.getCity();
            case "birth" -> passport.getBirthDate();
            case "trust" -> String.valueOf(passport.getTrustLevel());
            case "valid" -> passport.getValidityStatus();
            default -> null;
        };
    }
}
