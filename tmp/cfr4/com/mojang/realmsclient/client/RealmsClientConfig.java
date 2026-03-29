/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.client;

import java.net.Proxy;
import org.jspecify.annotations.Nullable;

public class RealmsClientConfig {
    private static @Nullable Proxy proxy;

    public static @Nullable Proxy getProxy() {
        return proxy;
    }

    public static void setProxy(Proxy proxy) {
        if (RealmsClientConfig.proxy == null) {
            RealmsClientConfig.proxy = proxy;
        }
    }
}

