package sh.lue.spawnlevel.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.event.TownSpawnEvent;
import com.palmergames.bukkit.towny.event.NationSpawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.Objects;
import sh.lue.spawnlevel.manager.MetadataManager;

public final class SpawnListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onTownSpawn(TownSpawnEvent event) {
        final Player player = event.getPlayer();
        if (hasAdminSpawn(player)) return;

        final Resident resident = TownyAPI.getInstance().getResident(player);

        final Town town = event.getToTown();
        if (town == null) {
            event.setCancelMessage(Translatable.of("msg_err_town_has_not_set_a_spawn_location").forLocale(player));
            event.setCancelled(true);
            return;
        }

        final Town resTown = resident == null ? null : resident.getTownOrNull();

        final String spawnLevel = MetadataManager.getSpawnLevel(town);
        if (spawnLevel.equals("outsider")) return;
        if (Objects.equals(town, resTown)) return;
        if (MetadataManager.inAllowedSpawnList(town, player.getUniqueId())) return;
        if (resident != null && town.hasTrustedResident(resident)) return;

        final Nation nation = town.getNationOrNull();
        final Nation resNation = resTown == null ? null : resTown.getNationOrNull();

        if (!spawnLevel.equals("resident") && nation != null && resNation != null) {
            if (spawnLevel.equals("ally") && nation.isAlliedWith(resNation)) return;
            if (nation.equals(resNation)) return;
        }

        event.setCancelMessage(Translatable.of("spawnlevel_err_teleport_denied_spawnlevel_" + spawnLevel, town.getName()).forLocale(player));
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onNationSpawn(NationSpawnEvent event) {
        final Player player = event.getPlayer();
        if (hasAdminSpawn(player)) return;

        final Resident resident = TownyAPI.getInstance().getResident(player);

        final Nation nation = event.getToNation();
        if (nation == null) {
            event.setCancelMessage(Translatable.of("msg_err_nation_has_not_set_a_spawn_location").forLocale(player));
            event.setCancelled(true);
            return;
        }

        final Town nationCapital = nation.getCapital();

        if (resident != null && nationCapital != null && nationCapital.hasOutlaw(resident) && !TownyUniverse.getInstance().getPermissionSource().testPermission(player, "spawnlevel.bypass_capital_ban")) {
            event.setCancelMessage(Translatable.of("spawnlevel_err_capital_ban", nation.getName()).forLocale(player));
            event.setCancelled(true);
            return;
        }

        final String spawnLevel = MetadataManager.getSpawnLevel(nation);
        if (spawnLevel.equals("outsider")) return;
        if (MetadataManager.inAllowedSpawnList(nation, player.getUniqueId())) return;
        if (resident != null && resident.hasNation()) {
            final Nation resNation = resident.getTownOrNull().getNationOrNull();
            if (spawnLevel.equals("ally") && nation.isAlliedWith(resNation)) return;
            if (nation.equals(resNation)) return;
        }

        event.setCancelMessage(Translatable.of("spawnlevel_err_teleport_denied_spawnlevel_" + spawnLevel, nation.getName()).forLocale(player));
        event.setCancelled(true);
    }

    private boolean hasAdminSpawn(Player player) {
        final var permissionSource = TownyUniverse.getInstance().getPermissionSource();
        return permissionSource.isTownyAdmin(player) || permissionSource.testPermission(player, "towny.admin.spawn");
    }
}
