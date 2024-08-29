package com.covercorp.kaelaevent.minigame.games.basketball.arena.ball;

import com.covercorp.kaelaevent.entity.timed.TimedEntity;
import com.covercorp.kaelaevent.minigame.games.basketball.inventory.BasketballItemCollection;
import com.covercorp.kaelaevent.minigame.games.basketball.player.BasketballPlayer;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@Getter(AccessLevel.PUBLIC)
public final class ShootedBasketball extends TimedEntity<ArmorStand> {
    private final BasketballPlayer shooter;

    public ShootedBasketball(final BasketballPlayer shooter, final Location shootLocation) {
        super((ArmorStand) shootLocation.getWorld().spawnEntity(shootLocation, EntityType.ARMOR_STAND), 20 * 5);

        this.shooter = shooter;

        getEntity().setGravity(true);
        getEntity().setInvisible(true);
        getEntity().setInvulnerable(true);
        getEntity().addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);

        getEntity().setSmall(true);

        setRemainingTime(getBaseTime());

        setFace(BasketballItemCollection.BASKETBALL);
    }

    public void setFace(final ItemStack itemStack) {
        final EntityEquipment entityEquipment = getEntity().getEquipment();
        entityEquipment.setHelmet(itemStack);
    }

    @Override
    public void deSpawn() {
        if (!getEntity().isDead()) getEntity().remove();
        setKilled(true);

        getEntity().getLocation().getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, getEntity().getEyeLocation(), 1, 0.1, 0.1, 0.1, 0.1);
    }
}
