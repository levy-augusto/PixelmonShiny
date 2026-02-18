package io.github.polymeta.common.GUI;

import io.github.polymeta.common.config.GeneralConfigManager;
import io.github.polymeta.common.config.ModifierItemConfig;
import io.github.polymeta.common.modifier.ModifierType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;

public class SelectionGUI
{
    private static Object plugin;
    private static GeneralConfigManager cMan;

    private final Player player;
    private final ModifierType type;
    private final List<String> options;
    private final Inventory inventory;

    public SelectionGUI(Player player, ModifierType type)
    {
        this.player = player;
        this.type = type;
        ModifierItemConfig config = cMan.getModifierConfig(type);
        this.options = config.options;

        int rows = Math.max(1, Math.min(6, (int) Math.ceil(options.size() / 9.0D)));
        inventory = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(
                        TextSerializers.FORMATTING_CODE.deserialize(config.optionGuiTitle)))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9, rows))
                .listener(ClickInventoryEvent.class, e -> e.setCancelled(true))
                .listener(ClickInventoryEvent.Primary.class, this::onClick)
                .build(plugin);

        for (int i = 0; i < options.size(); i++)
        {
            ItemStack entry = ItemStack.builder()
                    .itemType(ItemTypes.PAPER)
                    .add(Keys.DISPLAY_NAME, Text.of(options.get(i)))
                    .build();
            inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(new SlotIndex(i))).set(entry);
        }
    }

    public static void initSelectionGUI(Object _plugin, GeneralConfigManager _cMan)
    {
        plugin = _plugin;
        cMan = _cMan;
    }

    private void onClick(ClickInventoryEvent.Primary event)
    {
        if(!(event.getCause().root() instanceof Player))
            return;
        SlotIndex slotIndex = event.getSlot()
                .orElseThrow(IllegalStateException::new)
                .getProperty(SlotIndex.class, SlotIndex.getDefaultKey(SlotIndex.class))
                .orElseThrow(IllegalAccessError::new);
        if(slotIndex.getValue() == null)
            return;

        int index = slotIndex.getValue();
        if(index < 0 || index >= options.size())
            return;

        new PartyGUI(player, type, options.get(index)).openInventoryOnPlayer();
    }

    public void openInventoryOnPlayer()
    {
        this.player.openInventory(inventory);
    }
}
