package sh.lue.spawnlevel.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.bukkit.towny.object.statusscreens.StatusScreen;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.event.HoverEvent;
import com.palmergames.adventure.text.event.ClickEvent;
import com.palmergames.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import sh.lue.spawnlevel.manager.MetadataManager;

public final class StatusScreenListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onTownStatusScreen(TownStatusScreenEvent event) {
        customizeStatusScreen(event.getStatusScreen(), event.getTown());
    }

    @EventHandler(ignoreCancelled = true)
    public void onNationStatusScreen(NationStatusScreenEvent event) {
        customizeStatusScreen(event.getStatusScreen(), event.getNation());
    }

    private void customizeStatusScreen(StatusScreen statusScreen, Government government) {
        patchSubtitleLine(statusScreen, government);
        addAllowSpawnComponent(statusScreen, government);
    }

    private void patchSubtitleLine(StatusScreen statusScreen, Government government) {
        final String spawnLevel = MetadataManager.getSpawnLevel(government);
        final Component oldSubtitle = statusScreen.getComponentOrNull("subtitle");
        if (spawnLevel == null || oldSubtitle == null) {
            return;
        }

        final CommandSender sender = statusScreen.getCommandSender();
        final String publicText = Colors.strip(Translatable.of("status_public").forLocale(sender));
        final String replacementText = Translatable.of("spawnlevel_status_spawnlevel_" + spawnLevel).forLocale(sender);
        final String newSubtitleStripped = MiniMessage.miniMessage().serialize(
            oldSubtitle
                .replaceText(c -> c.matchLiteral(publicText).replacement(replacementText))
                .replaceText(c -> c.match(" \\.\\]\\|\\[\\. |  +").replacement(""))
        );
        final String newSubtitleFormatted = ChatTools.formatSubTitle(newSubtitleStripped);
        final Component newSubtitle = MiniMessage.miniMessage().deserialize(Colors.translateLegacyCharacters(Colors.translateLegacyHex(newSubtitleFormatted)));

        statusScreen.replaceComponent("subtitle", newSubtitle);
    }

    private void addAllowSpawnComponent(StatusScreen statusScreen, Government government) {
        final var allowSpawnUUIDs = MetadataManager.getAllowedSpawnList(government);
        final List<Resident> allowSpawnResidents = allowSpawnUUIDs == null || allowSpawnUUIDs.isEmpty() ?
            Collections.emptyList() :
            allowSpawnUUIDs.stream()
                .map(uuid -> TownyAPI.getInstance().getResident(uuid))
                .filter(res -> res != null)
                .collect(Collectors.toList());

        if (allowSpawnResidents.isEmpty()) {
            return;
        }

        final Translator translator = Translator.locale(statusScreen.getCommandSender());
        List<String> formattedResidents = TownyFormatter.getFormattedNames(allowSpawnResidents);
        if (formattedResidents.size() > 34) {
            TownyFormatter.shortenOverLengthList(formattedResidents, 35, translator);
        }

        final String govType = government instanceof Town ? "town" : "nation";
        final String formattedResidentList = TownyFormatter.getFormattedStrings(translator.of("spawnlevel_allowspawn_list_short"), formattedResidents, allowSpawnResidents.size());
        final Component formattedResidentListComponent = MiniMessage.miniMessage().deserialize(Colors.translateLegacyCharacters(Colors.translateLegacyHex(formattedResidentList)));

        statusScreen.addComponentOf(
            "spawnleveloverrides",
            TownyFormatter.colourHoverKey(translator.of("spawnlevel_allowspawn_list")),
            HoverEvent.showText(formattedResidentListComponent
                .append(Component.newline())
                .append(translator.component("status_hover_click_for_more"))),
            ClickEvent.runCommand("/towny:" + govType + " allowspawn list " + government.getName())
        );
    }
}
