/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Const
 *  com.mojang.datafixers.kinds.Const$Mu
 *  com.mojang.datafixers.kinds.IdF
 *  com.mojang.datafixers.kinds.IdF$Mu
 *  com.mojang.datafixers.kinds.K1
 *  com.mojang.datafixers.kinds.OptionalBox
 *  com.mojang.datafixers.kinds.OptionalBox$Mu
 *  com.mojang.datafixers.util.Unit
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.brain;

import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.OptionalBox;
import com.mojang.datafixers.util.Unit;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.MemoryQueryResult;
import org.jspecify.annotations.Nullable;

public interface MemoryQuery<F extends K1, Value> {
    public MemoryModuleType<Value> memory();

    public MemoryModuleState getState();

    public @Nullable MemoryQueryResult<F, Value> toQueryResult(Brain<?> var1, java.util.Optional<Value> var2);

    public record Absent<Value>(MemoryModuleType<Value> memory) implements MemoryQuery<Const.Mu<Unit>, Value>
    {
        @Override
        public MemoryModuleState getState() {
            return MemoryModuleState.VALUE_ABSENT;
        }

        @Override
        public MemoryQueryResult<Const.Mu<Unit>, Value> toQueryResult(Brain<?> brain, java.util.Optional<Value> value) {
            if (value.isPresent()) {
                return null;
            }
            return new MemoryQueryResult(brain, this.memory, Const.create((Object)Unit.INSTANCE));
        }
    }

    public record Value<Value>(MemoryModuleType<Value> memory) implements MemoryQuery<IdF.Mu, Value>
    {
        @Override
        public MemoryModuleState getState() {
            return MemoryModuleState.VALUE_PRESENT;
        }

        @Override
        public MemoryQueryResult<IdF.Mu, Value> toQueryResult(Brain<?> brain, java.util.Optional<Value> value) {
            if (value.isEmpty()) {
                return null;
            }
            return new MemoryQueryResult(brain, this.memory, IdF.create(value.get()));
        }
    }

    public record Optional<Value>(MemoryModuleType<Value> memory) implements MemoryQuery<OptionalBox.Mu, Value>
    {
        @Override
        public MemoryModuleState getState() {
            return MemoryModuleState.REGISTERED;
        }

        @Override
        public MemoryQueryResult<OptionalBox.Mu, Value> toQueryResult(Brain<?> brain, java.util.Optional<Value> value) {
            return new MemoryQueryResult(brain, this.memory, OptionalBox.create(value));
        }
    }
}

