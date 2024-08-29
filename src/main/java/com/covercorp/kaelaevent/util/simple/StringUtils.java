package com.covercorp.kaelaevent.util.simple;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private final static int CENTER_PX = 154;

    public static void sendCenteredMessage(final Player player, String message) {
        if(message == null || message.equals("")) player.sendMessage("");
        message = translate(message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if (c == '§'){
                previousCode = true;
            } else if (previousCode){
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(sb + message);
    }

    public static void sendGlobalCenteredMessage(String message) {
        if(message == null || message.equals("")) return;
        message = translate(message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if (c == '§'){
                previousCode = true;
            } else if (previousCode){
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        final String s = sb + message;
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(s));
    }

    public static String translate(String string) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String color = string.substring(matcher.start(), matcher.end());
            string = string.replace(color, ChatColor.of(color) + "");
            matcher = pattern.matcher(string);
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String toMinusCaps(String text) {
        text = translate(text.toLowerCase());

        char[] subindices = {'₀', '₁', '₂', '₃', '₄', '₅', '₆', '₇', '₈', '₉'};

        StringBuilder resultado = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isDigit(c)) {
                resultado.append(subindices[c - '0']);
            } else {
                resultado.append(c);
            }
        }

        return resultado.toString();
    }
}