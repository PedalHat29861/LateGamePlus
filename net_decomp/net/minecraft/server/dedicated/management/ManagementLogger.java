/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated.management;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import org.slf4j.Logger;

public class ManagementLogger {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LOG_PREFIX = "RPC Connection #{}: ";

    public void logAction(ManagementConnectionId remote, String action, Object ... arguments) {
        if (arguments.length == 0) {
            LOGGER.info(LOG_PREFIX + action, (Object)remote.connectionId());
        } else {
            ArrayList<Object> list = new ArrayList<Object>(Arrays.asList(arguments));
            list.addFirst(remote.connectionId());
            LOGGER.info(LOG_PREFIX + action, list.toArray());
        }
    }
}

