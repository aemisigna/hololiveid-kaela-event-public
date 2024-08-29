package com.covercorp.kaelaevent.minigame.games.reflex.config;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

public final class ReflexConfigHelper {
    private final ConfigurationSection reflexSection;

    private final static String REFLEX_SECTION = "games.reflex";

    public ReflexConfigHelper(final FileConfiguration fileConfiguration) {
        reflexSection = fileConfiguration.getConfigurationSection(REFLEX_SECTION);
    }

    public int getMaxTeams() {
        return reflexSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return reflexSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return reflexSection.getLocation("spawn-points.lobby");
    }

    public Location getArenaCenter() {
        return reflexSection.getLocation("center");
    }

    public Location getSpotChair(final String spotId) {
        return reflexSection.getLocation("spots." + spotId + ".chair");
    }

    public Location getSpotButton(final String spotId) {
        return reflexSection.getLocation("spots." + spotId + ".button");
    }

    public Location getSpotScreen(final String spotId) {
        return reflexSection.getLocation("spots." + spotId + ".screen");
    }

    public Set<String> getSpotIds() {
        return reflexSection.getConfigurationSection("spots").getKeys(false);
    }
}