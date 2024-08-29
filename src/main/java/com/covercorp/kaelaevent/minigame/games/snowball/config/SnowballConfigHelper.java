package com.covercorp.kaelaevent.minigame.games.snowball.config;

import com.covercorp.kaelaevent.util.simple.Pair;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SnowballConfigHelper {
    private final ConfigurationSection snowballSection;

    private final static String SNOWBALL_SECTION = "games.snowball_race";

    public SnowballConfigHelper(final FileConfiguration fileConfiguration) {
        snowballSection = fileConfiguration.getConfigurationSection(SNOWBALL_SECTION);
    }

    public int getMaxTeams() {
        return snowballSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return snowballSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return snowballSection.getLocation("spawn-points.lobby");
    }

    public List<Location> getArenaSpawns() {
        final List<?> genericLocations = snowballSection.getList("spawn-points.arena");
        if (genericLocations == null) return List.of();

        final List<Location> locations = new ArrayList<>();
        genericLocations.forEach(genericLocation -> {
            if (genericLocation instanceof Location) {
                locations.add((Location) genericLocation);
            }
        });
        return locations;
    }

    public List<Location> getIceMachineSpawns() {
        final List<?> genericLocations = snowballSection.getList("ice-machines");
        if (genericLocations == null) return List.of();

        final List<Location> locations = new ArrayList<>();
        genericLocations.forEach(genericLocation -> {
            if (genericLocation instanceof Location) {
                locations.add((Location) genericLocation);
            }
        });
        return locations;
    }

    public List<Location> getScoreMachineSpawns() {
        final List<?> genericLocations = snowballSection.getList("score-machines");
        if (genericLocations == null) return List.of();

        final List<Location> locations = new ArrayList<>();
        genericLocations.forEach(genericLocation -> {
            if (genericLocation instanceof Location) {
                locations.add((Location) genericLocation);
            }
        });
        return locations;
    }

    public Location getArenaCenter() {
        return snowballSection.getLocation("center");
    }

    public Set<String> getLights() {
        return snowballSection.getConfigurationSection("lights").getKeys(false);
    }

    public Pair<Location, Location> getLightPairs(final String id) {
        final Location left = snowballSection.getLocation("lights." + id + ".left");
        final Location right = snowballSection.getLocation("lights." + id + ".right");

        return new Pair<>(left, right);
    }
}
