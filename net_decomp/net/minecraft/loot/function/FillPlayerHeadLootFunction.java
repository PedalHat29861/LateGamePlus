/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.context.ContextParameter;

public class FillPlayerHeadLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<FillPlayerHeadLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> FillPlayerHeadLootFunction.addConditionsField(instance).and((App)LootContext.EntityReference.CODEC.fieldOf("entity").forGetter(function -> function.entity)).apply((Applicative)instance, FillPlayerHeadLootFunction::new));
    private final LootContext.EntityReference entity;

    public FillPlayerHeadLootFunction(List<LootCondition> conditions, LootContext.EntityReference entity) {
        super(conditions);
        this.entity = entity;
    }

    public LootFunctionType<FillPlayerHeadLootFunction> getType() {
        return LootFunctionTypes.FILL_PLAYER_HEAD;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(this.entity.contextParam());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        Entity entity;
        if (stack.isOf(Items.PLAYER_HEAD) && (entity = context.get(this.entity.contextParam())) instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entity;
            stack.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(playerEntity.getGameProfile()));
        }
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(LootContext.EntityReference target) {
        return FillPlayerHeadLootFunction.builder((List<LootCondition> conditions) -> new FillPlayerHeadLootFunction((List<LootCondition>)conditions, target));
    }
}

