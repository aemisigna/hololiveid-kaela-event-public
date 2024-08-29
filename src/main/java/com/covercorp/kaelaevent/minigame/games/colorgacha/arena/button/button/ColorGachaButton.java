package com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button;

import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.state.ButtonColor;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.state.ButtonStatus;
import com.covercorp.kaelaevent.minigame.games.colorgacha.inventory.ColorGachaItemCollection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
public final class ColorGachaButton {
    private final String identifier;

    private final Location location;
    private final ArmorStand armorStand;

    private final ButtonColor color;

    private ButtonStatus status;

    public void setStatus(final ButtonStatus status) {
        this.status = status;

        if (armorStand == null) return;
        if (armorStand.isDead()) return;

        armorStand.getEquipment().setHelmet(ColorGachaItemCollection.getStand(color, status));
    }
}
