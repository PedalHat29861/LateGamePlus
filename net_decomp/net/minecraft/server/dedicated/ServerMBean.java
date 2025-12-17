/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated;

import com.mojang.logging.LogUtils;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class ServerMBean
implements DynamicMBean {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftServer server;
    private final MBeanInfo mBeanInfo;
    private final Map<String, Entry> entries = Stream.of(new Entry("tickTimes", this::getTickTimes, "Historical tick times (ms)", long[].class), new Entry("averageTickTime", this::getAverageTickTime, "Current average tick time (ms)", Long.TYPE)).collect(Collectors.toMap(entry -> entry.name, Function.identity()));

    private ServerMBean(MinecraftServer server) {
        this.server = server;
        MBeanAttributeInfo[] mBeanAttributeInfos = (MBeanAttributeInfo[])this.entries.values().stream().map(Entry::createInfo).toArray(MBeanAttributeInfo[]::new);
        this.mBeanInfo = new MBeanInfo(ServerMBean.class.getSimpleName(), "metrics for dedicated server", mBeanAttributeInfos, null, null, new MBeanNotificationInfo[0]);
    }

    public static void register(MinecraftServer server) {
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(new ServerMBean(server), new ObjectName("net.minecraft.server:type=Server"));
        }
        catch (InstanceAlreadyExistsException | MBeanRegistrationException | MalformedObjectNameException | NotCompliantMBeanException jMException) {
            LOGGER.warn("Failed to initialise server as JMX bean", (Throwable)jMException);
        }
    }

    private float getAverageTickTime() {
        return this.server.getAverageTickTime();
    }

    private long[] getTickTimes() {
        return this.server.getTickTimes();
    }

    @Override
    public @Nullable Object getAttribute(String attribute) {
        Entry entry = this.entries.get(attribute);
        return entry == null ? null : entry.getter.get();
    }

    @Override
    public void setAttribute(Attribute attribute) {
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        List<Attribute> list = Arrays.stream(attributes).map(this.entries::get).filter(Objects::nonNull).map(entry -> new Attribute(entry.name, entry.getter.get())).collect(Collectors.toList());
        return new AttributeList(list);
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return new AttributeList();
    }

    @Override
    public @Nullable Object invoke(String actionName, Object[] params, String[] signature) {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return this.mBeanInfo;
    }

    static final class Entry {
        final String name;
        final Supplier<Object> getter;
        private final String description;
        private final Class<?> type;

        Entry(String name, Supplier<Object> getter, String description, Class<?> type) {
            this.name = name;
            this.getter = getter;
            this.description = description;
            this.type = type;
        }

        private MBeanAttributeInfo createInfo() {
            return new MBeanAttributeInfo(this.name, this.type.getSimpleName(), this.description, true, false, false);
        }
    }
}

