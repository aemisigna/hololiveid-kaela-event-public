package com.covercorp.kaelaevent.minigame.games.zombie.arena.listener;

import com.covercorp.kaelaevent.minigame.games.zombie.arena.ZombieArena;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.state.ZombieMatchState;
import com.covercorp.kaelaevent.minigame.games.zombie.arena.task.ZombieStartingTask;
import com.covercorp.kaelaevent.minigame.games.zombie.player.ZombiePlayer;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import com.covercorp.kaelaevent.util.simple.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class ZombieMatchListener implements Listener {
    private final ZombieArena arena;

    public ZombieMatchListener(final ZombieArena arena) {
        this.arena = arena;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(final PlayerInteractEvent event) {
        final Player sender = event.getPlayer();

        final ItemStack itemStack = sender.getInventory().getItemInMainHand();

        if (itemStack.getType() == Material.AIR) return;

        if (itemStack.getType() == Material.BOW) return;

        if (!NBTMetadataUtil.hasString(itemStack, "accessor")) return;

        event.setCancelled(true);

        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

        final String accessor = NBTMetadataUtil.getString(itemStack, "accessor");
        if (accessor == null) return;

        if (accessor.equals("start_game")) {
            if (arena.getState() != ZombieMatchState.WAITING) {
                sender.sendMessage(arena.getZombieMiniGame().getMiniMessage().deserialize("<red>The game is already started!"));
                return;
            }

            sender.sendMessage(StringUtils.translate("&7[Zombie] Starting game..."));
            arena.getAnnouncer().sendGlobalMessage("&6" + sender.getName() + "&7" + " is trying to START the game.", false);

            if (arena.getZombieMiniGame().getPlayerHelper().getPlayerList().size() < 2) {
                sender.sendMessage(StringUtils.translate("&c&lCan't start the match!"));
                sender.sendMessage(StringUtils.translate("&cThere are not enough players! The minimum player size must be [2]\n "));
                return;
            }


            if (arena.getTeamHelper().getTeamList().stream().anyMatch(team -> team.getPlayers().isEmpty())) {
                sender.sendMessage(StringUtils.translate("&c&lCan't start the match!"));
                sender.sendMessage(StringUtils.translate("&cThere are teams without players!\n "));

                sender.sendMessage(StringUtils.translate("&7The following teams don't have players:"));
                arena.getTeamHelper().getTeamList().forEach(team -> {
                    if (team.getPlayers().isEmpty()) {
                        sender.sendMessage(arena.getGameMiniMessage().deserialize(
                                "<gray>- <white>"
                        ).append(team.getBetterPrefix()));
                    }
                });
                return;
            }

            if (!arena.getZombieMatchProperties().isStarting()) {
                arena.getZombieMatchProperties().setStarting(true);
                arena.getZombieMatchProperties().setStartingTaskId(Bukkit.getScheduler().runTaskTimer(arena.getZombieMiniGame().getKaelaEvent(), new ZombieStartingTask(arena), 0L, 20L).getTaskId());

                sender.sendMessage(StringUtils.translate("&7[!] Starting match..."));
                sender.playSound(sender, Sound.UI_BUTTON_CLICK, 0.8F, 0.8F);

                Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);

                return;
            }

            Bukkit.getScheduler().cancelTask(arena.getZombieMatchProperties().getStartingTaskId());

            arena.getZombieMatchProperties().resetTimer();

            arena.setState(ZombieMatchState.WAITING);

            sender.sendMessage(StringUtils.translate("&7[!] Stopped match start."));
            sender.playSound(sender, Sound.UI_BUTTON_CLICK, 0.8F, 0.8F);
        }
        if (accessor.equals("stop_game")) {
            if (arena.getState() != ZombieMatchState.GAME) {
                sender.sendMessage(StringUtils.translate("&cThe game is not started! If the game is paused, you must resume it first!"));
                return;
            }

            sender.sendMessage(StringUtils.translate("&7[Zombie] Stopping game..."));
            arena.getAnnouncer().sendGlobalMessage("&6" + sender.getName() + " &7is trying to STOP the game.", false);
            arena.stop();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();

        final Optional<ZombiePlayer> zombiePlayerOptional = arena.getZombieMiniGame().getPlayerHelper().getPlayer(player.getUniqueId())
                .map(miniGamePlayer -> (ZombiePlayer) miniGamePlayer);

        if (zombiePlayerOptional.isEmpty()) return;

        if (arena.getState() != ZombieMatchState.WAITING) {
            player.sendMessage(arena.getZombieMiniGame().getMiniMessage().deserialize("<red>You can't drop items while playing!"));
            event.setCancelled(true);
        }

        if (NBTMetadataUtil.hasString(event.getItemDrop().getItemStack(), "accessor")) event.setCancelled(true);
    }
}
