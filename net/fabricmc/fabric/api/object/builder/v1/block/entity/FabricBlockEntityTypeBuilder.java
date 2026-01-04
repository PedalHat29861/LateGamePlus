/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.object.builder.v1.block.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.mojang.datafixers.types.Type;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.impl.object.builder.ExtendedBlockEntityType;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2680;

/**
 * Use this builder to create a {@link class_2591}.
 */
public final class FabricBlockEntityTypeBuilder<T extends class_2586> {
	private final Factory<? extends T> factory;
	private final Set<class_2248> blocks = new HashSet<>();
	@Nullable
	private Boolean canPotentiallyExecuteCommands = null;

	private FabricBlockEntityTypeBuilder(Factory<? extends T> factory) {
		this.factory = factory;
	}

	public static <T extends class_2586> FabricBlockEntityTypeBuilder<T> create(Factory<? extends T> factory, class_2248... blocks) {
		return new FabricBlockEntityTypeBuilder<T>(factory).addBlocks(blocks);
	}

	/**
	 * Adds a supported block for the block entity type.
	 *
	 * @param block the supported block
	 * @return this builder
	 */
	public FabricBlockEntityTypeBuilder<T> addBlock(class_2248 block) {
		this.blocks.add(block);
		return this;
	}

	/**
	 * Adds supported blocks for the block entity type.
	 *
	 * @param blocks the supported blocks
	 * @return this builder
	 */
	public FabricBlockEntityTypeBuilder<T> addBlocks(class_2248... blocks) {
		Collections.addAll(this.blocks, blocks);
		return this;
	}

	/**
	 * Adds supported blocks for the block entity type.
	 *
	 * @param blocks the supported blocks
	 * @return this builder
	 */
	public FabricBlockEntityTypeBuilder<T> addBlocks(Collection<? extends class_2248> blocks) {
		this.blocks.addAll(blocks);
		return this;
	}

	/**
	 * Makes the built {@link class_2591} return {@code true} from
	 * {@link class_2591#method_65166()}.
	 *
	 * @param canPotentiallyExecuteCommands whether the block entity is able to execute commands
	 * @return this builder
	 */
	public FabricBlockEntityTypeBuilder<T> canPotentiallyExecuteCommands(boolean canPotentiallyExecuteCommands) {
		this.canPotentiallyExecuteCommands = canPotentiallyExecuteCommands;
		return this;
	}

	public class_2591<T> build() {
		return new ExtendedBlockEntityType<>(factory::create, new HashSet<>(blocks), canPotentiallyExecuteCommands);
	}

	/**
	 * @deprecated Use {@link #build()} instead.
	 */
	@Deprecated
	public class_2591<T> build(@Nullable Type<?> type) {
		return build();
	}

	@FunctionalInterface
	public interface Factory<T extends class_2586> {
		T create(class_2338 blockPos, class_2680 blockState);
	}
}
