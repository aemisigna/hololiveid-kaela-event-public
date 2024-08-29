package com.covercorp.kaelaevent.minigame.games.colorgacha.config;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

public final class ColorGachaConfigHelper {
    private final ConfigurationSection colorGachaSection;

    private final static String COLOR_GACHA_SECTION = "games.color_gacha";

    public ColorGachaConfigHelper(final FileConfiguration fileConfiguration) {
        this.colorGachaSection = fileConfiguration.getConfigurationSection(COLOR_GACHA_SECTION);
    }

    public int getMaxTeams() {
        return colorGachaSection.getInt("maxTeams");
    }

    public int getMaxPlayersPerTeam() {
        return colorGachaSection.getInt("maxPlayersPerTeam");
    }

    public Location getLobbySpawn() {
        return colorGachaSection.getLocation("spawn-points.lobby");
    }

    public Location getScenarioPressingSpawn() {
        return colorGachaSection.getLocation("spawn-points.scenario-pressing");
    }

    public Location getScenarioWaitingSpawn() {
        return colorGachaSection.getLocation("spawn-points.scenario-waiting");
    }

    public Set<String> getStands() {
        return colorGachaSection.getConfigurationSection("buttons").getKeys(false);
    }

    public String getStandColor(final String standIdentifier) {
        return colorGachaSection.getString("buttons." + standIdentifier + ".color");
    }

    public Location getStandLocation(final String standIdentifier) {
        return colorGachaSection.getLocation("buttons." + standIdentifier + ".location");
    }
}
