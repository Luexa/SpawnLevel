package sh.lue.spawnlevel.listener;

import com.palmergames.bukkit.towny.event.time.NewHourEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import sh.lue.spawnlevel.tasks.FixupMetadataTask;

public final class NewHourListener implements Listener {
    @EventHandler
    public void onNewHour(NewHourEvent event) {
        new FixupMetadataTask().run();
    }
}
