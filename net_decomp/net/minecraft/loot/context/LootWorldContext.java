/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.loot.context;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.util.context.ContextType;
import org.jspecify.annotations.Nullable;

public class LootWorldContext {
    private final ServerWorld world;
    private final ContextParameterMap parameters;
    private final Map<Identifier, DynamicDrop> dynamicDrops;
    private final float luck;

    public LootWorldContext(ServerWorld world, ContextParameterMap parameters, Map<Identifier, DynamicDrop> dynamicDrops, float luck) {
        this.world = world;
        this.parameters = parameters;
        this.dynamicDrops = dynamicDrops;
        this.luck = luck;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public ContextParameterMap getParameters() {
        return this.parameters;
    }

    public void addDynamicDrops(Identifier id, Consumer<ItemStack> lootConsumer) {
        DynamicDrop dynamicDrop = this.dynamicDrops.get(id);
        if (dynamicDrop != null) {
            dynamicDrop.add(lootConsumer);
        }
    }

    public float getLuck() {
        return this.luck;
    }

    @FunctionalInterface
    public static interface DynamicDrop {
        public void add(Consumer<ItemStack> var1);
    }

    public static class Builder {
        private final ServerWorld world;
        private final ContextParameterMap.Builder parameters = new ContextParameterMap.Builder();
        private final Map<Identifier, DynamicDrop> dynamicDrops = Maps.newHashMap();
        private float luck;

        public Builder(ServerWorld world) {
            this.world = world;
        }

        public ServerWorld getWorld() {
            return this.world;
        }

        public <T> Builder add(ContextParameter<T> parameter, T value) {
            this.parameters.add(parameter, value);
            return this;
        }

        public <T> Builder addOptional(ContextParameter<T> parameter, @Nullable T value) {
            this.parameters.addNullable(parameter, value);
            return this;
        }

        public <T> T get(ContextParameter<T> parameter) {
            return this.parameters.getOrThrow(parameter);
        }

        public <T> @Nullable T getOptional(ContextParameter<T> parameter) {
            return this.parameters.getNullable(parameter);
        }

        public Builder addDynamicDrop(Identifier id, DynamicDrop dynamicDrop) {
            DynamicDrop dynamicDrop2 = this.dynamicDrops.put(id, dynamicDrop);
            if (dynamicDrop2 != null) {
                throw new IllegalStateException("Duplicated dynamic drop '" + String.valueOf(this.dynamicDrops) + "'");
            }
            return this;
        }

        public Builder luck(float luck) {
            this.luck = luck;
            return this;
        }

        public LootWorldContext build(ContextType contextType) {
            ContextParameterMap contextParameterMap = this.parameters.build(contextType);
            return new LootWorldContext(this.world, contextParameterMap, this.dynamicDrops, this.luck);
        }
    }
}

