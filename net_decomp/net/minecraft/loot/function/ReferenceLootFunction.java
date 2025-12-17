/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ErrorReporter;
import org.slf4j.Logger;

public class ReferenceLootFunction
extends ConditionalLootFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<ReferenceLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> ReferenceLootFunction.addConditionsField(instance).and((App)RegistryKey.createCodec(RegistryKeys.ITEM_MODIFIER).fieldOf("name").forGetter(function -> function.name)).apply((Applicative)instance, ReferenceLootFunction::new));
    private final RegistryKey<LootFunction> name;

    private ReferenceLootFunction(List<LootCondition> conditions, RegistryKey<LootFunction> name) {
        super(conditions);
        this.name = name;
    }

    public LootFunctionType<ReferenceLootFunction> getType() {
        return LootFunctionTypes.REFERENCE;
    }

    @Override
    public void validate(LootTableReporter reporter) {
        if (!reporter.canUseReferences()) {
            reporter.report(new LootTableReporter.ReferenceNotAllowedError(this.name));
            return;
        }
        if (reporter.isInStack(this.name)) {
            reporter.report(new LootTableReporter.RecursionError(this.name));
            return;
        }
        super.validate(reporter);
        reporter.getDataLookup().getOptionalEntry(this.name).ifPresentOrElse(reference -> ((LootFunction)reference.value()).validate(reporter.makeChild(new ErrorReporter.ReferenceLootTableContext(this.name), this.name)), () -> reporter.report(new LootTableReporter.MissingElementError(this.name)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        LootFunction lootFunction = context.getLookup().getOptionalEntry(this.name).map(RegistryEntry::value).orElse(null);
        if (lootFunction == null) {
            LOGGER.warn("Unknown function: {}", (Object)this.name.getValue());
            return stack;
        }
        LootContext.Entry<LootFunction> entry = LootContext.itemModifier(lootFunction);
        if (context.markActive(entry)) {
            try {
                ItemStack itemStack = (ItemStack)lootFunction.apply(stack, context);
                return itemStack;
            }
            finally {
                context.markInactive(entry);
            }
        }
        LOGGER.warn("Detected infinite loop in loot tables");
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(RegistryKey<LootFunction> name) {
        return ReferenceLootFunction.builder((List<LootCondition> conditions) -> new ReferenceLootFunction((List<LootCondition>)conditions, name));
    }
}

