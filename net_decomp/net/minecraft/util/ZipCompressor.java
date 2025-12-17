/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class ZipCompressor
implements Closeable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path file;
    private final Path temp;
    private final FileSystem zip;

    public ZipCompressor(Path file) {
        this.file = file;
        this.temp = file.resolveSibling(file.getFileName().toString() + "_tmp");
        try {
            this.zip = Util.JAR_FILE_SYSTEM_PROVIDER.newFileSystem(this.temp, (Map<String, ?>)ImmutableMap.of((Object)"create", (Object)"true"));
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    public void write(Path target, String content) {
        try {
            Path path = this.zip.getPath(File.separator, new String[0]);
            Path path2 = path.resolve(target.toString());
            Files.createDirectories(path2.getParent(), new FileAttribute[0]);
            Files.write(path2, content.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    public void copy(Path target, File source) {
        try {
            Path path = this.zip.getPath(File.separator, new String[0]);
            Path path2 = path.resolve(target.toString());
            Files.createDirectories(path2.getParent(), new FileAttribute[0]);
            Files.copy(source.toPath(), path2, new CopyOption[0]);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    public void copyAll(Path source) {
        try {
            Path path2 = this.zip.getPath(File.separator, new String[0]);
            if (Files.isRegularFile(source, new LinkOption[0])) {
                Path path22 = path2.resolve(source.getParent().relativize(source).toString());
                Files.copy(path22, source, new CopyOption[0]);
                return;
            }
            try (Stream<Path> stream = Files.find(source, Integer.MAX_VALUE, (path, attributes) -> attributes.isRegularFile(), new FileVisitOption[0]);){
                for (Path path3 : stream.collect(Collectors.toList())) {
                    Path path4 = path2.resolve(source.relativize(path3).toString());
                    Files.createDirectories(path4.getParent(), new FileAttribute[0]);
                    Files.copy(path3, path4, new CopyOption[0]);
                }
            }
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    @Override
    public void close() {
        try {
            this.zip.close();
            Files.move(this.temp, this.file, new CopyOption[0]);
            LOGGER.info("Compressed to {}", (Object)this.file);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }
}

