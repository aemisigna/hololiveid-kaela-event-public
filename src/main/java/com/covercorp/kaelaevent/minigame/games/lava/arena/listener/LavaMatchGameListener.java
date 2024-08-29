package com.covercorp.kaelaevent.minigame.games.lava.arena.listener;

import com.covercorp.kaelaevent.minigame.games.lava.arena.LavaArena;
import com.covercorp.kaelaevent.minigame.games.lava.arena.event.LavaDisqualificationEvent;
import com.covercorp.kaelaevent.minigame.games.lava.arena.event.LavaRoundEndEvent;
import com.covercorp.kaelaevent.minigame.games.lava.arena.event.LavaRoundStartEvent;
import com.covercorp.kaelaevent.minigame.games.lava.arena.event.LavaTickEvent;
import com.covercorp.kaelaevent.minigame.games.lava.arena.slot.slot.state.SlotStatus;
import com.covercorp.kaelaevent.minigame.games.lava.arena.state.LavaMatchState;
import com.covercorp.kaelaevent.minigame.games.lava.player.LavaPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.time.Duration;

public final class LavaMatchGameListener implements Listener {
    private final LavaArena arena;

    public LavaMatchGameListener(final LavaArena arena) {
        this.arena = arena;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTick(final LavaTickEvent event) {
        if (arena.getState() != LavaMatchState.GAME) return;

        arena.getPlayerHelper().getPlayerList().stream().map(genericPlayer -> (LavaPlayer) genericPlayer).forEach(lavaPlayer -> {
            final Player player = Bukkit.getPlayer(lavaPlayer.getUniqueId());
            if (player == null) return;

            if (!lavaPlayer.isDead()) {
                if (player.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.RED_CONCRETE) {
                    // disqualify
                    Bukkit.getPluginManager().callEvent(new LavaDisqualificationEvent(arena, lavaPlayer));
                }
            }
            if (!arena.isShuffling()) {
                if (player.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.YELLOW_CONCRETE) {
                    player.showTitle(Title.title(
                            Component.empty(),
                            arena.getGameMiniMessage().deserialize("<yellow>[Move to a green zone!]"),
                            Title.Times.times(
                                    Duration.ZERO,
                                    Duration.ofMillis(500),
                                    Duration.ZERO
                            )
                    ));
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRoundStart(final LavaRoundStartEvent event) {
        if (arena.getState() != LavaMatchState.GAME) return;

        arena.getLavaMatchProperties().setRound(
                arena.getLavaMatchProperties().getRound() + 1
        );

        arena.setShuffling(true);
        arena.setChecking(false);

        arena.getAnnouncer().sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", false);
        arena.getAnnouncer().sendGlobalMessage("&f&lRound N°" + arena.getLavaMatchProperties().getRound(), true);
        arena.getAnnouncer().sendGlobalMessage("&eTime to run to the squares: &a[" + arena.getRoundRunTime() + " seconds]", true);
        arena.getAnnouncer().sendGlobalMessage("&eGreen slot size: &a[" + arena.getRoundSlotSize() + " safe slots]", true);
        arena.getAnnouncer().sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", false);
        arena.getAnnouncer().sendGlobalTitle(Title.title(
                Component.empty(),
                arena.getGameMiniMessage().deserialize("<green>Round " + arena.getLavaMatchProperties().getRound()),
                Title.Times.times(
                        Duration.ZERO,
                        Duration.ofMillis(500),
                        Duration.ZERO
                )
        ));

        arena.getAnnouncer().sendGlobalSound(Sound.UI_BUTTON_CLICK, 0.7F, 0.7F);
    }

    // Called when block cooldown reaches 0
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRoundEnd(final LavaRoundEndEvent event) {
        if (arena.getState() != LavaMatchState.GAME) return;

        arena.setChecking(true);

        arena.getLavaMatchProperties().setBlockCooldown(arena.getRoundRunTime());

        arena.getSlotHelper().getSlots().stream().filter(lavaSlot -> lavaSlot.getStatus() == SlotStatus.WARNING).forEach(lavaSlot -> {
            lavaSlot.setStatus(SlotStatus.LAVA);

            lavaSlot.getBlocks().forEach(blockLoc -> blockLoc.getWorld().spawnParticle(Particle.CLOUD, blockLoc, 1, 0.3, 0.3, 0.3));
        });

        arena.getAnnouncer().sendGlobalSound(Sound.BLOCK_PISTON_CONTRACT, 0.8F, 0.8F);

        Bukkit.getScheduler().runTaskLater(arena.getLavaMiniGame().getKaelaEvent(), () -> {
            if (arena.getState() != LavaMatchState.GAME) return;

            Bukkit.getPluginManager().callEvent(new LavaRoundStartEvent(arena));
        }, 60L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchDisqualification(final LavaDisqualificationEvent event) {
        if (arena.getState() != LavaMatchState.GAME) return;

        final LavaPlayer gamePlayer = event.getDisqualifiedPlayer();
        if (gamePlayer.isDead()) return;

        final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
        if (player == null) return;

        player.setVelocity(new Vector(0, 27, 0));
        player.getWorld().spawnParticle(Particle.FIREWORK, player.getEyeLocation(), 2, 0.1, 0.1, 0.1, 0.1);
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 1.5F, 1.5F));

        gamePlayer.setDead(true);

        arena.getAnnouncer().sendGlobalMessage(
                "&b" + gamePlayer.getName() + " &cstepped on a wrong slot and got burnt into a crisp!",
                false
        );

        Bukkit.getScheduler().runTaskLater(arena.getLavaMiniGame().getKaelaEvent(), () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 1, 0.0, 0.0, 0.0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 0.8f);
        }, 8L);

        arena.checkWinner();
    }
}
