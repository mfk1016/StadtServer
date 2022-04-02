package me.mfk1016.stadtserver.origin.trade;

import org.bukkit.entity.Villager;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

public abstract class VillagerOrigin extends MerchantOrigin {

    protected final int villagerLevel;

    public VillagerOrigin(int chance, int villagerLevel) {
        super(chance);
        this.villagerLevel = villagerLevel;
    }

    @Override
    protected boolean isMatched(Merchant merchant, MerchantRecipe recipe) {
        if (!(merchant instanceof Villager villager))
            return false;
        return villager.getVillagerLevel() == villagerLevel
                && matchProfession(villager)
                && super.isMatched(merchant, recipe);
    }

    protected abstract boolean matchProfession(Villager villager);
}
