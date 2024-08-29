package com.covercorp.kaelaevent.minigame.games.tower.arena.listener;

import com.covercorp.kaelaevent.minigame.games.tower.arena.TowerArena;
import com.covercorp.kaelaevent.minigame.games.tower.arena.event.*;
import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.TowerSpot;
import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.part.TowerGambleMachine;
import com.covercorp.kaelaevent.minigame.games.tower.arena.spot.part.status.GambleMachineStatus;
import com.covercorp.kaelaevent.minigame.games.tower.arena.state.TowerMatchState;
import com.covercorp.kaelaevent.minigame.games.tower.arena.state.TowerRoundState;
import com.covercorp.kaelaevent.minigame.games.tower.player.TowerPlayer;
import com.covercorp.kaelaevent.minigame.games.tower.team.TowerTeam;
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
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TowerMatchGameListener implements Listener {
    private final TowerArena arena;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTick(final TowerTickEvent event) {
        if (arena.getState() != TowerMatchState.GAME) return;

        arena.getSpots().values().forEach(spot -> {
            final TowerGambleMachine gambleMachine = spot.getGambleMachine();
            if (gambleMachine == null) return;
            if (gambleMachine.getStatus() != GambleMachineStatus.ROLL) return;

            final Location location = gambleMachine.getLocation();

            final float randomValue = 1.3F + (1.5F - 1.3F) * new Random().nextFloat();
            location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.2F, randomValue);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGambleStart(final TowerGambleRollStartEvent event) {
        if (arena.getState() != TowerMatchState.GAME) return;

        final TowerPlayer towerPlayer = event.getPlayer();
        final TowerSpot spot = event.getSpot();

        final Player player = Bukkit.getPlayer(towerPlayer.getUniqueId());
        if (player == null) return;

        if (spot.getGambleMachine().getStatus() != GambleMachineStatus.NO_ROLL) return;

        PlayerUtils.forceSwingAnimation(player);
        spot.getGambleMachine().setStatus(GambleMachineStatus.ROLL);

        Bukkit.getScheduler().runTaskLater(arena.getTowerMiniGame().getKaelaEvent(), task -> {
            if (arena.getState() != TowerMatchState.GAME) {
                task.cancel();
                return;
            }

            final TowerSpot enemySpot = arena.getOppositeTeam((TowerTeam) towerPlayer.getMiniGameTeam()).getTowerSpot();
            GambleMachineStatus status;

            do {
                status = spot.getGambleMachine().getRandomStatus();
            } while (status == enemySpot.getGambleMachine().getStatus());

            Bukkit.getServer().getPluginManager().callEvent(new TowerGambleRollEndEvent(
                    arena,
                    towerPlayer,
                    spot,
                    status
            ));
        }, 55L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGambleEnd(final TowerGambleRollEndEvent event) {
        if (arena.getState() != TowerMatchState.GAME) return;

        if (arena.getTowerMatchProperties().getRoundState() != TowerRoundState.ROLLING) return;

        event.getSpot().getGambleMachine().setStatus(event.getSelectedStatus());

        arena.getAnnouncer().sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 1.8F, 1.8F);

        final GambleMachineStatus status = event.getSpot().getGambleMachine().getStatus();
        arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize(
                "<#ffce7a>[♜] Talent <white>" + event.getPlayer().getName() + " <#ffce7a>rolled prize <#b2ffa1>" + status.getName() + " <#e0e0e0>(" + status.getLevel() + " ⬆)"
        ));

        // Check if all teams rolled
        if (arena.allMachinesRolled()) {
            Bukkit.getPluginManager().callEvent(new TowerGambleFinishEvent(arena));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGambleFinish(final TowerGambleFinishEvent event) {
        if (arena.getState() != TowerMatchState.GAME) return;

        arena.getTowerMatchProperties().setRoundState(TowerRoundState.ROLLED);

        final TowerTeam winnerTeam = arena.getRoundWinner();
        if (winnerTeam == null) {
            Bukkit.getPluginManager().callEvent(new TowerRoundTieEvent(arena));
            return;
        }

        final TowerTeam loserTeam = arena.getOppositeTeam(winnerTeam);
        Bukkit.getPluginManager().callEvent(new TowerRoundEndEvent(arena, winnerTeam, loserTeam));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRoundStart(final TowerRoundStartEvent event) {
        if (arena.getState() != TowerMatchState.GAME) return;

        arena.getTowerMatchProperties().setRoundState(TowerRoundState.ROLLING);

        arena.getSpots().values().forEach(spot -> spot.setGambleMachineStatus(GambleMachineStatus.NO_ROLL));

        arena.getAnnouncer().sendGlobalComponent(
                arena.getGameMiniMessage().deserialize("<#ffbe4d>[♜] Round start, gamble!")
        );
        arena.getAnnouncer().sendGlobalTitle(Title.title(
                Component.empty(),
                arena.getGameMiniMessage().deserialize("<#ffbe4d>Gamble!"),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)
        ));
        arena.getAnnouncer().sendGlobalSound(Sound.ITEM_BOOK_PAGE_TURN, 0.4F, 0.8F);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRoundTie(final TowerRoundTieEvent event) {
        if (arena.getState() != TowerMatchState.GAME) return;

        arena.getTowerMatchProperties().setRoundState(TowerRoundState.POST);

        arena.getAnnouncer().sendGlobalSound(Sound.ENTITY_VILLAGER_NO, 0.8F, 0.8F);
        arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize("<#ffce7a>[♜] Match tie! Both talents have the same prize!"));

        Bukkit.getScheduler().runTaskLater(arena.getTowerMiniGame().getKaelaEvent(), task -> {
            if (arena.getState() != TowerMatchState.GAME) {
                task.cancel();
                return;
            }

            Bukkit.getPluginManager().callEvent(new TowerRoundStartEvent(arena));
        }, 70L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRoundEnd(final TowerRoundEndEvent event) {
        if (arena.getState() != TowerMatchState.GAME) return;

        arena.getTowerMatchProperties().setRoundState(TowerRoundState.POST);

        final TowerTeam winnerTeam = event.getWinnerTeam();
        winnerTeam.setScore(winnerTeam.getScore() + 1);
        final TowerTeam loserTeam = event.getLoserTeam();

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

        /*final GambleMachineStatus winnerStatus = winnerTeam.getTowerSpot().getGambleMachine().getStatus();
        final GambleMachineStatus loserStatus = loserTeam.getTowerSpot().getGambleMachine().getStatus();*/

        arena.getAnnouncer().sendGlobalComponent(
                arena.getGameMiniMessage().deserialize("<#ffce7a>[♜] Team </#ffce7a><white>")
                        .append(winnerTeam.getBetterPrefix())
                        .append(arena.getGameMiniMessage().deserialize("<#ffce7a>won the round. <gray>(" + winnerTeam.getScore() + "/" + arena.getTowerMatchProperties().getRoundsToWin() + ")"))
        );

        Bukkit.getScheduler().runTaskLater(arena.getTowerMiniGame().getKaelaEvent(), task -> {
            if (arena.getState() != TowerMatchState.GAME) {
                task.cancel();
                return;
            }

            if (winnerTeam.getScore() >= arena.getTowerMatchProperties().getRoundsToWin()) {
                arena.setWinnerTeam(winnerTeam);
                arena.stop();
                return;
            }
            Bukkit.getPluginManager().callEvent(new TowerRoundStartEvent(arena));
        }, 100L);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteraction(final PlayerInteractAtEntityEvent event) {
        final Player player = event.getPlayer();
        if (event.getRightClicked().getType() != EntityType.INTERACTION) return;

        final Interaction interaction = (Interaction) event.getRightClicked();
        if (!NBTMetadataUtil.hasEntityString(interaction, "gamble_machine")) return;

        final UUID clickedButtonUniqueId = UUID.fromString(NBTMetadataUtil.getEntityString(interaction, "gamble_machine"));
        final TowerSpot clickedSpot = arena.getSpots().get(clickedButtonUniqueId);
        if (clickedSpot == null) return;

        final Optional<TowerPlayer> towerPlayerOptional = arena.getPlayerHelper().getPlayer(player.getUniqueId()).map(g->(TowerPlayer)g);
        if (towerPlayerOptional.isEmpty()) return;

        final TowerPlayer towerPlayer = towerPlayerOptional.get();
        final TowerTeam towerTeam = (TowerTeam) towerPlayer.getMiniGameTeam();
        if (towerTeam == null) return;

        final TowerSpot spot = towerTeam.getTowerSpot();
        if (spot == null) return;

        Bukkit.getPluginManager().callEvent(new TowerGambleRollStartEvent(arena, towerPlayer, clickedSpot));
    }
}
