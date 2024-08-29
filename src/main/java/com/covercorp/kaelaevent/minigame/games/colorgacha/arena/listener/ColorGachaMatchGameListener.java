package com.covercorp.kaelaevent.minigame.games.colorgacha.arena.listener;

import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.ColorGachaArena;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.ColorGachaButton;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.state.ButtonStatus;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.event.ColorGachaButtonPressEvent;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.event.ColorGachaDisqualificationEvent;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.state.ColorGachaMatchState;
import com.covercorp.kaelaevent.minigame.games.colorgacha.player.ColorGachaPlayer;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import com.covercorp.kaelaevent.util.PlayerUtils;
import com.covercorp.kaelaevent.util.simple.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Optional;

public final class ColorGachaMatchGameListener implements Listener {
    private final ColorGachaArena arena;

    public ColorGachaMatchGameListener(final ColorGachaArena arena) {
        this.arena = arena;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorStandClick(final PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof final ArmorStand armorStand)) return;

        if (!NBTMetadataUtil.hasEntityString(armorStand, "stand_accessor")) return;

        if (arena.getState() != ColorGachaMatchState.GAME) return;

        final Player player = event.getPlayer();
        final Optional<ColorGachaPlayer> gamePlayerOptional = arena.getPlayerHelper().getPlayer(player.getUniqueId())
                .map(mappedPlayer -> (ColorGachaPlayer) mappedPlayer);
        if (gamePlayerOptional.isEmpty()) return;

        final ColorGachaButton button = arena.getButtonHelper().getButton(NBTMetadataUtil.getEntityString(armorStand, "stand_accessor"));
        if (button == null) return;

        if (arena.isPressingButton()) return;

        arena.getColorGachaMiniGame().getKaelaEvent().getServer().getPluginManager().callEvent(new ColorGachaButtonPressEvent(arena, gamePlayerOptional.get(), button));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onButtonClick(final ColorGachaButtonPressEvent event) {
        final ColorGachaPlayer gamePlayer = event.getTalent();
        final ColorGachaButton button = event.getButton();

        if (arena.getState() != ColorGachaMatchState.GAME) return;

        if (!arena.getCurrentPlayer().equals(gamePlayer)) return;

        final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
        if (player == null) return;

        PlayerUtils.forceSwingAnimation(player);

        if (button.getStatus() == ButtonStatus.PRESSED) {
            player.sendMessage(StringUtils.translate("&cThis button is already pressed down!"));
            return;
        }

        arena.setPressingButton(true);
        button.setStatus(ButtonStatus.PRESSED);

        arena.getAnnouncer().sendGlobalSound(Sound.BLOCK_PISTON_EXTEND, 1.5F, 1.5F);

        Bukkit.getScheduler().runTaskLater(arena.getColorGachaMiniGame().getKaelaEvent(), () -> {
            arena.getAnnouncer().sendGlobalSound(Sound.ENTITY_IRON_GOLEM_HURT, 0.5F, 0.5F);
            arena.changeKaelaFace(ColorGachaArena.KAELA_MODEL_THINKING);
            arena.changeScreen(ColorGachaArena.SCREEN_MODEL_NORMAL);

            Bukkit.getScheduler().runTaskLater(arena.getColorGachaMiniGame().getKaelaEvent(), () -> {
                if (arena.getState() != ColorGachaMatchState.GAME) return;

                // Check if the button is bad
                if (arena.getBadButton().getIdentifier().equals(button.getIdentifier())) {
                    arena.changeKaelaFace(ColorGachaArena.KAELA_MODEL_MAD);
                    arena.changeScreen(ColorGachaArena.SCREEN_MODEL_INCORRECT);

                    button.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getEyeLocation(), 1, 0.1, 0.1, 0.1, 0.1);
                    arena.getButtonHelper().removeButton(button.getIdentifier());

                    arena.getColorGachaMiniGame().getKaelaEvent().getServer().getPluginManager().callEvent(new ColorGachaDisqualificationEvent(arena, gamePlayer));

                    arena.getButtonHelper().getButtons().forEach(gButton -> gButton.setStatus(ButtonStatus.UNPRESSED));
                } else {
                    arena.changeKaelaFace(ColorGachaArena.KAELA_MODEL_HAPPY);
                    arena.changeScreen(ColorGachaArena.SCREEN_MODEL_CORRECT);
                    arena.getAnnouncer().sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_BELL, 0.8F, 0.8F);
                    arena.getAnnouncer().sendGlobalMessage(
                            "&a[Game] &e" + gamePlayer.getName() + " &apressed a safe button!",
                            false
                    );
                    arena.getAnnouncer().sendGlobalTitle(Title.title(
                            Component.empty(),
                            arena.getGameMiniMessage().deserialize("<aqua>" + player.getName() + " <green>pressed a safe button!"),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)
                    ));
                }

                Bukkit.getScheduler().runTaskLater(arena.getColorGachaMiniGame().getKaelaEvent(), () -> {
                    if (arena.getState() != ColorGachaMatchState.GAME) return;

                    // Rotate
                    arena.rotateTalents();
                }, 140L);
            }, 100L);
        }, 30L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchDisqualification(final ColorGachaDisqualificationEvent event) {
        if (arena.getState() != ColorGachaMatchState.GAME) return;

        final ColorGachaPlayer gamePlayer = event.getDisqualifiedPlayer();

        final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
        if (player == null) return;

        player.setVelocity(new Vector(0, 2, 0));

        Bukkit.getScheduler().runTaskLater(arena.getColorGachaMiniGame().getKaelaEvent(), () -> {
            player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getEyeLocation(), 2, 0.1, 0.1, 0.1, 0.1);
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5F, 1.5F));
            player.setGameMode(GameMode.SPECTATOR);

            arena.getAnnouncer().sendGlobalMessage(
                    "&b" + gamePlayer.getName() + " &cpressed the wrong button and has been eliminated!",
                    false
            );
            arena.getAnnouncer().sendGlobalTitle(Title.title(
                    Component.empty(),
                    arena.getGameMiniMessage().deserialize("<aqua>" + player.getName() + " <red>pressed the wrong button!"),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)
            ));

            arena.checkWinner();
        }, 10L);
    }
}
