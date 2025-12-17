/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.server.dedicated.management;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.net.URI;
import java.net.URISyntaxException;

public class UriUtil {
    public static final Codec<URI> URI_CODEC = Codec.STRING.comapFlatMap(uri -> {
        try {
            return DataResult.success((Object)new URI((String)uri));
        }
        catch (URISyntaxException uRISyntaxException) {
            return DataResult.error(uRISyntaxException::getMessage);
        }
    }, URI::toString);

    public static URI createSchemasUri(String id) {
        return URI.create("#/components/schemas/" + id);
    }
}

