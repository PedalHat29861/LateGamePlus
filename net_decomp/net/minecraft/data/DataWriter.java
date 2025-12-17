/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.HashCode
 */
package net.minecraft.data;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import net.minecraft.util.path.PathUtil;

public interface DataWriter {
    public static final DataWriter UNCACHED = (path, data, hashCode) -> {
        PathUtil.createDirectories(path.getParent());
        Files.write(path, data, new OpenOption[0]);
    };

    public void write(Path var1, byte[] var2, HashCode var3) throws IOException;
}

