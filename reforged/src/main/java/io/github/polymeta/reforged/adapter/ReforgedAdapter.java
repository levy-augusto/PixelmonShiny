package io.github.polymeta.reforged.adapter;

import io.github.polymeta.common.adapter.IPixelmonAdapter;
import io.github.polymeta.common.modifier.ModifierContext;
import io.github.polymeta.common.modifier.ModifierResult;
import io.github.polymeta.common.modifier.ModifierType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReforgedAdapter implements IPixelmonAdapter
{
    @Override
    public List<ItemStack> getPartyAsItem(Player player, ModifierContext context)
    {
        List<ItemStack> result = new ArrayList<>();
        try
        {
            Object[] party = getPartyPokemon(player);
            for (Object pokemon : party)
            {
                if(pokemon == null)
                {
                    result.add(ItemStack.empty());
                    continue;
                }

                Object photo = invokeStatic("com.pixelmonmod.pixelmon.items.ItemPixelmonSprite", "getPhoto", new Class<?>[]{pokemon.getClass()}, pokemon);
                ItemStack item = (ItemStack)(Object)photo;
                Object species = tryInvoke(pokemon, "getSpecies");
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
            Object pokemon = party[partySlot];
            if(pokemon == null)
                return ModifierResult.fail("Esse slot esta vazio.");

            if(context.getType() == ModifierType.SHINY)
            {
                boolean shiny = (boolean) pokemon.getClass().getMethod("isShiny").invoke(pokemon);
                pokemon.getClass().getMethod("setShiny", boolean.class).invoke(pokemon, !shiny);
                return ModifierResult.success("Shiny alterado com sucesso.");
            }
            if(context.getType() == ModifierType.SIZE)
            {
                if(setWithEnum(pokemon, "setGrowth", context.getSelectedValue()))
                    return ModifierResult.success("Tamanho alterado para " + context.getSelectedValue() + ".");
                return ModifierResult.fail("Nao foi possivel alterar tamanho nessa versao.");
            }
            if(context.getType() == ModifierType.NATURE)
            {
                if(setWithEnum(pokemon, "setNature", context.getSelectedValue()))
                    return ModifierResult.success("Nature alterada para " + context.getSelectedValue() + ".");
                return ModifierResult.fail("Nao foi possivel alterar nature nessa versao.");
            }
            if(context.getType() == ModifierType.IV_REROLL)
            {
                if(tryNoArg(pokemon, "rerollIVs", "randomizeIVs", "setRandomIVs", "rollIVs"))
                    return ModifierResult.success("IVs rerrolados com sucesso.");
                Object ivs = tryInvoke(pokemon, "getIVs", "getIV");
                if(ivs != null && tryNoArg(ivs, "reroll", "randomize", "setRandom", "setRandomIVs"))
                    return ModifierResult.success("IVs rerrolados com sucesso.");
                return ModifierResult.fail("Nao foi possivel rerrolar IVs nessa versao.");
            }
            if(context.getType() == ModifierType.GENDER_SWAP)
            {
                Object gender = tryInvoke(pokemon, "getGender");
                if(gender == null)
                    return ModifierResult.fail("Nao foi possivel ler o genero.");

                Object opposite = getOppositeGender(gender);
                if(opposite == null)
                    return ModifierResult.fail("Pokemon sem genero. Nenhuma alteracao aplicada.");

                Method setGender = findSingleArgMethod(pokemon.getClass(), "setGender");
                if(setGender == null)
                    return ModifierResult.fail("Nao foi possivel alterar genero nessa versao.");
                setGender.invoke(pokemon, opposite);
                return ModifierResult.success("Genero alterado com sucesso.");
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
        Class<?> pixelmonClass = Class.forName("com.pixelmonmod.pixelmon.Pixelmon");
        Object storageManager = pixelmonClass.getField("storageManager").get(null);
        Method getParty = storageManager.getClass().getMethod("getParty", java.util.UUID.class);
        Object storage = getParty.invoke(storageManager, player.getUniqueId());
        Object all = storage.getClass().getMethod("getAll").invoke(storage);
        return (Object[]) all;
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

    private boolean setWithEnum(Object target, String methodName, String value) throws Exception
    {
        Method setter = findSingleArgMethod(target.getClass(), methodName);
        if(setter == null || !setter.getParameterTypes()[0].isEnum())
            return false;
        Object enumValue = matchEnum(setter.getParameterTypes()[0], value);
        if(enumValue == null)
            return false;
        setter.invoke(target, enumValue);
        return true;
    }

    private Object matchEnum(Class<?> enumClass, String value)
    {
        Object[] constants = enumClass.getEnumConstants();
        if(constants == null) return null;
        for (Object constant : constants)
        {
            if(((Enum<?>) constant).name().equalsIgnoreCase(value))
                return constant;
        }
        return null;
    }

    private boolean tryNoArg(Object target, String... names) throws Exception
    {
        for (String name : names)
        {
            try
            {
                Method method = target.getClass().getMethod(name);
                method.invoke(target);
                return true;
            }
            catch (NoSuchMethodException ignored)
            {
            }
        }
        return false;
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

    private Method findSingleArgMethod(Class<?> targetClass, String methodName)
    {
        for (Method method : targetClass.getMethods())
        {
            if(method.getName().equals(methodName) && method.getParameterCount() == 1)
                return method;
        }
        return null;
    }

    private Object getOppositeGender(Object currentGender)
    {
        Class<?> genderClass = currentGender.getClass();
        Object[] constants = genderClass.getEnumConstants();
        if(constants == null || constants.length == 0) return null;

        String current = ((Enum<?>) currentGender).name().toLowerCase();
        if(current.contains("male")) return getEnumByName(genderClass, "FEMALE");
        if(current.contains("female")) return getEnumByName(genderClass, "MALE");
        if(current.contains("none") || current.contains("genderless")) return null;
        return null;
    }

    private Object getEnumByName(Class<?> enumClass, String name)
    {
        Object[] constants = enumClass.getEnumConstants();
        if(constants == null) return null;
        for (Object constant : constants)
        {
            if(((Enum<?>) constant).name().equalsIgnoreCase(name))
                return constant;
        }
        return null;
    }
}
