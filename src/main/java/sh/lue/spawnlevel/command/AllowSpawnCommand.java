package sh.lue.spawnlevel.command;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.utils.NameUtil;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.util.StringMgmt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.Objects;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;
import sh.lue.spawnlevel.manager.MetadataManager;

public final class AllowSpawnCommand implements CommandExecutor, TabCompleter {
    private static final List<String> allowSpawnTabCompletes = Arrays.asList(
        "add",
        "remove",
        "list"
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final CommandType commandType = ((AddonCommand)command).getCommandType();

        switch (commandType) {
            case TOWN, NATION -> {
                if (!(sender instanceof Player)) {
                    TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_command_not_for_console_use"));
                    return true;
                }
            }
        }

        switch (commandType) {
            case TOWN -> townCommand(sender, false, null, args);
            case NATION -> nationCommand(sender, false, null, args);
            case TOWNYADMIN_TOWN -> townCommand(sender, true, args[0], StringMgmt.remArgs(args, 2));
            case TOWNYADMIN_NATION -> nationCommand(sender, true, args[0], StringMgmt.remArgs(args, 2));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final CommandType commandType = ((AddonCommand)command).getCommandType();
        final int firstArg = switch (commandType) {
            case TOWNYADMIN_TOWN, TOWNYADMIN_NATION -> 1;
            default -> 0;
        };
        final boolean isTown = switch (commandType) {
            case TOWN, TOWNYADMIN_TOWN -> true;
            default -> false;
        };

        if (args.length == firstArg + 1) {
            return NameUtil.filterByStart(allowSpawnTabCompletes, args[firstArg]);
        }

        if (args.length == firstArg + 2) {
            switch (args[firstArg].toLowerCase(Locale.ROOT)) {
                case "add" -> {
                    return BaseCommand.getTownyStartingWith(args[firstArg + 1], "r");
                }
                case "remove" -> {
                    return allowedSpawnNameList(sender, isTown, args[firstArg + 1], firstArg > 0);
                }
                case "list" -> {
                    if (firstArg == 0) {
                        return BaseCommand.getTownyStartingWith(args[1], isTown ? "t" : "n");
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> allowedSpawnNameList(CommandSender sender, boolean isTown, String arg, boolean admin) {
        Government government = null;
        if (admin) {
            return BaseCommand.getTownyStartingWith(arg, "r");
        } else if (sender instanceof Player player) {
            final Resident resident = TownyAPI.getInstance().getResident(player);
            final Town town = resident == null ? null : resident.getTownOrNull();
            if (isTown) {
                government = town;
            } else if (town != null) {
                government = town.getNationOrNull();
            }
        }

        if (government == null) {
            return Collections.emptyList();
        }

        final var allowSpawnUUIDs = MetadataManager.getAllowedSpawnList(government);
        if (allowSpawnUUIDs == null) {
            return Collections.emptyList();
        }

        final String lowercaseArg = arg.toLowerCase(Locale.ROOT);
        return allowSpawnUUIDs.stream()
            .map(uuid -> TownyAPI.getInstance().getResident(uuid))
            .map(res -> res == null ? null : res.getName())
            .filter(name -> name != null && name.toLowerCase(Locale.ROOT).startsWith(lowercaseArg))
            .collect(Collectors.toList());
    }

    private boolean checkPermission(CommandSender sender, boolean admin, String scope) {
        final String permissionNode =
            "spawnlevel.command." + (admin ? "admin." : "") + scope + ".allowspawn";
        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(sender, permissionNode)) {
            TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_command_disable"));
            return false;
        }
        return true;
    }

    private void sendUsage(CommandSender sender, boolean admin, boolean town) {
        final String objectType = town ? "town" : "nation";
        final String command = admin ?
            "/townyadmin " + objectType + " [" + objectType + "] allowspawn" :
            "/" + objectType + " allowspawn";

        TownyMessaging.sendMessage(sender, ChatTools.formatTitle(command));
        sendFormattedCommand(sender, command, "add [name]", objectType, "add");
        sendFormattedCommand(sender, command, "remove [name]", objectType, "remove");
        if (admin) {
            sendFormattedCommand(sender, command, "list", objectType, "list_other");
        } else {
            sendFormattedCommand(sender, command, "list", objectType, "list_your");
            sendFormattedCommand(sender, command, "list [" + objectType + "]", objectType, "list_other");
        }
    }

    private void sendFormattedCommand(CommandSender sender, String command, String subcommand, String objectType, String descKey) {
        final String desc = Translatable.of("spawnlevel_usage_allowspawn_" + descKey + "_" + objectType).forLocale(sender);
        TownyMessaging.sendMessage(sender, ChatTools.formatCommand(command, subcommand, desc));
    }

    private void townCommand(CommandSender sender, boolean admin, String townName, String[] args) {
        if (args.length < 1) {
            sendUsage(sender, admin, true);
            return;
        }

        final String subcommand = args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "list" -> {}
            case "add", "remove" -> {
                if (!checkPermission(sender, admin, "town")) {
                    return;
                }
                if (args.length < 2) {
                    sendUsage(sender, admin, true);
                    return;
                }
            }
            default -> {
                sendUsage(sender, admin, true);
                return;
            }
        }

        Town town = null;
        final Resident resident = sender instanceof Player ? TownyAPI.getInstance().getResident((Player)sender) : null;
        if (admin) {
            town = TownyAPI.getInstance().getTown(townName);
            if (town == null) {
                TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_town_unknown", townName));
                return;
            }
        } else if (subcommand.equals("list") && args.length >= 2) {
            town = TownyAPI.getInstance().getTown(args[1]);
            if (town == null) {
                TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_town_unknown", townName));
                return;
            }
        } else if (resident == null) {
            TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_not_registered"));
            return;
        } else {
            town = resident.getTownOrNull();
            if (town == null) {
                TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_dont_belong_town"));
                return;
            }
        }

        switch (subcommand) {
            case "list" -> printAllowSpawnList(sender, town);
            case "add" -> addToList(sender, town, args[1]);
            case "remove" -> removeFromList(sender, town, args[1]);
        }
    }

    private void nationCommand(CommandSender sender, boolean admin, String nationName, String[] args) {
        if (args.length < 1) {
            sendUsage(sender, admin, false);
            return;
        }

        final String subcommand = args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "list" -> {}
            case "add", "remove" -> {
                if (!checkPermission(sender, admin, "nation")) {
                    return;
                }
                if (args.length < 2) {
                    sendUsage(sender, admin, false);
                    return;
                }
            }
            default -> {
                sendUsage(sender, admin, false);
                return;
            }
        }

        Nation nation = null;
        final Resident resident = sender instanceof Player ? TownyAPI.getInstance().getResident((Player)sender) : null;
        if (admin) {
            nation = TownyAPI.getInstance().getNation(nationName);
            if (nation == null) {
                TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_nation_unknown", nationName));
                return;
            }
        } else if (subcommand.equals("list") && args.length >= 2) {
            nation = TownyAPI.getInstance().getNation(args[1]);
            if (nation == null) {
                TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_nation_unknown", nationName));
                return;
            }
        } else if (resident == null) {
            TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_not_registered"));
            return;
        } else {
            final Town town = resident.getTownOrNull();
            nation = town == null ? null : town.getNationOrNull();
            if (nation == null) {
                TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_dont_belong_nation"));
                return;
            }
        }

        switch (subcommand) {
            case "list" -> printAllowSpawnList(sender, nation);
            case "add" -> addToList(sender, nation, args[1]);
            case "remove" -> removeFromList(sender, nation, args[1]);
        }
    }

    private void printAllowSpawnList(CommandSender sender, Government government) {
        final var allowSpawnUUIDs = MetadataManager.getAllowedSpawnList(government);
        final List<Resident> allowSpawnResidents = allowSpawnUUIDs == null || allowSpawnUUIDs.isEmpty() ?
            Collections.emptyList() :
            allowSpawnUUIDs.stream()
                .map(uuid -> TownyAPI.getInstance().getResident(uuid))
                .filter(res -> res != null)
                .collect(Collectors.toList());

        if (allowSpawnResidents.isEmpty()) {
            final String govType = government instanceof Town ? "town" : "nation";
            TownyMessaging.sendMsg(sender, Translatable.of("spawnlevel_msg_allowspawn_list_empty_" + govType));
            return;
        }

        final String title = government.getName() + " " + Translatable.of("spawnlevel_allowspawn_list").forLocale(sender);
        final String listPrefix = Translatable.of("spawnlevel_allowspawn_list_short").forLocale(sender);
        final String formattedList = TownyFormatter.getFormattedTownyObjects(listPrefix, new ArrayList<>(allowSpawnResidents));
        TownyMessaging.sendMessage(sender, ChatTools.formatTitle(title));
        TownyMessaging.sendMessage(sender, formattedList);
    }

    private void addToList(CommandSender sender, Government government, String targetName) {
        final Resident targetResident = TownyAPI.getInstance().getResident(targetName);
        if (targetResident == null) {
            TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_resident_unknown", targetName));
            return;
        }

        if (!MetadataManager.addToAllowedSpawnList(government, targetResident.getUUID())) {
            TownyMessaging.sendErrorMsg(sender, Translatable.of("spawnlevel_err_allowspawn_exists", targetResident.getName()));
            return;
        }

        final Player targetPlayer = targetResident.isOnline() ? targetResident.getPlayer() : null;
        if (targetPlayer != null) {
            TownyMessaging.sendMsg(targetPlayer, Translatable.of("spawnlevel_msg_allowspawn_added_you", government.getName()));
        }

        sendGovernmentMessage(sender, government, Translatable.of("spawnlevel_msg_allowspawn_added_player", targetResident.getName()));
    }

    private void removeFromList(CommandSender sender, Government government, String targetName) {
        final Resident targetResident = TownyAPI.getInstance().getResident(targetName);
        if (targetResident == null) {
            TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_resident_unknown", targetName));
            return;
        }

        if (!MetadataManager.removeFromAllowedSpawnList(government, targetResident.getUUID())) {
            TownyMessaging.sendErrorMsg(sender, Translatable.of("spawnlevel_err_allowspawn_doesnt_exist", targetResident.getName()));
            return;
        }

        final Player targetPlayer = targetResident.isOnline() ? targetResident.getPlayer() : null;
        if (targetPlayer != null) {
            TownyMessaging.sendMsg(targetPlayer, Translatable.of("spawnlevel_msg_allowspawn_removed_you", government.getName()));
        }

        sendGovernmentMessage(sender, government, Translatable.of("spawnlevel_msg_allowspawn_removed_player", targetResident.getName()));
    }

    private void sendGovernmentMessage(CommandSender sender, Government government, Translatable message) {
        final Resident senderResident = sender instanceof Player ? TownyAPI.getInstance().getResident((Player)sender) : null;

        if (government instanceof Town town) {
            TownyMessaging.sendPrefixedTownMessage(town, message);
            if (sender instanceof Player && (senderResident == null || !Objects.equals(town, senderResident.getTownOrNull()))) {
                TownyMessaging.sendMessage(sender, Translatable.of("default_town_prefix", StringMgmt.remUnderscore(town.getName())).append(message));
            }
        } else if (government instanceof Nation nation) {
            TownyMessaging.sendPrefixedNationMessage(nation, message);
            if (sender instanceof Player && (senderResident == null || !senderResident.hasNation() || !Objects.equals(nation, senderResident.getTownOrNull().getNationOrNull()))) {
                TownyMessaging.sendMessage(sender, Translatable.of("default_nation_prefix", StringMgmt.remUnderscore(nation.getName())).append(message));
            }
        }
    }
}
