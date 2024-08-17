package sh.lue.spawnlevel.listener;

import com.palmergames.bukkit.towny.event.TranslationLoadEvent;
import com.palmergames.bukkit.towny.object.TranslationLoader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import sh.lue.spawnlevel.SpawnLevel;

public final class TranslationLoadListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onTranslationLoad(TranslationLoadEvent event) {
        final var translationLoader = new TranslationLoader(SpawnLevel.getPlugin());
        translationLoader.load();

        final var translations = translationLoader.getTranslations();
        for (final String language : translations.keySet()) {
            for (final var map : translations.get(language).entrySet()) {
                event.addTranslation(language, map.getKey(), map.getValue());
            }
        }
    }
}
