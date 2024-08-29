package com.covercorp.kaelaevent.minigame.games.tug.arena.listener;

import com.covercorp.kaelaevent.minigame.games.tug.arena.TugArena;
import com.covercorp.kaelaevent.minigame.games.tug.arena.event.TugTickEvent;
import com.covercorp.kaelaevent.minigame.games.tug.arena.state.TugMatchState;
import com.covercorp.kaelaevent.minigame.games.tug.player.TugPlayer;
import com.covercorp.kaelaevent.minigame.games.tug.team.TugTeam;
import com.covercorp.kaelaevent.minigame.games.tug.util.TugMatchUtil;
import com.covercorp.kaelaevent.util.PlayerUtils;
import com.covercorp.kaelaevent.util.TimeUtils;
import com.covercorp.kaelaevent.util.simple.StringUtils;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Optional;
import java.util.Random;

public final class TugMatchGameListener implements Listener {
    private final TugArena arena;

    public TugMatchGameListener(final TugArena arena) {
        this.arena = arena;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchTick(final TugTickEvent event) {
        if (arena.getState() != TugMatchState.GAME) return;

        // Send score actionbar
        final TugTeam team1 = (TugTeam) arena.getTeamHelper().getTeamList().get(0);
        final TugTeam team2 = (TugTeam) arena.getTeamHelper().getTeamList().get(1);

        final String scoreTunned = StringUtils.translate(
                LegacyComponentSerializer.legacyAmpersand().serialize(team1.getBetterPrefix()) + "&e(" + team1.getTeamScore() + " pts.)" +
                        " &7- " +
                        "&e(" + team2.getTeamScore() + " pts.) &f" + LegacyComponentSerializer.legacyAmpersand().serialize(team2.getBetterPrefix())
        );

        final String tagger = "&e[&fTime limit: &7%s&e] ";

        // Format the time left to a string
        final String timeLeft = TimeUtils.formatTime(arena.getTimeLeft());

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendActionBar(scoreTunned + StringUtils.translate(String.format(tagger, timeLeft)));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPullRope(final PlayerInteractAtEntityEvent event) {
        final Player player = event.getPlayer();

        if (event.getRightClicked().getType() != EntityType.INTERACTION) return;

        if (arena.getState() != TugMatchState.GAME) return;

        final Optional<TugPlayer> tugPlayerOptional = arena.getTugMiniGame().getPlayerHelper().getPlayer(player.getUniqueId())
                .map(miniGamePlayer -> (TugPlayer) miniGamePlayer);
        if (tugPlayerOptional.isEmpty()) return;

        final TugPlayer tugPlayer = tugPlayerOptional.get();
        final TugTeam tugTeam = (TugTeam) tugPlayer.getMiniGameTeam();

        if (tugTeam == null) return;

        if (tugPlayer.isSpectating()) return;

        tugPlayer.setScore(tugPlayer.getScore() + 1);

        PlayerUtils.forceSwingAnimation(player);
        player.playSound(player, Sound.ITEM_BONE_MEAL_USE, 0.8F, 0.8F);
        event.getRightClicked().getLocation().getWorld().spawnParticle(Particle.CRIT, event.getRightClicked().getLocation(), 3, 1, 1, 1);

        final TugTeam team1 = (TugTeam) arena.getTeamHelper().getTeamList().get(0);
        final TugTeam team2 = (TugTeam) arena.getTeamHelper().getTeamList().get(1);
        // Get a random player of the other team to pull to the center a bit.
        TugTeam rivalTeam;
        if (tugTeam.getIdentifier().equals(team1.getIdentifier())) {
            rivalTeam = team2;
        } else {
            rivalTeam = team1;
        }

        if (rivalTeam == null) return;

        final Random random = new Random();
        final TugPlayer rivalTugPlayer = rivalTeam.getPlayers().stream().toList().get(random.nextInt(rivalTeam.getPlayers().size()));
        final Player rivalPlayer = Bukkit.getPlayer(rivalTugPlayer.getUniqueId());
        if (rivalPlayer == null) return;

        if (rivalPlayer.getGameMode() != GameMode.SPECTATOR) {
            if (rivalPlayer.isOnGround()) {
                TugMatchUtil.moveToward(rivalPlayer, arena.getCenterLocation().clone().add(0, -8, 0), 0.06);
            }
        }

        // If the point difference between the teams is 100 or more, end the game.
        final int diff = Math.abs(team1.getTeamScore() - team2.getTeamScore());
        if (diff >= 100) {
            arena.setState(TugMatchState.ENDING);
            arena.runLoser();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;

        if (arena.getState() == TugMatchState.ARENA_STARTING) {
            // Check if die
            if (player.getHealth() - event.getFinalDamage() > 0) return;

            event.setCancelled(true);

            final Optional<TugPlayer> tugPlayerOptional = arena.getTugMiniGame().getPlayerHelper().getPlayer(player.getUniqueId())
                    .map(miniGamePlayer -> (TugPlayer) miniGamePlayer);
            if (tugPlayerOptional.isEmpty()) return;

            final TugPlayer tugPlayer = tugPlayerOptional.get();
            final TugTeam tugTeam = (TugTeam) tugPlayer.getMiniGameTeam();
            if (tugTeam == null) return;

            player.teleport(tugTeam.getSpawnPoint());
        }
        if (arena.getState() == TugMatchState.GAME || arena.getState() == TugMatchState.ENDING) {
            // Check if the player died
            if (player.getHealth() - event.getFinalDamage() > 0) return;

            event.setCancelled(true);

            final Optional<TugPlayer> tugPlayerOptional = arena.getTugMiniGame().getPlayerHelper().getPlayer(player.getUniqueId())
                    .map(miniGamePlayer -> (TugPlayer) miniGamePlayer);
            if (tugPlayerOptional.isEmpty()) return;

            final TugPlayer tugPlayer = tugPlayerOptional.get();
            final TugTeam tugTeam = (TugTeam) tugPlayer.getMiniGameTeam();
            if (tugTeam == null) return;

            final Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
            final FireworkMeta fireworkMeta = firework.getFireworkMeta();

            fireworkMeta.setPower(1);
            fireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.RED).withFade(Color.ORANGE).build());

            firework.setFireworkMeta(fireworkMeta);
            firework.detonate();

            player.setGameMode(GameMode.SPECTATOR);
            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0F, 0.6F);
            player.sendTitle(StringUtils.translate("&c&lYou died!"), StringUtils.translate("You fell off the bridge!"), 5, 40, 10);

            arena.getAnnouncer().sendGlobalMessage(StringUtils.translate("#ff6370" + tugPlayer.getName() + " fell off the platform!"), false);

            if (arena.getState() != TugMatchState.ENDING) {
                // Check if there's no more players alive in the team.
                if (tugTeam.getPlayers().stream().filter(playingPlayer -> !playingPlayer.isSpectating()).toList().isEmpty()) {
                    arena.setState(TugMatchState.ENDING);
                    arena.runLoser();

                    return;
                }

                // If there's no more players alive in general, end the game.
                if (arena.getPlayerHelper().getPlayerList().stream().map(genericPlayer -> (TugPlayer) genericPlayer).filter(playingPlayer -> !playingPlayer.isSpectating()).toList().isEmpty()) {
                    arena.setState(TugMatchState.ENDING);
                    arena.runLoser();
                }
            }
        }
    }
}
