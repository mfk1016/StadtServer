package me.mfk1016.stadtserver.logic.wrench.actions;

import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.logic.wrench.WrenchAction;
import me.mfk1016.stadtserver.logic.InventorySorter;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

public class WrenchActionInventory extends WrenchAction {

    private String blockName;

    public WrenchActionInventory(StadtServer plugin, String blockName) {
        super(plugin);
        this.blockName = blockName;
    }

    @Override
    protected Result wrenchBlock(Player player, Block target) {
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
