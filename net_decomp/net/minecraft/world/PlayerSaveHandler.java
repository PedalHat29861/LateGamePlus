/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.time.ZonedDateTime;
import java.util.Optional;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.DateTimeFormatters;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.slf4j.Logger;

public class PlayerSaveHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final File playerDataDir;
    protected final DataFixer dataFixer;

    public PlayerSaveHandler(LevelStorage.Session session, DataFixer dataFixer) {
        this.dataFixer = dataFixer;
        this.playerDataDir = session.getDirectory(WorldSavePath.PLAYERDATA).toFile();
        this.playerDataDir.mkdirs();
    }

    public void savePlayerData(PlayerEntity player) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(player.getErrorReporterContext(), LOGGER);){
            NbtWriteView nbtWriteView = NbtWriteView.create(logging, player.getRegistryManager());
            player.writeData(nbtWriteView);
            Path path = this.playerDataDir.toPath();
            Path path2 = Files.createTempFile(path, player.getUuidAsString() + "-", ".dat", new FileAttribute[0]);
            NbtCompound nbtCompound = nbtWriteView.getNbt();
            NbtIo.writeCompressed(nbtCompound, path2);
            Path path3 = path.resolve(player.getUuidAsString() + ".dat");
            Path path4 = path.resolve(player.getUuidAsString() + ".dat_old");
            Util.backupAndReplace(path3, path2, path4);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to save player data for {}", (Object)player.getStringifiedName());
        }
    }

    private void backupCorruptedPlayerData(PlayerConfigEntry playerConfigEntry, String extension) {
        Path path = this.playerDataDir.toPath();
        String string = playerConfigEntry.id().toString();
        Path path2 = path.resolve(string + extension);
        Path path3 = path.resolve(string + "_corrupted_" + ZonedDateTime.now().format(DateTimeFormatters.MINUTES) + extension);
        if (!Files.isRegularFile(path2, new LinkOption[0])) {
            return;
        }
        try {
            Files.copy(path2, path3, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to copy the player.dat file for {}", (Object)playerConfigEntry.name(), (Object)exception);
        }
    }

    private Optional<NbtCompound> loadPlayerData(PlayerConfigEntry playerConfigEntry, String extension) {
        File file = new File(this.playerDataDir, String.valueOf(playerConfigEntry.id()) + extension);
        if (file.exists() && file.isFile()) {
            try {
                return Optional.of(NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes()));
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to load player data for {}", (Object)playerConfigEntry.name());
            }
        }
        return Optional.empty();
    }

    public Optional<NbtCompound> loadPlayerData(PlayerConfigEntry playerConfigEntry) {
        Optional<NbtCompound> optional = this.loadPlayerData(playerConfigEntry, ".dat");
        if (optional.isEmpty()) {
            this.backupCorruptedPlayerData(playerConfigEntry, ".dat");
        }
        return optional.or(() -> this.loadPlayerData(playerConfigEntry, ".dat_old")).map(nbtCompound -> {
            int i = NbtHelper.getDataVersion(nbtCompound);
            nbtCompound = DataFixTypes.PLAYER.update(this.dataFixer, (NbtCompound)nbtCompound, i);
            return nbtCompound;
        });
    }
}

