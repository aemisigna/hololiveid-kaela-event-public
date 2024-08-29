package com.covercorp.kaelaevent.minigame.games.basketball.config;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class BasketballConfigHelper {
    private final ConfigurationSection basketballSection;

    private final static String BASKETBALL_SECTION = "games.basketball";

    public BasketballConfigHelper(final FileConfiguration fileConfiguration) {
        this.basketballSection = fileConfiguration.getConfigurationSection(BASKETBALL_SECTION);
    }

    public int getMaxTeams() {
        return basketballSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return basketballSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return basketballSection.getLocation("spawn-points.lobby");
    }

    public Location getArenaSpawn() {
        return basketballSection.getLocation("spawn-points.game");
    }

    public Location getBasketHitboxLocation() {
        return basketballSection.getLocation("hitbox");
    }

    public List<Location> getBallSpawns() {
        final List<Location> ballSpawns = new ArrayList<>();
        final List<?> ballSpawnList = basketballSection.getList("ball-spawn-points");

        if (ballSpawnList != null) {
            for (Object obj : ballSpawnList) {
                if (obj instanceof Location) {
                    ballSpawns.add((Location) obj);
                }
            }
        }

        return ballSpawns;
    }
}
