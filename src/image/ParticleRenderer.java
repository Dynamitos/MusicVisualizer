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
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import engine.math.Vector2f;
import engine.model.RawModel;
import engine.renderEngine.Loader;
import engine.toolbox.Maths;
import org.lwjgl.BufferUtils;

import engine.math.Matrix4f;
import engine.math.Vector3f;
import engine.math.Vector4f;
import engine.renderEngine.DisplayManager;

public class ParticleRenderer extends Thread {
	private int vboParticle;
	private final int VERTEX_SIZE = 3+3+2+1;
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
	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 10000f;
	private Loader loader;
	private int atlasTexture;
	private final int WIDTH = 2048;
	private final int NUM_PER_ROW = 8;
	private final float[] vertices = {-0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f};
	private RawModel particleModel;

	private static class Particle {
		Vector3f position;/*1*/
		Vector3f rotation;/*2*/
		Vector2f texCoords;/*3*/
		float scale;/*4*/
		Vector3f speed;
		float life;
	}

	public ParticleRenderer(Loader loader) {
		shader = new ParticleShader();
		particles = new Particle[MAX_PARTICLES];
        particleBuffer = BufferUtils.createFloatBuffer(MAX_PARTICLES * VERTEX_SIZE);
        particleData = new float[MAX_PARTICLES * VERTEX_SIZE];
        viewMatrix = new Matrix4f();

        position = new Vector3f(0, 0, -10);
        center = new Vector3f(0, 0, 0);
        up = new Vector3f(0, 1, 0);

        particleModel = loader.loadToVAO(vertices, 2);

        glBindVertexArray(particleModel.getVaoID());


		vboParticle = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboParticle);

		glBufferData(GL_ARRAY_BUFFER, particleBuffer, GL_STREAM_DRAW);

		glVertexAttribPointer(1, 3, GL_FLOAT, false, VERTEX_SIZE*4, 0*4);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, VERTEX_SIZE*4, 3*4);
		glVertexAttribPointer(3, 2, GL_FLOAT, false, VERTEX_SIZE*4, 6*4);
		glVertexAttribPointer(4, 1, GL_FLOAT, false, VERTEX_SIZE*4, 8*4);

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
		shader.loadWidth(WIDTH);
		shader.stop();

		this.loader = loader;
		atlasTexture = loader.loadTexture("/tex/snow.png");

		glCullFace(GL_NONE);
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

	public void render(float intensity) {
		shader.start();

        Maths.lookAt(viewMatrix, position, center, up);
        shader.loadViewMatrix(viewMatrix);

		int numParticles = (int) (500 * intensity * DisplayManager.getFrameTimeSeconds());
		for (int i = 0; i < numParticles; i++) {
			int index = findUnusedParticle();
			if (particles[index] == null)
				particles[index] = new Particle();
			particles[index].position = new Vector3f(0, 0, 0);
			particles[index].speed = new Vector3f((float) (Math.random() * 2) - 1, (float) (Math.random() * 2) - 1, 0)
					.normalize();
			particles[index].rotation = new Vector3f(0, 0, 0);
			particles[index].texCoords = new Vector2f(0, 0);
			particles[index].scale = 0.01f;
			particles[index].life = 2;
		}


		int length = 0;
		for (int i = 0; i < MAX_PARTICLES; i++) {
			if (particles[i] != null) {
				if (particles[i].life > 0) {
                    particles[i].position = particles[i].position.add(particles[i].speed.multiply(0.1f));
                    particles[i].rotation.z += DisplayManager.getFrameTimeSeconds();

					particleData[length * VERTEX_SIZE + 0] = particles[i].position.x;
					particleData[length * VERTEX_SIZE + 1] = particles[i].position.y;
					particleData[length * VERTEX_SIZE + 2] = particles[i].position.z;
					particleData[length * VERTEX_SIZE + 3] = particles[i].rotation.x;
					particleData[length * VERTEX_SIZE + 4] = particles[i].rotation.y;
					particleData[length * VERTEX_SIZE + 5] = particles[i].rotation.z;
					particleData[length * VERTEX_SIZE + 6] = particles[i].texCoords.x;
					particleData[length * VERTEX_SIZE + 7] = particles[i].texCoords.y;
					particleData[length * VERTEX_SIZE + 8] = particles[i].scale;
					length++;

					particles[i].life -= DisplayManager.getFrameTimeSeconds();
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
