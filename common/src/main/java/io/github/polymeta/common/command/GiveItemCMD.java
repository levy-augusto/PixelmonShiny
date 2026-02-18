package io.github.polymeta.common.command;

import io.github.polymeta.common.config.GeneralConfigManager;
import io.github.polymeta.common.modifier.ModifierType;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class GiveItemCMD implements CommandExecutor 
{
    private final GeneralConfigManager configManager;
    private final ModifierType forcedType;

    public GiveItemCMD(GeneralConfigManager configManager)
    {
        this(configManager, null);
    }

    public GiveItemCMD(GeneralConfigManager configManager, ModifierType forcedType)
    {
        this.configManager = configManager;
        this.forcedType = forcedType;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException 
    {
        Player player = args.<Player>getOne("player").orElseThrow(() -> new CommandException(Text.of(TextColors.RED, "Couldn't find player!")));
        int amount = args.<Integer>getOne("amount").orElse(1);
        ModifierType type;
        if(this.forcedType != null)
        {
            type = this.forcedType;
        }
        else
        {
            String itemTypeArg = args.<String>getOne("type").orElse("shiny");
            try
            {
                type = ModifierType.fromCommandValue(itemTypeArg);
            }
            catch (IllegalArgumentException ex)
            {
                throw new CommandException(Text.of(TextColors.RED, "Unknown item type. Use: shiny, size, nature, ivreroll, gender"));
            }
        }

        ItemStack modifierItem = configManager.getModifierItem(type);
        modifierItem.setQuantity(amount);
        InventoryTransactionResult result = player.getInventory().offer(modifierItem);
        if(result.getType().equals(InventoryTransactionResult.Type.SUCCESS))
        {
            src.sendMessage(Text.of(TextColors.GREEN, "Successfully gave " + amount + "x " + type.name() + " item" + (amount == 1 ? "": "s") + " to " + player.getName()));
        }
        else{
            src.sendMessage(Text.of(TextColors.RED, "Something went wrong while trying to give the item to " + player.getName() + "!"));
        }
        return CommandResult.success();
    }
}
