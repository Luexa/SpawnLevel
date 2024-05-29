package sh.lue.spawnlevel.command;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.utils.NameUtil;
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
import java.util.Locale;
import sh.lue.spawnlevel.manager.MetadataManager;

public final class SpawnLevelCommand implements CommandExecutor, TabCompleter {
    private static final List<String> townSpawnLevelTabCompletes = Arrays.asList(
        "resident",
        "nation",
        "ally",
        "outsider"
    );

    private static final List<String> nationSpawnLevelTabCompletes = Arrays.asList(
        "nation",
        "ally",
        "outsider"
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
        final List<String> tabCompletes = switch (commandType) {
            case TOWN, TOWNYADMIN_TOWN -> townSpawnLevelTabCompletes;
            default -> nationSpawnLevelTabCompletes;
        };

        if (args.length == firstArg + 1) {
            return NameUtil.filterByStart(tabCompletes, args[firstArg]);
        }

        return Collections.emptyList();
    }

    private boolean checkPermission(CommandSender sender, boolean admin, String scope) {
        final String permissionNode =
            "spawnlevel.command." + (admin ? "admin." : "") + scope + ".spawnlevel";
        if (!TownyUniverse.getInstance().getPermissionSource().testPermission(sender, permissionNode)) {
            TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_command_disable"));
            return false;
        }
        return true;
    }

    private void sendUsage(CommandSender sender, boolean admin, boolean town) {
        final String objectType = town ? "town" : "nation";
        final String command = admin ?
            "/townyadmin " + objectType + " [" + objectType + "] spawnlevel" :
            "/" + objectType + " spawnlevel";

        TownyMessaging.sendMessage(sender, ChatTools.formatTitle(command));
        if (town) {
            sendFormattedCommand(sender, command, "resident");
        }
        sendFormattedCommand(sender, command, "nation");
        sendFormattedCommand(sender, command, "ally");
        sendFormattedCommand(sender, command, "outsider");
    }

    private void sendFormattedCommand(CommandSender sender, String command, String spawnLevel) {
        final String desc = Translatable.of("spawnlevel_usage_spawnlevel_" + spawnLevel).forLocale(sender);
        TownyMessaging.sendMessage(sender, ChatTools.formatCommand(command, spawnLevel, desc));
    }

    private void townCommand(CommandSender sender, boolean admin, String townName, String[] args) {
        if (args.length < 1) {
            sendUsage(sender, admin, true);
            return;
        }

        final String spawnLevel = args[0].toLowerCase(Locale.ROOT);
        switch (spawnLevel) {
            case "resident", "nation", "ally", "outsider" -> {}
            default -> {
                sendUsage(sender, admin, true);
                return;
            }
        }

        if (!checkPermission(sender, admin, "town")) {
            return;
        }

        Town town = null;
        final Resident resident = sender instanceof Player ? TownyAPI.getInstance().getResident((Player)sender) : null;
        if (admin) {
            town = TownyAPI.getInstance().getTown(townName);
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

        MetadataManager.setSpawnLevel(town, spawnLevel);
        final Translatable message = Translatable.of("spawnlevel_msg_updated_spawn_level_" + spawnLevel);
        TownyMessaging.sendPrefixedTownMessage(town, message);
        if (admin && sender instanceof Player && (resident == null || !Objects.equals(resident.getTownOrNull(), town))) {
            TownyMessaging.sendMessage(sender, Translatable.of("default_town_prefix", StringMgmt.remUnderscore(town.getName())).append(message));
        }
    }

    private void nationCommand(CommandSender sender, boolean admin, String nationName, String[] args) {
        if (args.length < 1) {
            sendUsage(sender, admin, false);
            return;
        }

        final String spawnLevel = args[0].toLowerCase(Locale.ROOT);
        switch (spawnLevel) {
            case "nation", "ally", "outsider" -> {}
            default -> {
                sendUsage(sender, admin, false);
                return;
            }
        }

        if (!checkPermission(sender, admin, "nation")) {
            return;
        }

        Nation nation = null;
        final Resident resident = sender instanceof Player ? TownyAPI.getInstance().getResident((Player)sender) : null;
        if (admin) {
            nation = TownyAPI.getInstance().getNation(nationName);
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

        MetadataManager.setSpawnLevel(nation, spawnLevel);
        final Translatable message = Translatable.of("spawnlevel_msg_updated_spawn_level_" + spawnLevel);
        TownyMessaging.sendPrefixedNationMessage(nation, message);
        if (admin && sender instanceof Player && (resident == null || !resident.hasNation() || !Objects.equals(resident.getTownOrNull().getNationOrNull(), nation))) {
            TownyMessaging.sendMessage(sender, Translatable.of("default_nation_prefix", StringMgmt.remUnderscore(nation.getName())).append(message));
        }
    }
}
