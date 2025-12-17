/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.test;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TestBlock;
import net.minecraft.block.entity.TestBlockEntity;
import net.minecraft.block.enums.TestBlockMode;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestData;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.test.TestInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class BlockBasedTestInstance
extends TestInstance {
    public static final MapCodec<BlockBasedTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TestData.CODEC.forGetter(TestInstance::getData)).apply((Applicative)instance, BlockBasedTestInstance::new));

    public BlockBasedTestInstance(TestData<RegistryEntry<TestEnvironmentDefinition>> testData) {
        super(testData);
    }

    @Override
    public void start(TestContext context) {
        BlockPos blockPos = this.findStartBlockPos(context);
        TestBlockEntity testBlockEntity = context.getBlockEntity(blockPos, TestBlockEntity.class);
        testBlockEntity.trigger();
        context.forEachRemainingTick(() -> {
            boolean bl;
            List<BlockPos> list = this.findTestBlocks(context, TestBlockMode.ACCEPT);
            if (list.isEmpty()) {
                context.throwGameTestException(Text.translatable("test_block.error.missing", TestBlockMode.ACCEPT.getName()));
            }
            if (bl = list.stream().map(pos -> context.getBlockEntity((BlockPos)pos, TestBlockEntity.class)).anyMatch(TestBlockEntity::hasTriggered)) {
                context.complete();
            } else {
                this.handleTrigger(context, TestBlockMode.FAIL, testBlockEntity -> context.throwGameTestException(Text.literal(testBlockEntity.getMessage())));
                this.handleTrigger(context, TestBlockMode.LOG, TestBlockEntity::trigger);
            }
        });
    }

    private void handleTrigger(TestContext context, TestBlockMode mode, Consumer<TestBlockEntity> callback) {
        List<BlockPos> list = this.findTestBlocks(context, mode);
        for (BlockPos blockPos : list) {
            TestBlockEntity testBlockEntity = context.getBlockEntity(blockPos, TestBlockEntity.class);
            if (!testBlockEntity.hasTriggered()) continue;
            callback.accept(testBlockEntity);
            testBlockEntity.reset();
        }
    }

    private BlockPos findStartBlockPos(TestContext context) {
        List<BlockPos> list = this.findTestBlocks(context, TestBlockMode.START);
        if (list.isEmpty()) {
            context.throwGameTestException(Text.translatable("test_block.error.missing", TestBlockMode.START.getName()));
        }
        if (list.size() != 1) {
            context.throwGameTestException(Text.translatable("test_block.error.too_many", TestBlockMode.START.getName()));
        }
        return list.getFirst();
    }

    private List<BlockPos> findTestBlocks(TestContext context, TestBlockMode mode) {
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        context.forEachRelativePos(pos -> {
            BlockState blockState = context.getBlockState((BlockPos)pos);
            if (blockState.isOf(Blocks.TEST_BLOCK) && blockState.get(TestBlock.MODE) == mode) {
                list.add(pos.toImmutable());
            }
        });
        return list;
    }

    public MapCodec<BlockBasedTestInstance> getCodec() {
        return CODEC;
    }

    @Override
    protected MutableText getTypeDescription() {
        return Text.translatable("test_instance.type.block_based");
    }
}

