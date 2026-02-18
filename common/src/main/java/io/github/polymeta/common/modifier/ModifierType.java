package io.github.polymeta.common.modifier;

public enum ModifierType
{
    SHINY(false),
    SIZE(true),
    NATURE(true),
    IV_REROLL(false),
    GENDER_SWAP(false);

    private final boolean requiresOption;

    ModifierType(boolean requiresOption)
    {
        this.requiresOption = requiresOption;
    }

    public boolean requiresOption()
    {
        return this.requiresOption;
    }

    public String getConfigKey()
    {
        switch (this)
        {
            case SHINY:
                return "shiny";
            case SIZE:
                return "size";
            case NATURE:
                return "nature";
            case IV_REROLL:
                return "ivReroll";
            case GENDER_SWAP:
                return "genderSwap";
            default:
                throw new IllegalStateException("Unknown modifier type: " + this.name());
        }
    }

    public static ModifierType fromCommandValue(String value)
    {
        if(value == null)
            return SHINY;
        String normalized = value.trim().toLowerCase();
        switch (normalized)
        {
            case "shiny":
                return SHINY;
            case "size":
                return SIZE;
            case "nature":
                return NATURE;
            case "ivr":
            case "ivreroll":
            case "iv_reroll":
                return IV_REROLL;
            case "gender":
            case "genderswap":
            case "gender_swap":
                return GENDER_SWAP;
            default:
                throw new IllegalArgumentException("Unknown modifier type: " + value);
        }
    }
}
