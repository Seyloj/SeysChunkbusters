package com.seyloj.seysChunkbuster.util;

import org.bukkit.Bukkit;

public class VersionUtil {

    private static final int major;
    private static final int minor;

    static {
        String version = Bukkit.getBukkitVersion();
        String[] split = version.split("-")[0].split("\\.");

        major = Integer.parseInt(split[0]);
        minor = Integer.parseInt(split[1]);
    }

    public static boolean isAtLeast(int minMinorVersion) {
        return major > 1 || (major == 1 && minor >= minMinorVersion);
    }

    public static int getMinorVersion() {
        return minor;
    }
}
