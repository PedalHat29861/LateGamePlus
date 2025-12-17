/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 *  org.apache.commons.io.FilenameUtils
 */
package net.minecraft.util.path;

import com.mojang.serialization.DataResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;
import org.apache.commons.io.FilenameUtils;

public class PathUtil {
    private static final Pattern FILE_NAME_WITH_COUNT = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
    private static final int MAX_NAME_LENGTH = 255;
    private static final Pattern RESERVED_WINDOWS_NAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);
    private static final Pattern VALID_FILE_NAME = Pattern.compile("[-._a-z0-9]+");

    public static String replaceInvalidChars(String fileName) {
        for (char c : SharedConstants.INVALID_CHARS_LEVEL_NAME) {
            fileName = fileName.replace(c, '_');
        }
        return fileName.replaceAll("[./\"]", "_");
    }

    public static String getNextUniqueName(Path path, String name, String extension) throws IOException {
        if (!PathUtil.isNotReservedWindowsName((String)(name = PathUtil.replaceInvalidChars((String)name)))) {
            name = "_" + (String)name + "_";
        }
        Matcher matcher = FILE_NAME_WITH_COUNT.matcher((CharSequence)name);
        int i = 0;
        if (matcher.matches()) {
            name = matcher.group("name");
            i = Integer.parseInt(matcher.group("count"));
        }
        if (((String)name).length() > 255 - extension.length()) {
            name = ((String)name).substring(0, 255 - extension.length());
        }
        while (true) {
            Object string = name;
            if (i != 0) {
                String string2 = " (" + i + ")";
                int j = 255 - string2.length();
                if (((String)string).length() > j) {
                    string = ((String)string).substring(0, j);
                }
                string = (String)string + string2;
            }
            string = (String)string + extension;
            Path path2 = path.resolve((String)string);
            try {
                Path path3 = Files.createDirectory(path2, new FileAttribute[0]);
                Files.deleteIfExists(path3);
                return path.relativize(path3).toString();
            }
            catch (FileAlreadyExistsException fileAlreadyExistsException) {
                ++i;
                continue;
            }
            break;
        }
    }

    public static boolean isNormal(Path path) {
        Path path2 = path.normalize();
        return path2.equals(path);
    }

    public static boolean isAllowedName(Path path) {
        for (Path path2 : path) {
            if (PathUtil.isNotReservedWindowsName(path2.toString())) continue;
            return false;
        }
        return true;
    }

    public static boolean isNotReservedWindowsName(String path) {
        return !RESERVED_WINDOWS_NAMES.matcher(path).matches();
    }

    public static Path getResourcePath(Path path, String resourceName, String extension) {
        String string = resourceName + extension;
        Path path2 = Paths.get(string, new String[0]);
        if (path2.endsWith(extension)) {
            throw new InvalidPathException(string, "empty resource name");
        }
        return path.resolve(path2);
    }

    public static String getPosixFullPath(String path) {
        return FilenameUtils.getFullPath((String)path).replace(File.separator, "/");
    }

    public static String normalizeToPosix(String path) {
        return FilenameUtils.normalize((String)path).replace(File.separator, "/");
    }

    public static DataResult<List<String>> split(String path) {
        int i = path.indexOf(47);
        if (i == -1) {
            return switch (path) {
                case "", ".", ".." -> DataResult.error(() -> "Invalid path '" + path + "'");
                default -> !PathUtil.isFileNameValid(path) ? DataResult.error(() -> "Invalid path '" + path + "'") : DataResult.success(List.of(path));
            };
        }
        ArrayList<String> list = new ArrayList<String>();
        int j = 0;
        boolean bl = false;
        while (true) {
            String string;
            switch (string = path.substring(j, i)) {
                case "": 
                case ".": 
                case "..": {
                    return DataResult.error(() -> "Invalid segment '" + string + "' in path '" + path + "'");
                }
            }
            if (!PathUtil.isFileNameValid(string)) {
                return DataResult.error(() -> "Invalid segment '" + string + "' in path '" + path + "'");
            }
            list.add(string);
            if (bl) {
                return DataResult.success(list);
            }
            j = i + 1;
            if ((i = path.indexOf(47, j)) != -1) continue;
            i = path.length();
            bl = true;
        }
    }

    public static Path getPath(Path root, List<String> paths) {
        int i = paths.size();
        return switch (i) {
            case 0 -> root;
            case 1 -> root.resolve(paths.get(0));
            default -> {
                String[] strings = new String[i - 1];
                for (int j = 1; j < i; ++j) {
                    strings[j - 1] = paths.get(j);
                }
                yield root.resolve(root.getFileSystem().getPath(paths.get(0), strings));
            }
        };
    }

    private static boolean isFileNameValid(String name) {
        return VALID_FILE_NAME.matcher(name).matches();
    }

    public static boolean isPathSegmentValid(String name) {
        return !name.equals("..") && !name.equals(".") && PathUtil.isFileNameValid(name);
    }

    public static void validatePath(String ... paths) {
        if (paths.length == 0) {
            throw new IllegalArgumentException("Path must have at least one element");
        }
        for (String string : paths) {
            if (PathUtil.isPathSegmentValid(string)) continue;
            throw new IllegalArgumentException("Illegal segment " + string + " in path " + Arrays.toString(paths));
        }
    }

    public static void createDirectories(Path path) throws IOException {
        Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath(new LinkOption[0]) : path, new FileAttribute[0]);
    }
}

