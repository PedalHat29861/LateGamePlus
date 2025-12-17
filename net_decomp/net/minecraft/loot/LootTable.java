/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectListIterator
 *  org.slf4j.Logger
 */
package net.minecraft.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionConsumingBuilder;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;

public class LootTable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<RegistryKey<LootTable>> TABLE_KEY = RegistryKey.createCodec(RegistryKeys.LOOT_TABLE);
    public static final ContextType GENERIC = LootContextTypes.GENERIC;
    public static final long DEFAULT_SEED = 0L;
    public static final Codec<LootTable> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group((App)LootContextTypes.CODEC.lenientOptionalFieldOf("type", (Object)GENERIC).forGetter(table -> table.type), (App)Identifier.CODEC.optionalFieldOf("random_sequence").forGetter(table -> table.randomSequenceId), (App)LootPool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(table -> table.pools), (App)LootFunctionTypes.CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(table -> table.functions)).apply((Applicative)instance, LootTable::new)));
    public static final Codec<RegistryEntry<LootTable>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.LOOT_TABLE, CODEC);
    public static final LootTable EMPTY = new LootTable(LootContextTypes.EMPTY, Optional.empty(), List.of(), List.of());
    private final ContextType type;
    private final Optional<Identifier> randomSequenceId;
    private final List<LootPool> pools;
    private final List<LootFunction> functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunction;

    LootTable(ContextType type, Optional<Identifier> randomSequenceId, List<LootPool> pools, List<LootFunction> functions) {
        this.type = type;
        this.randomSequenceId = randomSequenceId;
        this.pools = pools;
        this.functions = functions;
        this.combinedFunction = LootFunctionTypes.join(functions);
    }

    public static Consumer<ItemStack> processStacks(ServerWorld world, Consumer<ItemStack> consumer) {
        return stack -> {
            if (!stack.isItemEnabled(world.getEnabledFeatures())) {
                return;
            }
            if (stack.getCount() < stack.getMaxCount()) {
                consumer.accept((ItemStack)stack);
            } else {
                ItemStack itemStack;
                for (int i = stack.getCount(); i > 0; i -= itemStack.getCount()) {
                    itemStack = stack.copyWithCount(Math.min(stack.getMaxCount(), i));
                    consumer.accept(itemStack);
                }
            }
        };
    }

    public void generateUnprocessedLoot(LootWorldContext parameters, Consumer<ItemStack> lootConsumer) {
        this.generateUnprocessedLoot(new LootContext.Builder(parameters).build(this.randomSequenceId), lootConsumer);
    }

    public void generateUnprocessedLoot(LootContext context, Consumer<ItemStack> lootConsumer) {
        LootContext.Entry<LootTable> entry = LootContext.table(this);
        if (context.markActive(entry)) {
            Consumer<ItemStack> consumer = LootFunction.apply(this.combinedFunction, lootConsumer, context);
            for (LootPool lootPool : this.pools) {
                lootPool.addGeneratedLoot(consumer, context);
            }
            context.markInactive(entry);
        } else {
            LOGGER.warn("Detected infinite loop in loot tables");
        }
    }

    public void generateLoot(LootWorldContext parameters, long seed, Consumer<ItemStack> lootConsumer) {
        this.generateUnprocessedLoot(new LootContext.Builder(parameters).random(seed).build(this.randomSequenceId), LootTable.processStacks(parameters.getWorld(), lootConsumer));
    }

    public void generateLoot(LootWorldContext parameters, Consumer<ItemStack> lootConsumer) {
        this.generateUnprocessedLoot(parameters, LootTable.processStacks(parameters.getWorld(), lootConsumer));
    }

    public void generateLoot(LootContext context, Consumer<ItemStack> lootConsumer) {
        this.generateUnprocessedLoot(context, LootTable.processStacks(context.getWorld(), lootConsumer));
    }

    public ObjectArrayList<ItemStack> generateLoot(LootWorldContext parameters, Random random) {
        return this.generateLoot(new LootContext.Builder(parameters).random(random).build(this.randomSequenceId));
    }

    public ObjectArrayList<ItemStack> generateLoot(LootWorldContext parameters, long seed) {
        return this.generateLoot(new LootContext.Builder(parameters).random(seed).build(this.randomSequenceId));
    }

    public ObjectArrayList<ItemStack> generateLoot(LootWorldContext parameters) {
        return this.generateLoot(new LootContext.Builder(parameters).build(this.randomSequenceId));
    }

    private ObjectArrayList<ItemStack> generateLoot(LootContext context) {
        ObjectArrayList objectArrayList = new ObjectArrayList();
        this.generateLoot(context, arg_0 -> ((ObjectArrayList)objectArrayList).add(arg_0));
        return objectArrayList;
    }

    public ContextType getType() {
        return this.type;
    }

    public void validate(LootTableReporter reporter) {
        int i;
        for (i = 0; i < this.pools.size(); ++i) {
            this.pools.get(i).validate(reporter.makeChild(new ErrorReporter.NamedListElementContext("pools", i)));
        }
        for (i = 0; i < this.functions.size(); ++i) {
            this.functions.get(i).validate(reporter.makeChild(new ErrorReporter.NamedListElementContext("functions", i)));
        }
    }

    public void supplyInventory(Inventory inventory, LootWorldContext parameters, long seed) {
        LootContext lootContext = new LootContext.Builder(parameters).random(seed).build(this.randomSequenceId);
        ObjectArrayList<ItemStack> objectArrayList = this.generateLoot(lootContext);
        Random random = lootContext.getRandom();
        List<Integer> list = this.getFreeSlots(inventory, random);
        this.spreadStacks(objectArrayList, list.size(), random);
        for (ItemStack itemStack : objectArrayList) {
            if (list.isEmpty()) {
                LOGGER.warn("Tried to over-fill a container");
                return;
            }
            if (itemStack.isEmpty()) {
                inventory.setStack(list.remove(list.size() - 1), ItemStack.EMPTY);
                continue;
            }
            inventory.setStack(list.remove(list.size() - 1), itemStack);
        }
    }

    private void spreadStacks(ObjectArrayList<ItemStack> stacks, int freeSlots, Random random) {
        ArrayList list = Lists.newArrayList();
        ObjectListIterator iterator = stacks.iterator();
        while (iterator.hasNext()) {
            ItemStack itemStack = (ItemStack)iterator.next();
            if (itemStack.isEmpty()) {
                iterator.remove();
                continue;
            }
            if (itemStack.getCount() <= 1) continue;
            list.add(itemStack);
            iterator.remove();
        }
        while (freeSlots - stacks.size() - list.size() > 0 && !list.isEmpty()) {
            ItemStack itemStack2 = (ItemStack)list.remove(MathHelper.nextInt(random, 0, list.size() - 1));
            int i = MathHelper.nextInt(random, 1, itemStack2.getCount() / 2);
            ItemStack itemStack3 = itemStack2.split(i);
            if (itemStack2.getCount() > 1 && random.nextBoolean()) {
                list.add(itemStack2);
            } else {
                stacks.add((Object)itemStack2);
            }
            if (itemStack3.getCount() > 1 && random.nextBoolean()) {
                list.add(itemStack3);
                continue;
            }
            stacks.add((Object)itemStack3);
        }
        stacks.addAll((Collection)list);
        Util.shuffle(stacks, random);
    }

    private List<Integer> getFreeSlots(Inventory inventory, Random random) {
        ObjectArrayList objectArrayList = new ObjectArrayList();
        for (int i = 0; i < inventory.size(); ++i) {
            if (!inventory.getStack(i).isEmpty()) continue;
            objectArrayList.add((Object)i);
        }
        Util.shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    implements LootFunctionConsumingBuilder<Builder> {
        private final ImmutableList.Builder<LootPool> pools = ImmutableList.builder();
        private final ImmutableList.Builder<LootFunction> functions = ImmutableList.builder();
        private ContextType type = GENERIC;
        private Optional<Identifier> randomSequenceId = Optional.empty();

        public Builder pool(LootPool.Builder poolBuilder) {
            this.pools.add((Object)poolBuilder.build());
            return this;
        }

        public Builder type(ContextType type) {
            this.type = type;
            return this;
        }

        public Builder randomSequenceId(Identifier randomSequenceId) {
            this.randomSequenceId = Optional.of(randomSequenceId);
            return this;
        }

        @Override
        public Builder apply(LootFunction.Builder builder) {
            this.functions.add((Object)builder.build());
            return this;
        }

        @Override
        public Builder getThisFunctionConsumingBuilder() {
            return this;
        }

        public LootTable build() {
            return new LootTable(this.type, this.randomSequenceId, (List<LootPool>)this.pools.build(), (List<LootFunction>)this.functions.build());
        }

        @Override
        public /* synthetic */ LootFunctionConsumingBuilder getThisFunctionConsumingBuilder() {
            return this.getThisFunctionConsumingBuilder();
        }

        @Override
        public /* synthetic */ LootFunctionConsumingBuilder apply(LootFunction.Builder function) {
            return this.apply(function);
        }
    }
}

