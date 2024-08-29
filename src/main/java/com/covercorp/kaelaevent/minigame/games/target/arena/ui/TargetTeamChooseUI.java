package com.covercorp.kaelaevent.minigame.games.target.arena.ui;

import com.covercorp.kaelaevent.minigame.games.target.TargetMiniGame;
import com.covercorp.kaelaevent.minigame.games.target.inventory.TargetItemCollection;
import com.covercorp.kaelaevent.minigame.games.target.player.TargetPlayer;
import com.covercorp.kaelaevent.minigame.games.target.team.TargetTeam;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.simple.LoreDisplayArray;
import com.covercorp.kaelaevent.util.simple.StringUtils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class TargetTeamChooseUI implements InventoryProvider {
    private final PlayerHelper<TargetMiniGame> playerHelper;
    private final TeamHelper<TargetMiniGame, TargetPlayer> teamHelper;

    private final int maxPlayersPerTeam;

    public TargetTeamChooseUI(final TargetMiniGame miniGame) {
        playerHelper = miniGame.getPlayerHelper();
        teamHelper = miniGame.getTeamHelper();

        maxPlayersPerTeam = miniGame.getConfigHelper().getMaxPlayersPerTeam();
    }

    @Override
    public void init(final Player player, final InventoryContents inventoryContents) {
        teamHelper.getTeamList().forEach(team -> {
            final int teamNumber = Integer.parseInt(team.getIdentifier().split("_")[1]);
            final ItemBuilder teamItemBuilder = new ItemBuilder(Material.PAPER)
                    .withName("&f" + LegacyComponentSerializer.legacyAmpersand().serialize(team.getBetterPrefix()) + "&7(" + team.getPlayers().size() + "/" + maxPlayersPerTeam + ")")
                    .withAmount(teamNumber)
                    .hideStats()
                    .hideEnchantments();

            final LoreDisplayArray<String> lore = new LoreDisplayArray<>();

            if (!team.getPlayers().isEmpty()) {
                lore.add("Team Members: ", ChatColor.GRAY);

                team.getPlayers().forEach(teamPlayer -> {
                    lore.add("&7- &f" + teamPlayer.getName(), ChatColor.GRAY);
                });
            }

            lore.add(" ");

            final Optional<TargetPlayer> gamePlayerOptional = playerHelper.getPlayer(player.getUniqueId())
                    .map(mappedPlayer -> (TargetPlayer) mappedPlayer);

            if (gamePlayerOptional.isEmpty()) {
                lore.add("&eClick to join this team &6and participate in the game&e.", ChatColor.YELLOW);

                teamItemBuilder.withLore(lore);

                final ItemStack teamItemStack = teamItemBuilder.build();

                inventoryContents.add(ClickableItem.of(teamItemStack, click -> {
                    if (team.getPlayers().size() >= maxPlayersPerTeam) {
                        player.sendMessage(StringUtils.translate("&c&lYou can't join this team right now."));
                        player.sendMessage(StringUtils.translate("&cThere's no enough space to join, if you need to"));
                        player.sendMessage(StringUtils.translate("&cparticipate, another player must leave the team."));

                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.5F,1.5F);
                        player.closeInventory();
                        return;
                    }

                    final Optional<TargetPlayer> createdGamePlayerOptional = playerHelper.getOrCreatePlayer(player)
                            .map(mappedPlayer -> (TargetPlayer) mappedPlayer);

                    if (createdGamePlayerOptional.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "An error has occurred whilst creating your player instance.");
                        player.closeInventory();

                        return;
                    }

                    final TargetPlayer createdGamePlayer = createdGamePlayerOptional.get();

                    teamHelper.addPlayerToTeam(createdGamePlayer, team.getIdentifier());

                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.2F,1.2F);
                    player.sendMessage(StringUtils.translate("&a&lYou are now participating in the next game!"));

                    TargetItemCollection.setupPlayerHotBar(createdGamePlayer);

                    player.closeInventory();
                }));

                return;
            }

            final TargetPlayer gamePlayer = gamePlayerOptional.get();
            final TargetTeam playerTeam = (TargetTeam) gamePlayer.getMiniGameTeam(); // Can be null

            if (playerTeam != null) {
                if (playerTeam.getIdentifier().equals(team.getIdentifier())) {
                    lore.add("&eYou are already on this team.", ChatColor.YELLOW);
                    lore.add(" ");
                    lore.add("&6Click to leave the team and &cnot participate &6in the next game.", ChatColor.YELLOW);

                    teamItemBuilder.setGlint(true);
                } else {
                    lore.add("&eClick to join this team.", ChatColor.YELLOW);
                }
            } else {
                lore.add("&eClick to join this team &6and participate in the game&e.", ChatColor.YELLOW);
            }

            teamItemBuilder.withLore(lore);

            final ItemStack teamItemStack = teamItemBuilder.build();

            inventoryContents.add(ClickableItem.of(teamItemStack, click -> {
                if (playerTeam != null && playerTeam.getIdentifier().equals(team.getIdentifier())) {
                    TargetItemCollection.resetPlayerHotBar(gamePlayer);

                    teamHelper.removePlayerFromTeam(gamePlayer, team.getIdentifier());
                    playerHelper.removePlayer(gamePlayer.getUniqueId());

                    player.playSound(player, Sound.BLOCK_ANVIL_BREAK, 1.2F,1.2F);
                    player.sendMessage(StringUtils.translate("&6&lYou are no longer participating in the next game."));

                    player.closeInventory();
                    return;
                }

                if (team.getPlayers().size() >= maxPlayersPerTeam) {
                    player.sendMessage(StringUtils.translate("&c&lYou can't join this team right now."));
                    player.sendMessage(StringUtils.translate("&cThere's no enough space to join, if you need to"));
                    player.sendMessage(StringUtils.translate("&cparticipate, another player must leave the team."));

                    player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.5F,1.5F);
                    player.closeInventory();
                    return;
                }

                playerHelper.removePlayer(gamePlayer.getUniqueId());

                final Optional<TargetPlayer> createdGamePlayerOptional = playerHelper.getOrCreatePlayer(player)
                        .map(mappedPlayer -> (TargetPlayer) mappedPlayer);

                if (createdGamePlayerOptional.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "An error has occurred whilst creating your player instance.");
                    player.closeInventory();

                    return;
                }

                final TargetPlayer createdGamePlayer = createdGamePlayerOptional.get();

                teamHelper.addPlayerToTeam(createdGamePlayer, team.getIdentifier());

                TargetItemCollection.setupPlayerHotBar(gamePlayer);

                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.2F,1.2F);
                player.sendMessage(StringUtils.translate("&a&lYou are now participating in the next game!"));

                player.closeInventory();
            }));
        });
    }

    @Override
    public void update(final Player player, final InventoryContents inventoryContents) {
        // Empty, for now
    }
}
