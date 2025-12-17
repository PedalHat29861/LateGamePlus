/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.DeprecatedLanguageData;
import net.minecraft.util.JsonHelper;
import org.slf4j.Logger;

public abstract class Language {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Pattern TOKEN_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    public static final String DEFAULT_LANGUAGE = "en_us";
    private static volatile Language instance = Language.create();

    private static Language create() {
        DeprecatedLanguageData deprecatedLanguageData = DeprecatedLanguageData.create();
        HashMap<String, String> map = new HashMap<String, String>();
        BiConsumer<String, String> biConsumer = map::put;
        Language.load(biConsumer, "/assets/minecraft/lang/en_us.json");
        deprecatedLanguageData.apply(map);
        final Map<String, String> map2 = Map.copyOf(map);
        return new Language(){

            @Override
            public String get(String key, String fallback) {
                return map2.getOrDefault(key, fallback);
            }

            @Override
            public boolean hasTranslation(String key) {
                return map2.containsKey(key);
            }

            @Override
            public boolean isRightToLeft() {
                return false;
            }

            @Override
            public OrderedText reorder(StringVisitable text) {
                return visitor -> text.visit((style, string) -> TextVisitFactory.visitFormatted(string, style, visitor) ? Optional.empty() : StringVisitable.TERMINATE_VISIT, Style.EMPTY).isPresent();
            }
        };
    }

    private static void load(BiConsumer<String, String> entryConsumer, String path) {
        try (InputStream inputStream = Language.class.getResourceAsStream(path);){
            Language.load(inputStream, entryConsumer);
        }
        catch (JsonParseException | IOException exception) {
            LOGGER.error("Couldn't read strings from {}", (Object)path, (Object)exception);
        }
    }

    public static void load(InputStream inputStream, BiConsumer<String, String> entryConsumer) {
        JsonObject jsonObject = (JsonObject)GSON.fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
        for (Map.Entry entry : jsonObject.entrySet()) {
            String string = TOKEN_PATTERN.matcher(JsonHelper.asString((JsonElement)entry.getValue(), (String)entry.getKey())).replaceAll("%$1s");
            entryConsumer.accept((String)entry.getKey(), string);
        }
    }

    public static Language getInstance() {
        return instance;
    }

    public static void setInstance(Language language) {
        instance = language;
    }

    public String get(String key) {
        return this.get(key, key);
    }

    public abstract String get(String var1, String var2);

    public abstract boolean hasTranslation(String var1);

    public abstract boolean isRightToLeft();

    public abstract OrderedText reorder(StringVisitable var1);

    public List<OrderedText> reorder(List<StringVisitable> texts) {
        return (List)texts.stream().map(this::reorder).collect(ImmutableList.toImmutableList());
    }
}

