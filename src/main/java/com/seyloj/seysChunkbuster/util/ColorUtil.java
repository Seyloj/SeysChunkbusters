package com.seyloj.seysChunkbuster.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String color(String input) {
        if (input == null) return null;

        String colored = ChatColor.translateAlternateColorCodes('&', input);

        if (VersionUtil.isAtLeast(16)) {
            Matcher matcher = HEX_PATTERN.matcher(colored);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                String hexCode = matcher.group(1);
                StringBuilder replacement = new StringBuilder("§x");
                for (char c : hexCode.toCharArray()) {
                    replacement.append('§').append(c);
                }
                matcher.appendReplacement(buffer, replacement.toString());
            }
            matcher.appendTail(buffer);
            return buffer.toString();
        }

        return colored;
    }
}
