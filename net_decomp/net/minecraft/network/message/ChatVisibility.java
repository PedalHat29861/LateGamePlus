/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.network.message;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.text.Text;
import net.minecraft.util.function.ValueLists;

public final class ChatVisibility
extends Enum<ChatVisibility> {
    public static final /* enum */ ChatVisibility FULL = new ChatVisibility(0, "options.chat.visibility.full");
    public static final /* enum */ ChatVisibility SYSTEM = new ChatVisibility(1, "options.chat.visibility.system");
    public static final /* enum */ ChatVisibility HIDDEN = new ChatVisibility(2, "options.chat.visibility.hidden");
    private static final IntFunction<ChatVisibility> BY_ID;
    public static final Codec<ChatVisibility> CODEC;
    private final int id;
    private final Text text;
    private static final /* synthetic */ ChatVisibility[] field_7537;

    public static ChatVisibility[] values() {
        return (ChatVisibility[])field_7537.clone();
    }

    public static ChatVisibility valueOf(String string) {
        return Enum.valueOf(ChatVisibility.class, string);
    }

    private ChatVisibility(int id, String translationKey) {
        this.id = id;
        this.text = Text.translatable(translationKey);
    }

    public Text getText() {
        return this.text;
    }

    private static /* synthetic */ ChatVisibility[] method_36660() {
        return new ChatVisibility[]{FULL, SYSTEM, HIDDEN};
    }

    static {
        field_7537 = ChatVisibility.method_36660();
        BY_ID = ValueLists.createIndexToValueFunction(chatVisibility -> chatVisibility.id, ChatVisibility.values(), ValueLists.OutOfBoundsHandling.WRAP);
        CODEC = Codec.INT.xmap(BY_ID::apply, visibility -> visibility.id);
    }
}

