package me.mfk1016.stadtserver.listener;

import me.mfk1016.stadtserver.spells.CustomSpell;
import me.mfk1016.stadtserver.spells.SpellManager;
import me.mfk1016.stadtserver.util.Pair;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

import java.util.Optional;

public class SpellListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onApplySpellMedium(PrepareSmithingEvent event) {
        SmithingInventory table = event.getInventory();
        if (table.getInputEquipment() == null || table.getInputMineral() == null)
            return;
        ItemStack medium = table.getInputMineral();
        Optional<Pair<CustomSpell, Integer>> spell = SpellManager.getMediumSpell(medium);
        if (spell.isEmpty())
            return;

        ItemStack result = table.getInputEquipment().clone();
        if (!spell.get()._1.canEnchantItem(result))
            return;
        SpellManager.addSpell(result, spell.get()._1, spell.get()._2);
        event.getInventory().setResult(result);
        event.setResult(result);
    }
}
