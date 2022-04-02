package me.mfk1016.stadtserver.origin.trade;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.mfk1016.stadtserver.StadtServer;
import me.mfk1016.stadtserver.spells.CustomSpell;
import me.mfk1016.stadtserver.spells.SpellManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static me.mfk1016.stadtserver.util.Functions.copyRecipe;

public class WanderingTraderSpellOrigin extends WanderingTraderOrigin {

    private final CustomSpell spell;
    private final int[] chargeWeights;
    private final int[] chargeCosts;
    private final int chargeWeightsSum;

    public WanderingTraderSpellOrigin(int chance, @NotNull CustomSpell spell, int[] chargeWeights, int[] chargeCosts) {
        super(chance);
        this.spell = spell;
        this.chargeWeights = chargeWeights;
        this.chargeCosts = chargeCosts;
        assert spell.getMaxLevel() == chargeWeights.length;
        assert spell.getMaxLevel() == chargeCosts.length;
        chargeWeightsSum = Arrays.stream(chargeWeights).sum();
        assert chargeWeightsSum > 0;
    }

    protected int getSpellCharges() {
        int target = StadtServer.RANDOM.nextInt(chargeWeightsSum);
        for (int i = 0; i < chargeWeights.length; i++) {
            target -= chargeWeights[i];
            if (target < 0)
                return i + 1;
        }
        return chargeWeights.length;
    }

    @Override
    protected MerchantRecipe createRecipe(Merchant merchant, MerchantRecipe oldRecipe) {
        int charges = getSpellCharges();
        ItemStack result = SpellManager.createSpellMedium(spell, charges);
        MerchantRecipe newRecipe = copyRecipe(oldRecipe, result);
        newRecipe.setMaxUses(3);
        newRecipe.setPriceMultiplier(0.2f);
        newRecipe.setIngredients(List.of(new ItemStack(Material.EMERALD, chargeCosts[charges - 1])));
        return newRecipe;
    }

    public static WanderingTraderSpellOrigin fromJson(JsonObject object, Gson gson) {
        int chance = object.get("chance").getAsInt();
        String spell = object.get("spell").getAsString();
        int[] weights = gson.fromJson(object.getAsJsonArray("weights"), int[].class);
        int[] costs = gson.fromJson(object.getAsJsonArray("costs"), int[].class);
        CustomSpell realSpell = null;
        for (CustomSpell spe : SpellManager.ALL_SPELLS) {
            if (spe.getKey().getKey().equals(spell))
                realSpell = spe;
        }
        assert realSpell != null;
        return new WanderingTraderSpellOrigin(chance, realSpell, weights, costs);
    }
}
