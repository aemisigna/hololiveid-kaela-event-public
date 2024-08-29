package com.covercorp.kaelaevent.minigame.games.trident.config;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class TridentConfigHelper {
    private final ConfigurationSection tridentSection;

    private final static String TRIDENT_SECTION = "games.trident_race";

    public TridentConfigHelper(final FileConfiguration fileConfiguration) {
        tridentSection = fileConfiguration.getConfigurationSection(TRIDENT_SECTION);
    }

    public int getMaxTeams() {
        return tridentSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return tridentSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return tridentSection.getLocation("spawn-points.lobby");
    }

    public Location getArenaSpawn() {
        return tridentSection.getLocation("spawn-points.arena");
    }

    public Location getArenaCenter() {
        return tridentSection.getLocation("center");
    }

    public List<Location> getTridentTargetLocations() {
        final List<?> genericLocations = tridentSection.getList("targets");
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
