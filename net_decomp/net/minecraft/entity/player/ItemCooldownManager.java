/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.entity.player;

import com.google.common.collect.Maps;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ItemCooldownManager {
    private final Map<Identifier, Entry> entries = Maps.newHashMap();
    private int tick;

    public boolean isCoolingDown(ItemStack stack) {
        return this.getCooldownProgress(stack, 0.0f) > 0.0f;
    }

    public float getCooldownProgress(ItemStack stack, float tickProgress) {
        Identifier identifier = this.getGroup(stack);
        Entry entry = this.entries.get(identifier);
        if (entry != null) {
            float f = entry.endTick - entry.startTick;
            float g = (float)entry.endTick - ((float)this.tick + tickProgress);
            return MathHelper.clamp(g / f, 0.0f, 1.0f);
        }
        return 0.0f;
    }

    public void update() {
        ++this.tick;
        if (!this.entries.isEmpty()) {
            Iterator<Map.Entry<Identifier, Entry>> iterator = this.entries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Identifier, Entry> entry = iterator.next();
                if (entry.getValue().endTick > this.tick) continue;
                iterator.remove();
                this.onCooldownUpdate(entry.getKey());
            }
        }
    }

    public Identifier getGroup(ItemStack stack) {
        UseCooldownComponent useCooldownComponent = stack.get(DataComponentTypes.USE_COOLDOWN);
        Identifier identifier = Registries.ITEM.getId(stack.getItem());
        if (useCooldownComponent == null) {
            return identifier;
        }
        return useCooldownComponent.cooldownGroup().orElse(identifier);
    }

    public void set(ItemStack stack, int duration) {
        this.set(this.getGroup(stack), duration);
    }

    public void set(Identifier groupId, int duration) {
        this.entries.put(groupId, new Entry(this.tick, this.tick + duration));
        this.onCooldownUpdate(groupId, duration);
    }

    public void remove(Identifier groupId) {
        this.entries.remove(groupId);
        this.onCooldownUpdate(groupId);
    }

    protected void onCooldownUpdate(Identifier groupId, int duration) {
    }

    protected void onCooldownUpdate(Identifier groupId) {
    }

    static final class Entry
    extends Record {
        final int startTick;
        final int endTick;

        Entry(int startTick, int endTick) {
            this.startTick = startTick;
            this.endTick = endTick;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "startTime;endTime", "startTick", "endTick"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "startTime;endTime", "startTick", "endTick"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "startTime;endTime", "startTick", "endTick"}, this, object);
        }

        public int startTick() {
            return this.startTick;
        }

        public int endTick() {
            return this.endTick;
        }
    }
}

