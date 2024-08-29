package com.covercorp.kaelaevent.minigame.games.zombie.config;

import com.covercorp.kaelaevent.util.ZoneCuboid;
import com.covercorp.kaelaevent.util.simple.Pair;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

public final class ZombieConfigHelper {
    private final ConfigurationSection targetSection;

    private final static String ZOMBIE_SECTION = "games.zombie";

    public ZombieConfigHelper(final FileConfiguration fileConfiguration) {
        this.targetSection = fileConfiguration.getConfigurationSection(ZOMBIE_SECTION);
    }

    public int getMaxTeams() {
        return this.targetSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return this.targetSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return this.targetSection.getLocation("spawn-points.lobby");
    }

    public Location getArenaSpawn() {
        return this.targetSection.getLocation("spawn-points.arena");
    }

    public ZoneCuboid getShootZone() {
        final Location pos1 = targetSection.getLocation("shoot-zone.pos1");
        final Location pos2 = targetSection.getLocation("shoot-zone.pos2");

        return new ZoneCuboid(pos1, pos2);
    }

    public Set<String> getZombieSpawns() {
        return this.targetSection.getConfigurationSection("spawn-points.zombies").getKeys(false);
    }

    public Pair<Location, Location> getZombieSpawn(final String id) {
        final Location point1 = targetSection.getLocation("spawn-points.zombies." + id + ".point1");
        final Location point2 = targetSection.getLocation("spawn-points.zombies." + id + ".point2");

        return new Pair<>(point1, point2);
    }
}
