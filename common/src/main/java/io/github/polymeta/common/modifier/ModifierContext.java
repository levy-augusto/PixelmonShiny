package io.github.polymeta.common.modifier;

public class ModifierContext
{
    private final ModifierType type;
    private final String selectedValue;

    public ModifierContext(ModifierType type, String selectedValue)
    {
        this.type = type;
        this.selectedValue = selectedValue;
    }

    public ModifierType getType()
    {
        return type;
    }

    public String getSelectedValue()
    {
        return selectedValue;
    }
}
