/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.registry.RegistryKey;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public interface ErrorReporter {
    public static final ErrorReporter EMPTY = new ErrorReporter(){

        @Override
        public ErrorReporter makeChild(Context context) {
            return this;
        }

        @Override
        public void report(Error error) {
        }
    };

    public ErrorReporter makeChild(Context var1);

    public void report(Error var1);

    public static class Logging
    extends Impl
    implements AutoCloseable {
        private final Logger logger;

        public Logging(Logger logger) {
            this.logger = logger;
        }

        public Logging(Context context, Logger logger) {
            super(context);
            this.logger = logger;
        }

        @Override
        public void close() {
            if (!this.isEmpty()) {
                this.logger.warn("[{}] Serialization errors:\n{}", (Object)this.logger.getName(), (Object)this.getErrorsAsLongString());
            }
        }
    }

    public static class Impl
    implements ErrorReporter {
        public static final Context CONTEXT = () -> "";
        private final @Nullable Impl parent;
        private final Context context;
        private final Set<ErrorEntry> errors;

        public Impl() {
            this(CONTEXT);
        }

        public Impl(Context context) {
            this.parent = null;
            this.errors = new LinkedHashSet<ErrorEntry>();
            this.context = context;
        }

        private Impl(Impl parent, Context context) {
            this.errors = parent.errors;
            this.parent = parent;
            this.context = context;
        }

        @Override
        public ErrorReporter makeChild(Context context) {
            return new Impl(this, context);
        }

        @Override
        public void report(Error error) {
            this.errors.add(new ErrorEntry(this, error));
        }

        public boolean isEmpty() {
            return this.errors.isEmpty();
        }

        public void apply(BiConsumer<String, Error> consumer) {
            ArrayList<Context> list = new ArrayList<Context>();
            StringBuilder stringBuilder = new StringBuilder();
            for (ErrorEntry errorEntry : this.errors) {
                Impl impl = errorEntry.source;
                while (impl != null) {
                    list.add(impl.context);
                    impl = impl.parent;
                }
                for (int i = list.size() - 1; i >= 0; --i) {
                    stringBuilder.append(((Context)list.get(i)).getName());
                }
                consumer.accept(stringBuilder.toString(), errorEntry.error());
                stringBuilder.setLength(0);
                list.clear();
            }
        }

        public String getErrorsAsString() {
            HashMultimap multimap = HashMultimap.create();
            this.apply((arg_0, arg_1) -> ((Multimap)multimap).put(arg_0, arg_1));
            return multimap.asMap().entrySet().stream().map(entry -> " at " + (String)entry.getKey() + ": " + ((Collection)entry.getValue()).stream().map(Error::getMessage).collect(Collectors.joining("; "))).collect(Collectors.joining("\n"));
        }

        public String getErrorsAsLongString() {
            ArrayList<Context> list = new ArrayList<Context>();
            ErrorList errorList = new ErrorList(this.context);
            for (ErrorEntry errorEntry : this.errors) {
                Impl impl = errorEntry.source;
                while (impl != this) {
                    list.add(impl.context);
                    impl = impl.parent;
                }
                ErrorList errorList2 = errorList;
                for (int i = list.size() - 1; i >= 0; --i) {
                    errorList2 = errorList2.get((Context)list.get(i));
                }
                list.clear();
                errorList2.errors.add(errorEntry.error);
            }
            return String.join((CharSequence)"\n", errorList.getMessages());
        }

        static final class ErrorEntry
        extends Record {
            final Impl source;
            final Error error;

            ErrorEntry(Impl source, Error error) {
                this.source = source;
                this.error = error;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{ErrorEntry.class, "source;problem", "source", "error"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ErrorEntry.class, "source;problem", "source", "error"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ErrorEntry.class, "source;problem", "source", "error"}, this, object);
            }

            public Impl source() {
                return this.source;
            }

            public Error error() {
                return this.error;
            }
        }

        static final class ErrorList
        extends Record {
            private final Context element;
            final List<Error> errors;
            private final Map<Context, ErrorList> children;

            public ErrorList(Context context) {
                this(context, new ArrayList<Error>(), new LinkedHashMap<Context, ErrorList>());
            }

            private ErrorList(Context element, List<Error> errors, Map<Context, ErrorList> children) {
                this.element = element;
                this.errors = errors;
                this.children = children;
            }

            public ErrorList get(Context context) {
                return this.children.computeIfAbsent(context, ErrorList::new);
            }

            public List<String> getMessages() {
                int i = this.errors.size();
                int j = this.children.size();
                if (i == 0 && j == 0) {
                    return List.of();
                }
                if (i == 0 && j == 1) {
                    ArrayList<String> list = new ArrayList<String>();
                    this.children.forEach((context, errors) -> list.addAll(errors.getMessages()));
                    list.set(0, this.element.getName() + (String)list.get(0));
                    return list;
                }
                if (i == 1 && j == 0) {
                    return List.of(this.element.getName() + ": " + this.errors.getFirst().getMessage());
                }
                ArrayList<String> list = new ArrayList<String>();
                this.children.forEach((context, errors) -> list.addAll(errors.getMessages()));
                list.replaceAll(message -> "  " + message);
                for (Error error : this.errors) {
                    list.add("  " + error.getMessage());
                }
                list.addFirst(this.element.getName() + ":");
                return list;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{ErrorList.class, "element;problems;children", "element", "errors", "children"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ErrorList.class, "element;problems;children", "element", "errors", "children"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ErrorList.class, "element;problems;children", "element", "errors", "children"}, this, object);
            }

            public Context element() {
                return this.element;
            }

            public List<Error> errors() {
                return this.errors;
            }

            public Map<Context, ErrorList> children() {
                return this.children;
            }
        }
    }

    public record ReferenceLootTableContext(RegistryKey<?> id) implements Context
    {
        @Override
        public String getName() {
            return "->{" + String.valueOf(this.id.getValue()) + "@" + String.valueOf(this.id.getRegistry()) + "}";
        }
    }

    public record ListElementContext(int index) implements Context
    {
        @Override
        public String getName() {
            return "[" + this.index + "]";
        }
    }

    public record NamedListElementContext(String key, int index) implements Context
    {
        @Override
        public String getName() {
            return "." + this.key + "[" + this.index + "]";
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{NamedListElementContext.class, "name;index", "key", "index"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{NamedListElementContext.class, "name;index", "key", "index"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{NamedListElementContext.class, "name;index", "key", "index"}, this, object);
        }
    }

    public record MapElementContext(String key) implements Context
    {
        @Override
        public String getName() {
            return "." + this.key;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{MapElementContext.class, "name", "key"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{MapElementContext.class, "name", "key"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{MapElementContext.class, "name", "key"}, this, object);
        }
    }

    public record LootTableContext(RegistryKey<?> id) implements Context
    {
        @Override
        public String getName() {
            return "{" + String.valueOf(this.id.getValue()) + "@" + String.valueOf(this.id.getRegistry()) + "}";
        }
    }

    public record CriterionContext(String name) implements Context
    {
        @Override
        public String getName() {
            return this.name;
        }
    }

    @FunctionalInterface
    public static interface Context {
        public String getName();
    }

    public static interface Error {
        public String getMessage();
    }
}

