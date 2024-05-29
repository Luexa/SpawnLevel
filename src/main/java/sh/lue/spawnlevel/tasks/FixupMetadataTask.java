package sh.lue.spawnlevel.tasks;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import sh.lue.spawnlevel.manager.MetadataManager;

public final class FixupMetadataTask implements Runnable {
    @Override
    public void run() {
        for (Town town : TownyAPI.getInstance().getTowns()) {
            MetadataManager.setupSpawnLevel(town);
            purgeDeletedResidents(town);
        }

        for (Nation nation : TownyAPI.getInstance().getNations()) {
            MetadataManager.setupSpawnLevel(nation);
            purgeDeletedResidents(nation);
        }
    }

    private void purgeDeletedResidents(Government government) {
        final var allowSpawnList = MetadataManager.getAllowedSpawnList(government);
        if (allowSpawnList != null) {
            allowSpawnList.removeIf(uuid -> TownyAPI.getInstance().getResident(uuid) == null);
            MetadataManager.setAllowedSpawnList(government, allowSpawnList);
        }
    }
}
