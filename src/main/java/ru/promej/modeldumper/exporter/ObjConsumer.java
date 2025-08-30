package ru.promej.modeldumper.exporter;

import net.minecraft.client.render.VertexConsumer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;



public class ObjConsumer implements VertexConsumer {

    private final List<VertexData> vertexData = new ArrayList<>();

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        // Unused for the batched vertex method but required by the interface
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer normal(float f, float g, float h) {
        return this;
    }

    @Override
    public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        vertexData.add(new VertexData(x, y, z, u, v, color));
    }


    public void writeData(File location, List<String> textures) throws IOException {

        if(location.exists()) {
            location.delete();
        }

        String name = location.getName();
        String base = name.substring(0, name.lastIndexOf('.'));
        File mtl = new File(location.getParentFile(), base + ".mtl");

        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(location)))){
            if(!textures.isEmpty()) {
                out.write("mtllib " + mtl.getName() + "\n");
                String material = textures.get(0);
                material = material.substring(0, material.lastIndexOf('.'));
                out.write("usemtl " + material + "\n");
            }
            for(VertexData vertex : vertexData) {
                out.write("v " + vertex.x + " " + vertex.y + " " + vertex.z + "\n");
            }
            for(VertexData vertex : vertexData) {
                out.write("vt " + vertex.u + " " + (1f-vertex.v) + "\n");
            }
            for(int i = 1; i <= vertexData.size(); i+=4) {
                out.write("f " + i + "/" + i + " " + (i+1) + "/"+ (i+1) + " " + (i+2) + "/"+ (i+2) +" " + (i+3) + "/"+ (i+3) +"\n");
            }
        }

        if(!textures.isEmpty()) {
            try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(mtl)))) {
                for(String tex : textures) {
                    String material = tex.substring(0, tex.lastIndexOf('.'));
                    out.write("newmtl " + material + "\n");
                    out.write("map_Kd " + tex + "\n\n");
                }
            }
        }
    }

    public List<VertexData> getVertexData() {
        return vertexData;
    }

    public static record VertexData(double x, double y, double z, float u, float v, int color) {}

}