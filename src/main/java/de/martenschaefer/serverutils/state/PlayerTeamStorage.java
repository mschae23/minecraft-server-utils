package de.martenschaefer.serverutils.state;

import java.util.HashMap;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;

public class PlayerTeamStorage {
    private final HashMap<String, Entry> entries;

    public PlayerTeamStorage() {
        this.entries = new HashMap<>();
    }

    public HashMap<String, Entry> getEntries() {
        return this.entries;
    }

    private Team createTeam(ServerPlayerEntity player, Formatting formatting) {
        Team team = new Team(player.getWorld().getScoreboard(), ServerUtilsMod.MODID + "_player_" + player.getEntityName());
        team.getPlayerList().add(player.getEntityName());
        team.setColor(formatting);
        return team;
    }

    public void onPlayerConnect(ServerPlayerEntity player) {
        Formatting formatting = ModUtils.getUsernameFormatting(player);
        Team team = this.createTeam(player, formatting);
        Entry entry = new Entry(team, formatting);
        this.entries.put(player.getEntityName(), entry);

        for (ServerPlayerEntity other : player.getServerWorld().getPlayers()) {
            Entry otherEntry = this.entries.get(other.getEntityName());

            if (otherEntry != null) {
                player.networkHandler.sendPacket(TeamS2CPacket.updateTeam(otherEntry.team, true));
            }
        }

        for (ServerPlayerEntity other : player.getServerWorld().getPlayers()) {
            if (other == player) {
                continue;
            }

            other.networkHandler.sendPacket(TeamS2CPacket.updateTeam(entry.team, true));
        }
    }

    public void updateFormatting(ServerPlayerEntity player, Formatting formatting) {
        Entry entry = this.entries.get(player.getEntityName());

        if (entry == null) {
            Team team = this.createTeam(player, formatting);
            entry = new Entry(team, formatting);
            this.entries.put(player.getEntityName(), entry);
        } else if (entry.formatting == formatting) {
            return;
        } else {
            entry.formatting = formatting;
            entry.team.setColor(formatting);
        }

        for (ServerPlayerEntity other : player.getServerWorld().getPlayers()) {
            other.networkHandler.sendPacket(TeamS2CPacket.updateTeam(entry.team, false));
        }
    }

    public void onPlayerDisconnect(ServerPlayerEntity player) {
        Entry entry = this.entries.get(player.getEntityName());

        if (entry == null) {
            return;
        }

        player.server.getPlayerManager().sendToAll(TeamS2CPacket.updateRemovedTeam(entry.team));
    }

    public static class Entry {
        private final Team team;
        private Formatting formatting;

        public Entry(Team team, Formatting formatting) {
            this.team = team;
            this.formatting = formatting;
        }

        public Team getTeam() {
            return this.team;
        }

        public Formatting getFormatting() {
            return this.formatting;
        }

        public void setFormatting(Formatting formatting) {
            this.formatting = formatting;
        }
    }
}
