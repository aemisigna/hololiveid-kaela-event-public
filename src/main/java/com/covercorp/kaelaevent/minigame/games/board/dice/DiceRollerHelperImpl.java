package com.covercorp.kaelaevent.minigame.games.board.dice;

import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.games.board.item.BoardItemCollection;
import com.covercorp.kaelaevent.minigame.games.board.dice.dice.BoardDice;
import com.covercorp.kaelaevent.minigame.games.board.dice.listener.BoardDiceListener;
import com.covercorp.kaelaevent.minigame.games.board.dice.type.DiceRollType;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DiceRollerHelperImpl implements DiceRollerHelper {
    private final MiniGame miniGame;

    private final Map<UUID, BoardDice> diceRollerMap;

    private final List<UUID> ticketedPlayers;

    private final static Map<Integer, String> RANDOM_EVENTS = Map.of(
            1, "Go to square N°3",
            2, "Move 3 squares FORWARD",
            3, "Swap places with nearest team in FRONT",
            4, "Swap places with nearest team BEHIND",
            5, "Move 3 squares BACKWARDS",
            6, "Go to square N°11",
            7, "Nothing happens!",
            8, "Skip ticket!"
    );

    public DiceRollerHelperImpl(final MiniGame miniGame) {
        this.miniGame = miniGame;

        diceRollerMap = new ConcurrentHashMap<>();

        ticketedPlayers = new ArrayList<>();

        miniGame.getKaelaEvent().getServer().getPluginManager().registerEvents(new BoardDiceListener(this), miniGame.getKaelaEvent());
    }

    @Override
    public void roll(final Player player, final DiceRollType diceRollType) {
        // Check if anyone is rolling
        if (!getRollers().isEmpty()) {
            player.sendMessage(miniGame.getMiniMessage().deserialize(
                    "<#ff8cbe>[!] There's already a dice rolling in the board! Please wait!"
            ));
            return;
        }

        // Spawn the dice
        final BoardDice dice = new BoardDice(player, diceRollType);
        // Cache de player
        diceRollerMap.put(player.getUniqueId(), dice);

        final Vector directionVector = player.getLocation().getDirection();
        final Vector launchVector = directionVector.clone().multiply(0.3).add(new Vector(0, 0.2, 0));

        // Move the dice
        final ArmorStand diceArmorStand = dice.getEntity();
        final Location armorStandLocation = diceArmorStand.getLocation();

        // Launch the dice
        diceArmorStand.setVelocity(launchVector);

        // Stop the dice after 10 ticks
        Bukkit.getServer().getScheduler().runTaskLater(miniGame.getKaelaEvent(), () -> {
            if (dice.isKilled()) return;

            diceArmorStand.setVelocity(new Vector(0, 0, 0));
            diceArmorStand.setGravity(false);

            // Spawn the cloud
            armorStandLocation.getWorld().spawnParticle(Particle.CLOUD, diceArmorStand.getEyeLocation(), 85, 0.5, 0.5, 0.5, 0.1);
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.playSound(player, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.0F));

            Bukkit.getServer().getScheduler().runTaskLater(miniGame.getKaelaEvent(), () -> {
                if (dice.isKilled()) return;

                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (dice.isKilled()) cancel();
                        if (ticks >= 60) {
                            switch (diceRollType) {
                                case BOARD -> {
                                    int random = new Random().nextInt(4) + 1;
                                    final ItemStack chosenItem = switch (random) {
                                        case 1 -> BoardItemCollection.ONE_DICE;
                                        case 2 -> BoardItemCollection.TWO_DICE;
                                        case 3 -> BoardItemCollection.THREE_DICE;
                                        case 4 -> BoardItemCollection.FOUR_DICE;
                                        default -> BoardItemCollection.REROLL_DICE;
                                    };

                                    player.showTitle(Title.title(
                                            miniGame.getMiniMessage().deserialize(" "),
                                            miniGame.getMiniMessage().deserialize("<gray>You got number <yellow>" + random + "<gray>!")
                                    ));
                                    Bukkit.broadcast(miniGame.getMiniMessage().deserialize("<newline><yellow><bold>" + player.getName() + "</bold> <gold>rolled the dice and got <white><bold>" + random));

                                    diceArmorStand.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, diceArmorStand.getEyeLocation(), 3, 0.1, 0.1, 0.1, 0.1);
                                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5F, 1.5F));
                                    dice.setFace(chosenItem);

                                    Bukkit.getScheduler().runTaskLater(miniGame.getKaelaEvent(), () -> {
                                        diceArmorStand.getWorld().spawnParticle(Particle.CLOUD, diceArmorStand.getEyeLocation(), 50, 0.5, 0.5, 0.5, 0.1);
                                        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.0F));

                                        cancelRoll(player);
                                    }, 40L);

                                    cancel();
                                    return;
                                }
                                case EVENT -> {
                                    int chosenEventNumber = new Random().nextInt(8) + 1;
                                    if (chosenEventNumber == 8) {
                                        if (!ticketedPlayers.contains(player.getUniqueId())) {
                                            ticketedPlayers.add(player.getUniqueId());
                                        } else {
                                            chosenEventNumber = new Random().nextInt(7) + 1;
                                        }
                                    }

                                    final String chosenEvent = RANDOM_EVENTS.get(chosenEventNumber);

                                    player.showTitle(Title.title(
                                            miniGame.getMiniMessage().deserialize("<gold><bold>Event chosen!"),
                                            miniGame.getMiniMessage().deserialize("<gray>" + chosenEvent)
                                    ));
                                    Bukkit.broadcast(miniGame.getMiniMessage().deserialize("<newline><yellow><bold>" + player.getName() + "</bold> <gold>rolled the <green><bold>EVENT</bold><gold> dice and got <white><bold>" + chosenEvent + "<newline>"));

                                    diceArmorStand.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, diceArmorStand.getEyeLocation(), 3, 0.1, 0.1, 0.1, 0.1);
                                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5F, 1.5F));
                                    dice.setFace(BoardItemCollection.ROLLED_DICE_GOLDEN_ITEM);

                                    Bukkit.getScheduler().runTaskLater(miniGame.getKaelaEvent(), () -> {
                                        diceArmorStand.getWorld().spawnParticle(Particle.CLOUD, diceArmorStand.getEyeLocation(), 50, 0.5, 0.5, 0.5, 0.1);
                                        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.0F));

                                        cancelRoll(player);
                                    }, 40L);

                                    cancel();
                                     return;
                                }
                            }
                        }
                        final EulerAngle headPose = diceArmorStand.getHeadPose();
                        diceArmorStand.setHeadPose(headPose.add(0, Math.PI / 8, 0));
                        float pitch = 0.5F + (1.5F / 60F) * ticks;
                        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.8F, pitch));
                        ticks++;
                    }
                }.runTaskTimer(miniGame.getKaelaEvent(), 1, 1);
            }, 20L);
        }, 10L);
    }

    @Override
    public void cancelRoll(final Player player) {
        if (!getRollers().contains(player.getUniqueId())) return;

        final BoardDice dice = diceRollerMap.remove(player.getUniqueId());
        dice.deSpawn();
    }

    @Override
    public ImmutableList<UUID> getRollers() {
        return ImmutableList.copyOf(diceRollerMap.keySet());
    }
}
