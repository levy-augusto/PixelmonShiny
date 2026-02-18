package io.github.polymeta.common.adapter;


import io.github.polymeta.common.modifier.ModifierContext;
import io.github.polymeta.common.modifier.ModifierResult;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;

public interface IPixelmonAdapter
{
    /**
     * @param player Player whose party we are looking at
     * @return Player's party in itemstack format with sprites and lore
     */
    List<ItemStack> getPartyAsItem(Player player, ModifierContext context);

    /**
     * @param player The player whose party we are looking at
     * @param partySlot the pokemon slot to modify
     * @param context contains modifier type and optional selected value
     * @return result with outcome message and whether to consume the item
     */
    ModifierResult applyModifier(Player player, int partySlot, ModifierContext context);
}
