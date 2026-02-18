package io.github.polymeta.common.GUI;

import io.github.polymeta.common.adapter.IPixelmonAdapter;
import io.github.polymeta.common.config.GeneralConfigManager;
import io.github.polymeta.common.modifier.ModifierContext;
import io.github.polymeta.common.modifier.ModifierResult;
import io.github.polymeta.common.modifier.ModifierType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;

public class PartyGUI
{
    private final Inventory inventory;
    private static final ItemStack border = ItemStack.builder()
            .itemType(ItemTypes.STAINED_GLASS_PANE)
            .add(Keys.DYE_COLOR, DyeColors.RED)
            .add(Keys.DISPLAY_NAME, Text.EMPTY)
            .build();

    private static IPixelmonAdapter adapter;
    private static Object plugin;
    private static GeneralConfigManager cMan;

    private final Player player;
    private final ModifierContext context;

    public PartyGUI(Player player, ModifierType type, String selectedValue)
    {
        this.player = player;
        this.context = new ModifierContext(type, selectedValue);

        String title = cMan.getModifierConfig(type).partyGuiTitle;
        inventory = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(
                        TextSerializers.FORMATTING_CODE.deserialize(title)))
                .listener(ClickInventoryEvent.class, e -> e.setCancelled(true))
                .listener(ClickInventoryEvent.Primary.class, this::fireClickEvent)
                .build(plugin);
        placeBorder();

        List<ItemStack> pokeParty = adapter.getPartyAsItem(this.player, context);

        if(pokeParty.get(0) != null && !pokeParty.get(0).isEmpty())
            inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(1, 1))).set(pokeParty.get(0));
        if(pokeParty.get(1) != null && !pokeParty.get(1).isEmpty())
            inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(2, 1))).set(pokeParty.get(1));
        if(pokeParty.get(2) != null && !pokeParty.get(2).isEmpty())
            inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(3, 1))).set(pokeParty.get(2));
        if(pokeParty.get(3) != null && !pokeParty.get(3).isEmpty())
            inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(5, 1))).set(pokeParty.get(3));
        if(pokeParty.get(4) != null && !pokeParty.get(4).isEmpty())
            inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(6, 1))).set(pokeParty.get(4));
        if(pokeParty.get(5) != null && !pokeParty.get(5).isEmpty())
            inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(7, 1))).set(pokeParty.get(5));
    }

    public static void initGUI(@NonNull Object _plugin, @NonNull GeneralConfigManager _cMan, @NonNull IPixelmonAdapter _adapter)
    {
        plugin = _plugin;
        adapter = _adapter;
        cMan = _cMan;
    }

    private void fireClickEvent(ClickInventoryEvent.Primary event)
    {
        event.setCancelled(true);
        if(!(event.getCause().root() instanceof Player))
            return;

        Player clickPlayer = (Player) event.getCause().root();
        SlotIndex slotIndex = event.getSlot()
                .orElseThrow(IllegalStateException::new)
                .getProperty(SlotIndex.class, SlotIndex.getDefaultKey(SlotIndex.class))
                .orElseThrow(IllegalAccessError::new);
        if(slotIndex.getValue() == null)
            return;

        int partyIndex = convertSlotToPartyIndex(slotIndex.getValue());
        if(partyIndex < 0)
            return;

        Sponge.getScheduler().createTaskBuilder().delayTicks(3L).execute(() -> {
            ModifierResult result = adapter.applyModifier(clickPlayer, partyIndex, context);
            if(result.getMessage() != null && !result.getMessage().trim().isEmpty())
            {
                clickPlayer.sendMessage(Text.of(result.isSuccess() ? TextColors.GREEN : TextColors.RED, result.getMessage()));
            }
            if(result.shouldConsumeItem())
            {
                clickPlayer.getItemInHand(HandTypes.MAIN_HAND).ifPresent(inHand ->
                        inHand.setQuantity(Math.max(0, inHand.getQuantity() - 1)));
            }
            clickPlayer.closeInventory();
        }).submit(plugin);
    }

    private int convertSlotToPartyIndex(int slotIndex)
    {
        if(slotIndex == 10) return 0;
        if(slotIndex == 11) return 1;
        if(slotIndex == 12) return 2;
        if(slotIndex == 14) return 3;
        if(slotIndex == 15) return 4;
        if(slotIndex == 16) return 5;
        return -1;
    }

    public void openInventoryOnPlayer()
    {
        this.player.openInventory(inventory);
    }

    private void placeBorder()
    {
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(0, 0))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(1, 0))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(2, 0))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(3, 0))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(4, 0))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(5, 0))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(6, 0))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(7, 0))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(8, 0))).set(border);

        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(0, 1))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(4, 1))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(8, 1))).set(border);

        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(0, 2))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(1, 2))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(2, 2))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(3, 2))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(4, 2))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(5, 2))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(6, 2))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(7, 2))).set(border);
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(8, 2))).set(border);
    }
}
