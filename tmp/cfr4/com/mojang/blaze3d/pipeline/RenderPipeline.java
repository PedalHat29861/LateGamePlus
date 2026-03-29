/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SharedConstants
 *  net.minecraft.resources.Identifier
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class RenderPipeline {
    private final Identifier location;
    private final Identifier vertexShader;
    private final Identifier fragmentShader;
    private final ShaderDefines shaderDefines;
    private final List<String> samplers;
    private final List<UniformDescription> uniforms;
    private final @Nullable DepthStencilState depthStencilState;
    private final PolygonMode polygonMode;
    private final boolean cull;
    private final ColorTargetState colorTargetState;
    private final VertexFormat vertexFormat;
    private final VertexFormat.Mode vertexFormatMode;
    private final int sortKey;
    private static int sortKeySeed;

    protected RenderPipeline(Identifier location, Identifier vertexShader, Identifier fragmentShader, ShaderDefines shaderDefines, List<String> samplers, List<UniformDescription> uniforms, ColorTargetState colorTargetState, @Nullable DepthStencilState depthStencilState, PolygonMode polygonMode, boolean cull, VertexFormat vertexFormat, VertexFormat.Mode vertexFormatMode, int sortKey) {
        this.location = location;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
        this.shaderDefines = shaderDefines;
        this.samplers = samplers;
        this.uniforms = uniforms;
        this.depthStencilState = depthStencilState;
        this.polygonMode = polygonMode;
        this.cull = cull;
        this.colorTargetState = colorTargetState;
        this.vertexFormat = vertexFormat;
        this.vertexFormatMode = vertexFormatMode;
        this.sortKey = sortKey;
    }

    public int getSortKey() {
        return SharedConstants.DEBUG_SHUFFLE_UI_RENDERING_ORDER ? super.hashCode() * (sortKeySeed + 1) : this.sortKey;
    }

    public static void updateSortKeySeed() {
        sortKeySeed = Math.round(100000.0f * (float)Math.random());
    }

    public String toString() {
        return this.location.toString();
    }

    public PolygonMode getPolygonMode() {
        return this.polygonMode;
    }

    public boolean isCull() {
        return this.cull;
    }

    public ColorTargetState getColorTargetState() {
        return this.colorTargetState;
    }

    public @Nullable DepthStencilState getDepthStencilState() {
        return this.depthStencilState;
    }

    public Identifier getLocation() {
        return this.location;
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    public VertexFormat.Mode getVertexFormatMode() {
        return this.vertexFormatMode;
    }

    public Identifier getVertexShader() {
        return this.vertexShader;
    }

    public Identifier getFragmentShader() {
        return this.fragmentShader;
    }

    public ShaderDefines getShaderDefines() {
        return this.shaderDefines;
    }

    public List<String> getSamplers() {
        return this.samplers;
    }

    public List<UniformDescription> getUniforms() {
        return this.uniforms;
    }

    public boolean wantsDepthTexture() {
        return this.depthStencilState != null;
    }

    public static Builder builder(Snippet ... snippets) {
        Builder builder = new Builder();
        for (Snippet snippet : snippets) {
            builder.withSnippet(snippet);
        }
        return builder;
    }

    public static class Builder {
        private static int nextPipelineSortKey;
        private Optional<Identifier> location = Optional.empty();
        private Optional<Identifier> fragmentShader = Optional.empty();
        private Optional<Identifier> vertexShader = Optional.empty();
        private Optional<ShaderDefines.Builder> definesBuilder = Optional.empty();
        private Optional<List<String>> samplers = Optional.empty();
        private Optional<List<UniformDescription>> uniforms = Optional.empty();
        private Optional<DepthStencilState> depthStencilState = Optional.empty();
        private Optional<PolygonMode> polygonMode = Optional.empty();
        private Optional<Boolean> cull = Optional.empty();
        private Optional<ColorTargetState> colorTargetState = Optional.empty();
        private Optional<VertexFormat> vertexFormat = Optional.empty();
        private Optional<VertexFormat.Mode> vertexFormatMode = Optional.empty();

        private Builder() {
        }

        public Builder withLocation(String location) {
            this.location = Optional.of(Identifier.withDefaultNamespace((String)location));
            return this;
        }

        public Builder withLocation(Identifier location) {
            this.location = Optional.of(location);
            return this;
        }

        public Builder withFragmentShader(String fragmentShader) {
            this.fragmentShader = Optional.of(Identifier.withDefaultNamespace((String)fragmentShader));
            return this;
        }

        public Builder withFragmentShader(Identifier fragmentShader) {
            this.fragmentShader = Optional.of(fragmentShader);
            return this;
        }

        public Builder withVertexShader(String vertexShader) {
            this.vertexShader = Optional.of(Identifier.withDefaultNamespace((String)vertexShader));
            return this;
        }

        public Builder withVertexShader(Identifier vertexShader) {
            this.vertexShader = Optional.of(vertexShader);
            return this;
        }

        public Builder withShaderDefine(String key) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }
            this.definesBuilder.get().define(key);
            return this;
        }

        public Builder withShaderDefine(String key, int value) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }
            this.definesBuilder.get().define(key, value);
            return this;
        }

        public Builder withShaderDefine(String key, float value) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }
            this.definesBuilder.get().define(key, value);
            return this;
        }

        public Builder withSampler(String sampler) {
            if (this.samplers.isEmpty()) {
                this.samplers = Optional.of(new ArrayList());
            }
            this.samplers.get().add(sampler);
            return this;
        }

        public Builder withUniform(String name, UniformType type) {
            if (this.uniforms.isEmpty()) {
                this.uniforms = Optional.of(new ArrayList());
            }
            if (type == UniformType.TEXEL_BUFFER) {
                throw new IllegalArgumentException("Cannot use texel buffer without specifying texture format");
            }
            this.uniforms.get().add(new UniformDescription(name, type));
            return this;
        }

        public Builder withUniform(String name, UniformType type, TextureFormat format) {
            if (this.uniforms.isEmpty()) {
                this.uniforms = Optional.of(new ArrayList());
            }
            if (type != UniformType.TEXEL_BUFFER) {
                throw new IllegalArgumentException("Only texel buffer can specify texture format");
            }
            this.uniforms.get().add(new UniformDescription(name, format));
            return this;
        }

        public Builder withPolygonMode(PolygonMode polygonMode) {
            this.polygonMode = Optional.of(polygonMode);
            return this;
        }

        public Builder withCull(boolean cull) {
            this.cull = Optional.of(cull);
            return this;
        }

        public Builder withColorTargetState(ColorTargetState colorTargetState) {
            this.colorTargetState = Optional.of(colorTargetState);
            return this;
        }

        public Builder withDepthStencilState(DepthStencilState depthStencilState) {
            this.depthStencilState = Optional.of(depthStencilState);
            return this;
        }

        public Builder withDepthStencilState(Optional<DepthStencilState> depthStencilState) {
            this.depthStencilState = depthStencilState;
            return this;
        }

        public Builder withVertexFormat(VertexFormat vertexFormat, VertexFormat.Mode vertexFormatMode) {
            this.vertexFormat = Optional.of(vertexFormat);
            this.vertexFormatMode = Optional.of(vertexFormatMode);
            return this;
        }

        private void withSnippet(Snippet snippet) {
            if (snippet.vertexShader.isPresent()) {
                this.vertexShader = snippet.vertexShader;
            }
            if (snippet.fragmentShader.isPresent()) {
                this.fragmentShader = snippet.fragmentShader;
            }
            if (snippet.shaderDefines.isPresent()) {
                if (this.definesBuilder.isEmpty()) {
                    this.definesBuilder = Optional.of(ShaderDefines.builder());
                }
                ShaderDefines snippetDefines = snippet.shaderDefines.get();
                for (Map.Entry<String, String> snippetValue : snippetDefines.values().entrySet()) {
                    this.definesBuilder.get().define(snippetValue.getKey(), snippetValue.getValue());
                }
                for (String flag : snippetDefines.flags()) {
                    this.definesBuilder.get().define(flag);
                }
            }
            snippet.samplers.ifPresent(builderSamplers -> {
                if (this.samplers.isPresent()) {
                    this.samplers.get().addAll((Collection<String>)builderSamplers);
                } else {
                    this.samplers = Optional.of(new ArrayList(builderSamplers));
                }
            });
            snippet.uniforms.ifPresent(builderUniforms -> {
                if (this.uniforms.isPresent()) {
                    this.uniforms.get().addAll((Collection<UniformDescription>)builderUniforms);
                } else {
                    this.uniforms = Optional.of(new ArrayList(builderUniforms));
                }
            });
            if (snippet.depthStencilState.isPresent()) {
                this.depthStencilState = snippet.depthStencilState;
            }
            if (snippet.cull.isPresent()) {
                this.cull = snippet.cull;
            }
            if (snippet.colorTargetState.isPresent()) {
                this.colorTargetState = snippet.colorTargetState;
            }
            if (snippet.vertexFormat.isPresent()) {
                this.vertexFormat = snippet.vertexFormat;
            }
            if (snippet.vertexFormatMode.isPresent()) {
                this.vertexFormatMode = snippet.vertexFormatMode;
            }
            if (snippet.polygonMode.isPresent()) {
                this.polygonMode = snippet.polygonMode;
            }
        }

        public Snippet buildSnippet() {
            return new Snippet(this.vertexShader, this.fragmentShader, this.definesBuilder.map(ShaderDefines.Builder::build), this.samplers.map(Collections::unmodifiableList), this.uniforms.map(Collections::unmodifiableList), this.colorTargetState, this.depthStencilState, this.polygonMode, this.cull, this.vertexFormat, this.vertexFormatMode);
        }

        public RenderPipeline build() {
            if (this.location.isEmpty()) {
                throw new IllegalStateException("Missing location");
            }
            if (this.vertexShader.isEmpty()) {
                throw new IllegalStateException("Missing vertex shader");
            }
            if (this.fragmentShader.isEmpty()) {
                throw new IllegalStateException("Missing fragment shader");
            }
            if (this.vertexFormat.isEmpty()) {
                throw new IllegalStateException("Missing vertex buffer format");
            }
            if (this.vertexFormatMode.isEmpty()) {
                throw new IllegalStateException("Missing vertex mode");
            }
            return new RenderPipeline(this.location.get(), this.vertexShader.get(), this.fragmentShader.get(), this.definesBuilder.orElse(ShaderDefines.builder()).build(), List.copyOf(this.samplers.orElse(new ArrayList())), this.uniforms.orElse(Collections.emptyList()), this.colorTargetState.orElse(ColorTargetState.DEFAULT), this.depthStencilState.orElse(null), this.polygonMode.orElse(PolygonMode.FILL), this.cull.orElse(true), this.vertexFormat.get(), this.vertexFormatMode.get(), nextPipelineSortKey++);
        }
    }

    public record Snippet(Optional<Identifier> vertexShader, Optional<Identifier> fragmentShader, Optional<ShaderDefines> shaderDefines, Optional<List<String>> samplers, Optional<List<UniformDescription>> uniforms, Optional<ColorTargetState> colorTargetState, Optional<DepthStencilState> depthStencilState, Optional<PolygonMode> polygonMode, Optional<Boolean> cull, Optional<VertexFormat> vertexFormat, Optional<VertexFormat.Mode> vertexFormatMode) {
    }

    public record UniformDescription(String name, UniformType type, @Nullable TextureFormat textureFormat) {
        public UniformDescription(String name, UniformType type) {
            this(name, type, null);
            if (type == UniformType.TEXEL_BUFFER) {
                throw new IllegalArgumentException("Texel buffer needs a texture format");
            }
        }

        public UniformDescription(String name, TextureFormat textureFormat) {
            this(name, UniformType.TEXEL_BUFFER, textureFormat);
        }
    }
}

