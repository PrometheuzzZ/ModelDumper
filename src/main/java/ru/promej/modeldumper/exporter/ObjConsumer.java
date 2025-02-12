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

    private double x,y,z;
    private float u,v;
    private int color;

    private List<VertexData> vertexData = new ArrayList<>();

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }


    @Override
    public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        this.vertex(x, y, z);
        this.color(color);
        this.texture(u, v);
        this.overlay(overlay);
        this.light(light);
        this.normal(normalX, normalY, normalZ);
        vertexData.add(new VertexData(x, y, z, u, v, color));
    }


    @Override
    public VertexConsumer color(int i, int j, int k, int l) {
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        this.u = u;
        this.v = v;
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


    public void writeData(File location) throws IOException {

        if(location.exists()) {
            location.delete();
        }

        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(location)))){
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
    }

    private record VertexData(double x, double y, double z, float u, float v, int color) {}

}