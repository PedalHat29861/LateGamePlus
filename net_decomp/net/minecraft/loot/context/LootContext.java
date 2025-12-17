/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.loot.context;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootEntityValueSource;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.math.random.Random;
import org.jspecify.annotations.Nullable;

public class LootContext {
    private final LootWorldContext worldContext;
    private final Random random;
    private final RegistryEntryLookup.RegistryLookup lookup;
    private final Set<Entry<?>> activeEntries = Sets.newLinkedHashSet();

    LootContext(LootWorldContext worldContext, Random random, RegistryEntryLookup.RegistryLookup lookup) {
        this.worldContext = worldContext;
        this.random = random;
        this.lookup = lookup;
    }

    public boolean hasParameter(ContextParameter<?> parameter) {
        return this.worldContext.getParameters().contains(parameter);
    }

    public <T> T getOrThrow(ContextParameter<T> parameter) {
        return this.worldContext.getParameters().getOrThrow(parameter);
    }

    public <T> @Nullable T get(ContextParameter<T> parameter) {
        return this.worldContext.getParameters().getNullable(parameter);
    }

    public void drop(Identifier id, Consumer<ItemStack> lootConsumer) {
        this.worldContext.addDynamicDrops(id, lootConsumer);
    }

    public boolean isActive(Entry<?> entry) {
        return this.activeEntries.contains(entry);
    }

    public boolean markActive(Entry<?> entry) {
        return this.activeEntries.add(entry);
    }

    public void markInactive(Entry<?> entry) {
        this.activeEntries.remove(entry);
    }

    public RegistryEntryLookup.RegistryLookup getLookup() {
        return this.lookup;
    }

    public Random getRandom() {
        return this.random;
    }

    public float getLuck() {
        return this.worldContext.getLuck();
    }

    public ServerWorld getWorld() {
        return this.worldContext.getWorld();
    }

    public static Entry<LootTable> table(LootTable table) {
        return new Entry<LootTable>(LootDataType.LOOT_TABLES, table);
    }

    public static Entry<LootCondition> predicate(LootCondition predicate) {
        return new Entry<LootCondition>(LootDataType.PREDICATES, predicate);
    }

    public static Entry<LootFunction> itemModifier(LootFunction itemModifier) {
        return new Entry<LootFunction>(LootDataType.ITEM_MODIFIERS, itemModifier);
    }

    public record Entry<T>(LootDataType<T> type, T value) {
    }

    public static final class ItemStackReference
    extends Enum<ItemStackReference>
    implements StringIdentifiable,
    LootEntityValueSource.ContextBased<ItemStack> {
        public static final /* enum */ ItemStackReference TOOL = new ItemStackReference("tool", LootContextParameters.TOOL);
        private final String id;
        private final ContextParameter<? extends ItemStack> parameter;
        private static final /* synthetic */ ItemStackReference[] field_63057;

        public static ItemStackReference[] values() {
            return (ItemStackReference[])field_63057.clone();
        }

        public static ItemStackReference valueOf(String string) {
            return Enum.valueOf(ItemStackReference.class, string);
        }

        private ItemStackReference(String id, ContextParameter<? extends ItemStack> parameter) {
            this.id = id;
            this.parameter = parameter;
        }

        @Override
        public ContextParameter<? extends ItemStack> contextParam() {
            return this.parameter;
        }

        @Override
        public String asString() {
            return this.id;
        }

        private static /* synthetic */ ItemStackReference[] method_74900() {
            return new ItemStackReference[]{TOOL};
        }

        static {
            field_63057 = ItemStackReference.method_74900();
        }
    }

    public static final class BlockEntityReference
    extends Enum<BlockEntityReference>
    implements StringIdentifiable,
    LootEntityValueSource.ContextBased<BlockEntity> {
        public static final /* enum */ BlockEntityReference BLOCK_ENTITY = new BlockEntityReference("block_entity", LootContextParameters.BLOCK_ENTITY);
        private final String id;
        private final ContextParameter<? extends BlockEntity> parameter;
        private static final /* synthetic */ BlockEntityReference[] field_49439;

        public static BlockEntityReference[] values() {
            return (BlockEntityReference[])field_49439.clone();
        }

        public static BlockEntityReference valueOf(String string) {
            return Enum.valueOf(BlockEntityReference.class, string);
        }

        private BlockEntityReference(String id, ContextParameter<? extends BlockEntity> parameter) {
            this.id = id;
            this.parameter = parameter;
        }

        @Override
        public ContextParameter<? extends BlockEntity> contextParam() {
            return this.parameter;
        }

        @Override
        public String asString() {
            return this.id;
        }

        private static /* synthetic */ BlockEntityReference[] method_57645() {
            return new BlockEntityReference[]{BLOCK_ENTITY};
        }

        static {
            field_49439 = BlockEntityReference.method_57645();
        }
    }

    public static final class EntityReference
    extends Enum<EntityReference>
    implements StringIdentifiable,
    LootEntityValueSource.ContextBased<Entity> {
        public static final /* enum */ EntityReference THIS = new EntityReference("this", LootContextParameters.THIS_ENTITY);
        public static final /* enum */ EntityReference ATTACKER = new EntityReference("attacker", LootContextParameters.ATTACKING_ENTITY);
        public static final /* enum */ EntityReference DIRECT_ATTACKER = new EntityReference("direct_attacker", LootContextParameters.DIRECT_ATTACKING_ENTITY);
        public static final /* enum */ EntityReference ATTACKING_PLAYER = new EntityReference("attacking_player", LootContextParameters.LAST_DAMAGE_PLAYER);
        public static final /* enum */ EntityReference TARGET_ENTITY = new EntityReference("target_entity", LootContextParameters.TARGET_ENTITY);
        public static final /* enum */ EntityReference INTERACTING_ENTITY = new EntityReference("interacting_entity", LootContextParameters.INTERACTING_ENTITY);
        public static final StringIdentifiable.EnumCodec<EntityReference> CODEC;
        private final String type;
        private final ContextParameter<? extends Entity> parameter;
        private static final /* synthetic */ EntityReference[] field_940;

        public static EntityReference[] values() {
            return (EntityReference[])field_940.clone();
        }

        public static EntityReference valueOf(String string) {
            return Enum.valueOf(EntityReference.class, string);
        }

        private EntityReference(String type, ContextParameter<? extends Entity> parameter) {
            this.type = type;
            this.parameter = parameter;
        }

        @Override
        public ContextParameter<? extends Entity> contextParam() {
            return this.parameter;
        }

        public static EntityReference fromString(String type) {
            EntityReference entityReference = CODEC.byId(type);
            if (entityReference != null) {
                return entityReference;
            }
            throw new IllegalArgumentException("Invalid entity target " + type);
        }

        @Override
        public String asString() {
            return this.type;
        }

        private static /* synthetic */ EntityReference[] method_36793() {
            return new EntityReference[]{THIS, ATTACKER, DIRECT_ATTACKER, ATTACKING_PLAYER, TARGET_ENTITY, INTERACTING_ENTITY};
        }

        static {
            field_940 = EntityReference.method_36793();
            CODEC = StringIdentifiable.createCodec(EntityReference::values);
        }
    }

    public static class Builder {
        private final LootWorldContext worldContext;
        private @Nullable Random random;

        public Builder(LootWorldContext worldContext) {
            this.worldContext = worldContext;
        }

        public Builder random(long seed) {
            if (seed != 0L) {
                this.random = Random.create(seed);
            }
            return this;
        }

        public Builder random(Random random) {
            this.random = random;
            return this;
        }

        public ServerWorld getWorld() {
            return this.worldContext.getWorld();
        }

        public LootContext build(Optional<Identifier> randomId) {
            ServerWorld serverWorld = this.getWorld();
            MinecraftServer minecraftServer = serverWorld.getServer();
            Random random = Optional.ofNullable(this.random).or(() -> randomId.map(serverWorld::getOrCreateRandom)).orElseGet(serverWorld::getRandom);
            return new LootContext(this.worldContext, random, minecraftServer.getReloadableRegistries().createRegistryLookup());
        }
    }
}

