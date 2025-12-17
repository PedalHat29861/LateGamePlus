/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.state.property;

import java.util.List;
import java.util.Optional;
import net.minecraft.state.property.Property;

public final class BooleanProperty
extends Property<Boolean> {
    private static final List<Boolean> VALUES = List.of(Boolean.valueOf(true), Boolean.valueOf(false));
    private static final int TRUE_ORDINAL = 0;
    private static final int FALSE_ORDINAL = 1;

    private BooleanProperty(String name) {
        super(name, Boolean.class);
    }

    @Override
    public List<Boolean> getValues() {
        return VALUES;
    }

    public static BooleanProperty of(String name) {
        return new BooleanProperty(name);
    }

    @Override
    public Optional<Boolean> parse(String name) {
        return switch (name) {
            case "true" -> Optional.of(true);
            case "false" -> Optional.of(false);
            default -> Optional.empty();
        };
    }

    @Override
    public String name(Boolean boolean_) {
        return boolean_.toString();
    }

    @Override
    public int ordinal(Boolean boolean_) {
        return boolean_ != false ? 0 : 1;
    }

    @Override
    public /* synthetic */ int ordinal(Comparable value) {
        return this.ordinal((Boolean)value);
    }

    @Override
    public /* synthetic */ String name(Comparable value) {
        return this.name((Boolean)value);
    }
}

