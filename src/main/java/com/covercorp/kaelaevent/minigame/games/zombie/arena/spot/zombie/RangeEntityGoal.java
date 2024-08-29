package com.covercorp.kaelaevent.minigame.games.zombie.arena.spot.zombie;

import com.covercorp.kaelaevent.KaelaEvent;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

import lombok.AccessLevel;
import lombok.Getter;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@Getter(AccessLevel.PUBLIC)
public final class RangeEntityGoal implements Goal<Mob> {
    private final GoalKey<Mob> key = GoalKey.of(
            Mob.class,
            new NamespacedKey(KaelaEvent.getKaelaEvent(), "kaela_zombie")
    );

    private final Mob mob;

    private final Location point1;
    private final Location point2;

    private Location currentTarget;

    private final double speed;

    public RangeEntityGoal(final Mob mob, final Location point1, final Location point2, final double speed) {
        this.mob = mob;

        this.point1 = point1;
        this.point2 = point2;

        this.speed = speed;

        this.currentTarget = point2;
    }

    @Override
    public void tick() {
        if (mob.getLocation().distance(point1) < 2) {
            currentTarget = point2;
        } else if (mob.getLocation().distance(point2) < 2) {
            currentTarget = point1;
        }

        mob.getPathfinder().moveTo(currentTarget, speed);
    }

    @Override
    public boolean shouldActivate() {
        return true;
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }
}
