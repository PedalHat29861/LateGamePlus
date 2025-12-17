/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.google.common.collect.Sets
 */
package net.minecraft.util.context;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.util.context.ContextParameter;

public class ContextType {
    private final Set<ContextParameter<?>> required;
    private final Set<ContextParameter<?>> allowed;

    ContextType(Set<ContextParameter<?>> required, Set<ContextParameter<?>> allowed) {
        this.required = Set.copyOf(required);
        this.allowed = Set.copyOf(Sets.union(required, allowed));
    }

    public Set<ContextParameter<?>> getRequired() {
        return this.required;
    }

    public Set<ContextParameter<?>> getAllowed() {
        return this.allowed;
    }

    public String toString() {
        return "[" + Joiner.on((String)", ").join(this.allowed.stream().map(parameter -> (this.required.contains(parameter) ? "!" : "") + String.valueOf(parameter.getId())).iterator()) + "]";
    }

    public static class Builder {
        private final Set<ContextParameter<?>> required = Sets.newIdentityHashSet();
        private final Set<ContextParameter<?>> allowed = Sets.newIdentityHashSet();

        public Builder require(ContextParameter<?> parameter) {
            if (this.allowed.contains(parameter)) {
                throw new IllegalArgumentException("Parameter " + String.valueOf(parameter.getId()) + " is already optional");
            }
            this.required.add(parameter);
            return this;
        }

        public Builder allow(ContextParameter<?> parameter) {
            if (this.required.contains(parameter)) {
                throw new IllegalArgumentException("Parameter " + String.valueOf(parameter.getId()) + " is already required");
            }
            this.allowed.add(parameter);
            return this;
        }

        public ContextType build() {
            return new ContextType(this.required, this.allowed);
        }
    }
}

