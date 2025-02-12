package ru.promej.modeldumper.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.promej.modeldumper.exporter.MultiBufferObjConsumer;

import java.io.File;

import static ru.promej.modeldumper.client.ModelDumperClient.*;
import static ru.promej.modeldumper.utils.StringUtils.*;


public class KeyInputHandler {

    public static final String KEY_CATEGORY_MODEL_DUMPER = "key.modeldumper.category";

    public static final String KEY_DUMP_DISPLAY_ENTITIES = "key.modeldumper.dumpentities";
    public static final String KEY_DUMP_ALL_ENTITIES = "key.modeldumper.dumpallentities";
    public static final String KEY_DUMP_PLAYER = "key.modeldumper.dumpplayer";


    public static KeyBinding dumpDisplayEntities;
    public static KeyBinding dumpAllEntities;
    public static KeyBinding dumpPlayer;

    public static boolean dumpDisplayEntitiesPressed = false;
    public static boolean dumpAllEntitiesPressed = false;
    public static boolean dumpPlayerPressed = false;

    private static int tickDelay = 0;


    private static boolean anyKeyPressed() {
        return dumpDisplayEntitiesPressed || dumpAllEntitiesPressed || dumpPlayerPressed;
    }


    public static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {


            if (!anyKeyPressed()) {

                delModelConsumer();

                tickDelay = 40;

                if (dumpDisplayEntities.wasPressed()) {
                    dumpDisplayEntityStart();
                }

                if (dumpAllEntities.wasPressed()) {
                    dumpAllEntitiesStart();
                }

                if (dumpPlayer.wasPressed()) {
                    dumpPlayerStart();
                }

            }

            if (dumpDisplayEntitiesPressed) {
                dumpDisplayEntityEnd();
            }

            if (dumpAllEntitiesPressed) {
                dumpAllEntitiesEnd();
            }

            if (dumpPlayerPressed) {
                dumpPlayerEnd();
            }



        });
    }

    private static void dumpPlayerStart() {
        dumpPlayerPressed = true;
    }

    private static void dumpPlayerEnd() {

        if (tickDelay > 0) {
            tickDelay--;
        } else if (tickDelay == 0) {
            if (dumpPlayerPressed) {
                sendTranslatableMessageToPlayer(Text.translatable("text.modeldumper.player.end", getDumpedEntities().size()));
                clearDumpedEntities();
                dumpPlayerPressed = false;
            }
        }
    }

    private static void dumpAllEntitiesStart() {
        dumpAllEntitiesPressed = true;
        File outFile = new File(getDumpFolder(), "AllEntityDump_" + getCurrentDateTime());
        outFile.mkdirs();
        setModelConsumer(new MultiBufferObjConsumer(outFile));

        MutableText translatable = Text.translatable("text.modeldumper.allentities.start");
        sendTranslatableMessageToPlayer(translatable);

    }

    private static void dumpAllEntitiesEnd() {
        if (tickDelay > 0) {
            tickDelay--;
        } else if (tickDelay == 0) {
            if (dumpAllEntitiesPressed) {
                closeModelConsumer();
                delModelConsumer();
                MutableText translatable = Text.translatable("text.modeldumper.allentities.end",getDumpedEntities().size());
                sendTranslatableMessageToPlayer(translatable);
                clearDumpedEntities();
                dumpAllEntitiesPressed = false;
            }
        }
    }


    private static void dumpDisplayEntityStart() {
        dumpDisplayEntitiesPressed = true;
        File outFile = new File(getDumpFolder(), "DisplayEntityDump_" + getCurrentDateTime());
        outFile.mkdirs();
        setModelConsumer(new MultiBufferObjConsumer(outFile));
        MutableText translatable = Text.translatable("text.modeldumper.displayentities.start");
        sendTranslatableMessageToPlayer(translatable);
    }

    private static void dumpDisplayEntityEnd() {

        if (tickDelay > 0) {
            tickDelay--;
        } else if (tickDelay == 0) {

            if (dumpDisplayEntitiesPressed) {
                closeModelConsumer();
                delModelConsumer();
                MutableText translatable = Text.translatable("text.modeldumper.displayentities.end", dumpedEntities.size());
                sendTranslatableMessageToPlayer(translatable);
                dumpDisplayEntitiesPressed = false;
            }

        }


    }



    public static void register() {

        dumpDisplayEntities = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_DUMP_DISPLAY_ENTITIES,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                KEY_CATEGORY_MODEL_DUMPER
        ));

        dumpAllEntities = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_DUMP_ALL_ENTITIES,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                KEY_CATEGORY_MODEL_DUMPER
        ));

        dumpPlayer = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_DUMP_PLAYER,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                KEY_CATEGORY_MODEL_DUMPER
        ));

        registerKeyInputs();


    }
}
