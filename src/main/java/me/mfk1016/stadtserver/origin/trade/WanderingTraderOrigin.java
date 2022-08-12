package me.mfk1016.stadtserver.origin.trade;

import org.bukkit.entity.WanderingTrader;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class WanderingTraderOrigin extends MerchantOrigin {

    public WanderingTraderOrigin(int chance) {
        super(chance);
    }

    @Override
    protected boolean isApplicable(Merchant merchant, MerchantRecipe recipe) {
        return merchant instanceof WanderingTrader;
    }

    @Override
    public MerchantRecipe applyOrigin(Merchant merchant, MerchantRecipe oldRecipe) {
        List<MerchantRecipe> recipes = new ArrayList<>(merchant.getRecipes());
        recipes.add(createRecipe(oldRecipe));
        Collections.shuffle(recipes);
        merchant.setRecipes(recipes);
        return oldRecipe;
    }

    protected abstract MerchantRecipe createRecipe(MerchantRecipe oldRecipe);
}
