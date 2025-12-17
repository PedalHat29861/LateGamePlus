/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.inventory;

import com.mojang.serialization.Codec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

public record ContainerLock(ItemPredicate predicate) {
    public static final ContainerLock EMPTY = new ContainerLock(ItemPredicate.Builder.create().build());
    public static final Codec<ContainerLock> CODEC = ItemPredicate.CODEC.xmap(ContainerLock::new, ContainerLock::predicate);
    public static final String LOCK_KEY = "lock";

    public boolean canOpen(ItemStack stack) {
        return this.predicate.test(stack);
    }

    public void write(WriteView view) {
        if (this != EMPTY) {
            view.put(LOCK_KEY, CODEC, this);
        }
    }

    public boolean checkUnlocked(PlayerEntity player) {
        return player.isSpectator() || this.canOpen(player.getMainHandStack());
    }

    public static ContainerLock read(ReadView view) {
        return view.read(LOCK_KEY, CODEC).orElse(EMPTY);
    }
}

