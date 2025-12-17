/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancement;

import com.mojang.serialization.Codec;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

public final class AdvancementFrame
extends Enum<AdvancementFrame>
implements StringIdentifiable {
    public static final /* enum */ AdvancementFrame TASK = new AdvancementFrame("task", Formatting.GREEN);
    public static final /* enum */ AdvancementFrame CHALLENGE = new AdvancementFrame("challenge", Formatting.DARK_PURPLE);
    public static final /* enum */ AdvancementFrame GOAL = new AdvancementFrame("goal", Formatting.GREEN);
    public static final Codec<AdvancementFrame> CODEC;
    private final String id;
    private final Formatting titleFormat;
    private final Text toastText;
    private static final /* synthetic */ AdvancementFrame[] field_1253;

    public static AdvancementFrame[] values() {
        return (AdvancementFrame[])field_1253.clone();
    }

    public static AdvancementFrame valueOf(String string) {
        return Enum.valueOf(AdvancementFrame.class, string);
    }

    private AdvancementFrame(String id, Formatting titleFormat) {
        this.id = id;
        this.titleFormat = titleFormat;
        this.toastText = Text.translatable("advancements.toast." + id);
    }

    public Formatting getTitleFormat() {
        return this.titleFormat;
    }

    public Text getToastText() {
        return this.toastText;
    }

    @Override
    public String asString() {
        return this.id;
    }

    public MutableText getChatAnnouncementText(AdvancementEntry advancementEntry, ServerPlayerEntity player) {
        return Text.translatable("chat.type.advancement." + this.id, player.getDisplayName(), Advancement.getNameFromIdentity(advancementEntry));
    }

    private static /* synthetic */ AdvancementFrame[] method_36593() {
        return new AdvancementFrame[]{TASK, CHALLENGE, GOAL};
    }

    static {
        field_1253 = AdvancementFrame.method_36593();
        CODEC = StringIdentifiable.createCodec(AdvancementFrame::values);
    }
}

