/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMaps
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import org.jspecify.annotations.Nullable;

public final class MergedComponentMap
implements ComponentMap {
    private final ComponentMap baseComponents;
    private Reference2ObjectMap<ComponentType<?>, Optional<?>> changedComponents;
    private boolean copyOnWrite;

    public MergedComponentMap(ComponentMap baseComponents) {
        this(baseComponents, Reference2ObjectMaps.emptyMap(), true);
    }

    private MergedComponentMap(ComponentMap baseComponents, Reference2ObjectMap<ComponentType<?>, Optional<?>> changedComponents, boolean copyOnWrite) {
        this.baseComponents = baseComponents;
        this.changedComponents = changedComponents;
        this.copyOnWrite = copyOnWrite;
    }

    public static MergedComponentMap create(ComponentMap baseComponents, ComponentChanges changes) {
        if (MergedComponentMap.shouldReuseChangesMap(baseComponents, changes.changedComponents)) {
            return new MergedComponentMap(baseComponents, changes.changedComponents, true);
        }
        MergedComponentMap mergedComponentMap = new MergedComponentMap(baseComponents);
        mergedComponentMap.applyChanges(changes);
        return mergedComponentMap;
    }

    private static boolean shouldReuseChangesMap(ComponentMap baseComponents, Reference2ObjectMap<ComponentType<?>, Optional<?>> changedComponents) {
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(changedComponents)) {
            Object object = baseComponents.get((ComponentType)entry.getKey());
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent() && optional.get().equals(object)) {
                return false;
            }
            if (!optional.isEmpty() || object != null) continue;
            return false;
        }
        return true;
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        Optional optional = (Optional)this.changedComponents.get(type);
        if (optional != null) {
            return optional.orElse(null);
        }
        return this.baseComponents.get(type);
    }

    public boolean hasChanged(ComponentType<?> type) {
        return this.changedComponents.containsKey(type);
    }

    public <T> @Nullable T set(ComponentType<T> type, @Nullable T value) {
        this.onWrite();
        T object = this.baseComponents.get(type);
        Optional optional = Objects.equals(value, object) ? (Optional)this.changedComponents.remove(type) : (Optional)this.changedComponents.put(type, Optional.ofNullable(value));
        if (optional != null) {
            return optional.orElse(object);
        }
        return object;
    }

    public <T> @Nullable T set(Component<T> component) {
        return this.set(component.type(), component.value());
    }

    public <T> @Nullable T remove(ComponentType<? extends T> type) {
        this.onWrite();
        T object = this.baseComponents.get(type);
        Optional optional = object != null ? (Optional)this.changedComponents.put(type, Optional.empty()) : (Optional)this.changedComponents.remove(type);
        if (optional != null) {
            return optional.orElse(null);
        }
        return object;
    }

    public void applyChanges(ComponentChanges changes) {
        this.onWrite();
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(changes.changedComponents)) {
            this.applyChange((ComponentType)entry.getKey(), (Optional)entry.getValue());
        }
    }

    private void applyChange(ComponentType<?> type, Optional<?> optional) {
        Object object = this.baseComponents.get(type);
        if (optional.isPresent()) {
            if (optional.get().equals(object)) {
                this.changedComponents.remove(type);
            } else {
                this.changedComponents.put(type, optional);
            }
        } else if (object != null) {
            this.changedComponents.put(type, Optional.empty());
        } else {
            this.changedComponents.remove(type);
        }
    }

    public void setChanges(ComponentChanges changes) {
        this.onWrite();
        this.changedComponents.clear();
        this.changedComponents.putAll(changes.changedComponents);
    }

    public void clearChanges() {
        this.onWrite();
        this.changedComponents.clear();
    }

    public void setAll(ComponentMap components) {
        for (Component<?> component : components) {
            component.apply(this);
        }
    }

    private void onWrite() {
        if (this.copyOnWrite) {
            this.changedComponents = new Reference2ObjectArrayMap(this.changedComponents);
            this.copyOnWrite = false;
        }
    }

    @Override
    public Set<ComponentType<?>> getTypes() {
        if (this.changedComponents.isEmpty()) {
            return this.baseComponents.getTypes();
        }
        ReferenceArraySet set = new ReferenceArraySet(this.baseComponents.getTypes());
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.changedComponents)) {
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent()) {
                set.add((ComponentType)entry.getKey());
                continue;
            }
            set.remove(entry.getKey());
        }
        return set;
    }

    @Override
    public Iterator<Component<?>> iterator() {
        if (this.changedComponents.isEmpty()) {
            return this.baseComponents.iterator();
        }
        ArrayList<Component> list = new ArrayList<Component>(this.changedComponents.size() + this.baseComponents.size());
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.changedComponents)) {
            if (!((Optional)entry.getValue()).isPresent()) continue;
            list.add(Component.of((ComponentType)entry.getKey(), ((Optional)entry.getValue()).get()));
        }
        for (Component component : this.baseComponents) {
            if (this.changedComponents.containsKey(component.type())) continue;
            list.add(component);
        }
        return list.iterator();
    }

    @Override
    public int size() {
        int i = this.baseComponents.size();
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.changedComponents)) {
            boolean bl2;
            boolean bl = ((Optional)entry.getValue()).isPresent();
            if (bl == (bl2 = this.baseComponents.contains((ComponentType)entry.getKey()))) continue;
            i += bl ? 1 : -1;
        }
        return i;
    }

    public ComponentChanges getChanges() {
        if (this.changedComponents.isEmpty()) {
            return ComponentChanges.EMPTY;
        }
        this.copyOnWrite = true;
        return new ComponentChanges(this.changedComponents);
    }

    public MergedComponentMap copy() {
        this.copyOnWrite = true;
        return new MergedComponentMap(this.baseComponents, this.changedComponents, true);
    }

    public ComponentMap immutableCopy() {
        if (this.changedComponents.isEmpty()) {
            return this.baseComponents;
        }
        return this.copy();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MergedComponentMap)) return false;
        MergedComponentMap mergedComponentMap = (MergedComponentMap)o;
        if (!this.baseComponents.equals(mergedComponentMap.baseComponents)) return false;
        if (!this.changedComponents.equals(mergedComponentMap.changedComponents)) return false;
        return true;
    }

    public int hashCode() {
        return this.baseComponents.hashCode() + this.changedComponents.hashCode() * 31;
    }

    public String toString() {
        return "{" + this.stream().map(Component::toString).collect(Collectors.joining(", ")) + "}";
    }
}

