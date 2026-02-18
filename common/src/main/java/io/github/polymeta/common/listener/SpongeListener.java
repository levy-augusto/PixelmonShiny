package io.github.polymeta.common.listener;

import io.github.polymeta.common.GUI.PartyGUI;
import io.github.polymeta.common.GUI.SelectionGUI;
import io.github.polymeta.common.config.GeneralConfigManager;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class SpongeListener
{
    private final GeneralConfigManager cManager;

    public SpongeListener(GeneralConfigManager _configManager)
    {
        this.cManager = _configManager;
    }

    @Listener
    public void onRightClick(InteractBlockEvent.Secondary event, @Root Player player)
    {
        player.getItemInHand(HandTypes.MAIN_HAND).ifPresent(itemStack ->
                this.tryOpenInventory(player, itemStack, event));
    }

    @Listener
    public void onPlace(ChangeBlockEvent.Place event, @Root Player player)
    {
        player.getItemInHand(HandTypes.MAIN_HAND).ifPresent(itemStack ->
                this.tryOpenInventory(player, itemStack, event));
    }

    private void tryOpenInventory(Player player, org.spongepowered.api.item.inventory.ItemStack itemStack, org.spongepowered.api.event.Event event)
    {
        cManager.identifyModifierType(itemStack).ifPresent(type -> {
            if(type.requiresOption())
            {
                new SelectionGUI(player, type).openInventoryOnPlayer();
            }
            else
            {
                new PartyGUI(player, type, null).openInventoryOnPlayer();
            }
            if(event instanceof InteractBlockEvent.Secondary)
            {
                ((InteractBlockEvent.Secondary) event).setCancelled(true);
            }
            if(event instanceof ChangeBlockEvent.Place)
            {
                ((ChangeBlockEvent.Place) event).setCancelled(true);
            }
        });
    }
}
