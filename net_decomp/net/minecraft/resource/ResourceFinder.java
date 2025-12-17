/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.resource;

import java.util.List;
import java.util.Map;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ResourceFinder {
    private final String directoryName;
    private final String fileExtension;

    public ResourceFinder(String directoryName, String fileExtension) {
        this.directoryName = directoryName;
        this.fileExtension = fileExtension;
    }

    public static ResourceFinder json(String directoryName) {
        return new ResourceFinder(directoryName, ".json");
    }

    public static ResourceFinder json(RegistryKey<? extends Registry<?>> registryRef) {
        return ResourceFinder.json(RegistryKeys.getPath(registryRef));
    }

    public Identifier toResourcePath(Identifier id) {
        return id.withPath(this.directoryName + "/" + id.getPath() + this.fileExtension);
    }

    public Identifier toResourceId(Identifier path) {
        String string = path.getPath();
        return path.withPath(string.substring(this.directoryName.length() + 1, string.length() - this.fileExtension.length()));
    }

    public Map<Identifier, Resource> findResources(ResourceManager resourceManager) {
        return resourceManager.findResources(this.directoryName, path -> path.getPath().endsWith(this.fileExtension));
    }

    public Map<Identifier, List<Resource>> findAllResources(ResourceManager resourceManager) {
        return resourceManager.findAllResources(this.directoryName, path -> path.getPath().endsWith(this.fileExtension));
    }
}

