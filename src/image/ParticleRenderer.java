package image;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glPointSize;
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

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import engine.math.Matrix4f;
import engine.math.Vector3f;
import engine.math.Vector4f;
import engine.renderEngine.DisplayManager;

public class ParticleRenderer extends Thread {
	private int vaoID;
	private int vboID;
	private Particle[] particles;
	private float[] positionBuffer;
	private ParticleShader shader;
	private int maxParticles;
	private static final int MAX_PARTICLES = 100000;
	private Vector4f color;
	/*
	 * private boolean colorFlagx = false; private boolean colorFlagy = false;
	 * private boolean colorFlagz = false;
	 */
	private Matrix4f projectionMatrix;
	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 10000f;

	private static class Particle {
		Vector3f position;
		@SuppressWarnings("unused")
		private Vector3f speed;
		float life;
	}

	public ParticleRenderer() {
		shader = new ParticleShader();
		color = new Vector4f(1f, 0f, 0f, 1);
		particles = new Particle[MAX_PARTICLES];
		positionBuffer = new float[MAX_PARTICLES * 3];
		vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, BufferUtils.createFloatBuffer(MAX_PARTICLES * 3), GL_STREAM_DRAW);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		createProjectionMatrix();
		shader.loadProjectionMatrix(projectionMatrix);
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
		shader.loadColor(color);
		int numParticles = (int) (50000 * intensity * DisplayManager.getFrameTimeSeconds());
		for (int i = 0; i < numParticles; i++) {
			int index = findUnusedParticle();
			if (particles[index] == null)
				particles[index] = new Particle();
			particles[index].position = new Vector3f(0, 0, -1);
			particles[index].speed = new Vector3f((float) (Math.random() * 2) - 1, (float) (Math.random() * 2) - 1, 0)
					.normalize().scale(1000);
			particles[index].life = 5;
		}
		int length = 0;
		for (int i = 0; i < MAX_PARTICLES; i++) {
			if (particles[i] != null) {
				if (particles[i].life > 0) {
					// particles[i].speed = particles[i].speed.scale(0.1f);
					// particles[i].position =
					// particles[i].speed.scale(intensity);
					particles[i].life -= DisplayManager.getFrameTimeSeconds();
					positionBuffer[length * 3 + 0] = particles[i].position.x;
					positionBuffer[length * 3 + 1] = particles[i].position.y;
					positionBuffer[length * 3 + 2] = particles[i].position.z;
					length++;
				}
			}
		}
		glPointSize(20);
		glBindVertexArray(vaoID);
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_PARTICLES * 3);
		buffer.put(positionBuffer);
		buffer.flip();
		glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
		glEnableVertexAttribArray(0);
		glDrawArrays(GL_POINTS, 0, length);
		glDisableVertexAttribArray(0);
		glBindVertexArray(0);
		shader.stop();
		if (length >= maxParticles) {
			maxParticles = length;
			System.out.println(maxParticles);
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
