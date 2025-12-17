/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class BlockEntityBannerColorFix
extends ChoiceFix {
    public BlockEntityBannerColorFix(Schema schema, boolean bl) {
        super(schema, bl, "BlockEntityBannerColorFix", TypeReferences.BLOCK_ENTITY, "minecraft:banner");
    }

    public Dynamic<?> fixBannerColor(Dynamic<?> bannerDynamic) {
        bannerDynamic = bannerDynamic.update("Base", baseDynamic -> baseDynamic.createInt(15 - baseDynamic.asInt(0)));
        bannerDynamic = bannerDynamic.update("Patterns", patternsDynamic -> (Dynamic)DataFixUtils.orElse((Optional)patternsDynamic.asStreamOpt().map(stream -> stream.map(patternDynamic -> patternDynamic.update("Color", colorDynamic -> colorDynamic.createInt(15 - colorDynamic.asInt(0))))).map(arg_0 -> ((Dynamic)patternsDynamic).createList(arg_0)).result(), (Object)patternsDynamic));
        return bannerDynamic;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fixBannerColor);
    }
}

