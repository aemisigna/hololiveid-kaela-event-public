package com.covercorp.kaelaevent.minigame.games.tug.util;

import com.covercorp.kaelaevent.minigame.games.tug.team.TugTeam;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public final class TugMatchUtil {
    private final static List<Material> BLOCK_WHITELIST = ImmutableList.of(
            Material.ACACIA_FENCE,
            Material.ACACIA_FENCE_GATE,
            Material.BIRCH_FENCE,
            Material.BIRCH_FENCE_GATE,
            Material.DARK_OAK_FENCE,
            Material.DARK_OAK_FENCE_GATE,
            Material.OAK_FENCE, Material.OAK_FENCE_GATE,
            Material.JUNGLE_FENCE,
            Material.JUNGLE_FENCE_GATE,
            Material.NETHER_BRICK_FENCE,
            Material.SPRUCE_FENCE,
            Material.SPRUCE_FENCE_GATE,
            Material.COBBLESTONE_WALL,
            Material.SOUL_SOIL,
            Material.CACTUS,
            Material.SOUL_SAND,
            Material.CHEST,
            Material.ENDER_CHEST,
            Material.TRAPPED_CHEST,
            Material.ACACIA_TRAPDOOR,
            Material.BAMBOO_TRAPDOOR,
            Material.BIRCH_TRAPDOOR,
            Material.DARK_OAK_TRAPDOOR,
            Material.JUNGLE_TRAPDOOR,
            Material.OAK_TRAPDOOR,
            Material.SPRUCE_TRAPDOOR,
            Material.IRON_TRAPDOOR,
            Material.STONE_SLAB,
            Material.OAK_SLAB,
            Material.ACACIA_STAIRS,
            Material.BIRCH_STAIRS,
            Material.JUNGLE_STAIRS,
            Material.DARK_OAK_STAIRS,
            Material.COBBLESTONE_STAIRS,
            Material.BRICK_STAIRS,
            Material.NETHER_BRICK_STAIRS,
            Material.QUARTZ_STAIRS,
            Material.SANDSTONE_STAIRS,
            Material.RED_SANDSTONE_STAIRS,
            Material.SMOOTH_QUARTZ_STAIRS,
            Material.SMOOTH_SANDSTONE_STAIRS
    );

    public static String getVersusBar(final TugTeam team1, final TugTeam team2) {
        final String indicator = "⬛";

        final ChatColor team1Color = ChatColor.RED;
        final ChatColor team2Color = ChatColor.YELLOW;

        final String baseBar = "⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛";

        // The team that has more points will have more indicators
        final int team1Points = team1.getTeamScore();
        final int team2Points = team2.getTeamScore();

        final int pointDifference = Math.abs(team1Points - team2Points);

        // Get the point difference and the team with more points
        if (team1Points > team2Points) {
            // Team 1 has more points
            if (pointDifference >= 100) {
                // Team 1 has 100 or more points than Team 2, the bar will be all team 1 color
                return team1Color + baseBar;
            }
            if (pointDifference >= 80) {
                // Team 1 has 80 or more points than Team 2, the bar will be 9 team 1 color and 1 team 2 color
                return team1Color + baseBar.substring(0, 9) + team2Color + baseBar.substring(9, 10) + team1Color + baseBar.substring(10);
            }
            if (pointDifference >= 60) {
                // Team 1 has 60 or more points than Team 2, the bar will be 8 team 1 color and 2 team 2 color
                return team1Color + baseBar.substring(0, 8) + team2Color + baseBar.substring(8, 10) + team1Color + baseBar.substring(10);
            }
            if (pointDifference >= 40) {
                // Team 1 has 40 or more points than Team 2, the bar will be 7 team 1 color and 3 team 2 color
                return team1Color + baseBar.substring(0, 7) + team2Color + baseBar.substring(7, 10) + team1Color + baseBar.substring(10);
            }
            if (pointDifference >= 20) {
                // Team 1 has 20 or more points than Team 2, the bar will be 6 team 1 color and 4 team 2 color
                return team1Color + baseBar.substring(0, 6) + team2Color + baseBar.substring(6, 10) + team1Color + baseBar.substring(10);
            }
            if (pointDifference >= 0) {
                // Team 1 has 10 or more points than Team 2, the bar will be 5 team 1 color and 5 team 2 color
                return team1Color + baseBar.substring(0, 5) + team2Color + baseBar.substring(5, 10) + team1Color + baseBar.substring(10);
            }
        } else {
            // Team 2 has more points
            if (pointDifference >= 100) {
                return team2Color + baseBar;
            }
            // The bar must NOT be inverted, the blue still has to be on the left
            if (pointDifference >= 80) {
                return team1Color + baseBar.substring(0, 1) + team2Color + baseBar.substring(1, 10) + team1Color + baseBar.substring(10);
            }
            if (pointDifference >= 60) {
                return team1Color + baseBar.substring(0, 2) + team2Color + baseBar.substring(2, 10) + team1Color + baseBar.substring(10);
            }
            if (pointDifference >= 40) {
                return team1Color + baseBar.substring(0, 3) + team2Color + baseBar.substring(3, 10) + team1Color + baseBar.substring(10);
            }
            if (pointDifference >= 20) {
                return team1Color + baseBar.substring(0, 4) + team2Color + baseBar.substring(4, 10) + team1Color + baseBar.substring(10);
            }
            if (pointDifference >= 0) {
                return team1Color + baseBar.substring(0, 5) + team2Color +  baseBar.substring(5, 10) + team1Color + baseBar.substring(10);
            }
        }

        return "???";
    }

    public static boolean isOnGround(final Player player) {
        final Location loc = player.getLocation().clone();
        loc.setY(loc.getY() - 0.3);

        final Block in = player.getWorld().getBlockAt(loc);

        if (BLOCK_WHITELIST.contains(in.getType())) return true;

        loc.setY(loc.getY() + 0.3);

        if (Math.abs(loc.getY() - loc.getBlockY()) < 0.3) {
            loc.setY(loc.getBlockY() - 1);

            final Block block = player.getWorld().getBlockAt(loc);

            if (BLOCK_WHITELIST.contains(block.getType())) return true;

            return !block.isLiquid() && !block.isEmpty() && block.getType() != Material.AIR;
        }

        return false;
    }

    public static void moveToward(final Entity entity, final Location to, final double speed) {
        final Location loc = entity.getLocation();
        final double x = loc.getX() - to.getX();
        final double y = loc.getY() - to.getY();
        final double z = loc.getZ() - to.getZ();

        final Vector velocity = new Vector(x, y, z).normalize().multiply(-speed);

        entity.setVelocity(velocity);
    }

    public static void fuckingKillThemAlready(final Entity entity, final Location to) {
        final Location loc = entity.getLocation();
        final double x = loc.getX() - to.getX();
        final double y = loc.getY() - to.getY();
        final double z = loc.getZ() - to.getZ();

        final Vector velocity = new Vector(x, y, z).normalize().multiply(-16);

        entity.setVelocity(velocity);
    }
}
