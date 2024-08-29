package com.covercorp.kaelaevent.minigame.games.reflex.arena.listener;

import com.covercorp.kaelaevent.minigame.games.reflex.arena.ReflexArena;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.event.*;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.ReflexSpot;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.part.status.ButtonStatus;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.spot.part.status.ScreenStatus;
import com.covercorp.kaelaevent.minigame.games.reflex.arena.state.ReflexMatchState;

import com.covercorp.kaelaevent.minigame.games.reflex.arena.state.ReflexRoundState;
import com.covercorp.kaelaevent.minigame.games.reflex.player.ReflexPlayer;
import com.covercorp.kaelaevent.minigame.games.reflex.team.ReflexTeam;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import com.covercorp.kaelaevent.util.PlayerUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ReflexMatchGameListener implements Listener {
    private final ReflexArena arena;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteraction(final PlayerInteractAtEntityEvent event) {
        final Player player = event.getPlayer();
        if (event.getRightClicked().getType() != EntityType.INTERACTION) return;

        final Interaction interaction = (Interaction) event.getRightClicked();
        if (!NBTMetadataUtil.hasEntityString(interaction, "reflex_button")) return;

        final UUID clickedButtonUniqueId = UUID.fromString(NBTMetadataUtil.getEntityString(interaction, "reflex_button"));
        final ReflexSpot clickedSpot = arena.getSpots().get(clickedButtonUniqueId);
        if (clickedSpot == null) return;

        final Optional<ReflexPlayer> reflexPlayerOptional = arena.getPlayerHelper().getPlayer(player.getUniqueId()).map(g->(ReflexPlayer)g);
        if (reflexPlayerOptional.isEmpty()) return;

        final ReflexPlayer reflexPlayer = reflexPlayerOptional.get();
        final ReflexTeam reflexTeam = (ReflexTeam) reflexPlayer.getMiniGameTeam();
        if (reflexTeam == null) return;

        final ReflexSpot spot = reflexTeam.getReflexSpot();
        if (spot == null) return;

        Bukkit.getPluginManager().callEvent(new ReflexButtonEvent(arena, reflexPlayer, clickedSpot));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onButtonClick(final ReflexButtonEvent event) {
        if (arena.getState() != ReflexMatchState.GAME) return;

        final ReflexPlayer reflexPlayer = event.getPlayer();
        final ReflexSpot spot = event.getSpot();

        final Player player = Bukkit.getPlayer(reflexPlayer.getUniqueId());
        if (player == null) return;

        if (spot.getButton().getStatus() == ButtonStatus.PRESSED) return;

        PlayerUtils.forceSwingAnimation(player);
        spot.getButton().setStatus(ButtonStatus.PRESSED);

        if (arena.getReflexMatchProperties().getRoundState() == ReflexRoundState.WAITING) {
            // player pressed the button too early
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                    "<#ff8a93>[⚡] Talent <white>" + reflexPlayer.getName() + " <#ff8a93>pressed the button too early!"
            ));

            final ReflexTeam loserTeam = (ReflexTeam) reflexPlayer.getMiniGameTeam();
            final ReflexTeam winnerTeam = arena.getOppositeTeam(loserTeam);

            Bukkit.getPluginManager().callEvent(new ReflexRoundEndEvent(arena, loserTeam, winnerTeam));
        }
        if (arena.getReflexMatchProperties().getRoundState() == ReflexRoundState.PRESS) {
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                    "<#75ffac>[⚡] Talent <white>" + reflexPlayer.getName() + " <#75ffac>pressed the button first!"
            ));

            final ReflexTeam winnerTeam = (ReflexTeam) reflexPlayer.getMiniGameTeam();
            final ReflexTeam loserTeam = arena.getOppositeTeam(winnerTeam);

            Bukkit.getPluginManager().callEvent(new ReflexRoundEndEvent(arena, loserTeam, winnerTeam));
        }
        if (arena.getReflexMatchProperties().getRoundState() == ReflexRoundState.POST) {
            /*player.sendMessage(arena.getGameMiniMessage().deserialize(
                    "<#ff8a93>Your opponent pressed the button already! You lost!"
            ));*/
            //player.playSound(player.getLocation(), "kaela:bad", 0.7F, 0.8F);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCooldownEnd(final ReflexCooldownEndEvent event) {
        if (arena.getState() != ReflexMatchState.GAME) return;

        if (arena.getReflexMatchProperties().getRoundState() != ReflexRoundState.PRESS) return;

        // Event called when kaela machine needs to be pressed
        arena.getReflexMatchProperties().setRoundState(ReflexRoundState.PRESS);

        arena.getSpots().values().forEach(spot -> {
            spot.setButtonStatus(ButtonStatus.UNPRESSED);
            spot.setScreenStatus(ScreenStatus.PRESS);
        });

        arena.getAnnouncer().sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 1.8F, 1.8F);
        arena.getAnnouncer().sendGlobalMessage("&e[⚡] Press the button!", false);
        arena.getAnnouncer().sendGlobalTitle(Title.title(
                Component.empty(),
                arena.getGameMiniMessage().deserialize("<#d3ff73>PRESS!"),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)
        ));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRoundStart(final ReflexRoundStartEvent event) {
        if (arena.getState() != ReflexMatchState.GAME) return;

        arena.getReflexMatchProperties().setRoundState(ReflexRoundState.WAITING);
        arena.getReflexMatchProperties().setPressCooldown(new Random().nextInt(8 - 3 + 1) + 3);

        arena.getSpots().values().forEach(spot -> {
            spot.setButtonStatus(ButtonStatus.UNPRESSED);
            spot.setScreenStatus(ScreenStatus.WAIT);
        });

        arena.getAnnouncer().sendGlobalComponent(
                arena.getGameMiniMessage().deserialize("<#29ff7e>[⚡] Round start.")
        );
        arena.getAnnouncer().sendGlobalTitle(Title.title(
                Component.empty(),
                arena.getGameMiniMessage().deserialize("<#29ff7e>Round start..."),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)
        ));
        arena.getAnnouncer().sendGlobalSound(Sound.ITEM_BOOK_PAGE_TURN, 0.4F, 0.8F);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRoundEnd(final ReflexRoundEndEvent event) {
        if (arena.getState() != ReflexMatchState.GAME) return;

        arena.getReflexMatchProperties().setRoundState(ReflexRoundState.POST);

        final ReflexTeam winnerTeam = event.getWinnerTeam();
        winnerTeam.setScore(winnerTeam.getScore() + 1);
        winnerTeam.getReflexSpot().setScreenStatus(ScreenStatus.NICE);
        final ReflexTeam loserTeam = event.getLoserTeam();
        loserTeam.getReflexSpot().setScreenStatus(ScreenStatus.BAD);

        winnerTeam.getPlayers().forEach(teamPlayer -> {
            final Player player = Bukkit.getPlayer(teamPlayer.getUniqueId());
            if (player == null) return;

            player.showTitle(Title.title(
                    arena.getGameMiniMessage().deserialize("\uE300"),
                    Component.empty(),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofMillis(200))
            ));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.6F, 1.6F);


            final World world = player.getWorld();

            final Firework firework = world.spawn(player.getLocation(), Firework.class);
            final FireworkMeta fireworkMeta = firework.getFireworkMeta();

            fireworkMeta.setPower(2);
            fireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.GREEN).withFade(Color.YELLOW).build());

            firework.setFireworkMeta(fireworkMeta);
            firework.detonate();

            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8F, 0.8F);
            player.spawnParticle(Particle.FIREWORK, player.getLocation(), 100, 0.5, 0.5, 0.5, 0.1);
        });
        loserTeam.getPlayers().forEach(teamPlayer -> {
            final Player player = Bukkit.getPlayer(teamPlayer.getUniqueId());
            if (player == null) return;

            player.showTitle(Title.title(
                    arena.getGameMiniMessage().deserialize("\uE301"),
                    Component.empty(),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofMillis(200))
            ));
            player.playSound(player.getLocation(), "kaela:bad", 0.8F, 0.8F);
        });

        arena.getAnnouncer().sendGlobalComponent(
                arena.getGameMiniMessage().deserialize("<#29ff7e>[⚡] Team </#29ff7e><white>")
                        .append(winnerTeam.getBetterPrefix())
                        .append(arena.getGameMiniMessage().deserialize("<#29ff7e>won the round. <gray>(" + winnerTeam.getScore() + "/" + arena.getReflexMatchProperties().getRoundsToWin() + ")"))
        );

        Bukkit.getScheduler().runTaskLater(arena.getReflexMiniGame().getKaelaEvent(), task -> {
            if (arena.getState() != ReflexMatchState.GAME) {
                task.cancel();
                return;
            }

            if (winnerTeam.getScore() >= arena.getReflexMatchProperties().getRoundsToWin()) {
                arena.setWinnerTeam(winnerTeam);
                arena.stop();
                return;
            }
            Bukkit.getPluginManager().callEvent(new ReflexRoundStartEvent(arena));
        }, 100L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVehicleLeave(final EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        if (arena.getState() == ReflexMatchState.ARENA_STARTING || arena.getState() == ReflexMatchState.GAME) {
            event.setCancelled(true);
        }
    }
}
