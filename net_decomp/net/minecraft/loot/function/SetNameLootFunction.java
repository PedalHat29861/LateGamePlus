/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.loot.function;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.context.ContextParameter;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SetNameLootFunction
extends ConditionalLootFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SetNameLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetNameLootFunction.addConditionsField(instance).and(instance.group((App)TextCodecs.CODEC.optionalFieldOf("name").forGetter(function -> function.name), (App)LootContext.EntityReference.CODEC.optionalFieldOf("entity").forGetter(function -> function.entity), (App)Target.CODEC.optionalFieldOf("target", (Object)Target.CUSTOM_NAME).forGetter(function -> function.target))).apply((Applicative)instance, SetNameLootFunction::new));
    private final Optional<Text> name;
    private final Optional<LootContext.EntityReference> entity;
    private final Target target;

    private SetNameLootFunction(List<LootCondition> conditions, Optional<Text> name, Optional<LootContext.EntityReference> entity, Target target) {
        super(conditions);
        this.name = name;
        this.entity = entity;
        this.target = target;
    }

    public LootFunctionType<SetNameLootFunction> getType() {
        return LootFunctionTypes.SET_NAME;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return this.entity.map(entity -> Set.of(entity.contextParam())).orElse(Set.of());
    }

    public static UnaryOperator<Text> applySourceEntity(LootContext context, @Nullable LootContext.EntityReference sourceEntity) {
        Entity entity;
        if (sourceEntity != null && (entity = context.get(sourceEntity.contextParam())) != null) {
            ServerCommandSource serverCommandSource = entity.getCommandSource(context.getWorld()).withPermissions(LeveledPermissionPredicate.GAMEMASTERS);
            return textComponent -> {
                try {
                    return Texts.parse(serverCommandSource, textComponent, entity, 0);
                }
                catch (CommandSyntaxException commandSyntaxException) {
                    LOGGER.warn("Failed to resolve text component", (Throwable)commandSyntaxException);
                    return textComponent;
                }
            };
        }
        return textComponent -> textComponent;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        this.name.ifPresent(name -> stack.set(this.target.getComponentType(), (Text)SetNameLootFunction.applySourceEntity(context, this.entity.orElse(null)).apply((Text)name)));
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(Text name, Target target) {
        return SetNameLootFunction.builder(conditions -> new SetNameLootFunction((List<LootCondition>)conditions, Optional.of(name), Optional.empty(), target));
    }

    public static ConditionalLootFunction.Builder<?> builder(Text name, Target target, LootContext.EntityReference entity) {
        return SetNameLootFunction.builder(conditions -> new SetNameLootFunction((List<LootCondition>)conditions, Optional.of(name), Optional.of(entity), target));
    }

    public static final class Target
    extends Enum<Target>
    implements StringIdentifiable {
        public static final /* enum */ Target CUSTOM_NAME = new Target("custom_name");
        public static final /* enum */ Target ITEM_NAME = new Target("item_name");
        public static final Codec<Target> CODEC;
        private final String id;
        private static final /* synthetic */ Target[] field_50214;

        public static Target[] values() {
            return (Target[])field_50214.clone();
        }

        public static Target valueOf(String string) {
            return Enum.valueOf(Target.class, string);
        }

        private Target(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        public ComponentType<Text> getComponentType() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 1 -> DataComponentTypes.ITEM_NAME;
                case 0 -> DataComponentTypes.CUSTOM_NAME;
            };
        }

        private static /* synthetic */ Target[] method_58735() {
            return new Target[]{CUSTOM_NAME, ITEM_NAME};
        }

        static {
            field_50214 = Target.method_58735();
            CODEC = StringIdentifiable.createCodec(Target::values);
        }
    }
}

