package sh.lue.spawnlevel.listener;

import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.event.town.toggle.TownTogglePublicEvent;
import com.palmergames.bukkit.towny.event.nation.toggle.NationTogglePublicEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class TogglePublicListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTownTogglePublic(TownTogglePublicEvent event) {
        event.setCancelMessage(Translatable.of("spawnlevel_err_use_town_spawnlevel").forLocale(event.getSender()));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNationTogglePublic(NationTogglePublicEvent event) {
        event.setCancelMessage(Translatable.of("spawnlevel_err_use_nation_spawnlevel").forLocale(event.getSender()));
        event.setCancelled(true);
    }
}
