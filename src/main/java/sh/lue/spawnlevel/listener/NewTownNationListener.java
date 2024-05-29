package sh.lue.spawnlevel.listener;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.NewNationEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import sh.lue.spawnlevel.manager.MetadataManager;

public final class NewTownNationListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNewTown(NewTownEvent event) {
        MetadataManager.setupSpawnLevel(event.getTown());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNewNation(NewNationEvent event) {
        MetadataManager.setupSpawnLevel(event.getNation());
    }
}
