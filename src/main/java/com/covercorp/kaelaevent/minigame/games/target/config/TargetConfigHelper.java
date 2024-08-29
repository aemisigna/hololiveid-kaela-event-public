package com.covercorp.kaelaevent.minigame.games.target.config;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class TargetConfigHelper {
    private final ConfigurationSection targetSection;

    private final static String TARGET_SECTION = "games.target_shooting";

    public TargetConfigHelper(final FileConfiguration fileConfiguration) {
        this.targetSection = fileConfiguration.getConfigurationSection(TARGET_SECTION);
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

    public Location getArenaCenter() {
        return this.targetSection.getLocation("center");
    }

    public List<Location> getTargetLocations() {
        final List<?> genericLocations = targetSection.getList("targets");
        if (genericLocations == null) return List.of();

        final List<Location> locations = new ArrayList<>();
        genericLocations.forEach(genericLocation -> {
            if (genericLocation instanceof Location) {
                locations.add((Location) genericLocation);
            }
        });
        return locations;
    }
}
