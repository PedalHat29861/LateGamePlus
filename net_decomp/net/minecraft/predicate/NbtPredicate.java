/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.predicate;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record NbtPredicate(NbtCompound nbt) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<NbtPredicate> CODEC = StringNbtReader.NBT_COMPOUND_CODEC.xmap(NbtPredicate::new, NbtPredicate::nbt);
    public static final PacketCodec<ByteBuf, NbtPredicate> PACKET_CODEC = PacketCodecs.NBT_COMPOUND.xmap(NbtPredicate::new, NbtPredicate::nbt);
    public static final String SELECTED_ITEM_KEY = "SelectedItem";

    public boolean test(ComponentsAccess components) {
        NbtComponent nbtComponent = components.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        return nbtComponent.matches(this.nbt);
    }

    public boolean test(Entity entity) {
        return this.test(NbtPredicate.entityToNbt(entity));
    }

    public boolean test(@Nullable NbtElement element) {
        return element != null && NbtHelper.matches(this.nbt, element, true);
    }

    public static NbtCompound entityToNbt(Entity entity) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getErrorReporterContext(), LOGGER);){
            PlayerEntity playerEntity;
            ItemStack itemStack;
            NbtWriteView nbtWriteView = NbtWriteView.create(logging, entity.getRegistryManager());
            entity.writeData(nbtWriteView);
            if (entity instanceof PlayerEntity && !(itemStack = (playerEntity = (PlayerEntity)entity).getInventory().getSelectedStack()).isEmpty()) {
                nbtWriteView.put(SELECTED_ITEM_KEY, ItemStack.CODEC, itemStack);
            }
            NbtCompound nbtCompound = nbtWriteView.getNbt();
            return nbtCompound;
        }
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{NbtPredicate.class, "tag", "nbt"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{NbtPredicate.class, "tag", "nbt"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{NbtPredicate.class, "tag", "nbt"}, this, object);
    }
}

