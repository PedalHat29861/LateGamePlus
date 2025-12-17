/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  io.netty.buffer.ByteBuf
 *  org.slf4j.Logger
 */
package net.minecraft.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;

public final class TypedEntityData<IdType>
implements TooltipAppender {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ID_KEY = "id";
    final IdType type;
    final NbtCompound nbt;

    public static <T> Codec<TypedEntityData<T>> createCodec(final Codec<T> typeCodec) {
        return new Codec<TypedEntityData<T>>(){

            public <V> DataResult<Pair<TypedEntityData<T>, V>> decode(DynamicOps<V> ops, V value) {
                return NbtComponent.COMPOUND_CODEC.decode(ops, value).flatMap(pair -> {
                    NbtCompound nbtCompound = ((NbtCompound)pair.getFirst()).copy();
                    NbtElement nbtElement = nbtCompound.remove(TypedEntityData.ID_KEY);
                    if (nbtElement == null) {
                        return DataResult.error(() -> "Expected 'id' field in " + String.valueOf(value));
                    }
                    return typeCodec.parse(1.toNbtOps(ops), (Object)nbtElement).map(object -> Pair.of(new TypedEntityData<Object>(object, nbtCompound), (Object)pair.getSecond()));
                });
            }

            public <V> DataResult<V> encode(TypedEntityData<T> typedEntityData, DynamicOps<V> dynamicOps, V object) {
                return typeCodec.encodeStart(1.toNbtOps(dynamicOps), typedEntityData.type).flatMap(id -> {
                    NbtCompound nbtCompound = typedEntityData.nbt.copy();
                    nbtCompound.put(TypedEntityData.ID_KEY, (NbtElement)id);
                    return NbtComponent.COMPOUND_CODEC.encode((Object)nbtCompound, dynamicOps, object);
                });
            }

            private static <T> DynamicOps<NbtElement> toNbtOps(DynamicOps<T> ops) {
                if (ops instanceof RegistryOps) {
                    RegistryOps registryOps = (RegistryOps)ops;
                    return registryOps.withDelegate(NbtOps.INSTANCE);
                }
                return NbtOps.INSTANCE;
            }

            public /* synthetic */ DataResult encode(Object prefix, DynamicOps ops, Object value) {
                return this.encode((TypedEntityData)prefix, ops, value);
            }
        };
    }

    public static <B extends ByteBuf, T> PacketCodec<B, TypedEntityData<T>> createPacketCodec(PacketCodec<B, T> typePacketCodec) {
        return PacketCodec.tuple(typePacketCodec, TypedEntityData::getType, PacketCodecs.NBT_COMPOUND, TypedEntityData::getNbtWithoutIdInternal, TypedEntityData::new);
    }

    TypedEntityData(IdType type, NbtCompound nbt) {
        this.type = type;
        this.nbt = TypedEntityData.stripId(nbt);
    }

    public static <T> TypedEntityData<T> create(T type, NbtCompound nbt) {
        return new TypedEntityData<T>(type, nbt);
    }

    private static NbtCompound stripId(NbtCompound nbt) {
        if (nbt.contains(ID_KEY)) {
            NbtCompound nbtCompound = nbt.copy();
            nbtCompound.remove(ID_KEY);
            return nbtCompound;
        }
        return nbt;
    }

    public IdType getType() {
        return this.type;
    }

    public boolean contains(String key) {
        return this.nbt.contains(key);
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof TypedEntityData) {
            TypedEntityData typedEntityData = (TypedEntityData)other;
            return this.type == typedEntityData.type && this.nbt.equals(typedEntityData.nbt);
        }
        return false;
    }

    public int hashCode() {
        return 31 * this.type.hashCode() + this.nbt.hashCode();
    }

    public String toString() {
        return String.valueOf(this.type) + " " + String.valueOf(this.nbt);
    }

    public void applyToEntity(Entity entity) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getErrorReporterContext(), LOGGER);){
            NbtWriteView nbtWriteView = NbtWriteView.create(logging, entity.getRegistryManager());
            entity.writeData(nbtWriteView);
            NbtCompound nbtCompound = nbtWriteView.getNbt();
            UUID uUID = entity.getUuid();
            nbtCompound.copyFrom(this.getNbtWithoutId());
            entity.readData(NbtReadView.create(logging, entity.getRegistryManager(), nbtCompound));
            entity.setUuid(uUID);
        }
    }

    /*
     * Exception decompiling
     */
    public boolean applyToBlockEntity(BlockEntity blockEntity, RegistryWrapper.WrapperLookup registryLookup) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [5[CATCHBLOCK]], but top level block is 2[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private NbtCompound getNbtWithoutIdInternal() {
        return this.nbt;
    }

    @Deprecated
    public NbtCompound getNbtWithoutId() {
        return this.nbt;
    }

    public NbtCompound copyNbtWithoutId() {
        return this.nbt.copy();
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        if (this.type.getClass() == EntityType.class) {
            EntityType entityType = (EntityType)this.type;
            if (context.isDifficultyPeaceful() && !entityType.isAllowedInPeaceful()) {
                textConsumer.accept(Text.translatable("item.spawn_egg.peaceful").formatted(Formatting.RED));
            }
        }
    }

    private static /* synthetic */ String method_72542() {
        return "(rollback)";
    }
}

