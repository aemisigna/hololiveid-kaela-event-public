package com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.part;

import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.ReflexSpot;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.part.status.ButtonStatus;
import com.covercorp.kaelaevent.minigame.games.reflex.inventory.ReflexItemCollection;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;

@Getter(AccessLevel.PUBLIC)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ReflexButton {
    private final ReflexSpot reflexSpot;
    private final Location location;

    private ItemDisplay display;
    private Interaction hitbox;

    public void spawn() {
        deSpawn();

        final Interaction hitbox = (Interaction) location.getWorld().spawnEntity(location, EntityType.INTERACTION);
        NBTMetadataUtil.addStringToEntity(hitbox, "reflex_button", reflexSpot.getUniqueId().toString());

        hitbox.setInteractionHeight(0.8F);
        hitbox.setInteractionWidth(0.6F);

        this.hitbox = hitbox;

        final ItemDisplay display = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        display.setItemStack(ReflexItemCollection.REFLEX_BUTTON_UNPRESSED);
        NBTMetadataUtil.addStringToEntity(display, "reflex_button", reflexSpot.getUniqueId().toString());

        final Transformation transformation = display.getTransformation();

        transformation.getScale().set(1.0, 1.0, 1.0);
        display.setTransformation(transformation);

        this.display = display;
    }

    public void deSpawn() {
        if (!isAlive()) return;

        hitbox.remove();
        display.remove();
    }

    public ButtonStatus getStatus() {
        if (display.getItemStack() == ReflexItemCollection.REFLEX_BUTTON_PRESSED) return ButtonStatus.PRESSED;

        return ButtonStatus.UNPRESSED;
    }

    public void setStatus(final ButtonStatus buttonStatus) {
        if (buttonStatus == ButtonStatus.UNPRESSED) display.setItemStack(ReflexItemCollection.REFLEX_BUTTON_UNPRESSED);
        if (buttonStatus == ButtonStatus.PRESSED) display.setItemStack(ReflexItemCollection.REFLEX_BUTTON_PRESSED);
    }

    public boolean isAlive() {
        return display != null && !display.isDead() && hitbox != null && !hitbox.isDead();
    }
}
