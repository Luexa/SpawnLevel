package sh.lue.spawnlevel;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.object.TranslationLoader;
import com.palmergames.bukkit.towny.object.metadata.MetadataLoader;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.CompletableFuture;
import sh.lue.spawnlevel.command.SpawnLevelCommand;
import sh.lue.spawnlevel.command.AllowSpawnCommand;
import sh.lue.spawnlevel.listener.DeletePlayerListener;
import sh.lue.spawnlevel.listener.TogglePublicListener;
import sh.lue.spawnlevel.listener.SpawnListener;
import sh.lue.spawnlevel.listener.StatusScreenListener;
import sh.lue.spawnlevel.listener.NewTownNationListener;
import sh.lue.spawnlevel.object.AllowSpawnList;
import sh.lue.spawnlevel.object.AllowSpawnListDeserializer;
import sh.lue.spawnlevel.tasks.FixupMetadataTask;

public final class SpawnLevel extends JavaPlugin {
    @Override
    public void onEnable() {
        final var translationLoader = new TranslationLoader(this);
        translationLoader.load();
        TownyAPI.getInstance().addTranslations(this, translationLoader.getTranslations());

        MetadataLoader.getInstance().registerDeserializer(AllowSpawnList.typeID(), new AllowSpawnListDeserializer());

        registerCommands();
        registerListeners();
        scheduleTasks();
    }

    private void registerCommands() {
        final var spawnLevelCommand = new SpawnLevelCommand();
        TownyCommandAddonAPI.addSubCommand(CommandType.TOWN, "spawnlevel", spawnLevelCommand);
        TownyCommandAddonAPI.addSubCommand(CommandType.NATION, "spawnlevel", spawnLevelCommand);
        TownyCommandAddonAPI.addSubCommand(CommandType.TOWNYADMIN_TOWN, "spawnlevel", spawnLevelCommand);
        TownyCommandAddonAPI.addSubCommand(CommandType.TOWNYADMIN_NATION, "spawnlevel", spawnLevelCommand);

        final var allowSpawnCommand = new AllowSpawnCommand();
        TownyCommandAddonAPI.addSubCommand(CommandType.TOWN, "allowspawn", allowSpawnCommand);
        TownyCommandAddonAPI.addSubCommand(CommandType.NATION, "allowspawn", allowSpawnCommand);
        TownyCommandAddonAPI.addSubCommand(CommandType.TOWNYADMIN_TOWN, "allowspawn", allowSpawnCommand);
        TownyCommandAddonAPI.addSubCommand(CommandType.TOWNYADMIN_NATION, "allowspawn", allowSpawnCommand);
    }

    private void registerListeners() {
        final var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new DeletePlayerListener(), this);
        pluginManager.registerEvents(new TogglePublicListener(), this);
        pluginManager.registerEvents(new SpawnListener(), this);
        pluginManager.registerEvents(new StatusScreenListener(), this);
        pluginManager.registerEvents(new NewTownNationListener(), this);
    }

    private void scheduleTasks() {
        CompletableFuture.runAsync(new FixupMetadataTask());
    }
}
