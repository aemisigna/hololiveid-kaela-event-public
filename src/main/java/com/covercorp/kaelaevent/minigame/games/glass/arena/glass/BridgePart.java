package com.covercorp.kaelaevent.minigame.games.glass.arena.glass;

import com.covercorp.kaelaevent.minigame.games.glass.arena.glass.side.GlassSide;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;

@Getter(AccessLevel.PUBLIC)
public final class BridgePart {
    private final String id;
    private final List<GlassSide> glasses;

    public BridgePart(final String id, final Location left, final Location right) {
        this.id = id;
        this.glasses = new ArrayList<>();

        glasses.add(new GlassSide(this, left));
        glasses.add(new GlassSide(this, right));
    }

    public void spawnGlass() {
        this.glasses.forEach(glass -> {
            glass.setEvil(false);
            glass.spawn();
        });
        GlassSide glassSide = glasses.get(new Random().nextInt(glasses.size()));
        if (glassSide != null) {
            glassSide.setEvil(true);
        }
    }

    public void resetGlass() {
        this.glasses.forEach(glass -> {
            glass.deSpawn();
            glass.setEvil(false);
            glass.spawn();
        });

        final GlassSide glassSide = glasses.get(new Random().nextInt(glasses.size()));
        if (glassSide != null) {
            glassSide.setEvil(true);
        }
    }

    public void breakGlass(GlassSide glassSide) {
        if (glassSide != null) {
            if (this.glasses.contains(glassSide)) {
                if (glassSide.isAlive()) {
                    glassSide.breakGlass(true);
                }
            }
        }
    }
}