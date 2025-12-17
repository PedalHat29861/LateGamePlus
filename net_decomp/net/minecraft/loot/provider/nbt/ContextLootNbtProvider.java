/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.loot.provider.nbt;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootEntityValueSource;
import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.loot.provider.nbt.LootNbtProviderTypes;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.util.context.ContextParameter;
import org.jspecify.annotations.Nullable;

public class ContextLootNbtProvider
implements LootNbtProvider {
    private static final Codec<LootEntityValueSource<NbtElement>> TARGET_CODEC = LootEntityValueSource.createCodec(builder -> builder.forBlockEntities(BlockEntityTarget::new).forEntities(EntityTarget::new));
    public static final MapCodec<ContextLootNbtProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TARGET_CODEC.fieldOf("target").forGetter(provider -> provider.target)).apply((Applicative)instance, ContextLootNbtProvider::new));
    public static final Codec<ContextLootNbtProvider> INLINE_CODEC = TARGET_CODEC.xmap(ContextLootNbtProvider::new, provider -> provider.target);
    private final LootEntityValueSource<NbtElement> target;

    private ContextLootNbtProvider(LootEntityValueSource<NbtElement> target) {
        this.target = target;
    }

    @Override
    public LootNbtProviderType getType() {
        return LootNbtProviderTypes.CONTEXT;
    }

    @Override
    public @Nullable NbtElement getNbt(LootContext context) {
        return this.target.get(context);
    }

    @Override
    public Set<ContextParameter<?>> getRequiredParameters() {
        return Set.of(this.target.contextParam());
    }

    public static LootNbtProvider fromTarget(LootContext.EntityReference target) {
        return new ContextLootNbtProvider(new EntityTarget(target.contextParam()));
    }

    record EntityTarget(ContextParameter<? extends Entity> contextParam) implements LootEntityValueSource.ContextComponentBased<Entity, NbtElement>
    {
        @Override
        public NbtElement get(Entity entity) {
            return NbtPredicate.entityToNbt(entity);
        }
    }

    record BlockEntityTarget(ContextParameter<? extends BlockEntity> contextParam) implements LootEntityValueSource.ContextComponentBased<BlockEntity, NbtElement>
    {
        @Override
        public NbtElement get(BlockEntity blockEntity) {
            return blockEntity.createNbtWithIdentifyingData(blockEntity.getWorld().getRegistryManager());
        }
    }
}

