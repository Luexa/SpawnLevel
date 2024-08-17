package sh.lue.spawnlevel.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.event.DeletePlayerEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import sh.lue.spawnlevel.manager.MetadataManager;

public final class DeletePlayerListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onDeletePlayer(DeletePlayerEvent event) {
        final var uuid = event.getPlayerUUID();

        for (Town town : TownyAPI.getInstance().getTowns()) {
            MetadataManager.removeFromAllowedSpawnList(town, uuid);
        }

        for (Nation nation : TownyAPI.getInstance().getNations()) {
            MetadataManager.removeFromAllowedSpawnList(nation, uuid);
        }
    }
}
