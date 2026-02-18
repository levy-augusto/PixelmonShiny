package io.github.polymeta.generations.adapter;

import io.github.polymeta.common.adapter.IPixelmonAdapter;
import io.github.polymeta.common.modifier.ModifierContext;
import io.github.polymeta.common.modifier.ModifierResult;
import io.github.polymeta.common.modifier.ModifierType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GenerationsAdapter implements IPixelmonAdapter
{
    private static final Random RANDOM = new Random();

    @Override
    public List<ItemStack> getPartyAsItem(Player player, ModifierContext context)
    {
        List<ItemStack> result = new ArrayList<>();
        try
        {
            Object[] party = getPartyPokemon(player);
            for (Object nbt : party)
            {
                if(nbt == null)
                {
                    result.add(ItemStack.empty());
                    continue;
                }

                Object nbtLink = newNBTLink(nbt);
                Object photo = invokeStatic("com.pixelmongenerations.common.item.ItemPixelmonSprite", "getPhoto", new Class<?>[]{nbtLink.getClass()}, nbtLink);
                ItemStack item = (ItemStack)(Object)photo;
                Object species = tryInvoke(nbtLink, "getSpecies");
                String speciesName = species == null ? "Pokemon" : String.valueOf(getFieldValue(species, "name"));
                item.offer(Keys.DISPLAY_NAME, Text.of(speciesName));
                item.offer(Keys.ITEM_LORE, Arrays.asList(Text.EMPTY, Text.of(getClickLore(context))));
                result.add(item);
            }
        }
        catch (Exception e)
        {
            for (int i = 0; i < 6; i++) result.add(ItemStack.empty());
        }
        while (result.size() < 6) result.add(ItemStack.empty());
        return result;
    }

    @Override
    public ModifierResult applyModifier(Player player, int partySlot, ModifierContext context)
    {
        try
        {
            Object[] party = getPartyPokemon(player);
            Object pokemonNbt = party[partySlot];
            if(pokemonNbt == null)
                return ModifierResult.fail("Esse slot esta vazio.");

            if(context.getType() == ModifierType.SHINY)
            {
                String key = getNbtKey("IS_SHINY", "isShiny");
                boolean value = (boolean) pokemonNbt.getClass().getMethod("getBoolean", String.class).invoke(pokemonNbt, key);
                pokemonNbt.getClass().getMethod("setBoolean", String.class, boolean.class).invoke(pokemonNbt, key, !value);
                return ModifierResult.success("Shiny alterado com sucesso.");
            }
            if(context.getType() == ModifierType.SIZE)
            {
                pokemonNbt.getClass().getMethod("setString", String.class, String.class).invoke(pokemonNbt, "Growth", context.getSelectedValue());
                return ModifierResult.success("Tamanho alterado para " + context.getSelectedValue() + ".");
            }
            if(context.getType() == ModifierType.NATURE)
            {
                pokemonNbt.getClass().getMethod("setString", String.class, String.class).invoke(pokemonNbt, "Nature", context.getSelectedValue());
                return ModifierResult.success("Nature alterada para " + context.getSelectedValue() + ".");
            }
            if(context.getType() == ModifierType.GENDER_SWAP)
            {
                String current = String.valueOf(pokemonNbt.getClass().getMethod("getString", String.class).invoke(pokemonNbt, "Gender"));
                if(current == null || current.trim().isEmpty())
                    return ModifierResult.fail("Pokemon sem genero. Nenhuma alteracao aplicada.");
                String normalized = current.trim().toLowerCase();
                if(normalized.contains("male"))
                {
                    pokemonNbt.getClass().getMethod("setString", String.class, String.class).invoke(pokemonNbt, "Gender", "Female");
                    return ModifierResult.success("Genero alterado para Female.");
                }
                if(normalized.contains("female"))
                {
                    pokemonNbt.getClass().getMethod("setString", String.class, String.class).invoke(pokemonNbt, "Gender", "Male");
                    return ModifierResult.success("Genero alterado para Male.");
                }
                return ModifierResult.fail("Pokemon sem genero. Nenhuma alteracao aplicada.");
            }
            if(context.getType() == ModifierType.IV_REROLL)
            {
                if(rerollByKnownKeys(pokemonNbt))
                    return ModifierResult.success("IVs rerrolados com sucesso.");
                if(rerollByReflection(pokemonNbt))
                    return ModifierResult.success("IVs rerrolados com sucesso.");
                return ModifierResult.fail("Nao foi possivel rerrolar IVs.");
            }
        }
        catch (Exception e)
        {
            return ModifierResult.fail("Falha ao aplicar modificador: " + e.getClass().getSimpleName());
        }

        return ModifierResult.fail("Modificador desconhecido.");
    }

    private Object[] getPartyPokemon(Player player) throws Exception
    {
        Class<?> storageClass = Class.forName("com.pixelmongenerations.core.storage.PixelmonStorage");
        Object manager = storageClass.getField("pokeBallManager").get(null);
        Method getPlayerStorage = manager.getClass().getMethod("getPlayerStorageFromUUID", java.util.UUID.class);
        Object optional = getPlayerStorage.invoke(manager, player.getUniqueId());
        Object playerStorage = optional.getClass().getMethod("orElseThrow", java.util.function.Supplier.class)
                .invoke(optional, (java.util.function.Supplier<RuntimeException>) NullPointerException::new);
        return (Object[]) playerStorage.getClass().getField("partyPokemon").get(playerStorage);
    }

    private Object newNBTLink(Object nbt) throws Exception
    {
        Class<?> nbtLinkClass = Class.forName("com.pixelmongenerations.common.entity.pixelmon.stats.links.NBTLink");
        Constructor<?> constructor = nbtLinkClass.getConstructor(nbt.getClass());
        return constructor.newInstance(nbt);
    }

    private String getNbtKey(String fieldName, String fallback)
    {
        try
        {
            Class<?> keyClass = Class.forName("com.pixelmongenerations.core.storage.NbtKeys");
            Field field = keyClass.getField(fieldName);
            return String.valueOf(field.get(null));
        }
        catch (Exception ignored)
        {
            return fallback;
        }
    }

    private String getClickLore(ModifierContext context)
    {
        if(context.getType() == ModifierType.SIZE) return "Clique para aplicar tamanho: " + context.getSelectedValue();
        if(context.getType() == ModifierType.NATURE) return "Clique para aplicar nature: " + context.getSelectedValue();
        if(context.getType() == ModifierType.IV_REROLL) return "Clique para rerrolar IVs";
        if(context.getType() == ModifierType.GENDER_SWAP) return "Clique para trocar para genero oposto";
        return "Clique para alternar shiny";
    }

    private Object invokeStatic(String className, String method, Class<?>[] paramTypes, Object arg) throws Exception
    {
        Class<?> clazz = Class.forName(className);
        return clazz.getMethod(method, paramTypes).invoke(null, arg);
    }

    private Object getFieldValue(Object instance, String fieldName)
    {
        try
        {
            return instance.getClass().getField(fieldName).get(instance);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    private Object tryInvoke(Object target, String... names)
    {
        for (String name : names)
        {
            try
            {
                Method method = target.getClass().getMethod(name);
                return method.invoke(target);
            }
            catch (Exception ignored)
            {
            }
        }
        return null;
    }

    private boolean rerollByKnownKeys(Object pokemonNbt) throws Exception
    {
        Method hasKey = pokemonNbt.getClass().getMethod("hasKey", String.class);
        Method setInteger = pokemonNbt.getClass().getMethod("setInteger", String.class, int.class);
        Method getCompoundTag = pokemonNbt.getClass().getMethod("getCompoundTag", String.class);
        Method setTag = pokemonNbt.getClass().getMethod("setTag", String.class, Class.forName("net.minecraft.nbt.NBTBase"));

        if((boolean) hasKey.invoke(pokemonNbt, "IVs"))
        {
            Object ivs = getCompoundTag.invoke(pokemonNbt, "IVs");
            Method ivSetInteger = ivs.getClass().getMethod("setInteger", String.class, int.class);
            ivSetInteger.invoke(ivs, "HP", RANDOM.nextInt(32));
            ivSetInteger.invoke(ivs, "Attack", RANDOM.nextInt(32));
            ivSetInteger.invoke(ivs, "Defense", RANDOM.nextInt(32));
            ivSetInteger.invoke(ivs, "SpecialAttack", RANDOM.nextInt(32));
            ivSetInteger.invoke(ivs, "SpecialDefense", RANDOM.nextInt(32));
            ivSetInteger.invoke(ivs, "Speed", RANDOM.nextInt(32));
            setTag.invoke(pokemonNbt, "IVs", ivs);
            return true;
        }

        String[] flatKeys = new String[] {"HPIV", "AtkIV", "DefIV", "SpAIV", "SpDIV", "SpeIV"};
        boolean wroteAny = false;
        for (String key : flatKeys)
        {
            if((boolean) hasKey.invoke(pokemonNbt, key))
            {
                setInteger.invoke(pokemonNbt, key, RANDOM.nextInt(32));
                wroteAny = true;
            }
        }
        return wroteAny;
    }

    private boolean rerollByReflection(Object pokemonNbt)
    {
        try
        {
            Object link = newNBTLink(pokemonNbt);
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
