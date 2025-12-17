/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.function.IntFunction;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public final class GameMode
extends Enum<GameMode>
implements StringIdentifiable {
    public static final /* enum */ GameMode SURVIVAL = new GameMode(0, "survival");
    public static final /* enum */ GameMode CREATIVE = new GameMode(1, "creative");
    public static final /* enum */ GameMode ADVENTURE = new GameMode(2, "adventure");
    public static final /* enum */ GameMode SPECTATOR = new GameMode(3, "spectator");
    public static final GameMode DEFAULT;
    public static final StringIdentifiable.EnumCodec<GameMode> CODEC;
    private static final IntFunction<GameMode> INDEX_MAPPER;
    public static final PacketCodec<ByteBuf, GameMode> PACKET_CODEC;
    @Deprecated
    public static final Codec<GameMode> INDEX_CODEC;
    private static final int UNKNOWN = -1;
    private final int index;
    private final String id;
    private final Text simpleTranslatableName;
    private final Text translatableName;
    private static final /* synthetic */ GameMode[] field_9222;

    public static GameMode[] values() {
        return (GameMode[])field_9222.clone();
    }

    public static GameMode valueOf(String string) {
        return Enum.valueOf(GameMode.class, string);
    }

    private GameMode(int index, String id) {
        this.index = index;
        this.id = id;
        this.simpleTranslatableName = Text.translatable("selectWorld.gameMode." + id);
        this.translatableName = Text.translatable("gameMode." + id);
    }

    public int getIndex() {
        return this.index;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    public Text getTranslatableName() {
        return this.translatableName;
    }

    public Text getSimpleTranslatableName() {
        return this.simpleTranslatableName;
    }

    public void setAbilities(PlayerAbilities abilities) {
        if (this == CREATIVE) {
            abilities.allowFlying = true;
            abilities.creativeMode = true;
            abilities.invulnerable = true;
        } else if (this == SPECTATOR) {
            abilities.allowFlying = true;
            abilities.creativeMode = false;
            abilities.invulnerable = true;
            abilities.flying = true;
        } else {
            abilities.allowFlying = false;
            abilities.creativeMode = false;
            abilities.invulnerable = false;
            abilities.flying = false;
        }
        abilities.allowModifyWorld = !this.isBlockBreakingRestricted();
    }

    public boolean isBlockBreakingRestricted() {
        return this == ADVENTURE || this == SPECTATOR;
    }

    public boolean isCreative() {
        return this == CREATIVE;
    }

    public boolean isSurvivalLike() {
        return this == SURVIVAL || this == ADVENTURE;
    }

    public static GameMode byIndex(int index) {
        return INDEX_MAPPER.apply(index);
    }

    public static GameMode byId(String id) {
        return GameMode.byId(id, SURVIVAL);
    }

    @Contract(value="_,!null->!null;_,null->_")
    public static @Nullable GameMode byId(String id, @Nullable GameMode fallback) {
        GameMode gameMode = CODEC.byId(id);
        return gameMode != null ? gameMode : fallback;
    }

    public static int getId(@Nullable GameMode gameMode) {
        return gameMode != null ? gameMode.index : -1;
    }

    public static @Nullable GameMode getOrNull(int index) {
        if (index == -1) {
            return null;
        }
        return GameMode.byIndex(index);
    }

    public static boolean isValid(int index) {
        return Arrays.stream(GameMode.values()).anyMatch(gameMode -> gameMode.index == index);
    }

    private static /* synthetic */ GameMode[] method_36695() {
        return new GameMode[]{SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR};
    }

    static {
        field_9222 = GameMode.method_36695();
        DEFAULT = SURVIVAL;
        CODEC = StringIdentifiable.createCodec(GameMode::values);
        INDEX_MAPPER = ValueLists.createIndexToValueFunction(GameMode::getIndex, GameMode.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, GameMode::getIndex);
        INDEX_CODEC = Codec.INT.xmap(GameMode::byIndex, GameMode::getIndex);
    }
}

