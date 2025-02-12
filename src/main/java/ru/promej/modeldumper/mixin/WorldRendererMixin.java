package ru.promej.modeldumper.mixin;


import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.promej.modeldumper.client.ModelDumperClient;
import ru.promej.modeldumper.event.KeyInputHandler;

import static ru.promej.modeldumper.client.ModelDumperClient.*;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {


    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {

        VertexConsumerProvider source = ModelDumperClient.getModelConsumer();

        if (source != null && !getDumpedEntities().contains(entity)) {

            if (KeyInputHandler.dumpDisplayEntitiesPressed) {

                if (entity.getType().getName().toString().contains("item_display")) {
                    addDumpedEntities(entity);
                    this.entityRenderDispatcher.render(entity, entity.getX(), entity.getY(), entity.getZ(), tickDelta, matrices, source, this.entityRenderDispatcher.getLight(entity, tickDelta));
                } else if (entity.getType().getName().toString().contains("block_display")) {
                    addDumpedEntities(entity);
                    this.entityRenderDispatcher.render(entity, entity.getX(), entity.getY(), entity.getZ(), tickDelta, matrices, source, this.entityRenderDispatcher.getLight(entity, tickDelta));
                }
            } else if (KeyInputHandler.dumpAllEntitiesPressed) {
                addDumpedEntities(entity);
                this.entityRenderDispatcher.render(entity, entity.getX(), entity.getY(), entity.getZ(), tickDelta, matrices, source, this.entityRenderDispatcher.getLight(entity, tickDelta));
            }

        }
    }


}
