package com.covercorp.kaelaevent.minigame.games.glass.config;

import com.covercorp.kaelaevent.util.ZoneCuboid;
import com.covercorp.kaelaevent.util.simple.Pair;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class GlassConfigHelper {
    private final ConfigurationSection glassSection;

    private static final String GLASS_SECTION = "games.glass_bridge";

    public GlassConfigHelper(FileConfiguration fileConfiguration) {
        this.glassSection = fileConfiguration.getConfigurationSection(GLASS_SECTION);
    }

    public int getMaxTeams() {
        return this.glassSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return this.glassSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return this.glassSection.getLocation("spawn-points.lobby");
    }

    public Location getArenaSpawn() {
        return this.glassSection.getLocation("spawn-points.arena");
    }

    public Set<String> getGlasses() {
        return this.glassSection.getConfigurationSection("bridge").getKeys(false);
    }

    public Pair<Location, Location> getGlassSides(String id) {
        Location left = this.glassSection.getLocation("bridge." + id + ".left");
        Location right = this.glassSection.getLocation("bridge." + id + ".right");
        return new Pair<>(left, right);
    }

    public ZoneCuboid getFinishZone() {
        Location pos1 = this.glassSection.getLocation("finish.1");
        if (pos1 == null)
            return null;
        Location pos2 = this.glassSection.getLocation("finish.2");
        if (pos2 == null)
            return null;
        return new ZoneCuboid(pos1, pos2);
    }
}