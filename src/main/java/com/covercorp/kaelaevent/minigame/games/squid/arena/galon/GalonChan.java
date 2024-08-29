package com.covercorp.kaelaevent.minigame.games.squid.arena.galon;

import com.covercorp.kaelaevent.minigame.games.squid.arena.SquidArena;
import com.covercorp.kaelaevent.minigame.games.squid.arena.galon.status.GalonChanStatus;
import com.covercorp.kaelaevent.minigame.games.squid.inventory.SquidItemCollection;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.time.Duration;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class GalonChan {
    private final SquidArena arena;
    private final Location location;

    private GalonChanStatus status;
    private ItemDisplay display;

    public GalonChan(SquidArena arena) {
        this.arena = arena;
        this.location = arena.getGalonChanLocation();
        this.status = GalonChanStatus.YES;
    }

    public void spawn() {
        deSpawn();

        display = (ItemDisplay) location.getWorld().spawnEntity(this.location, EntityType.ITEM_DISPLAY);
        display.setItemStack(SquidItemCollection.GALON_CHAN_YES);
        NBTMetadataUtil.addStringToEntity(display, "galon_chan", status.name());

        final Transformation transformation = display.getTransformation();
        display.setTransformation(new Transformation(new Vector3f(-2.0F, 11.0F, 0.0F), transformation.getLeftRotation(), new Vector3f(12.0F, 12.0F, 12.0F), transformation.getRightRotation()));
        display.setRotation(-90.0F, 0.0F);

        display.setViewRange(5);
    }

    public void deSpawn() {
        if (display != null && !display.isDead()) display.remove();
    }

    public void setStatus(GalonChanStatus status) {
        this.status = status;
        switch (status) {
            case NO -> {
                display.setItemStack(SquidItemCollection.GALON_CHAN_NO);
                arena.getAnnouncer().sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 2.0F, 0.5F);
                arena.getAnnouncer().sendGlobalTitle(Title.title(
                        arena.getGameMiniMessage().deserialize(""),
                        Component.empty(),
                        Title.Times.times(Duration.ZERO, Duration.ofMinutes(5L), Duration.ofMillis(400L))));
            }
            case YES -> {
                display.setItemStack(SquidItemCollection.GALON_CHAN_YES);
                arena.getAnnouncer().sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 2.0F, 1.5F);
                arena.getAnnouncer().sendGlobalTitle(Title.title(
                            arena.getGameMiniMessage().deserialize(""),
                            Component.empty(),
                            Title.Times.times(Duration.ZERO, Duration.ofMinutes(5L), Duration.ofMillis(400L))
                        )
                );
            }
        }
    }
}
