package ru.promej.modeldumper.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StringUtils {


    public static void sendTranslatableMessageToPlayer(MutableText message) {
        MinecraftClient.getInstance().player.sendMessage(message, false);
    }


    public static String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy-HH-mm-ss");
        return now.format(formatter);
    }


}
