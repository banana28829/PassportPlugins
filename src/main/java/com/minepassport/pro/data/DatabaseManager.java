package com.minepassport.pro.data;

import com.minepassport.pro.MinePassportPro;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final MinePassportPro plugin;
    private Connection connection;

    public DatabaseManager(MinePassportPro plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    private void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/passports.db";
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("§aПідключено до бази даних SQLite");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Помилка підключення до бази даних!", e);
        }
    }

    private void createTables() {
        String sql = """
            CREATE TABLE IF NOT EXISTS passport_data (
                uuid TEXT PRIMARY KEY,
                player_name TEXT NOT NULL,
                series TEXT NOT NULL UNIQUE,
                number TEXT NOT NULL UNIQUE,
                unique_id TEXT NOT NULL UNIQUE,
                created_date TEXT NOT NULL,
                birth_date TEXT DEFAULT '',
                city TEXT DEFAULT '',
                status TEXT DEFAULT 'Житель',
                organization TEXT DEFAULT '',
                trust_level INTEGER DEFAULT 1,
                married TEXT DEFAULT '',
                is_valid INTEGER DEFAULT 1,
                is_duplicate INTEGER DEFAULT 0,
                duplicate_count INTEGER DEFAULT 0
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Помилка створення таблиці!", e);
        }
    }

    public boolean hasPassport(UUID uuid) {
        String sql = "SELECT 1 FROM passport_data WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Помилка перевірки паспорта!", e);
            return false;
        }
    }

    public boolean seriesNumberExists(String series, String number) {
        String sql = "SELECT 1 FROM passport_data WHERE series = ? OR number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, series);
            stmt.setString(2, number);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return true; // На випадок помилки не даємо створити
        }
    }

    public boolean uniqueIdExists(String uniqueId) {
        String sql = "SELECT 1 FROM passport_data WHERE unique_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uniqueId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return true;
        }
    }

    public void createPassport(Passport passport) {
        String sql = """
            INSERT INTO passport_data 
            (uuid, player_name, series, number, unique_id, created_date, birth_date, city, status, organization, trust_level, married, is_valid, is_duplicate, duplicate_count)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, passport.getUuid().toString());
            stmt.setString(2, passport.getPlayerName());
            stmt.setString(3, passport.getSeries());
            stmt.setString(4, passport.getNumber());
            stmt.setString(5, passport.getUniqueId());
            stmt.setString(6, passport.getCreatedDate());
            stmt.setString(7, passport.getBirthDate());
            stmt.setString(8, passport.getCity());
            stmt.setString(9, passport.getStatus());
            stmt.setString(10, passport.getOrganization());
            stmt.setInt(11, passport.getTrustLevel());
            stmt.setString(12, passport.getMarried());
            stmt.setInt(13, passport.isValid() ? 1 : 0);
            stmt.setInt(14, passport.isDuplicate() ? 1 : 0);
            stmt.setInt(15, passport.getDuplicateCount());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Помилка створення паспорта!", e);
        }
    }

    public Passport getPassport(UUID uuid) {
        String sql = "SELECT * FROM passport_data WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractPassport(rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Помилка отримання паспорта!", e);
        }
        return null;
    }

    public Passport getPassportByUniqueId(String uniqueId) {
        String sql = "SELECT * FROM passport_data WHERE unique_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uniqueId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractPassport(rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Помилка отримання паспорта по ID!", e);
        }
        return null;
    }

    private Passport extractPassport(ResultSet rs) throws SQLException {
        Passport passport = new Passport(
            UUID.fromString(rs.getString("uuid")),
            rs.getString("player_name")
        );
        passport.setSeries(rs.getString("series"));
        passport.setNumber(rs.getString("number"));
        passport.setUniqueId(rs.getString("unique_id"));
        passport.setCreatedDate(rs.getString("created_date"));
        passport.setBirthDate(rs.getString("birth_date"));
        passport.setCity(rs.getString("city"));
        passport.setStatus(rs.getString("status"));
        passport.setOrganization(rs.getString("organization"));
        passport.setTrustLevel(rs.getInt("trust_level"));
        passport.setMarried(rs.getString("married"));
        passport.setValid(rs.getInt("is_valid") == 1);
        passport.setDuplicate(rs.getInt("is_duplicate") == 1);
        passport.setDuplicateCount(rs.getInt("duplicate_count"));
        return passport;
    }

    public void updatePassport(Passport passport) {
        String sql = """
            UPDATE passport_data SET 
            birth_date = ?, city = ?, status = ?, organization = ?, 
            trust_level = ?, married = ?, is_valid = ?, is_duplicate = ?, duplicate_count = ?
            WHERE uuid = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, passport.getBirthDate());
            stmt.setString(2, passport.getCity());
            stmt.setString(3, passport.getStatus());
            stmt.setString(4, passport.getOrganization());
            stmt.setInt(5, passport.getTrustLevel());
            stmt.setString(6, passport.getMarried());
            stmt.setInt(7, passport.isValid() ? 1 : 0);
            stmt.setInt(8, passport.isDuplicate() ? 1 : 0);
            stmt.setInt(9, passport.getDuplicateCount());
            stmt.setString(10, passport.getUuid().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Помилка оновлення паспорта!", e);
        }
    }

    public void revokePassport(UUID uuid) {
        String sql = "UPDATE passport_data SET is_valid = 0 WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Помилка анулювання паспорта!", e);
        }
    }

    public void deletePassport(UUID uuid) {
        String sql = "DELETE FROM passport_data WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Помилка видалення паспорта!", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("§aЗ'єднання з базою даних закрито");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Помилка закриття з'єднання!", e);
        }
    }
}
