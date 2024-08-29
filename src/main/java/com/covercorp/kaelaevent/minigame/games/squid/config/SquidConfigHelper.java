package com.covercorp.kaelaevent.minigame.games.squid.config;

import com.covercorp.kaelaevent.util.ZoneCuboid;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class SquidConfigHelper {
    private final ConfigurationSection squidSection;

    private final static String SQUID_SECTION = "games.squid_game";

    public SquidConfigHelper(final FileConfiguration fileConfiguration) {
        this.squidSection = fileConfiguration.getConfigurationSection(SQUID_SECTION);
    }

    public int getMaxTeams() {
        return squidSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return squidSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return squidSection.getLocation("spawn-points.lobby");
    }

    public List<Location> getArenaSpawns() {
        final List<?> genLocs = this.squidSection.getList("spawn-points.arena");
        if (genLocs == null) return List.of();

        final List<Location> locations = new ArrayList<>();
        for (Object genLoc : genLocs) {
            if (genLoc instanceof Location)
                locations.add((Location)genLoc);
        }
        return locations;
    }

    public List<Location> getGunSpawns() {
        final List<?> genLocs = this.squidSection.getList("spawn-points.guns");
        if (genLocs == null) return List.of();

        final List<Location> locations = new ArrayList<>();
        for (Object genLoc : genLocs) {
            if (genLoc instanceof Location)
                locations.add((Location)genLoc);
        }
        return locations;
    }

    public Location getGalonChanSpawn() {
        return this.squidSection.getLocation("galon-chan");
    }

    public ZoneCuboid getStartZone() {
        Location pos1 = this.squidSection.getLocation("start.pos1");
        if (pos1 == null) {
            return null;
        } else {
            Location pos2 = this.squidSection.getLocation("start.pos2");
            return pos2 == null ? null : new ZoneCuboid(pos1, pos2);
        }
    }

    public ZoneCuboid getGoalZone() {
        Location pos1 = this.squidSection.getLocation("finish.pos1");
        if (pos1 == null) {
            return null;
        } else {
            Location pos2 = this.squidSection.getLocation("finish.pos2");
            return pos2 == null ? null : new ZoneCuboid(pos1, pos2);
        }
    }
}
