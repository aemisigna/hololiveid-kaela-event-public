package com.covercorp.kaelaevent.util;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter(AccessLevel.PUBLIC)
public final class ZoneCuboid {
    private final World world;

    private final int minX;
    private final int minY;
    private final int minZ;

    private final int maxX;
    private final int maxY;
    private final int maxZ;

    public ZoneCuboid(final Location startPoint, final Location endPoint) {
        this(startPoint.getWorld(), startPoint.getBlockX(), startPoint.getBlockY(), startPoint.getBlockZ(), endPoint.getBlockX(), endPoint.getBlockY(), endPoint.getBlockZ());
    }

    public ZoneCuboid(final World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = world;

        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);

        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public boolean containsCuboid(final ZoneCuboid cuboid) {
        return cuboid.getWorld().equals(world) &&
                cuboid.getMinX() >= minX && cuboid.getMaxX() <= maxX &&
                cuboid.getMinY() >= minY && cuboid.getMaxY() <= maxY &&
                cuboid.getMaxZ() >= minZ && cuboid.getMaxZ() <= maxZ;
    }

    public boolean containsLocation(final Location location) {
        return containsXyz(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public boolean isIn(final Location loc) {
        return loc.getWorld() == this.world && loc.getBlockX() >= this.minX && loc.getBlockX() <= this.maxX && loc.getBlockY() >= this.minY && loc.getBlockY() <= this.maxY && loc
                .getBlockZ() >= this.minZ && loc.getBlockZ() <= this.maxZ;
    }

    public boolean containsXyz(final double x, final double y, final double z) {
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    public boolean overlaps(final ZoneCuboid cuboid) {
        return cuboid.getWorld().equals(world) && !(
                cuboid.getMinX() > maxX ||
                        cuboid.getMinY() > maxY ||
                        cuboid.getMinZ() > maxZ ||
                        minZ > cuboid.getMaxX() ||
                        minY > cuboid.getMaxY() ||
                        minZ > cuboid.getMaxZ());
    }

    public int getHeight() {
        return maxY - minY + 1;
    }

    public int getXWidth() {
        return maxX - minX + 1;
    }

    public int getZWidth() {
        return maxZ - minZ + 1;
    }

    public int getTotalBlockSize() {
        return getHeight() * getXWidth() * getZWidth();
    }

    public Iterator<Block> getBlockList() {
        List<Block> bL = new ArrayList<>(getTotalBlockSize());
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    final Block b = world.getBlockAt(x, y, z);
                    bL.add(b);
                }
            }
        }
        return bL.iterator();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof final ZoneCuboid cuboidComp)) return false;

        return world.equals(cuboidComp.getWorld())
                && minX == cuboidComp.getMinX()
                && minY == cuboidComp.getMinY()
                && minZ == cuboidComp.getMinZ()
                && maxX == cuboidComp.getMaxX()
                && maxY == cuboidComp.getMaxY()
                && maxZ == cuboidComp.getMaxZ();
    }

    @Override
    public String toString() {
        return "ZoneCuboid[world:" + world.getName() +
                ", minX:" + minX +
                ", minY:" + minY +
                ", minZ:" + minZ +
                ", maxX:" + maxX +
                ", maxY:" + maxY +
                ", maxZ:" + maxZ + "]";
    }
}