package com.covercorp.kaelaevent.minigame.games.platformrush.config;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class PlatformRushConfigHelper {
    private final ConfigurationSection platformRushSection;

    private final static String PLATFORM_RUSH_SECTION = "games.platform_rush";

    public PlatformRushConfigHelper(final FileConfiguration fileConfiguration) {
        this.platformRushSection = fileConfiguration.getConfigurationSection(PLATFORM_RUSH_SECTION);
    }

    public int getMaxTeams() {
        return platformRushSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return platformRushSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return platformRushSection.getLocation("spawn-points.lobby");
    }

    public Location getArenaSpawn() {
        return platformRushSection.getLocation("spawn-points.game");
    }
}
