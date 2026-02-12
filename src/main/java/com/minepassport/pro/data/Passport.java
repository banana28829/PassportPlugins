package com.minepassport.pro.data;

import java.util.UUID;

public class Passport {

    private final UUID uuid;
    private final String playerName;
    private String series;
    private String number;
    private String uniqueId;
    private String createdDate;
    private String birthDate;
    private String city;
    private String status;
    private String organization;
    private int trustLevel;
    private String married;
    private boolean isValid;
    private boolean isDuplicate;
    private int duplicateCount;

    public Passport(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.birthDate = "";
        this.city = "";
        this.status = "Житель";
        this.organization = "";
        this.trustLevel = 1;
        this.married = "";
        this.isValid = true;
        this.isDuplicate = false;
        this.duplicateCount = 0;
    }

    // Getters
    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getSeries() {
        return series;
    }

    public String getNumber() {
        return number;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getCity() {
        return city;
    }

    public String getStatus() {
        return status;
    }

    public String getOrganization() {
        return organization;
    }

    public int getTrustLevel() {
        return trustLevel;
    }

    public String getMarried() {
        return married;
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public String getFullNumber() {
        return series + " " + number;
    }

    // Setters
    public void setSeries(String series) {
        this.series = series;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setTrustLevel(int trustLevel) {
        this.trustLevel = Math.max(1, Math.min(10, trustLevel));
    }

    public void setMarried(String married) {
        this.married = married;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public String getStatusColor() {
        return switch (status) {
            case "Мер" -> "§6";
            case "Бізнесмен" -> "§a";
            case "Розшукується" -> "§c";
            case "В'язень" -> "§4";
            default -> "§7";
        };
    }

    public String getValidityStatus() {
        if (!isValid) return "§c⚠ Анульований";
        if (isDuplicate) return "§e✔ Дублікат #" + duplicateCount;
        return "§a✔ Дійсний";
    }
}
