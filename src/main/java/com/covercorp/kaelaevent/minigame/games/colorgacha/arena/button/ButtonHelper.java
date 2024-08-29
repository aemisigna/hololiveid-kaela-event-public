package com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button;

import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.ColorGachaArena;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.ColorGachaButton;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.state.ButtonStatus;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ButtonHelper {
    public final ColorGachaArena arena;

    private Map<String, ColorGachaButton> buttons;

    public ButtonHelper(final ColorGachaArena arena) {
        this.arena = arena;

        buttons = new ConcurrentHashMap<>();
    }

    public void addButton(final @NotNull ColorGachaButton button) {
        button.setStatus(ButtonStatus.UNPRESSED);

        buttons.put(button.getIdentifier(), button);

        arena.getColorGachaMiniGame().getKaelaEvent().getLogger().info("Added button: " + button.getIdentifier());
    }

    public ColorGachaButton getButton(final String standIdentifier) {
        return buttons.get(standIdentifier);
    }

    public List<ColorGachaButton> getButtons() {
        return new ArrayList<>(buttons.values());
    }

    public void removeButton(final String standIdentifier) {
        final ColorGachaButton button = getButton(standIdentifier);
        if (button == null) {
            arena.getColorGachaMiniGame().getKaelaEvent().getLogger().info("Could not remove button: " + standIdentifier);
            return;
        }

        final ArmorStand armorStand = button.getArmorStand();
        if (armorStand != null && !armorStand.isDead()) armorStand.remove();

        buttons.remove(standIdentifier);

        arena.getColorGachaMiniGame().getKaelaEvent().getLogger().info("Removed button: " + standIdentifier);
    }
}
