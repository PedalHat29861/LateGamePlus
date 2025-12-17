/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft;

import com.mojang.logging.LogUtils;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Language;
import net.minecraft.util.annotation.SuppressLinter;
import net.minecraft.util.logging.DebugLoggerPrintStream;
import net.minecraft.util.logging.LoggerPrintStream;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleVisitor;
import net.minecraft.world.rule.GameRules;
import org.slf4j.Logger;

@SuppressLinter(reason="System.out setup")
public class Bootstrap {
    public static final PrintStream SYSOUT = System.out;
    private static volatile boolean initialized;
    private static final Logger LOGGER;
    public static final AtomicLong LOAD_TIME;

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        Instant instant = Instant.now();
        if (Registries.REGISTRIES.getIds().isEmpty()) {
            throw new IllegalStateException("Unable to load registries");
        }
        FireBlock.registerDefaultFlammables();
        ComposterBlock.registerDefaultCompostableItems();
        if (EntityType.getId(EntityType.PLAYER) == null) {
            throw new IllegalStateException("Failed loading EntityTypes");
        }
        EntitySelectorOptions.register();
        DispenserBehavior.registerDefaults();
        CauldronBehavior.registerBehavior();
        Registries.bootstrap();
        ItemGroups.collect();
        Bootstrap.setOutputStreams();
        LOAD_TIME.set(Duration.between(instant, Instant.now()).toMillis());
    }

    private static <T> void collectMissingTranslations(Iterable<T> registry, Function<T, String> keyExtractor, Set<String> translationKeys) {
        Language language = Language.getInstance();
        registry.forEach(object -> {
            String string = (String)keyExtractor.apply(object);
            if (!language.hasTranslation(string)) {
                translationKeys.add(string);
            }
        });
    }

    private static void collectMissingGameRuleTranslations(final Set<String> translations) {
        final Language language = Language.getInstance();
        GameRules gameRules = new GameRules(FeatureFlags.FEATURE_MANAGER.getFeatureSet());
        gameRules.accept(new GameRuleVisitor(){

            @Override
            public <T> void visit(GameRule<T> rule) {
                if (!language.hasTranslation(rule.getTranslationKey())) {
                    translations.add(rule.toShortString());
                }
            }
        });
    }

    public static Set<String> getMissingTranslations() {
        TreeSet<String> set = new TreeSet<String>();
        Bootstrap.collectMissingTranslations(Registries.ATTRIBUTE, EntityAttribute::getTranslationKey, set);
        Bootstrap.collectMissingTranslations(Registries.ENTITY_TYPE, EntityType::getTranslationKey, set);
        Bootstrap.collectMissingTranslations(Registries.STATUS_EFFECT, StatusEffect::getTranslationKey, set);
        Bootstrap.collectMissingTranslations(Registries.ITEM, Item::getTranslationKey, set);
        Bootstrap.collectMissingTranslations(Registries.BLOCK, AbstractBlock::getTranslationKey, set);
        Bootstrap.collectMissingTranslations(Registries.CUSTOM_STAT, stat -> "stat." + stat.toString().replace(':', '.'), set);
        Bootstrap.collectMissingGameRuleTranslations(set);
        return set;
    }

    public static void ensureBootstrapped(Supplier<String> callerGetter) {
        if (!initialized) {
            throw Bootstrap.createNotBootstrappedException(callerGetter);
        }
    }

    private static RuntimeException createNotBootstrappedException(Supplier<String> callerGetter) {
        try {
            String string = callerGetter.get();
            return new IllegalArgumentException("Not bootstrapped (called from " + string + ")");
        }
        catch (Exception exception) {
            IllegalArgumentException runtimeException = new IllegalArgumentException("Not bootstrapped (failed to resolve location)");
            runtimeException.addSuppressed(exception);
            return runtimeException;
        }
    }

    public static void logMissing() {
        Bootstrap.ensureBootstrapped(() -> "validate");
        if (SharedConstants.isDevelopment) {
            Bootstrap.getMissingTranslations().forEach(key -> LOGGER.error("Missing translations: {}", key));
            CommandManager.checkMissing();
        }
        DefaultAttributeRegistry.checkMissing();
    }

    private static void setOutputStreams() {
        if (LOGGER.isDebugEnabled()) {
            System.setErr(new DebugLoggerPrintStream("STDERR", System.err));
            System.setOut(new DebugLoggerPrintStream("STDOUT", SYSOUT));
        } else {
            System.setErr(new LoggerPrintStream("STDERR", System.err));
            System.setOut(new LoggerPrintStream("STDOUT", SYSOUT));
        }
    }

    public static void println(String str) {
        SYSOUT.println(str);
    }

    static {
        LOGGER = LogUtils.getLogger();
        LOAD_TIME = new AtomicLong(-1L);
    }
}

