package com.covercorp.kaelaevent.minigame.games.lava.arena.task;

import com.covercorp.kaelaevent.minigame.games.lava.arena.LavaArena;
import com.covercorp.kaelaevent.minigame.games.lava.arena.event.LavaRoundEndEvent;
import com.covercorp.kaelaevent.minigame.games.lava.arena.state.LavaMatchState;
import com.covercorp.kaelaevent.minigame.games.lava.util.LavaBarUtils;
import com.covercorp.kaelaevent.util.NegativeSpacingCollection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.util.Random;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class LavaTimeTask implements Runnable {
    private final LavaArena arena;

    private final static String FIRST_SEGMENT_BAR = "<font:kaela:boss_bar>" + "\uE006" + "</font>";
    private final static String SECOND_SEGMENT_BAR = "<font:minecraft:default>" + NegativeSpacingCollection.get(-126);
    private final static String THIRD_SEGMENT_BAR = "⌛";
    private final static String FOURTH_SEGMENT_BAR = NegativeSpacingCollection.get(2);
    private final static String FIFTH_SEGMENT_BAR = "</font>";

    @Override
    public void run() {
        if (arena.getState() != LavaMatchState.GAME) return;

        arena.setGameTime(arena.getGameTime() + 1); // Increase game time for this game

        if (arena.isShuffling()) {
            arena.getLavaMatchProperties().setShuffleTime(
                    arena.getLavaMatchProperties().getShuffleTime() - 1
            );
            if (arena.getLavaMatchProperties().getShuffleTime() <= 0) {
                this.arena.getAnnouncer().sendGlobalSound(Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 0.8f);
                arena.setShuffling(false);
                arena.getLavaMatchProperties().setShuffleTime(new Random().nextInt(5)); // set back to 5
            }
            return;
        }

        if (arena.isChecking()) return;

        // The arena is not shuffling the blocks, make the counter get down
        if (arena.getLavaMatchProperties().getBlockCooldown() > 0) {
            arena.getLavaMatchProperties().setBlockCooldown(arena.getLavaMatchProperties().getBlockCooldown() - 1);

            Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(
                    arena.getGameMiniMessage().deserialize(
                            FIRST_SEGMENT_BAR + SECOND_SEGMENT_BAR + THIRD_SEGMENT_BAR + FOURTH_SEGMENT_BAR +
                                    LavaBarUtils.getBar(arena.getLavaMatchProperties().getBlockCooldown(), arena.getRoundRunTime(), 27) +
                                    FIFTH_SEGMENT_BAR
                    )
            ));
        }

        if (arena.getLavaMatchProperties().getBlockCooldown() <= 0) {
            Bukkit.getServer().getPluginManager().callEvent(new LavaRoundEndEvent(arena));
        }
    }
}
