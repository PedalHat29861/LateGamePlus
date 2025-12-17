/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.schema;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.URI;
import java.util.List;
import net.minecraft.server.dedicated.management.schema.RpcSchema;

public record RpcSchemaEntry<T>(String name, URI reference, RpcSchema<T> schema) {
    public RpcSchema<T> ref() {
        return RpcSchema.ofReference(this.reference, this.schema.codec());
    }

    public RpcSchema<List<T>> array() {
        return RpcSchema.ofArray(this.ref(), this.schema.codec());
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{RpcSchemaEntry.class, "name;ref;schema", "name", "reference", "schema"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RpcSchemaEntry.class, "name;ref;schema", "name", "reference", "schema"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RpcSchemaEntry.class, "name;ref;schema", "name", "reference", "schema"}, this, object);
    }
}

