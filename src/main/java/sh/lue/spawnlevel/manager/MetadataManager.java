package sh.lue.spawnlevel.manager;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import java.util.LinkedHashSet;
import java.util.UUID;
import sh.lue.spawnlevel.object.AllowSpawnList;

public final class MetadataManager {
    private static final String SPAWNLEVEL_DF = "spawnlevel_spawnlevel";
    private static final String ALLOWSPAWN_DF = "spawnlevel_allowspawn";

    public static String getSpawnLevel(Government object) {
        if (object.hasMeta(SPAWNLEVEL_DF)) {
            final CustomDataField<?> cdf = object.getMetadata(SPAWNLEVEL_DF);
            if (cdf instanceof StringDataField sdf) {
                return sdf.getValue();
            }
        }
        return null;
    }

    public static void setSpawnLevel(Government object, String spawnLevel, boolean save) {
        if (object.hasMeta(SPAWNLEVEL_DF)) {
            final CustomDataField<?> cdf = object.getMetadata(SPAWNLEVEL_DF);
            if (cdf instanceof StringDataField sdf) {
                sdf.setValue(spawnLevel);
                if (save) {
                    object.save();
                }
                return;
            }
        }
        object.addMetaData(new StringDataField(SPAWNLEVEL_DF, spawnLevel));
        if (save) {
            object.save();
        }
    }

    public static void setSpawnLevel(Government object, String spawnLevel) {
        setSpawnLevel(object, spawnLevel, true);
    }

    public static void setupSpawnLevel(Government object) {
        final String spawnLevel = getSpawnLevel(object);
        if (spawnLevel == null) {
            if (object.isPublic()) {
                MetadataManager.setSpawnLevel(object, "outsider");
            } else {
                final String defaultSpawnLevel = object instanceof Town ? "resident" : "nation";
                MetadataManager.setSpawnLevel(object, defaultSpawnLevel, false);
                object.setPublic(true);
                object.save();
            }
        } else if (!object.isPublic()) {
            object.setPublic(true);
            object.save();
        }
    }

    public static LinkedHashSet<UUID> getAllowedSpawnList(Government object) {
        if (object.hasMeta(ALLOWSPAWN_DF)) {
            final CustomDataField<?> cdf = object.getMetadata(ALLOWSPAWN_DF);
            if (cdf instanceof AllowSpawnList als) {
                return als.getValue();
            }
        }
        return null;
    }

    public static void setAllowedSpawnList(Government object, LinkedHashSet<UUID> allowSpawnList, boolean save) {
        if (allowSpawnList == null || allowSpawnList.isEmpty()) {
            if (object.removeMetaData(ALLOWSPAWN_DF)) {
                if (save) {
                    object.save();
                }
            }
            return;
        }
        if (object.hasMeta(ALLOWSPAWN_DF)) {
            final CustomDataField<?> cdf = object.getMetadata(ALLOWSPAWN_DF);
            if (cdf instanceof AllowSpawnList als) {
                als.setValue(allowSpawnList);
                if (save) {
                    object.save();
                }
                return;
            }
        }
        object.addMetaData(new AllowSpawnList(ALLOWSPAWN_DF, allowSpawnList));
        if (save) {
            object.save();
        }
    }

    public static void setAllowedSpawnList(Government object, LinkedHashSet<UUID> allowSpawnList) {
        setAllowedSpawnList(object, allowSpawnList, true);
    }

    public static boolean addToAllowedSpawnList(Government object, UUID uuid) {
        if (uuid == null) {
            return false;
        }
        LinkedHashSet<UUID> allowSpawnList = getAllowedSpawnList(object);
        if (allowSpawnList == null) {
            allowSpawnList = new LinkedHashSet<>();
        }
        if (allowSpawnList.add(uuid)) {
            setAllowedSpawnList(object, allowSpawnList);
            return true;
        }
        return false;
    }

    public static boolean removeFromAllowedSpawnList(Government object, UUID uuid) {
        if (uuid == null) {
            return false;
        }
        final LinkedHashSet<UUID> allowSpawnList = getAllowedSpawnList(object);
        if (allowSpawnList != null && allowSpawnList.remove(uuid)) {
            setAllowedSpawnList(object, allowSpawnList);
            return true;
        }
        return false;
    }

    public static boolean inAllowedSpawnList(Government object, UUID uuid) {
        if (uuid == null) {
            return false;
        }
        final LinkedHashSet<UUID> allowSpawnList = getAllowedSpawnList(object);
        return allowSpawnList != null && allowSpawnList.contains(uuid);
    }
}
