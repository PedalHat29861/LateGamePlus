/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public final class NbtComponent {
    public static final NbtComponent DEFAULT = new NbtComponent(new NbtCompound());
    public static final Codec<NbtCompound> COMPOUND_CODEC = Codec.withAlternative(NbtCompound.CODEC, StringNbtReader.STRINGIFIED_CODEC);
    public static final Codec<NbtComponent> CODEC = COMPOUND_CODEC.xmap(NbtComponent::new, component -> component.nbt);
    @Deprecated
    public static final PacketCodec<ByteBuf, NbtComponent> PACKET_CODEC = PacketCodecs.NBT_COMPOUND.xmap(NbtComponent::new, component -> component.nbt);
    private final NbtCompound nbt;

    private NbtComponent(NbtCompound nbt) {
        this.nbt = nbt;
    }

    public static NbtComponent of(NbtCompound nbt) {
        return new NbtComponent(nbt.copy());
    }

    public boolean matches(NbtCompound nbt) {
        return NbtHelper.matches(nbt, this.nbt, true);
    }

    public static void set(ComponentType<NbtComponent> type, ItemStack stack, Consumer<NbtCompound> nbtSetter) {
        NbtComponent nbtComponent = stack.getOrDefault(type, DEFAULT).apply(nbtSetter);
        if (nbtComponent.nbt.isEmpty()) {
            stack.remove(type);
        } else {
            stack.set(type, nbtComponent);
        }
    }

    public static void set(ComponentType<NbtComponent> type, ItemStack stack, NbtCompound nbt) {
        if (!nbt.isEmpty()) {
            stack.set(type, NbtComponent.of(nbt));
        } else {
            stack.remove(type);
        }
    }

    public NbtComponent apply(Consumer<NbtCompound> nbtConsumer) {
        NbtCompound nbtCompound = this.nbt.copy();
        nbtConsumer.accept(nbtCompound);
        return new NbtComponent(nbtCompound);
    }

    public boolean isEmpty() {
        return this.nbt.isEmpty();
    }

    public NbtCompound copyNbt() {
        return this.nbt.copy();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof NbtComponent) {
            NbtComponent nbtComponent = (NbtComponent)o;
            return this.nbt.equals(nbtComponent.nbt);
        }
        return false;
    }

    public int hashCode() {
        return this.nbt.hashCode();
    }

    public String toString() {
        return this.nbt.toString();
    }
}

