package ru.promej.modeldumper.exporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Utility to build a single GLB model combining all exported OBJ parts.
 */
public class GlbExporter {

    public static void export(List<MultiBufferObjConsumer.Part> parts, File folder) {
        if(parts == null || parts.isEmpty()) {
            return;
        }
        try {
            ByteArrayOutputStream bin = new ByteArrayOutputStream();
            JsonArray bufferViews = new JsonArray();
            JsonArray accessors = new JsonArray();
            JsonArray images = new JsonArray();
            JsonArray textures = new JsonArray();
            JsonArray materials = new JsonArray();
            JsonArray meshes = new JsonArray();
            JsonArray nodes = new JsonArray();
            JsonArray sceneNodes = new JsonArray();
            JsonArray samplers = new JsonArray();

            // one sampler with NEAREST filtering for pixel art
            JsonObject sampler = new JsonObject();
            sampler.addProperty("magFilter", 9728); // GL_NEAREST
            sampler.addProperty("minFilter", 9728); // GL_NEAREST
            // avoid texture bleeding by clamping
            sampler.addProperty("wrapS", 33071); // GL_CLAMP_TO_EDGE
            sampler.addProperty("wrapT", 33071); // GL_CLAMP_TO_EDGE
            samplers.add(sampler);
            int samplerIndex = 0;

            for(MultiBufferObjConsumer.Part part : parts) {
                List<ObjConsumer.VertexData> verts = part.vertices();
                if(verts.isEmpty()) {
                    continue;
                }
                int vertexCount = verts.size();

                ByteBuffer posBuf = ByteBuffer.allocate(vertexCount * 3 * 4).order(ByteOrder.LITTLE_ENDIAN);
                ByteBuffer uvBuf = ByteBuffer.allocate(vertexCount * 2 * 4).order(ByteOrder.LITTLE_ENDIAN);
                for(ObjConsumer.VertexData v : verts) {
                    posBuf.putFloat((float)v.x());
                    posBuf.putFloat((float)v.y());
                    posBuf.putFloat((float)v.z());
                    uvBuf.putFloat(v.u());
                    uvBuf.putFloat(1f - v.v());
                }
                byte[] posBytes = posBuf.array();
                byte[] uvBytes = uvBuf.array();

                int quadCount = vertexCount / 4;
                ByteBuffer idxBuf = ByteBuffer.allocate(quadCount * 6 * 4).order(ByteOrder.LITTLE_ENDIAN);
                for(int i = 0; i < quadCount; i++) {
                    int base = i * 4;
                    idxBuf.putInt(base);
                    idxBuf.putInt(base + 1);
                    idxBuf.putInt(base + 2);
                    idxBuf.putInt(base);
                    idxBuf.putInt(base + 2);
                    idxBuf.putInt(base + 3);
                }
                byte[] idxBytes = idxBuf.array();

                int posOffset = append(bin, posBytes);
                JsonObject posBv = new JsonObject();
                posBv.addProperty("buffer", 0);
                posBv.addProperty("byteOffset", posOffset);
                posBv.addProperty("byteLength", posBytes.length);
                posBv.addProperty("target", 34962);
                int posBvIndex = bufferViews.size();
                bufferViews.add(posBv);

                int uvOffset = append(bin, uvBytes);
                JsonObject uvBv = new JsonObject();
                uvBv.addProperty("buffer", 0);
                uvBv.addProperty("byteOffset", uvOffset);
                uvBv.addProperty("byteLength", uvBytes.length);
                uvBv.addProperty("target", 34962);
                int uvBvIndex = bufferViews.size();
                bufferViews.add(uvBv);

                int idxOffset = append(bin, idxBytes);
                JsonObject idxBv = new JsonObject();
                idxBv.addProperty("buffer", 0);
                idxBv.addProperty("byteOffset", idxOffset);
                idxBv.addProperty("byteLength", idxBytes.length);
                idxBv.addProperty("target", 34963);
                int idxBvIndex = bufferViews.size();
                bufferViews.add(idxBv);

                JsonObject posAcc = new JsonObject();
                posAcc.addProperty("bufferView", posBvIndex);
                posAcc.addProperty("componentType", 5126);
                posAcc.addProperty("count", vertexCount);
                posAcc.addProperty("type", "VEC3");
                accessors.add(posAcc);
                int posAccIdx = accessors.size() - 1;

                JsonObject uvAcc = new JsonObject();
                uvAcc.addProperty("bufferView", uvBvIndex);
                uvAcc.addProperty("componentType", 5126);
                uvAcc.addProperty("count", vertexCount);
                uvAcc.addProperty("type", "VEC2");
                accessors.add(uvAcc);
                int uvAccIdx = accessors.size() - 1;

                JsonObject idxAcc = new JsonObject();
                idxAcc.addProperty("bufferView", idxBvIndex);
                idxAcc.addProperty("componentType", 5125);
                idxAcc.addProperty("count", quadCount * 6);
                idxAcc.addProperty("type", "SCALAR");
                accessors.add(idxAcc);
                int idxAccIdx = accessors.size() - 1;

                Integer materialIndex = null;
                if(part.texture() != null) {
                    File texFile = new File(folder, part.texture());
                    if(texFile.exists()) {
                        byte[] imgBytes = Files.readAllBytes(texFile.toPath());
                        int imgOffset = append(bin, imgBytes);
                        JsonObject imgBv = new JsonObject();
                        imgBv.addProperty("buffer", 0);
                        imgBv.addProperty("byteOffset", imgOffset);
                        imgBv.addProperty("byteLength", imgBytes.length);
                        int imgBvIndex = bufferViews.size();
                        bufferViews.add(imgBv);

                        JsonObject image = new JsonObject();
                        image.addProperty("bufferView", imgBvIndex);
                        image.addProperty("mimeType", "image/png");
                        int imageIndex = images.size();
                        images.add(image);

                        JsonObject texture = new JsonObject();
                        texture.addProperty("sampler", samplerIndex);
                        texture.addProperty("source", imageIndex);
                        int textureIndex = textures.size();
                        textures.add(texture);

                        JsonObject pbr = new JsonObject();
                        JsonObject baseColor = new JsonObject();
                        baseColor.addProperty("index", textureIndex);
                        pbr.add("baseColorTexture", baseColor);
                        JsonObject material = new JsonObject();
                        material.add("pbrMetallicRoughness", pbr);
                        material.addProperty("alphaMode", "BLEND");
                        materialIndex = materials.size();
                        materials.add(material);
                    }
                }

                JsonObject attrs = new JsonObject();
                attrs.addProperty("POSITION", posAccIdx);
                attrs.addProperty("TEXCOORD_0", uvAccIdx);
                JsonObject prim = new JsonObject();
                prim.add("attributes", attrs);
                prim.addProperty("indices", idxAccIdx);
                if(materialIndex != null) {
                    prim.addProperty("material", materialIndex);
                }
                JsonArray prims = new JsonArray();
                prims.add(prim);
                JsonObject mesh = new JsonObject();
                mesh.add("primitives", prims);
                int meshIndex = meshes.size();
                meshes.add(mesh);

                JsonObject node = new JsonObject();
                node.addProperty("mesh", meshIndex);
                int nodeIndex = nodes.size();
                nodes.add(node);
                sceneNodes.add(nodeIndex);
            }

            if(nodes.size() == 0) {
                return;
            }

            JsonObject asset = new JsonObject();
            asset.addProperty("version", "2.0");

            JsonArray buffers = new JsonArray();
            JsonObject buffer = new JsonObject();
            buffer.addProperty("byteLength", bin.size());
            buffers.add(buffer);

            JsonObject scene = new JsonObject();
            scene.add("nodes", sceneNodes);
            JsonArray scenes = new JsonArray();
            scenes.add(scene);

            JsonObject gltf = new JsonObject();
            gltf.add("asset", asset);
            gltf.add("scenes", scenes);
            gltf.addProperty("scene", 0);
            gltf.add("nodes", nodes);
            gltf.add("meshes", meshes);
            if(materials.size() > 0) gltf.add("materials", materials);
            if(textures.size() > 0) gltf.add("textures", textures);
            if(images.size() > 0) gltf.add("images", images);
            if(samplers.size() > 0) gltf.add("samplers", samplers);
            gltf.add("accessors", accessors);
            gltf.add("bufferViews", bufferViews);
            gltf.add("buffers", buffers);

            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            byte[] jsonBytes = gson.toJson(gltf).getBytes(StandardCharsets.UTF_8);
            int jsonPad = (4 - (jsonBytes.length % 4)) % 4;
            int binPad = (4 - (bin.size() % 4)) % 4;

            int totalLen = 12 + 8 + jsonBytes.length + jsonPad + 8 + bin.size() + binPad;
            ByteBuffer header = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
            header.putInt(0x46546C67); // magic glTF
            header.putInt(2); // version 2
            header.putInt(totalLen);

            ByteBuffer jsonHeader = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            jsonHeader.putInt(jsonBytes.length + jsonPad);
            jsonHeader.putInt(0x4E4F534A); // JSON

            ByteBuffer binHeader = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            binHeader.putInt(bin.size() + binPad);
            binHeader.putInt(0x004E4942); // BIN

            File outFile = new File(folder, "full_model.glb");
            try(FileOutputStream fos = new FileOutputStream(outFile)) {
                fos.write(header.array());
                fos.write(jsonHeader.array());
                fos.write(jsonBytes);
                for(int i = 0; i < jsonPad; i++) fos.write(0x20);
                fos.write(binHeader.array());
                fos.write(bin.toByteArray());
                for(int i = 0; i < binPad; i++) fos.write(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int append(ByteArrayOutputStream bin, byte[] data) throws IOException {
        int offset = bin.size();
        bin.write(data);
        int pad = (4 - (data.length % 4)) % 4;
        for(int i = 0; i < pad; i++) {
            bin.write(0);
        }
        return offset;
    }
}
