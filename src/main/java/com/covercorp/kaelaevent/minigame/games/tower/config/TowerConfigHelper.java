package com.covercorp.kaelaevent.minigame.games.tower.config;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

public final class TowerConfigHelper {
    private final ConfigurationSection towerSection;
    
    private final static String TOWER_SECTION = "games.tower";

    public TowerConfigHelper(final FileConfiguration fileConfiguration) {
        towerSection = fileConfiguration.getConfigurationSection(TOWER_SECTION);
    }

    public int getMaxTeams() {
        return towerSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return towerSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return towerSection.getLocation("spawn-points.lobby");
    }

    public Location getArenaCenter() {
        return towerSection.getLocation("center");
    }

    public Location getSpotSpawnLocation(final String spotId) {
        return towerSection.getLocation("spots." + spotId + ".spawn");
    }

    public Location getSpotGambleMachine(final String spotId) {
        return towerSection.getLocation("spots." + spotId + ".gamble");
    }

    public Set<String> getSpotIds() {
        return towerSection.getConfigurationSection("spots").getKeys(false);
    }
}