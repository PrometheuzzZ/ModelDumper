package ru.promej.modeldumper.mixin;


import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.promej.modeldumper.event.KeyInputHandler;
import ru.promej.modeldumper.exporter.MultiBufferObjConsumer;

import java.io.File;

import static ru.promej.modeldumper.client.ModelDumperClient.*;
import static ru.promej.modeldumper.utils.StringUtils.getCurrentDateTime;
import static ru.promej.modeldumper.utils.StringUtils.sendTranslatableMessageToPlayer;


@Mixin(value = PlayerEntityRenderer.class)
public abstract class PlayerRenderMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityRenderState, PlayerEntityModel> {



    public PlayerRenderMixin(EntityRendererFactory.Context ctx, PlayerEntityModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("RETURN"))
    public void render(AbstractClientPlayerEntity abstractClientPlayerEntity, PlayerEntityRenderState playerEntityRenderState, float f, CallbackInfo ci) {

        if(KeyInputHandler.dumpPlayerPressed && !getDumpedEntities().contains(abstractClientPlayerEntity)) {
            addDumpedEntities(abstractClientPlayerEntity);
            File outFile = new File(getDumpFolder(), abstractClientPlayerEntity.getName().getString() + "_" + getCurrentDateTime());
            outFile.mkdirs();
            MultiBufferObjConsumer consumer = new MultiBufferObjConsumer(outFile);
            render(playerEntityRenderState, new MatrixStack(), consumer, 0);
            consumer.close();
            sendTranslatableMessageToPlayer(Text.translatable("text.modeldumper.player.start", abstractClientPlayerEntity.getName().getString()));
        }
    }

}
