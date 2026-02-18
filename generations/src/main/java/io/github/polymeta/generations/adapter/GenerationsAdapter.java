package io.github.polymeta.generations.adapter;

import com.pixelmongenerations.common.entity.pixelmon.stats.links.NBTLink;
import com.pixelmongenerations.common.item.ItemPixelmonSprite;
import com.pixelmongenerations.core.storage.NbtKeys;
import com.pixelmongenerations.core.storage.PixelmonStorage;
import com.pixelmongenerations.core.storage.PlayerStorage;
import io.github.polymeta.common.adapter.IPixelmonAdapter;
import io.github.polymeta.common.modifier.ModifierContext;
import io.github.polymeta.common.modifier.ModifierResult;
import io.github.polymeta.common.modifier.ModifierType;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GenerationsAdapter implements IPixelmonAdapter
{
    private static final Random RANDOM = new Random();

    @Override
    public List<ItemStack> getPartyAsItem(Player player, ModifierContext context)
    {
        PlayerStorage storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(player.getUniqueId()).orElseThrow(NullPointerException::new);
        return Arrays.stream(storage.partyPokemon)
                .map(nbt -> {
                    if(nbt == null)
                        return ItemStack.empty();

                    NBTLink nbtLink = new NBTLink(nbt);
                    net.minecraft.item.ItemStack photo = ItemPixelmonSprite.getPhoto(nbtLink);
                    ItemStack item = (ItemStack)(Object)photo;
                    item.offer(Keys.DISPLAY_NAME, Text.of(nbtLink.getSpecies().name));
                    item.offer(Keys.ITEM_LORE, Arrays.asList(Text.EMPTY, Text.of(getClickLore(context))));
                    return item;
                }).collect(Collectors.toList());
    }

    @Override
    public ModifierResult applyModifier(Player player, int partySlot, ModifierContext context)
    {
        PlayerStorage storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(player.getUniqueId()).orElseThrow(NullPointerException::new);
        NBTTagCompound pokemon = storage.partyPokemon[partySlot];
        if(pokemon == null)
            return ModifierResult.fail("Esse slot esta vazio.");

        if(context.getType() == ModifierType.SHINY)
        {
            pokemon.setBoolean(NbtKeys.IS_SHINY, !pokemon.getBoolean(NbtKeys.IS_SHINY));
            return ModifierResult.success("Shiny alterado com sucesso.");
        }
        if(context.getType() == ModifierType.SIZE)
        {
            pokemon.setString("Growth", context.getSelectedValue());
            return ModifierResult.success("Tamanho alterado para " + context.getSelectedValue() + ".");
        }
        if(context.getType() == ModifierType.NATURE)
        {
            pokemon.setString("Nature", context.getSelectedValue());
            return ModifierResult.success("Nature alterada para " + context.getSelectedValue() + ".");
        }
        if(context.getType() == ModifierType.GENDER_SWAP)
        {
            String current = pokemon.getString("Gender");
            if(current == null || current.trim().isEmpty())
                return ModifierResult.fail("Pokemon sem genero. Nenhuma alteracao aplicada.");

            String normalized = current.trim().toLowerCase();
            if(normalized.contains("male"))
            {
                pokemon.setString("Gender", "Female");
                return ModifierResult.success("Genero alterado para Female.");
            }
            if(normalized.contains("female"))
            {
                pokemon.setString("Gender", "Male");
                return ModifierResult.success("Genero alterado para Male.");
            }
            return ModifierResult.fail("Pokemon sem genero. Nenhuma alteracao aplicada.");
        }
        if(context.getType() == ModifierType.IV_REROLL)
        {
            if(rerollByKnownKeys(pokemon))
                return ModifierResult.success("IVs rerrolados com sucesso.");
            if(rerollByReflection(pokemon))
                return ModifierResult.success("IVs rerrolados com sucesso.");
            return ModifierResult.fail("Nao foi possivel rerrolar IVs.");
        }

        return ModifierResult.fail("Modificador desconhecido.");
    }

    private String getClickLore(ModifierContext context)
    {
        if(context.getType() == ModifierType.SIZE)
            return "Clique para aplicar tamanho: " + context.getSelectedValue();
        if(context.getType() == ModifierType.NATURE)
            return "Clique para aplicar nature: " + context.getSelectedValue();
        if(context.getType() == ModifierType.IV_REROLL)
            return "Clique para rerrolar IVs";
        if(context.getType() == ModifierType.GENDER_SWAP)
            return "Clique para trocar para genero oposto";
        return "Clique para alternar shiny";
    }

    private boolean rerollByKnownKeys(NBTTagCompound pokemon)
    {
        if(pokemon.hasKey("IVs"))
        {
            NBTTagCompound ivs = pokemon.getCompoundTag("IVs");
            ivs.setInteger("HP", RANDOM.nextInt(32));
            ivs.setInteger("Attack", RANDOM.nextInt(32));
            ivs.setInteger("Defense", RANDOM.nextInt(32));
            ivs.setInteger("SpecialAttack", RANDOM.nextInt(32));
            ivs.setInteger("SpecialDefense", RANDOM.nextInt(32));
            ivs.setInteger("Speed", RANDOM.nextInt(32));
            pokemon.setTag("IVs", ivs);
            return true;
        }

        String[] flatKeys = new String[] {"HPIV", "AtkIV", "DefIV", "SpAIV", "SpDIV", "SpeIV"};
        boolean wroteAny = false;
        for (String key : flatKeys)
        {
            if(pokemon.hasKey(key))
            {
                pokemon.setInteger(key, RANDOM.nextInt(32));
                wroteAny = true;
            }
        }
        return wroteAny;
    }

    private boolean rerollByReflection(NBTTagCompound pokemon)
    {
        try
        {
            NBTLink link = new NBTLink(pokemon);
            for (String methodName : Arrays.asList("rerollIVs", "randomizeIVs", "setRandomIVs"))
            {
                try
                {
                    Method method = link.getClass().getMethod(methodName);
                    method.invoke(link);
                    return true;
                }
                catch (NoSuchMethodException ignored)
                {
                }
            }
        }
        catch (Exception ignored)
        {
        }
        return false;
    }
}
