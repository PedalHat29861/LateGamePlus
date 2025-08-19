package com.pedalhat.lategameplus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Path.of("config");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("lategameplus.json");

    private static ModConfig INSTANCE = new ModConfig();

    public static synchronized ModConfig get() {
        return INSTANCE;
    }

    public static synchronized void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            if (Files.exists(CONFIG_FILE)) {
                try (Reader r = Files.newBufferedReader(CONFIG_FILE)) {
                    ModConfig loaded = GSON.fromJson(r, ModConfig.class);
                    if (loaded != null) INSTANCE = loaded;
                }
            } else {
                save(); // crea archivo con defaults
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void save() {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            try (Writer w = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
