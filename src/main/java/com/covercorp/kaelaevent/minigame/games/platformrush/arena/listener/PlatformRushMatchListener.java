package com.covercorp.kaelaevent.minigame.games.platformrush.arena.listener;

import com.covercorp.kaelaevent.minigame.games.platformrush.arena.PlatformRushArena;

import com.covercorp.kaelaevent.minigame.games.platformrush.arena.state.PlatformRushMatchState;
import com.covercorp.kaelaevent.minigame.games.platformrush.arena.task.PlatformRushStartingTask;
import com.covercorp.kaelaevent.minigame.games.platformrush.player.PlatformRushPlayer;
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
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class PlatformRushMatchListener implements Listener {
    private final PlatformRushArena arena;

    public PlatformRushMatchListener(final PlatformRushArena arena) {
        this.arena = arena;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(final PlayerInteractEvent event) {
        final Player sender = event.getPlayer();

        final ItemStack itemStack = sender.getInventory().getItemInMainHand();

        if (itemStack.getType() == Material.AIR) return;

        if (itemStack.getType() == Material.DIAMOND_SHOVEL) return;

        if (!NBTMetadataUtil.hasString(itemStack, "accessor")) return;

        event.setCancelled(true);

        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

        final String accessor = NBTMetadataUtil.getString(itemStack, "accessor");

        if (accessor.equals("start_game")) {
            if (arena.getState() != PlatformRushMatchState.WAITING) {
                sender.sendMessage(arena.getPlatformRushMiniGame().getMiniMessage().deserialize("<red>The game is already started!"));
                return;
            }

            sender.sendMessage(StringUtils.translate("&7[Platform Rush] Starting game..."));
            arena.getAnnouncer().sendGlobalMessage("&6" + sender.getName() + "&7" + " is trying to START the game.", false);

            if (arena.getPlatformRushMiniGame().getPlayerHelper().getPlayerList().size() < 2) {
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

            if (!arena.getPlatformRushMatchProperties().isStarting()) {
                arena.getPlatformRushMatchProperties().setStarting(true);
                arena.getPlatformRushMatchProperties().setStartingTaskId(Bukkit.getScheduler().runTaskTimer(arena.getPlatformRushMiniGame().getKaelaEvent(), new PlatformRushStartingTask(arena), 0L, 20L).getTaskId());

                sender.sendMessage(StringUtils.translate("&7[!] Starting match..."));
                sender.playSound(sender, Sound.UI_BUTTON_CLICK, 0.8F, 0.8F);

                Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);

                return;
            }

            Bukkit.getScheduler().cancelTask(arena.getPlatformRushMatchProperties().getStartingTaskId());

            arena.getPlatformRushMatchProperties().resetTimer();

            arena.setState(PlatformRushMatchState.WAITING);

            sender.sendMessage(StringUtils.translate("&7[!] Stopped match start."));
            sender.playSound(sender, Sound.UI_BUTTON_CLICK, 0.8F, 0.8F);
        }
        if (accessor.equals("stop_game")) {
            if (arena.getState() != PlatformRushMatchState.GAME) {
                sender.sendMessage(StringUtils.translate("&cThe game is not started! If the game is paused, you must resume it first!"));
                return;
            }

            sender.sendMessage(StringUtils.translate("&7[Platform Rush] Stopping game..."));
            arena.getAnnouncer().sendGlobalMessage("&6" + sender.getName() + " &7is trying to STOP the game.", false);
            arena.stop();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();

        final Optional<PlatformRushPlayer> platformRushPlayerOptional = arena.getPlatformRushMiniGame().getPlayerHelper().getPlayer(player.getUniqueId())
                .map(miniGamePlayer -> (PlatformRushPlayer) miniGamePlayer);

        if (platformRushPlayerOptional.isEmpty()) return;

        if (arena.getState() != PlatformRushMatchState.WAITING) {
            player.sendMessage(arena.getPlatformRushMiniGame().getMiniMessage().deserialize("<red>You can't drop items while playing!"));
            event.setCancelled(true);
        }

        if (NBTMetadataUtil.hasString(event.getItemDrop().getItemStack(), "accessor")) event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSneak(final PlayerToggleSneakEvent event) {
        if (arena.getState() != PlatformRushMatchState.GAME) return;

        final Player player = event.getPlayer();
        final Optional<PlatformRushPlayer> platformRushPlayerOptional = arena.getPlatformRushMiniGame().getPlayerHelper().getPlayer(player.getUniqueId())
                .map(miniGamePlayer -> (PlatformRushPlayer) miniGamePlayer);

        if (platformRushPlayerOptional.isEmpty()) return;

        if (event.isSneaking()) {
            player.setGlowing(true);

            return;
        }

        player.setGlowing(false);
    }
}
