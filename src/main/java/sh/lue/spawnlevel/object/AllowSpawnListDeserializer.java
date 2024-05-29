package sh.lue.spawnlevel.object;

import com.palmergames.bukkit.towny.object.metadata.DataFieldDeserializer;

public final class AllowSpawnListDeserializer implements DataFieldDeserializer<AllowSpawnList> {
    @Override
    public AllowSpawnList deserialize(String key, String value) {
        return new AllowSpawnList(key, AllowSpawnList.deserializeValue(value == null ? "" : value));
    }
}
