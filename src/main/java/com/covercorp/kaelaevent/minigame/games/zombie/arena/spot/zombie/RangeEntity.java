package com.covercorp.kaelaevent.minigame.games.zombie.arena.spot.zombie;

import com.covercorp.kaelaevent.minigame.games.zombie.arena.spot.EntitySpot;
import com.covercorp.kaelaevent.minigame.games.zombie.inventory.SkinTextureCollection;
import com.covercorp.kaelaevent.minigame.games.zombie.inventory.ZombieItemCollection;
import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.NBTMetadataUtil;

import lombok.AccessLevel;
import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

@Getter(AccessLevel.PUBLIC)
public final class RangeEntity {
    private final EntitySpot entitySpot;

    private Mob entity;

    private final static List<EntityType> ENTITY_TYPES = List.of(
            EntityType.ZOMBIE,
            EntityType.SKELETON
    );

    public RangeEntity(final EntitySpot entitySpot) {
        this.entitySpot = entitySpot;
    }

    public void spawn() {
        deSpawn();

        final Location spawnLoc = entitySpot.getStartLocation();
        entity = (Mob) spawnLoc.getWorld().spawnEntity(spawnLoc, ENTITY_TYPES.get(new Random().nextInt(ENTITY_TYPES.size())));
        if (entity instanceof Zombie) ((Zombie) entity).setAdult();

        entity.setHealth(0.1);
        entity.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
        entity.setCanPickupItems(false);
        entity.setCustomNameVisible(true);
        entity.customName(entitySpot.getArena().getGameMiniMessage().deserialize("<green>+1"));

        NBTMetadataUtil.addStringToEntity(entity, "zombiespot_id", entitySpot.getUniqueId().toString());

        // Set random talent head skin
        final String skinUrl = SkinTextureCollection.getRandomSkin();
        entity.getEquipment().setHelmet(new ItemBuilder(Material.PLAYER_HEAD).withTexture(skinUrl).build());

        // Set zombie buff scores
        final int randomNumber = new Random().nextInt(8);
        if (randomNumber == 1) {
            NBTMetadataUtil.addStringToEntity(entity, "type", "golden");
            entity.getEquipment().setChestplate(ZombieItemCollection.GOLD_CHESTPLATE);
            entity.getEquipment().setLeggings(ZombieItemCollection.GOLD_LEGGINGS);
            entity.getEquipment().setBoots(ZombieItemCollection.GOLD_BOOTS);
            entity.customName(
                    entitySpot.getArena().getGameMiniMessage().deserialize("<yellow>+3")
            );
            entity.setGlowing(true);
        }
        if (randomNumber == 2) {
            NBTMetadataUtil.addStringToEntity(entity, "type", "tnt");
            entity.getEquipment().setHelmet(ZombieItemCollection.TNT_HELMET);
        }

        double randomValue = 1.0 + (1.5 - 1.0) * new Random().nextDouble();

        Bukkit.getMobGoals().removeAllGoals(entity);
        Bukkit.getMobGoals().addGoal(entity, 0, new RangeEntityGoal(entity, spawnLoc, entitySpot.getBackLocation(), randomValue));
    }

    public void deSpawn() {
        if (!isAlive()) return;

        entity.remove();
        entity = null;
    }

    public boolean isAlive() {
        if (entity == null) return false;
        if (entity.isDead()) return false;

        return true;
    }
}
