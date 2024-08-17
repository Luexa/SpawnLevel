package sh.lue.spawnlevel.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.invites.Invite;
import com.palmergames.bukkit.towny.event.town.TownPreInvitePlayerEvent;
import com.palmergames.bukkit.towny.event.TownPreAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.nation.NationPreInviteTownEvent;
import com.palmergames.bukkit.towny.event.NationPreAddTownEvent;
import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.towny.event.town.TownOutlawAddEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.ArrayList;

public final class NationOutlawEnforcementListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onTownPreInvitePlayer(TownPreInvitePlayerEvent event) {
        final Town town = event.getTown();
        final Nation nation = town.getNationOrNull();
        final Town nationCapital = nation == null ? null : nation.getCapital();

        if (nationCapital != null && nationCapital.hasOutlaw(event.getInvitedResident())) {
            event.setCancelMessage("You cannot invite this player to your town as your nation has outlawed them.");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTownPreAddResident(TownPreAddResidentEvent event) {
        final Town town = event.getTown();
        final Nation nation = town.getNationOrNull();
        final Town nationCapital = nation == null ? null : nation.getCapital();

        if (nationCapital != null && nationCapital.hasOutlaw(event.getResident())) {
            event.setCancelMessage("You cannot join this town as its nation has outlawed you.");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTownAddResident(TownAddResidentEvent event) {
        final Town town = event.getTown();
        if (town.getNationOrNull() != null) {
            return;
        }

        final Resident resident = event.getResident();
        for (Invite invite : new ArrayList<>(town.getReceivedInvites())) {
            if (invite.getSender() instanceof Nation nation) {
                final Town nationCapital = nation.getCapital();
                if (nationCapital != null && nationCapital.hasOutlaw(resident)) {
                    invite.decline(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onNationPreInviteTown(NationPreInviteTownEvent event) {
        final Nation nation = event.getNation();
        final Town town = event.getInvitedTown();
        final Town nationCapital = nation.getCapital();

        if (nationCapital != null) {
            for (Resident resident : town.getResidents()) {
                if (nationCapital.hasOutlaw(resident)) {
                    event.setCancelMessage("You cannot invite this town as it is harboring the nation outlaw " + resident.getName() + ".");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onNationPreAddTown(NationPreAddTownEvent event) {
        final Nation nation = event.getNation();
        final Town town = event.getTown();
        final Town nationCapital = nation.getCapital();

        if (nationCapital != null) {
            for (Resident resident : town.getResidents()) {
                if (nationCapital.hasOutlaw(resident)) {
                    event.setCancelMessage("You cannot join this nation as your town is harboring the nation outlaw " + resident.getName() + ".");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onNationAddTown(NationAddTownEvent event) {
        final Nation nation = event.getNation();
        final Town town = event.getTown();
        final Town nationCapital = nation.getCapital();

        if (nationCapital != null) {
            for (Invite invite : new ArrayList<>(town.getSentInvites())) {
                if (invite.getReceiver() instanceof Resident resident) {
                    if (nationCapital.hasOutlaw(resident)) {
                        invite.decline(true);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTownAddOutlaw(TownOutlawAddEvent event) {
        final Town town = event.getTown();
        final Resident outlaw = event.getOutlawedResident();

        if (town.isCapital()) {
            for (Invite invite : new ArrayList<>(town.getNationOrNull().getSentInvites())) {
                if (invite.getReceiver() instanceof Town invitedTown) {
                    if (invitedTown.hasResident(outlaw)) {
                        invite.decline(true);
                    }
                }
            }
        }
    }
}
