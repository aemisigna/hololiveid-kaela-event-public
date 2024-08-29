package com.covercorp.kaelaevent.minigame.games.squid.arena.listener;

import com.covercorp.kaelaevent.minigame.games.squid.arena.SquidArena;
import com.covercorp.kaelaevent.minigame.games.squid.arena.event.*;
import com.covercorp.kaelaevent.minigame.games.squid.arena.galon.status.GalonChanStatus;
import com.covercorp.kaelaevent.minigame.games.squid.arena.state.SquidMatchState;
import com.covercorp.kaelaevent.minigame.games.squid.player.SquidPlayer;
import com.covercorp.kaelaevent.minigame.games.squid.team.SquidTeam;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SquidMatchGameListener implements Listener {
    private final SquidArena arena;
    private final Map<UUID, Location> lastLocations;

    public SquidMatchGameListener(final SquidArena arena) {
        this.arena = arena;

        this.lastLocations = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchTick(final SquidTickEvent event) {
        if (arena.getState() != SquidMatchState.GAME) return;

        arena.getPlayerHelper().getPlayerList()
                .stream()
                .map(g -> (SquidPlayer)g)
                .filter(squidPlayer -> !squidPlayer.isFinished())
                .forEach(squidPlayer -> {
                    final Player player = Bukkit.getPlayer(squidPlayer.getUniqueId());
                    if (player == null) return;
                    final Location location = player.getLocation();

                    if (squidPlayer.isDead()) {
                        player.setPose(Pose.SWIMMING, true);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5, 100));
                        return;
                    }

                    if (!squidPlayer.isFlaggedToDeath() && !arena.getMovedTalents().contains(squidPlayer.getUniqueId())) {
                        if (arena.getGoalZone().containsLocation(location)) {
                            Bukkit.getPluginManager().callEvent(new SquidPlayerFinishEvent(arena, squidPlayer));
                            return;
                        }
                    }

                    if (arena.getSquidMatchProperties().isAnalyzing() &&
                            lastLocations.containsKey(player.getUniqueId()) &&
                            hasMoved(lastLocations.get(player.getUniqueId()), location) &&
                            !arena.getMovedTalents().contains(squidPlayer.getUniqueId()) &&
                            !squidPlayer.isFinished())

                        arena.getMovedTalents().add(squidPlayer.getUniqueId());

                    lastLocations.put(player.getUniqueId(), location);
                });
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onJump(PlayerJumpEvent event) {
        if (this.arena.getState() != SquidMatchState.GAME) return;
        final Player player = event.getPlayer();
        final Optional<SquidPlayer> gamePlayer = arena.getPlayerHelper().getPlayer(player.getUniqueId()).map(SquidPlayer.class::cast);

        if (gamePlayer.isEmpty()) return;

        if ((gamePlayer.get()).isDead()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDisqualification(final SquidPlayerDeathEvent event) {
        final SquidPlayer squidPlayer = event.getPlayer();
        final Player player = Bukkit.getPlayer(squidPlayer.getUniqueId());
        if (player == null) return;

        if (squidPlayer.isDead()) return;

        squidPlayer.setDead(true);

        final Location location = player.getLocation();
        final Location gunLocation = arena.getArenaGunLocations().get(new Random().nextInt(arena.getArenaGunLocations().size()));
        final Vector direction = gunLocation.toVector().subtract(location.toVector()).normalize();
        final double distance = location.distance(gunLocation);
        final World world = location.getWorld();

        for (double i = 0.0; i < distance; i += 0.2) {
            final Location currentLocation = location.clone().add(direction.clone().multiply(i));
            world.spawnParticle(Particle.SMOKE, currentLocation, 1, 0.0, 0.0, 0.0, 0.0);
        }

        location.getWorld().spawnParticle(Particle.DUST_PILLAR, location, 150, 0.5, 0.7, 0.5, Material.REDSTONE_BLOCK.createBlockData());

        final Color fromColor = Color.fromRGB(237, 51, 59);
        final Color toColor = Color.fromRGB(166, 28, 46);
        final Particle.DustTransition dustTransition = new Particle.DustTransition(fromColor, toColor, 2.0f);

        world.spawnParticle(Particle.DUST_COLOR_TRANSITION, location, 3, 0.3, 0.4, 0.3, 0.0, (Object)dustTransition);

        arena.getAnnouncer().sendGlobalSound("kaela:gunshot", 1.0f, 1.9f);
        arena.getAnnouncer().sendGlobalComponent(this.arena.getGameMiniMessage().deserialize("<#7dffd6>[\ud83e\udd91] Talent <white>" + squidPlayer.getName() + "<#7dffd6>, eliminated."));
        player.sendMessage(arena.getGameMiniMessage().deserialize(
                "<#ff7881>Galon-chan detected a movement and you got shot!")
        );
        Bukkit.getScheduler().runTaskLater(arena.getSquidMiniGame().getKaelaEvent(), () -> {
            if (arena.getState() != SquidMatchState.GAME) return;
            if (arena.getUnfinishedAlivePlayers().size() <= 0) {
                arena.endGame();
            }
        }, 40L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerFinish(final SquidPlayerFinishEvent event) {
        final SquidPlayer squidPlayer = event.getPlayer();

        squidPlayer.setFlaggedToDeath(false);
        squidPlayer.setFinished(true);
        squidPlayer.setFinishTime(Instant.now());

        final Player player = Bukkit.getPlayer(squidPlayer.getUniqueId());
        if (player == null) return;

        final Instant finishTime = squidPlayer.getFinishTime();
        final Instant startTime = this.arena.getSquidMatchProperties().getStartTime();
        final Duration duration = Duration.between(startTime, finishTime);

        final long minutes = duration.toMinutes();
        final long seconds = duration.get(ChronoUnit.SECONDS) % 60L;

        final String formattedTime = String.format("%02d:%02d", minutes, seconds);

        arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize("<#7dffd6>[\ud83e\udd91] Talent <white>" + squidPlayer.getName() + " <#7dffd6>finished the race in <white>" + formattedTime + "<#7dffd6>."));
        final Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        final FireworkMeta fireworkMeta = firework.getFireworkMeta();

        fireworkMeta.setPower(2);
        fireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.GREEN).withFade(Color.YELLOW).build());
        firework.setFireworkMeta(fireworkMeta);
        firework.detonate();

        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 0.8f);
        player.spawnParticle(Particle.FIREWORK, player.getLocation(), 100, 0.5, 0.5, 0.5, 0.1);

        final SquidTeam team = (SquidTeam) squidPlayer.getMiniGameTeam();
        if (team == null) return;

        if (team.getFinishedPlayers() == team.getPlayers().size()) {
            arena.getAnnouncer().sendGlobalComponent(this.arena.getGameMiniMessage().deserialize("<#7dffd6>The team ").append(team.getBetterPrefix()).append(arena.getGameMiniMessage().deserialize("<#7dffd6>finished the race, match finished!")));
            arena.endGame();
            return;
        }

        if (arena.getUnfinishedAlivePlayers().size() <= 0) {
            arena.getAnnouncer().sendGlobalComponent(arena.getGameMiniMessage().deserialize("<#7dffd6>All alive talents passed the finish line, match finished!"));
            arena.endGame();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnalyzeStart(final SquidGalonChanStartAnalyzingEvent event) {
        if (arena.getState() == SquidMatchState.GAME) {
            arena.getGalonChan().setStatus(GalonChanStatus.NO);
            Bukkit.getScheduler().runTaskLater(arena.getSquidMiniGame().getKaelaEvent(), () -> arena.getSquidMatchProperties().setAnalyzing(true), 15L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnalyzeEnd(final SquidGalonChanEndAnalyzingEvent event) {
        if (this.arena.getState() == SquidMatchState.GAME) {
            this.arena.getGalonChan().setStatus(GalonChanStatus.YES);
            this.arena.getSquidMatchProperties().setAnalyzing(false);
        }
    }

    private boolean hasMoved(Location lastLocation, Location currentLocation) {
        return lastLocation.getX() != currentLocation.getX() ||
                lastLocation.getY() != currentLocation.getY() ||
                lastLocation.getZ() != currentLocation.getZ();
    }
}
