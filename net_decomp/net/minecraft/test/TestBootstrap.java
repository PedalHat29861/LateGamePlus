/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  org.apache.commons.io.FileUtils
 *  org.slf4j.Logger
 */
package net.minecraft.test;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.Bootstrap;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.test.TestFailureLogger;
import net.minecraft.test.TestServer;
import net.minecraft.test.XmlReportingTestCompletionListener;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.SuppressLinter;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class TestBootstrap {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFAULT_UNIVERSE = "gametestserver";
    private static final String DEFAULT_WORLD = "gametestworld";
    private static final OptionParser PARSER = new OptionParser();
    private static final OptionSpec<String> UNIVERSE = PARSER.accepts("universe", "The path to where the test server world will be created. Any existing folder will be replaced.").withRequiredArg().defaultsTo((Object)"gametestserver", (Object[])new String[0]);
    private static final OptionSpec<File> REPORT = PARSER.accepts("report", "Exports results in a junit-like XML report at the given path.").withRequiredArg().ofType(File.class);
    private static final OptionSpec<String> TESTS = PARSER.accepts("tests", "Which test(s) to run (namespaced ID selector using wildcards). Empty means run all.").withRequiredArg();
    private static final OptionSpec<Boolean> VERIFY = PARSER.accepts("verify", "Runs the tests specified with `test` or `testNamespace` 100 times for each 90 degree rotation step").withRequiredArg().ofType(Boolean.class).defaultsTo((Object)false, (Object[])new Boolean[0]);
    private static final OptionSpec<String> PACKS = PARSER.accepts("packs", "A folder of datapacks to include in the world").withRequiredArg();
    private static final OptionSpec<Void> HELP = PARSER.accepts("help").forHelp();

    @SuppressLinter(reason="Using System.err due to no bootstrap")
    public static void run(String[] args, Consumer<String> universeCallback) throws Exception {
        PARSER.allowsUnrecognizedOptions();
        OptionSet optionSet = PARSER.parse(args);
        if (optionSet.has(HELP)) {
            PARSER.printHelpOn((OutputStream)System.err);
            return;
        }
        if (((Boolean)optionSet.valueOf(VERIFY)).booleanValue() && !optionSet.has(TESTS)) {
            LOGGER.error("Please specify a test selection to run the verify option. For example: --verify --tests example:test_something_*");
            System.exit(-1);
        }
        LOGGER.info("Running GameTestMain with cwd '{}', universe path '{}'", (Object)System.getProperty("user.dir"), optionSet.valueOf(UNIVERSE));
        if (optionSet.has(REPORT)) {
            TestFailureLogger.setCompletionListener(new XmlReportingTestCompletionListener((File)REPORT.value(optionSet)));
        }
        Bootstrap.initialize();
        Util.startTimerHack();
        String string = (String)optionSet.valueOf(UNIVERSE);
        TestBootstrap.empty(string);
        universeCallback.accept(string);
        if (optionSet.has(PACKS)) {
            String string2 = (String)optionSet.valueOf(PACKS);
            TestBootstrap.copyPacks(string, string2);
        }
        LevelStorage.Session session = LevelStorage.create(Paths.get(string, new String[0])).createSessionWithoutSymlinkCheck(DEFAULT_WORLD);
        ResourcePackManager resourcePackManager = VanillaDataPackProvider.createManager(session);
        MinecraftServer.startServer(thread -> TestServer.create(thread, session, resourcePackManager, TestBootstrap.get(optionSet, TESTS), optionSet.has(VERIFY)));
    }

    private static Optional<String> get(OptionSet options, OptionSpec<String> option) {
        return options.has(option) ? Optional.of((String)options.valueOf(option)) : Optional.empty();
    }

    private static void empty(String path) throws IOException {
        Path path2 = Paths.get(path, new String[0]);
        if (Files.exists(path2, new LinkOption[0])) {
            FileUtils.deleteDirectory((File)path2.toFile());
        }
        Files.createDirectories(path2, new FileAttribute[0]);
    }

    private static void copyPacks(String universe, String packDir) throws IOException {
        Path path2;
        Path path = Paths.get(universe, new String[0]).resolve(DEFAULT_WORLD).resolve("datapacks");
        if (!Files.exists(path, new LinkOption[0])) {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        if (Files.exists(path2 = Paths.get(packDir, new String[0]), new LinkOption[0])) {
            try (Stream<Path> stream = Files.list(path2);){
                for (Path path3 : stream.toList()) {
                    Path path4 = path.resolve(path3.getFileName());
                    if (Files.isDirectory(path3, new LinkOption[0])) {
                        if (!Files.isRegularFile(path3.resolve("pack.mcmeta"), new LinkOption[0])) continue;
                        FileUtils.copyDirectory((File)path3.toFile(), (File)path4.toFile());
                        LOGGER.info("Included folder pack {}", (Object)path3.getFileName());
                        continue;
                    }
                    if (!path3.toString().endsWith(".zip")) continue;
                    Files.copy(path3, path4, new CopyOption[0]);
                    LOGGER.info("Included zip pack {}", (Object)path3.getFileName());
                }
            }
        }
    }
}

