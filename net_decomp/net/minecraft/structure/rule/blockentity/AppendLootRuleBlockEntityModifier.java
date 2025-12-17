/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.structure.rule.blockentity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.rule.blockentity.RuleBlockEntityModifier;
import net.minecraft.structure.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.util.math.random.Random;
import org.jspecify.annotations.Nullable;

public class AppendLootRuleBlockEntityModifier
implements RuleBlockEntityModifier {
    public static final MapCodec<AppendLootRuleBlockEntityModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootTable.TABLE_KEY.fieldOf("loot_table").forGetter(modifier -> modifier.lootTable)).apply((Applicative)instance, AppendLootRuleBlockEntityModifier::new));
    private final RegistryKey<LootTable> lootTable;

    public AppendLootRuleBlockEntityModifier(RegistryKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public NbtCompound modifyBlockEntityNbt(Random random, @Nullable NbtCompound nbt) {
        NbtCompound nbtCompound = nbt == null ? new NbtCompound() : nbt.copy();
        nbtCompound.put("LootTable", LootTable.TABLE_KEY, this.lootTable);
        nbtCompound.putLong("LootTableSeed", random.nextLong());
        return nbtCompound;
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.APPEND_LOOT;
    }
}

