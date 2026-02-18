package io.github.polymeta.common.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class ModifierItemConfig
{
    @Setting(comment = "GUI title used when selecting the pokemon in party")
    public String partyGuiTitle = "&2Selecione o Pokemon";

    @Setting(comment = "Item type in modid:itemid format")
    public String itemType = "minecraft:sponge";

    @Setting(comment = "Display name of the modifier item")
    public String itemName = "Modifier Item";

    @Setting(comment = "Lore of the modifier item. Together with name it identifies valid items")
    public List<String> itemLore = Arrays.asList("Right click to use");

    @Setting(comment = "GUI title used for option selection (size/nature)")
    public String optionGuiTitle = "&3Selecione uma opcao";

    @Setting(comment = "List of options shown in option GUI (only used for size/nature)")
    public List<String> options = Arrays.asList();
}
