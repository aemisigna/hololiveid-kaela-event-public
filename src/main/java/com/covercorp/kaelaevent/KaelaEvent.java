package com.covercorp.kaelaevent;

import com.covercorp.kaelaevent.command.BackToBoardCommand;
import com.covercorp.kaelaevent.listener.CosmeticJoinListener;
import com.covercorp.kaelaevent.minigame.MiniGame;
import com.covercorp.kaelaevent.minigame.games.basketball.BasketballMiniGame;
import com.covercorp.kaelaevent.minigame.games.board.MainBoardMiniGame;
import com.covercorp.kaelaevent.minigame.games.colorgacha.ColorGachaMiniGame;
import com.covercorp.kaelaevent.minigame.games.glass.GlassMiniGame;
import com.covercorp.kaelaevent.minigame.games.lava.LavaMiniGame;
import com.covercorp.kaelaevent.minigame.games.platformrush.PlatformRushMiniGame;
import com.covercorp.kaelaevent.minigame.games.reflex.ReflexMiniGame;
import com.covercorp.kaelaevent.minigame.games.snowball.SnowballMiniGame;
import com.covercorp.kaelaevent.minigame.games.squid.SquidMiniGame;
import com.covercorp.kaelaevent.minigame.games.target.TargetMiniGame;
import com.covercorp.kaelaevent.minigame.games.tower.TowerMiniGame;
import com.covercorp.kaelaevent.minigame.games.trident.TridentMiniGame;
import com.covercorp.kaelaevent.minigame.games.tug.TugMiniGame;
import com.covercorp.kaelaevent.minigame.games.zombie.ZombieMiniGame;
import com.covercorp.kaelaevent.minigame.type.MiniGameType;

import fr.minuskube.inv.InventoryManager;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter(AccessLevel.PUBLIC)
public final class KaelaEvent extends JavaPlugin {
    @Getter(AccessLevel.PUBLIC) private static KaelaEvent kaelaEvent;

    private InventoryManager inventoryManager;

    private MiniGameType miniGameType;
    private MiniGame miniGame;

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        kaelaEvent = this;

        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        // Register bungee msg for player transfer
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getServer().getPluginManager().registerEvents(new CosmeticJoinListener(this), this);

        getServer().getPluginCommand("back").setExecutor(new BackToBoardCommand());

        miniGameType = MiniGameType.valueOf(getConfig().getString("game-type"));

        switch (miniGameType) {
            case BOARD -> miniGame = new MainBoardMiniGame(this);
            case BASKETBALL -> miniGame = new BasketballMiniGame(this);
            case PLATFORM_RUSH -> miniGame = new PlatformRushMiniGame(this);
            case TUG_OF_WAR -> miniGame = new TugMiniGame(this);
            case COLOR_GACHA -> miniGame = new ColorGachaMiniGame(this);
            case LAVA_ROOF -> miniGame = new LavaMiniGame(this);
            case TARGET_SHOOTING -> miniGame = new TargetMiniGame(this);
            case GLASS_BRIDGE -> miniGame = new GlassMiniGame(this);
            case GREEN_LIGHT_RED_LIGHT -> miniGame = new SquidMiniGame(this);
            case TRIDENT_RACE -> miniGame = new TridentMiniGame(this);
            case ZOMBIE -> miniGame = new ZombieMiniGame(this);
            case REFLEX_GAME -> miniGame = new ReflexMiniGame(this);
            case SNOWBALL_RACE -> miniGame = new SnowballMiniGame(this);
            case TOWER_OF_LUCK -> miniGame = new TowerMiniGame(this);
            default -> miniGame = null;
        }

        if (miniGame == null) {
            getLogger().severe("ERROR! Invalid game type! Please check your config.yml! The server will now shut down!");
            getServer().shutdown();
            return;
        }

        miniGame.onGameLoad();
    }

    @Override
    public void onDisable() {
        if (miniGame != null) miniGame.onGameUnload();

        inventoryManager = null;
        kaelaEvent = null;
    }
}
