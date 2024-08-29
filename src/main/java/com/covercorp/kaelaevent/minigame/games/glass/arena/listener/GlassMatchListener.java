package com.covercorp.kaelaevent.minigame.games.glass.arena.listener;

import com.covercorp.kaelaevent.minigame.games.glass.arena.GlassArena;
import com.covercorp.kaelaevent.minigame.games.glass.arena.state.GlassMatchState;
import com.covercorp.kaelaevent.minigame.games.glass.arena.task.GlassStartingTask;
import com.covercorp.kaelaevent.minigame.games.glass.player.GlassPlayer;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import com.covercorp.kaelaevent.util.simple.StringUtils;
import java.util.Optional;
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

public final class GlassMatchListener implements Listener {
    private final GlassArena arena;

    public GlassMatchListener(GlassArena arena) {
        this.arena = arena;
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onInteract(PlayerInteractEvent event) {
        Player sender = event.getPlayer();
        ItemStack itemStack = sender.getInventory().getItemInMainHand();
        if (itemStack.getType() != Material.AIR) {
            if (itemStack.getType() != Material.BOW) {
                if (NBTMetadataUtil.hasString(itemStack, "accessor")) {
                    event.setCancelled(true);
                    if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                        final String accessor = NBTMetadataUtil.getString(itemStack, "accessor");
                        if (accessor == null) return;

                        if (accessor.equals("start_game")) {
                            if (this.arena.getState() != GlassMatchState.WAITING) {
                                sender.sendMessage(this.arena.getGlassMiniGame().getMiniMessage().deserialize("<red>The game is already started!"));
                                return;
                            }

                            sender.sendMessage(StringUtils.translate("&7[Glass Bridge] Starting game..."));
                            this.arena.getAnnouncer().sendGlobalMessage("&6" + sender.getName() + "&7 is trying to START the game.", false);
                            if (this.arena.getGlassMiniGame().getPlayerHelper().getPlayerList().size() < 2) {
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

                            if (!this.arena.getGlassMatchProperties().isStarting()) {
                                this.arena.getGlassMatchProperties().setStarting(true);
                                this.arena
                                        .getGlassMatchProperties()
                                        .setStartingTaskId(
                                                Bukkit.getScheduler()
                                                        .runTaskTimer(this.arena.getGlassMiniGame().getKaelaEvent(), new GlassStartingTask(this.arena), 0L, 20L)
                                                        .getTaskId()
                                        );
                                sender.sendMessage(StringUtils.translate("&7[!] Starting match..."));
                                sender.playSound(sender, Sound.UI_BUTTON_CLICK, 0.8F, 0.8F);
                                Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);
                                return;
                            }

                            Bukkit.getScheduler().cancelTask(this.arena.getGlassMatchProperties().getStartingTaskId());
                            this.arena.getGlassMatchProperties().resetTimer();
                            this.arena.setState(GlassMatchState.WAITING);
                            sender.sendMessage(StringUtils.translate("&7[!] Stopped match start."));
                            sender.playSound(sender, Sound.UI_BUTTON_CLICK, 0.8F, 0.8F);
                        }

                        if (accessor.equals("stop_game")) {
                            if (this.arena.getState() != GlassMatchState.GAME) {
                                sender.sendMessage(StringUtils.translate("&cThe game is not started! If the game is paused, you must resume it first!"));
                                return;
                            }

                            sender.sendMessage(StringUtils.translate("&7[Glass Bridge] Stopping game..."));
                            this.arena.getAnnouncer().sendGlobalMessage("&6" + sender.getName() + " &7is trying to STOP the game.", false);
                            this.arena.stop();
                        }
                    }
                }
            }
        }
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Optional<GlassPlayer> tugPlayerOptional = this.arena
                .getGlassMiniGame()
                .getPlayerHelper()
                .getPlayer(player.getUniqueId())
                .map(miniGamePlayer -> (GlassPlayer)miniGamePlayer);
        if (!tugPlayerOptional.isEmpty()) {
            if (this.arena.getState() != GlassMatchState.WAITING) {
                player.sendMessage(this.arena.getGlassMiniGame().getMiniMessage().deserialize("<red>You can't drop items while playing!"));
                event.setCancelled(true);
            }

            if (NBTMetadataUtil.hasString(event.getItemDrop().getItemStack(), "accessor")) {
                event.setCancelled(true);
            }
        }
    }
}