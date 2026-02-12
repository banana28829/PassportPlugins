package com.minepassport.pro.commands;

import com.minepassport.pro.MinePassportPro;
import com.minepassport.pro.data.Passport;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class PassportCommand implements CommandExecutor {

    private final MinePassportPro plugin;
    private static final List<String> VALID_STATUSES = Arrays.asList(
        "Житель", "Бізнесмен", "Розшукується", "В'язень", "Мер"
    );

    public PassportCommand(MinePassportPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give" -> handleGive(sender, args);
            case "revoke" -> handleRevoke(sender, args);
            case "setstatus" -> handleSetStatus(sender, args);
            case "setbirth" -> handleSetBirth(sender, args);
            case "setcity" -> handleSetCity(sender, args);
            case "setorg" -> handleSetOrg(sender, args);
            case "settrust" -> handleSetTrust(sender, args);
            case "setmarried" -> handleSetMarried(sender, args);
            case "verify" -> handleVerify(sender, args);
            case "duplicate" -> handleDuplicate(sender, args);
            case "info" -> handleInfo(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "minepassport.give")) return;

        if (args.length < 2) {
            sender.sendMessage("§c✖ Використання: /passport give <гравець>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c✖ Гравця не знайдено!");
            return;
        }

        if (plugin.getDatabaseManager().hasPassport(target.getUniqueId())) {
            sender.sendMessage("§c✖ У гравця вже є паспорт!");
            return;
        }

        Passport passport = plugin.getPassportManager().createPassport(target);
        if (passport != null) {
            plugin.getPassportManager().givePassport(target, passport);
            sender.sendMessage("§a✔ Паспорт видано гравцю §e" + target.getName());
            target.sendMessage("§a✔ Вам видано паспорт громадянина!");
            target.sendMessage("§7Серія: §f" + passport.getSeries() + " §7Номер: §f" + passport.getNumber());
        } else {
            sender.sendMessage("§c✖ Помилка створення паспорта!");
        }
    }

    private void handleRevoke(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "minepassport.revoke")) return;

        if (args.length < 2) {
            sender.sendMessage("§c✖ Використання: /passport revoke <гравець>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c✖ Гравця не знайдено!");
            return;
        }

        if (!plugin.getDatabaseManager().hasPassport(target.getUniqueId())) {
            sender.sendMessage("§c✖ У гравця немає паспорта!");
            return;
        }

        plugin.getPassportManager().revokePassport(target);
        sender.sendMessage("§c✔ Паспорт гравця §e" + target.getName() + " §cанульовано!");
        target.sendMessage("§c⚠ Ваш паспорт було анульовано!");
    }

    private void handleSetStatus(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "minepassport.setstatus")) return;

        if (args.length < 3) {
            sender.sendMessage("§c✖ Використання: /passport setstatus <гравець> <статус>");
            sender.sendMessage("§7Доступні статуси: " + String.join(", ", VALID_STATUSES));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c✖ Гравця не знайдено!");
            return;
        }

        Passport passport = plugin.getDatabaseManager().getPassport(target.getUniqueId());
        if (passport == null) {
            sender.sendMessage("§c✖ У гравця немає паспорта!");
            return;
        }

        String status = args[2];
        if (!VALID_STATUSES.contains(status)) {
            sender.sendMessage("§c✖ Невірний статус! Доступні: " + String.join(", ", VALID_STATUSES));
            return;
        }

        passport.setStatus(status);
        plugin.getDatabaseManager().updatePassport(passport);
        plugin.getPassportManager().updatePassportItem(target, passport);

        sender.sendMessage("§a✔ Статус гравця §e" + target.getName() + " §aзмінено на §f" + status);
        target.sendMessage("§a✔ Ваш статус змінено на: " + passport.getStatusColor() + status);
    }

    private void handleSetBirth(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "minepassport.setbirth")) return;

        if (args.length < 3) {
            sender.sendMessage("§c✖ Використання: /passport setbirth <гравець> <дата>");
            sender.sendMessage("§7Формат дати: ДД.ММ.РРРР");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c✖ Гравця не знайдено!");
            return;
        }

        Passport passport = plugin.getDatabaseManager().getPassport(target.getUniqueId());
        if (passport == null) {
            sender.sendMessage("§c✖ У гравця немає паспорта!");
            return;
        }

        String date = args[2];
        if (!date.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
            sender.sendMessage("§c✖ Невірний формат дати! Використовуйте: ДД.ММ.РРРР");
            return;
        }

        passport.setBirthDate(date);
        plugin.getDatabaseManager().updatePassport(passport);
        plugin.getPassportManager().updatePassportItem(target, passport);

        sender.sendMessage("§a✔ Дата народження §e" + target.getName() + " §aвстановлена: §f" + date);
    }

    private void handleSetCity(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "minepassport.setcity")) return;

        if (args.length < 3) {
            sender.sendMessage("§c✖ Використання: /passport setcity <гравець> <місто>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c✖ Гравця не знайдено!");
            return;
        }

        Passport passport = plugin.getDatabaseManager().getPassport(target.getUniqueId());
        if (passport == null) {
            sender.sendMessage("§c✖ У гравця немає паспорта!");
            return;
        }

        String city = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        passport.setCity(city);
        plugin.getDatabaseManager().updatePassport(passport);
        plugin.getPassportManager().updatePassportItem(target, passport);

        sender.sendMessage("§a✔ Місце проживання §e" + target.getName() + " §aвстановлено: §f" + city);
    }

    private void handleSetOrg(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "minepassport.setorg")) return;

        if (args.length < 3) {
            sender.sendMessage("§c✖ Використання: /passport setorg <гравець> <організація>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c✖ Гравця не знайдено!");
            return;
        }

        Passport passport = plugin.getDatabaseManager().getPassport(target.getUniqueId());
        if (passport == null) {
            sender.sendMessage("§c✖ У гравця немає паспорта!");
            return;
        }

        String org = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        passport.setOrganization(org);
        plugin.getDatabaseManager().updatePassport(passport);
        plugin.getPassportManager().updatePassportItem(target, passport);

        sender.sendMessage("§a✔ Організація §e" + target.getName() + " §aвстановлена: §f" + org);
    }

    private void handleSetTrust(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "minepassport.settrust")) return;

        if (args.length < 3) {
            sender.sendMessage("§c✖ Використання: /passport settrust <гравець> <рівень 1-10>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c✖ Гравця не знайдено!");
            return;
        }

        Passport passport = plugin.getDatabaseManager().getPassport(target.getUniqueId());
        if (passport == null) {
            sender.sendMessage("§c✖ У гравця немає паспорта!");
            return;
        }

        try {
            int level = Integer.parseInt(args[2]);
            if (level < 1 || level > 10) {
                sender.sendMessage("§c✖ Рівень повинен бути від 1 до 10!");
                return;
            }

            passport.setTrustLevel(level);
            plugin.getDatabaseManager().updatePassport(passport);
            plugin.getPassportManager().updatePassportItem(target, passport);

            sender.sendMessage("§a✔ Рівень довіри §e" + target.getName() + " §aвстановлено: §f" + level);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c✖ Введіть число від 1 до 10!");
        }
    }

    private void handleSetMarried(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "minepassport.setmarried")) return;

        if (args.length < 3) {
            sender.sendMessage("§c✖ Використання: /passport setmarried <гравець> <партнер/ні>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c✖ Гравця не знайдено!");
            return;
        }

        Passport passport = plugin.getDatabaseManager().getPassport(target.getUniqueId());
        if (passport == null) {
            sender.sendMessage("§c✖ У гравця немає паспорта!");
            return;
        }

        String partner = args[2].equalsIgnoreCase("ні") || args[2].equalsIgnoreCase("no") ? "" : args[2];
        passport.setMarried(partner);
        plugin.getDatabaseManager().updatePassport(passport);
        plugin.getPassportManager().updatePassportItem(target, passport);

        if (partner.isEmpty()) {
            sender.sendMessage("§a✔ Гравець §e" + target.getName() + " §aтепер не одружений");
        } else {
            sender.sendMessage("§a✔ Гравець §e" + target.getName() + " §aодружений з §d" + partner);
        }
    }

    private void handleVerify(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "minepassport.verify")) return;

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c✖ Ця команда тільки для гравців!");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§c✖ Використання: /passport verify <гравець>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c✖ Гравця не знайдено!");
            return;
        }

        Passport passport = plugin.getDatabaseManager().getPassport(target.getUniqueId());
        if (passport == null) {
            player.sendTitle("§c§l✖ НЕ ЗНАЙДЕНО", "§7Паспорт відсутній в базі", 10, 40, 10);
            return;
        }

        plugin.getGuiManager().showVerificationResult(player, passport);
    }

    private void handleDuplicate(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "minepassport.duplicate")) return;

        if (args.length < 2) {
            sender.sendMessage("§c✖ Використання: /passport duplicate <гравець>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c✖ Гравця не знайдено!");
            return;
        }

        Passport passport = plugin.getPassportManager().createDuplicate(target);
        if (passport != null) {
            plugin.getPassportManager().givePassport(target, passport);
            sender.sendMessage("§e✔ Дублікат паспорта видано гравцю §e" + target.getName() + " §7(#" + passport.getDuplicateCount() + ")");
            target.sendMessage("§e✔ Вам видано дублікат паспорта!");
        } else {
            sender.sendMessage("§c✖ У гравця немає оригінального паспорта!");
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "minepassport.info")) return;

        if (args.length < 2) {
            sender.sendMessage("§c✖ Використання: /passport info <гравець>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c✖ Гравця не знайдено!");
            return;
        }

        Passport passport = plugin.getDatabaseManager().getPassport(target.getUniqueId());
        if (passport == null) {
            sender.sendMessage("§c✖ У гравця немає паспорта!");
            return;
        }

        sender.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage("§6§lІнформація про паспорт");
        sender.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage("§7Власник: §f" + passport.getPlayerName());
        sender.sendMessage("§7Серія/Номер: §f" + passport.getFullNumber());
        sender.sendMessage("§7UUID: §8" + passport.getUuid());
        sender.sendMessage("§7Створено: §f" + passport.getCreatedDate());
        sender.sendMessage("§7Статус: " + passport.getStatusColor() + passport.getStatus());
        sender.sendMessage("§7Дійсність: " + passport.getValidityStatus());
        sender.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    private void handleReload(CommandSender sender) {
        if (!hasPermission(sender, "minepassport.reload")) return;

        plugin.reload();
        sender.sendMessage("§a✔ Конфігурацію плагіна перезавантажено!");
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage("§c✖ Недостатньо прав!");
            return false;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage("§6§lMinePassportPro §7— Команди");
        sender.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage("§e/passport give <гравець> §7— Видати паспорт");
        sender.sendMessage("§e/passport revoke <гравець> §7— Анулювати паспорт");
        sender.sendMessage("§e/passport duplicate <гравець> §7— Видати дублікат");
        sender.sendMessage("§e/passport setstatus <гравець> <статус> §7— Змінити статус");
        sender.sendMessage("§e/passport setbirth <гравець> <дата> §7— Встановити дату народження");
        sender.sendMessage("§e/passport setcity <гравець> <місто> §7— Встановити місто");
        sender.sendMessage("§e/passport setorg <гравець> <орг> §7— Встановити організацію");
        sender.sendMessage("§e/passport settrust <гравець> <1-10> §7— Встановити рівень довіри");
        sender.sendMessage("§e/passport setmarried <гравець> <партнер/ні> §7— Одруження");
        sender.sendMessage("§e/passport verify <гравець> §7— Перевірити паспорт");
        sender.sendMessage("§e/passport info <гравець> §7— Інформація про паспорт");
        sender.sendMessage("§e/passport reload §7— Перезавантажити конфіг");
        sender.sendMessage("§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }
}
