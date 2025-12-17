/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.NbtDataSource;
import net.minecraft.text.NbtDataSourceTypes;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TextContent;
import net.minecraft.text.Texts;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class NbtTextContent
implements TextContent {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<NbtTextContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("nbt").forGetter(NbtTextContent::getPath), (App)Codec.BOOL.lenientOptionalFieldOf("interpret", (Object)false).forGetter(NbtTextContent::shouldInterpret), (App)TextCodecs.CODEC.lenientOptionalFieldOf("separator").forGetter(NbtTextContent::getSeparator), (App)NbtDataSourceTypes.CODEC.forGetter(NbtTextContent::getDataSource)).apply((Applicative)instance, NbtTextContent::new));
    private final boolean interpret;
    private final Optional<Text> separator;
    private final String rawPath;
    private final NbtDataSource dataSource;
    protected final @Nullable NbtPathArgumentType.NbtPath path;

    public NbtTextContent(String rawPath, boolean interpret, Optional<Text> separator, NbtDataSource dataSource) {
        this(rawPath, NbtTextContent.parsePath(rawPath), interpret, separator, dataSource);
    }

    private NbtTextContent(String rawPath, @Nullable NbtPathArgumentType.NbtPath path, boolean interpret, Optional<Text> separator, NbtDataSource dataSource) {
        this.rawPath = rawPath;
        this.path = path;
        this.interpret = interpret;
        this.separator = separator;
        this.dataSource = dataSource;
    }

    private static @Nullable NbtPathArgumentType.NbtPath parsePath(String rawPath) {
        try {
            return new NbtPathArgumentType().parse(new StringReader(rawPath));
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    public String getPath() {
        return this.rawPath;
    }

    public boolean shouldInterpret() {
        return this.interpret;
    }

    public Optional<Text> getSeparator() {
        return this.separator;
    }

    public NbtDataSource getDataSource() {
        return this.dataSource;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NbtTextContent)) return false;
        NbtTextContent nbtTextContent = (NbtTextContent)o;
        if (!this.dataSource.equals(nbtTextContent.dataSource)) return false;
        if (!this.separator.equals(nbtTextContent.separator)) return false;
        if (this.interpret != nbtTextContent.interpret) return false;
        if (!this.rawPath.equals(nbtTextContent.rawPath)) return false;
        return true;
    }

    public int hashCode() {
        int i = this.interpret ? 1 : 0;
        i = 31 * i + this.separator.hashCode();
        i = 31 * i + this.rawPath.hashCode();
        i = 31 * i + this.dataSource.hashCode();
        return i;
    }

    public String toString() {
        return "nbt{" + String.valueOf(this.dataSource) + ", interpreting=" + this.interpret + ", separator=" + String.valueOf(this.separator) + "}";
    }

    @Override
    public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        if (source == null || this.path == null) {
            return Text.empty();
        }
        Stream<String> stream = this.dataSource.get(source).flatMap(nbt -> {
            try {
                return this.path.get((NbtElement)nbt).stream();
            }
            catch (CommandSyntaxException commandSyntaxException) {
                return Stream.empty();
            }
        });
        if (this.interpret) {
            RegistryOps<NbtElement> registryOps = source.getRegistryManager().getOps(NbtOps.INSTANCE);
            Text text2 = (Text)DataFixUtils.orElse(Texts.parse(source, this.separator, sender, depth), (Object)Texts.DEFAULT_SEPARATOR_TEXT);
            return stream.flatMap(nbt -> {
                try {
                    Text text = (Text)TextCodecs.CODEC.parse((DynamicOps)registryOps, nbt).getOrThrow();
                    return Stream.of(Texts.parse(source, text, sender, depth));
                }
                catch (Exception exception) {
                    LOGGER.warn("Failed to parse component: {}", nbt, (Object)exception);
                    return Stream.of(new MutableText[0]);
                }
            }).reduce((accumulator, current) -> accumulator.append(text2).append((Text)current)).orElseGet(Text::empty);
        }
        Stream<String> stream2 = stream.map(NbtTextContent::asString);
        return Texts.parse(source, this.separator, sender, depth).map(text -> stream2.map(Text::literal).reduce((accumulator, current) -> accumulator.append((Text)text).append((Text)current)).orElseGet(Text::empty)).orElseGet(() -> Text.literal(stream2.collect(Collectors.joining(", "))));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static String asString(NbtElement nbt) {
        if (!(nbt instanceof NbtString)) return nbt.toString();
        NbtString nbtString = (NbtString)nbt;
        try {
            String string = nbtString.value();
            return string;
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    public MapCodec<NbtTextContent> getCodec() {
        return CODEC;
    }
}

