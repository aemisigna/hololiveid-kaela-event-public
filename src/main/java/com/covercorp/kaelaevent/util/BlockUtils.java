package com.covercorp.kaelaevent.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class BlockUtils {
    public static List<Block> getAdjacentBlocks(final Location loc) {
        final List<Block> blocks = new ArrayList<>();
        final World world = loc.getWorld();

        final int x = loc.getBlockX();
        final int y = loc.getBlockY();
        final int z = loc.getBlockZ();

        final int[][] deltas = {
                { 1, 0 },
                { -1, 0 },
                { 0, 1 },
                { 0,-1 },
                { 1, 1 },
                { 1,-1 },
                { -1, 1 },
                { -1,-1 }
        };

        blocks.add(world.getBlockAt(x, y, z));

        for (int[] delta : deltas) {
            int dx = x + delta[0];
            int dz = z + delta[1];
            blocks.add(world.getBlockAt(dx, y, dz));
        }
        return blocks;
    }
}
