package image;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.nio.FloatBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import engine.math.Vector2f;
import engine.model.RawModel;
import engine.renderEngine.Loader;
import engine.toolbox.Maths;
import org.lwjgl.BufferUtils;

import engine.math.Matrix4f;
import engine.math.Vector3f;
import engine.renderEngine.DisplayManager;
import sun.nio.ch.ThreadPool;

public class ParticleRenderer extends Thread {
    private int vboParticle;
    private final int VERTEX_SIZE = 3 + 3 + 2 + 1;
    private Particle[] particles;
    private FloatBuffer particleBuffer;
    private float[] particleData;
    private ParticleShader shader;
    private int maxParticles;
    private static final int MAX_PARTICLES = 2000;
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Vector3f position;
    private Vector3f center;
    private Vector3f up;

    private ThreadPoolExecutor executor;

    private Wind wind;
    private PerlinNoise perlinNoise;
    private Vector3f attractor;

    private static final float FOV = 70;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 10000f;
    private Loader loader;
    private int atlasTexture;
    private final float[] vertices = {-0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f};
    private RawModel particleModel;

    public ParticleRenderer(Loader loader) {
        shader = new ParticleShader();
        particles = new Particle[MAX_PARTICLES];
        particleBuffer = BufferUtils.createFloatBuffer(MAX_PARTICLES * VERTEX_SIZE);
        particleData = new float[MAX_PARTICLES * VERTEX_SIZE];
        viewMatrix = new Matrix4f();

        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(16);

        position = new Vector3f(0, 0, -10);
        center = new Vector3f(0, 0, 0);
        up = new Vector3f(0, 1, 0);

        perlinNoise = new PerlinNoise(20);
        wind = new Wind(new Vector3f(-20, 0, 0), new Vector3f(20, 0, 0));
        attractor = new Vector3f(0, 1, 0);

        particleModel = loader.loadToVAO(vertices, 2);

        glBindVertexArray(particleModel.getVaoID());

        vboParticle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboParticle);

        glBufferData(GL_ARRAY_BUFFER, particleBuffer, GL_STREAM_DRAW);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, VERTEX_SIZE * 4, 0 * 4);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, VERTEX_SIZE * 4, 3 * 4);
        glVertexAttribPointer(3, 2, GL_FLOAT, false, VERTEX_SIZE * 4, 6 * 4);
        glVertexAttribPointer(4, 1, GL_FLOAT, false, VERTEX_SIZE * 4, 8 * 4);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);

        glVertexAttribDivisor(0, 0);
        glVertexAttribDivisor(1, 1);
        glVertexAttribDivisor(2, 1);
        glVertexAttribDivisor(3, 1);
        glVertexAttribDivisor(4, 1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        createProjectionMatrix();
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.loadTexture();
        shader.stop();

        this.loader = loader;
        atlasTexture = loader.loadTexture("/tex/particle.png");

    }

    private int lastUsedParticle = 0;

    public int findUnusedParticle() {
        for (int i = lastUsedParticle; i < MAX_PARTICLES; i++) {
            if (particles[i] == null) {
                particles[i] = new Particle();
            }
            if (particles[i].life <= 0) {
                lastUsedParticle = i;
                return i;
            }
        }
        for (int i = 0; i < lastUsedParticle; i++) {
            if (particles[i].life <= 0) {
                lastUsedParticle = i;
                return i;
            }
        }

        return 0;
    }
    private float counter = 0;
    public void render(float intensity) {
        shader.start();

        float frameTime = DisplayManager.getFrameTimeSeconds();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        Maths.lookAt(viewMatrix, position, center, up);
        shader.loadViewMatrix(viewMatrix);
        attractor.x += random(-5 * frameTime, 5 * frameTime);
        attractor.y += random(-5 * frameTime, 5 * frameTime);
        attractor = attractor.normalize();

        counter+=frameTime*10;

        int numParticles = (int) counter;

        counter -= numParticles;

        for (int i = 0; i < numParticles; i++) {
            int index = findUnusedParticle();
            Particle p = particles[index];
            if (p == null)
                p = new Particle();
            p.position = new Vector3f(random(-5, 5), -10, random(-5, 5));
            p.speed = new Vector3f(random(-0.5f, 0.5f), 2, random(-0.5f, 0.5f));
            p.rotation = new Vector3f(0, 0, 0);
            p.dimensions = new Vector2f(1, 1);
            p.scale = 0.1f;
            p.life = 10f;
        }

        int length = 0;
        for (int i = 0; i < MAX_PARTICLES; i++) {
            Particle p = particles[i];
            if (p != null) {
                if (p.life > 0) {
                    final int index = length;
                    executor.submit(() ->
                    {
                        p.position = p.position.add(p.speed.scale(frameTime));
                        p.rotation = p.rotation.add(p.speed.multiply(frameTime));

                        p.speed = p.speed.add(perlinNoise.perlin(p.position.x, p.position.y, p.position.z).scale(0.1f));

                        particleData[index * VERTEX_SIZE + 0] = p.position.x;
                        particleData[index * VERTEX_SIZE + 1] = p.position.y;
                        particleData[index * VERTEX_SIZE + 2] = p.position.z;
                        particleData[index * VERTEX_SIZE + 3] = p.rotation.x;
                        particleData[index * VERTEX_SIZE + 4] = p.rotation.y;
                        particleData[index * VERTEX_SIZE + 5] = p.rotation.z;
                        particleData[index * VERTEX_SIZE + 6] = p.dimensions.x;
                        particleData[index * VERTEX_SIZE + 7] = p.dimensions.y;
                        particleData[index * VERTEX_SIZE + 8] = p.scale;

                        p.life -= frameTime;
                    });
                    length++;
                }
            }
        }
        glBindVertexArray(particleModel.getVaoID());

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlasTexture);

        glBindBuffer(GL_ARRAY_BUFFER, vboParticle);

        particleBuffer.put(particleData);
        particleBuffer.flip();
        glBufferSubData(GL_ARRAY_BUFFER, 0, particleBuffer);

        glDrawArraysInstanced(GL_TRIANGLE_FAN, 0, 4, length);

        glBindVertexArray(0);
        shader.stop();
        if (length >= maxParticles) {
            maxParticles = length;
        }
        glDisable(GL_BLEND);
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min)) + min;
    }

    private void createProjectionMatrix() {
        float aspectRatio = (float) DisplayManager.WIDTH / (float) DisplayManager.HEIGHT;
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
        float x_scale = y_scale / aspectRatio;
        float frustrum_length = FAR_PLANE - NEAR_PLANE;

        projectionMatrix = new Matrix4f();
        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustrum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustrum_length);
        projectionMatrix.m33 = 0;

    }

    public void terminate() {
        executor.shutdown();
    }
    @Override
    public void run() {/*
     * while (false) { if (colorFlagx) { color.x +=
     * DisplayManager.getFrameTimeSeconds() / 10; if
     * (color.x >= 1) colorFlagx = false; } else { color.x
     * -= DisplayManager.getFrameTimeSeconds() / 10; if
     * (color.x <= 0) colorFlagx = true; } if (colorFlagy) {
     * color.y += DisplayManager.getFrameTimeSeconds() / 10;
     * if (color.y >= 1) colorFlagy = false; } else {
     * color.y -= DisplayManager.getFrameTimeSeconds() / 10;
     * if (color.y <= 0) colorFlagy = true; } if
     * (colorFlagz) { color.z +=
     * DisplayManager.getFrameTimeSeconds() / 10; if
     * (color.z >= 1) colorFlagz = false; } else { color.z
     * -= DisplayManager.getFrameTimeSeconds() / 10; if
     * (color.z <= 0) colorFlagz = true; } try {
     * sleep((long)
     * (DisplayManager.getFrameTimeSeconds()*1000)); } catch
     * (InterruptedException e) { interrupt(); } }
     */
    }

}