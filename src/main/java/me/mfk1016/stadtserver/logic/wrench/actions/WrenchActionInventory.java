package me.mfk1016.stadtserver.logic.wrench.actions;

import me.mfk1016.stadtserver.brewing.BarrelManager;
import me.mfk1016.stadtserver.logic.InventorySorter;
import me.mfk1016.stadtserver.logic.wrench.WrenchAction;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.Lootable;

public class WrenchActionInventory extends WrenchAction {

    private String blockName;

    public WrenchActionInventory(String blockName) {
        this.blockName = blockName;
    }

    @Override
    protected Result wrenchBlock(Player player, Block target) {
        if (BarrelManager.isFluidBarrel(target))
            return Result.FALSE;
        if (target.getState() instanceof Lootable lootable) {
            if (lootable.hasLootTable())
                return Result.FALSE;
        }
        if (!(target.getState() instanceof Container container))
            return Result.FALSE;
        Inventory inv = container.getInventory();
        if (inv instanceof DoubleChestInventory dcInv) {
            InventorySorter.sortDoubleChestInventory(dcInv.getLeftSide(), dcInv.getRightSide());
            blockName = "Double chest";
        } else {
            InventorySorter.sortInventory(inv);
        }
        return Result.TRUE;
    }

    @Override
    protected String wrenchMessage(Result result) {
        if (result == Result.TRUE)
            return blockName + " sorted";
        else
            return null;
    }

    @Override
    protected boolean isEventCancelled(Result result) {
        return result == Result.TRUE;
    }
}
