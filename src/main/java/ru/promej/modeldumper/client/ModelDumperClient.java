package ru.promej.modeldumper.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.entity.Entity;
import ru.promej.modeldumper.event.KeyInputHandler;
import ru.promej.modeldumper.exporter.MultiBufferObjConsumer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ModelDumperClient implements ClientModInitializer {

    private static MultiBufferObjConsumer modelConsumer;
    private static final File dumpFolder = new File("ModelDumps");
    public static Set<Entity> dumpedEntities = new HashSet<>();

    public static File getDumpFolder(){
        return dumpFolder;
    }

    @Override
    public void onInitializeClient() {
        KeyInputHandler.register();
    }


    public static void clearDumpedEntities() {
        dumpedEntities.clear();
    }

    public static void delModelConsumer() {
        modelConsumer = null;
    }

    public static void setModelConsumer(MultiBufferObjConsumer newModelConsumer) {
        modelConsumer = newModelConsumer;
    }


    public static void closeModelConsumer(){
        if(modelConsumer!=null){
            modelConsumer.close();
        }
    }

    public static MultiBufferObjConsumer getModelConsumer() {
        return modelConsumer;
    }

    public static Set<Entity> getDumpedEntities() {
        return dumpedEntities;
    }

    public static void addDumpedEntities(Entity entity) {
        dumpedEntities.add(entity);
    }


}

