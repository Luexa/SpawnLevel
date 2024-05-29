package sh.lue.spawnlevel.object;

import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

public final class AllowSpawnList extends CustomDataField<LinkedHashSet<UUID>> {
    public AllowSpawnList(String key, LinkedHashSet<UUID> value, String label) {
        super(key, value, label);
    }

    public AllowSpawnList(String key, LinkedHashSet<UUID> value) {
        super(key, value);
    }

    @Override
    public String getTypeID() {
        return typeID();
    }

    public static String typeID() {
        return "spawnlevel_allowspawndf";
    }

    @Override
    public void setValueFromString(String string) {
        setValue(deserializeValue(string));
    }

    @Override
    protected String displayFormattedValue() {
        return getValue().stream()
            .map(Object::toString)
            .collect(Collectors.joining(", "));
    }

    @Override
    public String serializeValueToString() {
        return getValue().stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));
    }

    @Override
    protected boolean canParseFromString(String strValue) {
        try {
            deserializeValue(strValue);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    @Override
    public CustomDataField<LinkedHashSet<UUID>> clone() {
        return new AllowSpawnList(getKey(), (LinkedHashSet<UUID>)getValue().clone(), label);
    }

    static LinkedHashSet<UUID> deserializeValue(String string) {
        return Arrays.stream(string.split(","))
            .map(UUID::fromString)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
