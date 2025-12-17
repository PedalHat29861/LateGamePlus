/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util.path;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.slf4j.Logger;

public class PathResolving {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Path toPath(URI uri) throws IOException {
        try {
            return Paths.get(uri);
        }
        catch (FileSystemNotFoundException fileSystemNotFoundException) {
        }
        catch (Throwable throwable) {
            LOGGER.warn("Unable to get path for: {}", (Object)uri, (Object)throwable);
        }
        try {
            FileSystems.newFileSystem(uri, Collections.emptyMap());
        }
        catch (FileSystemAlreadyExistsException fileSystemAlreadyExistsException) {
            // empty catch block
        }
        return Paths.get(uri);
    }
}

