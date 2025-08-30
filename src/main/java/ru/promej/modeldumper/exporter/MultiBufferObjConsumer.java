package ru.promej.modeldumper.exporter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.opengl.GL15;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;


public class MultiBufferObjConsumer implements VertexConsumerProvider {

    private RenderLayer lastType = null;
    private ObjConsumer lastConsumer = null;
    private final File outputFolder;
    private int id = 0;
    private Set<Integer> dumpedTextureIds = new HashSet<>();

    public MultiBufferObjConsumer(File outputFolder) {
        this.outputFolder = outputFolder;
    }



    private void saveCurrent() {
        if(lastConsumer != null) {
            try {
                File objFile = new File(outputFolder, "part_" + id + ".obj");
                List<String> textures = new ArrayList<>();
                lastType.startDrawing();
                int shaderId = 0;
                while(true) {
                    int textureId = RenderSystem.getShaderTexture(shaderId);
                    shaderId++;
                    if(textureId == 0) {
                        break;
                    }
                    String texName = "texture_" + textureId + ".png";
                    textures.add(texName);
                    if(!dumpedTextureIds.contains(textureId)) {
                        dumpedTextureIds.add(textureId);
                        GL15.glBindTexture(GL15.GL_TEXTURE_2D, textureId);
                        float width = GL15.glGetTexLevelParameterf(GL15.GL_TEXTURE_2D, 0, GL15.GL_TEXTURE_WIDTH);
                        float height = GL15.glGetTexLevelParameterf(GL15.GL_TEXTURE_2D, 0, GL15.GL_TEXTURE_HEIGHT);
                        if(width > 0 && height > 0) {
                            try(NativeImage img = new NativeImage(NativeImage.Format.RGBA, (int)width, (int)height, false)){
                                img.loadFromTextureImage(0, false);
                                img.writeTo(new File(outputFolder, texName));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                lastConsumer.writeData(objFile, textures);
                id++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        saveCurrent();
        lastConsumer = null;
        lastType = null;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        if(layer != lastType) {
            saveCurrent();
            lastType = layer;
            lastConsumer = new ObjConsumer();
        }
        return lastConsumer;
    }
}