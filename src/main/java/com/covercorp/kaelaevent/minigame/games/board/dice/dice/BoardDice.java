package com.covercorp.kaelaevent.minigame.games.board.dice.dice;

import com.covercorp.kaelaevent.entity.timed.TimedEntity;
import com.covercorp.kaelaevent.minigame.games.board.item.BoardItemCollection;
import com.covercorp.kaelaevent.minigame.games.board.dice.type.DiceRollType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class BoardDice extends TimedEntity<ArmorStand> {
    private final Player diceRoller;

    public BoardDice(final Player diceRoller, final DiceRollType rollType) {
        super((ArmorStand) diceRoller.getWorld().spawnEntity(diceRoller.getLocation(), EntityType.ARMOR_STAND), 20 * 8);

        this.diceRoller = diceRoller;

        getEntity().setGravity(true);
        getEntity().setInvisible(true);
        getEntity().setInvulnerable(true);
        getEntity().addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);

        setRemainingTime(getBaseTime());

        switch (rollType) {
            case BOARD -> setFace(BoardItemCollection.BASE_DICE_ITEM);
            case EVENT -> setFace(BoardItemCollection.BASE_DICE_GOLDEN_ITEM);
        }
    }

    public void setFace(final ItemStack itemStack) {
        final EntityEquipment entityEquipment = getEntity().getEquipment();
        entityEquipment.setHelmet(itemStack);
    }
}
