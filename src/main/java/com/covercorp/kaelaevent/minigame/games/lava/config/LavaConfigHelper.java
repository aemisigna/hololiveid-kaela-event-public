package com.covercorp.kaelaevent.minigame.games.lava.config;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class LavaConfigHelper {
    private final ConfigurationSection lavaSection;

    private final static String LAVA_SECTION = "games.lava";

    public LavaConfigHelper(final FileConfiguration fileConfiguration) {
        this.lavaSection = fileConfiguration.getConfigurationSection(LAVA_SECTION);
    }

    public int getMaxTeams() {
        return lavaSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return lavaSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return lavaSection.getLocation("spawn-points.lobby");
    }

    public Location getArenaSpawn() {
        return lavaSection.getLocation("spawn-points.arena");
    }

    public Set<String> getSlots() {
        return lavaSection.getConfigurationSection("slots").getKeys(false);
    }

    public Location getSlot(final String standIdentifier) {
        return lavaSection.getLocation("slots." + standIdentifier + ".location");
    }
}
