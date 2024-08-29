package com.covercorp.kaelaevent.minigame.games.reflex.arena.spot;

import com.covercorp.kaelaevent.minigame.games.reflex.arena.ReflexArena;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.part.ReflexButton;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.part.ReflexChair;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.part.ReflexScreen;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.part.status.ButtonStatus;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.part.status.ScreenStatus;
import com.covercorp.kaelaevent.minigame.games.reflex.player.ReflexPlayer;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter(AccessLevel.PUBLIC)
public final class ReflexSpot {
    private final UUID uniqueId;

    private final ReflexChair chair;
    private final ReflexButton button;
    private final ReflexScreen screen;

    public ReflexSpot(final ReflexArena arena, final String spotId) {
        this.uniqueId = UUID.randomUUID();

        chair = new ReflexChair(this, arena.getReflexMiniGame().getConfigHelper().getSpotChair(spotId));
        button = new ReflexButton(this, arena.getReflexMiniGame().getConfigHelper().getSpotButton(spotId));
        screen = new ReflexScreen(this, arena.getReflexMiniGame().getConfigHelper().getSpotScreen(spotId));
    }

    public void spawnParts() {
        chair.spawn();
        screen.spawn();
        button.spawn();
    }

    public void deSpawnParts() {
        chair.deSpawn();
        screen.deSpawn();
        button.deSpawn();
    }

    public void setButtonStatus(final ButtonStatus status) {
        if (button == null) return;

        button.setStatus(status);
    }

    public void setScreenStatus(final ScreenStatus status) {
        if (screen == null) return;

        screen.setStatus(status);
    }

    public void sitPlayer(final ReflexPlayer reflexPlayer) {
        final Player player = Bukkit.getPlayer(reflexPlayer.getUniqueId());
        if (player == null) return;

        if (chair.getDisplay() == null) return;
        final ItemDisplay entity = chair.getDisplay();
        entity.addPassenger(player);
    }

    public void unSitPlayer(final ReflexPlayer reflexPlayer) {
        final Player player = Bukkit.getPlayer(reflexPlayer.getUniqueId());
        if (player == null) return;

        if (chair.getDisplay() == null) return;
        final ItemDisplay entity = chair.getDisplay();

        entity.removePassenger(player);
    }

    public Location getSpotSpawn() {
        return chair.getLocation();
    }
}
