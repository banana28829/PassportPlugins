package com.minepassport.pro.commands;

import com.minepassport.pro.MinePassportPro;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PassportTabCompleter implements TabCompleter {

    private final MinePassportPro plugin;
    private static final List<String> SUBCOMMANDS = Arrays.asList(
        "give", "revoke", "duplicate", "setstatus", "setbirth", 
        "setcity", "setorg", "settrust", "setmarried", "verify", "info", "reload"
    );
    private static final List<String> STATUSES = Arrays.asList(
        "Житель", "Бізнесмен", "Розшукується", "В'язень", "Мер"
    );

    public PassportTabCompleter(MinePassportPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            completions = SUBCOMMANDS.stream()
                .filter(sub -> sub.startsWith(input))
                .filter(sub -> sender.hasPermission("minepassport." + sub))
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String input = args[1].toLowerCase();

            if (Arrays.asList("give", "revoke", "duplicate", "setstatus", "setbirth", 
                    "setcity", "setorg", "settrust", "setmarried", "verify", "info").contains(subCommand)) {
                completions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            String input = args[2].toLowerCase();

            if (subCommand.equals("setstatus")) {
                completions = STATUSES.stream()
                    .filter(status -> status.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
            } else if (subCommand.equals("settrust")) {
                for (int i = 1; i <= 10; i++) {
                    String num = String.valueOf(i);
                    if (num.startsWith(input)) {
                        completions.add(num);
                    }
                }
            } else if (subCommand.equals("setmarried")) {
                completions.add("ні");
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .toList());
            } else if (subCommand.equals("setbirth")) {
                completions.add("01.01.2000");
            }
        }

        return completions;
    }
}
