package io.github.polymeta.common.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Arrays;

@ConfigSerializable
public class GeneralConfig
{
    @Setting(comment = "Config for shiny modifier item")
    public ModifierItemConfig shiny = createShinyDefaults();

    @Setting(comment = "Config for size modifier item")
    public ModifierItemConfig size = createSizeDefaults();

    @Setting(comment = "Config for nature modifier item")
    public ModifierItemConfig nature = createNatureDefaults();

    @Setting(comment = "Config for IV reroll modifier item")
    public ModifierItemConfig ivReroll = createIvDefaults();

    @Setting(comment = "Config for gender swap modifier item")
    public ModifierItemConfig genderSwap = createGenderDefaults();

    private static ModifierItemConfig createShinyDefaults()
    {
        ModifierItemConfig config = new ModifierItemConfig();
        config.partyGuiTitle = "&6Troca de Shiny";
        config.itemType = "minecraft:sponge";
        config.itemName = "&eShiny Stone";
        config.itemLore = Arrays.asList("&7Troca shiny/non-shiny", "&7Clique direito para usar");
        return config;
    }

    private static ModifierItemConfig createSizeDefaults()
    {
        ModifierItemConfig config = new ModifierItemConfig();
        config.partyGuiTitle = "&6Escolha o Pokemon para tamanho";
        config.optionGuiTitle = "&6Troca de Tamanho";
        config.itemType = "minecraft:prismarine_shard";
        config.itemName = "&bSize Crystal";
        config.itemLore = Arrays.asList("&7Escolha um tamanho e depois o Pokemon", "&7Clique direito para usar");
        config.options = Arrays.asList("Microscopic", "Pygmy", "Runt", "Small", "Ordinary", "Huge", "Giant", "Enormous", "Ginormous");
        return config;
    }

    private static ModifierItemConfig createNatureDefaults()
    {
        ModifierItemConfig config = new ModifierItemConfig();
        config.partyGuiTitle = "&6Escolha o Pokemon para nature";
        config.optionGuiTitle = "&6Troca de Nature";
        config.itemType = "minecraft:book";
        config.itemName = "&aNature Scroll";
        config.itemLore = Arrays.asList("&7Escolha a nature e depois o Pokemon", "&7Clique direito para usar");
        config.options = Arrays.asList(
                "Hardy", "Lonely", "Brave", "Adamant", "Naughty",
                "Bold", "Docile", "Relaxed", "Impish", "Lax",
                "Timid", "Hasty", "Serious", "Jolly", "Naive",
                "Modest", "Mild", "Quiet", "Bashful", "Rash",
                "Calm", "Gentle", "Sassy", "Careful", "Quirky"
        );
        return config;
    }

    private static ModifierItemConfig createIvDefaults()
    {
        ModifierItemConfig config = new ModifierItemConfig();
        config.partyGuiTitle = "&6Reroll de IVs";
        config.itemType = "minecraft:blaze_powder";
        config.itemName = "&cIV Reroll";
        config.itemLore = Arrays.asList("&7Rerrola os IVs do Pokemon", "&7Clique direito para usar");
        return config;
    }

    private static ModifierItemConfig createGenderDefaults()
    {
        ModifierItemConfig config = new ModifierItemConfig();
        config.partyGuiTitle = "&6Transformador de Genero";
        config.itemType = "minecraft:chorus_fruit";
        config.itemName = "&dGender Swapper";
        config.itemLore = Arrays.asList("&7Troca para o genero oposto", "&7Sem efeito em Pokemon sem genero");
        return config;
    }
}
