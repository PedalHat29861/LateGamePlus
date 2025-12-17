/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancement;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.LazyContainer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public record AdvancementRewards(int experience, List<RegistryKey<LootTable>> loot, List<RegistryKey<Recipe<?>>> recipes, Optional<LazyContainer> function) {
    public static final Codec<AdvancementRewards> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.optionalFieldOf("experience", (Object)0).forGetter(AdvancementRewards::experience), (App)LootTable.TABLE_KEY.listOf().optionalFieldOf("loot", List.of()).forGetter(AdvancementRewards::loot), (App)Recipe.KEY_CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(AdvancementRewards::recipes), (App)LazyContainer.CODEC.optionalFieldOf("function").forGetter(AdvancementRewards::function)).apply((Applicative)instance, AdvancementRewards::new));
    public static final AdvancementRewards NONE = new AdvancementRewards(0, List.of(), List.of(), Optional.empty());

    public void apply(ServerPlayerEntity player) {
        player.addExperience(this.experience);
        ServerWorld serverWorld = player.getEntityWorld();
        MinecraftServer minecraftServer = serverWorld.getServer();
        LootWorldContext lootWorldContext = new LootWorldContext.Builder(serverWorld).add(LootContextParameters.THIS_ENTITY, player).add(LootContextParameters.ORIGIN, player.getEntityPos()).build(LootContextTypes.ADVANCEMENT_REWARD);
        boolean bl = false;
        for (RegistryKey<LootTable> registryKey : this.loot) {
            for (ItemStack itemStack : minecraftServer.getReloadableRegistries().getLootTable(registryKey).generateLoot(lootWorldContext)) {
                if (player.giveItemStack(itemStack)) {
                    serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                    bl = true;
                    continue;
                }
                ItemEntity itemEntity = player.dropItem(itemStack, false);
                if (itemEntity == null) continue;
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
            }
        }
        if (bl) {
            player.currentScreenHandler.sendContentUpdates();
        }
        if (!this.recipes.isEmpty()) {
            player.unlockRecipes(this.recipes);
        }
        this.function.flatMap(function -> function.get(minecraftServer.getCommandFunctionManager())).ifPresent(function -> minecraftServer.getCommandFunctionManager().execute((CommandFunction<ServerCommandSource>)function, player.getCommandSource().withSilent().withPermissions(LeveledPermissionPredicate.GAMEMASTERS)));
    }

    public static class Builder {
        private int experience;
        private final ImmutableList.Builder<RegistryKey<LootTable>> loot = ImmutableList.builder();
        private final ImmutableList.Builder<RegistryKey<Recipe<?>>> recipes = ImmutableList.builder();
        private Optional<Identifier> function = Optional.empty();

        public static Builder experience(int experience) {
            return new Builder().setExperience(experience);
        }

        public Builder setExperience(int experience) {
            this.experience += experience;
            return this;
        }

        public static Builder loot(RegistryKey<LootTable> loot) {
            return new Builder().addLoot(loot);
        }

        public Builder addLoot(RegistryKey<LootTable> loot) {
            this.loot.add(loot);
            return this;
        }

        public static Builder recipe(RegistryKey<Recipe<?>> recipeKey) {
            return new Builder().addRecipe(recipeKey);
        }

        public Builder addRecipe(RegistryKey<Recipe<?>> recipeKey) {
            this.recipes.add(recipeKey);
            return this;
        }

        public static Builder function(Identifier function) {
            return new Builder().setFunction(function);
        }

        public Builder setFunction(Identifier function) {
            this.function = Optional.of(function);
            return this;
        }

        public AdvancementRewards build() {
            return new AdvancementRewards(this.experience, (List<RegistryKey<LootTable>>)this.loot.build(), (List<RegistryKey<Recipe<?>>>)this.recipes.build(), this.function.map(LazyContainer::new));
        }
    }
}

