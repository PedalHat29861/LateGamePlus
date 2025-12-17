/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.registry.Registries;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.AttributeValidator;
import net.minecraft.world.attribute.EnvironmentAttributeType;
import org.jspecify.annotations.Nullable;

public class EnvironmentAttribute<Value> {
    private final EnvironmentAttributeType<Value> type;
    private final Value defaultValue;
    private final AttributeValidator<Value> validator;
    private final boolean synced;
    private final boolean positional;
    private final boolean interpolated;

    EnvironmentAttribute(EnvironmentAttributeType<Value> type, Value defaultValue, AttributeValidator<Value> validator, boolean synced, boolean positional, boolean interpolated) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.validator = validator;
        this.synced = synced;
        this.positional = positional;
        this.interpolated = interpolated;
    }

    public static <Value> Builder<Value> builder(EnvironmentAttributeType<Value> type) {
        return new Builder<Value>(type);
    }

    public EnvironmentAttributeType<Value> getType() {
        return this.type;
    }

    public Value getDefaultValue() {
        return this.defaultValue;
    }

    public Codec<Value> getCodec() {
        return this.type.valueCodec().validate(this.validator::validate);
    }

    public Value clamp(Value value) {
        return this.validator.clamp(value);
    }

    public boolean isSynced() {
        return this.synced;
    }

    public boolean isPositional() {
        return this.positional;
    }

    public boolean isInterpolated() {
        return this.interpolated;
    }

    public String toString() {
        return Util.registryValueToString(Registries.ENVIRONMENTAL_ATTRIBUTE, this);
    }

    public static class Builder<Value> {
        private final EnvironmentAttributeType<Value> type;
        private @Nullable Value defaultValue;
        private AttributeValidator<Value> validator = AttributeValidator.all();
        private boolean synced = false;
        private boolean positional = true;
        private boolean interpolated = false;

        public Builder(EnvironmentAttributeType<Value> type) {
            this.type = type;
        }

        public Builder<Value> defaultValue(Value defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<Value> validator(AttributeValidator<Value> validator) {
            this.validator = validator;
            return this;
        }

        public Builder<Value> synced() {
            this.synced = true;
            return this;
        }

        public Builder<Value> global() {
            this.positional = false;
            return this;
        }

        public Builder<Value> interpolated() {
            this.interpolated = true;
            return this;
        }

        public EnvironmentAttribute<Value> build() {
            return new EnvironmentAttribute<Value>(this.type, Objects.requireNonNull(this.defaultValue, "Missing default value"), this.validator, this.synced, this.positional, this.interpolated);
        }
    }
}

