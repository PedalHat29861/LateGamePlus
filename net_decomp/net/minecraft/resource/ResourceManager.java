/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.resource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

public interface ResourceManager
extends ResourceFactory {
    public Set<String> getAllNamespaces();

    public List<Resource> getAllResources(Identifier var1);

    public Map<Identifier, Resource> findResources(String var1, Predicate<Identifier> var2);

    public Map<Identifier, List<Resource>> findAllResources(String var1, Predicate<Identifier> var2);

    public Stream<ResourcePack> streamResourcePacks();

    public static final class Empty
    extends Enum<Empty>
    implements ResourceManager {
        public static final /* enum */ Empty INSTANCE = new Empty();
        private static final /* synthetic */ Empty[] field_25352;

        public static Empty[] values() {
            return (Empty[])field_25352.clone();
        }

        public static Empty valueOf(String string) {
            return Enum.valueOf(Empty.class, string);
        }

        @Override
        public Set<String> getAllNamespaces() {
            return Set.of();
        }

        @Override
        public Optional<Resource> getResource(Identifier identifier) {
            return Optional.empty();
        }

        @Override
        public List<Resource> getAllResources(Identifier id) {
            return List.of();
        }

        @Override
        public Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
            return Map.of();
        }

        @Override
        public Map<Identifier, List<Resource>> findAllResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
            return Map.of();
        }

        @Override
        public Stream<ResourcePack> streamResourcePacks() {
            return Stream.of(new ResourcePack[0]);
        }

        private static /* synthetic */ Empty[] method_36585() {
            return new Empty[]{INSTANCE};
        }

        static {
            field_25352 = Empty.method_36585();
        }
    }
}

