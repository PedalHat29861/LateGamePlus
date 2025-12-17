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
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ErrorReporter;
import org.slf4j.Logger;

public record ReferenceLootCondition(RegistryKey<LootCondition> id) implements LootCondition
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<ReferenceLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RegistryKey.createCodec(RegistryKeys.PREDICATE).fieldOf("name").forGetter(ReferenceLootCondition::id)).apply((Applicative)instance, ReferenceLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.REFERENCE;
    }

    @Override
    public void validate(LootTableReporter reporter) {
        if (!reporter.canUseReferences()) {
            reporter.report(new LootTableReporter.ReferenceNotAllowedError(this.id));
            return;
        }
        if (reporter.isInStack(this.id)) {
            reporter.report(new LootTableReporter.RecursionError(this.id));
            return;
        }
        LootCondition.super.validate(reporter);
        reporter.getDataLookup().getOptionalEntry(this.id).ifPresentOrElse(entry -> ((LootCondition)entry.value()).validate(reporter.makeChild(new ErrorReporter.ReferenceLootTableContext(this.id), this.id)), () -> reporter.report(new LootTableReporter.MissingElementError(this.id)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean test(LootContext lootContext) {
        LootCondition lootCondition = lootContext.getLookup().getOptionalEntry(this.id).map(RegistryEntry.Reference::value).orElse(null);
        if (lootCondition == null) {
            LOGGER.warn("Tried using unknown condition table called {}", (Object)this.id.getValue());
            return false;
        }
        LootContext.Entry<LootCondition> entry = LootContext.predicate(lootCondition);
        if (lootContext.markActive(entry)) {
            try {
                boolean bl = lootCondition.test(lootContext);
                return bl;
            }
            finally {
                lootContext.markInactive(entry);
            }
        }
        LOGGER.warn("Detected infinite loop in loot tables");
        return false;
    }

    public static LootCondition.Builder builder(RegistryKey<LootCondition> key) {
        return () -> new ReferenceLootCondition(key);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ReferenceLootCondition.class, "name", "id"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ReferenceLootCondition.class, "name", "id"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ReferenceLootCondition.class, "name", "id"}, this, object);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

