/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Function
 *  com.google.common.base.Ticker
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.util.concurrent.MoreExecutors
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectLists
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ReferenceImmutableList
 *  it.unimi.dsi.fastutil.objects.ReferenceList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.base.Function;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceImmutableList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.Schemas;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Property;
import net.minecraft.util.CachedMapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeSupplier;
import net.minecraft.util.annotation.SuppressLinter;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.crash.ReportType;
import net.minecraft.util.function.CharPredicate;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.thread.NameableExecutor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Util {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_PARALLELISM = 255;
    private static final int BACKUP_ATTEMPTS = 10;
    private static final String MAX_BG_THREADS_PROPERTY = "max.bg.threads";
    private static final NameableExecutor MAIN_WORKER_EXECUTOR = Util.createWorker("Main");
    private static final NameableExecutor IO_WORKER_EXECUTOR = Util.createIoWorker("IO-Worker-", false);
    private static final NameableExecutor DOWNLOAD_WORKER_EXECUTOR = Util.createIoWorker("Download-", true);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
    public static final int field_46220 = 8;
    private static final Set<String> SUPPORTED_URI_PROTOCOLS = Set.of("http", "https");
    public static final long NANOS_PER_MILLI = 1000000L;
    public static TimeSupplier.Nanoseconds nanoTimeSupplier = System::nanoTime;
    public static final Ticker TICKER = new Ticker(){

        public long read() {
            return nanoTimeSupplier.getAsLong();
        }
    };
    public static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final FileSystemProvider JAR_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().stream().filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar")).findFirst().orElseThrow(() -> new IllegalStateException("No jar file system provider found"));
    private static Consumer<String> missingBreakpointHandler = message -> {};

    public static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T> Collector<T, ?, List<T>> toArrayList() {
        return Collectors.toCollection(Lists::newArrayList);
    }

    public static <T extends Comparable<T>> String getValueAsString(Property<T> property, Object value) {
        return property.name((Comparable)value);
    }

    public static String createTranslationKey(String type, @Nullable Identifier id) {
        if (id == null) {
            return type + ".unregistered_sadface";
        }
        return type + "." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }

    public static long getMeasuringTimeMs() {
        return Util.getMeasuringTimeNano() / 1000000L;
    }

    public static long getMeasuringTimeNano() {
        return nanoTimeSupplier.getAsLong();
    }

    public static long getEpochTimeMs() {
        return Instant.now().toEpochMilli();
    }

    public static String getFormattedCurrentTime() {
        return DATE_TIME_FORMATTER.format(ZonedDateTime.now());
    }

    private static NameableExecutor createWorker(final String name) {
        Object executorService;
        int i = Util.getAvailableBackgroundThreads();
        if (i <= 0) {
            executorService = MoreExecutors.newDirectExecutorService();
        } else {
            AtomicInteger atomicInteger = new AtomicInteger(1);
            executorService = new ForkJoinPool(i, pool -> {
                final String string2 = "Worker-" + name + "-" + atomicInteger.getAndIncrement();
                ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(pool){

                    @Override
                    protected void onStart() {
                        TracyClient.setThreadName((String)string2, (int)name.hashCode());
                        super.onStart();
                    }

                    @Override
                    protected void onTermination(@Nullable Throwable throwable) {
                        if (throwable != null) {
                            LOGGER.warn("{} died", (Object)this.getName(), (Object)throwable);
                        } else {
                            LOGGER.debug("{} shutdown", (Object)this.getName());
                        }
                        super.onTermination(throwable);
                    }
                };
                forkJoinWorkerThread.setName(string2);
                return forkJoinWorkerThread;
            }, Util::uncaughtExceptionHandler, true);
        }
        return new NameableExecutor((ExecutorService)executorService);
    }

    public static int getAvailableBackgroundThreads() {
        return MathHelper.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, Util.getMaxBackgroundThreads());
    }

    private static int getMaxBackgroundThreads() {
        String string = System.getProperty(MAX_BG_THREADS_PROPERTY);
        if (string != null) {
            try {
                int i = Integer.parseInt(string);
                if (i >= 1 && i <= 255) {
                    return i;
                }
                LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{MAX_BG_THREADS_PROPERTY, string, 255});
            }
            catch (NumberFormatException numberFormatException) {
                LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{MAX_BG_THREADS_PROPERTY, string, 255});
            }
        }
        return 255;
    }

    public static NameableExecutor getMainWorkerExecutor() {
        return MAIN_WORKER_EXECUTOR;
    }

    public static NameableExecutor getIoWorkerExecutor() {
        return IO_WORKER_EXECUTOR;
    }

    public static NameableExecutor getDownloadWorkerExecutor() {
        return DOWNLOAD_WORKER_EXECUTOR;
    }

    public static void shutdownExecutors() {
        MAIN_WORKER_EXECUTOR.shutdown(3L, TimeUnit.SECONDS);
        IO_WORKER_EXECUTOR.shutdown(3L, TimeUnit.SECONDS);
    }

    private static NameableExecutor createIoWorker(String namePrefix, boolean daemon) {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        return new NameableExecutor(Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            String string2 = namePrefix + atomicInteger.getAndIncrement();
            TracyClient.setThreadName((String)string2, (int)namePrefix.hashCode());
            thread.setName(string2);
            thread.setDaemon(daemon);
            thread.setUncaughtExceptionHandler(Util::uncaughtExceptionHandler);
            return thread;
        }));
    }

    public static void throwUnchecked(Throwable t) {
        throw t instanceof RuntimeException ? (RuntimeException)t : new RuntimeException(t);
    }

    private static void uncaughtExceptionHandler(Thread thread, Throwable t) {
        Util.getFatalOrPause(t);
        if (t instanceof CompletionException) {
            t = t.getCause();
        }
        if (t instanceof CrashException) {
            CrashException crashException = (CrashException)t;
            Bootstrap.println(crashException.getReport().asString(ReportType.MINECRAFT_CRASH_REPORT));
            System.exit(-1);
        }
        LOGGER.error("Caught exception in thread {}", (Object)thread, (Object)t);
    }

    public static @Nullable Type<?> getChoiceType(DSL.TypeReference typeReference, String id) {
        if (!SharedConstants.useChoiceTypeRegistrations) {
            return null;
        }
        return Util.getChoiceTypeInternal(typeReference, id);
    }

    private static @Nullable Type<?> getChoiceTypeInternal(DSL.TypeReference typeReference, String id) {
        Type type;
        block2: {
            type = null;
            try {
                type = Schemas.getFixer().getSchema(DataFixUtils.makeKey((int)SharedConstants.getGameVersion().dataVersion().id())).getChoiceType(typeReference, id);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.error("No data fixer registered for {}", (Object)id);
                if (!SharedConstants.isDevelopment) break block2;
                throw illegalArgumentException;
            }
        }
        return type;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void runInNamedZone(Runnable runnable, String name) {
        block16: {
            if (SharedConstants.isDevelopment) {
                Thread thread = Thread.currentThread();
                String string = thread.getName();
                thread.setName(name);
                try (Zone zone = TracyClient.beginZone((String)name, (boolean)SharedConstants.isDevelopment);){
                    runnable.run();
                    break block16;
                }
                finally {
                    thread.setName(string);
                }
            }
            try (Zone zone2 = TracyClient.beginZone((String)name, (boolean)SharedConstants.isDevelopment);){
                runnable.run();
            }
        }
    }

    public static <T> String registryValueToString(Registry<T> registry, T value) {
        Identifier identifier = registry.getId(value);
        if (identifier == null) {
            return "[unregistered]";
        }
        return identifier.toString();
    }

    public static <T> Predicate<T> and() {
        return o -> true;
    }

    public static <T> Predicate<T> and(Predicate<? super T> a) {
        return a;
    }

    public static <T> Predicate<T> and(Predicate<? super T> a, Predicate<? super T> b) {
        return o -> a.test(o) && b.test(o);
    }

    public static <T> Predicate<T> and(Predicate<? super T> a, Predicate<? super T> b, Predicate<? super T> c) {
        return o -> a.test(o) && b.test(o) && c.test(o);
    }

    public static <T> Predicate<T> and(Predicate<? super T> a, Predicate<? super T> b, Predicate<? super T> c, Predicate<? super T> d) {
        return o -> a.test(o) && b.test(o) && c.test(o) && d.test(o);
    }

    public static <T> Predicate<T> and(Predicate<? super T> a, Predicate<? super T> b, Predicate<? super T> c, Predicate<? super T> d, Predicate<? super T> e) {
        return o -> a.test(o) && b.test(o) && c.test(o) && d.test(o) && e.test(o);
    }

    @SafeVarargs
    public static <T> Predicate<T> and(Predicate<? super T> ... predicates) {
        return o -> {
            for (Predicate predicate : predicates) {
                if (predicate.test(o)) continue;
                return false;
            }
            return true;
        };
    }

    public static <T> Predicate<T> allOf(List<? extends Predicate<? super T>> predicates) {
        return switch (predicates.size()) {
            case 0 -> Util.and();
            case 1 -> Util.and(predicates.get(0));
            case 2 -> Util.and(predicates.get(0), predicates.get(1));
            case 3 -> Util.and(predicates.get(0), predicates.get(1), predicates.get(2));
            case 4 -> Util.and(predicates.get(0), predicates.get(1), predicates.get(2), predicates.get(3));
            case 5 -> Util.and(predicates.get(0), predicates.get(1), predicates.get(2), predicates.get(3), predicates.get(4));
            default -> {
                Predicate[] predicates2 = (Predicate[])predicates.toArray(Predicate[]::new);
                yield Util.and(predicates2);
            }
        };
    }

    public static <T> Predicate<T> or() {
        return o -> false;
    }

    public static <T> Predicate<T> or(Predicate<? super T> a) {
        return a;
    }

    public static <T> Predicate<T> or(Predicate<? super T> a, Predicate<? super T> b) {
        return o -> a.test(o) || b.test(o);
    }

    public static <T> Predicate<T> or(Predicate<? super T> a, Predicate<? super T> b, Predicate<? super T> c) {
        return o -> a.test(o) || b.test(o) || c.test(o);
    }

    public static <T> Predicate<T> or(Predicate<? super T> a, Predicate<? super T> b, Predicate<? super T> c, Predicate<? super T> d) {
        return o -> a.test(o) || b.test(o) || c.test(o) || d.test(o);
    }

    public static <T> Predicate<T> or(Predicate<? super T> a, Predicate<? super T> b, Predicate<? super T> c, Predicate<? super T> d, Predicate<? super T> e) {
        return o -> a.test(o) || b.test(o) || c.test(o) || d.test(o) || e.test(o);
    }

    @SafeVarargs
    public static <T> Predicate<T> or(Predicate<? super T> ... predicates) {
        return o -> {
            for (Predicate predicate : predicates) {
                if (!predicate.test(o)) continue;
                return true;
            }
            return false;
        };
    }

    public static <T> Predicate<T> anyOf(List<? extends Predicate<? super T>> predicates) {
        return switch (predicates.size()) {
            case 0 -> Util.or();
            case 1 -> Util.or(predicates.get(0));
            case 2 -> Util.or(predicates.get(0), predicates.get(1));
            case 3 -> Util.or(predicates.get(0), predicates.get(1), predicates.get(2));
            case 4 -> Util.or(predicates.get(0), predicates.get(1), predicates.get(2), predicates.get(3));
            case 5 -> Util.or(predicates.get(0), predicates.get(1), predicates.get(2), predicates.get(3), predicates.get(4));
            default -> {
                Predicate[] predicates2 = (Predicate[])predicates.toArray(Predicate[]::new);
                yield Util.or(predicates2);
            }
        };
    }

    public static <T> boolean isSymmetrical(int width, int height, List<T> list) {
        if (width == 1) {
            return true;
        }
        int i = width / 2;
        for (int j = 0; j < height; ++j) {
            for (int k = 0; k < i; ++k) {
                T object2;
                int l = width - 1 - k;
                T object = list.get(k + j * width);
                if (object.equals(object2 = list.get(l + j * width))) continue;
                return false;
            }
        }
        return true;
    }

    public static int nextCapacity(int current, int min) {
        return (int)Math.max(Math.min((long)current + (long)(current >> 1), 0x7FFFFFF7L), (long)min);
    }

    @SuppressLinter(reason="Intentional use of default locale for user-visible date")
    public static DateTimeFormatter getDefaultLocaleFormatter(FormatStyle style) {
        return DateTimeFormatter.ofLocalizedDateTime(style);
    }

    public static OperatingSystem getOperatingSystem() {
        String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (string.contains("win")) {
            return OperatingSystem.WINDOWS;
        }
        if (string.contains("mac")) {
            return OperatingSystem.OSX;
        }
        if (string.contains("solaris")) {
            return OperatingSystem.SOLARIS;
        }
        if (string.contains("sunos")) {
            return OperatingSystem.SOLARIS;
        }
        if (string.contains("linux")) {
            return OperatingSystem.LINUX;
        }
        if (string.contains("unix")) {
            return OperatingSystem.LINUX;
        }
        return OperatingSystem.UNKNOWN;
    }

    public static boolean isOnAarch64() {
        String string = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        return string.equals("aarch64");
    }

    public static URI validateUri(String uri) throws URISyntaxException {
        URI uRI = new URI(uri);
        String string = uRI.getScheme();
        if (string == null) {
            throw new URISyntaxException(uri, "Missing protocol in URI: " + uri);
        }
        String string2 = string.toLowerCase(Locale.ROOT);
        if (!SUPPORTED_URI_PROTOCOLS.contains(string2)) {
            throw new URISyntaxException(uri, "Unsupported protocol in URI: " + uri);
        }
        return uRI;
    }

    public static <T> T next(Iterable<T> iterable, @Nullable T object) {
        Iterator<T> iterator = iterable.iterator();
        T object2 = iterator.next();
        if (object != null) {
            T object3 = object2;
            while (true) {
                if (object3 == object) {
                    if (!iterator.hasNext()) break;
                    return iterator.next();
                }
                if (!iterator.hasNext()) continue;
                object3 = iterator.next();
            }
        }
        return object2;
    }

    public static <T> T previous(Iterable<T> iterable, @Nullable T object) {
        Iterator<T> iterator = iterable.iterator();
        T object2 = null;
        while (iterator.hasNext()) {
            T object3 = iterator.next();
            if (object3 == object) {
                if (object2 != null) break;
                object2 = (T)(iterator.hasNext() ? Iterators.getLast(iterator) : object);
                break;
            }
            object2 = object3;
        }
        return object2;
    }

    public static <T> T make(Supplier<T> factory) {
        return factory.get();
    }

    public static <T> T make(T object, Consumer<? super T> initializer) {
        initializer.accept(object);
        return object;
    }

    public static <K extends Enum<K>, V> Map<K, V> mapEnum(Class<K> enumClass, java.util.function.Function<K, V> mapper) {
        EnumMap<Enum, V> enumMap = new EnumMap<Enum, V>(enumClass);
        for (Enum enum_ : (Enum[])enumClass.getEnumConstants()) {
            enumMap.put(enum_, mapper.apply(enum_));
        }
        return enumMap;
    }

    public static <K, V1, V2> Map<K, V2> transformMapValues(Map<K, V1> map, java.util.function.Function<? super V1, V2> transformer) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> transformer.apply((Object)entry.getValue())));
    }

    public static <K, V1, V2> Map<K, V2> transformMapValuesLazy(Map<K, V1> map, Function<V1, V2> transformer) {
        return Maps.transformValues(map, transformer);
    }

    public static <V> CompletableFuture<List<V>> combineSafe(List<? extends CompletableFuture<V>> futures) {
        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        if (futures.size() == 1) {
            return futures.getFirst().thenApply(ObjectLists::singleton);
        }
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return completableFuture.thenApply(void_ -> futures.stream().map(CompletableFuture::join).toList());
    }

    public static <V> CompletableFuture<List<V>> combine(List<? extends CompletableFuture<? extends V>> futures) {
        CompletableFuture completableFuture = new CompletableFuture();
        return Util.combine(futures, completableFuture::completeExceptionally).applyToEither((CompletionStage)completableFuture, java.util.function.Function.identity());
    }

    public static <V> CompletableFuture<List<V>> combineCancellable(List<? extends CompletableFuture<? extends V>> futures) {
        CompletableFuture completableFuture = new CompletableFuture();
        return Util.combine(futures, throwable -> {
            if (completableFuture.completeExceptionally((Throwable)throwable)) {
                for (CompletableFuture completableFuture2 : futures) {
                    completableFuture2.cancel(true);
                }
            }
        }).applyToEither((CompletionStage)completableFuture, java.util.function.Function.identity());
    }

    private static <V> CompletableFuture<List<V>> combine(List<? extends CompletableFuture<? extends V>> futures, Consumer<Throwable> exceptionHandler) {
        ObjectArrayList objectArrayList = new ObjectArrayList();
        objectArrayList.size(futures.size());
        CompletableFuture[] completableFutures = new CompletableFuture[futures.size()];
        for (int i = 0; i < futures.size(); ++i) {
            int j = i;
            completableFutures[i] = futures.get(i).whenComplete((value, throwable) -> {
                if (throwable != null) {
                    exceptionHandler.accept((Throwable)throwable);
                } else {
                    objectArrayList.set(j, value);
                }
            });
        }
        return CompletableFuture.allOf(completableFutures).thenApply(void_ -> objectArrayList);
    }

    public static <T> Optional<T> ifPresentOrElse(Optional<T> optional, Consumer<T> presentAction, Runnable elseAction) {
        if (optional.isPresent()) {
            presentAction.accept(optional.get());
        } else {
            elseAction.run();
        }
        return optional;
    }

    public static <T> Supplier<T> debugSupplier(final Supplier<T> supplier, Supplier<String> messageSupplier) {
        if (SharedConstants.NAMED_RUNNABLES) {
            final String string = messageSupplier.get();
            return new Supplier<T>(){

                @Override
                public T get() {
                    return supplier.get();
                }

                public String toString() {
                    return string;
                }
            };
        }
        return supplier;
    }

    public static Runnable debugRunnable(final Runnable runnable, Supplier<String> messageSupplier) {
        if (SharedConstants.NAMED_RUNNABLES) {
            final String string = messageSupplier.get();
            return new Runnable(){

                @Override
                public void run() {
                    runnable.run();
                }

                public String toString() {
                    return string;
                }
            };
        }
        return runnable;
    }

    public static void logErrorOrPause(String message) {
        LOGGER.error(message);
        if (SharedConstants.isDevelopment) {
            Util.pause(message);
        }
    }

    public static void logErrorOrPause(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
        if (SharedConstants.isDevelopment) {
            Util.pause(message);
        }
    }

    public static <T extends Throwable> T getFatalOrPause(T t) {
        if (SharedConstants.isDevelopment) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t);
            Util.pause(t.getMessage());
        }
        return t;
    }

    public static void setMissingBreakpointHandler(Consumer<String> missingBreakpointHandler) {
        Util.missingBreakpointHandler = missingBreakpointHandler;
    }

    private static void pause(String message) {
        boolean bl;
        Instant instant = Instant.now();
        LOGGER.warn("Did you remember to set a breakpoint here?");
        boolean bl2 = bl = Duration.between(instant, Instant.now()).toMillis() > 500L;
        if (!bl) {
            missingBreakpointHandler.accept(message);
        }
    }

    public static String getInnermostMessage(Throwable t) {
        if (t.getCause() != null) {
            return Util.getInnermostMessage(t.getCause());
        }
        if (t.getMessage() != null) {
            return t.getMessage();
        }
        return t.toString();
    }

    public static <T> T getRandom(T[] array, Random random) {
        return array[random.nextInt(array.length)];
    }

    public static int getRandom(int[] array, Random random) {
        return array[random.nextInt(array.length)];
    }

    public static <T> T getRandom(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }

    public static <T> Optional<T> getRandomOrEmpty(List<T> list, Random random) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Util.getRandom(list, random));
    }

    private static BooleanSupplier renameTask(final Path src, final Path dest) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.move(src, dest, new CopyOption[0]);
                    return true;
                }
                catch (IOException iOException) {
                    LOGGER.error("Failed to rename", (Throwable)iOException);
                    return false;
                }
            }

            public String toString() {
                return "rename " + String.valueOf(src) + " to " + String.valueOf(dest);
            }
        };
    }

    private static BooleanSupplier deleteTask(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.deleteIfExists(path);
                    return true;
                }
                catch (IOException iOException) {
                    LOGGER.warn("Failed to delete", (Throwable)iOException);
                    return false;
                }
            }

            public String toString() {
                return "delete old " + String.valueOf(path);
            }
        };
    }

    private static BooleanSupplier deletionVerifyTask(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return !Files.exists(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf(path) + " is deleted";
            }
        };
    }

    private static BooleanSupplier existenceCheckTask(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return Files.isRegularFile(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf(path) + " is present";
            }
        };
    }

    private static boolean attemptTasks(BooleanSupplier ... tasks) {
        for (BooleanSupplier booleanSupplier : tasks) {
            if (booleanSupplier.getAsBoolean()) continue;
            LOGGER.warn("Failed to execute {}", (Object)booleanSupplier);
            return false;
        }
        return true;
    }

    private static boolean attemptTasks(int retries, String taskName, BooleanSupplier ... tasks) {
        for (int i = 0; i < retries; ++i) {
            if (Util.attemptTasks(tasks)) {
                return true;
            }
            LOGGER.error("Failed to {}, retrying {}/{}", new Object[]{taskName, i, retries});
        }
        LOGGER.error("Failed to {}, aborting, progress might be lost", (Object)taskName);
        return false;
    }

    public static void backupAndReplace(Path current, Path newPath, Path backup) {
        Util.backupAndReplace(current, newPath, backup, false);
    }

    public static boolean backupAndReplace(Path current, Path newPath, Path backup, boolean noRestoreOnFail) {
        if (Files.exists(current, new LinkOption[0]) && !Util.attemptTasks(10, "create backup " + String.valueOf(backup), Util.deleteTask(backup), Util.renameTask(current, backup), Util.existenceCheckTask(backup))) {
            return false;
        }
        if (!Util.attemptTasks(10, "remove old " + String.valueOf(current), Util.deleteTask(current), Util.deletionVerifyTask(current))) {
            return false;
        }
        if (!Util.attemptTasks(10, "replace " + String.valueOf(current) + " with " + String.valueOf(newPath), Util.renameTask(newPath, current), Util.existenceCheckTask(current)) && !noRestoreOnFail) {
            Util.attemptTasks(10, "restore " + String.valueOf(current) + " from " + String.valueOf(backup), Util.renameTask(backup, current), Util.existenceCheckTask(current));
            return false;
        }
        return true;
    }

    public static int moveCursor(String string, int cursor, int delta) {
        int i = string.length();
        if (delta >= 0) {
            for (int j = 0; cursor < i && j < delta; ++j) {
                if (!Character.isHighSurrogate(string.charAt(cursor++)) || cursor >= i || !Character.isLowSurrogate(string.charAt(cursor))) continue;
                ++cursor;
            }
        } else {
            for (int j = delta; cursor > 0 && j < 0; ++j) {
                if (!Character.isLowSurrogate(string.charAt(--cursor)) || cursor <= 0 || !Character.isHighSurrogate(string.charAt(cursor - 1))) continue;
                --cursor;
            }
        }
        return cursor;
    }

    public static Consumer<String> addPrefix(String prefix, Consumer<String> consumer) {
        return string -> consumer.accept(prefix + string);
    }

    public static DataResult<int[]> decodeFixedLengthArray(IntStream stream, int length) {
        int[] is = stream.limit(length + 1).toArray();
        if (is.length != length) {
            Supplier<String> supplier = () -> "Input is not a list of " + length + " ints";
            if (is.length >= length) {
                return DataResult.error(supplier, (Object)Arrays.copyOf(is, length));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success((Object)is);
    }

    public static DataResult<long[]> decodeFixedLengthArray(LongStream stream, int length) {
        long[] ls = stream.limit(length + 1).toArray();
        if (ls.length != length) {
            Supplier<String> supplier = () -> "Input is not a list of " + length + " longs";
            if (ls.length >= length) {
                return DataResult.error(supplier, (Object)Arrays.copyOf(ls, length));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success((Object)ls);
    }

    public static <T> DataResult<List<T>> decodeFixedLengthList(List<T> list, int length) {
        if (list.size() != length) {
            Supplier<String> supplier = () -> "Input is not a list of " + length + " elements";
            if (list.size() >= length) {
                return DataResult.error(supplier, list.subList(0, length));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success(list);
    }

    public static void startTimerHack() {
        Thread thread = new Thread("Timer hack thread"){

            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(Integer.MAX_VALUE);
                    }
                }
                catch (InterruptedException interruptedException) {
                    LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                    return;
                }
            }
        };
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
        thread.start();
    }

    public static void relativeCopy(Path src, Path dest, Path toCopy) throws IOException {
        Path path = src.relativize(toCopy);
        Path path2 = dest.resolve(path);
        Files.copy(toCopy, path2, new CopyOption[0]);
    }

    public static String replaceInvalidChars(String string, CharPredicate predicate) {
        return string.toLowerCase(Locale.ROOT).chars().mapToObj(charCode -> predicate.test((char)charCode) ? Character.toString((char)charCode) : "_").collect(Collectors.joining());
    }

    public static <K, V> CachedMapper<K, V> cachedMapper(java.util.function.Function<K, V> mapper) {
        return new CachedMapper<K, V>(mapper);
    }

    public static <T, R> java.util.function.Function<T, R> memoize(final java.util.function.Function<T, R> function) {
        return new java.util.function.Function<T, R>(){
            private final Map<T, R> cache = new ConcurrentHashMap();

            @Override
            public R apply(T object) {
                return this.cache.computeIfAbsent(object, function);
            }

            public String toString() {
                return "memoize/1[function=" + String.valueOf(function) + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> biFunction) {
        return new BiFunction<T, U, R>(){
            private final Map<Pair<T, U>, R> cache = new ConcurrentHashMap();

            @Override
            public R apply(T a, U b) {
                return this.cache.computeIfAbsent(Pair.of(a, b), pair -> biFunction.apply(pair.getFirst(), pair.getSecond()));
            }

            public String toString() {
                return "memoize/2[function=" + String.valueOf(biFunction) + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T> List<T> copyShuffled(Stream<T> stream, Random random) {
        ObjectArrayList objectArrayList = (ObjectArrayList)stream.collect(ObjectArrayList.toList());
        Util.shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static IntArrayList shuffle(IntStream stream, Random random) {
        int i;
        IntArrayList intArrayList = IntArrayList.wrap((int[])stream.toArray());
        for (int j = i = intArrayList.size(); j > 1; --j) {
            int k = random.nextInt(j);
            intArrayList.set(j - 1, intArrayList.set(k, intArrayList.getInt(j - 1)));
        }
        return intArrayList;
    }

    public static <T> List<T> copyShuffled(T[] array, Random random) {
        ObjectArrayList objectArrayList = new ObjectArrayList((Object[])array);
        Util.shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static <T> List<T> copyShuffled(ObjectArrayList<T> list, Random random) {
        ObjectArrayList objectArrayList = new ObjectArrayList(list);
        Util.shuffle(objectArrayList, random);
        return objectArrayList;
    }

    public static <T> void shuffle(List<T> list, Random random) {
        int i;
        for (int j = i = list.size(); j > 1; --j) {
            int k = random.nextInt(j);
            list.set(j - 1, list.set(k, list.get(j - 1)));
        }
    }

    public static <T> CompletableFuture<T> waitAndApply(java.util.function.Function<Executor, CompletableFuture<T>> resultFactory) {
        return Util.waitAndApply(resultFactory, CompletableFuture::isDone);
    }

    public static <T> T waitAndApply(java.util.function.Function<Executor, T> resultFactory, Predicate<T> donePredicate) {
        int i;
        LinkedBlockingQueue blockingQueue = new LinkedBlockingQueue();
        T object = resultFactory.apply(blockingQueue::add);
        while (!donePredicate.test(object)) {
            try {
                Runnable runnable = (Runnable)blockingQueue.poll(100L, TimeUnit.MILLISECONDS);
                if (runnable == null) continue;
                runnable.run();
            }
            catch (InterruptedException interruptedException) {
                LOGGER.warn("Interrupted wait");
                break;
            }
        }
        if ((i = blockingQueue.size()) > 0) {
            LOGGER.warn("Tasks left in queue: {}", (Object)i);
        }
        return object;
    }

    public static <T> ToIntFunction<T> lastIndexGetter(List<T> values) {
        int i = values.size();
        if (i < 8) {
            return values::indexOf;
        }
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap(i);
        object2IntMap.defaultReturnValue(-1);
        for (int j = 0; j < i; ++j) {
            object2IntMap.put(values.get(j), j);
        }
        return object2IntMap;
    }

    public static <T> ToIntFunction<T> lastIdentityIndexGetter(List<T> values) {
        int i = values.size();
        if (i < 8) {
            ReferenceImmutableList referenceList = new ReferenceImmutableList(values);
            return arg_0 -> ((ReferenceList)referenceList).indexOf(arg_0);
        }
        Reference2IntOpenHashMap reference2IntMap = new Reference2IntOpenHashMap(i);
        reference2IntMap.defaultReturnValue(-1);
        for (int j = 0; j < i; ++j) {
            reference2IntMap.put(values.get(j), j);
        }
        return reference2IntMap;
    }

    public static <A, B> Typed<B> apply(Typed<A> typed, Type<B> type, UnaryOperator<Dynamic<?>> modifier) {
        Dynamic dynamic = (Dynamic)typed.write().getOrThrow();
        return Util.readTyped(type, (Dynamic)modifier.apply(dynamic), true);
    }

    public static <T> Typed<T> readTyped(Type<T> type, Dynamic<?> value) {
        return Util.readTyped(type, value, false);
    }

    public static <T> Typed<T> readTyped(Type<T> type, Dynamic<?> value, boolean allowPartial) {
        DataResult dataResult = type.readTyped(value).map(Pair::getFirst);
        try {
            if (allowPartial) {
                return (Typed)dataResult.getPartialOrThrow(IllegalStateException::new);
            }
            return (Typed)dataResult.getOrThrow(IllegalStateException::new);
        }
        catch (IllegalStateException illegalStateException) {
            CrashReport crashReport = CrashReport.create(illegalStateException, "Reading type");
            CrashReportSection crashReportSection = crashReport.addElement("Info");
            crashReportSection.add("Data", value);
            crashReportSection.add("Type", type);
            throw new CrashException(crashReport);
        }
    }

    public static <T> List<T> withAppended(List<T> list, T valueToAppend) {
        return ImmutableList.builderWithExpectedSize((int)(list.size() + 1)).addAll(list).add(valueToAppend).build();
    }

    public static <T> List<T> withPrepended(T valueToPrepend, List<T> list) {
        return ImmutableList.builderWithExpectedSize((int)(list.size() + 1)).add(valueToPrepend).addAll(list).build();
    }

    public static <K, V> Map<K, V> mapWith(Map<K, V> map, K keyToAppend, V valueToAppend) {
        return ImmutableMap.builderWithExpectedSize((int)(map.size() + 1)).putAll(map).put(keyToAppend, valueToAppend).buildKeepingLast();
    }

    public static sealed class OperatingSystem
    extends Enum<OperatingSystem> {
        public static final /* enum */ OperatingSystem LINUX = new OperatingSystem("linux");
        public static final /* enum */ OperatingSystem SOLARIS = new OperatingSystem("solaris");
        public static final /* enum */ OperatingSystem WINDOWS = new OperatingSystem("windows"){

            @Override
            protected String[] getURIOpenCommand(URI uri) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", uri.toString()};
            }
        };
        public static final /* enum */ OperatingSystem OSX = new OperatingSystem("mac"){

            @Override
            protected String[] getURIOpenCommand(URI uri) {
                return new String[]{"open", uri.toString()};
            }
        };
        public static final /* enum */ OperatingSystem UNKNOWN = new OperatingSystem("unknown");
        private final String name;
        private static final /* synthetic */ OperatingSystem[] field_1136;

        public static OperatingSystem[] values() {
            return (OperatingSystem[])field_1136.clone();
        }

        public static OperatingSystem valueOf(String string) {
            return Enum.valueOf(OperatingSystem.class, string);
        }

        OperatingSystem(String name) {
            this.name = name;
        }

        public void open(URI uri) {
            try {
                Process process = Runtime.getRuntime().exec(this.getURIOpenCommand(uri));
                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
            }
            catch (IOException iOException) {
                LOGGER.error("Couldn't open location '{}'", (Object)uri, (Object)iOException);
            }
        }

        public void open(File file) {
            this.open(file.toURI());
        }

        public void open(Path path) {
            this.open(path.toUri());
        }

        protected String[] getURIOpenCommand(URI uri) {
            String string = uri.toString();
            if ("file".equals(uri.getScheme())) {
                string = string.replace("file:", "file://");
            }
            return new String[]{"xdg-open", string};
        }

        public void open(String uri) {
            try {
                this.open(new URI(uri));
            }
            catch (IllegalArgumentException | URISyntaxException exception) {
                LOGGER.error("Couldn't open uri '{}'", (Object)uri, (Object)exception);
            }
        }

        public String getName() {
            return this.name;
        }

        private static /* synthetic */ OperatingSystem[] method_36579() {
            return new OperatingSystem[]{LINUX, SOLARIS, WINDOWS, OSX, UNKNOWN};
        }

        static {
            field_1136 = OperatingSystem.method_36579();
        }
    }
}

