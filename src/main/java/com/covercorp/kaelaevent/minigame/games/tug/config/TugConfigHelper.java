package com.covercorp.kaelaevent.minigame.games.tug.config;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class TugConfigHelper {
    private final ConfigurationSection tugSection;

    private final static String TUG_SECTION = "games.tug";

    public TugConfigHelper(final FileConfiguration fileConfiguration) {
        this.tugSection = fileConfiguration.getConfigurationSection(TUG_SECTION);
    }

    public int getMaxTeams() {
        return tugSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return tugSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return tugSection.getLocation("spawn-points.lobby");
    }

    public Location getCenter() {
        return tugSection.getLocation("center");
    }

    public List<Location> getTeamSpawns() {
        final List<Location> teamSpawns = new ArrayList<>();
        final List<?> teamSpawnList = tugSection.getList("game-spawns");

        if (teamSpawnList != null) {
            for (Object obj : teamSpawnList) {
                if (obj instanceof Location) {
                    teamSpawns.add((Location) obj);
                }
            }
        }

        return teamSpawns;
    }
}
