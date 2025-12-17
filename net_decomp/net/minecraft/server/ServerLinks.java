/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.server;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.function.ValueLists;

public record ServerLinks(List<Entry> entries) {
    public static final ServerLinks EMPTY = new ServerLinks(List.of());
    public static final PacketCodec<ByteBuf, Either<Known, Text>> TYPE_CODEC = PacketCodecs.either(Known.CODEC, TextCodecs.PACKET_CODEC);
    public static final PacketCodec<ByteBuf, List<StringifiedEntry>> LIST_CODEC = StringifiedEntry.CODEC.collect(PacketCodecs.toList());

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public Optional<Entry> getEntryFor(Known known) {
        return this.entries.stream().filter(entry -> (Boolean)entry.type.map(type -> type == known, text -> false)).findFirst();
    }

    public List<StringifiedEntry> getLinks() {
        return this.entries.stream().map(entry -> new StringifiedEntry(entry.type, entry.link.toString())).toList();
    }

    public static final class Known
    extends Enum<Known> {
        public static final /* enum */ Known BUG_REPORT = new Known(0, "report_bug");
        public static final /* enum */ Known COMMUNITY_GUIDELINES = new Known(1, "community_guidelines");
        public static final /* enum */ Known SUPPORT = new Known(2, "support");
        public static final /* enum */ Known STATUS = new Known(3, "status");
        public static final /* enum */ Known FEEDBACK = new Known(4, "feedback");
        public static final /* enum */ Known COMMUNITY = new Known(5, "community");
        public static final /* enum */ Known WEBSITE = new Known(6, "website");
        public static final /* enum */ Known FORUMS = new Known(7, "forums");
        public static final /* enum */ Known NEWS = new Known(8, "news");
        public static final /* enum */ Known ANNOUNCEMENTS = new Known(9, "announcements");
        private static final IntFunction<Known> FROM_ID;
        public static final PacketCodec<ByteBuf, Known> CODEC;
        private final int id;
        private final String name;
        private static final /* synthetic */ Known[] field_51986;

        public static Known[] values() {
            return (Known[])field_51986.clone();
        }

        public static Known valueOf(String string) {
            return Enum.valueOf(Known.class, string);
        }

        private Known(int id, String name) {
            this.id = id;
            this.name = name;
        }

        private Text getText() {
            return Text.translatable("known_server_link." + this.name);
        }

        public Entry createEntry(URI link) {
            return Entry.create(this, link);
        }

        private static /* synthetic */ Known[] method_60669() {
            return new Known[]{BUG_REPORT, COMMUNITY_GUIDELINES, SUPPORT, STATUS, FEEDBACK, COMMUNITY, WEBSITE, FORUMS, NEWS, ANNOUNCEMENTS};
        }

        static {
            field_51986 = Known.method_60669();
            FROM_ID = ValueLists.createIndexToValueFunction(known -> known.id, Known.values(), ValueLists.OutOfBoundsHandling.ZERO);
            CODEC = PacketCodecs.indexed(FROM_ID, known -> known.id);
        }
    }

    public record StringifiedEntry(Either<Known, Text> type, String link) {
        public static final PacketCodec<ByteBuf, StringifiedEntry> CODEC = PacketCodec.tuple(TYPE_CODEC, StringifiedEntry::type, PacketCodecs.STRING, StringifiedEntry::link, StringifiedEntry::new);
    }

    public static final class Entry
    extends Record {
        final Either<Known, Text> type;
        final URI link;

        public Entry(Either<Known, Text> type, URI link) {
            this.type = type;
            this.link = link;
        }

        public static Entry create(Known known, URI link) {
            return new Entry((Either<Known, Text>)Either.left((Object)((Object)known)), link);
        }

        public static Entry create(Text name, URI link) {
            return new Entry((Either<Known, Text>)Either.right((Object)name), link);
        }

        public Text getText() {
            return (Text)this.type.map(Known::getText, text -> text);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "type;link", "type", "link"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "type;link", "type", "link"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "type;link", "type", "link"}, this, object);
        }

        public Either<Known, Text> type() {
            return this.type;
        }

        public URI link() {
            return this.link;
        }
    }
}

