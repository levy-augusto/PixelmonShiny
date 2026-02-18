package io.github.polymeta.common.config;

import com.google.common.reflect.TypeToken;
import io.github.polymeta.common.modifier.ModifierType;
import io.github.polymeta.common.utility.Utils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class GeneralConfigManager
{
    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationNode node;
    private GeneralConfig config;

    public GeneralConfigManager(ConfigurationLoader<CommentedConfigurationNode> _loader)
    {
        this.loader = _loader;
    }

    public GeneralConfig getConfig()
    {
        if(config == null)
        {
            if(this.LoadConfig())
            {
                return this.config;
            }
            else {
                System.out.print("Warning! Failed to get Config");
                return null;
            }
        }
        else return this.config;
    }

    @SuppressWarnings("UnstableApiUsage")
    public boolean LoadConfig()
    {
        try {
            this.node = loader.load();
            TypeToken<GeneralConfig> type = TypeToken.of(GeneralConfig.class);
            this.config = node.getValue(type, new GeneralConfig());
            node.setValue(type, this.config);
            this.loader.save(node);
            return true;
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings({"unused", "UnstableApiUsage"})
    public void saveConfig() {
        try {
            TypeToken<GeneralConfig> type = TypeToken.of(GeneralConfig.class);
            node.setValue(type, this.config);
            this.loader.save(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ItemStack getShinyItem()
    {
        return getModifierItem(ModifierType.SHINY);
    }

    public ItemStack getModifierItem(ModifierType type)
    {
        ModifierItemConfig itemConfig = this.getModifierConfig(type);
        return ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, itemConfig.itemType).orElse(ItemTypes.SPONGE))
                .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(itemConfig.itemName))
                .add(Keys.ITEM_LORE, Utils.deserializeLore(itemConfig.itemLore))
                .build();
    }

    public ModifierItemConfig getModifierConfig(ModifierType type)
    {
        switch (type)
        {
            case SHINY:
                return this.config.shiny;
            case SIZE:
                return this.config.size;
            case NATURE:
                return this.config.nature;
            case IV_REROLL:
                return this.config.ivReroll;
            case GENDER_SWAP:
                return this.config.genderSwap;
            default:
                throw new IllegalStateException("Unknown modifier type: " + type.name());
        }
    }

    public Optional<ModifierType> identifyModifierType(ItemStack itemStack)
    {
        for (ModifierType type : ModifierType.values())
        {
            ItemStack configured = this.getModifierItem(type);
            if(!configured.getType().equals(itemStack.getType()))
                continue;

            Optional<Text> display = itemStack.get(Keys.DISPLAY_NAME);
            if(!display.isPresent())
                continue;

            String expectedName = this.getModifierConfig(type).itemName;
            if(!TextSerializers.FORMATTING_CODE.serialize(display.get()).equalsIgnoreCase(expectedName))
                continue;

            Optional<List<Text>> lore = itemStack.get(Keys.ITEM_LORE);
            if(!lore.isPresent())
                continue;
            if(!isLoreSame(lore.get(), this.getModifierConfig(type).itemLore))
                continue;

            return Optional.of(type);
        }
        return Optional.empty();
    }

    private boolean isLoreSame(List<Text> lore1, List<String> lore2)
    {
        if (lore1.size() != lore2.size())
            return false;

        for (int i = 0; i < lore1.size(); i++)
        {
            if (!TextSerializers.FORMATTING_CODE.serialize(lore1.get(i)).equalsIgnoreCase(lore2.get(i)))
                return false;
        }
        return true;
    }
}
