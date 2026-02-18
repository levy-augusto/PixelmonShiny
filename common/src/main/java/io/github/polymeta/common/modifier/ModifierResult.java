package io.github.polymeta.common.modifier;

public class ModifierResult
{
    private final boolean success;
    private final boolean consumeItem;
    private final String message;

    public ModifierResult(boolean success, boolean consumeItem, String message)
    {
        this.success = success;
        this.consumeItem = consumeItem;
        this.message = message;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public boolean shouldConsumeItem()
    {
        return consumeItem;
    }

    public String getMessage()
    {
        return message;
    }

    public static ModifierResult success(String message)
    {
        return new ModifierResult(true, true, message);
    }

    public static ModifierResult fail(String message)
    {
        return new ModifierResult(false, false, message);
    }
}
