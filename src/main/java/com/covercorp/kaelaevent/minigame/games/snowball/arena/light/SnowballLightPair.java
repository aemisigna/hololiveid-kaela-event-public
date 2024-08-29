package com.covercorp.kaelaevent.minigame.games.snowball.arena.light;

import com.covercorp.kaelaevent.minigame.games.snowball.arena.SnowballArena;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.event.SnowballLightStealEvent;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.light.light.SnowballLight;
import com.covercorp.kaelaevent.minigame.games.snowball.arena.light.light.side.LightSide;
import com.covercorp.kaelaevent.util.simple.Pair;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SnowballLightPair {
    @Getter(AccessLevel.PUBLIC) private final UUID uniqueId;

    @Getter(AccessLevel.PUBLIC) private final SnowballArena snowballArena;

    private final Pair<SnowballLight, SnowballLight> snowballLightPair; // Left / Right

    public SnowballLightPair(final UUID uniqueId, final SnowballArena snowballArena, final Location left, final Location right) {
        this.uniqueId = uniqueId;
        this.snowballArena = snowballArena;

        snowballLightPair = new Pair<>(
                new SnowballLight(this, left),
                new SnowballLight(this, right)
        );
    }

    public List<SnowballLight> getLights() {
        final List<SnowballLight> lights = new ArrayList<>();
        lights.add(snowballLightPair.key());
        lights.add(snowballLightPair.value());

        return lights;
    }

    public LightSide getTurnedOnLightSide() {
        if (snowballLightPair.key().isLit()) {
            return LightSide.LEFT;
        }
        if (snowballLightPair.value().isLit()) {
            return LightSide.RIGHT;
        }
        return null;
    }

    public void lightUpLight(final LightSide lightSide) {
        switch (lightSide) {
            case LEFT -> {
                final SnowballLight left = snowballLightPair.key();
                left.light(true);
                final SnowballLight right = snowballLightPair.value();
                right.light(false);

                Bukkit.getPluginManager().callEvent(new SnowballLightStealEvent(snowballArena, LightSide.LEFT));
            }
            case RIGHT -> {
                final SnowballLight left = snowballLightPair.key();
                left.light(false);
                final SnowballLight right = snowballLightPair.value();
                right.light(true);

                Bukkit.getPluginManager().callEvent(new SnowballLightStealEvent(snowballArena, LightSide.RIGHT));
            }
        }
    }
}
