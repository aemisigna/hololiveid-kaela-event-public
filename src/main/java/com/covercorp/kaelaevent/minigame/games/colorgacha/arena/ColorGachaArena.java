package com.covercorp.kaelaevent.minigame.games.colorgacha.arena;

import com.covercorp.kaelaevent.minigame.announcer.Announcer;
import com.covercorp.kaelaevent.minigame.games.colorgacha.ColorGachaMiniGame;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.ButtonHelper;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.ColorGachaButton;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.state.ButtonColor;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.button.button.state.ButtonStatus;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.listener.ColorGachaMatchGameListener;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.listener.ColorGachaMatchListener;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.properties.ColorGachaMatchProperties;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.state.ColorGachaMatchState;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.task.ColorGachaFireworkTask;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.task.ColorGachaPreLobbyTask;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.task.ColorGachaTickTask;
import com.covercorp.kaelaevent.minigame.games.colorgacha.arena.task.ColorGachaTimeTask;
import com.covercorp.kaelaevent.minigame.games.colorgacha.inventory.ColorGachaItemCollection;
import com.covercorp.kaelaevent.minigame.games.colorgacha.player.ColorGachaPlayer;
import com.covercorp.kaelaevent.minigame.games.colorgacha.team.ColorGachaTeam;
import com.covercorp.kaelaevent.minigame.player.PlayerHelper;
import com.covercorp.kaelaevent.minigame.team.TeamHelper;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Getter(AccessLevel.PUBLIC)
public final class ColorGachaArena {
    private final ColorGachaMiniGame colorGachaMiniGame;

    private final PlayerHelper<ColorGachaMiniGame> playerHelper;
    private final TeamHelper<ColorGachaMiniGame, ColorGachaPlayer> teamHelper;
    private final Announcer<ColorGachaMiniGame> announcer;

    private final MiniMessage gameMiniMessage;

    private final ButtonHelper buttonHelper;

    private final Location lobbyLocation;
    private final Location scenarioWaitingLocation;
    private final Location scenarioPressingLocation;

    private final ColorGachaMatchProperties colorGachaMatchProperties;

    @Setter(AccessLevel.PUBLIC) private int gameTime;
    @Setter(AccessLevel.PUBLIC) private ColorGachaMatchState state;

    private final static int DEFAULT_CHOOSE_TIME = 10;

    @Setter(AccessLevel.PUBLIC) private int chooseTime;

    private final Queue<ColorGachaPlayer> rotationQueue;
    @Setter(AccessLevel.PUBLIC) private ColorGachaPlayer currentPlayer;

    @Setter(AccessLevel.PUBLIC) private ColorGachaButton badButton;
    @Setter(AccessLevel.PUBLIC) private boolean pressingButton;

    private ColorGachaTeam possibleWinnerTeam;

    public final static int KAELA_MODEL_NORMAL = 38;
    public final static int KAELA_MODEL_HAPPY = 39;
    public final static int KAELA_MODEL_MAD = 40;
    public final static int KAELA_MODEL_THINKING = 41;

    public final static int SCREEN_MODEL_NORMAL = 42;
    public final static int SCREEN_MODEL_CORRECT = 43;
    public final static int SCREEN_MODEL_INCORRECT = 44;

    public ColorGachaArena(final ColorGachaMiniGame colorGachaMiniGame) {
        this.colorGachaMiniGame = colorGachaMiniGame;

        playerHelper = colorGachaMiniGame.getPlayerHelper();
        teamHelper = colorGachaMiniGame.getTeamHelper();
        announcer = colorGachaMiniGame.getAnnouncer();

        gameMiniMessage = colorGachaMiniGame.getMiniMessage();
        buttonHelper = new ButtonHelper(this);

        lobbyLocation = colorGachaMiniGame.getConfigHelper().getLobbySpawn();
        scenarioWaitingLocation = colorGachaMiniGame.getConfigHelper().getScenarioWaitingSpawn();
        scenarioPressingLocation = colorGachaMiniGame.getConfigHelper().getScenarioPressingSpawn();

        colorGachaMatchProperties = new ColorGachaMatchProperties(this);

        chooseTime = DEFAULT_CHOOSE_TIME;

        rotationQueue = new LinkedList<>();

        Bukkit.getServer().getPluginManager().registerEvents(new ColorGachaMatchListener(this), getColorGachaMiniGame().getKaelaEvent());
        Bukkit.getServer().getPluginManager().registerEvents(new ColorGachaMatchGameListener(this), getColorGachaMiniGame().getKaelaEvent());

        setState(ColorGachaMatchState.WAITING);
    }

    public void start() {
        // Cancel the match start item task
        Bukkit.getScheduler().cancelTask(getColorGachaMatchProperties().getStartingTaskId());
        colorGachaMatchProperties.setStartingTaskId(0);

        setGameTime(0);

        setPressingButton(false);

        setChooseTime(DEFAULT_CHOOSE_TIME);
        getRotationQueue().clear();

        changeKaelaFace(KAELA_MODEL_THINKING);
        changeScreen(SCREEN_MODEL_NORMAL);

        getColorGachaMiniGame().getConfigHelper().getStands().forEach(standIdentifier -> {
            final Location location = getColorGachaMiniGame().getConfigHelper().getStandLocation(standIdentifier);
            final ButtonColor buttonColor = ButtonColor.valueOf(getColorGachaMiniGame().getConfigHelper().getStandColor(standIdentifier));

            final ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

            armorStand.setInvisible(true);
            armorStand.setGravity(false);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);

            NBTMetadataUtil.addStringToEntity(armorStand, "stand_accessor", standIdentifier);

            buttonHelper.addButton(new ColorGachaButton(standIdentifier, location, armorStand, buttonColor));
        });

        playerHelper.getPlayerList().forEach(participant -> {
            final Player player = Bukkit.getPlayer(participant.getUniqueId());
            if (player == null) return;

            final ColorGachaTeam team = (ColorGachaTeam) participant.getMiniGameTeam();
            if (team == null) return;

            player.teleport(getScenarioWaitingLocation());
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setFlying(false);
        });

        announcer.sendGlobalTitle(Title.title(
                gameMiniMessage.deserialize("<gold><bold>COLOR GACHA"),
                gameMiniMessage.deserialize("<gray>Get ready!"),
                Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofMillis(2000),
                        Duration.ofMillis(1000)
                )
        ));

        announcer.sendGlobalSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.5F);

        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        announcer.sendGlobalMessage("&f&lColor Gacha", true);
        announcer.sendGlobalMessage("&0 ", false);
        announcer.sendGlobalMessage("&e&lPress the button you think is safe.", true);
        announcer.sendGlobalMessage("&e&lOne of them will make Kaela very mad!", true);
        announcer.sendGlobalMessage("&e&lThis is a luck based game, so don't overthink", true);
        announcer.sendGlobalMessage("&e&lyour choice!", true);
        announcer.sendGlobalMessage("&0 ", true);
        announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

        // The task constructor has the setPreLobby(true) func
        colorGachaMatchProperties.setPreLobbyTaskId(
                Bukkit.getScheduler().runTaskTimer(colorGachaMiniGame.getKaelaEvent(), new ColorGachaPreLobbyTask(this), 0L, 20L).getTaskId()
        );
        setState(ColorGachaMatchState.ARENA_STARTING);
    }

    public void postStart() {
        // Cancel the previous task
        colorGachaMatchProperties.setPreLobby(false);
        Bukkit.getScheduler().cancelTask(colorGachaMatchProperties.getPreLobbyTaskId());

        colorGachaMatchProperties.setArenaTickTaskId(
                Bukkit.getScheduler().runTaskTimer(getColorGachaMiniGame().getKaelaEvent(), new ColorGachaTickTask(this), 0L, 1L).getTaskId()
        );
        colorGachaMatchProperties.setArenaTimeTaskId(
                Bukkit.getScheduler().runTaskTimer(getColorGachaMiniGame().getKaelaEvent(), new ColorGachaTimeTask(this), 0L, 20L).getTaskId()
        );

        // Clear the queue just in case
        rotationQueue.clear();
        // Add every talent
        final List<ColorGachaPlayer> colorGachaPlayers = new ArrayList<>(getPlayerHelper().getPlayerList().stream().map(genericPlayer -> (ColorGachaPlayer) genericPlayer).toList());

        Collections.shuffle(colorGachaPlayers);

        rotationQueue.addAll(colorGachaPlayers);

        // Make the first rotation
        rotateTalents();

        setState(ColorGachaMatchState.GAME);
    }

    public void rotateTalents() {
        // Teleport back if previous one
        if (currentPlayer != null) {
            if (!currentPlayer.isDead()) {
                final Player player = Bukkit.getPlayer(currentPlayer.getUniqueId());
                if (player != null) player.teleport(getScenarioWaitingLocation());
            }
        }

        // Check if there's any dead talents and remove them from the queue
        rotationQueue.removeIf(ColorGachaPlayer::isDead);

        changeKaelaFace(KAELA_MODEL_NORMAL);
        changeScreen(SCREEN_MODEL_NORMAL);
        announcer.sendGlobalMessage("&7The machine is looking for the next talent...", false);

        final ColorGachaPlayer chosenOne = rotationQueue.poll();
        if (chosenOne == null) {
            stop();

            announcer.sendGlobalMessage("&cThe match has been cancelled due to a failed rotation.", false);
            return;
        }

        rotationQueue.add(chosenOne);

        setCurrentPlayer(chosenOne);

        final Player player = Bukkit.getPlayer(currentPlayer.getUniqueId());
        if (player == null) {
            stop();
            announcer.sendGlobalMessage("&cThe rotation has been cancelled due to selected talent being disconnected.", false);
            return;
        }

        player.teleport(getScenarioPressingLocation());

        announcer.sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
        announcer.sendGlobalTitle(Title.title(
                Component.empty(),
                gameMiniMessage.deserialize("<yellow>Next talent is: <aqua>" + player.getName()),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)
        ));

        // Set the bad button
        final List<ColorGachaButton> availableButtons = buttonHelper.getButtons().stream().filter(button -> button.getStatus() != ButtonStatus.PRESSED).toList();
        if (availableButtons.isEmpty()) {
            announcer.sendGlobalMessage("&cThe match has been cancelled due to no buttons being available.", false);
            stop();
            return;
        }
        if (availableButtons.size() == 1) {
            buttonHelper.getButtons().forEach(resettedButton -> resettedButton.setStatus(ButtonStatus.UNPRESSED));
            announcer.sendGlobalMessage("&a[!] The buttons are now unpressed.", false);
        }

        final ColorGachaButton badButton = availableButtons.get(new Random().nextInt(availableButtons.size()));
        setBadButton(badButton);

        setPressingButton(false);
    }

    public void stop() {
        setState(ColorGachaMatchState.ENDING);

        getRotationQueue().clear();
        setChooseTime(DEFAULT_CHOOSE_TIME);

        setPressingButton(false);

        stopTasks();

        if (possibleWinnerTeam == null) {
            announcer.sendGlobalMessage(" \n&6&lThe game ended without a winner...", false);
            announcer.sendGlobalSound(Sound.ENTITY_CAT_PURREOW, 0.8F, 0.8F);
        } else {
            announcer.sendGlobalTitle(Title.title(
                    gameMiniMessage.deserialize("<green>Game ended!"),
                    gameMiniMessage.deserialize(
                            possibleWinnerTeam.getPlayers().stream().map(ColorGachaPlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")) + " <gray>won the match!"
                    ),
                    Title.Times.times(
                            Duration.ofMillis(0),
                            Duration.ofMillis(5000),
                            Duration.ofMillis(1000)
                    )
            ));

            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
            announcer.sendGlobalMessage("&e&lMatch ended!", true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&eWinner team: &f" + LegacyComponentSerializer.legacyAmpersand().serialize(possibleWinnerTeam.getBetterPrefix()), true);
            announcer.sendGlobalMessage(possibleWinnerTeam.getPlayers().stream().map(ColorGachaPlayer::getName).collect(Collectors.joining("<aqua> <white>& <aqua>")), true);
            announcer.sendGlobalMessage(" ", false);
            announcer.sendGlobalMessage("&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 0.8F));

            colorGachaMatchProperties.setFireworkTaskId(
                    Bukkit.getScheduler().runTaskTimer(getColorGachaMiniGame().getKaelaEvent(), new ColorGachaFireworkTask(this, possibleWinnerTeam), 0L, 20L).getTaskId()
            );

            Bukkit.getScheduler().runTaskLater(getColorGachaMiniGame().getKaelaEvent(), () -> {
                Bukkit.getScheduler().cancelTask(colorGachaMatchProperties.getFireworkTaskId());
                colorGachaMatchProperties.setFireworkTaskId(0);
            }, 20L * 3L);
        }

        // Remove buttons
        buttonHelper.getButtons().forEach(button -> buttonHelper.removeButton(button.getIdentifier()));

        // CLear team data
        Bukkit.getScheduler().runTaskLater(getColorGachaMiniGame().getKaelaEvent(), () -> {
            playerHelper.getPlayerList().stream().map(genericPlayer -> (ColorGachaPlayer) genericPlayer).forEach(gamePlayer -> {
                final Player player = Bukkit.getPlayer(gamePlayer.getUniqueId());
                if (player == null) return;

                player.teleport(lobbyLocation);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);

                ColorGachaItemCollection.resetPlayerHotBar(gamePlayer);

                if (gamePlayer.getMiniGameTeam() != null) teamHelper.removePlayerFromTeam(gamePlayer, gamePlayer.getMiniGameTeam().getIdentifier());

                playerHelper.removePlayer(gamePlayer.getUniqueId());
            });

            // Get all teams, clear all the players if there are and set the goals to 0
            teamHelper.getTeamList().forEach(team -> {
                if (!team.getPlayers().isEmpty()) team.getPlayers().forEach(teamPlayer -> teamHelper.removePlayerFromTeam(teamPlayer, team.getIdentifier()));
            });

            // Reset properties
            possibleWinnerTeam = null;
            colorGachaMatchProperties.resetTimer();

            changeKaelaFace(KAELA_MODEL_NORMAL);
            changeScreen(SCREEN_MODEL_NORMAL);

            setState(ColorGachaMatchState.WAITING);
        }, 120L);
    }

    public void checkWinner() {
        final List<ColorGachaTeam> aliveTeams = teamHelper.getTeamList().stream()
                .filter(ColorGachaTeam.class::isInstance)
                .map(ColorGachaTeam.class::cast)
                .filter(team -> team.getPlayers().stream().anyMatch(player -> !player.isDead()))
                .toList();

        if (aliveTeams.size() == 1) {
            possibleWinnerTeam = aliveTeams.get(0);
            stop();
        } else if (aliveTeams.isEmpty()) {
            possibleWinnerTeam = null;
            stop();
        }
    }

    private void stopTasks() {
        Bukkit.getScheduler().cancelTask(colorGachaMatchProperties.getStartingTaskId());
        colorGachaMatchProperties.setStartingTaskId(0);
        Bukkit.getScheduler().cancelTask(colorGachaMatchProperties.getPreLobbyTaskId());
        colorGachaMatchProperties.setPreLobbyTaskId(0);
        Bukkit.getScheduler().cancelTask(colorGachaMatchProperties.getArenaTickTaskId());
        colorGachaMatchProperties.setArenaTickTaskId(0);
        Bukkit.getScheduler().cancelTask(colorGachaMatchProperties.getArenaTimeTaskId());
        colorGachaMatchProperties.setArenaTimeTaskId(0);
    }

    public void changeKaelaFace(final int modelData) {
        final Location kaelaLocAprox = new Location(lobbyLocation.getWorld(), 3.5, 77.7, 0.5);
        kaelaLocAprox.getChunk().load();

        Collection<ItemDisplay> itemDisplays = kaelaLocAprox.getWorld().getNearbyEntitiesByType(ItemDisplay.class, kaelaLocAprox, 8)
                .stream().filter(itemDisplay -> {
                    final ItemStack itemStack = itemDisplay.getItemStack();
                    if (itemStack == null) return false;
                    final ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta == null) return false;

                    return itemMeta.getCustomModelData() == KAELA_MODEL_NORMAL ||
                           itemMeta.getCustomModelData() == KAELA_MODEL_HAPPY ||
                            itemMeta.getCustomModelData() == KAELA_MODEL_MAD ||
                            itemMeta.getCustomModelData() == KAELA_MODEL_THINKING;
                }).toList();
        if (!itemDisplays.isEmpty()) {
            final ItemStack itemStack = new ItemBuilder(Material.POPPED_CHORUS_FRUIT).withCustomModel(modelData).build();
            for (final ItemDisplay itemDisplay : itemDisplays) {
                itemDisplay.setItemStack(itemStack);
            }
        }
    }

    public void changeScreen(final int modelData) {
        final Location screenLocAprox = new Location(lobbyLocation.getWorld(), -0.3, 85.28803, 0.381);
        screenLocAprox.getChunk().load();

        Collection<ItemDisplay> itemDisplays = screenLocAprox.getWorld().getNearbyEntitiesByType(ItemDisplay.class, screenLocAprox, 2)
                .stream().filter(itemDisplay -> {
                    final ItemStack itemStack = itemDisplay.getItemStack();
                    if (itemStack == null) return false;
                    final ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta == null) return false;

                    return itemMeta.getCustomModelData() == SCREEN_MODEL_NORMAL ||
                            itemMeta.getCustomModelData() == SCREEN_MODEL_CORRECT ||
                            itemMeta.getCustomModelData() == SCREEN_MODEL_INCORRECT;
                }).toList();
        if (!itemDisplays.isEmpty()) {
            final ItemStack itemStack = new ItemBuilder(Material.POPPED_CHORUS_FRUIT).withCustomModel(modelData).build();
            for (final ItemDisplay itemDisplay : itemDisplays) {
                itemDisplay.setItemStack(itemStack);
            }
        }
    }
}
